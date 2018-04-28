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
import org.icgc.dcc.song.server.repository.SterileSpecimenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_ID_IS_CORRUPTED;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.server.model.entity.specimen.impl.SterileSpecimenEntity.createSterileSpecimenEntity;

@RequiredArgsConstructor
@Service
@Transactional
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
  private final SterileSpecimenRepository sterileRepository;
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

  public String create(@NonNull String studyId, @NonNull FullSpecimenEntity specimenRequest) {
    val id = createSpecimenId(studyId, specimenRequest);
    val sterileSpecimen = createSterileSpecimenEntity(id, specimenRequest.getDonorId(), specimenRequest);
    sterileRepository.save(sterileSpecimen);
    infoService.create(id, specimenRequest.getInfoAsString());
    specimenRequest.getSamples().forEach(x -> sampleService.create(studyId, x));
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


  FullSpecimenEntity readWithSamples(String id) {
    val specimenEntity = read(id);
    sampleService.readByParentId(id).forEach(specimenEntity::addSample);
    return specimenEntity;
  }

  Set<FullSpecimenEntity> readByParentId(String parentId) {
    val specimens = sterileRepository.findAllByDonorId(parentId);
    return specimens.stream()
        .map(AbstractSpecimenEntity::getSpecimenId)
        .map(this::readWithSamples)
        .collect(toImmutableSet());
  }

  public void update(@NonNull SterileSpecimenEntity specimen) {
    //TODO: rtisma check that parent donor still exists
    //TODO: rtisma check that persisted donorId matches the requested donorId. If it doesnt, then its an illegal update since the relationship is being changed. Instead, the user should delete, and then recreate
    checkSpecimenExist(specimen.getSpecimenId());
    sterileRepository.save(specimen);
    infoService.update(specimen.getSpecimenId(), specimen.getInfoAsString());
  }

  public boolean isSpecimenExist(@NonNull String id){
    return sterileRepository.existsById(id);
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
    sterileRepository.deleteById(id);
    infoService.delete(id);
  }

  public void delete(@NonNull List<String> ids) {
    ids.forEach(this::delete);
  }

  //TODO: rtisma should check that donorId exists?
  void deleteByParentId(@NonNull String parentId) {
    sterileRepository.findAllByDonorId(parentId)
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
