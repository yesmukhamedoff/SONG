package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.single.SingleStudy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyDTORepository extends JpaRepository<SingleStudy, String> {

}
