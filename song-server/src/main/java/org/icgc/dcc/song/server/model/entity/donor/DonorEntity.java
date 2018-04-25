package org.icgc.dcc.song.server.model.entity.donor;

import lombok.NonNull;

public interface DonorEntity extends Donor {

  String getDonorId();

  String getStudyId();

  void setDonorId(String donorId);

  default void setWith(@NonNull String id, @NonNull Donor donor){
    setWithDonor(donor);
    setDonorId(id);
  }

  default void setWithDonorEntity(@NonNull DonorEntity donorEntity){
    setWith(donorEntity.getDonorId(), donorEntity);
  }

}
