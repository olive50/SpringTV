package com.tvboot.tivio.service;

import com.tvboot.tivio.config.CdnConfig;
import com.tvboot.tivio.dto.*;
import com.tvboot.tivio.entities.*;
import com.tvboot.tivio.exception.*;
import com.tvboot.tivio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TvChannelService {

    private final TvChannelRepository tvChannelRepository;
    private final TvChannelCategoryRepository categoryRepository;
    private final LanguageRepository languageRepository;

    private String baseUrl = "http://localhost:8080";
    private String logosPath = "/uploads/logos/channels";
    private boolean enabled = false;

//    @Autowired
//    private CdnConfig cdnConfig;

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

    // Ajoutez cette méthode à votre TvChannelService existant

    public TvChannelDTO createChannelWithLogo(TvChannelCreateDTO createDTO, MultipartFile logoFile) throws IOException {
        log.info("Creating new TV channel with logo: {}", createDTO.getName());

        try {
            // Validation du fichier logo
            validateLogoFile(logoFile);

            // Validation standard de la chaîne
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

            // Sauvegarder le logo et obtenir l'URL
            String localPath = saveLogoFile(logoFile, createDTO.getName());
//            channel.setLogoUrl(localPath);
            String logoUrl=getFullLogoUrl(localPath);

            log.debug("Logo saved successfully: {}", logoUrl);

            // Créer la chaîne avec l'URL du logo
            TvChannel channel = TvChannel.builder()
                    .channelNumber(createDTO.getChannelNumber())
                    .name(createDTO.getName())
                    .description(createDTO.getDescription())
                    .ip(createDTO.getIp())
                    .port(createDTO.getPort())
                    .logoUrl(logoUrl)  // URL du logo sauvegardé
                    .category(category)
                    .language(language)
                    .build();

            TvChannel savedChannel = tvChannelRepository.save(channel);

            log.info("Successfully created TV channel with logo: {} (ID: {}, Number: {}, Logo: {})",
                    savedChannel.getName(), savedChannel.getId(), savedChannel.getChannelNumber(), logoUrl);

            // Ajouter des métadonnées au contexte de logging
            MDC.put("channelId", savedChannel.getId().toString());
            MDC.put("channelNumber", String.valueOf(savedChannel.getChannelNumber()));
            MDC.put("logoUrl", logoUrl);

            return convertToDTO(savedChannel);

        } catch (TvBootException e) {
            throw e; // Re-lancer les exceptions métier
        } catch (IOException e) {
            log.error("I/O error while creating channel with logo: {}", createDTO.getName(), e);
            throw e; // Re-lancer les erreurs I/O pour gestion par le contrôleur
        } catch (Exception e) {
            log.error("Unexpected error creating TV channel with logo: {}", createDTO.getName(), e);
            throw new TvBootException("Failed to create TV channel with logo", "CREATE_CHANNEL_LOGO_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public String getFullLogoUrl(String logoPath) {
//        if (!enabled || logoPath == null) {
//            return logoPath;
//        }

        // If it's already a full URL, return as is
        if (logoPath.startsWith("http://") || logoPath.startsWith("https://")) {
            return logoPath;
        }

        // If logoPath starts with /, remove it to avoid double slashes
        if (logoPath.startsWith("/")) {
            logoPath = logoPath.substring(1);
        }

        return baseUrl + "/" + logoPath;
    }

    /**
     * Valide le fichier logo téléchargé
     */
    private void validateLogoFile(MultipartFile logoFile) {
        if (logoFile == null || logoFile.isEmpty()) {
            throw new ValidationException("Logo file is required and cannot be empty");
        }

        // Vérifier la taille du fichier (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB en bytes
        if (logoFile.getSize() > maxSize) {
            log.warn("Logo file size exceeds maximum: {} bytes (max: {} bytes)",
                    logoFile.getSize(), maxSize);
            throw new ValidationException("Logo file size exceeds maximum allowed size of 5MB");
        }

        // Vérifier le type de fichier
        String contentType = logoFile.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            log.warn("Invalid logo file type: {}", contentType);
            throw new IllegalArgumentException("Invalid file type. Only image files (PNG, JPG, JPEG, GIF) are allowed");
        }

        // Vérifier l'extension du fichier
        String originalFilename = logoFile.getOriginalFilename();
        if (originalFilename == null || !hasValidImageExtension(originalFilename)) {
            log.warn("Invalid logo file extension: {}", originalFilename);
            throw new IllegalArgumentException("Invalid file extension. Only .png, .jpg, .jpeg, .gif files are allowed");
        }

        log.debug("Logo file validation passed: {} ({})", originalFilename, contentType);
    }

    /**
     * Vérifie si le type MIME est valide pour une image
     */
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/png") ||
                contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp");
    }

    /**
     * Vérifie si l'extension du fichier est valide pour une image
     */
    private boolean hasValidImageExtension(String filename) {
        String lowerFilename = filename.toLowerCase();
        return lowerFilename.endsWith(".png") ||
                lowerFilename.endsWith(".jpg") ||
                lowerFilename.endsWith(".jpeg") ||
                lowerFilename.endsWith(".gif") ||
                lowerFilename.endsWith(".webp");
    }

    /**
     * Sauvegarde le fichier logo et retourne l'URL
     */
    private String saveLogoFile(MultipartFile logoFile, String channelName) throws IOException {
        try {
            // Créer le répertoire de destination si nécessaire
            String uploadDir = "uploads/logos/channels/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.debug("Created upload directory: {}", uploadPath);
            }

            // Générer un nom de fichier unique
            String originalFilename = logoFile.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // Format: channel_<channelName>_<timestamp>.<extension>
            String sanitizedChannelName = channelName.replaceAll("[^a-zA-Z0-9]", "_");
            String timestamp = String.valueOf(System.currentTimeMillis());
            String filename = String.format("channel_%s_%s%s",
                    sanitizedChannelName, timestamp, extension);

            // Chemin complet du fichier
            Path filePath = uploadPath.resolve(filename);

            // Sauvegarder le fichier
            try (InputStream inputStream = logoFile.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Retourner l'URL relative
            String logoUrl = "/" + uploadDir + filename;
            log.info("Logo file saved successfully: {} -> {}", originalFilename, logoUrl);

            return logoUrl;

        } catch (IOException e) {
            log.error("Failed to save logo file for channel: {}", channelName, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error saving logo file for channel: {}", channelName, e);
            throw new IOException("Failed to save logo file: " + e.getMessage(), e);
        }
    }
}