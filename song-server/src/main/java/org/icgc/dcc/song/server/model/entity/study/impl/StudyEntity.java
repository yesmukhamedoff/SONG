package org.icgc.dcc.song.server.model.entity.study.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.FetchProfile;
import org.hibernate.annotations.FetchProfiles;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.icgc.dcc.song.server.model.entity.donor.CompositeDonorEntity;
import org.icgc.dcc.song.server.model.entity.sample.CompositeSampleEntity;
import org.icgc.dcc.song.server.model.entity.specimen.CompositeSpecimenEntity;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@FilterDef(name="myFilter",
    parameters={
        @ParamDef( name="idType", type="string" )
    })
@FetchProfiles(value = {
    @FetchProfile(name = "studyWithInfo", fetchOverrides = {
        @FetchProfile.FetchOverride(entity = CompositeStudyEntity.class, association = ModelAttributeNames.INFO, mode = FetchMode.JOIN)}),
    @FetchProfile(name = "studyWithSamples", fetchOverrides = {
        @FetchProfile.FetchOverride(entity = CompositeStudyEntity.class, association = ModelAttributeNames.SAMPLES, mode = FetchMode.JOIN),
        @FetchProfile.FetchOverride(entity = CompositeDonorEntity.class, association = ModelAttributeNames.SPECIMENS, mode = FetchMode.JOIN),
        @FetchProfile.FetchOverride(entity = CompositeSpecimenEntity.class, association = ModelAttributeNames.SAMPLES, mode = FetchMode.JOIN)
    } ),
    @FetchProfile(name = "studyWithSamplesAndInfo", fetchOverrides = {
        @FetchProfile.FetchOverride(entity = CompositeStudyEntity.class, association = ModelAttributeNames.SAMPLES, mode = FetchMode.JOIN),
        @FetchProfile.FetchOverride(entity = CompositeStudyEntity.class, association = ModelAttributeNames.INFO, mode = FetchMode.JOIN),
        @FetchProfile.FetchOverride(entity = CompositeDonorEntity.class, association = ModelAttributeNames.SPECIMENS, mode = FetchMode.JOIN),
        @FetchProfile.FetchOverride(entity = CompositeDonorEntity.class, association = ModelAttributeNames.INFO, mode = FetchMode.JOIN),
        @FetchProfile.FetchOverride(entity = CompositeSpecimenEntity.class, association = ModelAttributeNames.SAMPLES, mode = FetchMode.JOIN),
        @FetchProfile.FetchOverride(entity = CompositeSpecimenEntity.class, association = ModelAttributeNames.INFO, mode = FetchMode.JOIN),
        @FetchProfile.FetchOverride(entity = CompositeSampleEntity.class, association = ModelAttributeNames.INFO, mode = FetchMode.JOIN)
    })
})
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@MappedSuperclass
public class StudyEntity extends Study {

  @Id
  @Column(name = TableAttributeNames.ID,
      updatable = false, unique = true, nullable = false)
  private String studyId;

  public void setWithStudyEntity(@NonNull StudyEntity studyEntity){
    setStudyId(studyEntity.getStudyId());
    setWithStudy(studyEntity);
  }

  public static StudyEntity createStudyEntity(@NonNull String studyId,
      String name, String organization, String description){
    val study = createStudy(name, organization, description);
    return createStudyEntity(studyId, study);
  }

  public static StudyEntity createStudyEntity(@NonNull String studyId, @NonNull Study study){
    val s = new StudyEntity();
    s.setWithStudy(study);
    s.setStudyId(studyId);
    return s;
  }

}
