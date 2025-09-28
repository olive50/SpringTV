package com.tvboot.tivio.tv;

import com.tvboot.tivio.common.exception.BusinessException;
import com.tvboot.tivio.common.exception.ResourceNotFoundException;
import com.tvboot.tivio.common.util.FileStorageService;
import com.tvboot.tivio.language.Language;
import com.tvboot.tivio.language.LanguageRepository;
import com.tvboot.tivio.tv.dto.*;
import com.tvboot.tivio.tv.tvcategory.TvChannelCategory;
import com.tvboot.tivio.tv.tvcategory.TvChannelCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class TvChannelServiceImpl implements TvChannelService {

    private final TvChannelRepository tvChannelRepository;
//    private final TvChannelRepository tvChannelRepository;

    private final TvChannelCategoryRepository categoryRepository;
    private final LanguageRepository languageRepository;

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
        return tvChannelRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TvChannel> getChannelsByCategory(String category, int page, int size) {
        log.debug("Getting channels by category: {} - page: {}, size: {}", category, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return tvChannelRepository.findByActiveTrueAndCategoryOrderBySortOrderAscNameAsc(
                category, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TvChannel> getGuestChannels(int page, int size) {
        log.debug("Getting guest channels - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return tvChannelRepository.findByActiveTrueAndAvailableTrueOrderBySortOrderAscNameAsc(
                pageable);
    }



    @Override
    @Transactional(readOnly = true)
    public Page<TvChannel> getChannelsByLanguage(String language, int page, int size) {
        log.debug("Getting channels by language: {} - page: {}, size: {}", language, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return tvChannelRepository.findByActiveTrueAndLanguage_NameOrderBySortOrderAscNameAsc(
                language, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TvChannel> searchChannels(String search, int page, int size) {
        log.debug("Searching channels with term: '{}' - page: {}, size: {}", search, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return tvChannelRepository.searchChannels(search, pageable);
    }

    @Override
    public Page<TvChannel> getChannels(int page, int size, String q, Long categoryId, Long languageId, Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("channelNumber").ascending());

        // Simplest version: adjust according to your repo methods
        return tvChannelRepository.findAllWithFilters(q, categoryId, languageId, isActive, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countChannels() {
        log.debug("Counting all active channels");
        return tvChannelRepository.countByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public long countChannelsByCategory(String category) {
        log.debug("Counting channels by category: {}", category);
        return tvChannelRepository.countByActiveTrueAndCategory_name(category);
    }

    @Override
    @Transactional(readOnly = true)
    public long countGuestChannels() {
        log.debug("Counting guest channels");
        return tvChannelRepository.countByActiveTrueAndAvailableTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public long countSearchChannels(String search) {
        log.debug("Counting search results for: '{}'", search);
        return tvChannelRepository.countSearchChannels(search);
    }

    @Override
    public TvChannel createChannel(TvChannelCreateDTO createDTO) {
        log.info("Creating new tvChannel: {}", createDTO.getName());

        // Validate unique tvChannel number
        if (tvChannelRepository.findByChannelNumberAndActiveTrue(createDTO.getChannelNumber()).isPresent()) {
            throw new IllegalArgumentException("TvChannel number already exists: " + createDTO.getChannelNumber());
        }


        if (createDTO.getActive() == null) {
            createDTO.setActive(true);
        }
        if (createDTO.getAvailable() == null) {
            createDTO.setAvailable(true);
        }

        TvChannel channel = mapper.toEntity(createDTO);

        if (createDTO.getCategoryId() != null) {
            TvChannelCategory category = categoryRepository.findById(createDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", createDTO.getCategoryId()));
            channel.setCategory(category);
            log.debug("set channel category to: {}", category.getName());
        }

        if (createDTO.getLanguageId() != null) {
            Language language = languageRepository.findById(createDTO.getLanguageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Language", "id", createDTO.getLanguageId()));
            channel.setLanguage(language);
            log.debug("set channel language to: {}", language.getName());
        }

        return tvChannelRepository.save(channel);
    }
/*
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
*/
   @Override
    public TvChannelResponseDTO updateChannel(Long id, TvChannelUpdateDTO updateDTO) {
        log.info("Updating TV channel with ID: {}", id);

        TvChannel channel = tvChannelRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("TV channel not found for update with ID: {}", id);
                    return new ResourceNotFoundException("TV Channel", "id", id);
                });

        String originalName = channel.getName();

        // Mise à jour conditionnelle des champs
        if (updateDTO.getChannelNumber() != null) {
            validateChannelNumber(updateDTO.getSortOrder() , id);
            channel.setChannelNumber(updateDTO.getChannelNumber());
            log.debug("Updated channel number to {}", updateDTO.getChannelNumber());
        }
       if (updateDTO.getSortOrder() != null) {
           validateChannelSortOrder(updateDTO.getSortOrder(), id);
           channel.setSortOrder(updateDTO.getSortOrder());
           log.debug("Updated channel number to {}", updateDTO.getChannelNumber());
       }

        if (updateDTO.getName() != null) {
            channel.setName(updateDTO.getName());
            log.debug("Updated channel name from '{}' to '{}'", originalName, updateDTO.getName());
        }

        if (updateDTO.getDescription() != null) {
            channel.setDescription(updateDTO.getDescription());
        }

        if (updateDTO.getIp() != null || updateDTO.getPort() != null) {
            String newIp = updateDTO.getIp() != null ? updateDTO.getIp() : channel.getIp();
            int newPort = updateDTO.getPort() != null ? updateDTO.getPort() : channel.getPort();

            validateIpPortCombination(newIp, newPort, id);

            channel.setIp(newIp);
            channel.setPort(newPort);
            log.debug("Updated IP:Port to {}:{}", newIp, newPort);
        }


        if (updateDTO.getCategoryId() != null) {
            TvChannelCategory category = categoryRepository.findById(updateDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", updateDTO.getCategoryId()));
            channel.setCategory(category);
            log.debug("Updated channel category to: {}", category.getName());
        }

        if (updateDTO.getLanguageId() != null) {
            Language language = languageRepository.findById(updateDTO.getLanguageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Language", "id", updateDTO.getLanguageId()));
            channel.setLanguage(language);
            log.debug("Updated channel language to: {}", language.getName());
        }

        TvChannel savedChannel = tvChannelRepository.save(channel);

        log.info("Successfully updated TV channel: {} (ID: {})",
                savedChannel.getName(), savedChannel.getId());

        return  mapper.toDTO(savedChannel);
    }

    @Override
    @Transactional
    public void deleteChannel(Long id) {
        log.info("Deleting tvChannel with id: {}", id);

        TvChannel tvChannel = tvChannelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TvChannel not found with id: " + id));
     fileStorageService.deleteFile(tvChannel.getLogoPath());
        // Soft delete - set isActive to false
        tvChannelRepository.delete(tvChannel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TvChannel> getChannelById(Long id) {
        log.debug("Getting tvChannel by id: {}", id);
        return tvChannelRepository.findById(id);
//             .filter(TvChannel::getActive);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TvChannel> getChannelByNumber(Integer channelNumber) {
        log.debug("Getting tvChannel by number: {}", channelNumber);
        return tvChannelRepository.findByChannelNumberAndActiveTrue(channelNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        log.debug("Getting all tvChannel categories");
        return tvChannelRepository.findAllCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllLanguages() {
        log.debug("Getting all tvChannel languages");
        return tvChannelRepository.findAllLanguages();
    }

    @Override
    public List<TvChannel> createChannelsInBulk(List<TvChannel> channels) {
        log.info("Creating {} channels in bulk", channels.size());

        // Validate each tvChannel
        for (TvChannel tvChannel : channels) {
            if (tvChannelRepository.findByChannelNumberAndActiveTrue(tvChannel.getChannelNumber()).isPresent()) {
                throw new IllegalArgumentException("TvChannel number already exists: " + tvChannel.getChannelNumber());
            }

            // Set defaults

            if (tvChannel.getActive() == null) tvChannel.setActive(true);
            if (tvChannel.getAvailable() == null) tvChannel.setAvailable(true);

                   }

        return tvChannelRepository.saveAll(channels);
    }

    @Override
    public void updateChannelOrder(Long channelId, Integer newOrder) {
        log.info("Updating tvChannel order - id: {}, new order: {}", channelId, newOrder);

        TvChannel tvChannel = tvChannelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("TvChannel not found with id: " + channelId));

        tvChannel.setSortOrder(newOrder);
        tvChannelRepository.save(tvChannel);
    }

    @Override
    public TvChannelResponseDTO createChannelWithLogo(TvChannelCreateDTO createDTO, MultipartFile logoFile) {
        // Save channel first
        TvChannel channel = mapper.toEntity(createDTO);

        // Handle logo upload with custom filename
        if (logoFile != null && !logoFile.isEmpty()) {
            // Generate custom filename using channel name and number
            String customFilename = fileStorageService.generateChannelLogoFilename(
                    createDTO.getName(),           // Use DTO name since entity might not have all fields set
                    createDTO.getChannelNumber(),  // Use DTO channel number
                    logoFile.getOriginalFilename()
            );

            // Store file with custom name instead of random UUID
            String logoPath = fileStorageService.storeFileWithCustomName(logoFile, LOGO_DIR, customFilename);
            channel.setLogoPath(logoPath);

            log.info("Saved logo with custom name: {}", logoPath);
        }

        if (createDTO.getCategoryId() != null) {
            TvChannelCategory category = categoryRepository.findById(createDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", createDTO.getCategoryId()));
            channel.setCategory(category);
            log.debug("set channel category to: {}", category.getName());
        }

        if (createDTO.getLanguageId() != null) {
            Language language = languageRepository.findById(createDTO.getLanguageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Language", "id", createDTO.getLanguageId()));
            channel.setLanguage(language);
            log.debug("set channel language to: {}", language.getName());
        }

        channel = tvChannelRepository.save(channel);
        return mapper.toDTO(channel);
    }

    @Override
    public TvChannelResponseDTO updateChannelWithLogo(Long id, TvChannelUpdateDTO updateDTO, MultipartFile logoFile) {
        log.info("Updating TV channel with logo - ID: {}", id);

        TvChannel channel = tvChannelRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("TV channel not found for update with ID: {}", id);
                    return new ResourceNotFoundException("TV Channel", "id", id);
                });

        String oldLogoPath = channel.getLogoPath();

        // Update basic channel info first
        updateChannelBasicInfo(channel, updateDTO);

        // Handle logo update if provided
        if (logoFile != null && !logoFile.isEmpty()) {
            // Delete old logo if exists
            if (oldLogoPath != null) {
                fileStorageService.deleteFile(oldLogoPath);
                log.info("Deleted old logo: {}", oldLogoPath);
            }

            // Generate custom filename and save new logo
            String customFilename = fileStorageService.generateChannelLogoFilename(
                    channel.getName(),
                    channel.getChannelNumber(),
                    logoFile.getOriginalFilename()
            );

            String newLogoPath = fileStorageService.storeFileWithCustomName(
                    logoFile,
                    LOGO_DIR,
                    customFilename
            );

            channel.setLogoPath(newLogoPath);
            log.info("Saved new logo: {}", newLogoPath);
        }

        TvChannel savedChannel = tvChannelRepository.save(channel);
        log.info("Successfully updated TV channel with logo: {}", savedChannel.getName());

        return mapper.toDTO(savedChannel);
    }

    private void updateChannelBasicInfo(TvChannel channel, TvChannelUpdateDTO updateDTO) {
        if (updateDTO.getChannelNumber() != null) {
            validateChannelNumber(updateDTO.getChannelNumber(), channel.getId());
            channel.setChannelNumber(updateDTO.getChannelNumber());
        }

        if (updateDTO.getName() != null) {
            channel.setName(updateDTO.getName());
        }

        if (updateDTO.getDescription() != null) {
            channel.setDescription(updateDTO.getDescription());
        }

        if (updateDTO.getIp() != null) {
            channel.setIp(updateDTO.getIp());
        }

        if (updateDTO.getPort() != null) {
            channel.setPort(updateDTO.getPort());
        }

        if (updateDTO.getStreamUrl() != null) {
            channel.setStreamUrl(updateDTO.getStreamUrl());
        }

        // Update category if provided
        if (updateDTO.getCategoryId() != null) {
            TvChannelCategory category = categoryRepository.findById(updateDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", updateDTO.getCategoryId()));
            channel.setCategory(category);
        }

        // Update language if provided
        if (updateDTO.getLanguageId() != null) {
            Language language = languageRepository.findById(updateDTO.getLanguageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Language", "id", updateDTO.getLanguageId()));
            channel.setLanguage(language);
        }

        if (updateDTO.getActive() != null) {
            channel.setActive(updateDTO.getActive());
            log.debug("Updated active status to: {}", updateDTO.getActive());
        } else {
            log.debug("Keeping existing active status: {}", channel.getActive());
        }
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

    // Méthodes de validation privées
    private void validateChannelNumber(int channelNumber, Long excludeId) {
        Optional<TvChannel> existingChannel = tvChannelRepository.findByChannelNumber(channelNumber);

        if (existingChannel.isPresent() &&
                (excludeId == null || !existingChannel.get().getId().equals(excludeId))) {
            log.warn("Channel number {} already exists for channel: {}",
                    channelNumber, existingChannel.get().getName());

            Map<String, Object> data = new HashMap<>();
            data.put("channelNumber", channelNumber);
            data.put("existingChannelName", existingChannel.get().getName());
            data.put("existingChannelId", existingChannel.get().getId());

            throw new BusinessException(
                    "Channel number " + channelNumber + " is already in use",
                    data
            );
        }
    }
    private void validateChannelSortOrder(int sortOrder, Long excludeId) {
        Optional<TvChannel> existingChannel = tvChannelRepository.findBySortOrder(sortOrder);

        if (existingChannel.isPresent() &&
                (excludeId == null || !existingChannel.get().getId().equals(excludeId))) {
            log.warn("Channel sort order {} already exists for channel: {}",
                    sortOrder, existingChannel.get().getName());

            Map<String, Object> data = new HashMap<>();
            data.put("sortOrder", sortOrder);
            data.put("existingChannelName", existingChannel.get().getName());
            data.put("existingChannelId", existingChannel.get().getId());

            throw new BusinessException(
                    "Channel sort order " + sortOrder + " is already in use",
                    data
            );
        }
    }

    private void validateIpPortCombination(String ip, int port, Long excludeId) {
        Optional<TvChannel> existingChannel = tvChannelRepository.findByIpAndPort(ip, port);

        if (existingChannel.isPresent() &&
                (excludeId == null || !existingChannel.get().getId().equals(excludeId))) {
            log.warn("IP:Port combination {}:{} already exists for channel: {}",
                    ip, port, existingChannel.get().getName());

            Map<String, Object> data = new HashMap<>();
            data.put("ip", ip);
            data.put("port", port);
            data.put("existingChannelName", existingChannel.get().getName());
            data.put("existingChannelId", existingChannel.get().getId());

            throw new BusinessException(
                    "IP and port combination " + ip + ":" + port + " is already in use",
                    data
            );
        }
    }


}
