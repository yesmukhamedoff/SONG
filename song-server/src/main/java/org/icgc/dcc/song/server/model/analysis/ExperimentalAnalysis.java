package org.icgc.dcc.song.server.model.analysis;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;
import org.icgc.dcc.song.server.model.JsonAttributeNames;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.experiment.Experiment;

@Value
public class ExperimentalAnalysis<E extends Experiment> extends Metadata implements Analysis {

  @JsonIgnore
  private final Analysis analysis;

  private final E experiment;

  @Override
  @JsonGetter(value = JsonAttributeNames.STUDY)
  public String getStudyId() {
    return analysis.getStudyId();
  }

  @Override
  public void setAnalysisState(String state) {
    analysis.setAnalysisState(state);
  }

  @Override
  public String getAnalysisType() {
    return analysis.getAnalysisType();
  }

  @Override
  public String getAnalysisId() {
    return analysis.getAnalysisId();
  }

  @Override
  public String getAnalysisState() {
    return analysis.getAnalysisState();
  }

  @Override
  public void setAnalysisId(String analysisId) {
    analysis.setAnalysisId(analysisId);
  }

  public static <E extends Experiment> ExperimentalAnalysis<E> createExperimentalAnalysis(Analysis analysis,
      E experiment) {
    return new ExperimentalAnalysis<E>(analysis, experiment);
  }

}
