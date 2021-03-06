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

package bio.overture.song.server.service.export;

import bio.overture.song.core.model.ExportedPayload;
import bio.overture.song.core.model.enums.AnalysisStates;
import bio.overture.song.server.model.analysis.AbstractAnalysis;
import bio.overture.song.server.model.analysis.SequencingReadAnalysis;
import bio.overture.song.server.model.analysis.VariantCallAnalysis;
import bio.overture.song.server.service.AnalysisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.groupingBy;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static bio.overture.song.core.model.ExportedPayload.createExportedPayload;
import static bio.overture.song.core.utils.JsonUtils.readTree;
import static bio.overture.song.core.utils.JsonUtils.toPrettyJson;
import static bio.overture.song.server.model.enums.AnalysisTypes.SEQUENCING_READ;
import static bio.overture.song.server.model.enums.AnalysisTypes.VARIANT_CALL;
import static bio.overture.song.server.model.enums.AnalysisTypes.resolveAnalysisType;
import static bio.overture.song.server.service.export.PayloadConverter.createPayloadConverter;
import static bio.overture.song.server.service.export.PayloadParser.createPayloadParser;

@Service
public class ExportService {

  private static final Set<String> ALL_ANALYSIS_STATES = stream(AnalysisStates.values())
      .map(AnalysisStates::toString)
      .collect(toImmutableSet());

  @Autowired
  private AnalysisService analysisService;

  @SneakyThrows
  public List<ExportedPayload> exportPayload(@NonNull List<String> analysisIds,
      boolean includeAnalysisId){
    val analysisMap = aggregateByStudy(analysisIds);
    return analysisMap.entrySet().stream()
        .map(e -> buildExportedPayload(e.getKey(), e.getValue(), includeAnalysisId))
        .collect(toImmutableList());
  }

  @SneakyThrows
  public List<ExportedPayload> exportPayloadsForStudy(@NonNull String studyId,
      boolean includeAnalysisId){
    val payloads = analysisService.getAnalysisByView(studyId, ALL_ANALYSIS_STATES).stream()
        .map(x -> convertToPayload(x, includeAnalysisId))
        .collect(toImmutableList());
    return ImmutableList.of(createExportedPayload(studyId, payloads));
  }

  private Map<String, List<AbstractAnalysis>> aggregateByStudy(List<String> analysisIds){
    return analysisIds.stream()
        .map(analysisService::unsecuredDeepRead)
        .collect(groupingBy(AbstractAnalysis::getStudy));
  }

  private static ExportedPayload buildExportedPayload(String studyId, List<AbstractAnalysis> analyses,
      boolean includeAnalysisId){
    val payloads = analyses.stream()
        .map(x -> convertToPayload(x, includeAnalysisId))
        .collect(toImmutableList());
    return createExportedPayload(studyId, payloads);
  }

  @SneakyThrows
  private static JsonNode convertToPayload(@NonNull AbstractAnalysis a, boolean includeAnalysisId) {
    JsonNode output;
    val analysisType = resolveAnalysisType(a.getAnalysisType());
    if (analysisType == SEQUENCING_READ) {
      val seqRead = (SequencingReadAnalysis) a;
      output = readTree(toPrettyJson(seqRead));
    } else if (analysisType == VARIANT_CALL) {
      val varCall = (VariantCallAnalysis) a;
      output = readTree(toPrettyJson(varCall));
    } else {
      throw new IllegalStateException(
          format("Should not be here, unsupported analysisType '%s'",
              a.getAnalysisType()));
    }

    val payloadConverter = createPayloadConverter(includeAnalysisId);
    val payloadParser = createPayloadParser(output);
    return payloadConverter.convert(payloadParser);
  }

}
