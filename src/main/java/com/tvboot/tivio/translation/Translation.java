package com.tvboot.tivio.translation;


import com.tvboot.tivio.language.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "translations",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_translation_language_key",
                columnNames = {"language_id", "message_key"}
        ),
        indexes = {
                @Index(name = "idx_translation_language_id", columnList = "language_id"),
                @Index(name = "idx_translation_message_key", columnList = "message_key")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "language")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Translation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Many-to-One relationship: Many translations belong to one language
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "language_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_translation_language")
    )
    private Language language;

    @NotBlank(message = "Message key is required")
    @Size(max = 255, message = "Message key must not exceed 255 characters")
    @Column(name = "message_key", nullable = false, length = 255)
    private String messageKey;

    @NotBlank(message = "Message value is required")
    @Column(name = "message_value", nullable = false, columnDefinition = "TEXT")
    private String messageValue;

    // Audit fields (matching your Language entity)
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;



    // Helper method to get language code (useful for API responses)
    public String getLanguageCode() {
        return language != null ? language.getIso6391() : null;
    }
}