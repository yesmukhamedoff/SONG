package org.icgc.dcc.song.server.config;

import lombok.val;
import org.icgc.dcc.song.server.model.entity.study.impl.FullStudyEntity;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.icgc.dcc.song.server.repository.FetchPlanner;
import org.icgc.dcc.song.server.utils.graph.SimpleGraph;
import org.icgc.dcc.song.server.utils.graph.SimpleNodePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

import static org.icgc.dcc.song.server.repository.FetchPlanner.createFetchPlanner;

@Configuration
public class RepositoryConfig {

  @Autowired
  private EntityManager entityManager;


  @Bean
  public FetchPlanner<FullStudyEntity, String> studyWithDonorsFetchPlan(){
    val nodePath = SimpleNodePath.<String>builder()
        .node(ModelAttributeNames.DONORS)
        .node(ModelAttributeNames.SPECIMENS)
        .node(ModelAttributeNames.SAMPLES)
        .build();
    val simpleGraph  = SimpleGraph.<String>builder()
        .graphPath(nodePath)
        .build();
    val graph = simpleGraph.mergePaths();
    return createFetchPlanner(FullStudyEntity.class, entityManager, graph,  ModelAttributeNames.DONORS);
  }

  @Bean
  public FetchPlanner<FullStudyEntity, String> studyWithDonorsInfoFetchPlan(){
    val samplePath = SimpleNodePath.<String>builder()
        .node(ModelAttributeNames.DONORS)
        .node(ModelAttributeNames.SPECIMENS)
        .node(ModelAttributeNames.SAMPLES)
        .node(ModelAttributeNames.INFO)
        .build();
    val specimenPath = SimpleNodePath.<String>builder()
        .node(ModelAttributeNames.DONORS)
        .node(ModelAttributeNames.SPECIMENS)
        .node(ModelAttributeNames.INFO)
        .build();
    val donorPath = SimpleNodePath.<String>builder()
        .node(ModelAttributeNames.DONORS)
        .node(ModelAttributeNames.INFO)
        .build();
    val studyPath = SimpleNodePath.<String>builder()
        .node(ModelAttributeNames.INFO)
        .build();
    val simpleGraph  = SimpleGraph.<String>builder()
        .graphPath(samplePath)
        .graphPath(specimenPath)
        .graphPath(donorPath)
        .graphPath(studyPath)
        .build();
    val graph = simpleGraph.mergePaths();
    return createFetchPlanner(FullStudyEntity.class, entityManager, graph,  ModelAttributeNames.DONORS);
  }

}
