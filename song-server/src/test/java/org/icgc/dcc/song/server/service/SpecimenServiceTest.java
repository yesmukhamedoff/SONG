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
import com.google.common.collect.Sets;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.model.entity.donor.CompositeDonorEntity;
import org.icgc.dcc.song.server.model.entity.sample.CompositeSampleEntity;
import org.icgc.dcc.song.server.model.entity.sample.SampleEntity;
import org.icgc.dcc.song.server.model.entity.specimen.CompositeSpecimenEntity;
import org.icgc.dcc.song.server.model.entity.specimen.Specimen;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_ID_IS_CORRUPTED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.testing.SongErrorAssertions.assertSongError;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.server.model.enums.Constants.DONOR_GENDER;
import static org.icgc.dcc.song.server.model.enums.Constants.SAMPLE_TYPE;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_CLASS;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_TYPE;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_DONOR_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_SPECIMEN_ID;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static org.icgc.dcc.song.server.utils.TestFiles.getInfoName;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@ActiveProfiles("dev")
public class SpecimenServiceTest {

    @Autowired
    SpecimenService specimenService;
    @Autowired
    SampleService sampleService;
    @Autowired
    StudyService studyService;
    @Autowired
    DonorService donorService;

    private final RandomGenerator randomGenerator = createRandomGenerator(SpecimenServiceTest.class.getSimpleName());

    @Before
    public void beforeTest(){
        assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();
    }

    @Test
    public void testReadSpecimen() {
        // find existing specimen in the database
        val id = "SP1";
        val s = specimenService.read(id);
        assertThat(s.getSpecimenId()).isEqualTo(id);
        assertThat(s.getSpecimenSubmitterId()).isEqualTo("Tissue-Culture 284 Gamma 3");
        assertThat(s.getSpecimenClass()).isEqualTo("Tumour");
        assertThat(s.getSpecimenType()).isEqualTo("Recurrent tumour - solid tissue");
        assertThat(getInfoName(s)).isEqualTo("specimen1");
    }

    @Test
    public void testReadWithSamples() {
        // see if we can read a composite object successfully
        val submitterId = randomGenerator.generateRandomUUIDAsString();
        val studyId = DEFAULT_STUDY_ID;
        val donorId = DEFAULT_DONOR_ID;
        val specimenClass  = randomGenerator.randomElement(newArrayList(SPECIMEN_CLASS));
        val specimenType = randomGenerator.randomElement(newArrayList(SPECIMEN_TYPE));

        val randomSpecimenCreateRequest = new CompositeSpecimenEntity();
        randomSpecimenCreateRequest.setSpecimenId(null);
        randomSpecimenCreateRequest.setDonorId(donorId);
        randomSpecimenCreateRequest.setSpecimenSubmitterId(submitterId);
        randomSpecimenCreateRequest.setSpecimenClass(specimenClass);
        randomSpecimenCreateRequest.setSpecimenType(specimenType);
        randomSpecimenCreateRequest.setInfo("name", "specimen1");

        val specimenId =  specimenService.create(DEFAULT_STUDY_ID, randomSpecimenCreateRequest);

        val sampleInput1CreateRequest = new CompositeSampleEntity();
        sampleInput1CreateRequest.setSampleId(null);
        sampleInput1CreateRequest.setSpecimenId(specimenId);
        sampleInput1CreateRequest.setSampleSubmitterId(randomGenerator.generateRandomUUIDAsString());
        sampleInput1CreateRequest.setSampleType( randomGenerator.randomElement(newArrayList(SAMPLE_TYPE)) );

        val sampleInput2CreateRequest = new CompositeSampleEntity();
        sampleInput2CreateRequest.setSampleId(null);
        sampleInput2CreateRequest.setSpecimenId(specimenId);
        sampleInput2CreateRequest.setSampleSubmitterId(randomGenerator.generateRandomUUIDAsString());
        sampleInput2CreateRequest.setSampleType( randomGenerator.randomElement(newArrayList(SAMPLE_TYPE)) );

        val sampleId1 = sampleService.create(studyId, sampleInput1CreateRequest);
        val sampleId2 = sampleService.create(studyId, sampleInput2CreateRequest);

        val specimen = specimenService.readWithSamples(specimenId);
        assertThat(specimen.getSpecimenId()).isEqualTo(specimenId);
        assertThat(specimen.getSpecimenSubmitterId()).isEqualTo(submitterId);
        assertThat(specimen.getSpecimenClass()).isEqualTo(specimenClass);
        assertThat(specimen.getSpecimenType()).isEqualTo(specimenType);
        assertThat(specimen.getSamples().size()).isEqualTo(2);
        assertThat(getInfoName(specimen)).isEqualTo("specimen1");

        // Verify that we got the same samples as the sample service says we should.
        val actualSet = specimen.getSamples().stream()
            .map(SampleEntity::getSampleId)
            .collect(toSet());
        val expectedSet = newHashSet(sampleId1, sampleId2);
        assertThat(actualSet).hasSameSizeAs(expectedSet);
        assertThat(actualSet).containsAll(expectedSet);
        specimen.getSamples().forEach(sample -> assertThat(sample).isEqualTo(getSample(sample.getSampleId())));
    }

    private CompositeSampleEntity getSample(String id) {
        return sampleService.read(id);
    }

    @Test
    public void testCreateAndDeleteSpecimen() {
        val donorId = "DO2";
        val specimenCreateRequest = new CompositeSpecimenEntity();
        specimenCreateRequest.setSpecimenId(null);
        specimenCreateRequest.setDonorId(donorId);
        specimenCreateRequest.setSpecimenSubmitterId("Specimen 101 Ipsilon Prime");
        specimenCreateRequest.setSpecimenClass("Tumour");
        specimenCreateRequest.setSpecimenType("Cell line - derived from tumour");
        specimenCreateRequest.setInfo(JsonUtils.fromSingleQuoted("{'ageCategory': 42, 'status': 'deceased'}"));

        val status = specimenService.create(DEFAULT_STUDY_ID, specimenCreateRequest);
        val id = specimenCreateRequest.getSpecimenId();

        assertThat(id).startsWith("SP");
        assertThat(status).isEqualTo(id);

        val check = specimenService.read(id);
        assertThat(specimenCreateRequest).isEqualToComparingFieldByField(check);

        specimenService.delete(newArrayList(id));
        assertThat(specimenService.isSpecimenExist(id)).isFalse();
    }

    @Test
    public void testUpdateSpecimen() {
        val donorId = "DO2";
        val specimenCreateRequest = new CompositeSpecimenEntity();
        specimenCreateRequest.setSpecimenId(null);
        specimenCreateRequest.setDonorId(donorId);
        specimenCreateRequest.setSpecimenSubmitterId("Specimen 102 Chiron-Beta Prime");
        specimenCreateRequest.setSpecimenClass("Tumour");
        specimenCreateRequest.setSpecimenType("Metastatic tumour - additional metastatic");

        specimenService.create(DEFAULT_STUDY_ID, specimenCreateRequest);

        val id = specimenCreateRequest.getSpecimenId();

        val specimenUpdateRequest = new Specimen();
        specimenUpdateRequest.setSpecimenSubmitterId(specimenCreateRequest.getSpecimenSubmitterId());
        specimenUpdateRequest.setSpecimenClass("Normal");
        specimenUpdateRequest.setSpecimenType("Normal - other");
        specimenUpdateRequest.setInfo(JsonUtils.fromSingleQuoted("{'notes': ['A sharp, B flat']}"));
        specimenService.update(id, specimenUpdateRequest);

        val s3 = specimenService.read(id);
        val expectedSpecimen = new CompositeSpecimenEntity();
        expectedSpecimen.setSpecimenId(specimenCreateRequest.getSpecimenId());
        expectedSpecimen.setDonorId(specimenCreateRequest.getDonorId());
        expectedSpecimen.setWithSpecimen(specimenUpdateRequest);

        assertThat(s3).isEqualToComparingFieldByField(expectedSpecimen);
    }

    @Test
    public void testSpecimenExists(){
        val existingSpecimenId= DEFAULT_SPECIMEN_ID;
        assertThat(specimenService.isSpecimenExist(existingSpecimenId)).isTrue();
        specimenService.checkSpecimenExist(existingSpecimenId);
        val nonExistingSpecimenId = randomGenerator.generateRandomUUIDAsString();
        assertThat(specimenService.isSpecimenExist(nonExistingSpecimenId)).isFalse();
        specimenService.checkSpecimenExist(existingSpecimenId);
        specimenService.checkSpecimenDoesNotExist(nonExistingSpecimenId);

        assertSongError(() -> specimenService.checkSpecimenExist(nonExistingSpecimenId), SPECIMEN_DOES_NOT_EXIST);
        assertSongError(() -> specimenService.checkSpecimenDoesNotExist(existingSpecimenId), SPECIMEN_ALREADY_EXISTS);
    }

    @Test
    public void testCreateStudyDNE(){
        val randomStudyId = randomGenerator.generateRandomUUIDAsString();
        val specimen = new CompositeSpecimenEntity();
        assertSongError(() -> specimenService.create(randomStudyId, specimen), STUDY_ID_DOES_NOT_EXIST);
    }

    @Test
    public void testCreateCorruptionAndAlreadyExistsErrors(){
        val donorId = DEFAULT_DONOR_ID;
        val existingStudyId = DEFAULT_STUDY_ID;

        val specimenCreateRequest = new CompositeSpecimenEntity();
        specimenCreateRequest.setSpecimenId(null);
        specimenCreateRequest.setDonorId(donorId);
        specimenCreateRequest.setSpecimenType(randomGenerator.randomElement(newArrayList(SPECIMEN_TYPE)));
        specimenCreateRequest.setSpecimenSubmitterId(randomGenerator.generateRandomUUIDAsString());
        specimenCreateRequest.setSpecimenClass(randomGenerator.randomElement(newArrayList(SPECIMEN_CLASS)));

        // Create a specimen
        val specimenId = specimenService.create(existingStudyId, specimenCreateRequest);
        assertThat(specimenService.isSpecimenExist(specimenId)).isTrue();

        // Try to create the specimen again, and assert that the right exception is thrown
        assertSongError(() -> specimenService.create(existingStudyId, specimenCreateRequest), SPECIMEN_ALREADY_EXISTS);

        // 'Accidentally' set the specimenId to something not generated by the idService, and try to create. Should
        // detected the corrupted id field, indicating user might have accidentally set the id, thinking it would be
        // persisted
        val specimenCreateRequest2 = new CompositeSpecimenEntity();
        specimenCreateRequest2.setSpecimenId(randomGenerator.generateRandomUUIDAsString());
        specimenCreateRequest2.setDonorId(donorId);
        specimenCreateRequest2.setSpecimenType(randomGenerator.randomElement(newArrayList(SPECIMEN_TYPE)));
        specimenCreateRequest2.setSpecimenSubmitterId(randomGenerator.generateRandomUUIDAsString());
        specimenCreateRequest2.setSpecimenClass(randomGenerator.randomElement(newArrayList(SPECIMEN_CLASS)));

        assertThat(specimenService.isSpecimenExist(specimenCreateRequest2.getSpecimenId())).isFalse();
        assertSongError(() -> specimenService.create(existingStudyId, specimenCreateRequest2), SPECIMEN_ID_IS_CORRUPTED);
    }

    @Test
    public void testReadSpecimenDNE(){
        val randomSpecimenId = randomGenerator.generateRandomUUIDAsString();
        assertThat(specimenService.isSpecimenExist(randomSpecimenId)).isFalse();
        assertSongError(() -> specimenService.read(randomSpecimenId), SPECIMEN_DOES_NOT_EXIST);
        assertSongError(() -> specimenService.readWithSamples(randomSpecimenId), SPECIMEN_DOES_NOT_EXIST);
    }

    @Test
    public void testReadAndDeleteByParentId(){
        // Create a donor, and then several specimens, and for each specimen 2 samples
        val studyId = DEFAULT_STUDY_ID;
        val donorCreateRequest = new CompositeDonorEntity();
        donorCreateRequest.setDonorId(null);
        donorCreateRequest.setStudyId(studyId);
        donorCreateRequest.setDonorSubmitterId(randomGenerator.generateRandomUUIDAsString());
        donorCreateRequest.setDonorGender(randomGenerator.randomElement(newArrayList(DONOR_GENDER)));

        val donorId = donorService.create(donorCreateRequest);

        val numSpecimens = 5;
        val numSamplesPerSpecimen = 2;
        val expectedSpecimenIds = Sets.<String>newHashSet();
        val expectedSampleIdMap = Maps.<String, Set<String>>newHashMap();
        for(int i=0; i<numSpecimens; i++){

            // Create specimen request
            val specimenCreateRequest = new CompositeSpecimenEntity();
            specimenCreateRequest.setSpecimenId(null);
            specimenCreateRequest.setDonorId(donorId);
            specimenCreateRequest.setSpecimenSubmitterId(randomGenerator.generateRandomUUIDAsString());
            specimenCreateRequest.setSpecimenClass(randomGenerator.randomElement(newArrayList(SPECIMEN_CLASS)));
            specimenCreateRequest.setSpecimenType(randomGenerator.randomElement(newArrayList(SPECIMEN_TYPE)));

            val specimenId = specimenService.create(studyId, specimenCreateRequest);
            expectedSpecimenIds.add(specimenId);

            //Create samples
            for (int j=0; j<numSamplesPerSpecimen; j++){

                // Create sample request
                val sampleCreateRequest = new CompositeSampleEntity();
                sampleCreateRequest.setSampleId(null);
                sampleCreateRequest.setSpecimenId(specimenId);
                sampleCreateRequest.setSampleSubmitterId(randomGenerator.generateRandomUUIDAsString());
                sampleCreateRequest.setSampleType(randomGenerator.randomElement(newArrayList(SAMPLE_TYPE)));

                val sampleId = sampleService.create(studyId, sampleCreateRequest);

                // Store the expected sampleId
                if (!expectedSampleIdMap.containsKey(specimenId)){
                    expectedSampleIdMap.put(specimenId, newHashSet());
                }
                val sampleIds = expectedSampleIdMap.get(specimenId);
                sampleIds.add(sampleId);
            }
        }

        // ReadByParentId (newly created donorId)
        val specimens = specimenService.readByParentId(donorId);
        assertThat(specimens).hasSize(numSpecimens);
        for(val specimen :  specimens){
            val actualSpecimenId = specimen.getSpecimenId();
            val actualSampleIds = specimen.getSamples().stream().map(SampleEntity::getSampleId).collect(toSet());
            assertThat(expectedSpecimenIds).contains(actualSpecimenId);
            assertThat(actualSampleIds).hasSize(numSamplesPerSpecimen);
            val expectedSampleIds = expectedSampleIdMap.get(actualSpecimenId);
            assertThat(expectedSampleIds).hasSize(numSamplesPerSpecimen);
            assertThat(actualSampleIds).isSubsetOf(expectedSampleIds);
            assertThat(expectedSampleIds).isSubsetOf(actualSampleIds);
        }


        // Assert that reading by a non-existent donorId returns something empty

        val randomDonorId = randomGenerator.generateRandomUUIDAsString();
        assertThat(donorService.isDonorExist(randomDonorId)).isFalse();
        val emptySpecimenList = specimenService.readByParentId(randomDonorId);
        assertThat(emptySpecimenList).isEmpty();

        // Delete by parent id
      specimenService.deleteByParentId(donorId);
      val emptySpecimenList2 = specimenService.readByParentId(donorId);
      assertThat(emptySpecimenList2).isEmpty();
    }

    @Test
    public void testDeleteSpecimenDNE(){
        val randomSpecimenId = randomGenerator.generateRandomUUIDAsString();
        assertSongError(() -> specimenService.delete(randomSpecimenId), SPECIMEN_DOES_NOT_EXIST);
        assertSongError(() -> specimenService.delete(newArrayList(randomSpecimenId)), SPECIMEN_DOES_NOT_EXIST);
    }

    @Test
    public void testUpdateSpecimenDNE(){
        val randomSpecimenId = randomGenerator.generateRandomUUIDAsString();
        val specimenUpdateRequest = new Specimen();
        specimenUpdateRequest.setSpecimenSubmitterId(randomGenerator.generateRandomUUIDAsString());
        specimenUpdateRequest.setSpecimenClass( randomGenerator.randomElement(newArrayList(SPECIMEN_CLASS)));
        specimenUpdateRequest.setSpecimenType(randomGenerator.randomElement(newArrayList(SPECIMEN_TYPE)));

        assertSongError(() -> specimenService.update(randomSpecimenId, specimenUpdateRequest), SPECIMEN_DOES_NOT_EXIST);
    }

}
