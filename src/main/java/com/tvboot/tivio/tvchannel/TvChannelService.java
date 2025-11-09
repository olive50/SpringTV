package com.tvboot.tivio.tvchannel;

import com.tvboot.tivio.tvchannel.dto.TvChannelCreateDTO;
import com.tvboot.tivio.tvchannel.dto.TvChannelResponseDTO;
import com.tvboot.tivio.tvchannel.dto.TvChannelStatsDTO;
import com.tvboot.tivio.tvchannel.dto.TvChannelUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface TvChannelService {

    // Pagination methods
    Page<TvChannel> getChannels(int page, int size);


//    List<TvChannel> getChannels();

    Page<TvChannel> getChannelsByCategory(String category, int page, int size);

    Page<TvChannel> getGuestChannels(int page, int size);
    List<TvChannel> getActiveAvailableChannels();


    Page<TvChannel> getChannelsByLanguage(String language, int page, int size);

    Page<TvChannel> searchChannels(String search, int page, int size);

    // Count methods
    long countChannels();

    long countChannelsByCategory(String category);

    long countGuestChannels();

    long countSearchChannels(String search);

    // CRUD operations
    TvChannel createChannel(TvChannelCreateDTO createDTO);

//    TvChannel updateChannel(Long id, TvChannel tvChannel);
   TvChannelResponseDTO updateChannel(Long id, TvChannelUpdateDTO updateDTO);

    void deleteChannel(Long id);

    Optional<TvChannel> getChannelById(Long id);

    Optional<TvChannel> getChannelByNumber(Integer channelNumber);

    // Utility methods
    List<String> getAllCategories();

    List<String> getAllLanguages();

    // Bulk operations
    List<TvChannel> createChannelsInBulk(List<TvChannel> channels);

    void updateChannelOrder(Long channelId, Integer newOrder);

    TvChannelResponseDTO createChannelWithLogo(TvChannelCreateDTO createDTO, MultipartFile logoFile);
    TvChannelResponseDTO updateChannelWithLogo(Long id, TvChannelUpdateDTO updateDTO, MultipartFile logoFile);

    TvChannelStatsDTO getChannelStatistics();

    Page<TvChannel> getChannels(int page, int size, String q, Long categoryId, Long languageId, Boolean isActive);
}
