package com.tvboot.tivio.media;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tvboot.tivio.language.Language;
import com.tvboot.tivio.media.epg.EpgEntry;
import com.tvboot.tivio.tv.tvchannel.tvchannelcategory.TvChannelCategory;
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
@Table(name = "tv_channels", schema = "public")
public class TvChannel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "channel_number", unique = true)
    private int channelNumber;

    @Column(name = "name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "description", columnDefinition = "VARCHAR(255")
    private String description;

    @Column(name = "ip", columnDefinition = "VARCHAR(45)")
    private String ip;

    @Column(name = "port")
    private int port;

    @Column(name = "stream_url", nullable = false, columnDefinition = "VARCHAR(255)")
    private String streamUrl;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private TvChannelCategory category;

    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language language;

    @Column(name = "logo_path", columnDefinition = "VARCHAR(500)")
    private String logoPath;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "is_available", nullable = true)
    private Boolean available = true;

   
    @Column(name = "sort_order", nullable = true)
    private int sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<EpgEntry> epg = new ArrayList<>();
}