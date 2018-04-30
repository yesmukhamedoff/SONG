package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.study.CompositeStudyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<CompositeStudyEntity, String> {

//  @Query("select s from Sample as, Specimen as sp, Donor as d "
//      + "where s.submitter_id = ?2 "
//      + "and s.specimen_id = sp.id "
//      + "and sp.donor_id = d.id "
//      + "and d.study_id = ?1")
//  String findSampleIdByBusinessKey(String studyId, String sampleSubmitterId );

//  @Query("select sp.id from  Specimen as sp, Donor as d "
//      + "where sp.submitter_id = ?2 "
//      + "and sp.donor_id = d.id "
//      + "and d.study_id = ?1")
//  String findSpecimenIdByBusinessKey(String studyId, String specimenSubmitterId );


}
