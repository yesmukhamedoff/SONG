package org.icgc.dcc.song.server.model.analysis;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.entity.sample.SampleEntity;
import org.icgc.dcc.song.server.model.enums.Constants;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.List;

import static org.icgc.dcc.song.server.model.enums.AnalysisStates.UNPUBLISHED;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@MappedSuperclass
public abstract class AbstractAnalysis<S extends SampleEntity> extends Metadata {

  @Column(name = TableAttributeNames.STATE)
  private String analysisState = UNPUBLISHED.name();

  @Column(name = TableAttributeNames.TYPE)
  public abstract String getAnalysisType();

  public void setAnalysisState(String state) {
    Constants.validate(Constants.ANALYSIS_STATE, state);
    this.analysisState=state;
  }

  public abstract List<S> getSamples();

}
