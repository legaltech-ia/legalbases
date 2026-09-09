package com.tys.legalbases.domain.model.repository;

import com.tys.legalbases.domain.model.entity.LegalEdgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LegalEdgeRepository extends JpaRepository<LegalEdgeEntity, Long> {

    List<LegalEdgeEntity> findBySourceId(Long sourceId);

    List<LegalEdgeEntity> findByTargetId(Long targetId);

    boolean existsBySourceIdAndTargetIdAndRelationType(Long sourceId, Long targetId, String relationType);
}
