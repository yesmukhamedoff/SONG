package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.analysis.impl.VariantCallAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantCallRespository extends JpaRepository<VariantCallAnalysisEntity, String> {

}
