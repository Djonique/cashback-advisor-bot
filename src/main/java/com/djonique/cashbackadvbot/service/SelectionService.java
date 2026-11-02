package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.domain.Bank;
import com.djonique.cashbackadvbot.domain.CashbackPercent;
import com.djonique.cashbackadvbot.domain.Category;
import com.djonique.cashbackadvbot.domain.UserBankSelection;
import com.djonique.cashbackadvbot.dto.ActiveSelectionDto;
import com.djonique.cashbackadvbot.dto.SelectionInput;
import com.djonique.cashbackadvbot.exception.ValidationException;
import com.djonique.cashbackadvbot.repository.BankCategoryRepository;
import com.djonique.cashbackadvbot.repository.UserBankSelectionRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SelectionService {

    private static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");

    private final UserBankSelectionRepository userBankSelectionRepository;
    private final BankCategoryRepository bankCategoryRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<ActiveSelectionDto> getActiveSelections(Long userId) {
        String yearMonth = currentYearMonth();
        List<UserBankSelection> selections =
                userBankSelectionRepository.findAllByUserIdAndYearMonth(userId, yearMonth);
        Map<BankCategoryKey, String> localNames = loadLocalCategoryNames(selections);

        return selections.stream()
                .map(selection -> toDto(selection, localNames))
                .toList();
    }

    @Transactional
    public void replaceSelection(Long userId, Long bankId, List<SelectionInput> items) {
        String yearMonth = currentYearMonth();
        List<SelectionInput> safeItems = items == null ? List.of() : items;

        safeItems.forEach(this::validatePercent);

        if (!safeItems.isEmpty()) {
            Set<Long> allowedCategoryIds = bankCategoryRepository.findCategoryIdsByBankId(bankId);
            safeItems.forEach(item -> validateCategoryBelongsToBank(item, allowedCategoryIds));
        }

        log.info("Saving selection: userId={}, bankId={}, items={}", userId, bankId, safeItems);

        userBankSelectionRepository.deleteSelection(userId, bankId, yearMonth);

        if (safeItems.isEmpty()) {
            log.info("Выборы очищены: userId={}, bankId={}, yearMonth={}", userId, bankId, yearMonth);
            return;
        }

        Bank bank = entityManager.getReference(Bank.class, bankId);
        List<UserBankSelection> selections = safeItems.stream()
                .map(item -> createSelection(userId, bank, item, yearMonth))
                .toList();
        userBankSelectionRepository.saveAll(selections);
    }

    private Map<BankCategoryKey, String> loadLocalCategoryNames(List<UserBankSelection> selections) {
        Set<Long> bankIds = selections.stream()
                .map(selection -> selection.getBank().getId())
                .collect(Collectors.toSet());
        if (bankIds.isEmpty()) {
            return Map.of();
        }

        return bankCategoryRepository.findAllByBankIdIn(bankIds).stream()
                .collect(Collectors.toMap(
                        bankCategory -> new BankCategoryKey(
                                bankCategory.getBank().getId(),
                                bankCategory.getCategory().getId()),
                        bankCategory -> bankCategory.getDisplayName(),
                        (first, second) -> first));
    }

    private ActiveSelectionDto toDto(UserBankSelection selection, Map<BankCategoryKey, String> localNames) {
        Bank bank = selection.getBank();
        Category category = selection.getCategory();
        String categoryName = localNames.getOrDefault(
                new BankCategoryKey(bank.getId(), category.getId()),
                category.getName());
        return new ActiveSelectionDto(
                bank.getId(),
                bank.getName(),
                category.getId(),
                categoryName,
                selection.getCashbackPercent());
    }

    private UserBankSelection createSelection(Long userId, Bank bank, SelectionInput item, String yearMonth) {
        UserBankSelection selection = new UserBankSelection();
        selection.setUser(entityManager.getReference(AppUser.class, userId));
        selection.setBank(bank);
        selection.setCategory(entityManager.getReference(Category.class, item.categoryId()));
        selection.setYearMonth(yearMonth);
        selection.setCashbackPercent(item.percent());
        return selection;
    }

    private void validatePercent(SelectionInput item) {
        if (!CashbackPercent.isValid(item.percent())) {
            throw new ValidationException("Процент должен быть больше 0 и не больше 100");
        }
    }

    private void validateCategoryBelongsToBank(SelectionInput item, Set<Long> allowedCategoryIds) {
        if (!allowedCategoryIds.contains(item.categoryId())) {
            throw new ValidationException("Категория не принадлежит выбранному банку");
        }
    }

    private String currentYearMonth() {
        return YearMonth.now(MOSCOW_ZONE).toString();
    }

    private record BankCategoryKey(Long bankId, Long categoryId) {
    }
}
