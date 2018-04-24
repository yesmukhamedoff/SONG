package org.icgc.dcc.song.server.model.entity.study.projections;

import org.icgc.dcc.song.server.model.entity.donor.Donor;
import org.icgc.dcc.song.server.model.entity.study.StudyEntity;

import java.util.List;

public interface StudyWithDonors extends StudyEntity {

  List<Donor> getDonors();

}
