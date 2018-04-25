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
import org.icgc.dcc.song.server.model.entity.specimen.impl.FullSpecimenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecimenRepository extends JpaRepository<FullSpecimenEntity, String> {

  default int create( FullSpecimenEntity specimen){
    save(specimen);
    return 1;
	}

  default FullSpecimenEntity read( String id){
    return findById(id).orElse(null);
	}

  default int update( FullSpecimenEntity specimen){
    val result = findById(specimen.getSpecimenId());
    if(result.isPresent()){
      val readSpecimen = result.get();
      if (!readSpecimen.equals(specimen)){
        save(specimen);
        return 1;
      }
    }
    return 0;
	}

  default int update( String id,  FullSpecimenEntity specimen){
    return update(specimen);
	}

  default int delete( String id){
    val result = findById(id);
    if(result.isPresent()){
      val readSpecimen = result.get();
      delete(readSpecimen);
      return 1;
    }
    return 0;
	}

  default List<String> findByParentId( String donor_id){
    throw new IllegalStateException("not implemented");
	}

  default String findByBusinessKey( String studyId,  String submitterId){
    throw new IllegalStateException("not implemented");
	}

}
