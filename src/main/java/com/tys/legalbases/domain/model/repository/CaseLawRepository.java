package com.tys.legalbases.domain.model.repository;

import com.tys.legalbases.domain.model.entity.CaseLawEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CaseLawRepository extends JpaRepository<CaseLawEntity, Long> {

    @Query("SELECT j FROM CaseLawEntity j WHERE LOWER(j.mainTopic) LIKE LOWER(CONCAT('%', :topic, '%'))")
    List<CaseLawEntity> findByTopic(@Param("topic") String topic);

    @Query("SELECT j FROM CaseLawEntity j WHERE j.court = :court")
    List<CaseLawEntity> findByCourt(@Param("court") String court);

    @Query("SELECT j FROM CaseLawEntity j WHERE j.court = :court AND LOWER(j.mainTopic) LIKE LOWER(CONCAT('%', :topic, '%'))")
    List<CaseLawEntity> findByCourtAndTopic(@Param("court") String court, @Param("topic") String topic);

    @Query("SELECT j FROM CaseLawEntity j WHERE j.date >= :from AND j.date <= :to")
    List<CaseLawEntity> findByDateBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT j FROM CaseLawEntity j WHERE j.filingNumber = :filingNumber")
    CaseLawEntity findByFilingNumber(@Param("filingNumber") String filingNumber);

    @Query("SELECT j FROM CaseLawEntity j WHERE LOWER(j.mainTopic) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(j.summary) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(j.fullText) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<CaseLawEntity> fullTextSearch(@Param("query") String query);
}
