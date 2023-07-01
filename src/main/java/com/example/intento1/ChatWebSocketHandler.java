package com.example.intento1;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    private List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private HashMap<WebSocketSession,String> sesion_idSesion = new HashMap<>();
    private HashMap<String,String> idSesion_username = new HashMap<>();

    /*Se ejecuta cuando se establece una nueva coneccion webSocket
      se agrega la sesion del cliente a la lista de sesiones activas*/
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    /*se invoca cuando se recibe un mensaje WebSocket de un cliente
      En este ejemplo, el mensaje se reenvía a todas las sesiones activas,
      lo que permite la mensajería entre los clientes conectados*/

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // Obtener el mensaje enviado por el cliente
        String messageText = message.getPayload().toString(); // =OPEN:Jorge

        if (messageText.startsWith("OPEN:")) {

            // Este mensaje proviene del evento socket.onopen
            System.out.println("Nombre es: " + messageText+" con IDSession: "+session.getId());
            session.sendMessage(new TextMessage("IDSESION:"+session.getId()));

            sesion_idSesion.put(session, session.getId());
            idSesion_username.put(session.getId(),messageText);

            // Envia la sesion del cliente nuevo a todas las sesiones
            for (WebSocketSession s : sessions) {
                s.sendMessage(new TextMessage("OPEN:"+session.getId()+":"+messageText.substring(5)));
            }

            // Enviar todas las sesiones ya existentes a la sesión actual
            for (Map.Entry<WebSocketSession, String> entry : sesion_idSesion.entrySet()) {
                WebSocketSession existingSession = entry.getKey();
                String sessionId = entry.getValue();

                if (!session.getId().equals(sessionId) && !messageText.equals(idSesion_username.get(sessionId))) {
                    session.sendMessage(new TextMessage("OPEN:" + sessionId + ":"+idSesion_username.get(sessionId).substring(5)));
                }
            }
        } else if (messageText.startsWith("CLICK:")) {
            // Este mensaje proviene del evento $("#enviar").click(function ()
            System.out.println("Contenido del mensaje enviado: " + messageText);

            /*se divide el mensaje en partes utilizando el carácter ":" como separador.
                El número 3 indica que se dividirá el mensaje en máximo 4 partes. */
            String[] parts = messageText.split(":", 4);

            /* parts[0] contendrá "CLICK", parts[1] contendrá el destinatario y parts[2] contendrá el remitente, parts[3] el contenido del mensaje.*/
            String destinatario = parts[1];
            String remitente = parts[2];
            String contenido = parts[3];
            System.out.println("el remitente es: "+remitente);
            System.out.println("el destinaraios es: "+destinatario);
            System.out.println("el contenido es: "+contenido);    //cotinuat con el envio de emnsajes

            if (destinatario.equals("todos")) {
                // Enviar el mensaje a todos los clientes
                for (WebSocketSession s : sessions) {
                    s.sendMessage(new TextMessage("CLICK:"+remitente+": "+contenido));
                }
            }else {
                // Encontrar la sesión del destinatario y enviar el mensaje solo a esa sesión
                for (Map.Entry<WebSocketSession, String> entry : sesion_idSesion.entrySet()) {
                    WebSocketSession existingSession = entry.getKey();
                    String sessionId = entry.getValue();
                    if (destinatario.equals(sessionId)) {
                        System.out.println("La sesión encontrada es: " + sessionId);
                        existingSession.sendMessage(new TextMessage("CLICK:" + remitente + ": " + contenido));
                        session.sendMessage(new TextMessage("CLICK:Tú: " + contenido));
                    }
                }
            }

        }
    }

    /*se ejecuta cuando se cierra una conexión WebSocket.
        Aquí, se elimina la sesión del cliente de la lista de sesiones activas*/
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        sessions.remove(session);
        // También debes eliminar la sesión del cliente de otros HashMaps si es necesario
        sesion_idSesion.remove(session);
    }
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }



    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

}
