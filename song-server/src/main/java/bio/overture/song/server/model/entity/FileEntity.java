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

package bio.overture.song.server.model.entity;

import bio.overture.song.core.model.enums.AccessTypes;
import bio.overture.song.core.model.enums.FileTypes;
import bio.overture.song.core.model.file.File;
import bio.overture.song.core.model.file.FileData;
import bio.overture.song.server.model.Metadata;
import bio.overture.song.server.model.enums.TableAttributeNames;
import bio.overture.song.server.model.enums.TableNames;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

import static bio.overture.song.core.model.enums.AccessTypes.resolveAccessType;

@Entity
@Table(name = TableNames.FILE)
@Data
@Builder
@RequiredArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class FileEntity extends Metadata implements Serializable, FileData, File {

  @Id
  @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
  private String objectId;

  @Column(name = TableAttributeNames.STUDY_ID, nullable = false)
  private String studyId;

  @Column(name = TableAttributeNames.ANALYSIS_ID, nullable = false)
  private String analysisId;

  @Column(name = TableAttributeNames.NAME, nullable = false)
  private String fileName;

  @Column(name = TableAttributeNames.SIZE, nullable = false)
  private Long fileSize;

  @Column(name = TableAttributeNames.TYPE, nullable = false)
  private String fileType;

  @Column(name = TableAttributeNames.MD5, nullable = false)
  private String fileMd5sum;

  @Column(name = TableAttributeNames.ACCESS, nullable = false)
  private String fileAccess;

  public FileEntity(String objectId, String studyId, String analysisId, String fileName, Long fileSize,
      String fileType, String fileMd5sum, String fileAccess) {
    this.objectId = objectId;
    this.studyId = studyId;
    this.analysisId = analysisId;
    this.fileName = fileName;
    this.fileSize = fileSize;
    setFileType(fileType);
    this.fileMd5sum = fileMd5sum;
    setFileAccess(fileAccess);
  }

  public void setFileType(FileTypes type) {
    this.fileType =  type.toString();
  }

  public void setFileType(String type) {
    setFileType(FileTypes.resolveFileType(type));
  }

  public void setFileAccess(@NonNull AccessTypes access){
    this.fileAccess = access.toString();
  }

  public void setFileAccess(@NonNull String access){
    setFileAccess(resolveAccessType(access));
  }

}
