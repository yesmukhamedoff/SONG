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

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.file.File;
import org.icgc.dcc.song.server.model.entity.file.FileEntity;
import org.icgc.dcc.song.server.repository.AnalysisRepository;
import org.icgc.dcc.song.server.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.icgc.dcc.song.core.exceptions.ServerErrors.DUPLICATE_DONOR_IDS;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.FILE_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;

@Service
@NoArgsConstructor
public class FileService {

  @Autowired
  AnalysisRepository analysisRepository;
  @Autowired
  FileRepository repository;
  @Autowired
  FileInfoService infoService;
  @Autowired
  IdService idService;
  @Autowired
  StudyService studyService;

  public String create(@NonNull String analysisId, @NonNull String studyId, @NonNull File file) {
    studyService.checkStudyExist(studyId);

    val id = idService.generateFileId(analysisId, file.getFileName());

    val fileCreateRequest = new FileEntity();
    fileCreateRequest.setWith(id, studyId, analysisId, file);

    repository.save(fileCreateRequest);
    infoService.create(id, file.getInfoAsString());

    return id;
  }

  public boolean isFileExist(@NonNull String id){
    return repository.existsById(id);
  }

  public void checkFileExists(String id){
    fileNotFoundCheck(isFileExist(id), id);
  }

  public void checkFileExists(@NonNull FileEntity file){
    checkFileExists(file.getObjectId());
  }

  public FileEntity read(@NonNull String id) {
    val result = repository.findById(id);
    fileNotFoundCheck(result.isPresent(), id);
    val file = result.get();
    file.setInfo(infoService.readNullableInfo(id));
    return file;
  }

  //TODO: rtisma here should compare if a business key was changed. Likewise with the others
  public void update(@NonNull String id, @NonNull File f) {
    val file = read(id);
    file.setWithFile(f);
    repository.save(file);
    infoService.update(file.getObjectId(), f.getInfoAsString());
  }

  public void delete(@NonNull String id) {
    checkFileExists(id);
    repository.deleteById(id);
    infoService.delete(id);
  }

  public Optional<String> findByBusinessKey(@NonNull String analysisId, @NonNull String fileName){
    val results = repository.findAllByAnalysisIdAndFileName(analysisId, fileName);
    checkServer(results.size() < 2, getClass(), DUPLICATE_DONOR_IDS,
        "Searching by fileName '%s' and analysisId '%s' resulted in more than 1 result (%s)",
        fileName, analysisId, results.size());
    return results.stream()
        .map(FileEntity::getObjectId)
        .findFirst();
  }

  public  String save(@NonNull String analysisId, @NonNull String studyId, @NonNull File file) {
    studyService.checkStudyExist(studyId);
    val idResult  = findByBusinessKey(analysisId, file.getFileName());
    String id;
    if (!idResult.isPresent()) {
      id = create(analysisId, studyId, file);
    } else {
      id = idResult.get();
      update(id, file);
    }
    return id;
  }

  private static void fileNotFoundCheck(boolean expression, @NonNull String id){
    checkServer(expression, FileService.class, FILE_NOT_FOUND,
        "The File with objectId '%s' does not exist", id);
  }

}
