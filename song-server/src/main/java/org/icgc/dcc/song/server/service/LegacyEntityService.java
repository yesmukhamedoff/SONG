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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.bohnman.squiggly.Squiggly;
import lombok.NonNull;
import lombok.val;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.converter.Converter;
import org.icgc.dcc.song.server.model.legacy.LegacyDto;
import org.icgc.dcc.song.server.model.legacy.LegacyEntity;
import org.icgc.dcc.song.server.repository.LegacyEntityRepository;
import org.icgc.dcc.song.server.utils.ParameterChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.icgc.dcc.common.core.util.Joiners.COMMA;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.LEGACY_ENTITY_NOT_FOUND;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;

@Service
public class LegacyEntityService {

  private static final String FIELDS = "fields";
  private static final String PAGE = "page";
  private static final String SIZE = "size";
  private static final Set<String> NON_LEGACY_ENTITY_PARAMS = newHashSet(FIELDS, PAGE, SIZE);
  private static final String SQUIGGLY_ALL_FILTER = "**";

  /**
   * Dependencies
   */
  private final LegacyEntityRepository legacyEntityRepository;
  private final ParameterChecker parameterChecker;
  private final Converter<LegacyEntity, LegacyDto> legacyDtoConverter;
  private final Converter<LegacyDto, LegacyEntity> legacyEntityConverter;

  @Autowired
  public LegacyEntityService(
      @NonNull LegacyEntityRepository legacyEntityRepository,
      @NonNull ParameterChecker parameterChecker,
      @NonNull Converter<LegacyEntity, LegacyDto> legacyDtoConverter,
      @NonNull Converter<LegacyDto, LegacyEntity> legacyEntityConverter) {
    this.legacyEntityRepository = legacyEntityRepository;
    this.parameterChecker = parameterChecker;
    this.legacyEntityConverter = legacyEntityConverter;
    this.legacyDtoConverter = legacyDtoConverter;
  }

  public LegacyDto getEntity( String id) {
    val result = legacyEntityRepository.findById(id);
    checkServer(result.isPresent(), getClass(), LEGACY_ENTITY_NOT_FOUND,
        "The LegacyEntity with id '%s' does not exist", id);
    return legacyDtoConverter.convert(result.get());
  }

  public JsonNode find( MultiValueMap<String, String> params, LegacyDto probe, Pageable pageable) {
    val queryParameters =  extractQueryParameters(params);
    val filterParameters = extractFilterParameters(params);

    parameterChecker.checkQueryParameters(LegacyEntity.class, queryParameters);
    parameterChecker.checkFilterParameters(LegacyEntity.class, filterParameters);

    val results = legacyEntityRepository.findAll(Example.of(legacyEntityConverter.convert(probe)), pageable);
    val filter = buildLegacyEntityPageFilter(filterParameters);
    val mapper = Squiggly.init(JsonUtils.mapper(), filter);
    return mapper.valueToTree(results);
  }

  private Set<String> extractQueryParameters(MultiValueMap<String, String> multiValueMap){
    return multiValueMap.keySet().stream()
        .filter(k -> !NON_LEGACY_ENTITY_PARAMS.contains(k))
        .collect(toImmutableSet());
  }

  private String buildLegacyEntityPageFilter(Set<String> filteredFieldNames){
    String filter = SQUIGGLY_ALL_FILTER;
    if (!filteredFieldNames.isEmpty()){
      filter = String.format("%s,content[%s]", SQUIGGLY_ALL_FILTER, COMMA.join(filteredFieldNames));
    }
    return filter;
  }

  private Set<String> extractFilterParameters(MultiValueMap<String, String> params){
    return params.entrySet().stream()
        .filter(x -> x.getKey().equals(FIELDS))
        .map(Map.Entry::getValue)
        .flatMap(Collection::stream)
        .map(Object::toString)
        .collect(toImmutableSet());
  }

  public static LegacyEntityService createLegacyEntityService(
      LegacyEntityRepository legacyEntityRepository,
      ParameterChecker parameterChecker,
      Converter<LegacyEntity, LegacyDto> legacyDtoConverter,
      Converter<LegacyDto, LegacyEntity> legacyEntityConverter) {
    return new LegacyEntityService(legacyEntityRepository, parameterChecker, legacyDtoConverter, legacyEntityConverter);
  }

}
