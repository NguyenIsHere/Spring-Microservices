package com.example.gateway.controller;

import com.example.gateway.dto.CreateUserDTO;
import com.example.gateway.dto.UpdateUserDTO;
import com.example.gateway.dto.UserResponseDTO;
import com.example.gateway.service.UserGrpcClient;

import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  private final UserGrpcClient userGrpcClient;

  public UserController(UserGrpcClient userGrpcClient) {
    this.userGrpcClient = userGrpcClient;
  }

  /**
   * Lấy thông tin user theo email.
   */
  @GetMapping("/{email}")
  public Mono<ResponseEntity<UserResponseDTO>> getUserByEmail(@PathVariable String email) {
    return userGrpcClient.getUserByEmail(email)
        .map(userResponse -> {
          UserResponseDTO userResponseDTO = new UserResponseDTO();
          userResponseDTO.setName(userResponse.getName());
          userResponseDTO.setEmail(userResponse.getEmail());
          userResponseDTO.setUserId(userResponse.getUserId());
          userResponseDTO.setRole(userResponse.getRole());
          return ResponseEntity.ok(userResponseDTO);
        })
        .defaultIfEmpty(ResponseEntity.notFound().build())
        .onErrorResume(e -> {
          // Log lỗi nếu có
          logger.error("Error fetching user by email {}: {}", email, e.getMessage());
          return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        });
  }

  /**
   * Tạo user mới.
   */
  @PostMapping
  public ResponseEntity<UserResponseDTO> createUser(@RequestBody CreateUserDTO createUserDTO) {
    UserResponseDTO userResponse = userGrpcClient.createUser(
        createUserDTO.getName(),
        createUserDTO.getEmail(),
        createUserDTO.getPassword(),
        createUserDTO.getRole());
    return ResponseEntity.ok(userResponse);
  }

  /**
   * Cập nhật thông tin user.
   */
  @PutMapping("/{email}")
  public ResponseEntity<UserResponseDTO> updateUser(@PathVariable String email,
      @RequestBody UpdateUserDTO updateUserDTO) {
    UserResponseDTO userResponse = userGrpcClient.updateUser(
        email,
        updateUserDTO.getName(),
        updateUserDTO.getPassword());
    return ResponseEntity.ok(userResponse);
  }
}
