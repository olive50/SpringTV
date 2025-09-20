package com.tvboot.tivio.tv;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ChannelService {
    // Pagination methods
    Page<TvChannel> getChannels(int page, int size);

    Page<TvChannel> getChannelsByCategory(String category, int page, int size);

    Page<TvChannel> getGuestChannels(int page, int size);

    Page<TvChannel> getPremiumChannels(int page, int size);

    Page<TvChannel> getChannelsByLanguage(String language, int page, int size);

    Page<TvChannel> searchChannels(String search, int page, int size);

    // Count methods
    long countChannels();

    long countChannelsByCategory(String category);

    long countGuestChannels();

    long countSearchChannels(String search);

    // CRUD operations
    TvChannel createChannel(TvChannel tvChannel);

    TvChannel updateChannel(Long id, TvChannel tvChannel);

    void deleteChannel(Long id);

    Optional<TvChannel> getChannelById(Long id);

    Optional<TvChannel> getChannelByNumber(Integer channelNumber);

    // Utility methods
    List<String> getAllCategories();

    List<String> getAllLanguages();

    // Bulk operations
    List<TvChannel> createChannelsInBulk(List<TvChannel> channels);

    void updateChannelOrder(Long channelId, Integer newOrder);
}
