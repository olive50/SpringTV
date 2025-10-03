package com.tvboot.tivio.tv.tvcategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TvChannelCategoryRepository extends JpaRepository<TvChannelCategory, Long> {
    Optional<TvChannelCategory> findByName(String name);
}