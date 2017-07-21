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
import java.util.Set;

//import org.junit.Before;
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
  public void validate_submit_sequencing_read_happy_path() throws Exception {
    val errors =
        validate("schemas/sequencingRead.json", "documents/sequencingread-valid.json");
    assertThat(errors.size()).isEqualTo(0);
  }

  @Test
  public void validate_submit_sequencing_read_missing_required() throws Exception {
    val errors = validate("schemas/sequencingRead.json",
            "documents/sequencingread-missing-required.json");
    assertThat(errors.size()).isEqualTo(2);
  }

  @Test
  public void validate_submit_sequencing_read_invalid_enum() throws Exception {
    val errors =
        validate("schemas/sequencingRead.json", "documents/sequencingread-invalid-enum.json");
    assertThat(errors.size()).isEqualTo(4);
  }

  @Test
  public void validate_submit_variant_call_happy_path() throws Exception {
    val errors = validate("schemas/variantCall.json", "documents/variantcall-valid.json");
    assertThat(errors.size()).isEqualTo(0);
  }

  @Test
  public void validate_submit_variant_call_missing_required() throws Exception {
    val errors =
        validate("schemas/variantCall.json", "documents/variantcall-missing-required.json");
    assertThat(errors.size()).isEqualTo(3);
  }

  @Test
  public void validate_submit_variant_call_invalid_enum() throws Exception {
    val errors =
        validate("schemas/variantCall.json", "documents/variantcall-invalid-enum.json");
    assertThat(errors.size()).isEqualTo(4);
  }

  protected Set<ValidationMessage> validate(String schemaFile, String documentFile) throws Exception {
    JsonSchema schema = getJsonSchemaFromClasspath(schemaFile);
    JsonNode node = getJsonNodeFromClasspath(documentFile);
    val errors = schema.validate(node);
    if (errors.size() > 0) {
      for (val msg : errors) {
        log.error(String.format("Error code %s: %s ", msg.getCode(), msg.getMessage()));
      }
    }
    return errors;
  }

  protected JsonSchema getJsonSchemaFromClasspath(String name) throws Exception {
    JsonSchemaFactory factory = new JsonSchemaFactory();
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    JsonSchema schema = factory.getSchema(is);
    return schema;
  }

  protected JsonNode getJsonNodeFromClasspath(String name) throws Exception {
    InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(is1);
    return node;
  }
}