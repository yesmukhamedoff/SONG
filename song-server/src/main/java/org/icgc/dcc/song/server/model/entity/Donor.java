package org.icgc.dcc.song.server.model.entity;

public interface Donor {

  String getDonorId();
  String getStudyId();
  String getDonorSubmitterId();
  String getDonorGender();
  void setDonorId(String donorId);
  void setDonorSubmitterId(String donorSubmitterId);
  void setDonorGender(String donorGender);

}
