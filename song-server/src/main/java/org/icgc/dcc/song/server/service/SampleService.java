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

import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.sample.CompositeSampleEntity;
import org.icgc.dcc.song.server.model.entity.sample.Sample;
import org.icgc.dcc.song.server.model.entity.sample.SampleEntity;
import org.icgc.dcc.song.server.repository.DonorRepository;
import org.icgc.dcc.song.server.repository.SampleRepository;
import org.icgc.dcc.song.server.repository.SpecimenRepository;
import org.icgc.dcc.song.server.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_ID_IS_CORRUPTED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_ID_NOT_DEFINED;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;

@Slf4j
@RequiredArgsConstructor
@Service
//@Transactional
public class SampleService {

  @Autowired
  private final SampleRepository fullRepository;

  @Autowired
  private final SampleInfoService infoService;

  @Autowired
  private final IdService idService;

  @Autowired
  private final StudyService studyService;

  @Autowired
  private final StudyRepository studyRepository;

  @Autowired
  private final SpecimenRepository specimenRepository;

  @Autowired
  private final DonorRepository donorRepository;

  private String createSampleId(String studyId, SampleEntity sampleEntity){
    studyService.checkStudyExist(studyId);
    val inputSampleId = sampleEntity.getSampleId();
    val id = idService.generateSampleId(sampleEntity.getSampleSubmitterId(), studyId);
    checkServer(isNullOrEmpty(inputSampleId) || id.equals(inputSampleId), getClass(),
        SAMPLE_ID_IS_CORRUPTED,
        "The input sampleId '%s' is corrupted because it does not match the idServices sampleId '%s'",
        inputSampleId, id);
    checkSampleDoesNotExist(id);
    return id;
  }

  public String create(@NonNull String studyId, @NonNull String specimenId, @NonNull Sample sampleData) {
    val sampleRequest = new CompositeSampleEntity();
    sampleRequest.setSpecimenId(specimenId);
    sampleRequest.setWithSample(sampleData);
    return create(studyId, sampleRequest);
  }

  //TODO: [Related to SONG-260] should we add a specimenService.checkSpecimenExists(sample.getSpecimenId()) here?
  public String create(@NonNull String studyId, @NonNull CompositeSampleEntity compositeSampleEntity) {
    //TODO: rtisma check speciemn exists once SpecimenService is working
    val id = createSampleId(studyId, compositeSampleEntity);

    //TODO: dont like this implicit modification,
    // but keeping it for backwards compatibility. Maybe should return the inputobject with the field filled
    compositeSampleEntity.setSampleId(id);
    //TODO: rtisma test this
    checkServer(!isNullOrEmpty(compositeSampleEntity.getSpecimenId()), getClass(), SPECIMEN_ID_NOT_DEFINED,
        "The CreateSample request is missing the parent specimenId for the data: '%s'", compositeSampleEntity);
    fullRepository.save(compositeSampleEntity);
    infoService.create(id, compositeSampleEntity.getInfoAsString());
    return id;
  }

  public CompositeSampleEntity read(@NonNull String id) {
    val sampleResult = fullRepository.findById(id);
    checkServer(sampleResult.isPresent(), getClass(), SAMPLE_DOES_NOT_EXIST,
        "The sample for sampleId '%s' could not be read because it does not exist", id);
    val sample = sampleResult.get();
    sample.setInfo(infoService.readNullableInfo(id));
    val nonProxy = new CompositeSampleEntity();
    nonProxy.setWithSampleEntity(sample);
    return nonProxy;
  }

  public void update(@NonNull String sampleId, @NonNull Sample sampleUpdate) {
    //TODO: rtisma check that parent specimen still exists
    //TODO: rtisma check that persisted specimenId matches the requested speciemnId. If it doesnt, then its an illegal update since the relationship is being changed. Instead, the user should delete, and then recreate
    val sample  = read(sampleId);
    sample.setWithSample(sampleUpdate);
    fullRepository.save(sample);
    infoService.update(sample.getSampleId(), sampleUpdate.getInfoAsString());
  }

  public void delete(@NonNull String id) {
    checkSampleExists(id);
    fullRepository.deleteById(id);
    infoService.delete(id);
  }

  public void delete(@NonNull List<String> ids){
    ids.forEach(this::delete);
  }

  void deleteByParentId(@NonNull String specimenId) {
    fullRepository.deleteAllBySpecimenId(specimenId);
  }


  public Optional<String> findByBusinessKey(@NonNull String studyId, @NonNull String submitterId) {
    studyService.checkStudyExist(studyId);
    val samples = fullRepository.findAllBySampleSubmitterId(submitterId);
    String sampleId = null;
    for( val sample : samples){
      sampleId = sample.getSampleId();
      val specimenId = sample.getSpecimenId();
      val specimenResult = specimenRepository.findById(specimenId);
      if (!specimenResult.isPresent()){
        sampleId = null;
        break;
      }
      val specimen = specimenResult.get();
      val donorId = specimen.getDonorId();
      val donorResult = donorRepository.findById(donorId);
      if (!donorResult.isPresent()){
        sampleId = null;
        break;
      }
      val donor = donorResult.get();
      if (!donor.getStudyId().equals(studyId)){
        sampleId = null;
      }

    }
    return Optional.ofNullable(sampleId);
  }

  public boolean isSampleExist(@NonNull String id){
    return fullRepository.existsById(id);
  }

  public void checkSampleExists(@NonNull String id){
    checkServer(isSampleExist(id), this.getClass(), SAMPLE_DOES_NOT_EXIST,
        "The sample with sampleId '%s' does not exist", id);
  }

  public void checkSampleDoesNotExist(@NonNull String id){
    checkServer(!isSampleExist(id), getClass(), SAMPLE_ALREADY_EXISTS,
        "The sample with sampleId '%s' already exists", id);
  }

  Set<CompositeSampleEntity> readByParentId(@NonNull String specimenId) {
    val results = fullRepository.findAllBySpecimenId(specimenId);
    val samplesBuilder = ImmutableSet.<CompositeSampleEntity>builder();
    for (val result : results){
      val s = new CompositeSampleEntity();
      s.setWithSampleEntity(result);
      samplesBuilder.add(s);
    }
    val samples = samplesBuilder.build();
    samples.forEach(x -> x.setInfo(infoService.readNullableInfo(x.getSampleId())));
    return samples;
  }

}
