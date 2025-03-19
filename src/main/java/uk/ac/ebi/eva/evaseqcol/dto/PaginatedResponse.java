package uk.ac.ebi.eva.evaseqcol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> results;
    private PaginationInfo pagination;

    @Data
    @AllArgsConstructor
    public static class PaginationInfo {
        private int page;
        private int pageSize;
        private long total;
    }

    public static <T> PaginatedResponse<T> fromPage(Page<T> pageData) {
        return new PaginatedResponse<>(
                pageData.getContent(),
                new PaginationInfo(pageData.getNumber(), pageData.getSize(), pageData.getTotalElements())
        );
    }

    public static <T> PaginatedResponse<T> fromPage(List<T> content, Page<T> pageData) {
        return new PaginatedResponse<>(
                content, new PaginationInfo(pageData.getNumber(), pageData.getSize(), pageData.getTotalElements())
        );
    }
}
