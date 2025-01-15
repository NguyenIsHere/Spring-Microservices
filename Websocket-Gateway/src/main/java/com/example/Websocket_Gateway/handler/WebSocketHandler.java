package com.example.Websocket_Gateway.handler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Controller
public class WebSocketHandler extends TextWebSocketHandler {

  private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    String userId = getUserIdFromSession(session);
    sessions.put(userId, session);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    String userId = getUserIdFromSession(session);
    sessions.remove(userId);
  }

  public static boolean sendMessageToClient(String userId, String message) {
    WebSocketSession session = sessions.get(userId);
    if (session != null && session.isOpen()) {
      try {
        session.sendMessage(new TextMessage(message));
        return true;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  private String getUserIdFromSession(WebSocketSession session) {
    return session.getUri().getQuery().split("=")[1]; // Lấy userId từ query params
  }
}
