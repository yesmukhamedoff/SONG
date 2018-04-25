package org.icgc.dcc.song.server.model.entity.specimen;

import lombok.NonNull;

public interface SpecimenEntity extends Specimen {

  String getDonorId();

  String getSpecimenId();

  void setSpecimenId(String specimenId);

  default void setWith(@NonNull String id, @NonNull Specimen specimen){
    setWithSpecimen(specimen);
    setSpecimenId(id);
  }

  default void setWithSpecimenEntity(@NonNull SpecimenEntity specimenEntity){
    setWith(specimenEntity.getSpecimenId(), specimenEntity);
  }

}
