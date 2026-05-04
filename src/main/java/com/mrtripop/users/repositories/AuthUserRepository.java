package com.mrtripop.users.repositories;

import com.mrtripop.users.models.db.AuthUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {
  Optional<AuthUser> findByUsername(String username);
  boolean existsByUsername(String username);
}