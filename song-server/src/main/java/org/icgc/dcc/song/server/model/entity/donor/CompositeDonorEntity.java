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

package org.icgc.dcc.song.server.model.entity.donor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.icgc.dcc.song.server.model.entity.specimen.CompositeSpecimenEntity;
import org.icgc.dcc.song.server.model.enums.LombokAttributeNames;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

@Entity
@Table(name = TableNames.DONOR)
@Data
@EqualsAndHashCode(callSuper = true, exclude = {
    LombokAttributeNames.specimens
})
@ToString(callSuper = true, exclude = {
    LombokAttributeNames.specimens
})
@JsonPropertyOrder({
    ModelAttributeNames.DONOR_ID,
    ModelAttributeNames.DONOR_SUBMITTER_ID,
    ModelAttributeNames.STUDY_ID,
    ModelAttributeNames.DONOR_GENDER,
    ModelAttributeNames.SPECIMENS,
    ModelAttributeNames.INFO })
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CompositeDonorEntity extends DonorEntity {

  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY)
  @JoinColumn(name = TableAttributeNames.DONOR_ID)
  private Set<CompositeSpecimenEntity> specimens = newHashSet();


  public CompositeDonorEntity addSpecimen(@NonNull CompositeSpecimenEntity specimen){
    this.specimens.add(specimen);
    return this;
  }

}
