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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.donor.impl.FullDonorEntity;
import org.icgc.dcc.song.server.model.entity.sample.AbstractSampleEntity;
import org.icgc.dcc.song.server.model.entity.sample.SampleEntity;
import org.icgc.dcc.song.server.model.entity.sample.impl.FullSampleEntity;
import org.icgc.dcc.song.server.model.entity.sample.impl.SterileSampleEntity;
import org.icgc.dcc.song.server.model.entity.specimen.impl.FullSpecimenEntity;
import org.icgc.dcc.song.server.model.entity.study.impl.AbstractStudyEntity;
import org.icgc.dcc.song.server.repository.FullSampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SAMPLE_ID_IS_CORRUPTED;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.server.model.entity.specimen.impl.FullSpecimenEntity.buildSpecimenIdOnly;

@Slf4j
@RequiredArgsConstructor
@Service
//@Transactional
public class SampleService {

  @Autowired
  private final FullSampleRepository fullRepository;

  @Autowired
  private final SampleInfoService infoService;

  @Autowired
  private final IdService idService;

  @Autowired
  private final StudyService studyService;

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

  private FullSampleEntity buildCreateRequest(FullSampleEntity sampleEntity){
    val orphanedDonor = buildSpecimenIdOnly(sampleEntity.getSpecimen());
    val s = new FullSampleEntity();
    s.setSampleId(sampleEntity.getSampleId());
    s.setSpecimen(orphanedDonor);
    s.setWithSample(sampleEntity);
    s.setInfo(sampleEntity.getInfo());
    return s;
  }

  //TODO: [Related to SONG-260] should we add a specimenService.checkSpecimenExists(sample.getSpecimenId()) here?
  public String create(@NonNull String studyId, @NonNull FullSampleEntity sampleEntity) {
    //TODO: rtisma check speciemn exists once SpecimenService is working
    val id = createSampleId(studyId, sampleEntity);

    //TODO: dont like this implicit modification,
    // but keeping it for backwards compatibility. Maybe should return the inputobject with the field filled
    sampleEntity.setSampleId(id);
    val sampleCreateRequest = buildCreateRequest(sampleEntity);

    fullRepository.save(sampleCreateRequest);
    infoService.create(id, sampleEntity.getInfoAsString());
    return id;
  }

  public FullSampleEntity read(@NonNull String id) {
    val sampleResult = fullRepository.findById(id);
    return interpretResult(id, sampleResult);
  }

  public void update(@NonNull SterileSampleEntity sampleRequest) {
    //TODO: rtisma check that parent specimen still exists
    //TODO: rtisma check that persisted specimenId matches the requested speciemnId. If it doesnt, then its an illegal update since the relationship is being changed. Instead, the user should delete, and then recreate
    val sample  = read(sampleRequest.getSampleId());
    sample.setWithSample(sampleRequest);
    fullRepository.save(sample);
    infoService.update(sample.getSampleId(), sampleRequest.getInfoAsString());
  }

  public void delete(@NonNull String id) {
    checkSampleExists(id);
    fullRepository.deleteById(id);
    infoService.delete(id);
  }

  public void delete(@NonNull List<String> ids){
    ids.forEach(this::delete);
  }

  void deleteByParentId(@NonNull String parentId) {
    val specimenRequest = new FullSpecimenEntity();
    specimenRequest.setSpecimenId(parentId);
    deleteByParent(specimenRequest);
  }

  void deleteByParent(@NonNull FullSpecimenEntity specimenEntity ) {
    fullRepository.deleteAllBySpecimen(specimenEntity);
  }

  public Optional<String> findByBusinessKey(@NonNull String studyId, @NonNull String submitterId) {
    studyService.checkStudyExist(studyId);
    return fullRepository.findAllBySampleSubmitterId(submitterId).stream()
        .map(FullSampleEntity::getSpecimen)
        .map(FullSpecimenEntity::getDonor)
        .map(FullDonorEntity::getStudy)
        .map(AbstractStudyEntity::getStudyId)
        .filter(x -> x.equals(studyId))
        .findFirst();
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

  Set<FullSampleEntity> readByParent(@NonNull FullSpecimenEntity specimenEntity) {
    val samples = fullRepository.findAllBySpecimen(specimenEntity);
    samples.forEach(x -> x.setInfo(infoService.readNullableInfo(x.getSampleId())));
    return samples;
  }

  Set<FullSampleEntity> readByParentId(@NonNull String parentId) {
    val specimenRequest = buildSpecimenIdOnly(parentId);
    return readByParent(specimenRequest);
  }

  private <T extends AbstractSampleEntity> T interpretResult(String id , Optional<T> sampleResult) {
    checkServer(sampleResult.isPresent(), getClass(), SAMPLE_DOES_NOT_EXIST,
        "The sample for sampleId '%s' could not be read because it does not exist", id);
    val sample = sampleResult.get();
    sample.setInfo(infoService.readNullableInfo(id));
    return sample;
  }


}
