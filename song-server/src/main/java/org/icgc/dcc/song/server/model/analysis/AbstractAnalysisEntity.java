package org.icgc.dcc.song.server.model.analysis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.icgc.dcc.song.server.model.analysis.impl.SequencingReadAnalysisEntity;
import org.icgc.dcc.song.server.model.analysis.impl.VariantCallAnalysisEntity;
import org.icgc.dcc.song.server.model.entity.file.FileEntity;
import org.icgc.dcc.song.server.model.entity.sample.CompositeSampleEntity;
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
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.icgc.dcc.song.server.model.enums.Constants.SEQUENCING_READ_TYPE;
import static org.icgc.dcc.song.server.model.enums.Constants.VARIANT_CALL_TYPE;

@Entity
@Table(name = TableNames.ANALYSIS)
@Inheritance(strategy = InheritanceType.JOINED)
@ToString(callSuper = true
    , exclude = {
//    LombokAttributeNames.study,
//    LombokAttributeNames.files,
    LombokAttributeNames.samples
}
)
@EqualsAndHashCode(callSuper = true
    , exclude = {
//    LombokAttributeNames.study,
//    LombokAttributeNames.files,
    LombokAttributeNames.samples
}
)
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonTypeInfo(
    use=JsonTypeInfo.Id.NAME,
    include=JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property=ModelAttributeNames.ANALYSIS_TYPE
)
@JsonSubTypes({
    @JsonSubTypes.Type(value=SequencingReadAnalysisEntity.class, name=SEQUENCING_READ_TYPE),
    @JsonSubTypes.Type(value=VariantCallAnalysisEntity.class, name=VARIANT_CALL_TYPE)
})
public abstract class AbstractAnalysisEntity extends AbstractAnalysis<CompositeSampleEntity> {

  @Id
  @Column(name = TableAttributeNames.ID,
      updatable = false, unique = true, nullable = false)
  private String analysisId="";

  @Column(name = TableAttributeNames.STUDY_ID, nullable = false)
  private String studyId;

  //TODO: rtisma not ready yet for this....
//  @JsonIgnore
//  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//  @JoinColumn(name = TableAttributeNames.STUDY_ID, nullable = false)
//  private CompositeStudyEntity study;

  //TODO: rtisma not ready yet for this....
//  @OneToMany(cascade = CascadeType.ALL,
//      fetch = FetchType.LAZY,
//      mappedBy = ModelAttributeNames.ANALYSIS)
  @Transient
  private List<FileEntity> files;

//  @Transient
//  private List<CompositeEntity> samples;

  //TODO: rtisma not ready for this relationship yet....for not managing manually, so this would required
 //  significant change to alot of things downstream

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinTable(
      name = TableNames.SAMPLESET,
      joinColumns = @JoinColumn(name = TableAttributeNames.ANALYSIS_ID),
      inverseJoinColumns = @JoinColumn(name = TableAttributeNames.SAMPLE_ID))
  private Set<CompositeSampleEntity> samples = newHashSet();

}
