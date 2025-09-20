package com.tvboot.tivio.tv;

import com.tvboot.tivio.common.util.FileStorageService;
import com.tvboot.tivio.tv.dto.TvChannelCreateDTO;
import com.tvboot.tivio.tv.dto.TvChannelDTO;
import com.tvboot.tivio.tv.dto.TvChannelUpdateDTO;
import com.tvboot.tivio.common.exception.ResourceNotFoundException;
import com.tvboot.tivio.tv.dto.TvChannelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class TvChannelService2 {

    @Autowired
    private TvChannelRepository repository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private TvChannelMapper mapper;

    private static final String LOGO_DIR = "channel-logos";

    private String baseUrl = "http://localhost:8080/";

    // CREATE without logo
    public TvChannelDTO createChannel(TvChannelCreateDTO createDTO) {
        TvChannel channel = mapper.toEntity(createDTO);
        channel = repository.save(channel);
        return mapper.toDTO(channel);
    }

    // CREATE with logo
    public TvChannelDTO createChannelWithLogo(TvChannelCreateDTO createDTO, MultipartFile logoFile) {
        // Save channel first
        TvChannel channel = mapper.toEntity(createDTO);

        // Handle logo upload
        if (logoFile != null && !logoFile.isEmpty()) {
            String logoPath = fileStorageService.storeFile(logoFile, LOGO_DIR);
            channel.setLogoPath(logoPath);
            channel.setLogoUrl(baseUrl+logoPath);
        }

        channel = repository.save(channel);
        return mapper.toDTO(channel);
    }

    // READ all
    public Page<TvChannelDTO> getAllChannels(int page, int size, String category, Boolean active) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("channelNumber"));
        Page<TvChannel> channels;

        if (category != null && active != null) {
            channels = repository.findByCategoryAndActive(category, active, pageable);
        } else if (category != null) {
            channels = repository.findByCategory(category, pageable);
        } else if (active != null) {
            channels = repository.findByActive(active, pageable);
        } else {
            channels = repository.findAll(pageable);
        }

        return channels.map(channel -> mapper.toDTO(channel));
    }

    // READ single
    public TvChannelDTO getChannelById(Long id) {
        TvChannel channel = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + id));
        return mapper.toDTO(channel);
    }

    // UPDATE without logo
    public TvChannelDTO updateChannel(Long id, TvChannelUpdateDTO updateDTO) {
        TvChannel channel = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + id));

        // ✅ CORRECT: Update the existing entity, don't create a new one
        mapper.updateEntityFromDTO(updateDTO, channel);

        channel = repository.save(channel);
        return mapper.toDTO(channel);
    }

    // UPDATE with logo
    public TvChannelDTO updateChannelWithLogo(Long id, TvChannelUpdateDTO updateDTO, MultipartFile logoFile) {
        TvChannel channel = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + id));

        // ✅ Update existing entity
        mapper.updateEntityFromDTO(updateDTO, channel);

        // Handle logo update if provided
        if (logoFile != null && !logoFile.isEmpty()) {
            // Delete old logo if exists
            if (channel.getLogoPath() != null) {
                fileStorageService.deleteFile(channel.getLogoPath());
            }
            // Store new logo
            String logoPath = fileStorageService.storeFile(logoFile, LOGO_DIR);
            channel.setLogoPath(logoPath);
        }

        channel = repository.save(channel);
        return mapper.toDTO(channel);
    }
    // DELETE
    public void deleteChannel(Long id) {
        TvChannel channel = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + id));

        // Delete logo file if exists
        if (channel.getLogoPath() != null) {
            fileStorageService.deleteFile(channel.getLogoPath());
        }

        repository.delete(channel);
    }

    // Get logo resource
    public Resource getChannelLogo(Long id) {
        TvChannel channel = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + id));

        if (channel.getLogoPath() == null) {
            throw new ResourceNotFoundException("Channel has no logo");
        }

        return fileStorageService.loadFileAsResource(channel.getLogoPath());
    }
}