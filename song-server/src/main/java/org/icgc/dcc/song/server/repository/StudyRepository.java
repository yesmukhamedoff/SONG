package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.study.Study;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, String> {


}
