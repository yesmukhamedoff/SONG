package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.analysis.impl.SequencingReadAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  SequencingReadRepository extends JpaRepository<SequencingReadAnalysisEntity, String>{

}
