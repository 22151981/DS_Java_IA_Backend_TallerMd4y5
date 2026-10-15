# 💈 Sillón · Proyecto integrador Módulos 4 y 5

**Java AI Engineer · Dev Senior Code**

---

## ¿Qué es esto?

### El proyecto
Vas a construir **Sillón**: un SaaS de agenda para barberías y salones de belleza.
API en Java con Spring Boot, JWT, Stripe, IA, Swagger y despliegue en Render.

### Dónde está el enunciado
El enunciado completo (negocio, reglas, contrato de la API y paso a paso) está en el **PDF que te compartió la profe**.
Este README solo explica **cómo entregar y cómo se califica**.

### La calificación es automática
Construyes el proyecto en **tu fork** y abres **un solo Pull Request**.
Cada vez que haces `git push`, un robot califica tu proyecto y te deja la nota en el PR.
No tienes que esperar a nadie: corriges, subes y la nota se actualiza sola.

### ¿Qué es una prueba automática?
**Técnica:** código que llama a tu API, revisa la respuesta (código HTTP y datos) y dice si cumple o no lo esperado.

**En la vida real:** es el inspector de la revisión técnico-mecánica.
No le importa cómo arreglaste el carro: revisa frenos, luces y emisiones contra una lista, y aprueba o rechaza.

---

## Qué hace el calificador en cada push

### Los pasos
1. Compila tu proyecto con Maven.
2. Corre **tus** tests JUnit.
3. Arranca **tu** API con MySQL, un simulador oficial de Stripe y una **IA simulada** (sin costo y sin llaves).
4. Corre la **colección oficial** de Postman: más de 100 pruebas contra tu API.
5. Revisa tu repositorio (SPEC, bitácora, colección, Dockerfile, secretos…).
6. Prueba tu API desplegada en Render (la URL que pongas en tu README).

### La nota
`nota = 5 × pruebas aprobadas ÷ pruebas totales`, con un decimal.
Cada prueba vale lo mismo, así que los criterios con más reglas pesan más.

---

## Los 9 criterios

| # | Criterio | Pruebas | Peso aprox. |
|---|---|---|---|
| 01 | Registro, login y configuración | 14 | 10 % |
| 02 | Catálogo y equipo | 9 | 6 % |
| 03 | Agenda | 34 | 23 % |
| 04 | Aislamiento y roles | 7 | 5 % |
| 05 | IA: recordatorio y chatbot | 16 | 11 % |
| 06 | Pagos con Stripe (incluye webhooks firmados) | 29 | 20 % |
| 07 | Tests JUnit | 8 | 6 % |
| 08 | Repositorio y entrega | 12 | 8 % |
| 09 | Producción en Render | 16 | 11 % |

### Qué revisa el criterio 07
Que el proyecto compile, que tus tests corran y pasen, que haya al menos 15 y al menos 30, y que haya tests de agenda, del webhook y del chatbot.

### Qué revisa el criterio 08
SPEC.md, AGENTS.md, BITACORA-IA.md (5 entradas o más), colección propia en `postman/` (50 peticiones o más, 40 con pruebas), dos entornos, Dockerfile, `secrets.example.properties`, que **no** subiste `secrets.properties` ni llaves reales, 2 prompts y la URL de Render en el README.

---

## Lo que el calificador necesita de tu proyecto

### El proyecto va en la raíz
El `pom.xml` debe quedar en la raíz de tu fork, al lado de este README.
Si lo dejas dentro de una subcarpeta, el calificador no lo encuentra y la nota es 0.0.

### El contrato de la API
Rutas, nombres de campos y códigos HTTP **exactamente** como dice la sección 4 del enunciado.
Un campo con otro nombre hace fallar la prueba aunque tu lógica esté bien.

### Variables de entorno
El calificador (y Render) configuran tu aplicación con estas variables.
Tu `application.properties` debe leerlas, con un valor de relleno para que arranque sin ellas.

| Variable | Para qué |
|---|---|
| `PORT` | Puerto de la aplicación |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Base de datos |
| `JWT_SECRETO` | Secreto para firmar los JWT |
| `URL_PUBLICA` | URL de tu API (para volver de Stripe) |
| `STRIPE_URL_API` | Dirección de la API de Stripe (por defecto `https://api.stripe.com`) |
| `STRIPE_CLAVE_SECRETA`, `STRIPE_SECRETO_WEBHOOK` | Llaves de Stripe |
| `STRIPE_PRECIO_PRO`, `STRIPE_PRECIO_CREDITOS` | Ids de los precios |
| `GEMINI_LLAVE` | Llave de Gemini |
| `SILLON_IA_OPENAI_URL` | Dirección del proveedor con formato OpenAI (propiedad `sillon.ia.openai.url`) |

### ¿Por qué STRIPE_URL_API y SILLON_IA_OPENAI_URL?
Porque el calificador no usa el Stripe real ni el Gemini real: usa simuladores.
Que la dirección de un servicio externo sea configurable es una práctica profesional: así se prueba sin gastar dinero.

### La IA simulada
No razona: responde según lo que **tu prompt** le pida.
- Si tu prompt pide un JSON con `respuesta` y `escalar`, responde como chatbot.
- Si tu prompt pide un JSON con `mensaje`, responde como recordatorio, usando los datos verificados que Java puso en el prompt.

En el chatbot, a veces "promete" un cupón a propósito: tu regla en Java debe bloquearlo.

### Swagger
El calificador revisa que `/v3/api-docs` responda en tu URL de Render.

---

## Paso 1 · Fork

### ¿Qué es?
**Técnica:** una copia de este repositorio en tu cuenta de GitHub, conectada con el original.

**En la vida real:** una fotocopia del cuaderno de la profe: escribes en la tuya sin dañar el de ella.

### Cómo
1. Arriba a la derecha de esta página: **Fork**.
2. Deja el nombre como está y clic en **Create fork**.

### Cómo saber que quedó bien
Arriba a la izquierda dice **tu-usuario/este-repo** y, debajo, "forked from" el repo de la profe.

---

## Paso 2 · Clone

### Cómo
1. En **tu** fork: botón verde **Code** → copia la URL.
2. En la terminal de Cursor, en la carpeta donde guardas tus proyectos:

```bash
git clone URL-DE-TU-FORK
```

3. Abre la carpeta del repo en Cursor (**File → Open Folder**).

---

## Paso 3 · Construir el proyecto

### Dónde
Genera el proyecto en Spring Initializr y **copia su contenido** (pom.xml, src, mvnw…) en la raíz de tu fork.

### Las reglas de oro
- No toques la carpeta `.github/`.
- Nunca subas `secrets.properties` (va en tu `.gitignore`).
- Un commit por cada paso del enunciado que quede en verde.

### Tu propio README
Cuando escribas el README de tu proyecto, reemplaza este archivo. Esta guía sigue en el repo de la profe.

---

## Paso 4 · Probar en local

### Tus pruebas
- Tus tests: `./mvnw test` (en Windows PowerShell: `.\mvnw test`).
- Tu colección de Postman: **Run collection** con el entorno local.

### La nota oficial
La nota oficial solo se calcula en GitHub, porque necesita los simuladores.
Si tus tests y tu colección pasan, lo normal es que la nota oficial también.

---

## Paso 5 · Subir

```bash
git add .
git commit -m "Fase 4: agenda con reglas de horario y cruces"
git push
```

### Qué hace cada uno
- `git add .` mete los cambios en la caja.
- `git commit -m` cierra la caja y le pone etiqueta.
- `git push` envía la caja a tu fork en GitHub.

---

## Paso 6 · Pull Request (solo UNO)

### ¿Qué es?
**Técnica:** una solicitud para que los cambios de tu fork se revisen contra el repo de la profe.

**En la vida real:** radicar un documento en una ventanilla: queda con número y se puede consultar su estado.

### Cómo
1. En tu fork: **Contribute → Open pull request**.
2. Título: **tu nombre completo**.
3. **Create pull request**.

### Uno solo
Después de abrirlo, cada `git push` a tu fork actualiza el mismo PR. No abras otro.

---

## Paso 7 · Ver la nota

### Dónde
- En el PR aparece un comentario con la nota y la tabla de criterios.
- En la lista de PRs, tu PR tiene la etiqueta `Nota X.X`.
- El enlace **Ver qué falta por corregir** muestra cada prueba que falló y por qué.

### Cuánto tarda
El calificador compila, arranca y prueba tu proyecto completo: espera unos minutos después de cada push.

### Ojo con la fecha límite
Después de la fecha límite, los push ya no cambian la nota.
Si tu primera entrega llega tarde, el PR queda con la etiqueta `⏰ Fuera de plazo`.

---

## Archivos que NO debes modificar

### La carpeta `.github/`
Ahí vive el calificador. Si la modificas, tu PR queda con la etiqueta `⚠️ Revisar` y la profe lo revisa a mano.
La nota se calcula igual, con el calificador original.

---

## Errores frecuentes

### "No encontré pom.xml en la raíz de tu fork"
Dejaste el proyecto dentro de una subcarpeta. Muévelo a la raíz.

### "Error de compilación en src/…, línea N"
Abre ese archivo en esa línea. Mientras no compile, casi todos los criterios quedan en cero.

### "La aplicación no arrancó: …"
Lee el motivo. Lo más común: una propiedad que no tiene valor de relleno, o un bean que Spring no sabe crear.

### "Se esperaba HTTP 201 y tu API respondió 404"
La ruta no existe o se llama distinto al contrato.

### "La respuesta no trae ese campo"
El JSON no tiene el nombre exacto del contrato (por ejemplo `creditosIa`).

### "Se esperaba HTTP 200 y tu API respondió 502" en pagos
Tu cliente de Stripe no está usando `STRIPE_URL_API`.

### Todo el criterio 05 en cero con "503"
Tu proveedor de IA no lee `SILLON_IA_OPENAI_URL` o `GEMINI_LLAVE`.

### "No encontré en tu README.md una URL de Render"
Agrega la URL completa (`https://tu-app.onrender.com`) a tu README.

### Error 403 al hacer push
Clonaste el repo de la profe en vez de tu fork. Vuelve al Paso 2 con la URL de **tu** fork.

### "Please tell me who you are"
Configura Git una sola vez:

```bash
git config --global user.name "Tu Nombre"
git config --global user.email "tu-correo@ejemplo.com"
```

---

<small>© 2026 Ana Alvarado · Educadora Tech & Desarrolladora Full Stack · Todos los derechos reservados · linkedin.com/in/ana-alvarado-instructora-full-stack</small>
