package com.tvboot.tivio.tv.tvchannel.tvchannelcategory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TvChannelCategoryService {

    private final TvChannelCategoryRepository categoryRepository;

    public List<TvChannelCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<TvChannelCategory> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public TvChannelCategory createCategory(TvChannelCategory category) {
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new RuntimeException("Category name already exists: " + category.getName());
        }
        return categoryRepository.save(category);
    }

    public TvChannelCategory updateCategory(Long id, TvChannelCategory categoryDetails) {
        TvChannelCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));

        // Check if new name is unique (excluding current category)
        Optional<TvChannelCategory> existingCategory = categoryRepository.findByName(categoryDetails.getName());
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
            throw new RuntimeException("Category name already exists: " + categoryDetails.getName());
        }

        category.setName(categoryDetails.getName());

        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }
}