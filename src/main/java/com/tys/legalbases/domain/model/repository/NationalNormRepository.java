package com.tys.legalbases.domain.model.repository;

import com.tys.legalbases.domain.model.entity.NationalNormEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NationalNormRepository
        extends JpaRepository<NationalNormEntity, Long>,
                JpaSpecificationExecutor<NationalNormEntity> {

    Optional<NationalNormEntity> findByTitle(String title);

    @EntityGraph(attributePaths = "articles")
    List<NationalNormEntity> findAllBy();

    @Query("SELECT DISTINCT n FROM NationalNormEntity n LEFT JOIN FETCH n.articles")
    List<NationalNormEntity> findAllWithArticles();

    @Query("SELECT DISTINCT n FROM NationalNormEntity n LEFT JOIN FETCH n.articles WHERE n.id = :id")
    Optional<NationalNormEntity> findByIdWithArticles(@Param("id") Long id);
}
