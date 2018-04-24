package org.icgc.dcc.song.server.model.entity.study;

import lombok.NoArgsConstructor;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;

import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@NoArgsConstructor
public class StudyEntityMaps {
  public static final String STUDY_WITH_SAMPLES_PATH = "studyWithSamples";
  public static final String DONOR_WITH_SAMPLES_PATH = "donorWithSamples";
  public static final String SPECIMEN_WITH_SAMPLES_PATH = "specimenWithSamples";

  @NamedEntityGraph(name = STUDY_WITH_SAMPLES_PATH,
      attributeNodes =
          @NamedAttributeNode(value = ModelAttributeNames.DONORS, subgraph = DONOR_WITH_SAMPLES_PATH),
      subgraphs = {
          @NamedSubgraph(name = DONOR_WITH_SAMPLES_PATH,
              attributeNodes =
                  @NamedAttributeNode(value = ModelAttributeNames.SPECIMENS, subgraph = SPECIMEN_WITH_SAMPLES_PATH)),
          @NamedSubgraph(name = SPECIMEN_WITH_SAMPLES_PATH,
              attributeNodes =
                  @NamedAttributeNode(value = ModelAttributeNames.SAMPLES))
      }
  )
  @Target({ ElementType.TYPE})
  @Retention(value = RUNTIME)
  public @interface LoadStudyWithSamples {}

}
