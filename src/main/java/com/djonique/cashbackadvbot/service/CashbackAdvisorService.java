package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.domain.Category;
import com.djonique.cashbackadvbot.domain.MccCode;
import com.djonique.cashbackadvbot.domain.Store;
import com.djonique.cashbackadvbot.dto.ActiveSelectionDto;
import com.djonique.cashbackadvbot.dto.RecommendationDto;
import com.djonique.cashbackadvbot.repository.BankCategoryMccRepository;
import com.djonique.cashbackadvbot.repository.BankExcludedMccRepository;
import com.djonique.cashbackadvbot.repository.CategoryRepository;
import com.djonique.cashbackadvbot.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashbackAdvisorService {

    private final SelectionService selectionService;
    private final BankCategoryMccRepository bankCategoryMccRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final BankExcludedMccRepository bankExcludedMccRepository;

    @Transactional(readOnly = true)
    public List<RecommendationDto> recommend(Long userId, String input) {
        if (input == null || input.isBlank()) {
            return List.of();
        }

        List<ActiveSelectionDto> selections = selectionService.getActiveSelections(userId);
        if (selections.isEmpty()) {
            return List.of();
        }

        String trimmedInput = input.trim();
        List<ActiveSelectionDto> matched = MccCode.isValid(trimmedInput)
                ? matchByMccCode(trimmedInput, selections)
                : matchByText(trimmedInput, selections);

        log.debug("Подобрано рекомендаций: {} для userId={}, input='{}'", matched.size(), userId, trimmedInput);

        return matched.stream()
                .map(selection -> new RecommendationDto(
                        selection.bankName(),
                        selection.categoryName(),
                        selection.percent()))
                .sorted(Comparator.comparing(RecommendationDto::percent,
                        Comparator.nullsLast(Comparator.<BigDecimal>reverseOrder())))
                .toList();
    }

    private List<ActiveSelectionDto> matchByMccCode(String mccCode, List<ActiveSelectionDto> selections) {
        Set<Long> excludedBankIds = bankExcludedMccRepository.findBankIdsByMccCode(mccCode);

        Set<BankCategoryKey> allowedPairs = bankCategoryMccRepository.findByMccCode(mccCode).stream()
                .filter(mcc -> !excludedBankIds.contains(mcc.getBankCategory().getBank().getId()))
                .map(mcc -> new BankCategoryKey(
                        mcc.getBankCategory().getBank().getId(),
                        mcc.getBankCategory().getCategory().getId()))
                .collect(Collectors.toSet());

        return selections.stream()
                .filter(selection -> allowedPairs.contains(
                        new BankCategoryKey(selection.bankId(), selection.categoryId())))
                .toList();
    }

    private List<ActiveSelectionDto> matchByText(String text, List<ActiveSelectionDto> selections) {
        List<Store> stores = storeRepository.findByNameOrAliasContaining(escapeLike(text));
        if (stores.isEmpty()) {
            return matchByCategory(text, selections);
        }

        Set<String> mccCodes = stores.stream()
                .map(Store::getMccCode)
                .filter(mccCode -> mccCode != null && !mccCode.isBlank())
                .map(String::trim)
                .collect(Collectors.toSet());

        List<ActiveSelectionDto> byMcc = matchByMccCodes(mccCodes, selections);
        if (!byMcc.isEmpty()) {
            return byMcc;
        }

        Set<Long> storeCategoryIds = stores.stream()
                .map(Store::getCategory)
                .filter(category -> category != null)
                .map(Category::getId)
                .collect(Collectors.toSet());
        return matchByCategoryIds(storeCategoryIds, selections);
    }

    private List<ActiveSelectionDto> matchByMccCodes(Set<String> mccCodes,
                                                     List<ActiveSelectionDto> selections) {
        if (mccCodes.isEmpty()) {
            return List.of();
        }

        Set<Long> excludedBankIds = bankExcludedMccRepository.findBankIdsByMccCodes(mccCodes);
        Set<BankCategoryKey> allowedPairs = bankCategoryMccRepository.findByMccCodeIn(mccCodes).stream()
                .filter(mcc -> !excludedBankIds.contains(mcc.getBankCategory().getBank().getId()))
                .map(mcc -> new BankCategoryKey(
                        mcc.getBankCategory().getBank().getId(),
                        mcc.getBankCategory().getCategory().getId()))
                .collect(Collectors.toSet());

        return selections.stream()
                .filter(selection -> allowedPairs.contains(
                        new BankCategoryKey(selection.bankId(), selection.categoryId())))
                .toList();
    }

    private List<ActiveSelectionDto> matchByCategory(String name, List<ActiveSelectionDto> selections) {
        Set<Long> matchedCategoryIds = categoryRepository.findByNameOrAliasContaining(escapeLike(name)).stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
        return matchByCategoryIds(matchedCategoryIds, selections);
    }

    private List<ActiveSelectionDto> matchByCategoryIds(Set<Long> categoryIds,
                                                        List<ActiveSelectionDto> selections) {
        return selections.stream()
                .filter(selection -> categoryIds.contains(selection.categoryId()))
                .toList();
    }

    private String escapeLike(String value) {
        return value.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }

    private record BankCategoryKey(Long bankId, Long categoryId) {
    }
}
