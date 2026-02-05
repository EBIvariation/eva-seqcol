package uk.ac.ebi.eva.evaseqcol.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaginatedResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testConstructorAndGetters() {
        List<String> results = Arrays.asList("digest1", "digest2", "digest3");
        PaginatedResponse.PaginationInfo pagination = new PaginatedResponse.PaginationInfo(0, 10, 100);

        PaginatedResponse<String> response = new PaginatedResponse<>(results, pagination);

        assertEquals(results, response.getResults());
        assertEquals(pagination, response.getPagination());
        assertEquals(3, response.getResults().size());
    }

    @Test
    void testPaginationInfo() {
        PaginatedResponse.PaginationInfo pagination = new PaginatedResponse.PaginationInfo(2, 25, 150);

        assertEquals(2, pagination.getPage());
        assertEquals(25, pagination.getPageSize());
        assertEquals(150, pagination.getTotal());
    }

    @Test
    void testFromPageWithPageData() {
        List<String> content = Arrays.asList("item1", "item2", "item3");
        Page<String> page = new PageImpl<>(content, PageRequest.of(1, 10), 50);

        PaginatedResponse<String> response = PaginatedResponse.fromPage(page);

        assertEquals(content, response.getResults());
        assertEquals(1, response.getPagination().getPage());
        assertEquals(10, response.getPagination().getPageSize());
        assertEquals(50, response.getPagination().getTotal());
    }

    @Test
    void testFromPageWithCustomContent() {
        List<String> originalContent = Arrays.asList("a", "b", "c");
        List<String> customContent = Arrays.asList("x", "y");
        Page<String> page = new PageImpl<>(originalContent, PageRequest.of(0, 5), 20);

        PaginatedResponse<String> response = PaginatedResponse.fromPage(customContent, page);

        assertEquals(customContent, response.getResults());
        assertEquals(2, response.getResults().size());
        assertEquals(0, response.getPagination().getPage());
        assertEquals(5, response.getPagination().getPageSize());
        assertEquals(20, response.getPagination().getTotal());
    }

    @Test
    void testFromPageWithEmptyPage() {
        List<String> emptyContent = Collections.emptyList();
        Page<String> page = new PageImpl<>(emptyContent, PageRequest.of(0, 10), 0);

        PaginatedResponse<String> response = PaginatedResponse.fromPage(page);

        assertTrue(response.getResults().isEmpty());
        assertEquals(0, response.getPagination().getTotal());
    }

    @Test
    void testFromPageFirstPage() {
        List<String> content = Arrays.asList("first", "second");
        Page<String> page = new PageImpl<>(content, PageRequest.of(0, 2), 10);

        PaginatedResponse<String> response = PaginatedResponse.fromPage(page);

        assertEquals(0, response.getPagination().getPage());
        assertEquals(2, response.getPagination().getPageSize());
        assertEquals(10, response.getPagination().getTotal());
    }

    @Test
    void testJsonSerializationPageSizeSnakeCase() throws JsonProcessingException {
        List<String> results = Arrays.asList("digest1");
        PaginatedResponse.PaginationInfo pagination = new PaginatedResponse.PaginationInfo(0, 10, 100);
        PaginatedResponse<String> response = new PaginatedResponse<>(results, pagination);

        String json = objectMapper.writeValueAsString(response);

        // Verify page_size is serialized with snake_case (not pageSize)
        assertTrue(json.contains("\"page_size\""));
        assertFalse(json.contains("\"pageSize\""));
    }

    @Test
    void testJsonSerializationStructure() throws JsonProcessingException {
        List<String> results = Arrays.asList("abc123", "def456");
        PaginatedResponse.PaginationInfo pagination = new PaginatedResponse.PaginationInfo(1, 20, 50);
        PaginatedResponse<String> response = new PaginatedResponse<>(results, pagination);

        String json = objectMapper.writeValueAsString(response);

        // Verify overall structure
        assertTrue(json.contains("\"results\""));
        assertTrue(json.contains("\"pagination\""));
        assertTrue(json.contains("\"page\""));
        assertTrue(json.contains("\"page_size\""));
        assertTrue(json.contains("\"total\""));
        assertTrue(json.contains("\"abc123\""));
        assertTrue(json.contains("\"def456\""));
    }

    @Test
    void testWithDifferentGenericTypes() {
        // Test with Integer type
        List<Integer> intResults = Arrays.asList(1, 2, 3);
        PaginatedResponse<Integer> intResponse = new PaginatedResponse<>(
                intResults, new PaginatedResponse.PaginationInfo(0, 10, 3));

        assertEquals(3, intResponse.getResults().size());
        assertEquals(Integer.valueOf(1), intResponse.getResults().get(0));

        // Test with custom object type
        List<Object> objectResults = Arrays.asList("string", 123, true);
        PaginatedResponse<Object> objectResponse = new PaginatedResponse<>(
                objectResults, new PaginatedResponse.PaginationInfo(0, 10, 3));

        assertEquals(3, objectResponse.getResults().size());
    }

    @Test
    void testEqualsAndHashCode() {
        List<String> results = Arrays.asList("a", "b");
        PaginatedResponse.PaginationInfo pagination1 = new PaginatedResponse.PaginationInfo(0, 10, 100);
        PaginatedResponse.PaginationInfo pagination2 = new PaginatedResponse.PaginationInfo(0, 10, 100);

        PaginatedResponse<String> response1 = new PaginatedResponse<>(results, pagination1);
        PaginatedResponse<String> response2 = new PaginatedResponse<>(results, pagination2);

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testPaginationInfoEqualsAndHashCode() {
        PaginatedResponse.PaginationInfo info1 = new PaginatedResponse.PaginationInfo(1, 20, 50);
        PaginatedResponse.PaginationInfo info2 = new PaginatedResponse.PaginationInfo(1, 20, 50);
        PaginatedResponse.PaginationInfo info3 = new PaginatedResponse.PaginationInfo(2, 20, 50);

        assertEquals(info1, info2);
        assertEquals(info1.hashCode(), info2.hashCode());
        assertNotEquals(info1, info3);
    }
}
