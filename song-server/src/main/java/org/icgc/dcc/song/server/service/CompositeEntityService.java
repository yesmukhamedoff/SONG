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

package org.icgc.dcc.song.server.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.server.model.CompositeEntity;
import org.icgc.dcc.song.server.model.entity.sample.CompositeSampleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CompositeEntityService {

  @Autowired
  private final SampleService sampleService;

  @Autowired
  private final SpecimenService specimenService;

  @Autowired
  private final DonorService donorService;

  public String save(String studyId, CompositeEntity s) {
    val idResult = sampleService.findByBusinessKey(studyId, s.getSampleSubmitterId());
    String id;
    if (!idResult.isPresent()) {
      s.setSpecimenId(getSampleParent(studyId, s));
      val sampleCreateRequest = new CompositeSampleEntity();
      sampleCreateRequest.setWithSampleEntity(s);
      id = sampleService.create(studyId, sampleCreateRequest);
    } else {
      id = idResult.get();
      sampleService.update(id, s);
    }
    return id;
  }

  private String getSampleParent(String studyId, CompositeEntity s) {
    val specimen = s.getSpecimen();
    val idResult = specimenService.findByBusinessKey(studyId, specimen.getSpecimenSubmitterId());
    String id;
    if (!idResult.isPresent()) {
      specimen.setDonorId(getSpecimenParent(studyId, s));
      id = specimenService.create(studyId, specimen);
    } else {
      id = idResult.get();
      s.setSpecimenId(id);
      specimenService.update(id, specimen);
    }
    return id;
  }

  private String getSpecimenParent(String studyId, CompositeEntity s) {
    return donorService.save(s.getDonor());
  }

  public CompositeEntity read(String sampleId) {
    val payload = new CompositeEntity();
    payload.setWithSampleEntity(sampleService.read(sampleId));
    payload.setSpecimen(specimenService.read(payload.getSpecimenId()));
    payload.setDonor(donorService.read(payload.getSpecimen().getDonorId()));
    return payload;
  }

}
