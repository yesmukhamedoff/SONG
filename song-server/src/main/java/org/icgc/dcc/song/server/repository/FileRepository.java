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

import org.icgc.dcc.song.server.model.entity.file.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, String>{

//  int create( FileEntity f);
//
//  FileEntity read( String id);
//
//  int update( FileEntity file);
//
//  int delete( String id);
//
//  List<FileEntity> readByParentId( String study_id);
//
//  String findByBusinessKey( String analysisId,  String fileName);

    List<FileEntity> findAllByAnalysisIdAndFileName(String analysisId, String fileName);
}
