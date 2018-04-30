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

package org.icgc.dcc.song.server.model.entity.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.enums.AccessTypes;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = TableNames.FILE)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@ToString(callSuper = true)
@Data
public class FileEntity extends File implements Serializable {

  @Id
  @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
  private String objectId;

  @Column(name = TableAttributeNames.STUDY_ID,  nullable = false)
  private String studyId;

  @Column(name = TableAttributeNames.ANALYSIS_ID, nullable = false)
  private String analysisId;

//  @JsonIgnore
//  @ManyToOne(cascade = CascadeType.ALL,
//      fetch = FetchType.LAZY)
//  @JoinColumn(name = TableAttributeNames.ANALYSIS_ID, nullable = false)
//  private AbstractAnalysis analysis;

//  @JsonIgnore
//  @ManyToOne(cascade = CascadeType.ALL,
//      fetch = FetchType.LAZY)
//  @JoinColumn(name = TableAttributeNames.STUDY_ID, nullable = false)
//  private CompositeStudyEntity study;

  public void setWithFileEntity(@NonNull FileEntity file){
    setWith(file.getObjectId(), file.getStudyId(), file.getAnalysisId(), file);
  }

  public void setWith(String objectId, String studyId, String analysisId, @NonNull File file){
    setObjectId(objectId);
    setStudyId(studyId);
    setAnalysisId(analysisId);
    setWithFile(file);
  }

  public static FileEntity create(String id, String name, Long size,
                            String type, String md5, AccessTypes access) {
    val f = new FileEntity();
    f.setObjectId(id);
    f.setFileName(name);
    f.setFileSize(size);
    f.setFileType(type);
    f.setFileMd5sum(md5);
    f.setFileAccess(access);
    return f;
  }

}
