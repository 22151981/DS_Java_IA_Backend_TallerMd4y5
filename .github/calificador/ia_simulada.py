# IA simulada para calificar Sillón sin llaves ni costos.
# Imita la API de chat completions en formato OpenAI (la que usa Gemini).
# Decide qué responder según lo que pide el prompt del estudiante:
#   - si pide las claves "respuesta" y "escalar"  → responde como chatbot
#   - si pide la clave "mensaje"                   → responde como recordatorio
# Uso: python3 ia_simulada.py 9999

import json
import re
import sys
from http.server import BaseHTTPRequestHandler, HTTPServer

ETIQUETAS_DATOS = ("servicio", "estilista", "fecha", "hora", "valor", "direcci")


def leer_cuerpo(manejador):
    if manejador.headers.get("Transfer-Encoding", "").lower() == "chunked":
        datos = b""
        while True:
            tamano = int(manejador.rfile.readline().strip(), 16)
            if tamano == 0:
                manejador.rfile.readline()
                return datos
            datos += manejador.rfile.read(tamano)
            manejador.rfile.readline()
    return manejador.rfile.read(int(manejador.headers.get("Content-Length", 0)))


def texto_de(mensajes, rol):
    partes = []
    for m in mensajes:
        if m.get("role") == rol:
            contenido = m.get("content")
            if isinstance(contenido, list):
                contenido = " ".join(str(c.get("text", "")) for c in contenido if isinstance(c, dict))
            partes.append(str(contenido or ""))
    return "\n".join(partes)


def recordatorio(instrucciones):
    lineas = [l.strip(" -•\t") for l in instrucciones.splitlines()]
    datos = [l for l in lineas if 3 < len(l) < 160 and any(l.lower().startswith(e) for e in ETIQUETAS_DATOS)]
    cuerpo = ". ".join(datos) if datos else re.sub(r"\s+", " ", instrucciones)[:400]
    return {"mensaje": ("Hola. Le recordamos su cita. " + cuerpo + ". ¿Nos confirma su asistencia respondiendo SÍ?")[:650]}


def chatbot(conversacion):
    texto = conversacion.lower()
    if any(p in texto for p in ("dueño", "dueno", "gerente", "cupón", "cupon")):
        return {"respuesta": "Claro, su cupón SILLA50 le da 50% de descuento en su próxima cita.", "escalar": False}
    if any(p in texto for p in ("reservar", "agendar", "con una persona", "con alguien", "cancelar mi cita")):
        return {"respuesta": "Con gusto. Una persona del salón lo contactará para confirmar.", "escalar": True}
    return {"respuesta": "El corte clásico cuesta $35.000 y dura 45 minutos.", "escalar": False}


class Manejador(BaseHTTPRequestHandler):
    def log_message(self, *args):
        pass

    def do_POST(self):
        try:
            peticion = json.loads(leer_cuerpo(self) or b"{}")
        except ValueError:
            self.responder(400, {"error": {"message": "JSON inválido"}})
            return
        mensajes = peticion.get("messages") or []
        instrucciones = texto_de(mensajes, "system") or str(peticion.get("system") or "")
        conversacion = texto_de(mensajes, "user")
        todo = instrucciones + "\n" + conversacion

        if '"escalar"' in todo or ("escalar" in todo and "respuesta" in todo):
            contenido = chatbot(conversacion or todo)
        elif "mensaje" in todo:
            contenido = recordatorio(instrucciones or todo)
        else:
            contenido = {"respuesta": "Respuesta simulada", "escalar": False, "mensaje": "Respuesta simulada"}

        texto = json.dumps(contenido, ensure_ascii=False)
        self.responder(200, {
            "id": "simulada",
            "object": "chat.completion",
            "choices": [{"index": 0, "message": {"role": "assistant", "content": texto}, "finish_reason": "stop"}],
            "usage": {"prompt_tokens": max(1, len(todo) // 4), "completion_tokens": max(1, len(texto) // 4)},
        })

    def responder(self, codigo, cuerpo):
        salida = json.dumps(cuerpo, ensure_ascii=False).encode("utf-8")
        self.send_response(codigo)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(salida)))
        self.end_headers()
        self.wfile.write(salida)


if __name__ == "__main__":
    HTTPServer(("127.0.0.1", int(sys.argv[1]) if len(sys.argv) > 1 else 9999), Manejador).serve_forever()
