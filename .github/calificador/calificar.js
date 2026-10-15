// Calificador de Sillón: compila el proyecto del estudiante, corre sus tests JUnit,
// lo arranca con MySQL, un simulador de Stripe y una IA simulada, corre la colección
// oficial de Postman y revisa el repositorio. Calcula la nota de 0.0 a 5.0.
//
// En GitHub Actions lo corre el workflow "Calificar proyecto" en cada push del Pull Request.
// En local (solo la profe): node .github/calificador/calificar.js
//   Necesita MySQL, Java 17, Maven, Python 3, Newman y stripe-mock en el puerto 12111.

const fs = require("node:fs");
const os = require("node:os");
const path = require("node:path");
const http = require("node:http");
const { spawn, spawnSync } = require("node:child_process");

const RAIZ = path.join(__dirname, "..", "..");
const CONFIG = JSON.parse(fs.readFileSync(path.join(RAIZ, ".github", "calificacion.json"), "utf8"));
const PATRON = new RegExp(CONFIG.patronTest, "gm");
const EN_GITHUB = process.env.GITHUB_ACTIONS === "true";
const MAX_FALLOS_EN_CONSOLA = 3;
const PUERTO_APP = 8080;
const PUERTO_IA = 9999;
const SECRETO_WEBHOOK = "whsec_calificador_sillon";

const ENTORNO_APP = {
  ...process.env,
  PORT: String(PUERTO_APP),
  SPRING_DATASOURCE_URL: process.env.CALIFICADOR_DB_URL || "jdbc:mysql://127.0.0.1:3306/sillon_calificador?createDatabaseIfNotExist=true",
  SPRING_DATASOURCE_USERNAME: process.env.CALIFICADOR_DB_USUARIO || "root",
  SPRING_DATASOURCE_PASSWORD: process.env.CALIFICADOR_DB_CLAVE || "root",
  JWT_SECRETO: "secreto-del-calificador-con-mas-de-32-caracteres",
  URL_PUBLICA: `http://localhost:${PUERTO_APP}`,
  STRIPE_URL_API: process.env.CALIFICADOR_STRIPE_URL || "http://localhost:12111",
  STRIPE_CLAVE_SECRETA: "sk_test_calificador",
  STRIPE_SECRETO_WEBHOOK: SECRETO_WEBHOOK,
  STRIPE_PRECIO_PRO: "price_pro_calificador",
  STRIPE_PRECIO_CREDITOS: "price_creditos_calificador",
  GEMINI_LLAVE: "llave-calificador",
  SILLON_IA_OPENAI_URL: `http://localhost:${PUERTO_IA}`,
  SILLON_IA_CLAUDE_URL: "http://localhost:9",
  TZ: "UTC",
};

const procesos = [];

// ---------- utilidades ----------

function existe(ruta) {
  return fs.existsSync(path.join(RAIZ, ruta));
}

function leer(ruta) {
  try {
    return fs.readFileSync(path.join(RAIZ, ruta), "utf8");
  } catch {
    return "";
  }
}

function totalDe(ejercicio) {
  return (fs.readFileSync(path.join(RAIZ, ejercicio.test), "utf8").match(PATRON) || []).length;
}

function comandoMaven() {
  const mvn = spawnSync(process.platform === "win32" ? "mvn.cmd" : "mvn", ["-v"], { encoding: "utf8", shell: process.platform === "win32" });
  if (mvn.status === 0) return process.platform === "win32" ? "mvn.cmd" : "mvn";
  return process.platform === "win32" ? "mvnw.cmd" : "./mvnw";
}

function maven(args, minutos, entorno) {
  return spawnSync(comandoMaven(), ["-B", "-ntp", ...args], {
    cwd: RAIZ, encoding: "utf8", timeout: minutos * 60000, env: entorno || process.env,
    maxBuffer: 64 * 1024 * 1024, shell: process.platform === "win32",
  });
}

function primerErrorMaven(salida) {
  const lineas = (salida || "").split(/\r?\n/);
  const compilacion = lineas.find((l) => /\[ERROR\].*\.java:\[\d+/.test(l));
  if (compilacion) {
    const m = compilacion.match(/(src\/[^\s:]+\.java):\[(\d+)[,\d]*\]\s*(.*)$/);
    if (m) return `Error de compilación en ${m[1]}, línea ${m[2]}: ${m[3]}`;
    return compilacion.replace(/^\[ERROR\]\s*/, "");
  }
  if (lineas.some((l) => /Non-resolvable parent POM|Could not find artifact|Could not resolve dependencies/.test(l))) {
    return "Maven no pudo descargar una dependencia o la versión del padre: revisa las versiones del pom.xml (¿quedó un .RELEASE?)";
  }
  const error = lineas.find((l) => /^\[ERROR\]/.test(l) && !/^\[ERROR\]\s*$/.test(l) && !/Re-run Maven|For more information|Help 1|To see the full/.test(l));
  return error ? error.replace(/^\[ERROR\]\s*/, "") : "Maven terminó con error";
}

function peticionHttp(url, msTimeout) {
  return new Promise((resolve) => {
    const req = http.get(url, { timeout: msTimeout }, (res) => {
      res.resume();
      resolve(res.statusCode);
    });
    req.on("timeout", () => { req.destroy(); resolve(0); });
    req.on("error", () => resolve(0));
  });
}

const esperar = (ms) => new Promise((r) => setTimeout(r, ms));

function lanzar(comando, args, entorno, archivoLog) {
  const salida = fs.openSync(archivoLog, "w");
  const proceso = spawn(comando, args, { cwd: RAIZ, env: entorno, stdio: ["ignore", salida, salida], detached: process.platform !== "win32" });
  procesos.push(proceso);
  return proceso;
}

function detenerTodo() {
  for (const p of procesos) {
    try {
      if (process.platform !== "win32") process.kill(-p.pid, "SIGKILL");
      else p.kill("SIGKILL");
    } catch {
      // ya terminó
    }
  }
}

function traducirAsercion(mensaje) {
  const estado = mensaje.match(/expected response to have status code (\d+) but got (\d+)/);
  if (estado) return `Se esperaba HTTP ${estado[1]} y tu API respondió ${estado[2]}`;
  if (/Unexpected token|JSONError|is not valid JSON/.test(mensaje)) return "La respuesta no es un JSON válido";
  if (/Cannot read properties of (undefined|null)/.test(mensaje)) {
    const prop = (mensaje.match(/reading '([^']+)'/) || [])[1];
    return prop ? `La respuesta no trae el campo esperado (se intentó leer "${prop}"): revisa los nombres del contrato` : "La respuesta no trae los campos del contrato";
  }
  if (/^expected undefined/.test(mensaje)) return "La respuesta no trae ese campo: revisa el nombre exacto en el contrato";
  const igual = mensaje.match(/^expected (.+?) to (?:deeply )?equal (.+)$/);
  if (igual) return `Se esperaba ${igual[2]} y llegó ${igual[1]}`;
  const incluye = mensaje.match(/^expected (.+?) to include (.+)$/);
  if (incluye) return `Se esperaba que la respuesta incluyera ${incluye[2]}`;
  const noIncluye = mensaje.match(/^expected (.+?) to not include (.+)$/);
  if (noIncluye) return `La respuesta no debería incluir ${noIncluye[2]}`;
  if (/not to match/.test(mensaje)) return "La respuesta contiene un texto que no debería: " + mensaje.replace(/^expected /, "").replace(/ not to match.*$/, "");
  if (/to match/.test(mensaje)) return "El valor no tiene el formato esperado: " + mensaje.replace(/^expected /, "");
  if (/ECONNREFUSED|ECONNRESET|ETIMEDOUT|ESOCKETTIMEDOUT/.test(mensaje)) return "Tu API no respondió (¿se cayó o tardó demasiado?)";
  return mensaje;
}

// ---------- Postman / Newman ----------

function cargarCarpeta(ejercicio) {
  const carpeta = JSON.parse(fs.readFileSync(path.join(RAIZ, ejercicio.test), "utf8"));
  carpeta.item.forEach((item, i) => { item.id = `${ejercicio.id}-${i}`; });
  return carpeta;
}

function correrNewman(ejercicios, baseUrl, msPorPeticion) {
  const coleccion = {
    info: { name: "Sillón · colección oficial", schema: "https://schema.getpostman.com/json/collection/v2.1.0/collection.json" },
    item: ejercicios.map(cargarCarpeta),
    variable: [],
  };
  const temp = fs.mkdtempSync(path.join(os.tmpdir(), "sillon-newman-"));
  const archivoColeccion = path.join(temp, "coleccion.json");
  const archivoReporte = path.join(temp, "reporte.json");
  fs.writeFileSync(archivoColeccion, JSON.stringify(coleccion));
  const args = ["run", archivoColeccion, "--env-var", `baseUrl=${baseUrl}`, "--env-var", `secretoWebhook=${SECRETO_WEBHOOK}`,
    "--reporters", "json", "--reporter-json-export", archivoReporte, "--timeout-request", String(msPorPeticion), "--delay-request", "50"];
  const binario = process.env.NEWMAN_BIN || "newman";
  spawnSync(binario, args, { encoding: "utf8", timeout: 15 * 60000, shell: process.platform === "win32" });

  const resultado = {};
  for (const ej of ejercicios) resultado[ej.id] = { pasados: 0, fallidos: [] };
  try {
    const reporte = JSON.parse(fs.readFileSync(archivoReporte, "utf8"));
    for (const ejecucion of reporte.run.executions) {
      const id = String(ejecucion.item.id || "").split("-")[0];
      if (!resultado[id]) continue;
      if (ejecucion.requestError) {
        resultado[id].fallidos.push({ nombre: ejecucion.item.name, mensaje: traducirAsercion(ejecucion.requestError.message || "") });
      }
      for (const a of ejecucion.assertions || []) {
        if (a.error) resultado[id].fallidos.push({ nombre: `${ejecucion.item.name} · ${a.assertion}`, mensaje: traducirAsercion(a.error.message || "") });
        else resultado[id].pasados++;
      }
    }
  } catch {
    for (const ej of ejercicios) resultado[ej.id].aviso = "No se pudo correr la colección oficial (Newman no generó reporte)";
  } finally {
    fs.rmSync(temp, { recursive: true, force: true });
  }
  return resultado;
}

// ---------- pasos ----------

function revisarRepositorio() {
  const r = {};
  r.spec = leer("SPEC.md").trim().length > 500;
  r.agents = leer("AGENTS.md").trim().length > 300;
  const filasBitacora = leer("BITACORA-IA.md").split(/\r?\n/).filter((l) => /^\|\s*\d+\s*\|/.test(l) && l.split("|").slice(2).some((c) => c.trim().length > 2));
  r.bitacora = filasBitacora.length >= 5;

  let peticiones = 0;
  let conPruebas = 0;
  const contar = (items) => {
    for (const it of items || []) {
      if (it.item) contar(it.item);
      if (it.request) {
        peticiones++;
        const pruebas = (it.event || []).filter((e) => e.listen === "test").flatMap((e) => (e.script && e.script.exec) || []);
        if ([].concat(pruebas).join("\n").includes("pm.test(")) conPruebas++;
      }
    }
  };
  const archivosPostman = existe("postman") ? fs.readdirSync(path.join(RAIZ, "postman")) : [];
  for (const a of archivosPostman.filter((f) => f.endsWith(".postman_collection.json"))) {
    try {
      contar(JSON.parse(leer(path.join("postman", a))).item);
    } catch {
      // colección inválida: no suma
    }
  }
  r.coleccion = peticiones >= 50;
  r["coleccion-pruebas"] = conPruebas >= 40;
  r.entornos = archivosPostman.filter((f) => f.endsWith(".postman_environment.json")).length >= 2;
  r.dockerfile = /^\s*FROM\s+/im.test(leer("Dockerfile"));
  r["plantilla-secretos"] = existe("secrets.example.properties");
  const hayProyecto = existe("pom.xml");
  r["sin-secretos"] = hayProyecto && !existe("secrets.properties");

  const patronesLlave = [/sk_(test|live)_[A-Za-z0-9]{20,}/, /rk_(test|live)_[A-Za-z0-9]{20,}/, /whsec_[A-Za-z0-9]{20,}/, /AIza[0-9A-Za-z_-]{30,}/];
  let llaves = false;
  const recorrer = (dir) => {
    if (!fs.existsSync(dir)) return;
    for (const nombre of fs.readdirSync(dir)) {
      const ruta = path.join(dir, nombre);
      if (fs.statSync(ruta).isDirectory()) recorrer(ruta);
      else if (/\.(java|properties|txt|json|md|yml|yaml)$/.test(nombre) && patronesLlave.some((p) => p.test(fs.readFileSync(ruta, "utf8")))) llaves = true;
    }
  };
  recorrer(path.join(RAIZ, "src"));
  recorrer(path.join(RAIZ, "postman"));
  for (const a of ["README.md", "secrets.example.properties", "secrets.properties"]) {
    if (patronesLlave.some((p) => p.test(leer(a)))) llaves = true;
  }
  r["sin-llaves"] = hayProyecto && !llaves;

  const carpetaPrompts = path.join(RAIZ, "src", "main", "resources", "prompts");
  r.prompts = fs.existsSync(carpetaPrompts) && fs.readdirSync(carpetaPrompts).filter((f) => f.endsWith(".txt")).length >= 2;
  r["readme-render"] = Boolean(urlProduccion());
  return r;
}

function comandoPython() {
  for (const c of ["python3", "python", "py"]) {
    if (spawnSync(c, ["--version"], { encoding: "utf8", shell: process.platform === "win32" }).status === 0) return c;
  }
  return "python3";
}

function urlProduccion() {
  if (process.env.CALIFICADOR_PRODUCCION_LOCAL === "1") return `http://localhost:${PUERTO_APP}`;
  if (process.env.SILLON_URL_PRODUCCION) return process.env.SILLON_URL_PRODUCCION.replace(/\/+$/, "");
  const urls = leer("README.md").match(/https:\/\/[a-z0-9][a-z0-9-]*\.onrender\.com/gi) || [];
  return urls.find((u) => !/TU-APP/i.test(u)) || "";
}

function revisarTests(compilo) {
  const r = { compila: compilo };
  if (!compilo) return { r, aviso: "" };
  const salida = maven(["test"], 12, ENTORNO_APP);
  const carpeta = path.join(RAIZ, "target", "surefire-reports");
  let total = 0;
  let fallas = 0;
  if (fs.existsSync(carpeta)) {
    for (const a of fs.readdirSync(carpeta).filter((f) => /^TEST-.*\.xml$/.test(f))) {
      const xml = fs.readFileSync(path.join(carpeta, a), "utf8");
      const m = xml.match(/<testsuite[^>]*>/);
      if (!m) continue;
      const atributo = (n) => Number((m[0].match(new RegExp(`\\b${n}="(\\d+)"`)) || [])[1] || 0);
      total += atributo("tests") - atributo("skipped");
      fallas += atributo("failures") + atributo("errors");
    }
  }
  r["tests-corren"] = total > 0;
  r["tests-verdes"] = total > 0 && fallas === 0 && salida.status === 0;
  r["tests-15"] = total >= 15;
  r["tests-30"] = total >= 30;

  const fuentes = [];
  const recorrer = (dir) => {
    if (!fs.existsSync(dir)) return;
    for (const n of fs.readdirSync(dir)) {
      const ruta = path.join(dir, n);
      if (fs.statSync(ruta).isDirectory()) recorrer(ruta);
      else if (n.endsWith(".java")) fuentes.push({ nombre: n, codigo: fs.readFileSync(ruta, "utf8") });
    }
  };
  recorrer(path.join(RAIZ, "src", "test"));
  const conTests = fuentes.filter((f) => /@(Test|ParameterizedTest)\b/.test(f.codigo));
  const hay = (patron) => conTests.some((f) => patron.test(f.nombre) || patron.test(f.codigo));
  r["tests-agenda"] = hay(/Cita|Agenda/);
  r["tests-webhook"] = hay(/Webhook/);
  r["tests-chatbot"] = hay(/Chatbot/);

  let aviso = "";
  if (salida.error && salida.error.code === "ETIMEDOUT") aviso = "Tiempo agotado corriendo tus tests: ¿algún test se queda esperando?";
  else if (total === 0) aviso = "No se encontraron resultados de tests: " + primerErrorMaven(salida.stdout);
  else if (fallas > 0) aviso = `${fallas} de tus ${total} tests fallan (o tienen error)`;
  return { r, aviso, total };
}

async function arrancarApp() {
  const jar = (fs.existsSync(path.join(RAIZ, "target")) ? fs.readdirSync(path.join(RAIZ, "target")) : [])
    .find((f) => f.endsWith(".jar") && !f.endsWith("-plain.jar"));
  if (!jar) return "No se encontró el .jar después de compilar";
  const log = path.join(os.tmpdir(), "sillon-app.log");
  const proceso = lanzar("java", ["-jar", path.join("target", jar)], ENTORNO_APP, log);
  for (let i = 0; i < 90; i++) {
    await esperar(2000);
    if (proceso.exitCode !== null) break;
    const estado = await peticionHttp(`http://localhost:${PUERTO_APP}/api/salon`, 3000);
    if (estado > 0) return "";
  }
  const texto = fs.existsSync(log) ? fs.readFileSync(log, "utf8") : "";
  const descripcion = texto.match(/Description:\s*\n+([^\n]+)/);
  if (descripcion) return `La aplicación no arrancó: ${descripcion[1].trim()}`;
  const causa = [...texto.matchAll(/Caused by: ([^\n]+)/g)].pop();
  if (causa) return `La aplicación no arrancó: ${causa[1].trim().slice(0, 220)}`;
  return "La aplicación no arrancó o no respondió en el puerto 8080";
}

// ---------- reporte ----------

function icono(r) {
  if (r.total > 0 && r.pasados === r.total) return "✅";
  return r.pasados > 0 ? "🟡" : "❌";
}

function imprimirEnConsola(resultados, resumen) {
  console.log(`\n${CONFIG.curso} · Resultado de tu proyecto\n`);
  for (const r of resultados) {
    console.log(`${icono(r)} ${r.id} ${r.titulo.padEnd(34)} ${r.pasados}/${r.total}`);
    if (r.aviso) console.log(`     ⚠️  ${r.aviso}`);
    for (const f of r.fallidos.slice(0, MAX_FALLOS_EN_CONSOLA)) {
      console.log(`     ✗ ${f.nombre}`);
      if (f.mensaje) console.log(`       → ${f.mensaje}`);
    }
    const ocultos = r.fallidos.length - MAX_FALLOS_EN_CONSOLA;
    if (ocultos > 0) console.log(`     … y ${ocultos} prueba(s) más por corregir`);
  }
  console.log(`\n📊 Pruebas aprobadas: ${resumen.pasados}/${resumen.total}`);
  console.log(`🎯 Nota: ${resumen.nota.toFixed(1)} / 5.0\n`);
}

function escribirResumenGithub(resultados, resumen) {
  const filas = resultados.map((r) => `| ${icono(r)} | ${r.id} · ${r.titulo} | ${r.pasados}/${r.total} |`);
  const detalles = resultados
    .filter((r) => r.fallidos.length || r.aviso)
    .map((r) => {
      const items = r.fallidos.map((f) => `- ✗ ${f.nombre}${f.mensaje ? ` → ${f.mensaje}` : ""}`);
      if (r.aviso) items.unshift(`- ⚠️ ${r.aviso}`);
      return `**${r.id} · ${r.titulo}**\n${items.join("\n")}`;
    });
  const markdown = [
    `## 🎯 Nota: ${resumen.nota.toFixed(1)} / 5.0`,
    `Pruebas aprobadas: **${resumen.pasados}/${resumen.total}**`,
    "",
    "| Estado | Criterio | Pruebas |",
    "|---|---|---|",
    ...filas,
    "",
    detalles.length ? "### Qué falta por corregir\n\n" + detalles.join("\n\n") : "### ¡Todo perfecto! 🎉",
  ].join("\n");
  if (process.env.GITHUB_STEP_SUMMARY) fs.appendFileSync(process.env.GITHUB_STEP_SUMMARY, markdown + "\n");
  const paraArtefacto = resultados.map(({ id, pasados, total }) => ({ id, pasados, total }));
  fs.writeFileSync(path.join(RAIZ, "resultado.json"), JSON.stringify({ ejercicios: paraArtefacto }));
}

function desdeChequeos(ejercicio, valores, aviso) {
  const criterio = JSON.parse(fs.readFileSync(path.join(RAIZ, ejercicio.test), "utf8"));
  const fallidos = criterio.chequeos.filter((c) => !valores[c.chequeo]).map((c) => ({ nombre: c.descripcion, mensaje: "" }));
  return { pasados: criterio.chequeos.length - fallidos.length, fallidos, aviso };
}

// ---------- principal ----------

async function principal() {
  const porId = Object.fromEntries(CONFIG.ejercicios.map((e) => [e.id, e]));
  const parciales = {};
  const coleccionLocal = ["01", "02", "03", "04", "05", "06"].map((id) => porId[id]);

  const hayPom = existe("pom.xml");
  const compilacion = hayPom ? maven(["-DskipTests", "package"], 10) : { status: 1, stdout: "" };
  const compilo = compilacion.status === 0;
  const avisoCompilacion = compilo ? "" : hayPom ? primerErrorMaven(compilacion.stdout)
    : "No encontré pom.xml en la raíz de tu fork: el proyecto va en la raíz, no dentro de una subcarpeta";

  const tests = revisarTests(compilo);
  parciales["07"] = desdeChequeos(porId["07"], tests.r, compilo ? tests.aviso : avisoCompilacion);
  parciales["08"] = desdeChequeos(porId["08"], revisarRepositorio(), "");

  let avisoApp = avisoCompilacion;
  if (compilo) {
    lanzar(comandoPython(), [path.join(__dirname, "ia_simulada.py"), String(PUERTO_IA)], process.env, path.join(os.tmpdir(), "sillon-ia.log"));
    avisoApp = await arrancarApp();
  }
  if (!avisoApp) {
    Object.assign(parciales, correrNewman(coleccionLocal, `http://localhost:${PUERTO_APP}`, 30000));
  } else {
    for (const ej of coleccionLocal) parciales[ej.id] = { pasados: 0, fallidos: [], aviso: avisoApp };
  }

  // Solo para la verificación local de la profe: "producción" es la misma app local
  const url = urlProduccion();
  if (process.env.CALIFICADOR_PRODUCCION_LOCAL === "1") {
    parciales["09"] = avisoApp ? { pasados: 0, fallidos: [], aviso: avisoApp } : correrNewman([porId["09"]], url, 30000)["09"];
    detenerTodo();
  } else if (url) {
    detenerTodo();
    await peticionHttp(`${url}/v3/api-docs`, 120000);
    parciales["09"] = correrNewman([porId["09"]], url, 120000)["09"];
  } else {
    detenerTodo();
    parciales["09"] = { pasados: 0, fallidos: [], aviso: "No encontré en tu README.md una URL de Render (https://…onrender.com)" };
  }

  const resultados = CONFIG.ejercicios.map((ej) => {
    const total = totalDe(ej);
    const p = parciales[ej.id] || { pasados: 0, fallidos: [] };
    return { id: ej.id, titulo: ej.titulo, total, pasados: Math.min(p.pasados, total), fallidos: p.fallidos, aviso: p.aviso || "" };
  });
  const pasados = resultados.reduce((s, r) => s + r.pasados, 0);
  const total = resultados.reduce((s, r) => s + r.total, 0);
  const resumen = { pasados, total, nota: total ? Math.round((pasados / total) * 50) / 10 : 0 };

  imprimirEnConsola(resultados, resumen);
  if (EN_GITHUB) escribirResumenGithub(resultados, resumen);
  process.exitCode = pasados === total ? 0 : 1;
}

process.on("exit", detenerTodo);
principal().catch((error) => {
  detenerTodo();
  console.log(`❌ El calificador falló: ${error.message}`);
  process.exitCode = 1;
});
