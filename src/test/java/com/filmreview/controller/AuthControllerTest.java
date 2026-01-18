package com.filmreview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filmreview.dto.LoginRequest;
import com.filmreview.dto.RegisterRequest;
import com.filmreview.entity.User;
import com.filmreview.repository.UserRepository;
import com.filmreview.security.JwtTokenProvider;
import com.filmreview.util.TestDataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtTokenProvider tokenProvider;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private TestDataUtil testDataUtil;

  @BeforeEach
  void setUp() {
    testDataUtil = new TestDataUtil(jdbcTemplate, passwordEncoder, userRepository);
    testDataUtil.cleanup();
  }

  @Test
  void testRegister_Success() throws Exception {
    RegisterRequest request = new RegisterRequest();
    request.setEmail("test@example.com");
    request.setPassword("password123");

    mockMvc.perform(post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.user.email").value("test@example.com"))
        .andExpect(jsonPath("$.user.username").exists())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists())
        .andExpect(cookie().exists("refresh_token"));
  }

  @Test
  void testRegister_DuplicateEmail() throws Exception {
    // Create existing user using utility
    testDataUtil.createAndSaveUser("existing@example.com", "existing");

    RegisterRequest request = new RegisterRequest();
    request.setEmail("existing@example.com");
    request.setPassword("password123");

    mockMvc.perform(post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("Email already exists"));
  }

  @Test
  void testRegister_InvalidEmail() throws Exception {
    RegisterRequest request = new RegisterRequest();
    request.setEmail("invalid-email");
    request.setPassword("password123");

    mockMvc.perform(post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  void testRegister_ShortPassword() throws Exception {
    RegisterRequest request = new RegisterRequest();
    request.setEmail("test@example.com");
    request.setPassword("short");

    mockMvc.perform(post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  void testLogin_Success() throws Exception {
    // Create user using utility
    testDataUtil.createAndSaveUser("test@example.com", "testuser");

    LoginRequest request = new LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("password123");

    mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.email").value("test@example.com"))
        .andExpect(jsonPath("$.user.username").value("testuser"))
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists())
        .andExpect(cookie().exists("refresh_token"));
  }

  @Test
  void testLogin_InvalidCredentials() throws Exception {
    LoginRequest request = new LoginRequest();
    request.setEmail("nonexistent@example.com");
    request.setPassword("wrongpassword");

    mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.message").value("Invalid email or password"));
  }

  @Test
  void testLogin_WrongPassword() throws Exception {
    // Create user using utility
    testDataUtil.createAndSaveUser("test@example.com", "testuser");

    LoginRequest request = new LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("wrongpassword");

    mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  @Test
  void testRefreshToken_Success() throws Exception {
    // Create user using utility
    User user = testDataUtil.createAndSaveUser("test@example.com", "testuser");

    // Generate refresh token
    List<String> roles = List.of("USER");
    List<String> permissions = List.of();
    String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getUsername(), user.getEmail(), roles,
        permissions);

    mockMvc.perform(post("/api/v1/auth/refresh")
        .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists())
        .andExpect(cookie().exists("refresh_token"));
  }

  @Test
  void testRefreshToken_NoCookie() throws Exception {
    mockMvc.perform(post("/api/v1/auth/refresh"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void testRefreshToken_InvalidToken() throws Exception {
    mockMvc.perform(post("/api/v1/auth/refresh")
        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "invalid-token")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void testLogout_Success() throws Exception {
    // Create user using utility
    User user = testDataUtil.createAndSaveUser("test@example.com", "testuser");

    // Generate refresh token
    List<String> roles = List.of("USER");
    List<String> permissions = List.of();
    String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getUsername(), user.getEmail(), roles,
        permissions);

    mockMvc.perform(post("/api/v1/auth/logout")
        .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Logged out successfully"))
        .andExpect(cookie().maxAge("refresh_token", 0));
  }
}
