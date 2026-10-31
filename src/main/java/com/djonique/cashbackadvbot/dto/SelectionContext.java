package com.djonique.cashbackadvbot.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record SelectionContext(
        Integer bankId,
        Set<Long> selectedCategoryIds,
        Map<Long, BigDecimal> percents,
        List<Long> pendingCategories) {
}
