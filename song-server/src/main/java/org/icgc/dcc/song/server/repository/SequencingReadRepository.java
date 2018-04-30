package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.analysis.SequencingReadAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  SequencingReadRepository extends JpaRepository<SequencingReadAnalysis, String>{

}
