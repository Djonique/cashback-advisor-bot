package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.BankCategoryMcc;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface BankCategoryMccRepository extends JpaRepository<BankCategoryMcc, Long> {

    @EntityGraph(attributePaths = {"bankCategory", "bankCategory.category", "bankCategory.bank"})
    List<BankCategoryMcc> findByMccCode(String mccCode);

    @EntityGraph(attributePaths = {"bankCategory", "bankCategory.category", "bankCategory.bank"})
    List<BankCategoryMcc> findByMccCodeIn(Collection<String> mccCodes);
}
