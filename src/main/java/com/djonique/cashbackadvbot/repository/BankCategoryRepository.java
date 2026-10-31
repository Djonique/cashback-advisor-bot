package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.BankCategory;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankCategoryRepository extends JpaRepository<BankCategory, Long> {

    @EntityGraph(attributePaths = {"category"})
    List<BankCategory> findAllByBankId(Long bankId);
}
