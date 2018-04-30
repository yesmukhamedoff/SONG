package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.analysis.AbstractAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<AbstractAnalysis, String> {

}
