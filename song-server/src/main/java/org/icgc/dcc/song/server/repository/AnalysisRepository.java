package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.analysis.BaseAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<BaseAnalysis, String> {

}
