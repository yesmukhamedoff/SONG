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
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.study.impl.AbstractStudyEntity;
import org.icgc.dcc.song.server.model.entity.study.impl.FullStudyEntity;
import org.icgc.dcc.song.server.model.entity.study.impl.SterileStudyEntity;
import org.icgc.dcc.song.server.model.entity.study.impl.StudyImpl;
import org.icgc.dcc.song.server.repository.FullStudyRepository;
import org.icgc.dcc.song.server.repository.SterileStudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.Thread.currentThread;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.ENTITY_NOT_IMPLEMENTED;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerException.buildServerException;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.server.model.entity.study.impl.SterileStudyEntity.createSterileStudy;

@Service
public class StudyService {

  private final FullStudyRepository fullRepository;
  private final SterileStudyRepository sterileRepository;
  private final StudyInfoService infoService;

  @Autowired
  public StudyService(
      @NonNull FullStudyRepository fullRepository,
      @NonNull SterileStudyRepository sterileRepository,
      @NonNull StudyInfoService studyInfoService){
    this.infoService = studyInfoService;
    this.sterileRepository = sterileRepository;
    this.fullRepository = fullRepository;
  }

  @SneakyThrows
  public FullStudyEntity read(@NonNull String studyId) {
    val studyResponseResult = fullRepository.findById(studyId);
    checkServer(studyResponseResult.isPresent(), getClass(), STUDY_ID_DOES_NOT_EXIST,
        "The studyId '%s' does not exist", studyId);
    val info = infoService.readNullableInfo(studyId);
    val studyResponse = studyResponseResult.get();
    studyResponse.setInfo(info);
    return studyResponse;
  }

  public boolean isStudyExist(@NonNull String studyId){
    return fullRepository.existsById(studyId);
  }

  //TODO: rtisma need to test
  public void create(@NonNull String id, @NonNull StudyImpl studyRequest) {
    create(createSterileStudy(id, studyRequest));
  }

  //TODO: rtisma need to test
  public void create(@NonNull AbstractStudyEntity studyEntity) {
    internalSave(studyEntity, false);
  }

  //TODO: rtisma need to test
  public void update(@NonNull String id, @NonNull StudyImpl studyRequest) {
    update(createSterileStudy(id, studyRequest));
  }

  //TODO: rtisma need to test
  public void update(@NonNull AbstractStudyEntity studyEntity) {
    internalSave(studyEntity, true);
  }

  public List<String> findAllStudies() {
    return fullRepository.findAll().stream()
        .map(AbstractStudyEntity::getStudyId)
        .collect(toImmutableList());
  }

  @SneakyThrows
  public void checkStudyExist(@NonNull String studyId){
    val previousCallingClass = Class.forName(currentThread().getStackTrace()[2].getClassName());
    checkServer(isStudyExist(studyId), previousCallingClass, STUDY_ID_DOES_NOT_EXIST,
        "The studyId '%s' does not exist", studyId);
  }

  private void internalSave(AbstractStudyEntity studyEntity, boolean isUpdate) {
    // Check study existence depending on if this is an update
    val id = studyEntity.getStudyId();
    if (isUpdate){
      checkStudyExist(studyEntity.getStudyId());
    } else {
      checkServer(!isStudyExist(id), getClass(), STUDY_ALREADY_EXISTS,
          "The studyId '%s' already exists. Cannot save the study: %s " ,
          id,
          studyEntity);
    }

    //TODO: rtisma try to refactor this somehow when more mature
    // Save entity based on instance type. Not cleanest way of doing this
    if (studyEntity instanceof SterileStudyEntity){
      sterileRepository.save((SterileStudyEntity)studyEntity);
    } else if(studyEntity instanceof FullStudyEntity){
      fullRepository.save((FullStudyEntity)studyEntity);
    } else {
      throw buildServerException(getClass(), ENTITY_NOT_IMPLEMENTED,
          "Unimplemented subclass of AbstractStudyEntity: %s",
          studyEntity.getClass());
    }

    // Update or Create study info data
    if(isUpdate) {
      infoService.update(id, studyEntity.getInfoAsString());
    } else {
      infoService.create(id, studyEntity.getInfoAsString());
    }
  }


}
