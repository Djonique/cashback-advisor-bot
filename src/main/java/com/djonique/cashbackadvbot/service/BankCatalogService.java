package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.domain.Bank;
import com.djonique.cashbackadvbot.domain.Category;
import com.djonique.cashbackadvbot.dto.BankCategoryDto;
import com.djonique.cashbackadvbot.dto.BankDto;
import com.djonique.cashbackadvbot.repository.BankCategoryRepository;
import com.djonique.cashbackadvbot.repository.BankRepository;
import com.djonique.cashbackadvbot.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Справочники банков и категорий меняются только миграциями Flyway,
 * поэтому кэшируем их в памяти до перезапуска приложения.
 */
@Service
@RequiredArgsConstructor
public class BankCatalogService {

    private final BankRepository bankRepository;
    private final BankCategoryRepository bankCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final Map<Long, List<BankCategoryDto>> bankCategoriesCache = new ConcurrentHashMap<>();
    private final Map<Long, Optional<String>> categoryNamesCache = new ConcurrentHashMap<>();
    private volatile List<BankDto> banksCache;

    @Transactional(readOnly = true)
    public List<BankDto> getBanksOrdered() {
        List<BankDto> cached = banksCache;
        if (cached == null) {
            cached = bankRepository.findAllByOrderByDisplayOrderAsc().stream()
                    .map(this::toBankDto)
                    .toList();
            banksCache = cached;
        }
        return cached;
    }

    @Transactional(readOnly = true)
    public boolean isBankMissing(Long bankId) {
        return bankId == null || !bankRepository.existsById(bankId);
    }

    @Transactional(readOnly = true)
    public List<BankCategoryDto> getBankCategories(Long bankId) {
        return bankCategoriesCache.computeIfAbsent(bankId,
                id -> bankCategoryRepository.findAllByBankId(id).stream()
                        .map(bankCategory -> new BankCategoryDto(
                                bankCategory.getCategory().getId(),
                                bankCategory.getCategory().getSlug(),
                                bankCategory.getDisplayName()))
                        .toList());
    }

    @Transactional(readOnly = true)
    public boolean categoryBelongsToBank(Long bankId, Long categoryId) {
        return bankId != null && categoryId != null
                && bankCategoryRepository.existsByBank_IdAndCategory_Id(bankId, categoryId);
    }

    @Transactional(readOnly = true)
    public Optional<String> findCategoryName(Long categoryId) {
        return categoryNamesCache.computeIfAbsent(categoryId,
                id -> categoryRepository.findById(id).map(Category::getName));
    }

    private BankDto toBankDto(Bank bank) {
        return new BankDto(bank.getId(), bank.getName());
    }
}
