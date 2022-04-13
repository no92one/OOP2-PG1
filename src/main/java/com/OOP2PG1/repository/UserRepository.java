package com.OOP2PG1.repository;

import com.OOP2PG1.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);

    Optional<User> findByroles(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);


}