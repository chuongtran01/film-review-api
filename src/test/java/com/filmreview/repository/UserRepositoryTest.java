package com.filmreview.repository;

import com.filmreview.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private User testUser;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();

    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setUsername("testuser");
    testUser.setPasswordHash(passwordEncoder.encode("password123"));
    testUser = userRepository.save(testUser);
  }

  @Test
  void testFindByEmail_Success() {
    Optional<User> found = userRepository.findByEmail("test@example.com");

    assertTrue(found.isPresent());
    assertEquals("test@example.com", found.get().getEmail());
    assertEquals("testuser", found.get().getUsername());
  }

  @Test
  void testFindByEmail_NotFound() {
    Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

    assertFalse(found.isPresent());
  }

  @Test
  void testFindByUsername_Success() {
    Optional<User> found = userRepository.findByUsername("testuser");

    assertTrue(found.isPresent());
    assertEquals("testuser", found.get().getUsername());
  }

  @Test
  void testFindByUsername_NotFound() {
    Optional<User> found = userRepository.findByUsername("nonexistent");

    assertFalse(found.isPresent());
  }

  @Test
  void testExistsByEmail_True() {
    assertTrue(userRepository.existsByEmail("test@example.com"));
  }

  @Test
  void testExistsByEmail_False() {
    assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
  }

  @Test
  void testExistsByUsername_True() {
    assertTrue(userRepository.existsByUsername("testuser"));
  }

  @Test
  void testExistsByUsername_False() {
    assertFalse(userRepository.existsByUsername("nonexistent"));
  }

  @Test
  void testSaveUser() {
    User newUser = new User();
    newUser.setEmail("new@example.com");
    newUser.setUsername("newuser");
    newUser.setPasswordHash(passwordEncoder.encode("password123"));

    User saved = userRepository.save(newUser);

    assertNotNull(saved.getId());
    assertEquals("new@example.com", saved.getEmail());
    assertEquals("newuser", saved.getUsername());
  }

  @Test
  void testUserEmailUnique() {
    User duplicateUser = new User();
    duplicateUser.setEmail("test@example.com"); // Same email
    duplicateUser.setUsername("differentuser");
    duplicateUser.setPasswordHash(passwordEncoder.encode("password123"));

    // Should throw exception or fail due to unique constraint
    assertThrows(Exception.class, () -> userRepository.saveAndFlush(duplicateUser));
  }

  @Test
  void testUserUsernameUnique() {
    User duplicateUser = new User();
    duplicateUser.setEmail("different@example.com");
    duplicateUser.setUsername("testuser"); // Same username
    duplicateUser.setPasswordHash(passwordEncoder.encode("password123"));

    // Should throw exception or fail due to unique constraint
    assertThrows(Exception.class, () -> userRepository.saveAndFlush(duplicateUser));
  }
}
