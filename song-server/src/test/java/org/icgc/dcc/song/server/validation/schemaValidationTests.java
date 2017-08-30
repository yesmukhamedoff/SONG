/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.icgc.dcc.song.server.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

//import org.junit.Before;
//import com.sun.tools.javac.util.List;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class schemaValidationTests {

  @Test
  public void validate_submit_sequencing_read_happy_path() {
    val errors = validate("json-schemas/analysis.json", "documents/sequencingread-valid.json");

    assertThat(errors.isEmpty());
  }

  @Test
  public void validate_submit_variant_call_invalid_enum() {
    val errors = validate("json-schemas/analysis.json", "documents/variantcall-invalid-enum.json");
    val paths = getPaths(errors);

    assertThat(paths).isEqualTo(list(
        "$.sample[0].donor.donorGender",
        "$.sample[0].sampleType",
        "$.sample[0].specimen.specimenType",
        "$.sample[0].specimen.specimenClass")
    );
  }

  private List<String> getPaths(List<ValidationMessage> errors) {
    return errors.stream().map(ValidationMessage::getPath).collect(Collectors.toList());
  }


  @Test
  public void validate_submit_sequencing_read_missing_required()  {
    val errors = validate("json-schemas/analysis.json",
            "documents/sequencingread-missing-required.json");
    val paths = errors.stream().map(ValidationMessage::getPath).collect(Collectors.toList());
    assertThat(paths).isEqualTo(list( "$.file[0]", "$", "$.sample[0]"));
  }

  @Test
  public void validate_submit_sequencing_read_invalid_enum()  {
    val errors =
        validate("json-schemas/analysis.json", "documents/sequencingread-invalid-enum.json");
    val paths = errors.stream().map(f->{return f.getPath();}).collect(Collectors.toList());

    assertThat(paths).isEqualTo(list(
        "$.sample[0].donor.donorGender",
        "$.sample[0].sampleType",
        "$.sample[0].specimen.specimenType",
        "$.sample[0].specimen.specimenClass"));
  }

  @Test
  public void validate_submit_variant_call_happy_path()  {
    val errors = validate("json-schemas/analysis.json", "documents/variantcall-valid.json");
    assertThat(errors.isEmpty());
  }

  @Test
  public void validate_submit_variant_call_missing_required()  {
    val errors =
        validate("json-schemas/analysis.json", "documents/variantcall-missing-required.json");
    val paths = errors.stream().map(f->{return f.getPath();}).collect(Collectors.toList());
    assertThat(paths).isEqualTo(list( "$", "$", "$.sample[0]"));

  }

  private <T> List<T> list(T... values) {
    return Arrays.asList(values);
  }


  @SneakyThrows
  protected List<ValidationMessage> validate(String schemaFile, String documentFile)  {
    JsonSchema schema = getJsonSchemaFromClasspath(schemaFile);
    JsonNode node = getJsonNodeFromClasspath(documentFile);
    val errors = schema.validate(node);
    if (errors.size() > 0) {
      log.debug(errors.toString());
    }

    return Arrays.asList(errors.toArray(new ValidationMessage[0]));
  }

  protected JsonSchema getJsonSchemaFromClasspath(String name)  {
    JsonSchemaFactory factory = new JsonSchemaFactory();
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    JsonSchema schema = factory.getSchema(is);
    return schema;
  }

  @SneakyThrows
  protected JsonNode getJsonNodeFromClasspath(String name)  {
    InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(is1);
    return node;
  }
}
