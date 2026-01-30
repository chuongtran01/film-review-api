package com.filmreview.service;

import com.filmreview.dto.TitleDto;
import com.filmreview.dto.WatchlistRequest;
import com.filmreview.dto.WatchlistResponse;
import com.filmreview.dto.WatchlistUpdateRequest;
import com.filmreview.entity.Title;
import com.filmreview.entity.Watchlist;
import com.filmreview.exception.NotFoundException;
import com.filmreview.mapper.TitleDtoMapper;
import com.filmreview.repository.TitleRepository;
import com.filmreview.repository.WatchlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceImplTest {

  @Mock
  private WatchlistRepository watchlistRepository;

  @Mock
  private TitleRepository titleRepository;

  @Mock
  private TitleDtoMapper titleDtoMapper;

  @InjectMocks
  private WatchlistServiceImpl watchlistService;

  private UUID userId;
  private UUID titleId;
  private Title testTitle;
  private Watchlist testWatchlist;
  private TitleDto testTitleDto;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    titleId = UUID.randomUUID();

    testTitle = new Title();
    testTitle.setId(titleId);
    testTitle.setTitle("Test Movie");
    testTitle.setTmdbId(12345);
    testTitle.setType(Title.TitleType.movie);

    testTitleDto = new TitleDto();
    testTitleDto.setId(titleId);
    testTitleDto.setTitle("Test Movie");
    testTitleDto.setTmdbId(12345);

    testWatchlist = new Watchlist();
    testWatchlist.setId(UUID.randomUUID());
    testWatchlist.setUserId(userId);
    testWatchlist.setTitleId(titleId);
    testWatchlist.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);
    testWatchlist.setCreatedAt(LocalDateTime.now());
    testWatchlist.setUpdatedAt(LocalDateTime.now());
  }

  @Test
  void testGetUserWatchlist_Success_WithStatusFilter() {
    // Arrange
    Watchlist.WatchlistStatus status = Watchlist.WatchlistStatus.WANT_TO_WATCH;
    Pageable pageable = PageRequest.of(0, 20);
    Page<Watchlist> watchlistPage = new PageImpl<>(Arrays.asList(testWatchlist), pageable, 1);

    when(watchlistRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable))
        .thenReturn(watchlistPage);

    // Act
    Page<WatchlistResponse> result = watchlistService.getUserWatchlist(userId, status, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(userId, result.getContent().get(0).getUserId());
    assertEquals(titleId, result.getContent().get(0).getTitleId());
    assertEquals(Watchlist.WatchlistStatus.WANT_TO_WATCH, result.getContent().get(0).getStatus());
    verify(watchlistRepository).findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
    verify(watchlistRepository, never()).findByUserIdOrderByCreatedAtDesc(any(), any());
  }

  @Test
  void testGetUserWatchlist_Success_WithoutStatusFilter() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 20);
    Page<Watchlist> watchlistPage = new PageImpl<>(Arrays.asList(testWatchlist), pageable, 1);

    when(watchlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
        .thenReturn(watchlistPage);

    // Act
    Page<WatchlistResponse> result = watchlistService.getUserWatchlist(userId, null, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    verify(watchlistRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    verify(watchlistRepository, never()).findByUserIdAndStatusOrderByCreatedAtDesc(any(), any(), any());
  }

  @Test
  void testGetUserWatchlist_EmptyList() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 20);
    Page<Watchlist> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

    when(watchlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
        .thenReturn(emptyPage);

    // Act
    Page<WatchlistResponse> result = watchlistService.getUserWatchlist(userId, null, pageable);

    // Assert
    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
    assertEquals(0, result.getTotalElements());
    verify(watchlistRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
  }

  @Test
  void testAddToWatchlist_Success_NewItem() {
    // Arrange
    WatchlistRequest request = new WatchlistRequest();
    request.setTitleId(titleId);
    request.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);

    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(watchlistRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.empty());
    when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> {
      Watchlist w = invocation.getArgument(0);
      if (w.getId() == null) {
        w.setId(UUID.randomUUID());
      }
      if (w.getCreatedAt() == null) {
        w.setCreatedAt(LocalDateTime.now());
      }
      if (w.getUpdatedAt() == null) {
        w.setUpdatedAt(LocalDateTime.now());
      }
      return w;
    });
    when(titleDtoMapper.toDto(testTitle)).thenReturn(testTitleDto);

    // Act
    WatchlistResponse result = watchlistService.addToWatchlist(userId, request);

    // Assert
    assertNotNull(result);
    assertEquals(userId, result.getUserId());
    assertEquals(titleId, result.getTitleId());
    assertEquals(Watchlist.WatchlistStatus.WANT_TO_WATCH, result.getStatus());
    assertNotNull(result.getTitle());
    verify(titleRepository).findById(titleId);
    verify(watchlistRepository).findByUserIdAndTitleId(userId, titleId);
    verify(watchlistRepository).save(any(Watchlist.class));
    verify(titleDtoMapper).toDto(testTitle);
  }

  @Test
  void testAddToWatchlist_Success_UpdateExistingItem() {
    // Arrange
    WatchlistRequest request = new WatchlistRequest();
    request.setTitleId(titleId);
    request.setStatus(Watchlist.WatchlistStatus.WATCHING);

    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(watchlistRepository.findByUserIdAndTitleId(userId, titleId))
        .thenReturn(Optional.of(testWatchlist));
    when(watchlistRepository.save(any(Watchlist.class))).thenReturn(testWatchlist);
    when(titleDtoMapper.toDto(testTitle)).thenReturn(testTitleDto);

    // Act
    WatchlistResponse result = watchlistService.addToWatchlist(userId, request);

    // Assert
    assertNotNull(result);
    assertEquals(Watchlist.WatchlistStatus.WATCHING, result.getStatus());
    verify(titleRepository).findById(titleId);
    verify(watchlistRepository).findByUserIdAndTitleId(userId, titleId);
    verify(watchlistRepository).save(testWatchlist);
    verify(titleDtoMapper).toDto(testTitle);
  }

  @Test
  void testAddToWatchlist_TitleNotFound() {
    // Arrange
    WatchlistRequest request = new WatchlistRequest();
    request.setTitleId(titleId);
    request.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);

    when(titleRepository.findById(titleId)).thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> watchlistService.addToWatchlist(userId, request));
    assertEquals("Title not found", exception.getMessage());
    verify(titleRepository).findById(titleId);
    verify(watchlistRepository, never()).save(any());
  }

  @Test
  void testUpdateWatchlistStatus_Success() {
    // Arrange
    WatchlistUpdateRequest request = new WatchlistUpdateRequest();
    request.setStatus(Watchlist.WatchlistStatus.COMPLETED);

    when(watchlistRepository.findByUserIdAndTitleId(userId, titleId))
        .thenReturn(Optional.of(testWatchlist));
    when(watchlistRepository.save(any(Watchlist.class))).thenReturn(testWatchlist);
    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(titleDtoMapper.toDto(testTitle)).thenReturn(testTitleDto);

    // Act
    WatchlistResponse result = watchlistService.updateWatchlistStatus(userId, titleId, request);

    // Assert
    assertNotNull(result);
    assertEquals(Watchlist.WatchlistStatus.COMPLETED, result.getStatus());
    verify(watchlistRepository).findByUserIdAndTitleId(userId, titleId);
    verify(watchlistRepository).save(testWatchlist);
    verify(titleRepository).findById(titleId);
    verify(titleDtoMapper).toDto(testTitle);
  }

  @Test
  void testUpdateWatchlistStatus_WatchlistItemNotFound() {
    // Arrange
    WatchlistUpdateRequest request = new WatchlistUpdateRequest();
    request.setStatus(Watchlist.WatchlistStatus.COMPLETED);

    when(watchlistRepository.findByUserIdAndTitleId(userId, titleId))
        .thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> watchlistService.updateWatchlistStatus(userId, titleId, request));
    assertEquals("Watchlist item not found", exception.getMessage());
    verify(watchlistRepository).findByUserIdAndTitleId(userId, titleId);
    verify(watchlistRepository, never()).save(any());
    verify(titleRepository, never()).findById(any());
  }

  @Test
  void testUpdateWatchlistStatus_TitleNotFound() {
    // Arrange
    WatchlistUpdateRequest request = new WatchlistUpdateRequest();
    request.setStatus(Watchlist.WatchlistStatus.COMPLETED);

    when(watchlistRepository.findByUserIdAndTitleId(userId, titleId))
        .thenReturn(Optional.of(testWatchlist));
    when(watchlistRepository.save(any(Watchlist.class))).thenReturn(testWatchlist);
    when(titleRepository.findById(titleId)).thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> watchlistService.updateWatchlistStatus(userId, titleId, request));
    assertEquals("Title not found", exception.getMessage());
    verify(watchlistRepository).findByUserIdAndTitleId(userId, titleId);
    verify(watchlistRepository).save(testWatchlist);
    verify(titleRepository).findById(titleId);
  }

  @Test
  void testRemoveFromWatchlist_Success() {
    // Arrange
    when(watchlistRepository.findByUserIdAndTitleId(userId, titleId))
        .thenReturn(Optional.of(testWatchlist));
    doNothing().when(watchlistRepository).delete(testWatchlist);

    // Act
    watchlistService.removeFromWatchlist(userId, titleId);

    // Assert
    verify(watchlistRepository).findByUserIdAndTitleId(userId, titleId);
    verify(watchlistRepository).delete(testWatchlist);
  }

  @Test
  void testRemoveFromWatchlist_WatchlistItemNotFound() {
    // Arrange
    when(watchlistRepository.findByUserIdAndTitleId(userId, titleId))
        .thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> watchlistService.removeFromWatchlist(userId, titleId));
    assertEquals("Watchlist item not found", exception.getMessage());
    verify(watchlistRepository).findByUserIdAndTitleId(userId, titleId);
    verify(watchlistRepository, never()).delete(any());
  }

  @Test
  void testGetWatchlistItem_Success() {
    // Arrange
    when(watchlistRepository.findByUserIdAndTitleId(userId, titleId))
        .thenReturn(Optional.of(testWatchlist));
    when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
    when(titleDtoMapper.toDto(testTitle)).thenReturn(testTitleDto);

    // Act
    Optional<WatchlistResponse> result = watchlistService.getWatchlistItem(userId, titleId);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(userId, result.get().getUserId());
    assertEquals(titleId, result.get().getTitleId());
    assertNotNull(result.get().getTitle());
    verify(watchlistRepository).findByUserIdAndTitleId(userId, titleId);
    verify(titleRepository).findById(titleId);
    verify(titleDtoMapper).toDto(testTitle);
  }

  @Test
  void testGetWatchlistItem_NotFound() {
    // Arrange
    when(watchlistRepository.findByUserIdAndTitleId(userId, titleId))
        .thenReturn(Optional.empty());

    // Act
    Optional<WatchlistResponse> result = watchlistService.getWatchlistItem(userId, titleId);

    // Assert
    assertTrue(result.isEmpty());
    verify(watchlistRepository).findByUserIdAndTitleId(userId, titleId);
    verify(titleRepository, never()).findById(any());
  }

  @Test
  void testGetWatchlistItem_TitleNotFound() {
    // Arrange
    when(watchlistRepository.findByUserIdAndTitleId(userId, titleId))
        .thenReturn(Optional.of(testWatchlist));
    when(titleRepository.findById(titleId)).thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> watchlistService.getWatchlistItem(userId, titleId));
    assertEquals("Title not found", exception.getMessage());
    verify(watchlistRepository).findByUserIdAndTitleId(userId, titleId);
    verify(titleRepository).findById(titleId);
  }

  @Test
  void testGetUserWatchlist_WithDifferentStatuses() {
    // Arrange
    Watchlist watchlist1 = new Watchlist();
    watchlist1.setId(UUID.randomUUID());
    watchlist1.setUserId(userId);
    watchlist1.setTitleId(UUID.randomUUID());
    watchlist1.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);

    Watchlist watchlist2 = new Watchlist();
    watchlist2.setId(UUID.randomUUID());
    watchlist2.setUserId(userId);
    watchlist2.setTitleId(UUID.randomUUID());
    watchlist2.setStatus(Watchlist.WatchlistStatus.WATCHING);

    Pageable pageable = PageRequest.of(0, 20);
    Page<Watchlist> watchlistPage = new PageImpl<>(Arrays.asList(watchlist1, watchlist2), pageable, 2);

    when(watchlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
        .thenReturn(watchlistPage);

    // Act
    Page<WatchlistResponse> result = watchlistService.getUserWatchlist(userId, null, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    verify(watchlistRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
  }

  @Test
  void testAddToWatchlist_WithAllStatusTypes() {
    // Arrange
    Watchlist.WatchlistStatus[] statuses = {
        Watchlist.WatchlistStatus.WANT_TO_WATCH,
        Watchlist.WatchlistStatus.WATCHING,
        Watchlist.WatchlistStatus.COMPLETED,
        Watchlist.WatchlistStatus.DROPPED
    };

    for (Watchlist.WatchlistStatus status : statuses) {
      WatchlistRequest request = new WatchlistRequest();
      request.setTitleId(titleId);
      request.setStatus(status);

      when(titleRepository.findById(titleId)).thenReturn(Optional.of(testTitle));
      when(watchlistRepository.findByUserIdAndTitleId(userId, titleId)).thenReturn(Optional.empty());
      when(watchlistRepository.save(any(Watchlist.class))).thenAnswer(invocation -> {
        Watchlist w = invocation.getArgument(0);
        w.setId(UUID.randomUUID());
        w.setCreatedAt(LocalDateTime.now());
        w.setUpdatedAt(LocalDateTime.now());
        return w;
      });
      when(titleDtoMapper.toDto(testTitle)).thenReturn(testTitleDto);

      // Act
      WatchlistResponse result = watchlistService.addToWatchlist(userId, request);

      // Assert
      assertNotNull(result);
      assertEquals(status, result.getStatus());
    }
  }
}
