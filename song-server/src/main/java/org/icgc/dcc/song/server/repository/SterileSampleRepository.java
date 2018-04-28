package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.sample.impl.SterileSampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SterileSampleRepository extends JpaRepository<SterileSampleEntity, String> {

  List<SterileSampleEntity> findAllBySpecimenId(String specimenId);
  void deleteAllBySpecimenId(String specimenId);

}
