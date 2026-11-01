package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.BankCategoryMcc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankCategoryMccRepository extends JpaRepository<BankCategoryMcc, Long> {

    List<BankCategoryMcc> findByMccCode(String mccCode);
}
