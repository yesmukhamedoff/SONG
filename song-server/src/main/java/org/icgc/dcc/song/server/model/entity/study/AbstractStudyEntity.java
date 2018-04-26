package org.icgc.dcc.song.server.model.entity.study;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.FetchProfile;
import org.hibernate.annotations.FetchProfiles;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.icgc.dcc.song.server.model.entity.donor.impl.FullDonorEntity;
import org.icgc.dcc.song.server.model.entity.info.AbstractInfo.StudyInfo;
import org.icgc.dcc.song.server.model.entity.sample.impl.FullSampleEntity;
import org.icgc.dcc.song.server.model.entity.specimen.impl.FullSpecimenEntity;
import org.icgc.dcc.song.server.model.entity.study.impl.FullStudyEntity;
import org.icgc.dcc.song.server.model.entity.study.impl.StudyData;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper=true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@FilterDef(name="myFilter",
    parameters={
        @ParamDef( name="idType", type="string" )
    })
@FetchProfiles(value = {
    @FetchProfile(name = "studyWithInfo", fetchOverrides = {
        @FetchProfile.FetchOverride(entity = FullStudyEntity.class, association = ModelAttributeNames.INFO, mode = FetchMode.JOIN)}),
    @FetchProfile(name = "studyWithSamples", fetchOverrides = {
        @FetchProfile.FetchOverride(entity = FullStudyEntity.class, association = ModelAttributeNames.SAMPLES, mode = FetchMode.JOIN),
        @FetchProfile.FetchOverride(entity = FullDonorEntity.class, association = ModelAttributeNames.SPECIMENS, mode = FetchMode.JOIN),
        @FetchProfile.FetchOverride(entity = FullSpecimenEntity.class, association = ModelAttributeNames.SAMPLES, mode = FetchMode.JOIN)
    } ),
    @FetchProfile(name = "studyWithSamplesAndInfo", fetchOverrides = {
        @FetchProfile.FetchOverride(entity = FullStudyEntity.class, association = ModelAttributeNames.SAMPLES, mode = FetchMode.JOIN),
        @FetchProfile.FetchOverride(entity = FullStudyEntity.class, association = ModelAttributeNames.INFO, mode = FetchMode.JOIN),
        @FetchProfile.FetchOverride(entity = FullDonorEntity.class, association = ModelAttributeNames.SPECIMENS, mode = FetchMode.JOIN),
        @FetchProfile.FetchOverride(entity = FullDonorEntity.class, association = ModelAttributeNames.INFO, mode = FetchMode.JOIN),
        @FetchProfile.FetchOverride(entity = FullSpecimenEntity.class, association = ModelAttributeNames.SAMPLES, mode = FetchMode.JOIN),
        @FetchProfile.FetchOverride(entity = FullSpecimenEntity.class, association = ModelAttributeNames.INFO, mode = FetchMode.JOIN),
        @FetchProfile.FetchOverride(entity = FullSampleEntity.class, association = ModelAttributeNames.INFO, mode = FetchMode.JOIN)
    })
})
public abstract class AbstractStudyEntity extends StudyData implements StudyEntity {

  @Id
  @Column(name = TableAttributeNames.ID,
      updatable = false, unique = true, nullable = false)
  private String studyId;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = TableAttributeNames.ID)
  private StudyInfo info;


}
