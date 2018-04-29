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
import org.icgc.dcc.song.server.model.entity.study.impl.CompositeStudyEntity;
import org.icgc.dcc.song.server.model.entity.study.impl.Study;
import org.icgc.dcc.song.server.model.entity.study.impl.StudyEntity;
import org.icgc.dcc.song.server.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.Thread.currentThread;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ALREADY_EXISTS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.STUDY_ID_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;
import static org.icgc.dcc.song.server.model.entity.study.impl.StudyEntity.createStudyEntity;

@Service
public class StudyService {

  private final StudyRepository fullRepository;
  private final StudyInfoService infoService;

  @Autowired
  public StudyService(
      @NonNull StudyRepository fullRepository,
      @NonNull StudyInfoService studyInfoService){
    this.infoService = studyInfoService;
    this.fullRepository = fullRepository;
  }

  @SneakyThrows
  public CompositeStudyEntity read(@NonNull String studyId) {
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
  public void create(@NonNull CompositeStudyEntity studyEntity) {
    internalSave(studyEntity, false);
  }

  //TODO: rtisma need to test
  public void create(@NonNull StudyEntity studyEntity) {
    create(buildEmpty(studyEntity));
  }

  //TODO: rtisma need to test
  public void create(@NonNull String id, @NonNull Study study) {
    create(createStudyEntity(id, study));
  }

  //TODO: rtisma need to test
  public void update(@NonNull CompositeStudyEntity compositeStudyEntity) {
    internalSave(compositeStudyEntity, true);
  }

  //TODO: rtisma need to test
  public void update(@NonNull StudyEntity studyEntity) {
    update(buildEmpty(studyEntity));
  }

  //TODO: rtisma need to test
  public void update(@NonNull String id, @NonNull Study study) {
    update(createStudyEntity(id, study));
  }

  public List<String> findAllStudies() {
    return fullRepository.findAll().stream()
        .map(StudyEntity::getStudyId)
        .collect(toImmutableList());
  }

  @SneakyThrows
  public void checkStudyExist(@NonNull String studyId){
    val previousCallingClass = Class.forName(currentThread().getStackTrace()[2].getClassName());
    checkServer(isStudyExist(studyId), previousCallingClass, STUDY_ID_DOES_NOT_EXIST,
        "The studyId '%s' does not exist", studyId);
  }

  private void internalSave(CompositeStudyEntity compositeStudyEntity, boolean isUpdate) {
    // Check study existence depending on if this is an update
    val id = compositeStudyEntity.getStudyId();
    if (isUpdate){
      checkStudyExist(compositeStudyEntity.getStudyId());
    } else {
      checkServer(!isStudyExist(id), getClass(), STUDY_ALREADY_EXISTS,
          "The studyId '%s' already exists. Cannot save the study: %s " ,
          id,
          compositeStudyEntity);
    }

    fullRepository.save(compositeStudyEntity);

    // Update or Create study info data
    if(isUpdate) {
      infoService.update(id, compositeStudyEntity.getInfoAsString());
    } else {
      infoService.create(id, compositeStudyEntity.getInfoAsString());
    }
  }

  private CompositeStudyEntity buildEmpty(StudyEntity studyEntity){
    val s = new CompositeStudyEntity();
    s.setWithStudyEntity(studyEntity);
    return s;
  }

}
