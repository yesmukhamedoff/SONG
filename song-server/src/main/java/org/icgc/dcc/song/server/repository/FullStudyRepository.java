package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.study.impl.FullStudyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FullStudyRepository extends JpaRepository<FullStudyEntity, String> {

}
