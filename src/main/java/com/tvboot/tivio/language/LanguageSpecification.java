package com.tvboot.tivio.language;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class LanguageSpecification {

    public static Specification<Language> withFilters(String query,
                                                      Boolean isAdminEnabled,
                                                      Boolean isGuestEnabled,
                                                      Boolean isRtl) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtre de recherche textuelle
            if (query != null && !query.trim().isEmpty()) {
                String searchPattern = "%" + query.toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        searchPattern
                );
                predicates.add(namePredicate);
            }

            // Filtre isAdminEnabled
            if (isAdminEnabled != null) {
                predicates.add(criteriaBuilder.equal(root.get("isAdminEnabled"), isAdminEnabled));
            }

            // Filtre isGuestEnabled
            if (isGuestEnabled != null) {
                predicates.add(criteriaBuilder.equal(root.get("isGuestEnabled"), isGuestEnabled));
            }

            // Filtre isRtl
            if (isRtl != null) {
                predicates.add(criteriaBuilder.equal(root.get("isRtl"), isRtl));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}