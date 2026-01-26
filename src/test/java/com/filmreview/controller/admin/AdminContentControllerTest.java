package com.filmreview.controller.admin;

import com.filmreview.entity.Genre;
import com.filmreview.entity.Role;
import com.filmreview.entity.RoleType;
import com.filmreview.entity.User;
import com.filmreview.entity.UserRole;
import com.filmreview.faker.UserFaker;
import com.filmreview.repository.GenreRepository;
import com.filmreview.repository.RoleRepository;
import com.filmreview.repository.UserRepository;
import com.filmreview.repository.UserRoleRepository;
import com.filmreview.security.JwtTokenProvider;
import com.filmreview.service.GenreService;
import com.filmreview.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({ "dev", "test" })
@Transactional
class AdminContentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private GenreRepository genreRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private UserRoleRepository userRoleRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtTokenProvider tokenProvider;

  @Autowired
  private PermissionService permissionService;

  @MockBean
  private GenreService genreService;

  private User adminUser;
  private User moderatorUser;
  private User regularUser;
  private String adminToken;
  private String moderatorToken;
  private String regularUserToken;

  @BeforeEach
  void setUp() {
    // Create admin user
    adminUser = UserFaker.generate("admin@example.com", "adminuser");
    adminUser.setPasswordHash(passwordEncoder.encode("password123"));
    adminUser = userRepository.save(adminUser);

    // Create moderator user
    moderatorUser = UserFaker.generate("moderator@example.com", "moderatoruser");
    moderatorUser.setPasswordHash(passwordEncoder.encode("password123"));
    moderatorUser = userRepository.save(moderatorUser);

    // Create regular user
    regularUser = UserFaker.generate("user@example.com", "regularuser");
    regularUser.setPasswordHash(passwordEncoder.encode("password123"));
    regularUser = userRepository.save(regularUser);

    // Get or create ADMIN role
    Role adminRole = roleRepository.findByName(RoleType.ADMIN.getName())
        .orElseGet(() -> {
          Role role = new Role();
          role.setName(RoleType.ADMIN.getName());
          role.setDisplayName("Administrator");
          role.setDescription("Full system access");
          return roleRepository.save(role);
        });

    // Get or create MODERATOR role
    Role moderatorRole = roleRepository.findByName(RoleType.MODERATOR.getName())
        .orElseGet(() -> {
          Role role = new Role();
          role.setName(RoleType.MODERATOR.getName());
          role.setDisplayName("Moderator");
          role.setDescription("Content moderation access");
          return roleRepository.save(role);
        });

    // Assign ADMIN role to admin user
    UserRole adminUserRole = new UserRole();
    adminUserRole.setUserId(adminUser.getId());
    adminUserRole.setRoleId(adminRole.getId());
    userRoleRepository.save(adminUserRole);

    // Assign MODERATOR role to moderator user
    UserRole moderatorUserRole = new UserRole();
    moderatorUserRole.setUserId(moderatorUser.getId());
    moderatorUserRole.setRoleId(moderatorRole.getId());
    userRoleRepository.save(moderatorUserRole);

    // Generate tokens with permissions
    List<String> adminRoles = List.of("ADMIN");
    List<String> moderatorRoles = List.of("MODERATOR");
    List<String> userRoles = List.of("USER");

    // Get permissions for roles (from database)
    List<String> adminPermissions = new java.util.ArrayList<>(
        permissionService.getUserPermissions(adminUser.getId()));
    List<String> moderatorPermissions = new java.util.ArrayList<>(
        permissionService.getUserPermissions(moderatorUser.getId()));
    List<String> userPermissions = new java.util.ArrayList<>(
        permissionService.getUserPermissions(regularUser.getId()));

    adminToken = tokenProvider.generateAccessToken(
        adminUser.getId(),
        adminUser.getUsername(),
        adminUser.getEmail(),
        adminRoles,
        adminPermissions);

    moderatorToken = tokenProvider.generateAccessToken(
        moderatorUser.getId(),
        moderatorUser.getUsername(),
        moderatorUser.getEmail(),
        moderatorRoles,
        moderatorPermissions);

    regularUserToken = tokenProvider.generateAccessToken(
        regularUser.getId(),
        regularUser.getUsername(),
        regularUser.getEmail(),
        userRoles,
        userPermissions);
  }

  @Test
  void testGetGenres_Success_AsAdmin() throws Exception {
    // Arrange - Create test genres (only if they don't exist)
    genreRepository.findById(28).orElseGet(() -> {
      Genre g = new Genre();
      g.setId(28);
      g.setName("Action");
      g.setSlug("action");
      g.setCreatedAt(LocalDateTime.now());
      return genreRepository.save(g);
    });

    genreRepository.findById(12).orElseGet(() -> {
      Genre g = new Genre();
      g.setId(12);
      g.setName("Adventure");
      g.setSlug("adventure");
      g.setCreatedAt(LocalDateTime.now());
      return genreRepository.save(g);
    });

    // Act & Assert
    mockMvc.perform(get("/api/v1/admin/titles/genres")
        .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isNotEmpty())
        .andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.hasItem(28)))
        .andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.hasItem(12)))
        .andExpect(jsonPath("$[?(@.id == 28)].name").value("Action"))
        .andExpect(jsonPath("$[?(@.id == 12)].name").value("Adventure"));
  }

  @Test
  void testGetGenres_Success_AsModerator() throws Exception {
    // Arrange - Create test genre (only if it doesn't exist)
    genreRepository.findById(18).orElseGet(() -> {
      Genre g = new Genre();
      g.setId(18);
      g.setName("Drama");
      g.setSlug("drama");
      g.setCreatedAt(LocalDateTime.now());
      return genreRepository.save(g);
    });

    // Act & Assert
    mockMvc.perform(get("/api/v1/admin/titles/genres")
        .header("Authorization", "Bearer " + moderatorToken))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isNotEmpty())
        .andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.hasItem(18)))
        .andExpect(jsonPath("$[?(@.id == 18)].name").value("Drama"));
  }

  @Test
  void testGetGenres_EmptyList() throws Exception {
    // Arrange - Delete all genres for this test
    genreRepository.deleteAll();

    // Act & Assert - No genres in database
    mockMvc.perform(get("/api/v1/admin/titles/genres")
        .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void testGetGenres_Unauthorized_NoToken() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/v1/admin/titles/genres"))
        .andExpect(status().isForbidden());
  }

  @Test
  void testGetGenres_Forbidden_RegularUser() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/v1/admin/titles/genres")
        .header("Authorization", "Bearer " + regularUserToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void testSyncGenres_Success_AsAdmin() throws Exception {
    // Arrange - Mock GenreService to avoid calling real TMDB API
    when(genreService.syncMovieGenres()).thenReturn(5);
    when(genreService.syncTvSeriesGenres()).thenReturn(3);

    // Act & Assert
    mockMvc.perform(post("/api/v1/admin/titles/genres/sync")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("Movie and TV series genres synced successfully"))
        .andExpect(jsonPath("$.movieGenresSynced").value(5))
        .andExpect(jsonPath("$.tvSeriesGenresSynced").value(3));
  }

  @Test
  void testSyncGenres_Success_AsModerator() throws Exception {
    // Arrange - Mock GenreService to avoid calling real TMDB API
    when(genreService.syncMovieGenres()).thenReturn(10);
    when(genreService.syncTvSeriesGenres()).thenReturn(8);

    // Act & Assert
    mockMvc.perform(post("/api/v1/admin/titles/genres/sync")
        .header("Authorization", "Bearer " + moderatorToken)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("Movie and TV series genres synced successfully"))
        .andExpect(jsonPath("$.movieGenresSynced").value(10))
        .andExpect(jsonPath("$.tvSeriesGenresSynced").value(8));
  }

  @Test
  void testSyncGenres_Unauthorized_NoToken() throws Exception {
    // Act & Assert
    mockMvc.perform(post("/api/v1/admin/titles/genres/sync")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void testSyncGenres_Forbidden_RegularUser() throws Exception {
    // Act & Assert
    mockMvc.perform(post("/api/v1/admin/titles/genres/sync")
        .header("Authorization", "Bearer " + regularUserToken)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }
}
