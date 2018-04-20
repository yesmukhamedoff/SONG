package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.experiment.SequencingReadExperiment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  SequencingReadRepository extends JpaRepository<SequencingReadExperiment, String>{

}
