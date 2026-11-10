package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /*
     * Точное совпадение без учёта регистра: с названием категории целиком
     * или с одним из её алиасов целиком.
     */
    @Query("""
            SELECT DISTINCT c FROM Category c
            LEFT JOIN c.aliases a
            WHERE LOWER(c.name) = LOWER(:text)
               OR LOWER(a.alias) = LOWER(:text)
            """)
    List<Category> findByNameOrAlias(@Param("text") String text);
}
