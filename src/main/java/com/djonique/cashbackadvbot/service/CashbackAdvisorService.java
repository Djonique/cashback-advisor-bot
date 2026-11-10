package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.domain.Category;
import com.djonique.cashbackadvbot.domain.MccCode;
import com.djonique.cashbackadvbot.domain.Store;
import com.djonique.cashbackadvbot.dto.ActiveSelectionDto;
import com.djonique.cashbackadvbot.dto.BankCategoryKey;
import com.djonique.cashbackadvbot.dto.RecommendationDto;
import com.djonique.cashbackadvbot.repository.BankCategoryMccRepository;
import com.djonique.cashbackadvbot.repository.BankExcludedMccRepository;
import com.djonique.cashbackadvbot.repository.CategoryRepository;
import com.djonique.cashbackadvbot.repository.StoreRepository;
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

    private static final String ALL_PURCHASES_SLUG = "all_purchases";

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

        SearchTarget target = resolveTarget(input.trim());
        List<ActiveSelectionDto> matched = selections.stream()
                .filter(selection -> matches(selection, target))
                .toList();

        log.debug("Совпадений: {}, userId={}", matched.size(), userId);

        // Один банк может совпасть по нескольким категориям — оставляем лучший процент.
        Map<Long, ActiveSelectionDto> bestByBank = new LinkedHashMap<>();
        matched.forEach(selection -> bestByBank.merge(
                selection.bankId(),
                selection,
                (first, second) -> comparePercent(first, second) >= 0 ? first : second));

        return bestByBank.values().stream()
                .map(selection -> new RecommendationDto(
                        selection.bankName(),
                        selection.categoryName(),
                        selection.percent()))
                .sorted(Comparator.comparing(RecommendationDto::percent,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /*
     * Цель поиска:
     * - MCC: пары банк↔категория из bank_category_mcc (MCC может относиться к нескольким категориям);
     * - магазин: те же пары, но дополнительно сужены категорией магазина (store.category_id),
     *   чтобы развести общий MCC экосистемы (например, 3990 у Яндекса);
     * - текст категории: набор categoryId, общий для всех банков.
     */
    private SearchTarget resolveTarget(String input) {
        if (MccCode.isValid(input)) {
            return targetByMccCodes(Set.of(input), Set.of());
        }

        List<Store> stores = storeRepository.findByNameOrAlias(escapeLike(input));
        if (!stores.isEmpty()) {
            Set<String> mccCodes = stores.stream()
                    .map(Store::getMccCode)
                    .filter(mccCode -> mccCode != null && !mccCode.isBlank())
                    .map(String::trim)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            Set<Long> storeCategoryIds = stores.stream()
                    .map(Store::getCategory)
                    .filter(Objects::nonNull)
                    .map(Category::getId)
                    .collect(Collectors.toSet());
            return targetByMccCodes(mccCodes, storeCategoryIds);
        }

        Set<Long> categoryIds = categoryRepository.findByNameOrAlias(escapeLike(input)).stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
        return new SearchTarget(categoryIds, Set.of(), false, Set.of());
    }

    private SearchTarget targetByMccCodes(Set<String> mccCodes, Set<Long> categoryFilter) {
        if (mccCodes.isEmpty()) {
            return new SearchTarget(Set.of(), Set.of(), true, Set.of());
        }

        Set<Long> excludedBankIds = bankExcludedMccRepository.findBankIdsByMccCodes(mccCodes);
        Set<BankCategoryKey> allowedPairs = bankCategoryMccRepository.findByMccCodeIn(mccCodes).stream()
                .filter(mcc -> !excludedBankIds.contains(mcc.getBankCategory().getBank().getId()))
                .filter(mcc -> categoryFilter.isEmpty()
                        || categoryFilter.contains(mcc.getBankCategory().getCategory().getId()))
                .map(mcc -> new BankCategoryKey(
                        mcc.getBankCategory().getBank().getId(),
                        mcc.getBankCategory().getCategory().getId()))
                .collect(Collectors.toSet());
        return new SearchTarget(Set.of(), allowedPairs, true, excludedBankIds);
    }

    private boolean matches(ActiveSelectionDto selection, SearchTarget target) {
        if (target.allowedPairs().contains(new BankCategoryKey(selection.bankId(), selection.categoryId()))
                || target.categoryIds().contains(selection.categoryId())) {
            return true;
        }
        if (!isAllPurchases(selection)) {
            return false;
        }
        if (target.mccKnown()) {
            // Для MCC/магазина «Все покупки» подходит, если банк не исключил этот MCC.
            return !target.excludedBankIds().contains(selection.bankId());
        }
        /*
         * Для текстового запроса точный MCC неизвестен, поэтому «Все покупки» подходит.
         * Но только если текст действительно нашёл хотя бы одну категорию:
         * иначе это опечатка или мусорный ввод, и рекомендаций быть не должно.
         */
        return !target.categoryIds().isEmpty();
    }

    private int comparePercent(ActiveSelectionDto first, ActiveSelectionDto second) {
        if (first.percent() == null) {
            return second.percent() == null ? 0 : -1;
        }
        if (second.percent() == null) {
            return 1;
        }
        return first.percent().compareTo(second.percent());
    }

    private boolean isAllPurchases(ActiveSelectionDto selection) {
        return ALL_PURCHASES_SLUG.equals(selection.categorySlug());
    }

    private String escapeLike(String value) {
        return value.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }

    private record SearchTarget(Set<Long> categoryIds,
                                Set<BankCategoryKey> allowedPairs,
                                boolean mccKnown,
                                Set<Long> excludedBankIds) {
    }
}
