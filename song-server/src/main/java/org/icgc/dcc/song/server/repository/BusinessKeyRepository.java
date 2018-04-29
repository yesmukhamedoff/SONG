package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.BusinessKeyView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface BusinessKeyRepository extends JpaRepository<BusinessKeyView, String>{

  Set<BusinessKeyView> findAllByStudyIdAndDonorSubmitterId(String studyId, String donorSubmitterId);
  Set<BusinessKeyView> findAllByStudyIdAndSpecimenSubmitterId(String studyId, String specimenSubmitterId);
  Set<BusinessKeyView> findAllByStudyIdAndSampleSubmitterId(String studyId, String sampleSubmitterId);

}
