package org.icgc.dcc.song.server.repository;

import com.google.common.collect.Maps;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.specimen.impl.FullSpecimenEntity;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.EntityManager;
import java.util.Map;
import java.util.Optional;

@NoRepositoryBean
public class SpecimenRepository extends SimpleJpaRepository<FullSpecimenEntity, String>{
  private static final String FETCHGRAPH_PROPERTY_KEY = "javax.persistence.fetchgraph";

  private final EntityManager em;

  public SpecimenRepository(EntityManager em) {
    super(FullSpecimenEntity.class, em);
    this.em = em;
  }

  public Optional<FullSpecimenEntity> findSpecimenWithSamples(String id){
    val hints = createFetchHints(em, FullSpecimenEntity.SPECIMEN_WITH_SAMPLES);
    return Optional.ofNullable(em.find(FullSpecimenEntity.class, id, hints));
  }

  public static <T> Map<String, Object> createFetchHints(EntityManager em, String entityGraphName){
    val g = em.getEntityGraph(FullSpecimenEntity.SPECIMEN_WITH_SAMPLES);
    val map = Maps.<String, Object>newHashMap();
    map.put(FETCHGRAPH_PROPERTY_KEY, g);
    return map;
  }

}
