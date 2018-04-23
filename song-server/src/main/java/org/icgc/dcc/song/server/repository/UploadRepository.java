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
package org.icgc.dcc.song.server.repository;

import lombok.val;
import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.model.entity.study.Study;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.server.model.enums.UploadStates.resolveState;

public interface UploadRepository extends JpaRepository<Upload, String> {

//  @SqlUpdate("INSERT INTO upload (id, study_id, analysis_id, state, payload, updated_at) " +
//          "VALUES (:id, :studyId, :analysisId, :state, :payload, now())")
  default int create( String id,  Study study,  String analysisId,
              String state,  String jsonPayload){
    val createdAt = LocalDateTime.now();
    val u = Upload.createUpload(id, analysisId, resolveState(state), "", jsonPayload, createdAt, createdAt);
    u.setStudy(study);
    save(u);
    return 1;
  }

//  @SqlQuery("SELECT id from upload where study_id=:studyId AND analysis_id=:analysisId")
  List<Upload> findAllByStudyAndAnalysisId(Study study, String analysisId);
  default List<String> findByBusinessKey( Study study, String analysisId){
    return findAllByStudyAndAnalysisId(study, analysisId).stream()
        .map(Upload::getUploadId).collect(
        toImmutableList());
  }

//  @SqlUpdate("UPDATE upload set payload=:payload, state=:state, updated_at = now() WHERE id=:id")
  default int update_payload( String id,  String state,  String payload){
    val result = findById(id);
    if (!result.isPresent()){
      return 0;
    }
    val upload = result.get();
    upload.setState(state);
    upload.setPayload(payload);
    save(upload);
    return 1;
  }

  // note: avoiding handling datetime's in application; keeping it all in the SQL (also, see schema)
//  @SqlUpdate("UPDATE upload SET state = :state, errors = :errors, updated_at = now() WHERE id = :id")
  default int update( String id,  String state,  String errors){
    val result = findById(id);
    if (!result.isPresent()){
      return 0;
    }
    val upload = result.get();
    upload.setState(state);
    upload.setErrors(errors);
    save(upload);
    return 1;
  }

//  @SqlQuery("SELECT id, analysis_id, study_id, state, created_at, updated_at, errors, payload FROM upload WHERE id = :uploadId")
  default Upload get( String id){
    return findById(id).orElse(null);
  }

}
