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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.stream;
import static org.icgc.dcc.common.core.util.Splitters.DOT;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static bio.overture.song.core.exceptions.ServerErrors.SEARCH_TERM_SYNTAX;
import static bio.overture.song.core.exceptions.ServerException.checkServer;

/**
 * Contains a key-value pair, as well as methods for parsing the key hierarchy in to chain of keys
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchTerm {

  private static final Splitter EQUALS_SPLITTER = Splitter.on('=');
  private static final List<String> EMPTY_STRING_LIST = ImmutableList.of();

  @NonNull @Getter private String key;

  @ApiModelProperty(notes = "Regex pattern")
  @NonNull @Getter private String value;

  private List<String> keyChain;

  public void setKey(String key){
    this.key = key.trim();
    this.keyChain = parseKeyChain(key);
  }


  public void setValue(String value){
    checkServer(!isNullOrEmpty(value),
        this.getClass(), SEARCH_TERM_SYNTAX ,
        "value for key '%s' must be non-empty and non-null", getKey());
    this.value = value;
  }

  /**
   * Get the Non leaf keys of the keyChain. I.e if the keyChain is "country,province,city", then the nonleaf keys are
   * country,province
   * @return list of strings
   */
  @JsonIgnore
  public List<String> getNonLeafKeys() {
    if (keyChain.size() < 1){
      return EMPTY_STRING_LIST;
    } else {
      return keyChain.stream()
          .limit(keyChain.size() - 1)
          .collect(toImmutableList());
    }
  }

  /**
   * Get the Leaf keys of the keyChain. I.e if the keyChain is "country,province,city", then the leaf key is "city"
   * @return string
   */
  @JsonIgnore
  public String getLeafKey() {
    return keyChain.get(keyChain.size() - 1);
  }

  private static List<String> parseKeyChain(String key){
    checkServer(key.matches(".*\\S.*"),
        SearchTerm.class, SEARCH_TERM_SYNTAX,
        "The key '%s' is not acceptable. There must be at least one non-whitespace character", key);
    return DOT.splitToList(key);
  }

  public static SearchTerm createSearchTerm(String key, String value){
    val st = new SearchTerm();
    st.setKey(key);
    st.setValue(value);
    return st;
  }

  public static SearchTerm parseSearchTerm(String keyValuePair){
    val list = EQUALS_SPLITTER.limit(2).splitToList(keyValuePair);
    return createSearchTerm(list.get(0), list.get(1));
  }

  public static List<SearchTerm> parseSearchTerms(String ... keyValuePairs){
    return stream(keyValuePairs)
        .map(SearchTerm::parseSearchTerm)
        .collect(toImmutableList());
  }

  public static List<SearchTerm> createMultiSearchTerms(@NonNull String key, @NonNull List<String> values) {
    return values.stream()
        .map(v -> createSearchTerm(key, v))
        .collect(toImmutableList());
  }

}
