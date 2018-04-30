package org.icgc.dcc.song.server.model.experiment;

import org.icgc.dcc.song.server.model.analysis.AbstractAnalysis;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractExperiment extends AbstractAnalysis implements Experiment {

  @Override
  public String getAnalysisType() {
    return getType();
  }

}
