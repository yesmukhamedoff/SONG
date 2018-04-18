package org.icgc.dcc.song.server.config;

import lombok.NoArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

@NoArgsConstructor
@Configuration
public class HibernateConfig {

  @Autowired
  EntityManagerFactory entityManagerFactory;

  public SessionFactory getSessionFactory() {
    if (entityManagerFactory.unwrap(SessionFactory.class) == null) {
      throw new NullPointerException("factory is not a hibernate factory");
    }
    return entityManagerFactory.unwrap(SessionFactory.class);
  }

  @Bean
  public EntityManager entityManager(){
    return entityManagerFactory.createEntityManager();
  }

}
