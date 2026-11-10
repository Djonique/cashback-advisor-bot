package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.Store;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    /*
     * Магазины — небольшой статичный справочник (сотни строк), поэтому точное
     * сравнение по названию и алиасам выполняет сервис, а не JPQL.
     */
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT s FROM Store s")
    List<Store> findAllWithCategory();
}
