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
package org.icgc.dcc.song.server.model.entity.composites;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.ModelAttributeNames;
import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.model.analysis.BaseAnalysis;
import org.icgc.dcc.song.server.model.entity.AbstractStudy;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.isNull;

@EqualsAndHashCode(callSuper=true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@ToString(callSuper = true, exclude = {
    ModelAttributeNames.DONORS,
    ModelAttributeNames.ANALYSES,
    ModelAttributeNames.UPLOADS,
    ModelAttributeNames.FILES})
@Entity
@Table(name = TableNames.STUDY)
public class CompositeStudy extends AbstractStudy {

//  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = ModelAttributeNames.STUDY)
  private List<File> files = newArrayList();

//  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = ModelAttributeNames.STUDY)
  private List<BaseAnalysis> analyses = newArrayList();

//  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = ModelAttributeNames.STUDY)
  private List<Upload> uploads = newArrayList();

//  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = ModelAttributeNames.STUDY)
  private List<CompositeDonor> donors;

  public List<CompositeDonor> getDonors(){
    initDonor();
    return donors;
  }

  public CompositeStudy withDonor(@NonNull CompositeDonor compositeDonor){
    initDonor();
    donors.add(compositeDonor);
    if (compositeDonor.getStudy() != this){
      compositeDonor.setStudy(this);
    }
    return this;
  }

  private void initDonor(){
    if (isNull(donors)){
      donors = newArrayList();
    }
  }

  public static CompositeStudy create(@NonNull String id, String name, String org, String desc) {
    val s = new CompositeStudy();
    s.setStudyId(id);
    s.setName(name);
    s.setOrganization(org);
    s.setDescription(desc);
    return s;
  }

}
