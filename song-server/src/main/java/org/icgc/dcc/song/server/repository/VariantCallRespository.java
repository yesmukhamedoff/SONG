package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.analysis.VariantCallAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantCallRespository extends JpaRepository<VariantCallAnalysis, String> {

}
