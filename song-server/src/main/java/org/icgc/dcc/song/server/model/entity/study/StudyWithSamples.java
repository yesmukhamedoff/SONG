package org.icgc.dcc.song.server.model.entity.study;

import java.util.List;

public interface StudyWithSamples extends StudyEntity {

  List<DonorWithSamples> getDonors();

  interface  DonorWithSamples {
    String getDonorId();
    String getDonorSubmitterId();
    String getDonorGender();
    List<SpecimenWithSamples> getSpecimens();
  }

  interface SpecimenWithSamples {
    String getSpecimenId();
    String getSpecimenSubmitterId();
    String getSpecimenClass();
    String getSpecimenType();
    List<ISample> getSamples();
  }

  interface ISample {
        String getSampleSubmitterId();
        String getSampleType();
  }

}
