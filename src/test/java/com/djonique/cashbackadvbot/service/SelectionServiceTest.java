package com.djonique.cashbackadvbot.service;

import com.djonique.cashbackadvbot.domain.AppUser;
import com.djonique.cashbackadvbot.domain.Bank;
import com.djonique.cashbackadvbot.domain.Category;
import com.djonique.cashbackadvbot.domain.UserBankSelection;
import com.djonique.cashbackadvbot.dto.SelectionInput;
import com.djonique.cashbackadvbot.exception.ValidationException;
import com.djonique.cashbackadvbot.repository.BankCategoryRepository;
import com.djonique.cashbackadvbot.repository.UserBankSelectionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelectionServiceTest {

    @Mock
    private UserBankSelectionRepository userBankSelectionRepository;

    @Mock
    private BankCategoryRepository bankCategoryRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private SelectionService selectionService;

    @Test
    void shouldDeleteSelection_whenItemsAreEmpty() {
        selectionService.replaceSelection(1L, 2L, List.of());

        verify(userBankSelectionRepository).deleteSelection(eq(1L), eq(2L), anyString());
        verify(userBankSelectionRepository, never()).saveAll(any());
    }

    @Test
    void shouldRejectSelection_whenPercentIsInvalid() {
        assertThatThrownBy(() -> selectionService.replaceSelection(
                1L, 2L, List.of(new SelectionInput(3L, BigDecimal.ZERO))))
                .isInstanceOf(ValidationException.class);

        verifyNoInteractions(userBankSelectionRepository);
    }

    @Test
    void shouldRejectSelection_whenCategoryDoesNotBelongToBank() {
        when(bankCategoryRepository.findCategoryIdsByBankId(2L)).thenReturn(Set.of(3L));

        assertThatThrownBy(() -> selectionService.replaceSelection(
                1L, 2L, List.of(new SelectionInput(4L, new BigDecimal("5")))))
                .isInstanceOf(ValidationException.class);

        verify(userBankSelectionRepository, never()).deleteSelection(any(), any(), any());
    }

    @Test
    void shouldReplaceSelection_whenInputIsValid() {
        when(bankCategoryRepository.findCategoryIdsByBankId(2L)).thenReturn(Set.of(3L));

        AppUser user = new AppUser();
        user.setId(1L);
        Bank bank = new Bank();
        bank.setId(2L);
        Category category = new Category();
        category.setId(3L);

        when(entityManager.getReference(Bank.class, 2L)).thenReturn(bank);
        when(entityManager.getReference(AppUser.class, 1L)).thenReturn(user);
        when(entityManager.getReference(Category.class, 3L)).thenReturn(category);

        selectionService.replaceSelection(1L, 2L,
                List.of(new SelectionInput(3L, new BigDecimal("5"))));

        verify(userBankSelectionRepository).deleteSelection(eq(1L), eq(2L), anyString());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UserBankSelection>> captor = ArgumentCaptor.forClass(List.class);
        verify(userBankSelectionRepository).saveAll(captor.capture());

        assertThat(captor.getValue()).singleElement()
                .satisfies(selection -> {
                    assertThat(selection.getUser()).isSameAs(user);
                    assertThat(selection.getBank()).isSameAs(bank);
                    assertThat(selection.getCategory()).isSameAs(category);
                    assertThat(selection.getYearMonth()).matches("\\d{4}-\\d{2}");
                    assertThat(selection.getCashbackPercent()).isEqualByComparingTo("5");
                });
    }
}
