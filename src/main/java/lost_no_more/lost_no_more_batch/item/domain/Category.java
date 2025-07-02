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
@Table(name = "category",
    indexes = {
        @Index(name = "idx_category_name", columnList = "name", unique = true)
    })
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Builder
    public Category(String name) {
        this.name = name;
    }
}
