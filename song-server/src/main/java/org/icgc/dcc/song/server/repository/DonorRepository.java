package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.donor.impl.FullDonorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonorRepository extends JpaRepository<FullDonorEntity, String>{

}
