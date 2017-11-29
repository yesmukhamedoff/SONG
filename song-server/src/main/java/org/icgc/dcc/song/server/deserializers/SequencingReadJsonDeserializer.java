package org.icgc.dcc.song.server.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.val;
import org.icgc.dcc.song.server.model.experiment.SequencingRead;

import java.io.IOException;

import static org.icgc.dcc.song.server.deserializers.FieldEvaluator.createOptionalFieldEvaluator;
import static org.icgc.dcc.song.server.deserializers.FieldEvaluator.createRequiredFieldEvaluator;

public class SequencingReadJsonDeserializer extends JsonDeserializer<SequencingRead> {

  private static final String ANALYSIS_ID = "analysisId";
  private static final String ALIGNED = "aligned";
  private static final String ALIGNMENT_TOOL= "alignmentTool";
  private static final String INSERT_SIZE = "insertSize";
  private static final String LIBARARY_STRATEGY = "libraryStrategy";
  private static final String PAIRED_END = "pairedEnd";
  private static final String REFERENCE_GENOME = "referenceGenome";

  @Override
  public SequencingRead deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException, JsonProcessingException {
    val node = (JsonNode)jsonParser.getCodec().readTree(jsonParser);
    val optionalFieldEvaluator = createOptionalFieldEvaluator(node);
    val requiredFieldEvaluator = createRequiredFieldEvaluator(node);
    return SequencingRead.create(
        optionalFieldEvaluator.getString(ANALYSIS_ID),
        optionalFieldEvaluator.getBoolean(ALIGNED),
        optionalFieldEvaluator.getString(ALIGNMENT_TOOL),
        optionalFieldEvaluator.getInt(INSERT_SIZE ).longValue(),
        requiredFieldEvaluator.getString(LIBARARY_STRATEGY),
        optionalFieldEvaluator.getBoolean(PAIRED_END),
        optionalFieldEvaluator.getString(REFERENCE_GENOME)
    );
  }

}
