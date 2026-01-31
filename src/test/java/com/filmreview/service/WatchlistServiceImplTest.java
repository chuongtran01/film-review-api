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
    when(titleRepository.findAllById(Arrays.asList(titleId))).thenReturn(Arrays.asList(testTitle));
    when(titleDtoMapper.toDto(testTitle)).thenReturn(testTitleDto);

    // Act
    Page<WatchlistResponse> result = watchlistService.getUserWatchlist(userId, status, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(userId, result.getContent().get(0).getUserId());
    assertEquals(titleId, result.getContent().get(0).getTitleId());
    assertEquals(Watchlist.WatchlistStatus.WANT_TO_WATCH, result.getContent().get(0).getStatus());
    assertNotNull(result.getContent().get(0).getTitle());
    verify(watchlistRepository).findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
    verify(watchlistRepository, never()).findByUserIdOrderByCreatedAtDesc(any(), any());
    verify(titleRepository).findAllById(Arrays.asList(titleId));
    verify(titleDtoMapper).toDto(testTitle);
  }

  @Test
  void testGetUserWatchlist_Success_WithoutStatusFilter() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 20);
    Page<Watchlist> watchlistPage = new PageImpl<>(Arrays.asList(testWatchlist), pageable, 1);

    when(watchlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
        .thenReturn(watchlistPage);
    when(titleRepository.findAllById(Arrays.asList(titleId))).thenReturn(Arrays.asList(testTitle));
    when(titleDtoMapper.toDto(testTitle)).thenReturn(testTitleDto);

    // Act
    Page<WatchlistResponse> result = watchlistService.getUserWatchlist(userId, null, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertNotNull(result.getContent().get(0).getTitle());
    verify(watchlistRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    verify(watchlistRepository, never()).findByUserIdAndStatusOrderByCreatedAtDesc(any(), any(), any());
    verify(titleRepository).findAllById(Arrays.asList(titleId));
    verify(titleDtoMapper).toDto(testTitle);
  }

  @Test
  void testGetUserWatchlist_EmptyList() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 20);
    Page<Watchlist> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

    when(watchlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
        .thenReturn(emptyPage);
    when(titleRepository.findAllById(Collections.emptyList())).thenReturn(Collections.emptyList());

    // Act
    Page<WatchlistResponse> result = watchlistService.getUserWatchlist(userId, null, pageable);

    // Assert
    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
    assertEquals(0, result.getTotalElements());
    verify(watchlistRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    verify(titleRepository).findAllById(Collections.emptyList());
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
    UUID titleId1 = UUID.randomUUID();
    UUID titleId2 = UUID.randomUUID();

    Watchlist watchlist1 = new Watchlist();
    watchlist1.setId(UUID.randomUUID());
    watchlist1.setUserId(userId);
    watchlist1.setTitleId(titleId1);
    watchlist1.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);

    Watchlist watchlist2 = new Watchlist();
    watchlist2.setId(UUID.randomUUID());
    watchlist2.setUserId(userId);
    watchlist2.setTitleId(titleId2);
    watchlist2.setStatus(Watchlist.WatchlistStatus.WATCHING);

    Title title1 = new Title();
    title1.setId(titleId1);
    title1.setTitle("Movie 1");
    title1.setTmdbId(1001);
    title1.setType(Title.TitleType.movie);

    Title title2 = new Title();
    title2.setId(titleId2);
    title2.setTitle("Movie 2");
    title2.setTmdbId(1002);
    title2.setType(Title.TitleType.movie);

    TitleDto titleDto1 = new TitleDto();
    titleDto1.setId(titleId1);
    titleDto1.setTitle("Movie 1");

    TitleDto titleDto2 = new TitleDto();
    titleDto2.setId(titleId2);
    titleDto2.setTitle("Movie 2");

    Pageable pageable = PageRequest.of(0, 20);
    Page<Watchlist> watchlistPage = new PageImpl<>(Arrays.asList(watchlist1, watchlist2), pageable, 2);

    when(watchlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
        .thenReturn(watchlistPage);
    when(titleRepository.findAllById(Arrays.asList(titleId1, titleId2)))
        .thenReturn(Arrays.asList(title1, title2));
    when(titleDtoMapper.toDto(title1)).thenReturn(titleDto1);
    when(titleDtoMapper.toDto(title2)).thenReturn(titleDto2);

    // Act
    Page<WatchlistResponse> result = watchlistService.getUserWatchlist(userId, null, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    assertNotNull(result.getContent().get(0).getTitle());
    assertNotNull(result.getContent().get(1).getTitle());
    verify(watchlistRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    verify(titleRepository).findAllById(Arrays.asList(titleId1, titleId2));
    verify(titleDtoMapper).toDto(title1);
    verify(titleDtoMapper).toDto(title2);
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

  @Test
  void testGetUserWatchlist_BatchFetchesTitles() {
    // Arrange - Multiple watchlist items with different titles
    UUID titleId1 = UUID.randomUUID();
    UUID titleId2 = UUID.randomUUID();
    UUID titleId3 = UUID.randomUUID();

    Watchlist watchlist1 = new Watchlist();
    watchlist1.setId(UUID.randomUUID());
    watchlist1.setUserId(userId);
    watchlist1.setTitleId(titleId1);
    watchlist1.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);

    Watchlist watchlist2 = new Watchlist();
    watchlist2.setId(UUID.randomUUID());
    watchlist2.setUserId(userId);
    watchlist2.setTitleId(titleId2);
    watchlist2.setStatus(Watchlist.WatchlistStatus.WATCHING);

    Watchlist watchlist3 = new Watchlist();
    watchlist3.setId(UUID.randomUUID());
    watchlist3.setUserId(userId);
    watchlist3.setTitleId(titleId3);
    watchlist3.setStatus(Watchlist.WatchlistStatus.COMPLETED);

    Title title1 = new Title();
    title1.setId(titleId1);
    title1.setTitle("Movie 1");
    title1.setTmdbId(1001);
    title1.setType(Title.TitleType.movie);

    Title title2 = new Title();
    title2.setId(titleId2);
    title2.setTitle("Movie 2");
    title2.setTmdbId(1002);
    title2.setType(Title.TitleType.movie);

    Title title3 = new Title();
    title3.setId(titleId3);
    title3.setTitle("Movie 3");
    title3.setTmdbId(1003);
    title3.setType(Title.TitleType.movie);

    TitleDto titleDto1 = new TitleDto();
    titleDto1.setId(titleId1);
    titleDto1.setTitle("Movie 1");

    TitleDto titleDto2 = new TitleDto();
    titleDto2.setId(titleId2);
    titleDto2.setTitle("Movie 2");

    TitleDto titleDto3 = new TitleDto();
    titleDto3.setId(titleId3);
    titleDto3.setTitle("Movie 3");

    Pageable pageable = PageRequest.of(0, 20);
    Page<Watchlist> watchlistPage = new PageImpl<>(
        Arrays.asList(watchlist1, watchlist2, watchlist3), pageable, 3);

    when(watchlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
        .thenReturn(watchlistPage);
    when(titleRepository.findAllById(Arrays.asList(titleId1, titleId2, titleId3)))
        .thenReturn(Arrays.asList(title1, title2, title3));
    when(titleDtoMapper.toDto(title1)).thenReturn(titleDto1);
    when(titleDtoMapper.toDto(title2)).thenReturn(titleDto2);
    when(titleDtoMapper.toDto(title3)).thenReturn(titleDto3);

    // Act
    Page<WatchlistResponse> result = watchlistService.getUserWatchlist(userId, null, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(3, result.getContent().size());
    // Verify all responses have titles
    assertNotNull(result.getContent().get(0).getTitle());
    assertNotNull(result.getContent().get(1).getTitle());
    assertNotNull(result.getContent().get(2).getTitle());
    // Verify batch fetch was called once with all title IDs
    verify(titleRepository, times(1)).findAllById(Arrays.asList(titleId1, titleId2, titleId3));
    verify(titleDtoMapper).toDto(title1);
    verify(titleDtoMapper).toDto(title2);
    verify(titleDtoMapper).toDto(title3);
  }

  @Test
  void testGetUserWatchlist_MissingTitleInMap() {
    // Arrange - Title not found in repository (edge case)
    UUID missingTitleId = UUID.randomUUID();

    Watchlist watchlist = new Watchlist();
    watchlist.setId(UUID.randomUUID());
    watchlist.setUserId(userId);
    watchlist.setTitleId(missingTitleId);
    watchlist.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);

    Pageable pageable = PageRequest.of(0, 20);
    Page<Watchlist> watchlistPage = new PageImpl<>(Arrays.asList(watchlist), pageable, 1);

    when(watchlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
        .thenReturn(watchlistPage);
    when(titleRepository.findAllById(Arrays.asList(missingTitleId)))
        .thenReturn(Collections.emptyList()); // Title not found

    // Act
    Page<WatchlistResponse> result = watchlistService.getUserWatchlist(userId, null, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(userId, result.getContent().get(0).getUserId());
    assertEquals(missingTitleId, result.getContent().get(0).getTitleId());
    // Title should be null when not found in map
    assertNull(result.getContent().get(0).getTitle());
    verify(titleRepository).findAllById(Arrays.asList(missingTitleId));
    verify(titleDtoMapper, never()).toDto(any());
  }

  @Test
  void testGetUserWatchlist_PartialTitleFetch() {
    // Arrange - Some titles found, some missing
    UUID titleId1 = UUID.randomUUID();
    UUID titleId2 = UUID.randomUUID();
    UUID missingTitleId = UUID.randomUUID();

    Watchlist watchlist1 = new Watchlist();
    watchlist1.setId(UUID.randomUUID());
    watchlist1.setUserId(userId);
    watchlist1.setTitleId(titleId1);
    watchlist1.setStatus(Watchlist.WatchlistStatus.WANT_TO_WATCH);

    Watchlist watchlist2 = new Watchlist();
    watchlist2.setId(UUID.randomUUID());
    watchlist2.setUserId(userId);
    watchlist2.setTitleId(missingTitleId);
    watchlist2.setStatus(Watchlist.WatchlistStatus.WATCHING);

    Watchlist watchlist3 = new Watchlist();
    watchlist3.setId(UUID.randomUUID());
    watchlist3.setUserId(userId);
    watchlist3.setTitleId(titleId2);
    watchlist3.setStatus(Watchlist.WatchlistStatus.COMPLETED);

    Title title1 = new Title();
    title1.setId(titleId1);
    title1.setTitle("Movie 1");
    title1.setTmdbId(1001);
    title1.setType(Title.TitleType.movie);

    Title title2 = new Title();
    title2.setId(titleId2);
    title2.setTitle("Movie 2");
    title2.setTmdbId(1002);
    title2.setType(Title.TitleType.movie);

    TitleDto titleDto1 = new TitleDto();
    titleDto1.setId(titleId1);
    titleDto1.setTitle("Movie 1");

    TitleDto titleDto2 = new TitleDto();
    titleDto2.setId(titleId2);
    titleDto2.setTitle("Movie 2");

    Pageable pageable = PageRequest.of(0, 20);
    Page<Watchlist> watchlistPage = new PageImpl<>(
        Arrays.asList(watchlist1, watchlist2, watchlist3), pageable, 3);

    when(watchlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
        .thenReturn(watchlistPage);
    // Only title1 and title2 are found, missingTitleId is not
    when(titleRepository.findAllById(Arrays.asList(titleId1, missingTitleId, titleId2)))
        .thenReturn(Arrays.asList(title1, title2));
    when(titleDtoMapper.toDto(title1)).thenReturn(titleDto1);
    when(titleDtoMapper.toDto(title2)).thenReturn(titleDto2);

    // Act
    Page<WatchlistResponse> result = watchlistService.getUserWatchlist(userId, null, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(3, result.getContent().size());
    // First item has title
    assertNotNull(result.getContent().get(0).getTitle());
    assertEquals("Movie 1", result.getContent().get(0).getTitle().getTitle());
    // Second item (missing title) should not have title
    assertNull(result.getContent().get(1).getTitle());
    // Third item has title
    assertNotNull(result.getContent().get(2).getTitle());
    assertEquals("Movie 2", result.getContent().get(2).getTitle().getTitle());
    verify(titleRepository).findAllById(Arrays.asList(titleId1, missingTitleId, titleId2));
  }
}
