package com.tvboot.tivio.service;

import com.tvboot.tivio.dto.*;
import com.tvboot.tivio.entities.*;
import com.tvboot.tivio.exception.*;
import com.tvboot.tivio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TvChannelService {

    private final TvChannelRepository tvChannelRepository;
    private final TvChannelCategoryRepository categoryRepository;
    private final LanguageRepository languageRepository;

    public List<TvChannelDTO> getAllChannels() {
        log.debug("Fetching all TV channels");

        try {
            List<TvChannel> channels = tvChannelRepository.findAll();
            log.info("Successfully retrieved {} TV channels", channels.size());

            return channels.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching all TV channels", e);
            throw new TvBootException("Failed to retrieve TV channels", "FETCH_CHANNELS_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public Page<TvChannelDTO> getAllChannels(Pageable pageable) {
        log.debug("Fetching TV channels with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<TvChannel> channels = tvChannelRepository.findAll(pageable);
            log.info("Successfully retrieved {} TV channels (page {}/{})",
                    channels.getNumberOfElements(),
                    channels.getNumber() + 1,
                    channels.getTotalPages());

            return channels.map(this::convertToDTO);

        } catch (Exception e) {
            log.error("Error fetching paginated TV channels", e);
            throw new TvBootException("Failed to retrieve paginated TV channels", "FETCH_CHANNELS_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public Optional<TvChannelDTO> getChannelById(Long id) {
        log.debug("Fetching TV channel by ID: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid channel ID provided: {}", id);
            throw new ValidationException("Channel ID must be a positive number");
        }

        try {
            Optional<TvChannel> channel = tvChannelRepository.findById(id);

            if (channel.isPresent()) {
                log.info("Successfully found TV channel: {} (ID: {})",
                        channel.get().getName(), id);
                MDC.put("channelName", channel.get().getName());
                return channel.map(this::convertToDTO);
            } else {
                log.warn("TV channel not found with ID: {}", id);
                return Optional.empty();
            }

        } catch (Exception e) {
            log.error("Error fetching TV channel by ID: {}", id, e);
            throw new TvBootException("Failed to retrieve TV channel", "FETCH_CHANNEL_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public Optional<TvChannelDTO> getChannelByNumber(int channelNumber) {
        log.debug("Fetching TV channel by number: {}", channelNumber);

        try {
            Optional<TvChannel> channel = tvChannelRepository.findByChannelNumber(channelNumber);

            if (channel.isPresent()) {
                log.info("Successfully found TV channel: {} (Number: {})",
                        channel.get().getName(), channelNumber);
                return channel.map(this::convertToDTO);
            } else {
                log.warn("TV channel not found with number: {}", channelNumber);
                return Optional.empty();
            }

        } catch (Exception e) {
            log.error("Error fetching TV channel by number: {}", channelNumber, e);
            throw new TvBootException("Failed to retrieve TV channel", "FETCH_CHANNEL_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public List<TvChannelDTO> getChannelsByCategory(Long categoryId) {
        log.debug("Fetching TV channels by category ID: {}", categoryId);

        if (categoryId == null || categoryId <= 0) {
            throw new ValidationException("Category ID must be a positive number");
        }

        try {
            List<TvChannel> channels = tvChannelRepository.findByCategoryId(categoryId);
            log.info("Successfully retrieved {} TV channels for category ID: {}",
                    channels.size(), categoryId);

            return channels.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching TV channels by category ID: {}", categoryId, e);
            throw new TvBootException("Failed to retrieve TV channels by category", "FETCH_CHANNELS_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public List<TvChannelDTO> getChannelsByLanguage(Long languageId) {
        log.debug("Fetching TV channels by language ID: {}", languageId);

        if (languageId == null || languageId <= 0) {
            throw new ValidationException("Language ID must be a positive number");
        }

        try {
            List<TvChannel> channels = tvChannelRepository.findByLanguageId(languageId);
            log.info("Successfully retrieved {} TV channels for language ID: {}",
                    channels.size(), languageId);

            return channels.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching TV channels by language ID: {}", languageId, e);
            throw new TvBootException("Failed to retrieve TV channels by language", "FETCH_CHANNELS_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public Page<TvChannelDTO> searchChannelsByName(String name, Pageable pageable) {
        log.debug("Searching TV channels by name: '{}' with pagination", name);

        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Search name cannot be empty");
        }

        try {
            Page<TvChannel> channels = tvChannelRepository.findByNameContainingIgnoreCase(name, pageable);
            log.info("Successfully found {} TV channels matching name: '{}'",
                    channels.getNumberOfElements(), name);

            return channels.map(this::convertToDTO);

        } catch (Exception e) {
            log.error("Error searching TV channels by name: '{}'", name, e);
            throw new TvBootException("Failed to search TV channels", "SEARCH_CHANNELS_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public TvChannelDTO createChannel(TvChannelCreateDTO createDTO) {
        log.info("Creating new TV channel: {}", createDTO.getName());

        try {
            // Validation
            validateChannelNumber(createDTO.getChannelNumber(), null);
            validateIpPortCombination(createDTO.getIp(), createDTO.getPort(), null);

            // Récupérer les entités liées
            TvChannelCategory category = categoryRepository.findById(createDTO.getCategoryId())
                    .orElseThrow(() -> {
                        log.warn("Category not found with ID: {}", createDTO.getCategoryId());
                        return new ResourceNotFoundException("Category", createDTO.getCategoryId());
                    });

            Language language = languageRepository.findById(createDTO.getLanguageId())
                    .orElseThrow(() -> {
                        log.warn("Language not found with ID: {}", createDTO.getLanguageId());
                        return new ResourceNotFoundException("Language", createDTO.getLanguageId());
                    });

            // Créer la chaîne
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

            log.info("Successfully created TV channel: {} (ID: {}, Number: {})",
                    savedChannel.getName(), savedChannel.getId(), savedChannel.getChannelNumber());

            // Ajouter des métadonnées au contexte de logging
            MDC.put("channelId", savedChannel.getId().toString());
            MDC.put("channelNumber", String.valueOf(savedChannel.getChannelNumber()));

            return convertToDTO(savedChannel);

        } catch (TvBootException e) {
            throw e; // Re-lancer les exceptions métier
        } catch (Exception e) {
            log.error("Unexpected error creating TV channel: {}", createDTO.getName(), e);
            throw new TvBootException("Failed to create TV channel", "CREATE_CHANNEL_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public TvChannelDTO updateChannel(Long id, TvChannelUpdateDTO updateDTO) {
        log.info("Updating TV channel with ID: {}", id);

        try {
            TvChannel channel = tvChannelRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("TV channel not found for update with ID: {}", id);
                        return new ResourceNotFoundException("TV Channel", id);
                    });

            String originalName = channel.getName();

            // Mise à jour conditionnelle des champs
            if (updateDTO.getChannelNumber() != null) {
                validateChannelNumber(updateDTO.getChannelNumber(), id);
                channel.setChannelNumber(updateDTO.getChannelNumber());
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

            if (updateDTO.getLogoUrl() != null) {
                channel.setLogoUrl(updateDTO.getLogoUrl());
            }

            if (updateDTO.getCategoryId() != null) {
                TvChannelCategory category = categoryRepository.findById(updateDTO.getCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Category", updateDTO.getCategoryId()));
                channel.setCategory(category);
                log.debug("Updated channel category to: {}", category.getName());
            }

            if (updateDTO.getLanguageId() != null) {
                Language language = languageRepository.findById(updateDTO.getLanguageId())
                        .orElseThrow(() -> new ResourceNotFoundException("Language", updateDTO.getLanguageId()));
                channel.setLanguage(language);
                log.debug("Updated channel language to: {}", language.getName());
            }

            TvChannel savedChannel = tvChannelRepository.save(channel);

            log.info("Successfully updated TV channel: {} (ID: {})",
                    savedChannel.getName(), savedChannel.getId());

            return convertToDTO(savedChannel);

        } catch (TvBootException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating TV channel with ID: {}", id, e);
            throw new TvBootException("Failed to update TV channel", "UPDATE_CHANNEL_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public void deleteChannel(Long id) {
        log.info("Deleting TV channel with ID: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid channel ID provided for deletion: {}", id);
            throw new ValidationException("Channel ID must be a positive number");
        }

        try {
            TvChannel channel = tvChannelRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("TV channel not found for deletion with ID: {}", id);
                        return new ResourceNotFoundException("TV Channel", id);
                    });

            String channelName = channel.getName();
            int channelNumber = channel.getChannelNumber();

            // Ajouter contexte au MDC pour traçabilité
            MDC.put("channelName", channelName);
            MDC.put("channelNumber", String.valueOf(channelNumber));

            log.info("Found TV channel for deletion: '{}' (Number: {}, ID: {})",
                    channelName, channelNumber, id);

            try {
                tvChannelRepository.delete(channel);
                log.info("Successfully deleted TV channel: '{}' (ID: {})", channelName, id);

            } catch (DataIntegrityViolationException e) {
                log.error("Cannot delete TV channel '{}' due to data integrity constraints", channelName, e);

                String errorMessage = analyzeConstraintViolation(e, channelName);
                throw new BusinessException(errorMessage, "DELETE_CONSTRAINT_VIOLATION");
            }

        } catch (TvBootException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting TV channel with ID: {}", id, e);
            throw new TvBootException("Failed to delete TV channel", "DELETE_CHANNEL_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        } finally {
            // Nettoyer le MDC
            MDC.remove("channelName");
            MDC.remove("channelNumber");
        }
    }

    // Méthodes de validation privées
    private void validateChannelNumber(int channelNumber, Long excludeId) {
        Optional<TvChannel> existingChannel = tvChannelRepository.findByChannelNumber(channelNumber);

        if (existingChannel.isPresent() &&
                (excludeId == null || !existingChannel.get().getId().equals(excludeId))) {
            log.warn("Channel number {} already exists for channel: {}",
                    channelNumber, existingChannel.get().getName());
            throw new BusinessException(
                    "Channel number already exists: " + channelNumber,
                    "DUPLICATE_CHANNEL_NUMBER");
        }
    }

    private void validateIpPortCombination(String ip, int port, Long excludeId) {
        Optional<TvChannel> existingChannel = tvChannelRepository.findByIpAndPort(ip, port);

        if (existingChannel.isPresent() &&
                (excludeId == null || !existingChannel.get().getId().equals(excludeId))) {
            log.warn("IP:Port combination {}:{} already exists for channel: {}",
                    ip, port, existingChannel.get().getName());
            throw new BusinessException(
                    "IP and port combination already exists: " + ip + ":" + port,
                    "DUPLICATE_IP_PORT");
        }
    }

    private String analyzeConstraintViolation(DataIntegrityViolationException e, String channelName) {
        String errorMessage = e.getMessage() != null ? e.getMessage() : "";

        if (errorMessage.contains("foreign key constraint") || errorMessage.contains("FOREIGN KEY")) {
            if (errorMessage.contains("package_channels")) {
                return String.format("Channel '%s' cannot be deleted - it is still used in channel packages", channelName);
            } else if (errorMessage.contains("terminal_channel_assignments")) {
                return String.format("Channel '%s' cannot be deleted - it is still assigned to terminals", channelName);
            } else if (errorMessage.contains("epg_entries")) {
                return String.format("Channel '%s' cannot be deleted - it has EPG entries", channelName);
            } else {
                return String.format("Channel '%s' cannot be deleted - it is referenced by other records", channelName);
            }
        }

        return String.format("Channel '%s' cannot be deleted due to database constraints", channelName);
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