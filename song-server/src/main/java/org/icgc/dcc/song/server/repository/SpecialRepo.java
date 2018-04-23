package org.icgc.dcc.song.server.repository;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.EntityManager;

@NoRepositoryBean
public class SpecialRepo<T, ID> extends SimpleJpaRepository<T, ID> {

  private final EntityManager entityManager;

  public SpecialRepo(Class<T> domainClass, EntityManager em) {
    super(domainClass, em);
    this.entityManager = em;
  }

}
