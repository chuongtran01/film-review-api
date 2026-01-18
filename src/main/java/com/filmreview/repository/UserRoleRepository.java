package com.filmreview.repository;

import com.filmreview.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
  List<UserRole> findByUserId(UUID userId);

  @Query("SELECT ur.role.name FROM UserRole ur WHERE ur.userId = :userId")
  List<String> findRoleNamesByUserId(@Param("userId") UUID userId);

  boolean existsByUserIdAndRoleId(UUID userId, Integer roleId);

  void deleteByUserIdAndRoleId(UUID userId, Integer roleId);
}
