package com.tvboot.tivio.smartapp.amenity;

import com.tvboot.tivio.smartapp.amenity.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amenities")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AmenityController {

    private final AmenityService amenityService;

    @GetMapping
    public ResponseEntity<List<AmenityListDTO>> getAllAmenities() {
        return ResponseEntity.ok(amenityService.getAvailableAmenities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AmenityDetailDTO> getAmenityById(@PathVariable Long id) {
        return ResponseEntity.ok(amenityService.getAmenityById(id));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<AmenityListDTO>> getAmenitiesByCategory(
            @PathVariable Amenity.AmenityCategory category) {
        return ResponseEntity.ok(amenityService.getAmenitiesByCategory(category));
    }

    @PostMapping
    public ResponseEntity<AmenityDetailDTO> createAmenity(@Valid @RequestBody AmenityCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(amenityService.createAmenity(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AmenityDetailDTO> updateAmenity(
            @PathVariable Long id,
            @Valid @RequestBody AmenityUpdateDTO dto) {
        return ResponseEntity.ok(amenityService.updateAmenity(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAmenity(@PathVariable Long id) {
        amenityService.deleteAmenity(id);
        return ResponseEntity.noContent().build();
    }
}