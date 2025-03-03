package lost_no_more.lost_no_more_batch.item.service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lost_no_more.lost_no_more_batch.item.domain.Category;
import lost_no_more.lost_no_more_batch.item.domain.Location;
import lost_no_more.lost_no_more_batch.item.domain.LostItem;
import lost_no_more.lost_no_more_batch.item.repository.CategoryRepository;
import lost_no_more.lost_no_more_batch.item.repository.LostItemRepository;
import lost_no_more.lost_no_more_batch.open_api.dto.LostItemDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Slf4j
@Service
@RequiredArgsConstructor
public class LostItemService {

    private final CategoryRepository categoryRepository;
    private final LostItemRepository lostItemRepository;
    private final LocationService locationService;

    public List<LostItemDto> parseXmlToDto(String xmlData) throws Exception {
        List<LostItemDto> lostItemDtos = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlData.getBytes()));

        NodeList itemList = document.getElementsByTagName("item");

        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            String prdtClNm = item.getElementsByTagName("prdtClNm").item(0).getTextContent().split(" > ")[0];
            String clrNm = item.getElementsByTagName("clrNm").item(0).getTextContent();
            String fdFilePathImg = item.getElementsByTagName("fdFilePathImg").item(0).getTextContent();
            String fdPrdtNm = item.getElementsByTagName("fdPrdtNm").item(0).getTextContent();
            String fdYmd = item.getElementsByTagName("fdYmd").item(0).getTextContent();
            String depPlace = item.getElementsByTagName("depPlace").item(0).getTextContent();

            LostItemDto dto = LostItemDto.builder()
                    .categoryName(prdtClNm)
                    .color(clrNm)
                    .image(fdFilePathImg)
                    .name(fdPrdtNm)
                    .location(depPlace)
                    .date(LocalDate.parse(fdYmd))
                    .build();

            lostItemDtos.add(dto);
        }

        return lostItemDtos;
    }

    @Transactional
    public void saveLostItems(List<LostItemDto> lostItemDtos) {
        for (LostItemDto dto : lostItemDtos) {

            Category category = categoryRepository.findByName(dto.getCategoryName())
                    .orElseGet(() -> categoryRepository.save(new Category(dto.getCategoryName())));

            Location location = locationService.findOrCreateLocation(dto.getLocation());

            if (location == null) {
                log.warn("유효하지 않은 위치 정보: {}", dto.getLocation());
            }

            LostItem lostItem = LostItem.builder()
                    .category(category)
                    .location(location)
                    .color(dto.getColor())
                    .image(dto.getImage())
                    .name(dto.getName())
                    .date(dto.getDate())
                    .build();

            lostItemRepository.save(lostItem);
        }
    }
}