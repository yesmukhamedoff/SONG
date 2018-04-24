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

package org.icgc.dcc.song.server.model.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.JsonAttributeNames;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.ModelAttributeNames;
import org.icgc.dcc.song.server.model.analysis.BaseAnalysis;
import org.icgc.dcc.song.server.model.entity.study.Study;
import org.icgc.dcc.song.server.model.enums.AccessTypes;
import org.icgc.dcc.song.server.model.enums.Constants;
import org.icgc.dcc.song.server.model.enums.TableNames;
import org.icgc.dcc.song.server.repository.TableAttributeNames;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

import static org.icgc.dcc.song.server.model.enums.AccessTypes.resolveAccessType;

@Entity
@Table(name = TableNames.FILE)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@ToString(callSuper = true, exclude = {
    ModelAttributeNames.ANALYSIS,
    ModelAttributeNames.STUDY
})
@Data
public class File extends Metadata implements Serializable {

  @Id
  @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
  private String objectId;

  @JsonIgnore
  @ManyToOne(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY)
  @JoinColumn(name = TableAttributeNames.ANALYSIS_ID, nullable = false)
  private BaseAnalysis analysis;

  @JsonIgnore
  @ManyToOne(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY)
  @JoinColumn(name = TableAttributeNames.STUDY_ID, nullable = false)
  private Study study;

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

  @JsonGetter(value = JsonAttributeNames.ANALYSIS_ID)
  public String getAnalysisId(){
    return getAnalysis().getAnalysisId();
  }

  @JsonGetter(value = JsonAttributeNames.STUDY_ID)
  public String getStudyId(){
    return getStudy().getStudyId();
  }

  public static File create(String id, String name, Long size,
                            String type, String md5, AccessTypes access) {
    val f = new File();
    f.setObjectId(id);
    f.setFileName(name);
    f.setFileSize(size);
    f.setFileType(type);
    f.setFileMd5sum(md5);
    f.setFileAccess(access);
    return f;
  }

  public void setFileType(String type) {
    Constants.validate(Constants.FILE_TYPE, type);
    fileType = type;
  }

  public void setFileAccess(@NonNull AccessTypes access){
    this.fileAccess = access.toString();
  }

  public void setFileAccess(@NonNull String access){
    setFileAccess(resolveAccessType(access));
  }

}
