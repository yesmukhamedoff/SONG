/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.icgc.dcc.song.server.service;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.entity.study.StudyEntityMaps;
import org.icgc.dcc.song.server.model.entity.study.impl.FullStudyEntity;
import org.icgc.dcc.song.server.model.entity.study.impl.SterileStudyEntity;
import org.icgc.dcc.song.server.repository.StudyRepo;
import org.icgc.dcc.song.server.repository.StudyRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Subgraph;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.model.entity.study.impl.SterileStudyEntity.createSterileStudy;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static org.icgc.dcc.song.server.utils.TestFiles.getInfoName;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
public class StudyServiceTest {

  @Autowired
  StudyService service;

  private final RandomGenerator randomGenerator = createRandomGenerator(StudyServiceTest.class.getSimpleName());

  @Test
  public void testReadStudy() {
    // check for data that we know exists in the database already
    val study = service.read(DEFAULT_STUDY_ID);
    assertThat(study).isNotNull();
    assertThat(study.getStudyId()).isEqualTo("ABC123");
    assertThat(study.getName()).isEqualTo("X1-CA");
    assertThat(study.getDescription()).isEqualTo("A fictional study");
    assertThat(study.getOrganization()).isEqualTo("Sample Data Research Institute");
    assertThat(getInfoName(study)).isEqualTo("study1");
  }

  @Test
  public void testSave(){
    val studyId = randomGenerator.generateRandomUUID().toString();
    val organization = randomGenerator.generateRandomUUID().toString();
    val name  = randomGenerator.generateRandomAsciiString(10);
    val description = randomGenerator.generateRandomUUID().toString();
    val sterileStudy = createSterileStudy(studyId, name, organization, description);
    assertThat(service.isStudyExist(studyId)).isFalse();
    service.create(sterileStudy);
    val readStudy = service.read(studyId);
    assertThat(readStudy).isEqualToComparingFieldByFieldRecursively(sterileStudy);
  }

  @Test
  public void testFindAllStudies(){
    val studyIds = service.findAllStudies();
    assertThat(studyIds).contains(DEFAULT_STUDY_ID, "XYZ234");
    val study = SterileStudyEntity.createSterileStudy(
        randomGenerator.generateRandomUUIDAsString(),
        randomGenerator.generateRandomUUIDAsString(),
        randomGenerator.generateRandomUUIDAsString(),
        randomGenerator.generateRandomUUIDAsString()
    );

    service.create(study);
    val studyIds2 = service.findAllStudies();
    assertThat(studyIds2).contains(DEFAULT_STUDY_ID, "XYZ234", study.getStudyId());
  }

  @Test
  public void testDuplicateSaveStudyError(){
    val existentStudyId = DEFAULT_STUDY_ID;
    assertThat(service.isStudyExist(existentStudyId)).isTrue();
    val study = service.read(existentStudyId);
    assertSongError(() -> service.create(study), STUDY_ALREADY_EXISTS);
  }

  @Test
  public void testReadStudyError(){
    val nonExistentStudyId = genStudyId();
    assertSongError(() -> service.read(nonExistentStudyId), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testStudyCheck(){
    val existentStudyId = DEFAULT_STUDY_ID;
    assertThat(service.isStudyExist(existentStudyId)).isTrue();
    val nonExistentStudyId = genStudyId();
    assertThat(service.isStudyExist(nonExistentStudyId)).isFalse();
  }

  @Autowired EntityManagerFactory entityManagerFactory;

  @Test
  public void testRob(){
    val em = entityManagerFactory.createEntityManager();
    val graph = em.createEntityGraph(FullStudyEntity.class);
    Subgraph sub = graph.addSubgraph("donors");
    sub = sub.addSubgraph("specimens");
    sub = sub.addSubgraph("samples");


    val props = Maps.<String, Object>newHashMap();
    props.put("javax.persistence.fetchgraph", graph);
    val s = em.find(FullStudyEntity.class, "ABC123", props);
    em.close();
    val a = s.getAnalyses();
    val d = s.getDonors();
    val f = s.getFiles();
    val r = s.getUploads();
    log.info("sdf");

  }

  @Test
  public void testRob2() {
    val em = entityManagerFactory.createEntityManager();
    val graph = em.createEntityGraph(StudyEntityMaps.STUDY_WITH_SAMPLES_PATH);
    val hints = new HashMap();
    hints.put("javax.persistence.fetchgraph", graph);
    val s = em.find(FullStudyEntity.class, DEFAULT_STUDY_ID, hints);
    em.detach(s);
    log.info("sdf");
  }

  @Autowired StudyRepository studyRepository;
  @Autowired StudyRepo studyRepo;

  @Test
  public void testR(){
    val result = studyRepo.readStudyWithSamples(DEFAULT_STUDY_ID, false);
    log.info("sdf");
  }

  private String genStudyId(){
    return randomGenerator.generateRandomAsciiString(15);
  }

}
