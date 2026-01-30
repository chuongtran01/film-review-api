package com.filmreview.service;

import com.filmreview.dto.WatchlistRequest;
import com.filmreview.dto.WatchlistResponse;
import com.filmreview.dto.WatchlistUpdateRequest;
import com.filmreview.entity.Watchlist;
import com.filmreview.entity.Title;
import com.filmreview.exception.NotFoundException;
import com.filmreview.mapper.TitleDtoMapper;
import com.filmreview.repository.WatchlistRepository;
import com.filmreview.repository.TitleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class WatchlistServiceImpl implements WatchlistService {

  private final WatchlistRepository watchlistRepository;
  private final TitleRepository titleRepository;
  private final TitleDtoMapper titleDtoMapper;

  public WatchlistServiceImpl(
      WatchlistRepository watchlistRepository,
      TitleRepository titleRepository,
      TitleDtoMapper titleDtoMapper) {
    this.watchlistRepository = watchlistRepository;
    this.titleRepository = titleRepository;
    this.titleDtoMapper = titleDtoMapper;
  }

  @Override
  public Page<WatchlistResponse> getUserWatchlist(
      UUID userId,
      Watchlist.WatchlistStatus status,
      Pageable pageable) {
    Page<Watchlist> watchlistItems;

    if (status != null) {
      watchlistItems = watchlistRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
          userId, status, pageable);
    } else {
      watchlistItems = watchlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    return watchlistItems.map(this::mapToResponse);
  }

  @Override
  @Transactional
  public WatchlistResponse addToWatchlist(UUID userId, WatchlistRequest request) {
    // Validate title exists
    Title title = titleRepository.findById(request.getTitleId())
        .orElseThrow(() -> new NotFoundException("Title not found"));

    // Check if watchlist item already exists
    Watchlist watchlist = watchlistRepository.findByUserIdAndTitleId(userId, request.getTitleId())
        .orElse(new Watchlist());

    // Set or update fields
    watchlist.setUserId(userId);
    watchlist.setTitleId(request.getTitleId());
    watchlist.setStatus(request.getStatus());

    // Save (insert or update)
    watchlist = watchlistRepository.save(watchlist);

    return mapToResponse(watchlist, title);
  }

  @Override
  @Transactional
  public WatchlistResponse updateWatchlistStatus(
      UUID userId,
      UUID titleId,
      WatchlistUpdateRequest request) {
    Watchlist watchlist = watchlistRepository.findByUserIdAndTitleId(userId, titleId)
        .orElseThrow(() -> new NotFoundException("Watchlist item not found"));

    watchlist.setStatus(request.getStatus());
    watchlist = watchlistRepository.save(watchlist);

    // Fetch title for response
    Title title = titleRepository.findById(titleId)
        .orElseThrow(() -> new NotFoundException("Title not found"));

    return mapToResponse(watchlist, title);
  }

  @Override
  @Transactional
  public void removeFromWatchlist(UUID userId, UUID titleId) {
    Watchlist watchlist = watchlistRepository.findByUserIdAndTitleId(userId, titleId)
        .orElseThrow(() -> new NotFoundException("Watchlist item not found"));

    watchlistRepository.delete(watchlist);
  }

  @Override
  public Optional<WatchlistResponse> getWatchlistItem(UUID userId, UUID titleId) {
    Optional<Watchlist> watchlist = watchlistRepository.findByUserIdAndTitleId(userId, titleId);

    if (watchlist.isEmpty()) {
      return Optional.empty();
    }

    Title title = titleRepository.findById(titleId)
        .orElseThrow(() -> new NotFoundException("Title not found"));

    return Optional.of(mapToResponse(watchlist.get(), title));
  }

  private WatchlistResponse mapToResponse(Watchlist watchlist) {
    WatchlistResponse response = new WatchlistResponse();
    response.setId(watchlist.getId());
    response.setUserId(watchlist.getUserId());
    response.setTitleId(watchlist.getTitleId());
    response.setStatus(watchlist.getStatus());
    response.setCreatedAt(watchlist.getCreatedAt());
    response.setUpdatedAt(watchlist.getUpdatedAt());
    return response;
  }

  private WatchlistResponse mapToResponse(Watchlist watchlist, Title title) {
    WatchlistResponse response = mapToResponse(watchlist);
    response.setTitle(titleDtoMapper.toDto(title));
    return response;
  }
}
