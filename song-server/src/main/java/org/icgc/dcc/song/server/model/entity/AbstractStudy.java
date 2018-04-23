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

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.repository.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@EqualsAndHashCode(callSuper=true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@MappedSuperclass
public class AbstractStudy extends Metadata implements Study {

  @Id
  @Column(name = TableAttributeNames.ID,
      updatable = false, unique = true, nullable = false)
  private String studyId;

  @Column(name = TableAttributeNames.NAME, nullable = false)
  private String name;

  @Column(name = TableAttributeNames.ORGANIZATION, nullable = false)
  private String organization;

  @Column(name = TableAttributeNames.DESCRIPTION, nullable = false)
  private String description;

}
