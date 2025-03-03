package lost_no_more.lost_no_more_batch.open_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LostItemDto {

    private String categoryName;
    private String color;
    private String image;
    private String name;
    private String location;
    private LocalDate date;
}