package com.filmreview.service;

import com.filmreview.dto.tmdb.TmdbLanguageInfo;
import com.filmreview.entity.Language;
import com.filmreview.repository.LanguageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LanguageServiceImplTest {

  @Mock
  private LanguageRepository languageRepository;

  @Mock
  private TmdbService tmdbService;

  @InjectMocks
  private LanguageServiceImpl languageService;

  private TmdbLanguageInfo tmdbLanguage1;
  private TmdbLanguageInfo tmdbLanguage2;
  private Language existingLanguage;

  @BeforeEach
  void setUp() {
    tmdbLanguage1 = new TmdbLanguageInfo("en", "English", "English");
    tmdbLanguage2 = new TmdbLanguageInfo("es", "Spanish", "Español");

    existingLanguage = new Language();
    existingLanguage.setIso6391("en");
    existingLanguage.setEnglishName("English");
    existingLanguage.setName("English");
    existingLanguage.setCreatedAt(LocalDateTime.now());
  }

  @Test
  void testGetAllLanguages_Success() {
    // Arrange
    Language language1 = new Language();
    language1.setIso6391("en");
    language1.setEnglishName("English");
    language1.setName("English");

    Language language2 = new Language();
    language2.setIso6391("es");
    language2.setEnglishName("Spanish");
    language2.setName("Español");

    List<Language> languages = Arrays.asList(language1, language2);
    when(languageRepository.findAll()).thenReturn(languages);

    // Act
    List<Language> result = languageService.getAllLanguages();

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("en", result.get(0).getIso6391());
    assertEquals("English", result.get(0).getEnglishName());
    assertEquals("es", result.get(1).getIso6391());
    assertEquals("Spanish", result.get(1).getEnglishName());
    verify(languageRepository).findAll();
  }

  @Test
  void testGetAllLanguages_EmptyList() {
    // Arrange
    when(languageRepository.findAll()).thenReturn(Collections.emptyList());

    // Act
    List<Language> result = languageService.getAllLanguages();

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(languageRepository).findAll();
  }

  @Test
  void testSyncLanguages_Success_NewLanguages() {
    // Arrange
    List<TmdbLanguageInfo> tmdbLanguages = Arrays.asList(tmdbLanguage1, tmdbLanguage2);
    when(tmdbService.getLanguages()).thenReturn(tmdbLanguages);
    when(languageRepository.findById("en")).thenReturn(Optional.empty());
    when(languageRepository.findById("es")).thenReturn(Optional.empty());

    when(languageRepository.save(any(Language.class))).thenAnswer(invocation -> {
      Language lang = invocation.getArgument(0);
      if (lang.getCreatedAt() == null) {
        lang.setCreatedAt(LocalDateTime.now());
      }
      return lang;
    });

    // Act
    int result = languageService.syncLanguages();

    // Assert
    assertEquals(2, result);
    verify(tmdbService).getLanguages();
    verify(languageRepository, times(2)).findById(anyString());
    verify(languageRepository, times(2)).save(any(Language.class));
  }

  @Test
  void testSyncLanguages_Success_ExistingLanguages_NoUpdate() {
    // Arrange
    List<TmdbLanguageInfo> tmdbLanguages = Arrays.asList(tmdbLanguage1);
    when(tmdbService.getLanguages()).thenReturn(tmdbLanguages);
    when(languageRepository.findById("en")).thenReturn(Optional.of(existingLanguage));

    // Act
    int result = languageService.syncLanguages();

    // Assert
    assertEquals(0, result); // No updates needed
    verify(tmdbService).getLanguages();
    verify(languageRepository).findById("en");
    verify(languageRepository, never()).save(any(Language.class));
  }

  @Test
  void testSyncLanguages_Success_ExistingLanguages_WithEnglishNameUpdate() {
    // Arrange
    TmdbLanguageInfo updatedLanguage = new TmdbLanguageInfo("en", "English Updated", "English");
    List<TmdbLanguageInfo> tmdbLanguages = Arrays.asList(updatedLanguage);
    when(tmdbService.getLanguages()).thenReturn(tmdbLanguages);
    when(languageRepository.findById("en")).thenReturn(Optional.of(existingLanguage));

    Language updated = new Language();
    updated.setIso6391("en");
    updated.setEnglishName("English Updated");
    updated.setName("English");
    updated.setCreatedAt(existingLanguage.getCreatedAt());

    when(languageRepository.save(any(Language.class))).thenReturn(updated);

    // Act
    int result = languageService.syncLanguages();

    // Assert
    assertEquals(1, result); // One update
    verify(tmdbService).getLanguages();
    verify(languageRepository).findById("en");
    verify(languageRepository).save(any(Language.class));
  }

  @Test
  void testSyncLanguages_Success_ExistingLanguages_WithNameUpdate() {
    // Arrange
    TmdbLanguageInfo updatedLanguage = new TmdbLanguageInfo("en", "English", "English Updated");
    List<TmdbLanguageInfo> tmdbLanguages = Arrays.asList(updatedLanguage);
    when(tmdbService.getLanguages()).thenReturn(tmdbLanguages);
    when(languageRepository.findById("en")).thenReturn(Optional.of(existingLanguage));

    Language updated = new Language();
    updated.setIso6391("en");
    updated.setEnglishName("English");
    updated.setName("English Updated");
    updated.setCreatedAt(existingLanguage.getCreatedAt());

    when(languageRepository.save(any(Language.class))).thenReturn(updated);

    // Act
    int result = languageService.syncLanguages();

    // Assert
    assertEquals(1, result); // One update
    verify(tmdbService).getLanguages();
    verify(languageRepository).findById("en");
    verify(languageRepository).save(any(Language.class));
  }

  @Test
  void testSyncLanguages_Success_NewLanguage_NoCreatedAt() {
    // Arrange
    List<TmdbLanguageInfo> tmdbLanguages = Arrays.asList(tmdbLanguage1);
    when(tmdbService.getLanguages()).thenReturn(tmdbLanguages);

    Language newLanguage = new Language();
    newLanguage.setIso6391("en");
    newLanguage.setEnglishName("English");
    when(languageRepository.findById("en")).thenReturn(Optional.of(newLanguage));

    Language savedLanguage = new Language();
    savedLanguage.setIso6391("en");
    savedLanguage.setEnglishName("English");
    savedLanguage.setName("English");
    savedLanguage.setCreatedAt(LocalDateTime.now());

    when(languageRepository.save(any(Language.class))).thenReturn(savedLanguage);

    // Act
    int result = languageService.syncLanguages();

    // Assert
    assertEquals(1, result); // Saved because createdAt was null
    verify(tmdbService).getLanguages();
    verify(languageRepository).findById("en");
    verify(languageRepository).save(any(Language.class));
  }

  @Test
  void testSyncLanguages_EmptyList() {
    // Arrange
    when(tmdbService.getLanguages()).thenReturn(Collections.emptyList());

    // Act
    int result = languageService.syncLanguages();

    // Assert
    assertEquals(0, result);
    verify(tmdbService).getLanguages();
    verify(languageRepository, never()).findById(anyString());
    verify(languageRepository, never()).save(any(Language.class));
  }

  @Test
  void testSyncLanguages_NullList() {
    // Arrange
    when(tmdbService.getLanguages()).thenReturn(null);

    // Act
    int result = languageService.syncLanguages();

    // Assert
    assertEquals(0, result);
    verify(tmdbService).getLanguages();
    verify(languageRepository, never()).findById(anyString());
    verify(languageRepository, never()).save(any(Language.class));
  }

  @Test
  void testSyncLanguages_SkipsNullIso6391() {
    // Arrange
    TmdbLanguageInfo invalidLanguage = new TmdbLanguageInfo(null, "Unknown", "Unknown");
    List<TmdbLanguageInfo> tmdbLanguages = Arrays.asList(invalidLanguage, tmdbLanguage1);
    when(tmdbService.getLanguages()).thenReturn(tmdbLanguages);
    when(languageRepository.findById("en")).thenReturn(Optional.empty());

    when(languageRepository.save(any(Language.class))).thenAnswer(invocation -> {
      Language lang = invocation.getArgument(0);
      if (lang.getCreatedAt() == null) {
        lang.setCreatedAt(LocalDateTime.now());
      }
      return lang;
    });

    // Act
    int result = languageService.syncLanguages();

    // Assert
    assertEquals(1, result); // Only one valid language synced
    verify(tmdbService).getLanguages();
    verify(languageRepository).findById("en");
    verify(languageRepository, never()).findById(null);
    verify(languageRepository, times(1)).save(any(Language.class));
  }

  @Test
  void testSyncLanguages_SkipsEmptyIso6391() {
    // Arrange
    TmdbLanguageInfo invalidLanguage = new TmdbLanguageInfo("", "Unknown", "Unknown");
    List<TmdbLanguageInfo> tmdbLanguages = Arrays.asList(invalidLanguage, tmdbLanguage1);
    when(tmdbService.getLanguages()).thenReturn(tmdbLanguages);
    when(languageRepository.findById("en")).thenReturn(Optional.empty());

    when(languageRepository.save(any(Language.class))).thenAnswer(invocation -> {
      Language lang = invocation.getArgument(0);
      if (lang.getCreatedAt() == null) {
        lang.setCreatedAt(LocalDateTime.now());
      }
      return lang;
    });

    // Act
    int result = languageService.syncLanguages();

    // Assert
    assertEquals(1, result); // Only one valid language synced
    verify(tmdbService).getLanguages();
    verify(languageRepository).findById("en");
    verify(languageRepository, never()).findById("");
    verify(languageRepository, times(1)).save(any(Language.class));
  }

  @Test
  void testSyncLanguages_HandlesNullEnglishName() {
    // Arrange
    TmdbLanguageInfo languageWithNullEnglishName = new TmdbLanguageInfo("de", null, "Deutsch");
    List<TmdbLanguageInfo> tmdbLanguages = Arrays.asList(languageWithNullEnglishName);
    when(tmdbService.getLanguages()).thenReturn(tmdbLanguages);
    when(languageRepository.findById("de")).thenReturn(Optional.empty());

    when(languageRepository.save(any(Language.class))).thenAnswer(invocation -> {
      Language lang = invocation.getArgument(0);
      if (lang.getCreatedAt() == null) {
        lang.setCreatedAt(LocalDateTime.now());
      }
      return lang;
    });

    // Act
    int result = languageService.syncLanguages();

    // Assert
    assertEquals(1, result);
    verify(languageRepository).save(any(Language.class));
  }

  @Test
  void testSyncLanguages_HandlesNullName() {
    // Arrange
    TmdbLanguageInfo languageWithNullName = new TmdbLanguageInfo("de", "German", null);
    List<TmdbLanguageInfo> tmdbLanguages = Arrays.asList(languageWithNullName);
    when(tmdbService.getLanguages()).thenReturn(tmdbLanguages);
    when(languageRepository.findById("de")).thenReturn(Optional.empty());

    when(languageRepository.save(any(Language.class))).thenAnswer(invocation -> {
      Language lang = invocation.getArgument(0);
      if (lang.getCreatedAt() == null) {
        lang.setCreatedAt(LocalDateTime.now());
      }
      return lang;
    });

    // Act
    int result = languageService.syncLanguages();

    // Assert
    assertEquals(1, result);
    verify(languageRepository).save(any(Language.class));
  }

  @Test
  void testSyncLanguages_HandlesEmptyName() {
    // Arrange
    TmdbLanguageInfo languageWithEmptyName = new TmdbLanguageInfo("de", "German", "");
    List<TmdbLanguageInfo> tmdbLanguages = Arrays.asList(languageWithEmptyName);
    when(tmdbService.getLanguages()).thenReturn(tmdbLanguages);
    when(languageRepository.findById("de")).thenReturn(Optional.empty());

    when(languageRepository.save(any(Language.class))).thenAnswer(invocation -> {
      Language lang = invocation.getArgument(0);
      if (lang.getCreatedAt() == null) {
        lang.setCreatedAt(LocalDateTime.now());
      }
      return lang;
    });

    // Act
    int result = languageService.syncLanguages();

    // Assert
    assertEquals(1, result);
    verify(languageRepository).save(any(Language.class));
  }

  @Test
  void testSyncLanguages_MixedNewAndExisting() {
    // Arrange
    List<TmdbLanguageInfo> tmdbLanguages = Arrays.asList(tmdbLanguage1, tmdbLanguage2);
    when(tmdbService.getLanguages()).thenReturn(tmdbLanguages);
    when(languageRepository.findById("en")).thenReturn(Optional.of(existingLanguage));
    when(languageRepository.findById("es")).thenReturn(Optional.empty());

    when(languageRepository.save(any(Language.class))).thenAnswer(invocation -> {
      Language lang = invocation.getArgument(0);
      if (lang.getCreatedAt() == null) {
        lang.setCreatedAt(LocalDateTime.now());
      }
      return lang;
    });

    // Act
    int result = languageService.syncLanguages();

    // Assert
    assertEquals(1, result); // Only new language synced
    verify(tmdbService).getLanguages();
    verify(languageRepository).findById("en");
    verify(languageRepository).findById("es");
    verify(languageRepository, times(1)).save(any(Language.class));
  }
}
