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
    @Query(value = """
            SELECT DISTINCT c.* FROM category c
            LEFT JOIN category_alias ca ON c.id = ca.category_id
            WHERE LOWER(c.name) = LOWER(:text) 
               OR LOWER(ca.alias) = LOWER(:text)
            """, nativeQuery = true)
    List<Category> findByNameOrAlias(@Param("text") String text);
}
