package com.example.intento1;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    private List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private List<HashMap<WebSocketSession,String>> usuariosConectados = new CopyOnWriteArrayList<>();
    private HashMap<WebSocketSession,String> name_UserConId_session = new HashMap<>();

    /*Se ejecuta cuando se establece una nueva coneccion webSocket
      se agrega la sesion del cliente a la lista de sesiones activas*/
    int i=1;
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("El Id de la sesion "+i+" es: "+session.getId());
        i++;
    }


    /*se invoca cuando se recibe un mensaje WebSocket de un cliente
      En este ejemplo, el mensaje se reenvía a todas las sesiones activas,
      lo que permite la mensajería entre los clientes conectados*/
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // Obtener el mensaje enviado por el cliente
        String messageText = message.getPayload().toString();

        if (messageText.startsWith("OPEN:")) {
            // Este mensaje proviene del evento socket.onopen
            System.out.println("Nombre es: " + messageText+" con ID: "+session.getId());
            name_UserConId_session.put(session, session.getId());

            // Envia la sesion del cliente nuevo a todas las sesiones
            for (WebSocketSession s : sessions) {
                s.sendMessage(new TextMessage("OPEN:"+session.getId()+":"+messageText.substring(5)));
            }

            //Envia todas la sesiones ya existente a la sesion actual
            for(String usuarioConect : name_UserConId_session.values()){
                //omite volver a enviar la sesion actual dos veces
                if(!session.getId().equals(usuarioConect)){
                    session.sendMessage(new TextMessage("OPEN:"+usuarioConect+":"+messageText.substring(5)));
                }
            }
        } else if (messageText.startsWith("CLICK:")) {
            // Este mensaje proviene del evento $("#enviar").click(function ()
            System.out.println("Contenido del mensaje por 'enviar': " + messageText);

            /*se divide el mensaje en partes utilizando el carácter ":" como separador.
                El número 3 indica que se dividirá el mensaje en máximo 4 partes. */
            String[] parts = messageText.split(":", 4);
            /* parts[0] contendrá "CLICK", parts[1] contendrá el destinatario y parts[2] contendrá el remitente, parts[3] el contenido del mensaje.*/

            String destinatario = "OPEN:"+parts[1];
            String remitente = parts[2];
            String contenido = parts[3];
            System.out.println("el destinaraios es: "+destinatario);

            if (destinatario.equals("OPEN:todos")) {
                // Enviar el mensaje a todos los clientes
                for (WebSocketSession s : sessions) {
                    s.sendMessage(new TextMessage("CLICK:"+remitente+": "+contenido));
                }
            }else {
                // Encontrar la sesión del destinatario y enviar el mensaje solo a esa sesión
                for (WebSocketSession s : sessions) {
                    if (destinatario.equals(name_UserConId_session.get(s))) {
                        System.out.println("la sesion encontrada es: "+name_UserConId_session.get(s));
                        s.sendMessage(new TextMessage("CLICK:"+remitente+": "+contenido));
                        session.sendMessage(new TextMessage("CLICK:Tú: "+contenido));
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
        name_UserConId_session.remove(session);
    }
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }



    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

}
