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

package bio.overture.song.server.repository;

import bio.overture.song.server.model.entity.FullView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FullViewRepository extends JpaRepository<FullView, String> {

  List<FullView> findAllByAnalysisIdIn(List<String> analysisIds);
  List<FullView> findAllByStudyIdAndAnalysisStateIn(String studyId, List<String> analysisStates);

}
