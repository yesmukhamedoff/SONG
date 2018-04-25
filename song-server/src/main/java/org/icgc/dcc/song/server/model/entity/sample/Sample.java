package org.icgc.dcc.song.server.model.entity.sample;

import lombok.NonNull;

public interface Sample {

  String getSampleSubmitterId();

  void setSampleSubmitterId(String sampleSubmitterId);

  String getSampleType();

  void setSampleType(String sampleType);

  default void setWithSample(@NonNull Sample sample){
    setSampleSubmitterId(sample.getSampleSubmitterId());
    setSampleType(sample.getSampleType());
  }

}
