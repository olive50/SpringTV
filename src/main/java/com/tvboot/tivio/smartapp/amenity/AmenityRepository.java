package com.tvboot.tivio.smartapp.amenity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    List<Amenity> findByShowInMenuTrueOrderByDisplayOrderAsc();

    List<Amenity> findByCategoryOrderByDisplayOrderAsc(Amenity.AmenityCategory category);

    List<Amenity> findByAvailableTrueAndShowInMenuTrueOrderByDisplayOrderAsc();
}