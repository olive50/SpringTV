package com.tvboot.tivio.tv.tvpackage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Channel Packages", description = "Channel package management endpoints")
@RestController
@RequestMapping("/api/packages")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ChannelPackageController {

    private final ChannelPackageService packageService;

    @Operation(summary = "Get all channel packages")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<List<ChannelPackage>> getAllPackages() {
        List<ChannelPackage> packages = packageService.getAllPackages();
        return ResponseEntity.ok(packages);
    }

    @Operation(summary = "Get active channel packages")
    @GetMapping("/active")
    public ResponseEntity<List<ChannelPackage>> getActivePackages() {
        List<ChannelPackage> packages = packageService.getActivePackages();
        return ResponseEntity.ok(packages);
    }

    @Operation(summary = "Get premium channel packages")
    @GetMapping("/premium")
    public ResponseEntity<List<ChannelPackage>> getPremiumPackages() {
        List<ChannelPackage> packages = packageService.getPremiumPackages();
        return ResponseEntity.ok(packages);
    }

    @Operation(summary = "Get channel package by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<ChannelPackage> getPackageById(@PathVariable Long id) {
        return packageService.getPackageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get channel package by name")
    @GetMapping("/name/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<ChannelPackage> getPackageByName(@PathVariable String name) {
        return packageService.getPackageByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create new channel package")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ChannelPackage> createPackage(@Valid @RequestBody ChannelPackage channelPackage) {
        try {
            ChannelPackage createdPackage = packageService.createPackage(channelPackage);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPackage);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update channel package")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ChannelPackage> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody ChannelPackage packageDetails) {
        try {
            ChannelPackage updatedPackage = packageService.updatePackage(id, packageDetails);
            return ResponseEntity.ok(updatedPackage);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete channel package")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        try {
            packageService.deletePackage(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Add channel to package")
    @PostMapping("/{packageId}/channels/{channelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ChannelPackage> addChannelToPackage(
            @PathVariable Long packageId,
            @PathVariable Long channelId) {
        try {
            ChannelPackage updatedPackage = packageService.addChannelToPackage(packageId, channelId);
            return ResponseEntity.ok(updatedPackage);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Remove channel from package")
    @DeleteMapping("/{packageId}/channels/{channelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ChannelPackage> removeChannelFromPackage(
            @PathVariable Long packageId,
            @PathVariable Long channelId) {
        try {
            ChannelPackage updatedPackage = packageService.removeChannelFromPackage(packageId, channelId);
            return ResponseEntity.ok(updatedPackage);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get packages containing specific channel")
    @GetMapping("/by-channel/{channelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ChannelPackage>> getPackagesWithChannel(@PathVariable Long channelId) {
        List<ChannelPackage> packages = packageService.getPackagesWithChannel(channelId);
        return ResponseEntity.ok(packages);
    }
}