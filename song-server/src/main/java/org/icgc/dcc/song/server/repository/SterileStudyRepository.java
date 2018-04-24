package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.study.impl.SterileStudyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SterileStudyRepository extends JpaRepository<SterileStudyEntity, String>{

}
