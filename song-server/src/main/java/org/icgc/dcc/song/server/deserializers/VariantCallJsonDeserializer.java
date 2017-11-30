package org.icgc.dcc.song.server.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;
import org.icgc.dcc.song.server.model.experiment.VariantCall;

import java.io.IOException;

import static org.icgc.dcc.song.server.EntityFieldNames.ANALYSIS_ID;
import static org.icgc.dcc.song.server.EntityFieldNames.MATCHED_NORMAL_SAMPLE_SUBMITTER_ID;
import static org.icgc.dcc.song.server.EntityFieldNames.VARIANT_CALLING_TOOL;
import static org.icgc.dcc.song.server.deserializers.FieldEvaluator.createOptionalFieldEvaluator;

public class VariantCallJsonDeserializer extends JsonDeserializer<VariantCall> {

  @Override
  public VariantCall deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException, JsonProcessingException {
    val node = (JsonNode)jsonParser.getCodec().readTree(jsonParser);
    val optionalFieldEvaluator = createOptionalFieldEvaluator(node);
    return VariantCall.create(
        optionalFieldEvaluator.getString(ANALYSIS_ID),
        optionalFieldEvaluator.getString(VARIANT_CALLING_TOOL),
        optionalFieldEvaluator.getString(MATCHED_NORMAL_SAMPLE_SUBMITTER_ID)
    );
  }

}
