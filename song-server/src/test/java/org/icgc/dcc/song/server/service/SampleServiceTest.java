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

import com.google.common.collect.Sets;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.entity.sample.CompositeSampleEntity;
import org.icgc.dcc.song.server.model.entity.sample.SampleEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_ID_IS_CORRUPTED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.model.entity.sample.CompositeSampleEntity.buildSampleCreateRequest;
import static org.icgc.dcc.song.server.model.entity.sample.Sample.createSample;
import static org.icgc.dcc.song.server.model.entity.sample.SampleEntity.createSampleEntity;
import static org.icgc.dcc.song.server.model.entity.specimen.CompositeSpecimenEntity.buildSpecimenCreateRequest;
import static org.icgc.dcc.song.server.model.entity.specimen.Specimen.createSpecimen;
import static org.icgc.dcc.song.server.model.enums.Constants.SAMPLE_TYPE;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_TYPE;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_DONOR_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_SAMPLE_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_SPECIMEN_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static org.icgc.dcc.song.server.utils.TestFiles.getInfoName;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
//Note: since some tests are inserting and deleting, need transaction management
// to roll back transactions when a test finishes
@Transactional
public class SampleServiceTest {

  @Autowired
  SampleService sampleService;
  @Autowired
  StudyService studyService;

  @Autowired
  DonorService donorService;

  @Autowired
  SpecimenService specimenService;

  private final RandomGenerator randomGenerator = createRandomGenerator(SampleServiceTest.class.getSimpleName());

  @Before
  public void beforeTest(){
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();
  }

  @Test
  public void testReadSample() {
    val id = "SA1";
    val sample = sampleService.read(id);
    assertThat(sample.getSampleId()).isEqualTo(id);
    assertThat(sample.getSampleSubmitterId()).isEqualTo("T285-G7-A5");
    assertThat(sample.getSampleType()).isEqualTo("DNA");
    assertThat(getInfoName(sample)).isEqualTo("sample1");
  }

  @Test
  public void testCreateAndDeleteSample() {
    val specimenId = "SP2";
    val metadata = JsonUtils.fromSingleQuoted("{'ageCategory': 3, 'species': 'human'}");
    val sampleData = createSample("101-IP-A","Amplified DNA");
    sampleData.setInfo(metadata);

    val s = new CompositeSampleEntity();
    s.setWithSample(sampleData);

    val status = sampleService.create(DEFAULT_STUDY_ID, s);
    val id = s.getSampleId();

    assertThat(sampleService.isSampleExist(id)).isTrue();

    assertThat(id).startsWith("SA");
    assertThat(status).isEqualTo(id);

    val check = sampleService.read(id);
    assertThat(check).isEqualToComparingFieldByField(s);

    sampleService.delete(newArrayList(id));
    assertThat(sampleService.isSampleExist(id)).isFalse();
  }

  @Test
  public void testUpdateSample() {

    val specimenId = "SP2";
    val sampleData = createSample("102-CBP-A", "RNA");
    val sampleCreateRequest = buildSampleCreateRequest(specimenId, sampleData);

    val id = sampleService.create(DEFAULT_STUDY_ID, sampleCreateRequest);

    val metadata = JsonUtils.fromSingleQuoted("{'species': 'Canadian Beaver'}");
    val sampleUpdateRequest = createSample("Sample 102", "FFPE RNA");
    sampleUpdateRequest.setInfo(metadata);

    sampleService.update(id, sampleUpdateRequest);

    val expectedSampleEntity = new CompositeSampleEntity();
    expectedSampleEntity.setWithSampleEntity(createSampleEntity(id, specimenId, sampleUpdateRequest));

    val actualSampleEntity = sampleService.read(id);
    assertThat(actualSampleEntity).isEqualTo(expectedSampleEntity);
  }

  @Test
  public void testCreateStudyDNE(){
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    val sample = new CompositeSampleEntity();
    assertSongError(() -> sampleService.create(randomStudyId, sample), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testCreateCorruptedAndAlreadyExists(){
    val specimenId = DEFAULT_SPECIMEN_ID;
    val existingStudyId = DEFAULT_STUDY_ID;
    val specimenParent = specimenService.read(specimenId);

    val sampleData = createSample(randomGenerator.generateRandomUUIDAsString(),
        randomGenerator.randomElement(newArrayList(SAMPLE_TYPE)));
    val sampleCreateRequest = buildSampleCreateRequest(specimenId, sampleData);


    // Create a sample
    val sampleId = sampleService.create(existingStudyId, sampleCreateRequest);
    assertThat(sampleService.isSampleExist(sampleId)).isTrue();

    // Try to create the sample again, and assert that the right exception is thrown
    assertSongError(() -> sampleService.create(existingStudyId, sampleCreateRequest), SAMPLE_ALREADY_EXISTS);

    // 'Accidentally' set the sampleId to something not generated by the idService, and try to create. Should
    // detected the corrupted id field, indicating user might have accidentally set the id, thinking it would be
    // persisted
    val sample2 = new CompositeSampleEntity();
    sample2.setWithSampleEntity(
        createSampleEntity(
            randomGenerator.generateRandomUUIDAsString(),
            specimenId,
            randomGenerator.generateRandomUUIDAsString(),
            randomGenerator.randomElement(newArrayList(SAMPLE_TYPE))));


    assertThat(sampleService.isSampleExist(sample2.getSampleId())).isFalse();
    assertSongError(() -> sampleService.create(existingStudyId, sample2), SAMPLE_ID_IS_CORRUPTED);
  }

  @Test
  public void testSampleExists(){
    val existingSampleId= DEFAULT_SAMPLE_ID;
    assertThat(sampleService.isSampleExist(existingSampleId)).isTrue();
    sampleService.checkSampleExists(existingSampleId);
    val nonExistingSampleId = randomGenerator.generateRandomUUIDAsString();
    assertThat(sampleService.isSampleExist(nonExistingSampleId)).isFalse();
    sampleService.checkSampleExists(existingSampleId);
    sampleService.checkSampleDoesNotExist(nonExistingSampleId);

    assertSongError(() -> sampleService.checkSampleExists(nonExistingSampleId), SAMPLE_DOES_NOT_EXIST);
    assertSongError(() -> sampleService.checkSampleDoesNotExist(existingSampleId), SAMPLE_ALREADY_EXISTS);
  }

  @Test
  public void testReadSampleDNE(){
    val randomSampleId = randomGenerator.generateRandomUUIDAsString();
    assertThat(sampleService.isSampleExist(randomSampleId)).isFalse();
    assertSongError(() -> sampleService.read(randomSampleId), SAMPLE_DOES_NOT_EXIST);
  }

  @Test
  public void testReadAndDeleteByParentId(){
    val studyId = DEFAULT_STUDY_ID;
    val donorId = DEFAULT_DONOR_ID;

    val specimenCreateRequest = buildSpecimenCreateRequest(donorId,
        createSpecimen(
            randomGenerator.generateRandomUUIDAsString(),
            randomGenerator.randomElement(newArrayList(SPECIMEN_TYPE)),
            randomGenerator.randomElement(newArrayList(SPECIMEN_TYPE))
        ));

    // Create specimen
    val specimenId = specimenService.create(studyId, specimenCreateRequest);

    // Create samples
    val numSamples = 5;
    val expectedSampleIds = Sets.<String>newHashSet();
    for (int i =0; i< numSamples; i++){
      val sampleCreateRequest = buildSampleCreateRequest(specimenId,
          createSample(
              randomGenerator.randomElement(newArrayList(SAMPLE_TYPE)),
              randomGenerator.generateRandomUUIDAsString()));

      val sampleId = sampleService.create(studyId, sampleCreateRequest);
      expectedSampleIds.add(sampleId);
    }

    // Read the samples by parent Id (specimenId)
    val actualSamples = sampleService.readByParentId(specimenId);
    assertThat(actualSamples).hasSize(numSamples);
    assertThat(actualSamples.stream().map(SampleEntity::getSampleId).collect(toSet())).containsAll(expectedSampleIds);

    // Assert that reading by a non-existent specimenId returns something empty
    val randomSpecimenId = randomGenerator.generateRandomUUIDAsString();
    assertThat(specimenService.isSpecimenExist(randomSpecimenId)).isFalse();
    val emptySampleList = sampleService.readByParentId(randomSpecimenId);
    assertThat(emptySampleList).isEmpty();

    // Delete by parent id
    sampleService.deleteByParentId(specimenId);
    val emptySampleList2 = sampleService.readByParentId(specimenId);
    assertThat(emptySampleList2).isEmpty();
  }

  @Test
  public void testDeleteSampleDNE(){
    val randomSpecimenId = randomGenerator.generateRandomUUIDAsString();
    assertSongError(() -> sampleService.delete(randomSpecimenId), SAMPLE_DOES_NOT_EXIST);
    assertSongError(() -> sampleService.delete(newArrayList(randomSpecimenId)), SAMPLE_DOES_NOT_EXIST);
  }

  @Test
  public void testUpdateSpecimenDNE(){
    val randomSampleId = randomGenerator.generateRandomUUIDAsString();
    val sampleEntity = createSampleEntity(randomSampleId,
        DEFAULT_SPECIMEN_ID,
        randomGenerator.generateRandomUUIDAsString(),
        randomGenerator.randomElement(newArrayList(SAMPLE_TYPE)));

    assertSongError(() -> sampleService.update(sampleEntity.getSampleId(), sampleEntity), SAMPLE_DOES_NOT_EXIST);
  }

}
