package com.djonique.cashbackadvbot.repository;

import com.djonique.cashbackadvbot.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    @Query("""
            SELECT DISTINCT c FROM Category c
            LEFT JOIN c.aliases a
            WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :text, '%')) ESCAPE '!'
               OR LOWER(a.alias) LIKE LOWER(CONCAT('%', :text, '%')) ESCAPE '!'
            """)
    List<Category> findByNameOrAliasContaining(@Param("text") String text);
}
