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

import lombok.Getter;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.sample.Sample;
import org.icgc.dcc.song.server.model.entity.Specimen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.server.repository.SampleRepository.SpecimenRequest.createSpecimenRequest;

public interface SampleRepository extends JpaRepository<Sample, String> {

  default int create( Sample sample){
    save(sample);
    return 1;
	}

  default Sample read( String id){
    return findById(id).orElse(null);
	}

  List<Sample> findAllBySpecimen(Specimen specimen);

  class SpecimenRequest extends Specimen{

    @Getter private String specimenClass;
    @Getter private String specimenType;

    @Override public void setSpecimenClass(String specimenClass) {
      this.specimenClass = specimenClass;
    }

    @Override public void setSpecimenType(String type) {
      this.specimenType = type;
    }

    public static SpecimenRequest createSpecimenRequest(String id, String submitterId, String specimenClass, String specimenType ){
      val s = new SpecimenRequest();
      s.setSpecimenId(id);
      s.setSpecimenSubmitterId(submitterId);
      s.setSpecimenClass(specimenClass);
      s.setSpecimenType(specimenType);
      return s;
    }

  }

  default List<Sample> readByParentId( String specimenId){
    val req = createSpecimenRequest(specimenId, null, null, null);
    return findAllBySpecimen(req);
	}

  default int update( Sample sample){
    val result = findById(sample.getSampleId());
    if(result.isPresent()){
      val readSample = result.get();
      if (!readSample.equals(sample)){
        save(sample);
        return 1;
      }
    }
    return 0;
	}

  default int update( String id,  Sample sample){
    return update(sample);
	}

	int deleteAllBySpecimen(Specimen specimen);

  default int delete( String id){
    val result = findById(id);
    if(result.isPresent()){
      val readSpecimen = result.get();
      delete(readSpecimen);
      return 1;
    }
    return 0;
	}



  default String deleteByParentId(String specimenId){
    val req = createSpecimenRequest(specimenId, null, null,null);
    deleteAllBySpecimen(req);
    return "";
	}


  default List<String> findByParentId( String specimen_id){
    return readByParentId(specimen_id).stream().map(Sample::getSampleId).collect(toImmutableList());
	}

  default String findByBusinessKey( String studyId,  String submitterId){
    throw new IllegalStateException("not implemented");
	}

}