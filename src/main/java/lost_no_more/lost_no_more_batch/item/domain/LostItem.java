package lost_no_more.lost_no_more_batch.item.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lost_no_more.lost_no_more_batch.global.domain.BaseEntity;

@Getter
@Table(name = "lost_item")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LostItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lost_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "color")
    private String color;

    @Column(name = "image")
    private String image;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Builder
    public LostItem(Category category, Location location, String color, String image, String name, LocalDate date) {
        this.location = location;
        this.category = category;
        this.color = color;
        this.image = image;
        this.name = name;
        this.date = date;
    }
}
