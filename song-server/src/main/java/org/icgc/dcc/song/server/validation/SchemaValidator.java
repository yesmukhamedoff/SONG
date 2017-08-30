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

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Potentially extract a Validator interface if we want to pursue a Strategy pattern of multiple validation rules or
 * something
 *
 */
@Slf4j
public class SchemaValidator {
  @Autowired
  private JsonSchema schema;

  @Autowired(required = false)
  private Long validationDelayMs = -1L;

  @SneakyThrows
  public ValidationResponse validate(String schemaId, JsonNode payloadRoot) {
      val results = schema.validate(payloadRoot);
      val response = new ValidationResponse(results);
      log.info(response.getValidationErrors());

      debugDelay();

      return response;
  }

  /**
   * Creates an artificial delay for testing purposes.
   * The validationDelayMs should be controlled through the Spring "test" profile
   */
  @SneakyThrows
  private void debugDelay(){
    if (validationDelayMs > -1){
      log.info("Sleeping for {} ms", validationDelayMs);
      Thread.sleep(validationDelayMs);
    }
  }
}
