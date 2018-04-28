package org.icgc.dcc.song.server.model.entity.sample;

public interface SampleEntity extends Sample {

  String getSpecimenId();

  String getSampleId();

  void setSampleId(String sampleId);

}
