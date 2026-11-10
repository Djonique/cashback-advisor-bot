package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.Store;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    /*
     * Точное совпадение без учёта регистра:
     * - по названию магазина целиком;
     * - по одному из алиасов целиком (алиасы хранятся строкой через запятую).
     * Пробелы убираются с обеих сторон, чтобы «папа джонс» совпал с алиасом «папа джонс».
     */
    @EntityGraph(attributePaths = {"category"})
    @Query(value = """
            SELECT s.* FROM store s
            WHERE LOWER(s.name) = LOWER(:text)
               OR LOWER(',' || REPLACE(s.aliases, ' ', '') || ',') LIKE LOWER('%,' || REPLACE(:text, ' ', '') || ',%')
            """, nativeQuery = true)
    List<Store> findByNameOrAlias(@Param("text") String text);
}
