package lost_no_more.lost_no_more_batch.open_api.service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import lombok.RequiredArgsConstructor;
import lost_no_more.lost_no_more_batch.open_api.dto.LostItemDto;

@Service
@RequiredArgsConstructor
public class LostItemParseService {

	public List<LostItemDto> parseXmlToDto(String xmlData) throws Exception {
		Document document = createDocument(xmlData);
		NodeList itemList = document.getElementsByTagName("item");

		return IntStream.range(0, itemList.getLength())
			.mapToObj(i -> (Element) itemList.item(i))
			.map(this::createLostItemDto)
			.collect(Collectors.toList());
	}

	private Document createDocument(String xmlData) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(new ByteArrayInputStream(xmlData.getBytes()));
	}

	private LostItemDto createLostItemDto(Element item) {
		return LostItemDto.builder()
			.atcId(getTextContent(item, "atcId"))
			.categoryName(getTextContent(item, "prdtClNm").split(" > ")[0])
			.color(getTextContent(item, "clrNm"))
			.image(getTextContent(item, "fdFilePathImg"))
			.name(getTextContent(item, "fdPrdtNm"))
			.location(getTextContent(item, "depPlace"))
			.date(LocalDate.parse(getTextContent(item, "fdYmd")))
			.build();
	}

	private String getTextContent(Element element, String tagName) {
		return element.getElementsByTagName(tagName).item(0).getTextContent();
	}
}