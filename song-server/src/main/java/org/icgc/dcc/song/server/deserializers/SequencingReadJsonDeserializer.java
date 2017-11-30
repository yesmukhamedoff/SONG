package org.icgc.dcc.song.server.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;
import org.icgc.dcc.song.server.model.experiment.SequencingRead;

import java.io.IOException;

import static org.icgc.dcc.song.server.EntityFieldNames.ALIGNED;
import static org.icgc.dcc.song.server.EntityFieldNames.ANALYSIS_ID;
import static org.icgc.dcc.song.server.EntityFieldNames.INSERT_SIZE;
import static org.icgc.dcc.song.server.EntityFieldNames.LIBARARY_STRATEGY;
import static org.icgc.dcc.song.server.EntityFieldNames.PAIRED_END;
import static org.icgc.dcc.song.server.EntityFieldNames.REFERENCE_GENOME;
import static org.icgc.dcc.song.server.deserializers.FieldEvaluator.createOptionalFieldEvaluator;
import static org.icgc.dcc.song.server.repository.TableAttributeNames.ALIGNMENT_TOOL;

public class SequencingReadJsonDeserializer extends JsonDeserializer<SequencingRead> {

  /**
   * [rtisma-20171129] - Since default jackson deserialization converts null boolean variable as false, the
   * optionalFieldEvaluator was created to ensure that if a boolean field has a null value, that a null
   * object of type Boolean is returned. Similarly with Long and Integer. The same occurs with String,
   * however there is an additional rule to also return a null object of type String when the string field
   * has an empty value. Assuming JsonSchema is still being used, the validation rules defined in the schemas
   * will always be more restrictive than the rule defined below, therefore, the optionalFieldEvaluator will
   * function properly even for required fields
   */
  @Override
  public SequencingRead deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException, JsonProcessingException {
    val node = (JsonNode)jsonParser.getCodec().readTree(jsonParser);
    val optionalFieldEvaluator = createOptionalFieldEvaluator(node);
    return SequencingRead.create(
        optionalFieldEvaluator.getString(ANALYSIS_ID),
        optionalFieldEvaluator.getBoolean(ALIGNED),
        optionalFieldEvaluator.getString(ALIGNMENT_TOOL),
        optionalFieldEvaluator.getLong(INSERT_SIZE),
        optionalFieldEvaluator.getString(LIBARARY_STRATEGY),
        optionalFieldEvaluator.getBoolean(PAIRED_END),
        optionalFieldEvaluator.getString(REFERENCE_GENOME)
    );
  }

}
