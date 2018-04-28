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
import org.icgc.dcc.song.server.model.entity.donor.AbstractDonorEntity;
import org.icgc.dcc.song.server.model.entity.donor.DonorEntity;
import org.icgc.dcc.song.server.model.entity.donor.impl.FullDonorEntity;
import org.icgc.dcc.song.server.model.entity.study.impl.FullStudyEntity;
import org.icgc.dcc.song.server.repository.FullDonorRepository;
import org.icgc.dcc.song.server.repository.SterileDonorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DONOR_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DONOR_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DONOR_ID_IS_CORRUPTED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DUPLICATE_DONOR_IDS;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.server.model.entity.study.impl.FullStudyEntity.buildStudyIdOnly;

@RequiredArgsConstructor
@Service
//@Transactional
public class DonorService {

  @Autowired
  private final FullDonorRepository fullRepository;
  @Autowired
  private final SterileDonorRepository sterileRepository;
  @Autowired
  private final DonorInfoService infoService;
  @Autowired
  private final IdService idService;
  @Autowired
  private final SpecimenService specimenService;
  @Autowired
  private final StudyService studyService;

  private String createDonorId(DonorEntity donorRequest){
    studyService.checkStudyExist(donorRequest.getStudyId());
    val inputDonorId = donorRequest.getDonorId();
    val id = idService.generateDonorId(donorRequest.getDonorSubmitterId(), donorRequest.getStudyId());
    checkServer(isNullOrEmpty(inputDonorId) || id.equals(inputDonorId), getClass(),
        DONOR_ID_IS_CORRUPTED,
        "The input donorId '%s' is corrupted because it does not match the idServices donorId '%s'",
        inputDonorId, id);
    checkDonorDoesNotExist(id);
    return id;
  }

  /**
   * This is neccessaary since hibernate does not like it when the entity being saved has no children, however its
   * parent has children. In this case, when the create method is passed a donorEntity that is populated with children
   * that have not yet been assigned an id, a new donorEntity must be created that has the same data however is
   * without children. In addition, the parent should also been without children. All this manipulation is very dirty,
   * and needs to be refactored.
   */
  private FullDonorEntity buildCreateRequest(FullDonorEntity donorEntity){
    val orphanedStudy= buildStudyIdOnly(donorEntity.getStudy());
    val d = new FullDonorEntity();
    d.setDonorId(donorEntity.getDonorId());
    d.setStudy(orphanedStudy);
    d.setWithDonor(donorEntity);
    d.setInfo(donorEntity.getInfo());
    return d;
  }

  public String create(@NonNull FullDonorEntity donorRequest) {
    val id = createDonorId(donorRequest);
    donorRequest.setDonorId(id);
    val donorCreateRequest =  buildCreateRequest(donorRequest);
    fullRepository.save(donorCreateRequest);
    infoService.create(id, donorRequest.getInfoAsString());
    donorRequest.getSpecimens().forEach(x -> specimenService.create(donorRequest.getStudyId(), x));
    return id;
  }

  public FullDonorEntity read(@NonNull String id) {
    val donorResult = fullRepository.findById(id);
    checkServer(donorResult.isPresent(), getClass(), DONOR_DOES_NOT_EXIST,
      "The donor for donorId '%s' could not be read because it does not exist", id);
    val donor = donorResult.get();
    donor.setInfo(infoService.readNullableInfo(id));
    return donor;
  }

  public FullDonorEntity readWithSpecimens(@NonNull String id) {
    val donor = read(id);
    donor.setSpecimens(specimenService.readByParentId(id));
    return donor;
  }

  public FullDonorEntity populateSpecimens(@NonNull FullDonorEntity donorEntity) {

    donorEntity.setSpecimens(specimenService.readByParent(donorEntity));
    return donorEntity;
  }

  public Set<FullDonorEntity> readByParent(@NonNull FullStudyEntity studyEntity) {
    studyService.checkStudyExist(studyEntity.getStudyId());
    return fullRepository.findAllByStudy(studyEntity) .stream()
        .map(this::populateSpecimens)
        .collect(toImmutableSet());
  }

  public Set<FullDonorEntity> readByParentId(@NonNull String parentId) {
    val studyRequest = new FullStudyEntity();
    studyRequest.setStudyId(parentId);
    return readByParent(studyRequest);
  }

  public boolean isDonorExist(@NonNull String id){
    return sterileRepository.existsById(id);
  }

  public void checkDonorExists(@NonNull DonorEntity donor){
    checkDonorExists(donor.getDonorId());
  }

  public void checkDonorExists(@NonNull String id){
    checkServer(isDonorExist(id), this.getClass(), DONOR_DOES_NOT_EXIST,
        "The donor with donorId '%s' does not exist", id);
  }

  public void checkDonorDoesNotExist(@NonNull String id){
    checkServer(!isDonorExist(id), getClass(), DONOR_ALREADY_EXISTS,
        "The donor with donorId '%s' already exists", id);
  }

  public void update(@NonNull String id, @NonNull AbstractDonorEntity donorUpdate) {
    val fullDonor = read(id);
    fullDonor.setWithDonor(donorUpdate);
    fullRepository.save(fullDonor);
    infoService.update(id, donorUpdate.getInfoAsString());
  }

  public void delete(@NonNull List<String> ids) {
    ids.forEach(this::delete);
  }

  public void delete(@NonNull String id) {
    checkDonorExists(id);
    uncheckedDelete(id);
  }
  private void uncheckedDelete(@NonNull String id) {
    specimenService.deleteByParentId(id);
    fullRepository.deleteById(id);
    infoService.delete(id);
  }

  // TODO: [SONG-254] DeleteByParentId spec missing -- https://github.com/overture-stack/SONG/issues/254
  public void deleteByParentId(@NonNull String studyId) {
    studyService.checkStudyExist(studyId);
    sterileRepository.findAllByStudyId(studyId)
        .forEach(d -> uncheckedDelete(d.getDonorId()));
  }

  public Optional<String> findByBusinessKey(@NonNull String studyId, @NonNull String submitterId){
    val donors = sterileRepository.findAllByStudyIdAndDonorSubmitterId(studyId, submitterId);
    checkServer(donors.size() < 2, getClass(), DUPLICATE_DONOR_IDS,
        "Searching by studyId '%s' and donorSubmitterId '%s' resulted in more than 1 result (%s)",
        studyId, submitterId, donors.size());
    return donors.stream()
        .map(AbstractDonorEntity::getDonorId)
        .findFirst();
  }

  public String save(@NonNull FullDonorEntity donor) {
    val donorIdResult = findByBusinessKey(donor.getStudyId(), donor.getDonorSubmitterId());
    String donorId;
    if (donorIdResult.isPresent()) {
      donorId = donorIdResult.get();
      update(donorIdResult.get(), donor);
    } else {
      donorId = create(donor);
    }
    return donorId;
  }

}
