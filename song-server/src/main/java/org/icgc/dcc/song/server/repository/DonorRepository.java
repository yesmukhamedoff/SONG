package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.donor.CompositeDonorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface DonorRepository extends JpaRepository<CompositeDonorEntity, String>{

  Set<CompositeDonorEntity> findAllByStudyId(String studyId);
  List<CompositeDonorEntity> findAllByStudyIdAndDonorSubmitterId(String studyId, String donorSubmitterId);

}
