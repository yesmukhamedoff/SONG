package org.icgc.dcc.song.server.repository;

import org.icgc.dcc.song.server.model.entity.donor.Donor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonorRepository extends JpaRepository<Donor, String>{

}
