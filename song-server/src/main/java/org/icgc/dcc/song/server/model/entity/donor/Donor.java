package org.icgc.dcc.song.server.model.entity.donor;

import lombok.NonNull;

public interface Donor {

  String getDonorSubmitterId();

  String getDonorGender();

  void setDonorSubmitterId(String donorSubmitterId);

  void setDonorGender(String gender);

  default void setWithDonor(@NonNull Donor donor){
    setDonorGender(donor.getDonorGender());
    setDonorSubmitterId(donor.getDonorSubmitterId());
  }

}
