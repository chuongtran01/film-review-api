package com.filmreview.repository;

import com.filmreview.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
  Optional<Role> findByName(String name);

  @Query("SELECT DISTINCT p.name FROM Role r JOIN r.permissions p WHERE r.id IN :roleIds")
  List<String> findPermissionNamesByRoleIds(@Param("roleIds") List<Integer> roleIds);
}
