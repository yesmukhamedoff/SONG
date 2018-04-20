package org.icgc.dcc.song.server.model.experiment;

import org.icgc.dcc.song.server.model.analysis.BaseAnalysis;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractExperiment extends BaseAnalysis implements Experiment {

  @Override
  public String getAnalysisType() {
    return getType();
  }

}
