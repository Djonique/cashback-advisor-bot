package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.Bank;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {

    Optional<Bank> findBySlug(String slug);

    List<Bank> findAllByOrderByDisplayOrderAsc();
}
