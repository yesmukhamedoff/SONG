package org.icgc.dcc.song.server.repository;

import lombok.val;
import org.icgc.dcc.song.server.model.entity.study.impl.CompositeStudyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Optional;

import static org.icgc.dcc.song.server.model.enums.StudyEntityMaps.STUDY_WITH_SAMPLES_PATH;

public class StudyRepo extends SimpleJpaRepository<CompositeStudyEntity, String> implements JpaRepository<CompositeStudyEntity, String>{

  private final EntityManager em;

  public StudyRepo(EntityManager em) {
    super(CompositeStudyEntity.class, em);
    this.em = em;
  }

  public Optional<CompositeStudyEntity> readStudyWithSamples(String studyId, boolean includeInfo){
    val graph = em.createEntityGraph(STUDY_WITH_SAMPLES_PATH);
    val hints = new HashMap();
    hints.put("javax.persistence.fetchgraph", graph);
    val s = em.find(CompositeStudyEntity.class, studyId, hints);
    em.detach(s);
    return Optional.ofNullable(s);
  }
}
