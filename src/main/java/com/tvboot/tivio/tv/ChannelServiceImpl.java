package com.tvboot.tivio.tv;

import com.tvboot.tivio.common.util.FileStorageService;
import com.tvboot.tivio.tv.dto.TvChannelCreateDTO;
import com.tvboot.tivio.tv.dto.TvChannelDTO;
import com.tvboot.tivio.tv.dto.TvChannelMapper;
import com.tvboot.tivio.tv.dto.TvChannelStatsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChannelServiceImpl implements ChannelService{

    private final ChannelRepository channelRepository;
    private final TvChannelRepository tvChannelRepository;
    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private TvChannelMapper mapper;

    private static final String LOGO_DIR = "channel-logos";

    private String baseUrl = "http://localhost:8080";
    private String logosPath = "/uploads/logos/channels";

    @Override
    @Transactional(readOnly = true)
    public Page<TvChannel> getChannels(int page, int size) {
        log.debug("Getting channels - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return channelRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TvChannel> getChannelsByCategory(String category, int page, int size) {
        log.debug("Getting channels by category: {} - page: {}, size: {}", category, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return channelRepository.findByActiveTrueAndCategoryOrderBySortOrderAscNameAsc(
                category, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TvChannel> getGuestChannels(int page, int size) {
        log.debug("Getting guest channels - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return channelRepository.findByActiveTrueAndAvailableTrueOrderBySortOrderAscNameAsc(
                pageable);
    }



    @Override
    @Transactional(readOnly = true)
    public Page<TvChannel> getChannelsByLanguage(String language, int page, int size) {
        log.debug("Getting channels by language: {} - page: {}, size: {}", language, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return channelRepository.findByActiveTrueAndLanguageOrderBySortOrderAscNameAsc(
                language, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TvChannel> searchChannels(String search, int page, int size) {
        log.debug("Searching channels with term: '{}' - page: {}, size: {}", search, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return channelRepository.searchChannels(search, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countChannels() {
        log.debug("Counting all active channels");
        return channelRepository.countByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public long countChannelsByCategory(String category) {
        log.debug("Counting channels by category: {}", category);
        return channelRepository.countByActiveTrueAndCategory_name(category);
    }

    @Override
    @Transactional(readOnly = true)
    public long countGuestChannels() {
        log.debug("Counting guest channels");
        return channelRepository.countByActiveTrueAndAvailableTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public long countSearchChannels(String search) {
        log.debug("Counting search results for: '{}'", search);
        return channelRepository.countSearchChannels(search);
    }

    @Override
    public TvChannel createChannel(TvChannel tvChannel) {
        log.info("Creating new tvChannel: {}", tvChannel.getName());

        // Validate unique tvChannel number
        if (channelRepository.findByChannelNumberAndActiveTrue(tvChannel.getChannelNumber()).isPresent()) {
            throw new IllegalArgumentException("TvChannel number already exists: " + tvChannel.getChannelNumber());
        }


        if (tvChannel.getActive() == null) {
            tvChannel.setActive(true);
        }
        if (tvChannel.getAvailable() == null) {
            tvChannel.setAvailable(true);
        }


        return channelRepository.save(tvChannel);
    }

    @Override
    public TvChannel updateChannel(Long id, TvChannel channelDetails) {
        log.info("Updating tvChannel with id: {}", id);

        TvChannel existingChannel = channelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TvChannel not found with id: " + id));

        // Check if tvChannel number is being changed and if it's unique
        if (!(existingChannel.getChannelNumber() ==(channelDetails.getChannelNumber()))) {
            if (channelRepository.findByChannelNumberAndActiveTrue(channelDetails.getChannelNumber()).isPresent()) {
                throw new IllegalArgumentException("TvChannel number already exists: " + channelDetails.getChannelNumber());
            }
        }

        // Update fields
        existingChannel.setChannelNumber(channelDetails.getChannelNumber());
        existingChannel.setName(channelDetails.getName());
        existingChannel.setDescription(channelDetails.getDescription());
        existingChannel.setStreamUrl(channelDetails.getStreamUrl());
        existingChannel.setCategory(channelDetails.getCategory());
        existingChannel.setLanguage(channelDetails.getLanguage());
        existingChannel.setActive(channelDetails.getActive());

        existingChannel.setSortOrder(channelDetails.getSortOrder());
        existingChannel.setAvailable(channelDetails.getAvailable());


        return channelRepository.save(existingChannel);
    }

    @Override
    public void deleteChannel(Long id) {
        log.info("Deleting tvChannel with id: {}", id);

        TvChannel tvChannel = channelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TvChannel not found with id: " + id));

        // Soft delete - set isActive to false
        channelRepository.delete(tvChannel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TvChannel> getChannelById(Long id) {
        log.debug("Getting tvChannel by id: {}", id);
        return channelRepository.findById(id)
                .filter(TvChannel::getActive);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TvChannel> getChannelByNumber(Integer channelNumber) {
        log.debug("Getting tvChannel by number: {}", channelNumber);
        return channelRepository.findByChannelNumberAndActiveTrue(channelNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        log.debug("Getting all tvChannel categories");
        return channelRepository.findAllCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllLanguages() {
        log.debug("Getting all tvChannel languages");
        return channelRepository.findAllLanguages();
    }

    @Override
    public List<TvChannel> createChannelsInBulk(List<TvChannel> channels) {
        log.info("Creating {} channels in bulk", channels.size());

        // Validate each tvChannel
        for (TvChannel tvChannel : channels) {
            if (channelRepository.findByChannelNumberAndActiveTrue(tvChannel.getChannelNumber()).isPresent()) {
                throw new IllegalArgumentException("TvChannel number already exists: " + tvChannel.getChannelNumber());
            }

            // Set defaults

            if (tvChannel.getActive() == null) tvChannel.setActive(true);
            if (tvChannel.getAvailable() == null) tvChannel.setAvailable(true);

                   }

        return channelRepository.saveAll(channels);
    }

    @Override
    public void updateChannelOrder(Long channelId, Integer newOrder) {
        log.info("Updating tvChannel order - id: {}, new order: {}", channelId, newOrder);

        TvChannel tvChannel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("TvChannel not found with id: " + channelId));

        tvChannel.setSortOrder(newOrder);
        channelRepository.save(tvChannel);
    }

    public TvChannelDTO createChannelWithLogo(TvChannelCreateDTO createDTO, MultipartFile logoFile) {
        // Save channel first
        TvChannel channel = mapper.toEntity(createDTO);

        // Handle logo upload
        if (logoFile != null && !logoFile.isEmpty()) {
            String logoPath = fileStorageService.storeFile(logoFile, LOGO_DIR);
            channel.setLogoPath(logoPath);

        }

        channel = channelRepository.save(channel);
        return mapper.toDTO(channel);
    }

    public String getFullLogoUrl(String logoPath) {
        // If it's already a full URL, return as is
        if (logoPath == null || logoPath.startsWith("http://") || logoPath.startsWith("https://")) {
            return logoPath;
        }

        // If logoPath starts with /, remove it to avoid double slashes
        if (logoPath.startsWith("/")) {
            logoPath = logoPath.substring(1);
        }

        return baseUrl + "/" + logoPath;
    }

    /**
     * Get comprehensive channel statistics
     */
    public TvChannelStatsDTO getChannelStatistics() {
        // Get basic counts
        long totalChannels = tvChannelRepository.count();
        long activeChannels = tvChannelRepository.countByActive(true);
        long inactiveChannels = tvChannelRepository.countByActive(false);

        // Get category statistics
        Map<String, Long> categoryStats = new HashMap<>();
        List<Object[]> categoryResults = tvChannelRepository.countByCategory();
        for (Object[] result : categoryResults) {
            String categoryName = (String) result[0];
            Long count = (Long) result[1];
            categoryStats.put(categoryName, count);
        }

        // Get language statistics
        Map<String, Long> languageStats = new HashMap<>();
        List<Object[]> languageResults = tvChannelRepository.countByLanguage();
        for (Object[] result : languageResults) {
            String languageName = (String) result[0];
            Long count = (Long) result[1];
            languageStats.put(languageName, count);
        }

        return TvChannelStatsDTO.builder()
                .total(totalChannels)
                .active(activeChannels)
                .inactive(inactiveChannels)
                .byCategory(categoryStats)
                .byLanguage(languageStats)
                .build();
    }



}
