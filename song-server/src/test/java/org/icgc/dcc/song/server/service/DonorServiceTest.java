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
import org.icgc.dcc.song.server.model.entity.donor.CompositeDonorEntity;
import org.icgc.dcc.song.server.model.entity.donor.Donor;
import org.icgc.dcc.song.server.model.entity.donor.DonorEntity;
import org.icgc.dcc.song.server.model.entity.specimen.CompositeSpecimenEntity;
import org.icgc.dcc.song.server.model.entity.specimen.SpecimenEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DONOR_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DONOR_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DONOR_ID_IS_CORRUPTED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.model.enums.Constants.DONOR_GENDER;
import static org.icgc.dcc.song.server.utils.StudyGenerator.createStudyGenerator;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_DONOR_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static org.icgc.dcc.song.server.utils.TestFiles.getInfoName;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
public class DonorServiceTest {

  @Autowired
  DonorService service;
  @Autowired
  SpecimenService specimenService;
  @Autowired
  IdService idService;
  @Autowired
  StudyService studyService;


  private final RandomGenerator randomGenerator = createRandomGenerator(DonorServiceTest.class.getSimpleName());

  @Before
  public void beforeTest(){
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();
  }

  @Test
  public void testReadDonor() {
    // check for data that we know exists in the H2 database already
    val d = service.readWithSpecimens(DEFAULT_DONOR_ID);
    assertThat(d).isNotNull();
    assertThat(d.getDonorId()).isEqualTo(DEFAULT_DONOR_ID);
    assertThat(d.getDonorGender()).isEqualTo("male");
    assertThat(d.getDonorSubmitterId()).isEqualTo("Subject-X23Alpha7");
    assertThat(d.getSpecimens().size()).isEqualTo(2);
    assertThat(getInfoName(d)).isEqualTo("donor1");

    // Just check that each specimen object that we get is the same as the one we get from the
    // specimen service. Let the specimen service tests verify that the contents are right.
    d.getSpecimens().forEach(specimen -> assertThat(specimen.equals(getMatchingSpecimen(specimen))));

  }

  private CompositeSpecimenEntity getMatchingSpecimen(SpecimenEntity specimenEntity) {
    return specimenService.read(specimenEntity.getSpecimenId());
  }

  @Test
  public void testCreateAndDeleteDonor() {
    val json = JsonUtils.mapper().createObjectNode();
    val studyId = "XYZ234";
    json.put("donorId", "");
    json.put("donorSubmitterId", "Subject X21-Alpha");
    json.put("studyId", studyId);
    json.put("donorGender", "unspecified");
    json.put("species", "human");

    CompositeDonorEntity d = JsonUtils.mapper().convertValue(json, CompositeDonorEntity.class);
    assertThat(d.getDonorId()).isNull();

    val status = service.create(d);
    val id = d.getDonorId();

    assertThat(id).startsWith("DO");
    assertThat(status).isEqualTo(id);

    val check = service.readWithSpecimens(id);
    assertThat(d).isEqualToComparingFieldByField(check);

    service.delete(id);
    assertThat(service.isDonorExist(id)).isFalse();

    val status2 = service.create(d);
    assertThat(status2).isEqualTo(id);
    service.delete(id);
    assertThat(service.isDonorExist(id)).isFalse();
  }

  @Test
  public void testUpdateDonor() {
    val studyId = DEFAULT_STUDY_ID;
    val info = JsonUtils.fromSingleQuoted("{'test': 'new json'}");

    val donorCreateRequest = new CompositeDonorEntity();
    donorCreateRequest.setDonorId("");
    donorCreateRequest.setDonorSubmitterId("Triangle-Arrow-S");
    donorCreateRequest.setStudyId(studyId);
    donorCreateRequest.setDonorGender("male");

    val id= service.create(donorCreateRequest);
    assertThat(id).isEqualTo(donorCreateRequest.getDonorId());

    val donorUpdateRequest = new Donor();
    donorUpdateRequest.setDonorSubmitterId("X21-Beta-17");
    donorUpdateRequest.setDonorGender("female");
    donorUpdateRequest.setInfo(info);

    service.update(id, donorUpdateRequest);

    val expectedDonor = new CompositeDonorEntity();
    expectedDonor.setDonorId(id);
    expectedDonor.setStudyId(studyId);
    expectedDonor.setWithDonor(donorUpdateRequest);

    val d3 = service.read(id);
    assertThat(d3).isEqualToComparingFieldByField(expectedDonor);
  }

  @Test
  public void testSave(){
    val studyId = DEFAULT_STUDY_ID;
    val donorSubmitterId = randomGenerator.generateRandomUUIDAsString();
    val donorSaveRequest = new CompositeDonorEntity();
    donorSaveRequest.setDonorId("");
    donorSaveRequest.setDonorSubmitterId(donorSubmitterId);
    donorSaveRequest.setStudyId(studyId);
    donorSaveRequest.setDonorGender("male");
    val donorId = service.save(donorSaveRequest);
    val initialDonor = service.read(donorId);
    assertThat(initialDonor.getDonorGender()).isEqualTo("male");
    assertThat(service.isDonorExist(donorId)).isTrue();

    val donorUpdateRequest = new CompositeDonorEntity();
    donorUpdateRequest.setDonorSubmitterId(donorSubmitterId);
    donorUpdateRequest.setStudyId(studyId);
    donorUpdateRequest.setDonorGender("female");
    val donorId2 = service.save(donorUpdateRequest);
    assertThat(service.isDonorExist(donorId2)).isTrue();
    assertThat(donorId2).isEqualTo(donorId);
    val updateDonor = service.read(donorId2);
    assertThat(updateDonor.getDonorGender()).isEqualTo("female");
  }

  @Test
  public void testSaveStudyDNE(){
    val studyId = DEFAULT_STUDY_ID;
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    assertThat(studyService.isStudyExist(randomStudyId)).isFalse();
    val donorSubmitterId = randomGenerator.generateRandomUUIDAsString();
    val donorCreateRequest = new CompositeDonorEntity();
    donorCreateRequest.setDonorId("");
    donorCreateRequest.setDonorSubmitterId(donorSubmitterId);
    donorCreateRequest.setStudyId(studyId);
    donorCreateRequest.setDonorGender("male");
    val donorId = service.create(donorCreateRequest);
    assertThat(service.isDonorExist(donorId)).isTrue();

    val donorSaveRequest = new DonorEntity();
    donorSaveRequest.setWithDonorEntity(donorCreateRequest);
    donorSaveRequest.setStudyId(randomStudyId);

    assertSongError(() -> service.save(donorSaveRequest), STUDY_ID_DOES_NOT_EXIST);

    val donorSaveRequest2 = new DonorEntity();
    donorSaveRequest2.setDonorId("");
    donorSaveRequest2.setDonorSubmitterId(randomGenerator.generateRandomUUIDAsString());
    donorSaveRequest2.setStudyId(randomStudyId);
    donorSaveRequest2.setDonorGender("female");
    assertSongError(() -> service.save(donorSaveRequest2), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testDeleteByParentId(){
    val studyGenerator = createStudyGenerator(studyService, randomGenerator);
    val randomStudyId = studyGenerator.createRandomStudy();

    val donorCreateRequest = new CompositeDonorEntity();
    donorCreateRequest.setDonorId("");
    donorCreateRequest.setDonorSubmitterId(randomGenerator.generateRandomUUIDAsString());
    donorCreateRequest.setStudyId(randomStudyId);
    donorCreateRequest.setDonorGender("female");

    val id1 = service.create(donorCreateRequest);

    val donorCreateRequest2 = new CompositeDonorEntity();
    donorCreateRequest2.setDonorId("");
    donorCreateRequest2.setDonorSubmitterId(randomGenerator.generateRandomUUIDAsString());
    donorCreateRequest2.setStudyId(randomStudyId);
    donorCreateRequest2.setDonorGender("male");

    val id2 = service.create(donorCreateRequest2);

    val actualDonorWithSpecimens = service.readByParentId(randomStudyId);
    assertThat(actualDonorWithSpecimens).contains(donorCreateRequest, donorCreateRequest2);
    service.deleteByParentId(randomStudyId);
    val emptyDonorWithSpecimens = service.readByParentId(randomStudyId);
    assertThat(emptyDonorWithSpecimens).isEmpty();
  }

  @Test
  public void testDeleteByParentIdStudyDNE(){
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    assertSongError(() -> service.deleteByParentId(randomStudyId), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testDonorCheck(){
    val randomDonorId = randomGenerator.generateRandomUUIDAsString();
    val randomDonorSubmitterId = randomGenerator.generateRandomUUID().toString();
    val randomDonorGender = randomGenerator.randomElement(newArrayList(DONOR_GENDER));
    assertThat(service.isDonorExist(randomDonorId)).isFalse();

    assertSongError(() -> service.checkDonorExists(randomDonorId), DONOR_DOES_NOT_EXIST);

    val donorSaveRequest = new DonorEntity();
    donorSaveRequest.setStudyId(DEFAULT_STUDY_ID);
    donorSaveRequest.setDonorSubmitterId(randomDonorSubmitterId);
    donorSaveRequest.setDonorId(null);
    donorSaveRequest.setDonorGender(randomDonorGender);

    val donorId = service.save(donorSaveRequest);

    assertThat(service.isDonorExist(donorId)).isTrue();

    service.checkDonorExists(donorId);
  }

  @Test
  public void testCreateStudyDNE(){
    val donorCreateRequest = createRandomDonor();
    assertSongError(() -> service.create(donorCreateRequest), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testReadByParentId(){
    val studyGenerator = createStudyGenerator(studyService, randomGenerator);
    val studyId = studyGenerator.createRandomStudy();
    val numDonors = 7;
    val donorIdSet = Sets.<String>newHashSet();
    for (int i =0; i<numDonors; i++){

      val donorCreateRequest = new CompositeDonorEntity();
      donorCreateRequest.setStudyId(studyId);
      donorCreateRequest.setDonorSubmitterId(randomGenerator.generateRandomUUIDAsString());
      donorCreateRequest.setDonorGender(randomGenerator.randomElement(newArrayList(DONOR_GENDER)));

      val donorId = service.create(donorCreateRequest);
      donorIdSet.add(donorId);
    }
    val donors = service.readByParentId(studyId);
    assertThat(donors).hasSize(numDonors);
    assertThat(donors.stream().map(DonorEntity::getDonorId).collect(toSet())).containsAll(donorIdSet);
  }

  @Test
  public void testReadByParentIdStudyDNE(){
    val randomStudyId = randomGenerator.generateRandomUUIDAsString();
    assertSongError(() -> service.readByParentId(randomStudyId), STUDY_ID_DOES_NOT_EXIST);
  }

  @Test
  public void testCreateDonorAlreadyExists(){
    val studyId = DEFAULT_STUDY_ID;
    val randomGender = randomGenerator.randomElement(newArrayList(DONOR_GENDER));
    val randomDonorSubmitterId = randomGenerator.generateRandomUUIDAsString();
    val expectedId = idService.generateDonorId(randomDonorSubmitterId, studyId);

    val donorCreateRequest = new CompositeDonorEntity();
    donorCreateRequest.setStudyId(studyId);
    donorCreateRequest.setDonorSubmitterId(randomDonorSubmitterId);
    donorCreateRequest.setDonorGender(randomGender);
    donorCreateRequest.setInfo("someKey", "someValue");
    val donorId = service.create(donorCreateRequest);
    assertThat(donorId).isEqualTo(expectedId);

    donorCreateRequest.setDonorId("DO123");
    assertSongError(() -> service.create(donorCreateRequest), DONOR_ID_IS_CORRUPTED);

    donorCreateRequest.setDonorId(expectedId);
    assertSongError(() -> service.create(donorCreateRequest), DONOR_ALREADY_EXISTS);

    donorCreateRequest.setDonorId("");
    assertSongError(() -> service.create(donorCreateRequest), DONOR_ALREADY_EXISTS);

    donorCreateRequest.setDonorId(null);
    assertSongError(() -> service.create(donorCreateRequest), DONOR_ALREADY_EXISTS);
  }

  @Test
  public void testUpdateDonorDNE(){
    val randomDonor = createRandomDonor();
    assertSongError(() ->  service.update(randomDonor.getDonorId(), randomDonor), DONOR_DOES_NOT_EXIST);
  }

  @Test
  public void testDeleteDonorDNE(){
    val randomDonorId =  randomGenerator.generateRandomUUIDAsString();
    assertSongError(() -> service.delete(randomDonorId), DONOR_DOES_NOT_EXIST);
    assertSongError(() -> service.delete(newArrayList(randomDonorId, DEFAULT_DONOR_ID)),
        DONOR_DOES_NOT_EXIST);
  }

  @Test
  public void testReadDonorDNE(){
    val randomDonorId =  randomGenerator.generateRandomUUIDAsString();
    assertThat(service.isDonorExist(randomDonorId)).isFalse();
    assertSongError(() -> service.read(randomDonorId), DONOR_DOES_NOT_EXIST);
    assertSongError(() -> service.readWithSpecimens(randomDonorId), DONOR_DOES_NOT_EXIST);
  }

  private CompositeDonorEntity createRandomDonor(){
    val donor = new CompositeDonorEntity();
    donor.setDonorId(randomGenerator.generateRandomUUIDAsString());
    donor.setStudyId(randomGenerator.generateRandomUUIDAsString());
    donor.setDonorSubmitterId(randomGenerator.generateRandomUUIDAsString());
    donor.setDonorGender(randomGenerator.randomElement(newArrayList(DONOR_GENDER)));
    return donor;
  }

}
