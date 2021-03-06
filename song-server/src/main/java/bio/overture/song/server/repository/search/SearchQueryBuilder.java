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

package bio.overture.song.server.repository.search;

import bio.overture.song.server.model.enums.InfoSearchResponseColumns;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

/**
 * Mutable class that builds a search query for greedy regex searching key-value pairs in a table
 */
@RequiredArgsConstructor
public class SearchQueryBuilder {

  private static final String TABLE_NAME = "info";
  private static final String AND_DELIMITER = " AND ";
  private static final String JSON_OBJECT_ARROW = "->";
  private static final String JSON_VALUE_ARROW = "->>";
  private static final String REGEX_ASSIGNMENT = " ~ ";
  private static final String STATEMENT_END = ";";

  @NonNull private final String studyId;
  private final boolean includeInfoField;

  private final Set<SearchTerm> searchTerms = newLinkedHashSet();

  /**
   * Builds the query based on the current configuration
   * @return postgresql query string
   */
  public String build() {
    val query = generateSelectBaseQuery(includeInfoField, studyId);
    if (searchTerms.size() > 0){
      return query + AND_DELIMITER + generateWhereConditions() + STATEMENT_END;
    } else {
      return query + STATEMENT_END;
    }
  }

  /**
   * Adds a search term
   * @param searchTerm
   * @return this
   */
  public SearchQueryBuilder add(@NonNull SearchTerm searchTerm){
    this.searchTerms.add(searchTerm);
    return this;
  }

  /**
   * Adds a search term using a key-value pair as the argument
   * @param key String representing a hierarchy of keys separated by a DOT. I.e  key1.key2.key3
   * @param value greedy regex search value for the specified key
   * @return this
   */
  public SearchQueryBuilder add(@NonNull String key, @NonNull String value){
    return add(SearchTerm.createSearchTerm(key, value));
  }

  public SearchQueryBuilder add(@NonNull String key, @NonNull List<String> values){
    values.forEach(v -> add(key, v));
    return this;
  }

  private String generateWhereConditions(){
    return searchTerms.stream()
        .map(x -> convertToWhereCondition(TABLE_NAME, InfoSearchResponseColumns.INFO.toString(), x))
        .collect(joining(AND_DELIMITER));
  }

  public static SearchQueryBuilder createSearchQueryBuilder(String studyId, boolean includeInfoField){
    return new SearchQueryBuilder(studyId, includeInfoField);
  }

  private static String convertToWhereCondition(String tableName, String columnName, SearchTerm searchTerm){
    val sb = new StringBuilder();
    sb.append(tableName+"."+columnName);
    searchTerm.getNonLeafKeys().forEach(key -> sb.append(JSON_OBJECT_ARROW).append(surroundSingleQuotes(key)));
    sb.append(JSON_VALUE_ARROW)
        .append(surroundSingleQuotes(searchTerm.getLeafKey()))
        .append(REGEX_ASSIGNMENT)
        .append(surroundSingleQuotes(searchTerm.getValue()));
    return sb.toString();
  }

  private static String surroundSingleQuotes(String input){
    return format("'%s'", input);
  }

  private static String generateSelectBaseQuery(boolean includeInfoField, String studyId){
    val sb = new StringBuilder();
    sb.append(String.format("SELECT CAST(analysis.id AS VARCHAR) AS %s ", InfoSearchResponseColumns.ANALYSIS_ID.toString()));
    if (includeInfoField){
      sb.append(String.format(", CAST(info.info AS VARCHAR) AS %s ", InfoSearchResponseColumns.INFO.toString()));
    }
    sb.append(format("FROM analysis INNER JOIN %s ON analysis.id = info.id WHERE info.id_type = 'Analysis' AND analysis.study_id = '%s'", TABLE_NAME, studyId ));
    return sb.toString();
  }

}
