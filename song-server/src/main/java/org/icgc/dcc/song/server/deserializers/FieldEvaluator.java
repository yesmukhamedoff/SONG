package org.icgc.dcc.song.server.deserializers;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.function.Function;

import static java.util.Objects.isNull;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.JSON_FIELD_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.REQUIRED_JSON_FIELD_IS_EMPTY;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.REQUIRED_JSON_FIELD_IS_NULL;
import static org.icgc.dcc.song.core.exceptions.ServerException.checkServer;

@RequiredArgsConstructor
public class FieldEvaluator {

  private final JsonNode node;
  private final boolean isOptional;

  public String getString(String fieldName) {
    val value = getField(fieldName, JsonNode::textValue);
    if (!isNull(value)){
      val isEmpty = value.isEmpty();
      checkServer(!isEmpty , this.getClass(), REQUIRED_JSON_FIELD_IS_EMPTY,"The required field "
          + "'%s' cannot be empty ", fieldName );
    }
    return value;
  }

  public Boolean getBoolean(String fieldName){
    return getField(fieldName, JsonNode::asBoolean);
  }

  public Long getLong(String fieldName){
    return getField(fieldName, JsonNode::asLong);
  }

  public Integer getInt(String fieldName){
    return getField(fieldName, JsonNode::asInt);
  }

  private <T> T getField(String fieldName, Function<JsonNode,T> function) {
    val path = node.path(fieldName);
    val doesFieldExist = !path.isMissingNode();

    if (isOptional) {
      if (doesFieldExist){
        return path.isNull() ? null : function.apply(path);
      }
      return null;
    } else {
      checkServer(doesFieldExist, this.getClass(), JSON_FIELD_DOES_NOT_EXIST, "The required field '%s' does not "
          + "exist", fieldName );

      val isNull =  path.isNull();
      checkServer(!isNull, this.getClass(), REQUIRED_JSON_FIELD_IS_NULL,"The required field "
          + "'%s' cannot be null ", fieldName );

      return function.apply(path);
    }
  }

  public static FieldEvaluator createFieldEvaluator(JsonNode node, boolean isOptional) {
    return new FieldEvaluator(node, isOptional);
  }

  public static FieldEvaluator createOptionalFieldEvaluator(JsonNode node) {
    return createFieldEvaluator(node, true);
  }

  public static FieldEvaluator createRequiredFieldEvaluator(JsonNode node) {
    return createFieldEvaluator(node, false);
  }


}
