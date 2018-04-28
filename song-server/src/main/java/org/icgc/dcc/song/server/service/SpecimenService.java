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
import lombok.val;
import org.icgc.dcc.song.server.model.entity.donor.impl.FullDonorEntity;
import org.icgc.dcc.song.server.model.entity.specimen.AbstractSpecimenEntity;
import org.icgc.dcc.song.server.model.entity.specimen.SpecimenEntity;
import org.icgc.dcc.song.server.model.entity.specimen.impl.FullSpecimenEntity;
import org.icgc.dcc.song.server.model.entity.specimen.impl.SterileSpecimenEntity;
import org.icgc.dcc.song.server.model.entity.study.impl.AbstractStudyEntity;
import org.icgc.dcc.song.server.repository.FullSpecimenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_ID_IS_CORRUPTED;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.server.model.entity.donor.impl.FullDonorEntity.buildDonorIdOnly;
import static org.icgc.dcc.song.server.model.entity.specimen.impl.FullSpecimenEntity.createFullSpecimenEntity;

@RequiredArgsConstructor
@Service
//@Transactional
public class SpecimenService {

  @Autowired
  private final IdService idService;
  @Autowired
  private final SampleService sampleService;
  @Autowired
  private final SpecimenInfoService infoService;
  @Autowired
  private final FullSpecimenRepository fullRepository;
  @Autowired
  private final StudyService studyService;

  private String createSpecimenId(String studyId, SpecimenEntity specimenRequest){
    studyService.checkStudyExist(studyId);
    val inputSpecimenId = specimenRequest.getSpecimenId();
    val id = idService.generateSpecimenId(specimenRequest.getSpecimenSubmitterId(), studyId);
    checkServer(isNullOrEmpty(inputSpecimenId) || id.equals(inputSpecimenId), getClass(),
        SPECIMEN_ID_IS_CORRUPTED,
        "The input specimenId '%s' is corrupted because it does not match the idServices specimenId '%s'",
        inputSpecimenId, id);
    checkSpecimenDoesNotExist(id);
    return id;
  }

  private FullSpecimenEntity buildCreateRequest(FullSpecimenEntity specimenEntity){
    val orphanedDonor = buildDonorIdOnly(specimenEntity.getDonor());
    val s = new FullSpecimenEntity();
    s.setSpecimenId(specimenEntity.getSpecimenId());
    s.setDonor(orphanedDonor);
    s.setWithSpecimen(specimenEntity);
    s.setInfo(specimenEntity.getInfo());
    return s;
  }

  public String create(@NonNull String studyId, @NonNull FullSpecimenEntity specimenEntity) {
    val id = createSpecimenId(studyId, specimenEntity);
    specimenEntity.setSpecimenId(id);
    val specimenCreateRequest = buildCreateRequest(specimenEntity);
    fullRepository.save(specimenCreateRequest);
    infoService.create(id, specimenEntity.getInfoAsString());
    specimenEntity.getSamples().forEach(x -> sampleService.create(studyId, x));
    return id;
  }

  public FullSpecimenEntity read(@NonNull String id) {
    val specimenResult = fullRepository.findById(id);
    checkServer(specimenResult.isPresent(), getClass(), SPECIMEN_DOES_NOT_EXIST,
        "The specimen for specimenId '%s' could not be read because it does not exist", id);
    val specimen = specimenResult.get();
    specimen.setInfo(infoService.readNullableInfo(id));
    return specimen;
  }

  FullSpecimenEntity populateSamples(@NonNull FullSpecimenEntity specimenEntity) {
    sampleService.readByParent(specimenEntity).forEach(specimenEntity::addSample);
    return specimenEntity;

  }

  FullSpecimenEntity readWithSamples(String id) {
    val specimenEntity = read(id);
    return populateSamples(specimenEntity);
  }

  Set<FullSpecimenEntity> readByParent(@NonNull FullDonorEntity donorEntity) {
    val specimens = fullRepository.findAllByDonor(donorEntity);
    return specimens.stream()
        .map(this::populateSamples)
        .collect(toImmutableSet());
  }

  Set<FullSpecimenEntity> readByParentId(String parentId) {
    val donorRequest = buildDonorIdOnly(parentId);
    return readByParent(donorRequest);
  }

  //TODO: is sterile really that useful? It makes it clear that an update must not have any children,
  // however could just use a FullSpecimenEntity and then in the update method check that no children are being set
  public void update(@NonNull SterileSpecimenEntity specimenRequest) {
    //TODO: rtisma check that parent donor still exists
    //TODO: rtisma check that persisted donorId matches the requested donorId. If it doesnt, then its an illegal update since the relationship is being changed. Instead, the user should delete, and then recreate
    val specimen = read(specimenRequest.getSpecimenId());
    specimen.setWithSpecimen(specimenRequest);
    fullRepository.save(specimen);
    infoService.update(specimen.getSpecimenId(), specimenRequest.getInfoAsString());
  }

  public FullSpecimenEntity convertToFull(@NonNull SterileSpecimenEntity specimenEntity){
    val donorParent = new FullDonorEntity();
    donorParent.setDonorId(specimenEntity.getDonorId());
    val s = createFullSpecimenEntity(specimenEntity.getSpecimenId(), donorParent, specimenEntity);
    s.setInfo(specimenEntity.getInfo());
    return s;
  }

  public boolean isSpecimenExist(@NonNull String id){
    return fullRepository.existsById(id);
  }

  private FullSpecimenEntity specimenIdRequest(String id){
    val s = new FullSpecimenEntity();
    s.setSpecimenId(id);
    return s;
  }

  public void checkSpecimenExist(String id){
    checkServer(isSpecimenExist(id), getClass(), SPECIMEN_DOES_NOT_EXIST,
        "The specimen with specimenId '%s' does not exist", id);
  }

  public void checkSpecimenDoesNotExist(String id){
    checkServer(!isSpecimenExist(id), getClass(), SPECIMEN_ALREADY_EXISTS,
        "The specimen with specimenId '%s' already exists", id);
  }

  public void delete(@NonNull String id) {
    checkSpecimenExist(id);
    internalDelete(id);
  }

  private void internalDelete(String id){
    sampleService.deleteByParentId(id); //TODO: rtisma confirm the delete is cascaded and that this isnt needed anymore
    fullRepository.deleteById(id);
    infoService.delete(id);
  }

  public void delete(@NonNull List<String> ids) {
    ids.forEach(this::delete);
  }

  //TODO: rtisma should check that donorId exists?
  void deleteByParentId(@NonNull String parentId) {
    val donorRequest = new FullDonorEntity();
    donorRequest.setDonorId(parentId);
    fullRepository.findAllByDonor(donorRequest)
        .stream()
        .map(AbstractSpecimenEntity::getSpecimenId)
        .forEach(this::internalDelete);
  }

  public Optional<String> findByBusinessKey(@NonNull String studyId, @NonNull String submitterId) {
    studyService.checkStudyExist(studyId);
    return fullRepository.findAllBySpecimenSubmitterId(submitterId)
        .stream()
        .map(FullSpecimenEntity::getDonor)
        .map(FullDonorEntity::getStudy)
        .map(AbstractStudyEntity::getStudyId)
        .filter(x -> x.equals(studyId))
        .findFirst();
  }

}
