package com.example.gateway.controller;

import com.example.gateway.dto.CreateUserDTO;
import com.example.gateway.dto.UpdateUserDTO;
import com.example.gateway.dto.UserResponseDTO;
import com.example.gateway.service.UserGrpcClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserGrpcClient userGrpcClient;

  public UserController(UserGrpcClient userGrpcClient) {
    this.userGrpcClient = userGrpcClient;
  }

  /**
   * Lấy thông tin user theo email.
   */
  @GetMapping("/{email}")
  public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
    UserResponseDTO userResponse = userGrpcClient.getUserByEmail(email);
    return ResponseEntity.ok(userResponse);
  }

  /**
   * Tạo user mới.
   */
  @PostMapping
  public ResponseEntity<UserResponseDTO> createUser(@RequestBody CreateUserDTO createUserDTO) {
    UserResponseDTO userResponse = userGrpcClient.createUser(
        createUserDTO.getName(),
        createUserDTO.getEmail(),
        createUserDTO.getPassword());
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
