package com.tvboot.tivio.tv.tvpackage;

import com.tvboot.tivio.common.exception.ResourceNotFoundException;
import com.tvboot.tivio.tv.TvChannelRepository;
import com.tvboot.tivio.tv.TvChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChannelPackageService {

    private final ChannelPackageRepository packageRepository;
    private final TvChannelRepository tvChannelRepository;

    public List<ChannelPackage> getAllPackages() {
        return packageRepository.findAll();
    }

    public List<ChannelPackage> getActivePackages() {
        return packageRepository.findByIsActiveTrue();
    }

    public List<ChannelPackage> getPremiumPackages() {
        return packageRepository.findByIsPremiumTrue();
    }

    public Optional<ChannelPackage> getPackageById(Long id) {
        return packageRepository.findById(id);
    }

    public Optional<ChannelPackage> getPackageByName(String name) {
        return packageRepository.findByName(name);
    }

    public ChannelPackage createPackage(ChannelPackage channelPackage) {
        log.info("Creating new channel package: {}", channelPackage.getName());

        if (packageRepository.findByName(channelPackage.getName()).isPresent()) {
            throw new RuntimeException("Package name already exists: " + channelPackage.getName());
        }

        return packageRepository.save(channelPackage);
    }

    public ChannelPackage updatePackage(Long id, ChannelPackage packageDetails) {
        log.info("Updating channel package with ID: {}", id);

        ChannelPackage existingPackage = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with ID: " + id));

        // Check name uniqueness if changing
        if (!existingPackage.getName().equals(packageDetails.getName()) &&
                packageRepository.findByName(packageDetails.getName()).isPresent()) {
            throw new RuntimeException("Package name already exists: " + packageDetails.getName());
        }

        existingPackage.setName(packageDetails.getName());
        existingPackage.setDescription(packageDetails.getDescription());
        existingPackage.setPrice(packageDetails.getPrice());
        existingPackage.setIsPremium(packageDetails.getIsPremium());
        existingPackage.setIsActive(packageDetails.getIsActive());

        return packageRepository.save(existingPackage);
    }

    public void deletePackage(Long id) {
        log.info("Deleting channel package with ID: {}", id);

        if (!packageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Package not found with ID: " + id);
        }

        packageRepository.deleteById(id);
    }

    public ChannelPackage addChannelToPackage(Long packageId, Long channelId) {
        log.info("Adding channel {} to package {}", channelId, packageId);

        ChannelPackage channelPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with ID: " + packageId));

        TvChannel channel = tvChannelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with ID: " + channelId));

        if (!channelPackage.getChannels().contains(channel)) {
            channelPackage.getChannels().add(channel);
            channelPackage = packageRepository.save(channelPackage);
        }

        return channelPackage;
    }

    public ChannelPackage removeChannelFromPackage(Long packageId, Long channelId) {
        log.info("Removing channel {} from package {}", channelId, packageId);

        ChannelPackage channelPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with ID: " + packageId));

        TvChannel channel = tvChannelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with ID: " + channelId));

        channelPackage.getChannels().remove(channel);
        return packageRepository.save(channelPackage);
    }

    public List<ChannelPackage> getPackagesWithChannel(Long channelId) {
        return packageRepository.findPackagesWithChannel(channelId);
    }
}