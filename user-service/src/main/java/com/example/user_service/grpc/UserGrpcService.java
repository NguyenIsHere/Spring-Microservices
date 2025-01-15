package com.example.user_service.grpc;

import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import user.CreateUserRequest;
import user.UpdateUserRequest;
import user.UserEmailRequest;
import user.UserResponse;
import user.UserServiceGrpc;

import java.util.Optional;

@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

  @Autowired
  private UserRepository userRepository;

  /**
   * Lấy thông tin user theo email.
   */
  @Override
  public void getUserByEmail(UserEmailRequest request, StreamObserver<UserResponse> responseObserver) {
    try {
      Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
      if (userOptional.isEmpty()) {
        responseObserver.onError(
            Status.NOT_FOUND
                .withDescription("User not found with email: " + request.getEmail())
                .asRuntimeException());
        return;
      }

      User user = userOptional.get();
      UserResponse response = UserResponse.newBuilder()
          .setName(user.getName())
          .setEmail(user.getEmail())
          .setPassword(user.getPassword())
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL
              .withDescription("Error retrieving user by email")
              .augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }

  /**
   * Tạo một user mới.
   */
  @Override
  public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
    try {
      // Kiểm tra nếu email đã tồn tại
      if (userRepository.existsByEmail(request.getEmail())) {
        responseObserver.onError(
            Status.ALREADY_EXISTS
                .withDescription("Email already exists: " + request.getEmail())
                .asRuntimeException());
        return;
      }

      User user = new User();
      user.setName(request.getName());
      user.setEmail(request.getEmail());
      user.setPassword(request.getPassword()); // Lưu ý: Nên hash mật khẩu trước khi lưu

      User savedUser = userRepository.save(user);

      UserResponse response = UserResponse.newBuilder()
          .setName(savedUser.getName())
          .setEmail(savedUser.getEmail())
          .setPassword(savedUser.getPassword())
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL
              .withDescription("Error creating user")
              .augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }

  /**
   * Cập nhật thông tin user.
   */
  @Override
  public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
    try {
      Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
      if (userOptional.isEmpty()) {
        responseObserver.onError(
            Status.NOT_FOUND
                .withDescription("User not found with email: " + request.getEmail())
                .asRuntimeException());
        return;
      }

      User user = userOptional.get();

      // Cập nhật thông tin nếu có
      if (!request.getName().isEmpty()) {
        user.setName(request.getName());
      }

      if (!request.getPassword().isEmpty()) {
        user.setPassword(request.getPassword()); // Nên hash mật khẩu trước khi lưu
      }

      User updatedUser = userRepository.save(user);

      UserResponse response = UserResponse.newBuilder()
          .setName(updatedUser.getName())
          .setEmail(updatedUser.getEmail())
          .setPassword(updatedUser.getPassword())
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(
          Status.INTERNAL
              .withDescription("Error updating user")
              .augmentDescription(e.getMessage())
              .asRuntimeException());
    }
  }
}
