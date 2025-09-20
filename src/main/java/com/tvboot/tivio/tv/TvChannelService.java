package com.tvboot.tivio.tv;

import com.tvboot.tivio.tv.dto.TvChannelCreateDTO;
import com.tvboot.tivio.tv.dto.TvChannelDTO;
import com.tvboot.tivio.tv.dto.TvChannelStatsDTO;
import com.tvboot.tivio.tv.dto.TvChannelUpdateDTO;
import com.tvboot.tivio.common.util.FileStorageService;
import com.tvboot.tivio.common.exception.BusinessException;
import com.tvboot.tivio.common.exception.ResourceNotFoundException;
import com.tvboot.tivio.common.exception.ValidationException;
import com.tvboot.tivio.common.exception.DataIntegrityException;
import com.tvboot.tivio.language.Language;
import com.tvboot.tivio.language.LanguageRepository;
import com.tvboot.tivio.tv.dto.TvChannelMapper;
import com.tvboot.tivio.tv.tvcategory.TvChannelCategory;
import com.tvboot.tivio.tv.tvcategory.TvChannelCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private TvChannelMapper mapper;

    private static final String LOGO_DIR = "channel-logos";

    private String baseUrl = "http://localhost:8080";
    private String logosPath = "/uploads/logos/channels";

    public List<TvChannelDTO> getAllChannels() {
        log.debug("Fetching all TV channels");

        List<TvChannel> channels = tvChannelRepository.findAll();
        log.info("Successfully retrieved {} TV channels", channels.size());

        return channels.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<TvChannelDTO> getAllChannels(Pageable pageable) {
        log.debug("Fetching TV channels with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<TvChannel> channels = tvChannelRepository.findAll(pageable);
        log.info("Successfully retrieved {} TV channels (page {}/{})",
                channels.getNumberOfElements(),
                channels.getNumber() + 1,
                channels.getTotalPages());

        return channels.map(this::convertToDTO);
    }

    public Optional<TvChannelDTO> getChannelById(Long id) {
        log.debug("Fetching TV channel by ID: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid channel ID provided: {}", id);
            throw new ValidationException("Channel ID must be a positive number");
        }

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
    }

    public Optional<TvChannelDTO> getChannelByNumber(int channelNumber) {
        log.debug("Fetching TV channel by number: {}", channelNumber);

        Optional<TvChannel> channel = tvChannelRepository.findByChannelNumber(channelNumber);

        if (channel.isPresent()) {
            log.info("Successfully found TV channel: {} (Number: {})",
                    channel.get().getName(), channelNumber);
            return channel.map(this::convertToDTO);
        } else {
            log.warn("TV channel not found with number: {}", channelNumber);
            return Optional.empty();
        }
    }

    public List<TvChannelDTO> getChannelsByCategory(Long categoryId) {
        log.debug("Fetching TV channels by category ID: {}", categoryId);

        if (categoryId == null || categoryId <= 0) {
            throw new ValidationException("Category ID must be a positive number");
        }

        // Verify category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }

        List<TvChannel> channels = tvChannelRepository.findByCategoryId(categoryId);
        log.info("Successfully retrieved {} TV channels for category ID: {}",
                channels.size(), categoryId);

        return channels.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TvChannelDTO> getChannelsByLanguage(Long languageId) {
        log.debug("Fetching TV channels by language ID: {}", languageId);

        if (languageId == null || languageId <= 0) {
            throw new ValidationException("Language ID must be a positive number");
        }

        // Verify language exists
        if (!languageRepository.existsById(languageId)) {
            throw new ResourceNotFoundException("Language", "id", languageId);
        }

        List<TvChannel> channels = tvChannelRepository.findByLanguageId(languageId);
        log.info("Successfully retrieved {} TV channels for language ID: {}",
                channels.size(), languageId);

        return channels.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<TvChannelDTO> searchChannelsByName(String name, Pageable pageable) {
        log.debug("Searching TV channels by name: '{}' with pagination", name);

        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Search name cannot be empty");
        }

        Page<TvChannel> channels = tvChannelRepository.findByNameContainingIgnoreCase(name, pageable);
        log.info("Successfully found {} TV channels matching name: '{}'",
                channels.getNumberOfElements(), name);

        return channels.map(this::convertToDTO);
    }

    public TvChannelDTO createChannel(TvChannelCreateDTO createDTO) {
        log.info("Creating new TV channel: {}", createDTO.getName());

        // Validation
        validateChannelNumber(createDTO.getChannelNumber(), null);
        validateIpPortCombination(createDTO.getIp(), createDTO.getPort(), null);

        // Récupérer les entités liées
        TvChannelCategory category = categoryRepository.findById(createDTO.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Category not found with ID: {}", createDTO.getCategoryId());
                    return new ResourceNotFoundException("Category", "id", createDTO.getCategoryId());
                });

        Language language = languageRepository.findById(createDTO.getLanguageId())
                .orElseThrow(() -> {
                    log.warn("Language not found with ID: {}", createDTO.getLanguageId());
                    return new ResourceNotFoundException("Language", "id", createDTO.getLanguageId());
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
    }

    // CREATE with logo
    public TvChannelDTO createChannelWithLogo(TvChannelCreateDTO createDTO, MultipartFile logoFile) {
        // Save channel first
        TvChannel channel = mapper.toEntity(createDTO);

        // Handle logo upload
        if (logoFile != null && !logoFile.isEmpty()) {
            String logoPath = fileStorageService.storeFile(logoFile, LOGO_DIR);
            channel.setLogoPath(logoPath);
            channel.setLogoUrl(getFullLogoUrl(logoPath));
        }

        channel = tvChannelRepository.save(channel);
        return mapper.toDTO(channel);
    }

    public TvChannelDTO createChannelWithLogoV2(TvChannelCreateDTO createDTO, MultipartFile logoFile) throws IOException {
        log.info("Creating new TV channel with logo: {}", createDTO.getName());

        // Validation du fichier logo
        validateLogoFile(logoFile);

        // Validation standard de la chaîne
        validateChannelNumber(createDTO.getChannelNumber(), null);
        validateIpPortCombination(createDTO.getIp(), createDTO.getPort(), null);

        // Récupérer les entités liées
        TvChannelCategory category = categoryRepository.findById(createDTO.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Category not found with ID: {}", createDTO.getCategoryId());
                    return new ResourceNotFoundException("Category", "id", createDTO.getCategoryId());
                });

        Language language = languageRepository.findById(createDTO.getLanguageId())
                .orElseThrow(() -> {
                    log.warn("Language not found with ID: {}", createDTO.getLanguageId());
                    return new ResourceNotFoundException("Language", "id", createDTO.getLanguageId());
                });

        // Sauvegarder le logo et obtenir l'URL
        String localPath = saveLogoFile(logoFile, createDTO.getName());
        String logoUrl = getFullLogoUrl(localPath);

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
    }

    public TvChannelDTO updateChannel(Long id, TvChannelUpdateDTO updateDTO) {
        log.info("Updating TV channel with ID: {}", id);

        TvChannel channel = tvChannelRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("TV channel not found for update with ID: {}", id);
                    return new ResourceNotFoundException("TV Channel", "id", id);
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

        return convertToDTO(savedChannel);
    }

    public void deleteChannel(Long id) {
        log.info("Deleting TV channel with ID: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid channel ID provided for deletion: {}", id);
            throw new ValidationException("Channel ID must be a positive number");
        }

        TvChannel channel = tvChannelRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("TV channel not found for deletion with ID: {}", id);
                    return new ResourceNotFoundException("TV Channel", "id", id);
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

            // Use DataIntegrityException with additional data
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("channelId", id);
            errorData.put("channelName", channelName);
            errorData.put("constraintType", extractConstraintType(e));

            throw new DataIntegrityException(errorMessage, errorData);
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

    private String extractConstraintType(DataIntegrityViolationException e) {
        String message = e.getMessage() != null ? e.getMessage() : "";

        if (message.contains("package_channels")) return "PACKAGE_REFERENCE";
        if (message.contains("terminal_channel_assignments")) return "TERMINAL_ASSIGNMENT";
        if (message.contains("epg_entries")) return "EPG_ENTRIES";
        if (message.contains("foreign key")) return "FOREIGN_KEY";

        return "UNKNOWN";
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

        TvChannelDTO.TvLanguageDTO languageDTO = null;
        if (channel.getLanguage() != null) {
            languageDTO = TvChannelDTO.TvLanguageDTO.builder()
                    .id(channel.getLanguage().getId())
                    .name(channel.getLanguage().getName())
                    .code(channel.getLanguage().getLocaleCode())
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

            Map<String, Object> data = new HashMap<>();
            data.put("fileSize", logoFile.getSize());
            data.put("maxSize", maxSize);
            data.put("fileName", logoFile.getOriginalFilename());

            throw new ValidationException(
                    "Logo file size exceeds maximum allowed size of 5MB",
                    data
            );
        }

        // Vérifier le type de fichier
        String contentType = logoFile.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            log.warn("Invalid logo file type: {}", contentType);
            throw new ValidationException(
                    "contentType",
                    "Invalid file type. Only image files (PNG, JPG, JPEG, GIF, WEBP) are allowed"
            );
        }

        // Vérifier l'extension du fichier
        String originalFilename = logoFile.getOriginalFilename();
        if (originalFilename == null || !hasValidImageExtension(originalFilename)) {
            log.warn("Invalid logo file extension: {}", originalFilename);
            throw new ValidationException(
                    "fileExtension",
                    "Invalid file extension. Only .png, .jpg, .jpeg, .gif, .webp files are allowed"
            );
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

            Map<String, Object> errorData = new HashMap<>();
            errorData.put("channelName", channelName);
            errorData.put("fileName", logoFile.getOriginalFilename());

            throw new BusinessException(
                    "Failed to save logo file: " + e.getMessage(),
                    errorData
            );
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

    /**
     * Alternative: Get detailed statistics with additional metrics
     */
    public TvChannelStatsDTO getDetailedStatistics() {
        TvChannelStatsDTO stats = getChannelStatistics();

        // Add zero counts for categories without channels
        List<TvChannelCategory> allCategories = categoryRepository.findAll();
        for (TvChannelCategory category : allCategories) {
            stats.getByCategory().putIfAbsent(category.getName(), 0L);
        }

        // Add zero counts for languages without channels
        List<Language> allLanguages = languageRepository.findAll();
        for (Language language : allLanguages) {
            stats.getByLanguage().putIfAbsent(language.getName(), 0L);
        }

        return stats;
    }
}