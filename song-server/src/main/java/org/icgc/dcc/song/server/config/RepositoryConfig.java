package org.icgc.dcc.song.server.config;

import org.icgc.dcc.song.server.model.entity.donor.Donor;
import org.icgc.dcc.song.server.model.entity.donor.SterileDonor;
import org.icgc.dcc.song.server.model.entity.study.SterileStudy;
import org.icgc.dcc.song.server.model.entity.study.Study;
import org.icgc.dcc.song.server.repository.SpecialRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

@Configuration
public class RepositoryConfig {

  @Autowired
  private EntityManager entityManager;

  @Bean
  public SpecialRepo<SterileStudy, String> singleStudyRepository(){
    return new SpecialRepo<>(SterileStudy.class, entityManager);
  }

  @Bean
  public SpecialRepo<Study, String> compositeStudyRepository(){
    return new SpecialRepo<>(Study.class, entityManager);
  }

  @Bean
  public SpecialRepo<SterileDonor, String> singleDonorRepository(){
    return new SpecialRepo<>(SterileDonor.class, entityManager);
  }

  @Bean
  public SpecialRepo<Donor, String> compositeDonorRepository(){
    return new SpecialRepo<>(Donor.class, entityManager);
  }


}
