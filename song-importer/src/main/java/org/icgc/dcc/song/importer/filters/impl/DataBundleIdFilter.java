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

package org.icgc.dcc.song.importer.filters.impl;

import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.song.importer.filters.Filter;

import java.util.Map;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableSet;
import static org.icgc.dcc.song.importer.filters.impl.IdFilter.createIdFilter;

@RequiredArgsConstructor(access = PRIVATE)
public class DataBundleIdFilter implements Filter<String> {

  @NonNull private final Filter<String> internalIdFilter;

  @NonNull private final Map<String, Boolean> passingIdMap;

  @Override
  public boolean isPass(String id) {
    val result = internalIdFilter.isPass(id);
    passingIdMap.put(id, result);
    return result;
  }

  public Set<String> getProcessedIds(){
    return passingIdMap.entrySet().stream()
        .filter(Map.Entry::getValue)
        .map(Map.Entry::getKey)
        .collect(toImmutableSet());
  }

  public Set<String> getUnProcessedIds(){
    return passingIdMap.entrySet().stream()
        .filter(x -> !x.getValue())
        .map(Map.Entry::getKey)
        .collect(toImmutableSet());
  }


  public static DataBundleIdFilter createDataBundleIdFilter(final boolean enable,
      Set<String> ids, final boolean isGoodIds ) {
    val internalIdFilter = enable ? createIdFilter(ids, isGoodIds) : Filter.<String>passThrough();
    val map = Maps.<String, Boolean>newHashMap();
    ids.forEach(x -> map.put(x, false));
    return new DataBundleIdFilter(internalIdFilter, map);
  }

}
