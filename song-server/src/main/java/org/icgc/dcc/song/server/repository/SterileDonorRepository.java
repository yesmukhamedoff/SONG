package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.donor.impl.SterileDonorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SterileDonorRepository extends JpaRepository<SterileDonorEntity, String>{

  List<SterileDonorEntity> findAllByStudyId(String studyId);
  List<SterileDonorEntity> findAllByStudyIdAndDonorSubmitterId(String studyId, String submitterId);

}
