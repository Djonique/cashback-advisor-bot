package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.domain.BankCategoryMcc;
import com.djonique.cashbackadvbot.domain.Category;
import com.djonique.cashbackadvbot.dto.ActiveSelectionDto;
import com.djonique.cashbackadvbot.dto.RecommendationDto;
import com.djonique.cashbackadvbot.repository.BankCategoryMccRepository;
import com.djonique.cashbackadvbot.repository.BankExcludedMccRepository;
import com.djonique.cashbackadvbot.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashbackAdvisorService {

    private static final String MCC_REGEX = "\\d{4}";

    private final SelectionService selectionService;
    private final BankCategoryMccRepository bankCategoryMccRepository;
    private final CategoryRepository categoryRepository;
    private final BankExcludedMccRepository bankExcludedMccRepository;

    @Transactional(readOnly = true)
    public List<RecommendationDto> recommend(Long userId, String input) {
        List<ActiveSelectionDto> selections = selectionService.getActiveSelections(userId);
        if (selections.isEmpty() || input == null || input.isBlank()) {
            return Collections.emptyList();
        }

        String trimmedInput = input.trim();
        List<ActiveSelectionDto> matched = trimmedInput.matches(MCC_REGEX)
                ? matchByMccCode(trimmedInput, selections)
                : matchByCategory(trimmedInput, selections);

        log.debug("Подобрано рекомендаций: {} для userId={}, input='{}'", matched.size(), userId, trimmedInput);

        return matched.stream()
                .map(selection -> new RecommendationDto(
                        selection.bankName(),
                        selection.categoryName(),
                        selection.percent()))
                .sorted(Comparator.comparing(RecommendationDto::percent).reversed())
                .toList();
    }

    private List<ActiveSelectionDto> matchByMccCode(String mccCode, List<ActiveSelectionDto> selections) {
        Map<Long, Boolean> excludedByBankCache = new HashMap<>();

        Set<Long> categoryIds = bankCategoryMccRepository.findByMccCode(mccCode).stream()
                .filter(mcc -> !isExcludedForBank(mcc, mccCode, excludedByBankCache))
                .map(mcc -> mcc.getBankCategory().getCategory().getId())
                .collect(Collectors.toSet());

        return selections.stream()
                .filter(selection -> categoryIds.contains(selection.categoryId()))
                .toList();
    }

    private boolean isExcludedForBank(BankCategoryMcc mcc, String mccCode, Map<Long, Boolean> cache) {
        Long bankId = mcc.getBankCategory().getBank().getId();
        return cache.computeIfAbsent(bankId,
                id -> bankExcludedMccRepository.existsByBankIdAndMccCode(id, mccCode));
    }

    private List<ActiveSelectionDto> matchByCategory(String name, List<ActiveSelectionDto> selections) {
        Set<Long> matchedCategoryIds = categoryRepository.findByNameOrAliasContaining(name).stream()
                .map(Category::getId)
                .collect(Collectors.toSet());

        return selections.stream()
                .filter(selection -> matchedCategoryIds.contains(selection.categoryId()))
                .toList();
    }
}
