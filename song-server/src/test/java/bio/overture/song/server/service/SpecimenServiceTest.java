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
package bio.overture.song.server.service;

import bio.overture.song.core.testing.SongErrorAssertions;
import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.core.utils.RandomGenerator;
import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.model.entity.Sample;
import bio.overture.song.server.model.entity.Specimen;
import bio.overture.song.server.model.entity.composites.DonorWithSpecimens;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static bio.overture.song.core.exceptions.ServerErrors.SPECIMEN_ALREADY_EXISTS;
import static bio.overture.song.core.exceptions.ServerErrors.SPECIMEN_DOES_NOT_EXIST;
import static bio.overture.song.core.exceptions.ServerErrors.SPECIMEN_ID_IS_CORRUPTED;
import static bio.overture.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static bio.overture.song.core.testing.SongErrorAssertions.assertSongError;
import static bio.overture.song.core.utils.RandomGenerator.createRandomGenerator;
import static bio.overture.song.server.model.enums.Constants.DONOR_GENDER;
import static bio.overture.song.server.model.enums.Constants.SAMPLE_TYPE;
import static bio.overture.song.server.model.enums.Constants.SPECIMEN_CLASS;
import static bio.overture.song.server.model.enums.Constants.SPECIMEN_TYPE;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_DONOR_ID;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_SPECIMEN_ID;
import static bio.overture.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static bio.overture.song.server.utils.TestFiles.getInfoName;
import static bio.overture.song.server.utils.securestudy.impl.SecureSpecimenTester.createSecureSpecimenTester;

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
        assertThat(donorService.isDonorExist(DEFAULT_DONOR_ID)).isTrue();
    }

    @Test
    public void testReadSpecimen() {
        // find existing specimen in the database
        val id = "SP1";
        val s = specimenService.securedRead(DEFAULT_STUDY_ID, id);
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
        val randomSpecimen = Specimen.builder()
            .specimenId(null)
            .donorId(donorId)
            .specimenSubmitterId(submitterId)
            .specimenClass(specimenClass)
            .specimenType(specimenType )
            .build();
        randomSpecimen.setInfo("name", "specimen1");
        val specimenId =  specimenService.create(DEFAULT_STUDY_ID, randomSpecimen);
        val sampleInput1 = Sample.builder()
            .sampleId(null)
            .sampleSubmitterId(randomGenerator.generateRandomUUIDAsString())
            .specimenId(specimenId)
            .sampleType(randomGenerator.randomElement(newArrayList(SAMPLE_TYPE)) )
            .build();

        val sampleInput2 = Sample.builder()
            .sampleId(null)
            .sampleSubmitterId(randomGenerator.generateRandomUUIDAsString())
            .specimenId(specimenId)
            .sampleType(randomGenerator.randomElement(newArrayList(SAMPLE_TYPE)) )
            .build();

        val sampleId1 = sampleService.create(studyId, sampleInput1);
        val sampleId2 = sampleService.create(studyId, sampleInput2);



        val specimen = specimenService.readWithSamples(specimenId);
        assertThat(specimen.getSpecimenId()).isEqualTo(specimenId);
        assertThat(specimen.getSpecimenSubmitterId()).isEqualTo(submitterId);
        assertThat(specimen.getSpecimenClass()).isEqualTo(specimenClass);
        assertThat(specimen.getSpecimenType()).isEqualTo(specimenType);
        assertThat(specimen.getSamples().size()).isEqualTo(2);
        assertThat(getInfoName(specimen)).isEqualTo("specimen1");

        // Verify that we got the same samples as the sample service says we should.
        val actualSet = specimen.getSamples().stream()
            .map(Sample::getSampleId)
            .collect(toSet());
        val expectedSet = newHashSet(sampleId1, sampleId2);
        Assertions.assertThat(actualSet).hasSameSizeAs(expectedSet);
        Assertions.assertThat(actualSet).containsAll(expectedSet);
        specimen.getSamples().forEach(sample -> assertThat(sample).isEqualTo(getSample(sample.getSampleId())));
    }

    private Sample getSample(String id) {
        return sampleService.unsecuredRead(id);
    }

    @Test
//    @Transactional
    public void testCreateAndDeleteSpecimen() {
        val donorId = DEFAULT_DONOR_ID;
        val s = Specimen.builder()
            .specimenId("")
            .specimenSubmitterId("Specimen 101 Ipsilon Prime")
            .donorId(donorId)
            .specimenClass("Tumour")
            .specimenType("Cell line - derived from tumour")
            .build();

        s.setInfo(JsonUtils.fromSingleQuoted("{'ageCategory': 42, 'status': 'deceased'}"));

        val status = specimenService.create(DEFAULT_STUDY_ID, s);
        val id = s.getSpecimenId();
        assertThat(specimenService.isSpecimenExist(id)).isTrue();

        // Issue #288 - unable to read specimen after deleting all its samples
        // This is neccessary since the BusinessKeyView does inner joins on donor, specimen and sample.
        // If the specimen was created without any samples, then that specimen would not meet the constraints
        // of an inner join with the samples specimen_id. A solution is to change them to LEFT JOINs however
        // there is no reason for a LEFT JOIN outside of this testcase, as there can never be a childless specimen
        val sample1 = Sample.builder()
            .sampleSubmitterId(randomGenerator.generateRandomUUIDAsString())
            .sampleType(randomGenerator.randomElement(newArrayList(SAMPLE_TYPE)))
            .specimenId(id)
            .build();
        val sampleId = sampleService.create(DEFAULT_STUDY_ID, sample1);

        assertThat(id).startsWith("SP");
        Assertions.assertThat(status).isEqualTo(id);

        val check = specimenService.securedRead(DEFAULT_STUDY_ID, id);
        assertThat(s).isEqualToComparingFieldByField(check);

        val response = specimenService.securedDelete(DEFAULT_STUDY_ID, newArrayList(id));
        assertThat(specimenService.isSpecimenExist(id)).isFalse();
        Assertions.assertThat(response).isEqualTo("OK");
    }

    @Test
    public void testUpdateSpecimen() {
        val donorId = DEFAULT_DONOR_ID;
        val s = Specimen.builder()
            .specimenId("")
            .specimenSubmitterId("Specimen 102 Chiron-Beta Prime")
            .donorId(donorId)
            .specimenClass("Tumour")
            .specimenType("Metastatic tumour - additional metastatic")
            .build();

        specimenService.create(DEFAULT_STUDY_ID, s);

        val id = s.getSpecimenId();

        // Issue #288 - unable to read specimen after deleting all its samples
        // This is neccessary since the BusinessKeyView does inner joins on donor, specimen and sample.
        // If the specimen was created without any samples, then that specimen would not meet the constraints
        // of an inner join with the samples specimen_id. A solution is to change them to LEFT JOINs however
        // there is no reason for a LEFT JOIN outside of this testcase, as there can never be a childless specimen
        val sample1 = Sample.builder()
            .sampleSubmitterId(randomGenerator.generateRandomUUIDAsString())
            .sampleType(randomGenerator.randomElement(newArrayList(SAMPLE_TYPE)))
            .specimenId(id)
            .build();
        val sampleId = sampleService.create(DEFAULT_STUDY_ID, sample1);

        val s2 = Specimen.builder()
            .specimenId(id)
            .specimenSubmitterId("Specimen 102")
            .donorId(s.getDonorId())
            .specimenClass("Normal")
            .specimenType( "Normal - other")
            .build();

        s2.setInfo(JsonUtils.fromSingleQuoted("{'notes': ['A sharp, B flat']}"));
        specimenService.update(s2);

        val s3 = specimenService.securedRead(DEFAULT_STUDY_ID, id);
        Assertions.assertThat(s3).isEqualToComparingFieldByField(s2);
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
        val specimen = new Specimen();
        SongErrorAssertions
            .assertSongError(() -> specimenService.create(randomStudyId, specimen), STUDY_ID_DOES_NOT_EXIST);
    }

    @Test
    public void testCreateCorruptionAndAlreadyExistsErrors(){
        val donorId = DEFAULT_DONOR_ID;
        val existingStudyId = DEFAULT_STUDY_ID;

        val specimen = new Specimen();
        specimen.setSpecimenType(randomGenerator.randomElement(newArrayList(SPECIMEN_TYPE)));
        specimen.setSpecimenSubmitterId(randomGenerator.generateRandomUUIDAsString());
        specimen.setSpecimenClass(randomGenerator.randomElement(newArrayList(SPECIMEN_CLASS)));
        specimen.setDonorId(donorId);

        // Create a specimen
        val specimenId = specimenService.create(existingStudyId, specimen);
        assertThat(specimenService.isSpecimenExist(specimenId)).isTrue();

        // Try to create the specimen again, and assert that the right exception is thrown
        SongErrorAssertions
            .assertSongError(() -> specimenService.create(existingStudyId, specimen), SPECIMEN_ALREADY_EXISTS);

        // 'Accidentally' set the specimenId to something not generated by the idService, and try to create. Should
        // detected the corrupted id field, indicating user might have accidentally set the id, thinking it would be
        // persisted
        val specimen2 = new Specimen();
        specimen2.setSpecimenType(randomGenerator.randomElement(newArrayList(SPECIMEN_TYPE)));
        specimen2.setSpecimenSubmitterId(randomGenerator.generateRandomUUIDAsString());
        specimen2.setSpecimenClass(randomGenerator.randomElement(newArrayList(SPECIMEN_CLASS)));
        specimen2.setDonorId(donorId);
        specimen2.setSpecimenId(randomGenerator.generateRandomUUIDAsString());
        assertThat(specimenService.isSpecimenExist(specimen2.getSpecimenId())).isFalse();
        SongErrorAssertions
            .assertSongError(() -> specimenService.create(existingStudyId, specimen2), SPECIMEN_ID_IS_CORRUPTED);
    }

    @Test
    public void testReadSpecimenDNE(){
        val randomSpecimenId = randomGenerator.generateRandomUUIDAsString();
        assertThat(specimenService.isSpecimenExist(randomSpecimenId)).isFalse();
        SongErrorAssertions
            .assertSongError(() -> specimenService.unsecuredRead(randomSpecimenId), SPECIMEN_DOES_NOT_EXIST);
        SongErrorAssertions
            .assertSongError(() -> specimenService.readWithSamples(randomSpecimenId), SPECIMEN_DOES_NOT_EXIST);
    }

    @Test
    public void testReadAndDeleteByParentId(){
        // Create a donor, and then several specimens, and for each specimen 2 samples
        val studyId = DEFAULT_STUDY_ID;
        val donor = Donor.builder()
            .donorId("")
            .donorSubmitterId(randomGenerator.generateRandomUUIDAsString())
            .studyId(studyId)
            .donorGender(randomGenerator.randomElement(newArrayList(DONOR_GENDER)))
            .build();
        val donorWithSpecimens = new DonorWithSpecimens();
        donorWithSpecimens.setDonor(donor);
        val donorId = donorService.create(donorWithSpecimens);

        val numSpecimens = 5;
        val numSamplesPerSpecimen = 2;
        val expectedSpecimenIds = Sets.<String>newHashSet();
        val expectedSampleIdMap = Maps.<String, Set<String>>newHashMap();
        for(int i=0; i<numSpecimens; i++){
            // Create specimen
            val specimen = Specimen.builder()
                .specimenId("")
                .specimenSubmitterId(randomGenerator.generateRandomUUIDAsString())
                .donorId(donorId)
                .specimenClass( randomGenerator.randomElement(newArrayList(SPECIMEN_CLASS)))
                .specimenType( randomGenerator.randomElement(newArrayList(SPECIMEN_TYPE)))
                .build();
            val specimenId = specimenService.create(studyId,specimen);
            expectedSpecimenIds.add(specimenId);

            //Create samples
            for (int j=0; j<numSamplesPerSpecimen; j++){
                val sample = Sample.builder()
                    .sampleId("")
                    .sampleSubmitterId(randomGenerator.generateRandomUUIDAsString())
                    .specimenId(specimenId)
                    .sampleType(randomGenerator.randomElement(newArrayList(SAMPLE_TYPE)) )
                    .build();
                val sampleId = sampleService.create(studyId, sample);

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
        Assertions.assertThat(specimens).hasSize(numSpecimens);
        for(val specimen :  specimens){
            val actualSpecimenId = specimen.getSpecimenId();
            val actualSampleIds = specimen.getSamples().stream().map(Sample::getSampleId).collect(toSet());
            assertThat(expectedSpecimenIds).contains(actualSpecimenId);
            Assertions.assertThat(actualSampleIds).hasSize(numSamplesPerSpecimen);
            val expectedSampleIds = expectedSampleIdMap.get(actualSpecimenId);
            assertThat(expectedSampleIds).hasSize(numSamplesPerSpecimen);
            Assertions.assertThat(actualSampleIds).isSubsetOf(expectedSampleIds);
            assertThat(expectedSampleIds).isSubsetOf(actualSampleIds);
        }


        // Assert that reading by a non-existent donorId returns something empty

        val randomDonorId = randomGenerator.generateRandomUUIDAsString();
        assertThat(donorService.isDonorExist(randomDonorId)).isFalse();
        val emptySpecimenList = specimenService.readByParentId(randomDonorId);
        Assertions.assertThat(emptySpecimenList).isEmpty();

        // Delete by parent id
      val response = specimenService.deleteByParentId(donorId);
      Assertions.assertThat(response).isEqualTo("OK");
      val emptySpecimenList2 = specimenService.readByParentId(donorId);
      Assertions.assertThat(emptySpecimenList2).isEmpty();
    }

    @Test
    public void testDeleteSpecimenDNE(){
        val randomSpecimenId = randomGenerator.generateRandomUUIDAsString();
        SongErrorAssertions
            .assertSongError(() -> specimenService.unsecuredDelete(randomSpecimenId), SPECIMEN_DOES_NOT_EXIST);
        SongErrorAssertions.assertSongError(() -> specimenService.unsecuredDelete(newArrayList(randomSpecimenId)), SPECIMEN_DOES_NOT_EXIST);
    }

    @Test
    @Transactional
    public void testCheckSpecimenUnrelatedToStudy(){
        val existingDonorId = DEFAULT_DONOR_ID;
        val secureSpecimenTester = createSecureSpecimenTester(randomGenerator,studyService, donorService, specimenService);

        secureSpecimenTester.runSecureTest((studyId, id) -> specimenService.checkSpecimenRelatedToStudy(studyId, id),
            existingDonorId);

        secureSpecimenTester.runSecureTest((studyId, id) -> specimenService.securedRead(studyId, id),
            existingDonorId);

        secureSpecimenTester.runSecureTest((studyId, id) -> specimenService.securedDelete(studyId, id),
            existingDonorId);

        secureSpecimenTester.runSecureTest((studyId, id) -> specimenService.securedDelete(studyId, newArrayList(id)),
            existingDonorId);

    }


    @Test
    public void testUpdateSpecimenDNE(){
        val randomSpecimenId = randomGenerator.generateRandomUUIDAsString();
        val specimen = Specimen.builder()
            .specimenId(randomSpecimenId)
            .specimenSubmitterId(randomGenerator.generateRandomUUIDAsString())
            .donorId(DEFAULT_DONOR_ID)
            .specimenClass( randomGenerator.randomElement(newArrayList(SPECIMEN_CLASS)))
            .specimenType( randomGenerator.randomElement(newArrayList(SPECIMEN_TYPE)))
            .build();
        SongErrorAssertions.assertSongError(() -> specimenService.update(specimen), SPECIMEN_DOES_NOT_EXIST);
    }

}
