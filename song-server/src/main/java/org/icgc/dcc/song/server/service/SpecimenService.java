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
import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.donor.DonorEntity;
import org.icgc.dcc.song.server.model.entity.specimen.CompositeSpecimenEntity;
import org.icgc.dcc.song.server.model.entity.specimen.SpecimenEntity;
import org.icgc.dcc.song.server.repository.DonorRepository;
import org.icgc.dcc.song.server.repository.SpecimenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DONOR_ID_NOT_DEFINED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.SPECIMEN_ID_IS_CORRUPTED;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;

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
  private final SpecimenRepository fullRepository;
  @Autowired
  private final StudyService studyService;
  @Autowired
  private final DonorRepository donorRepository;

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

  public String create(@NonNull String studyId, @NonNull CompositeSpecimenEntity compositeSpecimenEntity) {
    val id = createSpecimenId(studyId, compositeSpecimenEntity);
    compositeSpecimenEntity.setSpecimenId(id);

    //TODO: rtisma test this
    checkServer(!isNullOrEmpty(compositeSpecimenEntity.getDonorId()), getClass(), DONOR_ID_NOT_DEFINED,
        "The CreateSpecimen request is missing the parent donorId for the data: '%s'", compositeSpecimenEntity);

    fullRepository.save(compositeSpecimenEntity);
    infoService.create(id, compositeSpecimenEntity.getInfoAsString());
    compositeSpecimenEntity.getSamples().forEach(x -> sampleService.create(studyId, x));
    return id;
  }

  public CompositeSpecimenEntity read(@NonNull String id) {
    val specimenResult = fullRepository.findById(id);
    checkServer(specimenResult.isPresent(), getClass(), SPECIMEN_DOES_NOT_EXIST,
        "The specimen for specimenId '%s' could not be read because it does not exist", id);
    val specimen = specimenResult.get();
    specimen.setInfo(infoService.readNullableInfo(id));
    return specimen;
  }

  CompositeSpecimenEntity readWithSamples(String id) {
    val specimenEntity = read(id);
    populateInplace(specimenEntity);
    return specimenEntity;
  }

  //TODO: rtisma there is no check for existence of a donorId...
  Set<CompositeSpecimenEntity> readByParentId(@NonNull String donorId) {
    val specimens = fullRepository.findAllByDonorId(donorId);
    specimens.forEach(this::populateInplace);
    return ImmutableSet.copyOf(specimens);
  }

  //TODO: is sterile really that useful? It makes it clear that an update must not have any children,
  // however could just use a FullSpecimenEntity and then in the update method check that no children are being set
  public void update(@NonNull SpecimenEntity specimenRequest) {
    //TODO: rtisma check that parent donor still exists
    //TODO: rtisma check that persisted donorId matches the requested donorId. If it doesnt, then its an illegal update since the relationship is being changed. Instead, the user should delete, and then recreate
    val specimen = read(specimenRequest.getSpecimenId());
    specimen.setWithSpecimen(specimenRequest);
    fullRepository.save(specimen);
    infoService.update(specimen.getSpecimenId(), specimenRequest.getInfoAsString());
  }

  public boolean isSpecimenExist(@NonNull String id){
    return fullRepository.existsById(id);
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
  void deleteByParentId(@NonNull String donorId) {
    fullRepository.findAllByDonorId(donorId).stream()
        .map(SpecimenEntity::getSpecimenId)
        .forEach(this::internalDelete);
  }

  //TODO: rtisma not the most efficient. Since we are not using Parent object (it complicated things before), we
  // cannot do specimen.getDonor().getStudy().getStudyId().equals(studyiId) anymore.
  public Optional<String> findByBusinessKey(@NonNull String studyId, @NonNull String submitterId) {
    studyService.checkStudyExist(studyId);
    return fullRepository.findAllBySpecimenSubmitterId(submitterId).stream()
        .map(SpecimenEntity::getDonorId)
        .map(Lists::newArrayList)
        .map(donorRepository::findAllById)
        .flatMap(Collection::stream)
        .filter(x -> x.getStudyId().equals(studyId))
        .map(DonorEntity::getDonorId)
        .findFirst();
  }

  private void populateInplace(CompositeSpecimenEntity specimenEntity){
    sampleService.readByParentId(specimenEntity.getSpecimenId())
        .forEach(specimenEntity::addSample);
  }

}
