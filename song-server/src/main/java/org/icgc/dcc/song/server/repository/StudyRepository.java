package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.study.impl.FullStudyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface StudyRepository extends JpaRepository<FullStudyEntity, String> {


  <T> Collection<T> findByStudyId(String studyId, Class<T> tClass);



}
