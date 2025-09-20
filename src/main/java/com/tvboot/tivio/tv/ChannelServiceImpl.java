package com.tvboot.tivio.tv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChannelServiceImpl implements ChannelService{

    private final ChannelRepository channelRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<TvChannel> getChannels(int page, int size) {
        log.debug("Getting channels - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return channelRepository.findByIsActiveTrueOrderBySortOrderAscNameAsc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TvChannel> getChannelsByCategory(String category, int page, int size) {
        log.debug("Getting channels by category: {} - page: {}, size: {}", category, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return channelRepository.findByIsActiveTrueAndCategoryOrderBySortOrderAscNameAsc(
                category, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TvChannel> getGuestChannels(int page, int size) {
        log.debug("Getting guest channels - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return channelRepository.findByIsActiveTrueAndIsAvailableTrueOrderBySortOrderAscNameAsc(
                pageable);
    }



    @Override
    @Transactional(readOnly = true)
    public Page<TvChannel> getChannelsByLanguage(String language, int page, int size) {
        log.debug("Getting channels by language: {} - page: {}, size: {}", language, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return channelRepository.findByIsActiveTrueAndLanguageOrderBySortOrderAscNameAsc(
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
        return channelRepository.countByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public long countChannelsByCategory(String category) {
        log.debug("Counting channels by category: {}", category);
        return channelRepository.countByIsActiveTrueAndCategory_name(category);
    }

    @Override
    @Transactional(readOnly = true)
    public long countGuestChannels() {
        log.debug("Counting guest channels");
        return channelRepository.countByIsActiveTrueAndIsAvailableTrue();
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
        if (channelRepository.findByChannelNumberAndIsActiveTrue(tvChannel.getChannelNumber()).isPresent()) {
            throw new IllegalArgumentException("TvChannel number already exists: " + tvChannel.getChannelNumber());
        }


        if (tvChannel.getIsActive() == null) {
            tvChannel.setIsActive(true);
        }
        if (tvChannel.getIsAvailable() == null) {
            tvChannel.setIsAvailable(true);
        }

        if (tvChannel.getIsHD() == null) {
            tvChannel.setIsHD(false);
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
            if (channelRepository.findByChannelNumberAndIsActiveTrue(channelDetails.getChannelNumber()).isPresent()) {
                throw new IllegalArgumentException("TvChannel number already exists: " + channelDetails.getChannelNumber());
            }
        }

        // Update fields
        existingChannel.setChannelNumber(channelDetails.getChannelNumber());
        existingChannel.setName(channelDetails.getName());
        existingChannel.setDescription(channelDetails.getDescription());
        existingChannel.setStreamUrl(channelDetails.getStreamUrl());
        existingChannel.setLogoUrl(channelDetails.getLogoUrl());
        existingChannel.setCategory(channelDetails.getCategory());
        existingChannel.setLanguage(channelDetails.getLanguage());
        existingChannel.setIsActive(channelDetails.getIsActive());
        existingChannel.setIsHD(channelDetails.getIsHD());
        existingChannel.setSortOrder(channelDetails.getSortOrder());
        existingChannel.setIsAvailable(channelDetails.getIsAvailable());


        return channelRepository.save(existingChannel);
    }

    @Override
    public void deleteChannel(Long id) {
        log.info("Deleting tvChannel with id: {}", id);

        TvChannel tvChannel = channelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TvChannel not found with id: " + id));

        // Soft delete - set isActive to false
        tvChannel.setIsActive(false);
        channelRepository.save(tvChannel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TvChannel> getChannelById(Long id) {
        log.debug("Getting tvChannel by id: {}", id);
        return channelRepository.findById(id)
                .filter(TvChannel::getIsActive);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TvChannel> getChannelByNumber(Integer channelNumber) {
        log.debug("Getting tvChannel by number: {}", channelNumber);
        return channelRepository.findByChannelNumberAndIsActiveTrue(channelNumber);
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
            if (channelRepository.findByChannelNumberAndIsActiveTrue(tvChannel.getChannelNumber()).isPresent()) {
                throw new IllegalArgumentException("TvChannel number already exists: " + tvChannel.getChannelNumber());
            }

            // Set defaults
            if (tvChannel.getSortOrder() == null) tvChannel.setSortOrder(0);
            if (tvChannel.getIsActive() == null) tvChannel.setIsActive(true);
            if (tvChannel.getIsAvailable() == null) tvChannel.setIsAvailable(true);

            if (tvChannel.getIsHD() == null) tvChannel.setIsHD(false);
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
}
