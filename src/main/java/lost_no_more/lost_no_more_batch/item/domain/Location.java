package lost_no_more.lost_no_more_batch.item.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lost_no_more.lost_no_more_batch.global.domain.BaseEntity;

@Getter
@Table(name = "location",
    indexes = {
        @Index(name = "idx_location_name", columnList = "location_name", unique = true)
    })
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long id;

    @Column(name = "location_name", nullable = false)
    private String name;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longtitude", nullable = false)
    private Double longitude;

    @Column(name = "region", nullable = false)
    private String region;

    @Builder
    public Location(String name, Double latitude, Double longitude, String region) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.region = region;
    }
}
