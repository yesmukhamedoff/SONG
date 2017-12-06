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
package org.icgc.dcc.song.client.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.common.core.json.JsonNodeBuilders;
import org.icgc.dcc.song.client.config.Config;
import org.icgc.dcc.song.client.register.Registry;
import org.icgc.dcc.song.core.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.io.Files.write;
import static java.util.Objects.isNull;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Save an uploaded analysis by it's upload id, and get the permanent analysis id")
public class SaveCommand extends Command {

  @Parameter(names = { "-u", "--upload-id" })
  private String uploadId;

  @Parameter(names = { "-i", "--ignore-id-collisions" },
      description = "Ignores analysisId collisions with ids from the IdService")
  boolean ignoreAnalysisIdCollisions = false;


  @Parameter(names = { "-f", "--input-file" }, description = "Input file containing a list of uploadIds to save. If one of the uploadIds is not VALIDATED, non will be saved")
  private String inputFilename;

  @Parameter(names = { "-o", "--output-file" }, description = "Output file containing a list of saved analysisIds")
  private String outputFilename;

  @NonNull
  private Registry registry;

  @NonNull
  private Config config;

  @Override
  public void run() throws IOException{
    if (isNull(uploadId) && isNull(inputFilename) ) {
      uploadId = getJson().at("/uploadId").asText("");
    } else if (!isNull(uploadId) && !isNull(inputFilename)){
      throw new IllegalStateException("the -u and -f switches are mutually exclusive");
    } else if (!isNull(uploadId)){
      val status = registry.save(config.getStudyId(), uploadId, ignoreAnalysisIdCollisions);
      save(status);
    } else {
      val path = Paths.get(inputFilename);
      checkArgument(Files.exists(path), "The path '%s' does not exist", inputFilename);
      checkArgument(Files.isRegularFile(path), "The path '%s' is not a file", inputFilename);
      checkArgument(Files.isReadable(path), "The file '%s' is not readable", inputFilename);
      checkArgument(!isNull(outputFilename), "The outputFile is not defined");
      val parentOutPath = Paths.get(outputFilename).toAbsolutePath().getParent();
      checkArgument(parentOutPath.toFile().exists(), "The parent directory '%s' does not exist", parentOutPath.toAbsolutePath().toString());
      val arrayBuilder = JsonNodeBuilders.array();
      Files.readAllLines(path, Charsets.UTF_8).stream()
          .map(this::getSaveState)
          .forEach(arrayBuilder::with);
      val summary = arrayBuilder.end();
      write(JsonUtils.toPrettyJson(summary).getBytes(), new File(this.outputFilename));
    }
  }

  @SneakyThrows
  private JsonNode getSaveState(String uploadId){
    val status = registry.save(config.getStudyId(), uploadId, ignoreAnalysisIdCollisions);
    return JsonUtils.readTree(status.getOutputs());
  }

}
