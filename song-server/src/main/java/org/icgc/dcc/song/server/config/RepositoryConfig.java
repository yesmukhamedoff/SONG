package org.icgc.dcc.song.server.config;

import org.icgc.dcc.song.server.model.entity.composites.CompositeDonor;
import org.icgc.dcc.song.server.model.entity.composites.CompositeStudy;
import org.icgc.dcc.song.server.model.entity.single.SingleDonor;
import org.icgc.dcc.song.server.model.entity.single.SingleStudy;
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
  public SpecialRepo<SingleStudy, String> singleStudyRepository(){
    return new SpecialRepo<>(SingleStudy.class, entityManager);
  }

  @Bean
  public SpecialRepo<CompositeStudy, String> compositeStudyRepository(){
    return new SpecialRepo<>(CompositeStudy.class, entityManager);
  }

  @Bean
  public SpecialRepo<SingleDonor, String> singleDonorRepository(){
    return new SpecialRepo<>(SingleDonor.class, entityManager);
  }

  @Bean
  public SpecialRepo<CompositeDonor, String> compositeDonorRepository(){
    return new SpecialRepo<>(CompositeDonor.class, entityManager);
  }

}
