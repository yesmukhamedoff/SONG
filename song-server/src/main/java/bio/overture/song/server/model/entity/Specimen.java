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

import static bio.overture.song.server.model.enums.Constants.SPECIMEN_CLASS;
import static bio.overture.song.server.model.enums.Constants.SPECIMEN_TYPE;
import static bio.overture.song.server.model.enums.Constants.validate;

@Entity
@Table(name = TableNames.SPECIMEN)
@Data
@Builder
@RequiredArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class Specimen extends Metadata {

  @Id
  @Column(name = TableAttributeNames.ID,
      updatable = false, unique = true, nullable = false)
  private String specimenId;

  @Column(name = TableAttributeNames.DONOR_ID, nullable = false)
  private String donorId;

  @Column(name = TableAttributeNames.SUBMITTER_ID, nullable = false)
  private String specimenSubmitterId;

  @Column(name = TableAttributeNames.CLASS, nullable = false)
  private String specimenClass;

  @Column(name = TableAttributeNames.TYPE, nullable = false)
  private String specimenType;

  public Specimen(String specimenId, String donorId, String specimenSubmitterId, String specimenClass,
      String specimenType) {
    this.specimenId = specimenId;
    this.donorId = donorId;
    this.specimenSubmitterId = specimenSubmitterId;
    setSpecimenClass(specimenClass);
    setSpecimenType(specimenType);
  }

  public void setSpecimenClass(String specimenClass) {
    validate(SPECIMEN_CLASS, specimenClass);
    this.specimenClass = specimenClass;
  }

  public void setSpecimenType(String type) {
    validate(SPECIMEN_TYPE, type);
    specimenType = type;
  }

  public void setWithSpecimen(@NonNull Specimen specimenUpdate){
    setSpecimenSubmitterId(specimenUpdate.getSpecimenSubmitterId());
    setDonorId(specimenUpdate.getDonorId());
    setSpecimenClass(specimenUpdate.getSpecimenClass());
    setSpecimenType(specimenUpdate.getSpecimenType());
    setSpecimenId(specimenUpdate.getSpecimenId());
    setInfo(specimenUpdate.getInfo());
  }

}
