package com.example.Websocket_Gateway.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import com.example.Websocket_Gateway.handler.WebSocketHandler;

import websocket.NotificationRequest;
import websocket.NotificationResponse;

import websocket.WebSocketServiceGrpc;

@GrpcService
public class WebSocketServiceImpl extends WebSocketServiceGrpc.WebSocketServiceImplBase {

  @Override
  public void sendNotification(NotificationRequest request, StreamObserver<NotificationResponse> responseObserver) {
    String userId = request.getUserId();
    String message = request.getMessage();

    // Giả sử bạn có logic gửi thông báo qua WebSocket tại đây
    boolean success = WebSocketHandler.sendMessageToClient(userId, message);

    NotificationResponse response = NotificationResponse.newBuilder()
        .setSuccess(success)
        .setError(success ? "" : "Failed to send notification")
        .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
