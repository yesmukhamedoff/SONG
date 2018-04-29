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
import lombok.val;
import org.icgc.dcc.song.server.model.entity.donor.CompositeDonorEntity;
import org.icgc.dcc.song.server.model.entity.donor.Donor;
import org.icgc.dcc.song.server.model.entity.donor.DonorEntity;
import org.icgc.dcc.song.server.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DONOR_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DONOR_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DONOR_ID_IS_CORRUPTED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.DUPLICATE_DONOR_IDS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_NOT_DEFINED;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;

@RequiredArgsConstructor
@Service
//@Transactional
public class DonorService {

  @Autowired
  private final DonorRepository fullRepository;

  @Autowired
  private final DonorInfoService infoService;
  @Autowired
  private final IdService idService;
  @Autowired
  private final SpecimenService specimenService;
  @Autowired
  private final StudyService studyService;

  public String create(@NonNull String donorId, @NonNull String studyId,
      @NonNull Donor donorData) {
    val donorRequest = new CompositeDonorEntity();
    donorRequest.setStudyId(studyId);
    donorRequest.setDonorId(donorId);
    donorRequest.setWithDonor(donorData);
    return create(donorRequest);
  }

  public String create(@NonNull CompositeDonorEntity donorRequest) {
    val id = createDonorId(donorRequest);
    donorRequest.setDonorId(id);

    //TODO: rtisma test this
    checkServer(!isNullOrEmpty(donorRequest.getStudyId()), getClass(), STUDY_ID_NOT_DEFINED,
        "The CreateDonor request is missing the parent studyId for the data: '%s'", donorRequest);

    fullRepository.save(donorRequest);
    infoService.create(id, donorRequest.getInfoAsString());
    donorRequest.getSpecimens().forEach(x -> specimenService.create(donorRequest.getStudyId(), x));
    return id;
  }

  public CompositeDonorEntity read(@NonNull String id) {
    val donorResult = fullRepository.findById(id);
    checkServer(donorResult.isPresent(), getClass(), DONOR_DOES_NOT_EXIST,
      "The donor for donorId '%s' could not be read because it does not exist", id);
    val donor = donorResult.get();
    donor.setInfo(infoService.readNullableInfo(id));
    val nonProxy = new CompositeDonorEntity();
    nonProxy.setWithDonorEntity(donor);
    return nonProxy;
  }

  public CompositeDonorEntity readWithSpecimens(@NonNull String id) {
    val donor = read(id);
    populateInplace(donor);
    return donor;
  }

  public Set<CompositeDonorEntity> readByParentId(@NonNull String studyId) {
    studyService.checkStudyExist(studyId);
    val results = fullRepository.findAllByStudyId(studyId);
    val donorsBuilder = ImmutableSet.<CompositeDonorEntity>builder();
    for(val result: results){
      val d = new CompositeDonorEntity();
      d.setWithDonorEntity(result);
      donorsBuilder.add(d);
    }
    val donors = donorsBuilder.build();
    donors.forEach(this::populateInplace);
    return donors;
  }

  public boolean isDonorExist(@NonNull String id){
    return fullRepository.existsById(id);
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

  public void update(@NonNull String id, @NonNull Donor donorUpdate) {
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

  // TODO: [SONG-254] DeleteByParentId spec missing -- https://github.com/overture-stack/SONG/issues/254
  public void deleteByParentId(@NonNull String studyId) {
    studyService.checkStudyExist(studyId);
    fullRepository.findAllByStudyId(studyId)
        .forEach(d -> uncheckedDelete(d.getDonorId()));
  }

  public Optional<String> findByBusinessKey(@NonNull String studyId, @NonNull String submitterId){
    val donors = fullRepository.findAllByStudyIdAndDonorSubmitterId(studyId, submitterId);
    checkServer(donors.size() < 2, getClass(), DUPLICATE_DONOR_IDS,
        "Searching by studyId '%s' and donorSubmitterId '%s' resulted in more than 1 result (%s)",
        studyId, submitterId, donors.size());
    return donors.stream()
        .map(DonorEntity::getDonorId)
        .findFirst();
  }

  public String save(@NonNull CompositeDonorEntity donor) {
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

  private void populateInplace(CompositeDonorEntity donorEntity){
    specimenService.readByParentId(donorEntity.getDonorId())
        .forEach(donorEntity::addSpecimen);
  }

  private void uncheckedDelete(@NonNull String id) {
    specimenService.deleteByParentId(id);
    fullRepository.deleteById(id);
    infoService.delete(id);
  }

  private CompositeDonorEntity buildChildless(DonorEntity donorEntity){
    val cde = new CompositeDonorEntity();
    cde.setWithDonorEntity(donorEntity);
    return cde;
  }

}
