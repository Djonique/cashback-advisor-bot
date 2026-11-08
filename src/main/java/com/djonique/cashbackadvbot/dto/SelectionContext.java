package com.djonique.cashbackadvbot.dto;

import java.math.BigDecimal;
import java.util.*;

public record SelectionContext(
        Long bankId,
        Set<Long> selectedCategoryIds,
        Map<Long, BigDecimal> percents,
        List<Long> pendingCategories) {

    public SelectionContext {
        selectedCategoryIds = selectedCategoryIds == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(selectedCategoryIds));
        percents = percents == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(percents));
        pendingCategories = pendingCategories == null
                ? List.of()
                : List.copyOf(pendingCategories);
    }
}
