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
package org.icgc.dcc.song.server.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.google.common.base.Joiner;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.Study;
import org.icgc.dcc.song.server.model.enums.TableNames;
import org.icgc.dcc.song.server.model.enums.UploadStates;
import org.icgc.dcc.song.server.repository.TableAttributeNames;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static org.icgc.dcc.song.server.model.enums.UploadStates.resolveState;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({
    ModelAttributeNames.ANALYSIS_ID,
    ModelAttributeNames.UPLOAD_ID,
    ModelAttributeNames.STUDY_ID,
    ModelAttributeNames.STATE,
    ModelAttributeNames.CREATED_AT,
    ModelAttributeNames.UPDATED_AT,
    ModelAttributeNames.ERRORS,
    ModelAttributeNames.PAYLOAD
})
@Data
@Entity
@Table(name = TableNames.UPLOAD)
@ToString(exclude = { ModelAttributeNames.STUDY })
public class Upload {

  private static final String ERROR_DELIM = "\\|";

  @Id
  @Column(name = TableAttributeNames.ID,
      updatable = false, unique = true, nullable = false)
  private String uploadId;

  @JsonIgnore
  @ManyToOne(cascade = CascadeType.ALL,
      fetch = FetchType.EAGER)
  @JoinColumn(name = TableAttributeNames.STUDY_ID, nullable = false)
  private Study study;

  @Column(name = TableAttributeNames.STATE, nullable = false)
  private String state;

  @Column(name = TableAttributeNames.ANALYSIS_ID, nullable = true)
  private String analysisId;

  @Column(name = TableAttributeNames.ERRORS, nullable = true)
  private String errors;

  @Column(name = TableAttributeNames.PAYLOAD, nullable = false)
  private String payload;

  @Column(name = TableAttributeNames.CREATED_AT,updatable = false, nullable = false)
  private LocalDateTime createdAt;

  @Column(name = TableAttributeNames.UPDATED_AT, nullable = false)
  private LocalDateTime updatedAt;

  @JsonGetter(value = JsonAttributeNames.STUDY_ID)
  public String getStudyId(){
    return getStudy().getStudyId();
  }

  public void setState(@NonNull UploadStates state){
    this.state = state.getText();
  }

  public void setState(@NonNull String state){
    setState(resolveState(state));
  }

  public static Upload createUpload(String id, String analysisId, UploadStates state, String errors,
      String payload, LocalDateTime created, LocalDateTime updated) {
    val u = new Upload();
    u.setUploadId(id);
    u.setAnalysisId(analysisId);
    u.setState(state);
    u.setErrors(errors);
    u.setPayload(payload);
    u.setCreatedAt(created);
    u.setUpdatedAt(updated);
    return u;
  }

  @JsonRawValue
  public String getPayload() {
    return payload;
  }

  public void setErrors(String errorString) {
    this.errors = errorString;
    if (isNull(errorString)) {
      this.errors = "";
    }
  }

  public Collection<String> getErrors(){
    if(isNull(errors)){
      return newArrayList();
    } else {
      return asList(errors.split(ERROR_DELIM));
    }
  }

  public void addErrors(Collection<String> errors) {
    val e = getErrors();
    e.addAll(errors);
    this.errors = Joiner.on(ERROR_DELIM).join(e);
  }


}
