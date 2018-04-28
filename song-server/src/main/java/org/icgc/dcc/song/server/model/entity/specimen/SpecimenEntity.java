package org.icgc.dcc.song.server.model.entity.specimen;

public interface SpecimenEntity extends Specimen {

  String getDonorId();

  String getSpecimenId();

  void setSpecimenId(String specimenId);


}
