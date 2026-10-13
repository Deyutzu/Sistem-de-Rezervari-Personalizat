package com.stayfinder.repository;

import com.stayfinder.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data deduce interogarea din numele metodei: WHERE email = ?
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
