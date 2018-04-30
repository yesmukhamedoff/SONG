package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.analysis.AbstractAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<AbstractAnalysisEntity, String> {

}
