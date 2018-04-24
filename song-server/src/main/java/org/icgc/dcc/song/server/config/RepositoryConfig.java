package org.icgc.dcc.song.server.config;

import org.icgc.dcc.song.server.model.entity.donor.Donor;
import org.icgc.dcc.song.server.model.entity.donor.SterileDonor;
import org.icgc.dcc.song.server.model.entity.study.impl.SterileStudyEntity;
import org.icgc.dcc.song.server.model.entity.study.impl.FullStudyEntity;
import org.icgc.dcc.song.server.repository.SpecialRepo;
import org.icgc.dcc.song.server.repository.StudyRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

@Configuration
public class RepositoryConfig {

  @Autowired
  private EntityManager entityManager;

  @Bean
  public SpecialRepo<SterileStudyEntity, String> singleStudyRepository(){
    return new SpecialRepo<>(SterileStudyEntity.class, entityManager);
  }

  @Bean
  public SpecialRepo<FullStudyEntity, String> compositeStudyRepository(){
    return new SpecialRepo<>(FullStudyEntity.class, entityManager);
  }

  @Bean
  public SpecialRepo<SterileDonor, String> singleDonorRepository(){
    return new SpecialRepo<>(SterileDonor.class, entityManager);
  }

  @Bean
  public SpecialRepo<Donor, String> compositeDonorRepository(){
    return new SpecialRepo<>(Donor.class, entityManager);
  }

  @Bean
  public StudyRepo studyRepo(){
    return new StudyRepo(entityManager);
  }

}
