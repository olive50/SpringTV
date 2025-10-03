package com.tvboot.tivio.smartapp.amenity;

import com.tvboot.tivio.smartapp.amenity.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AmenityService {

    private final AmenityRepository amenityRepository;
    private final AmenityMapper amenityMapper;

    public List<AmenityListDTO> getAllAmenities() {
        List<Amenity> amenities = amenityRepository.findByShowInMenuTrueOrderByDisplayOrderAsc();
        return amenityMapper.toListDTOs(amenities);
    }

    public List<AmenityListDTO> getAvailableAmenities() {
        List<Amenity> amenities = amenityRepository.findByAvailableTrueAndShowInMenuTrueOrderByDisplayOrderAsc();
        return amenityMapper.toListDTOs(amenities);
    }

    public AmenityDetailDTO getAmenityById(Long id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Amenity not found with id: " + id));
        return amenityMapper.toDetailDTO(amenity);
    }

    public List<AmenityListDTO> getAmenitiesByCategory(Amenity.AmenityCategory category) {
        List<Amenity> amenities = amenityRepository.findByCategoryOrderByDisplayOrderAsc(category);
        return amenityMapper.toListDTOs(amenities);
    }

    @Transactional
    public AmenityDetailDTO createAmenity(AmenityCreateDTO dto) {
        Amenity amenity = amenityMapper.toEntity(dto);
        Amenity saved = amenityRepository.save(amenity);
        return amenityMapper.toDetailDTO(saved);
    }

    @Transactional
    public AmenityDetailDTO updateAmenity(Long id, AmenityUpdateDTO dto) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Amenity not found with id: " + id));

        amenityMapper.updateEntityFromDTO(dto, amenity);
        Amenity updated = amenityRepository.save(amenity);
        return amenityMapper.toDetailDTO(updated);
    }

    @Transactional
    public void deleteAmenity(Long id) {
        amenityRepository.deleteById(id);
    }
}