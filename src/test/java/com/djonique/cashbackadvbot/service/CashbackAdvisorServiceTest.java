package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.domain.Bank;
import com.djonique.cashbackadvbot.domain.BankCategory;
import com.djonique.cashbackadvbot.domain.BankCategoryMcc;
import com.djonique.cashbackadvbot.domain.Category;
import com.djonique.cashbackadvbot.domain.Store;
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
        when(bankCategoryMccRepository.findByMccCode(MCC))
                .thenReturn(List.of(mccRow(2L, 10L)));
        when(bankExcludedMccRepository.findBankIdsByMccCode(MCC))
                .thenReturn(Set.of());

        List<RecommendationDto> result = service.recommend(1L, MCC);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRecommend_whenBankAndCategoryMatch() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(selection(1L, 10L, "5")));
        when(bankCategoryMccRepository.findByMccCode(MCC))
                .thenReturn(List.of(mccRow(1L, 10L)));
        when(bankExcludedMccRepository.findBankIdsByMccCode(MCC))
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
        when(bankCategoryMccRepository.findByMccCode(MCC))
                .thenReturn(List.of(mccRow(1L, 10L)));
        when(bankExcludedMccRepository.findBankIdsByMccCode(MCC))
                .thenReturn(Set.of(1L));

        List<RecommendationDto> result = service.recommend(1L, MCC);

        assertThat(result).isEmpty();
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
        when(categoryRepository.findByNameOrAliasContaining("category"))
                .thenReturn(List.of(category));

        List<RecommendationDto> result = service.recommend(1L, "category");

        assertThat(result)
                .extracting(RecommendationDto::percent)
                .containsExactly(new BigDecimal("10"), new BigDecimal("7"), new BigDecimal("3"));
    }

    @Test
    void shouldRecommendByStoreMcc_whenStoreFound() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(selection(1L, 10L, "5")));
        Store store = new Store();
        store.setName("РџСЏС‚С‘СЂРѕС‡РєР°");
        store.setMccCode(MCC);
        when(storeRepository.findByNameOrAliasContaining("РїСЏС‚РµСЂРѕС‡РєР°"))
                .thenReturn(List.of(store));
        when(bankCategoryMccRepository.findByMccCodeIn(Set.of(MCC)))
                .thenReturn(List.of(mccRow(1L, 10L)));
        when(bankExcludedMccRepository.findBankIdsByMccCodes(Set.of(MCC)))
                .thenReturn(Set.of());

        List<RecommendationDto> result = service.recommend(1L, "РїСЏС‚РµСЂРѕС‡РєР°");

        assertThat(result).singleElement()
                .satisfies(recommendation -> {
                    assertThat(recommendation.bankName()).isEqualTo("Bank1");
                    assertThat(recommendation.percent()).isEqualByComparingTo("5");
                });
    }

    @Test
    void shouldFallbackToStoreCategory_whenStoreMccIsNotMapped() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(selection(1L, 10L, "5")));
        Category storeCategory = new Category();
        storeCategory.setId(10L);
        storeCategory.setName("РЎСѓРїРµСЂРјР°СЂРєРµС‚С‹");
        Store store = new Store();
        store.setName("РџСЏС‚С‘СЂРѕС‡РєР°");
        store.setMccCode(MCC);
        store.setCategory(storeCategory);
        when(storeRepository.findByNameOrAliasContaining("РїСЏС‚РµСЂРѕС‡РєР°"))
                .thenReturn(List.of(store));
        when(bankCategoryMccRepository.findByMccCodeIn(Set.of(MCC)))
                .thenReturn(List.of());
        when(bankExcludedMccRepository.findBankIdsByMccCodes(Set.of(MCC)))
                .thenReturn(Set.of());

        List<RecommendationDto> result = service.recommend(1L, "РїСЏС‚РµСЂРѕС‡РєР°");

        assertThat(result).singleElement()
                .satisfies(recommendation -> {
                    assertThat(recommendation.categoryName()).isEqualTo("Category10");
                    assertThat(recommendation.percent()).isEqualByComparingTo("5");
                });
    }

    @Test
    void shouldEscapeLikeWildcards_whenSearchingByText() {
        when(selectionService.getActiveSelections(1L))
                .thenReturn(List.of(selection(1L, 10L, "5")));
        when(storeRepository.findByNameOrAliasContaining("50!%!_!!"))
                .thenReturn(List.of());
        when(categoryRepository.findByNameOrAliasContaining("50!%!_!!"))
                .thenReturn(List.of());

        assertThat(service.recommend(1L, "50%_!")).isEmpty();

        verify(storeRepository).findByNameOrAliasContaining("50!%!_!!");
        verify(categoryRepository).findByNameOrAliasContaining("50!%!_!!");
    }

    private ActiveSelectionDto selection(Long bankId, Long categoryId, String percent) {
        return new ActiveSelectionDto(
                bankId,
                "Bank" + bankId,
                categoryId,
                "Category" + categoryId,
                new BigDecimal(percent));
    }

    private BankCategoryMcc mccRow(Long bankId, Long categoryId) {
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
        mcc.setMccCode(MCC);
        return mcc;
    }
}
