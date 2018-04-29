package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.specimen.CompositeSpecimenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface SpecimenRepository extends  JpaRepository<CompositeSpecimenEntity, String> {

  Set<CompositeSpecimenEntity> findAllByDonorId(String donorId);
  Set<CompositeSpecimenEntity> findAllBySpecimenSubmitterId(String specimenSubmitterId);

}
