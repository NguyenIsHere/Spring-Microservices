package com.example.user_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.user_service.model.User;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
  Optional<User> findByEmail(String email);

  // Kiểm tra sự tồn tại của email
  boolean existsByEmail(String email);
}
