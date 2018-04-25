package org.icgc.dcc.song.server.model.analysis;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.sample.impl.FullSampleEntity;
import org.icgc.dcc.song.server.model.entity.study.impl.FullStudyEntity;
import org.icgc.dcc.song.server.model.enums.Constants;
import org.icgc.dcc.song.server.model.enums.LombokAttributeNames;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.icgc.dcc.song.server.model.enums.AnalysisStates.UNPUBLISHED;

@Entity
@Table(name = TableNames.ANALYSIS)
@Inheritance(strategy = InheritanceType.JOINED)
@ToString(callSuper = true, exclude = {
    LombokAttributeNames.study,
    LombokAttributeNames.files,
    LombokAttributeNames.samples
})
@EqualsAndHashCode(callSuper = true, exclude = {
    LombokAttributeNames.study,
    LombokAttributeNames.files,
    LombokAttributeNames.samples
})
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

  @JsonIgnore
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = TableAttributeNames.STUDY_ID, nullable = false)
  private FullStudyEntity study;

  @Column(name = TableAttributeNames.STATE)
  private String analysisState = UNPUBLISHED.name();

  @Column(name = TableAttributeNames.TYPE)
  private String analysisType;

  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = ModelAttributeNames.ANALYSIS)
  private Set<File> files = newHashSet();

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinTable(
      name = TableNames.SAMPLESET,
      joinColumns = @JoinColumn(name = TableAttributeNames.ANALYSIS_ID),
      inverseJoinColumns = @JoinColumn(name = TableAttributeNames.SAMPLE_ID))
  private Set<FullSampleEntity> samples = newHashSet();

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
