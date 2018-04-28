package org.icgc.dcc.song.server.model.entity.donor;

public interface DonorEntity extends Donor {

  String getDonorId();

  String getStudyId();

  void setDonorId(String donorId);

}
