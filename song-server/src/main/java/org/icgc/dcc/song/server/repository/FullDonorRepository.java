package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.donor.impl.FullDonorEntity;
import org.icgc.dcc.song.server.model.entity.study.impl.FullStudyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface FullDonorRepository extends JpaRepository<FullDonorEntity, String>{

  Set<FullDonorEntity> findAllByStudy(FullStudyEntity study);
  List<FullDonorEntity> findAllByStudyAndDonorSubmitterId(FullStudyEntity study, String donorSubmitterId);

}
