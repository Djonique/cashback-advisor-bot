package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.BankCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankCategoryRepository extends JpaRepository<BankCategory, Long> {

    @EntityGraph(attributePaths = {"category"})
    List<BankCategory> findAllByBankId(Long bankId);
}
