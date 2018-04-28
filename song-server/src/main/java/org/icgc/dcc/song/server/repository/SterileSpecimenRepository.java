package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.specimen.impl.SterileSpecimenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SterileSpecimenRepository extends JpaRepository<SterileSpecimenEntity, String> {

  List<SterileSpecimenEntity> findAllByDonorId(String donorId);

}
