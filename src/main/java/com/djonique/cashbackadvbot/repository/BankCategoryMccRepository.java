package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.BankCategoryMcc;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankCategoryMccRepository extends JpaRepository<BankCategoryMcc, Long> {

    List<BankCategoryMcc> findByMccCode(String mccCode);
}
