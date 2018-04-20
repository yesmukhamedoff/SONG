package org.icgc.dcc.song.server.model.analysis;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.ModelAttributeNames;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.model.enums.Constants;
import org.icgc.dcc.song.server.model.enums.TableNames;
import org.icgc.dcc.song.server.repository.TableAttributeNames;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static org.icgc.dcc.song.server.model.enums.AnalysisStates.UNPUBLISHED;

@Entity
@Table(name = TableNames.ANALYSIS)
@Inheritance(strategy = InheritanceType.JOINED)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonTypeInfo(
    use=JsonTypeInfo.Id.NAME,
    include=JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property=ModelAttributeNames.ANALYSIS_TYPE
)
//@JsonSubTypes({
//    @JsonSubTypes.Type(value=SequencingReadAnalysis.class, name=SEQUENCING_READ_TYPE)
//    @JsonSubTypes.Type(value=VariantCallAnalysis.class, name=VARIANT_CALL_TYPE)
//})
public class BaseAnalysis extends Metadata implements Analysis {

  @Id
  @Column(name = TableAttributeNames.ID,
      updatable = false, unique = true, nullable = false)
  private String analysisId="";

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = TableAttributeNames.STUDY_ID)
  @JsonIgnore
  private Study study;

  @Column(name = TableAttributeNames.STATE)
  private String analysisState = UNPUBLISHED.name();

  @Column(name = TableAttributeNames.TYPE)
  private String analysisType;

  //    @OneToMany
  //    private List<CompositeEntity> sample;

  //    @OneToMany
  //    private List<File> file;

  @Override
  public void setAnalysisState(String state) {
    Constants.validate(Constants.ANALYSIS_STATE, state);
    this.analysisState=state;
  }

  @Override
  @JsonGetter(value = "study")
  public String getStudyId() {
    return study.getStudyId();
  }

}
