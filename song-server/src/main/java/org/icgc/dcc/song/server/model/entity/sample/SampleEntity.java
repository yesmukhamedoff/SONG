package org.icgc.dcc.song.server.model.entity.sample;

import lombok.NonNull;

public interface SampleEntity extends Sample {

  String getSpecimenId();

  String getSampleId();

  void setSampleId(String sampleId);

  default void setWith(@NonNull String id, @NonNull Sample sample){
    setWithSample(sample);
    setSampleId(id);
  }

  default void setWithSampleEntity(@NonNull SampleEntity sampleEntity){
    setWith(sampleEntity.getSampleId(), sampleEntity);
  }

}
