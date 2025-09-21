// src/main/java/com/example/tikicktaka/service/stadiumAttractionService/StadiumAttractionQueryService.java
package com.example.tikicktaka.service.stadiumAttractionService;

import com.example.tikicktaka.web.dto.stadiumAttraction.StadiumAttractionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface StadiumAttractionQueryService {

    static List<String> parseCategoryCsv(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptyList();
        return Arrays.stream(csv.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).limit(2).toList();
    }

    Page<StadiumAttractionResponseDTO.Item> findItems(
            Long memberId,
            Long teamId,
            List<Long> styleIdsOverride,
            boolean useDefaultCategory,
            boolean myScrapOnly,
            String sort,
            Pageable pageable
    );
}
