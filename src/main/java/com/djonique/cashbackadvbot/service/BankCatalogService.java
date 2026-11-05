package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.domain.Bank;
import com.djonique.cashbackadvbot.domain.BankCategory;
import com.djonique.cashbackadvbot.domain.Category;
import com.djonique.cashbackadvbot.repository.BankCategoryRepository;
import com.djonique.cashbackadvbot.repository.BankRepository;
import com.djonique.cashbackadvbot.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BankCatalogService {

    private final BankRepository bankRepository;
    private final BankCategoryRepository bankCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Bank> getBanksOrdered() {
        return bankRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public boolean bankExists(Long bankId) {
        return bankId != null && bankRepository.existsById(bankId);
    }

    @Transactional(readOnly = true)
    public List<BankCategory> getBankCategories(Long bankId) {
        return bankCategoryRepository.findAllByBankId(bankId);
    }

    @Transactional(readOnly = true)
    public boolean categoryBelongsToBank(Long bankId, Long categoryId) {
        return bankId != null && categoryId != null
                && bankCategoryRepository.existsByBank_IdAndCategory_Id(bankId, categoryId);
    }

    @Transactional(readOnly = true)
    public Optional<String> findCategoryName(Long categoryId) {
        return categoryRepository.findById(categoryId).map(Category::getName);
    }
}
