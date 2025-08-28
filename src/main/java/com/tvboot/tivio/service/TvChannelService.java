package com.tvboot.tivio.service;

import com.tvboot.tivio.dto.*;
import com.tvboot.tivio.entities.*;
import com.tvboot.tivio.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TvChannelService {

    private final TvChannelRepository tvChannelRepository;
    private final TvChannelCategoryRepository categoryRepository;
    private final LanguageRepository languageRepository;

    public List<TvChannelDTO> getAllChannels() {
        return tvChannelRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<TvChannelDTO> getAllChannels(Pageable pageable) {
        return tvChannelRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public Optional<TvChannelDTO> getChannelById(Long id) {
        return tvChannelRepository.findById(id)
                .map(this::convertToDTO);
    }

    public Optional<TvChannelDTO> getChannelByNumber(int channelNumber) {
        return tvChannelRepository.findByChannelNumber(channelNumber)
                .map(this::convertToDTO);
    }

    public List<TvChannelDTO> getChannelsByCategory(Long categoryId) {
        return tvChannelRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TvChannelDTO> getChannelsByLanguage(Long languageId) {
        return tvChannelRepository.findByLanguageId(languageId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<TvChannelDTO> searchChannelsByName(String name, Pageable pageable) {
        return tvChannelRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::convertToDTO);
    }

    public TvChannelDTO createChannel(TvChannelCreateDTO createDTO) {
        // Validate unique channel number
        if (tvChannelRepository.findByChannelNumber(createDTO.getChannelNumber()).isPresent()) {
            throw new RuntimeException("Channel number already exists: " + createDTO.getChannelNumber());
        }

        // Validate unique IP and port combination
        if (tvChannelRepository.findByIpAndPort(createDTO.getIp(), createDTO.getPort()).isPresent()) {
            throw new RuntimeException("IP and port combination already exists: " + createDTO.getIp() + ":" + createDTO.getPort());
        }

        TvChannelCategory category = categoryRepository.findById(createDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + createDTO.getCategoryId()));

        Language language = languageRepository.findById(createDTO.getLanguageId())
                .orElseThrow(() -> new RuntimeException("Language not found: " + createDTO.getLanguageId()));

        TvChannel channel = TvChannel.builder()
                .channelNumber(createDTO.getChannelNumber())
                .name(createDTO.getName())
                .description(createDTO.getDescription())
                .ip(createDTO.getIp())
                .port(createDTO.getPort())
                .logoUrl(createDTO.getLogoUrl())
                .category(category)
                .language(language)
                .build();

        TvChannel savedChannel = tvChannelRepository.save(channel);
        return convertToDTO(savedChannel);
    }

    public TvChannelDTO updateChannel(Long id, TvChannelUpdateDTO updateDTO) {
        TvChannel channel = tvChannelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Channel not found: " + id));

        // Update only non-null fields
        if (updateDTO.getChannelNumber() != null) {
            // Check if new channel number is unique (excluding current channel)
            Optional<TvChannel> existingChannel = tvChannelRepository.findByChannelNumber(updateDTO.getChannelNumber());
            if (existingChannel.isPresent() && !existingChannel.get().getId().equals(id)) {
                throw new RuntimeException("Channel number already exists: " + updateDTO.getChannelNumber());
            }
            channel.setChannelNumber(updateDTO.getChannelNumber());
        }

        if (updateDTO.getName() != null) {
            channel.setName(updateDTO.getName());
        }

        if (updateDTO.getDescription() != null) {
            channel.setDescription(updateDTO.getDescription());
        }

        if (updateDTO.getIp() != null || updateDTO.getPort() != null) {
            String newIp = updateDTO.getIp() != null ? updateDTO.getIp() : channel.getIp();
            int newPort = updateDTO.getPort() != null ? updateDTO.getPort() : channel.getPort();

            // Check if new IP and port combination is unique (excluding current channel)
            Optional<TvChannel> existingChannel = tvChannelRepository.findByIpAndPort(newIp, newPort);
            if (existingChannel.isPresent() && !existingChannel.get().getId().equals(id)) {
                throw new RuntimeException("IP and port combination already exists: " + newIp + ":" + newPort);
            }

            channel.setIp(newIp);
            channel.setPort(newPort);
        }

        if (updateDTO.getLogoUrl() != null) {
            channel.setLogoUrl(updateDTO.getLogoUrl());
        }

        if (updateDTO.getCategoryId() != null) {
            TvChannelCategory category = categoryRepository.findById(updateDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found: " + updateDTO.getCategoryId()));
            channel.setCategory(category);
        }

        if (updateDTO.getLanguageId() != null) {
            Language language = languageRepository.findById(updateDTO.getLanguageId())
                    .orElseThrow(() -> new RuntimeException("Language not found: " + updateDTO.getLanguageId()));
            channel.setLanguage(language);
        }

        TvChannel savedChannel = tvChannelRepository.save(channel);
        return convertToDTO(savedChannel);
    }

    public void deleteChannel(Long id) {
        if (!tvChannelRepository.existsById(id)) {
            throw new RuntimeException("Channel not found: " + id);
        }
        tvChannelRepository.deleteById(id);
    }

    private TvChannelDTO convertToDTO(TvChannel channel) {
        TvChannelDTO.CategoryDTO categoryDTO = null;
        if (channel.getCategory() != null) {
            categoryDTO = TvChannelDTO.CategoryDTO.builder()
                    .id(channel.getCategory().getId())
                    .name(channel.getCategory().getName())
                    .description(channel.getCategory().getDescription())
                    .iconUrl(channel.getCategory().getIconUrl())
                    .build();
        }

        TvChannelDTO.LanguageDTO languageDTO = null;
        if (channel.getLanguage() != null) {
            languageDTO = TvChannelDTO.LanguageDTO.builder()
                    .id(channel.getLanguage().getId())
                    .name(channel.getLanguage().getName())
                    .code(channel.getLanguage().getCode())
                    .build();
        }

        return TvChannelDTO.builder()
                .id(channel.getId())
                .channelNumber(channel.getChannelNumber())
                .name(channel.getName())
                .description(channel.getDescription())
                .ip(channel.getIp())
                .port(channel.getPort())
                .logoUrl(channel.getLogoUrl())
                .category(categoryDTO)
                .language(languageDTO)
                .build();
    }
}