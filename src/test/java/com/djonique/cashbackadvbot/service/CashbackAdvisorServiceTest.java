package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.domain.*;
import com.djonique.cashbackadvbot.dto.ActiveSelectionDto;
import com.djonique.cashbackadvbot.dto.RecommendationDto;
import com.djonique.cashbackadvbot.repository.BankCategoryMccRepository;
import com.djonique.cashbackadvbot.repository.BankExcludedMccRepository;
import com.djonique.cashbackadvbot.repository.CategoryRepository;
import com.djonique.cashbackadvbot.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashbackAdvisorServiceTest {

    private static final String MCC = "5411";

    @Mock
    private SelectionService selectionService;

    @Mock
    private BankCategoryMccRepository bankCategoryMccRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private BankExcludedMccRepository bankExcludedMccRepository;

    @InjectMocks
    private CashbackAdvisorService service;

    @Test
    void shouldNotRecommend_whenMccBelongsToAnotherBank() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(selection(1L, 10L, "5")));
        when(bankCategoryMccRepository.findByMccCodeIn(Set.of(MCC)))
                .thenReturn(List.of(mccRow(2L, 10L)));
        when(bankExcludedMccRepository.findBankIdsByMccCodes(Set.of(MCC)))
                .thenReturn(Set.of());

        List<RecommendationDto> result = service.recommend(1L, MCC);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRecommend_whenBankAndCategoryMatch() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(selection(1L, 10L, "5")));
        when(bankCategoryMccRepository.findByMccCodeIn(Set.of(MCC)))
                .thenReturn(List.of(mccRow(1L, 10L)));
        when(bankExcludedMccRepository.findBankIdsByMccCodes(Set.of(MCC)))
                .thenReturn(Set.of());

        List<RecommendationDto> result = service.recommend(1L, MCC);

        assertThat(result).singleElement()
                .satisfies(recommendation -> {
                    assertThat(recommendation.bankName()).isEqualTo("Bank1");
                    assertThat(recommendation.categoryName()).isEqualTo("Category10");
                    assertThat(recommendation.percent()).isEqualByComparingTo("5");
                });
    }

    @Test
    void shouldNotRecommend_whenMccExcludedForBank() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(selection(1L, 10L, "5")));
        when(bankCategoryMccRepository.findByMccCodeIn(Set.of(MCC)))
                .thenReturn(List.of(mccRow(1L, 10L)));
        when(bankExcludedMccRepository.findBankIdsByMccCodes(Set.of(MCC)))
                .thenReturn(Set.of(1L));

        List<RecommendationDto> result = service.recommend(1L, MCC);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRecommendAllPurchases_whenMccIsNotExcluded() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(allPurchases(1L, "5")));
        when(bankCategoryMccRepository.findByMccCodeIn(Set.of(MCC)))
                .thenReturn(List.of());
        when(bankExcludedMccRepository.findBankIdsByMccCodes(Set.of(MCC)))
                .thenReturn(Set.of());

        List<RecommendationDto> result = service.recommend(1L, MCC);

        assertThat(result).singleElement()
                .satisfies(recommendation -> {
                    assertThat(recommendation.bankName()).isEqualTo("Bank1");
                    assertThat(recommendation.categoryName()).isEqualTo("Все покупки");
                    assertThat(recommendation.percent()).isEqualByComparingTo("5");
                });
    }

    @Test
    void shouldNotRecommendAllPurchases_whenMccExcludedForBank() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(allPurchases(1L, "5")));
        when(bankCategoryMccRepository.findByMccCodeIn(Set.of(MCC)))
                .thenReturn(List.of());
        when(bankExcludedMccRepository.findBankIdsByMccCodes(Set.of(MCC)))
                .thenReturn(Set.of(1L));

        List<RecommendationDto> result = service.recommend(1L, MCC);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldKeepAllPurchases_whenItsPercentIsHigherThanCategoryPercent() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(
                        selection(1L, 10L, "3"),
                        allPurchases(1L, "7")));
        when(bankCategoryMccRepository.findByMccCodeIn(Set.of(MCC)))
                .thenReturn(List.of(mccRow(1L, 10L)));
        when(bankExcludedMccRepository.findBankIdsByMccCodes(Set.of(MCC)))
                .thenReturn(Set.of());

        List<RecommendationDto> result = service.recommend(1L, MCC);

        assertThat(result).singleElement()
                .satisfies(recommendation -> {
                    assertThat(recommendation.categoryName()).isEqualTo("Все покупки");
                    assertThat(recommendation.percent()).isEqualByComparingTo("7");
                });
    }

    @Test
    void shouldRecommendAllPurchases_whenStoreMccHasNoCategoryMapping() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(allPurchases(1L, "5")));
        Store store = new Store();
        store.setName("Пятёрочка");
        store.setMccCode(MCC);
        when(storeRepository.findAllWithCategory())
                .thenReturn(List.of(store));
        when(bankCategoryMccRepository.findByMccCodeIn(Set.of(MCC)))
                .thenReturn(List.of());
        when(bankExcludedMccRepository.findBankIdsByMccCodes(Set.of(MCC)))
                .thenReturn(Set.of());

        List<RecommendationDto> result = service.recommend(1L, "пятёрочка");

        assertThat(result).singleElement()
                .satisfies(recommendation -> {
                    assertThat(recommendation.categoryName()).isEqualTo("Все покупки");
                    assertThat(recommendation.percent()).isEqualByComparingTo("5");
                });
    }

    @Test
    void shouldSortByPercentDescending_whenSearchingByCategory() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(
                        selection(1L, 10L, "3"),
                        selection(2L, 10L, "10"),
                        selection(3L, 10L, "7")));
        Category category = new Category();
        category.setId(10L);
        category.setName("Category10");
        when(categoryRepository.findByNameOrAlias("category"))
                .thenReturn(List.of(category));

        List<RecommendationDto> result = service.recommend(1L, "category");

        assertThat(result)
                .extracting(RecommendationDto::percent)
                .containsExactly(new BigDecimal("10"), new BigDecimal("7"), new BigDecimal("3"));
    }

    @Test
    void shouldKeepOnlyBestPercent_whenSameBankMatchesSeveralCategories() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(
                        selection(1L, 10L, "3"),
                        selection(1L, 20L, "7")));
        Category first = new Category();
        first.setId(10L);
        first.setName("Category10");
        Category second = new Category();
        second.setId(20L);
        second.setName("Category20");
        when(categoryRepository.findByNameOrAlias("category"))
                .thenReturn(List.of(first, second));

        List<RecommendationDto> result = service.recommend(1L, "category");

        assertThat(result).singleElement()
                .satisfies(recommendation -> {
                    assertThat(recommendation.bankName()).isEqualTo("Bank1");
                    assertThat(recommendation.categoryName()).isEqualTo("Category20");
                    assertThat(recommendation.percent()).isEqualByComparingTo("7");
                });
    }

    @Test
    void shouldRecommendByStoreMcc_whenStoreFound() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(selection(1L, 10L, "5")));
        Store store = new Store();
        store.setName("Пятёрочка");
        store.setMccCode(MCC);
        when(storeRepository.findAllWithCategory())
                .thenReturn(List.of(store));
        when(bankCategoryMccRepository.findByMccCodeIn(Set.of(MCC)))
                .thenReturn(List.of(mccRow(1L, 10L)));
        when(bankExcludedMccRepository.findBankIdsByMccCodes(Set.of(MCC)))
                .thenReturn(Set.of());

        List<RecommendationDto> result = service.recommend(1L, "пятёрочка");

        assertThat(result).singleElement()
                .satisfies(recommendation -> {
                    assertThat(recommendation.bankName()).isEqualTo("Bank1");
                    assertThat(recommendation.percent()).isEqualByComparingTo("5");
                });
    }

    @Test
    void shouldNarrowByStoreCategory_whenMccIsSharedBySeveralCategories() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(
                        selection(1L, 10L, "5"),
                        selection(1L, 20L, "3")));
        Category taxi = new Category();
        taxi.setId(20L);
        taxi.setName("Такси");
        Store store = new Store();
        store.setName("Яндекс Такси");
        store.setMccCode("3990");
        store.setCategory(taxi);
        when(storeRepository.findAllWithCategory())
                .thenReturn(List.of(store));
        when(bankCategoryMccRepository.findByMccCodeIn(Set.of("3990")))
                .thenReturn(List.of(
                        mccRow("3990", 1L, 10L),
                        mccRow("3990", 1L, 20L)));
        when(bankExcludedMccRepository.findBankIdsByMccCodes(Set.of("3990")))
                .thenReturn(Set.of());

        List<RecommendationDto> result = service.recommend(1L, "яндекс такси");

        assertThat(result).singleElement()
                .satisfies(recommendation -> {
                    assertThat(recommendation.categoryName()).isEqualTo("Category20");
                    assertThat(recommendation.percent()).isEqualByComparingTo("3");
                });
    }

    @Test
    void shouldNotMatchStoreCategory_whenStoreMccHasNoCategoryMapping() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(selection(1L, 10L, "5")));
        Category storeCategory = new Category();
        storeCategory.setId(10L);
        storeCategory.setName("Супермаркеты");
        Store store = new Store();
        store.setName("Пятёрочка");
        store.setMccCode(MCC);
        store.setCategory(storeCategory);
        when(storeRepository.findAllWithCategory())
                .thenReturn(List.of(store));
        when(bankCategoryMccRepository.findByMccCodeIn(Set.of(MCC)))
                .thenReturn(List.of());
        when(bankExcludedMccRepository.findBankIdsByMccCodes(Set.of(MCC)))
                .thenReturn(Set.of());

        List<RecommendationDto> result = service.recommend(1L, "пятёрочка");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRecommendAllPurchases_whenSearchingByCategoryName() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(
                        selection(1L, 10L, "5"),
                        allPurchases(2L, "1")));
        Category category = new Category();
        category.setId(10L);
        category.setName("Супермаркеты");
        when(storeRepository.findAllWithCategory())
                .thenReturn(List.of());
        when(categoryRepository.findByNameOrAlias("продукты"))
                .thenReturn(List.of(category));

        List<RecommendationDto> result = service.recommend(1L, "продукты");

        assertThat(result)
                .extracting(RecommendationDto::categoryName)
                .containsExactly("Category10", "Все покупки");
    }

    @Test
    void shouldNotRecommendAnything_whenTextMatchesNoStoreAndNoCategory() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(allPurchases(1L, "5")));
        when(storeRepository.findAllWithCategory())
                .thenReturn(List.of());
        when(categoryRepository.findByNameOrAlias("ААААААА"))
                .thenReturn(List.of());

        List<RecommendationDto> result = service.recommend(1L, "ААААААА");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldPassSpecialCharactersToCategoryQuery() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(selection(1L, 10L, "5")));
        when(storeRepository.findAllWithCategory())
                .thenReturn(List.of());
        when(categoryRepository.findByNameOrAlias("50%_!"))
                .thenReturn(List.of());

        assertThat(service.recommend(1L, "50%_!")).isEmpty();

        verify(categoryRepository).findByNameOrAlias("50%_!");
    }

    @Test
    void shouldNotMatchStore_whenInputIsOnlyPartOfName() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(allPurchases(1L, "5")));
        Store samokat = new Store();
        samokat.setName("Самокат");
        samokat.setAliases("самокат, samokat");
        samokat.setMccCode("3991");
        Store samberi = new Store();
        samberi.setName("Самбери");
        samberi.setAliases("самбери, samberi");
        samberi.setMccCode("5411");
        when(storeRepository.findAllWithCategory())
                .thenReturn(List.of(samokat, samberi));
        when(categoryRepository.findByNameOrAlias("сам"))
                .thenReturn(List.of());

        assertThat(service.recommend(1L, "сам")).isEmpty();
    }

    @Test
    void shouldMatchStoreByAliasCaseInsensitively() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(selection(1L, 10L, "5")));
        Store store = new Store();
        store.setName("Papa John’s");
        store.setAliases("папа джонс, papa johns");
        store.setMccCode("5814");
        when(storeRepository.findAllWithCategory())
                .thenReturn(List.of(store));
        when(bankCategoryMccRepository.findByMccCodeIn(Set.of("5814")))
                .thenReturn(List.of(mccRow("5814", 1L, 10L)));
        when(bankExcludedMccRepository.findBankIdsByMccCodes(Set.of("5814")))
                .thenReturn(Set.of());

        List<RecommendationDto> result = service.recommend(1L, "ПАПА ДЖОНС");

        assertThat(result).singleElement()
                .satisfies(recommendation -> {
                    assertThat(recommendation.bankName()).isEqualTo("Bank1");
                    assertThat(recommendation.percent()).isEqualByComparingTo("5");
                });
    }

    private ActiveSelectionDto selection(Long bankId, Long categoryId, String percent) {
        return new ActiveSelectionDto(
                bankId,
                "Bank" + bankId,
                categoryId,
                "category" + categoryId,
                "Category" + categoryId,
                new BigDecimal(percent));
    }

    private ActiveSelectionDto allPurchases(Long bankId, String percent) {
        return new ActiveSelectionDto(
                bankId,
                "Bank" + bankId,
                100L,
                "all_purchases",
                "Все покупки",
                new BigDecimal(percent));
    }

    private BankCategoryMcc mccRow(Long bankId, Long categoryId) {
        return mccRow(MCC, bankId, categoryId);
    }

    private BankCategoryMcc mccRow(String mccCode, Long bankId, Long categoryId) {
        Bank bank = new Bank();
        bank.setId(bankId);
        bank.setName("Bank" + bankId);

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Category" + categoryId);

        BankCategory bankCategory = new BankCategory();
        bankCategory.setBank(bank);
        bankCategory.setCategory(category);

        BankCategoryMcc mcc = new BankCategoryMcc();
        mcc.setBankCategory(bankCategory);
        mcc.setMccCode(mccCode);
        return mcc;
    }
}
