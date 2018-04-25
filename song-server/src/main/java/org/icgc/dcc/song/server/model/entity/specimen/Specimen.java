package org.icgc.dcc.song.server.model.entity.specimen;

import lombok.NonNull;

public interface Specimen {

  void setSpecimenClass(String specimenClass);

  void setSpecimenType(String type);

  String getSpecimenSubmitterId();

  String getSpecimenClass();

  String getSpecimenType();

  void setSpecimenSubmitterId(String specimenSubmitterId);

  default void setWithSpecimen(@NonNull Specimen specimen){
    setSpecimenClass(specimen.getSpecimenClass());
    setSpecimenSubmitterId(specimen.getSpecimenSubmitterId());
    setSpecimenType(specimen.getSpecimenType());
  }

}
