package com.tvboot.tivio.tv;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tvboot.tivio.language.Language;
import com.tvboot.tivio.tv.epg.EpgEntry;
import com.tvboot.tivio.tv.tvcategory.TvChannelCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tv_channels")
public class TvChannel  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "channel_number", unique = true)
    private int channelNumber;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "ip")
    private String ip;

    @Column(name = "port")
    private int port;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "category_id")
    private TvChannelCategory category;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "language_id")
    private Language language;

    @Column(name = "logo_url")
    private String logoUrl;
    @Column(name = "logo_path")
    private String logoPath;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<EpgEntry> epg = new ArrayList<>();

    @Column(name = "is_active")
    private boolean active;
//    @Column(name = "is_available")
//    private boolean available;
}