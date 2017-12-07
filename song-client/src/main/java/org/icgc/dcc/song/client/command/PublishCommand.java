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
import static java.nio.file.Files.readAllLines;
import static java.util.Objects.isNull;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Publish an analysis id" )
public class PublishCommand extends Command {

  @Parameter(names = { "-a", "--analysis-id" }, required = false)
  private String analysisId;

  @NonNull
  private Registry registry;

  @NonNull
  private Config config;

  @Parameter(names = { "-f", "--input-file" }, required = false, description = "Input file with a list of analysisIds to be published")
  private String analysisIdFile;

  @Parameter(names = { "-o", "--output-file" }, required = false, description = "The output filename for the batch report for analysisIds after being published")
  private String outputFile;

  @Override
  public void run() throws IOException {
    if (isNull(analysisId) && isNull(analysisIdFile)) {
      val analysisId = getJson().at("/analysisId").asText("");
      val status = registry.publish(config.getStudyId(), analysisId);
      save(status);
    } else if (!isNull(analysisId) && !isNull(analysisIdFile)){
      throw new IllegalStateException("the -u and -f switches are mutually exclusive");
    } else if (!isNull(analysisId)){
      val status = registry.publish(config.getStudyId(), analysisId);
      save(status);
    } else {
      val path = Paths.get(analysisIdFile);
      checkArgument(Files.exists(path), "The path '%s' does not exist", analysisIdFile);
      checkArgument(Files.isRegularFile(path), "The path '%s' is not a file", analysisIdFile);
      checkArgument(Files.isReadable(path), "The file '%s' is not readable", analysisIdFile);
      checkArgument(!isNull(outputFile), "The outputFile is not defined");
      val parentOutPath = Paths.get(outputFile).toAbsolutePath().getParent();
      checkArgument(parentOutPath.toFile().exists(), "The parent directory '%s' does not exist", parentOutPath.toAbsolutePath().toString());
      val arrayBuilder = JsonNodeBuilders.array();
      readAllLines(path, Charsets.UTF_8).stream()
          .map(this::getPublishState)
          .forEach(arrayBuilder::with);
      val summary = arrayBuilder.end();
      write(JsonUtils.toPrettyJson(summary).getBytes(), new File(this.outputFile));
    }
}

  @SneakyThrows
  private JsonNode getPublishState(String analysisId){
    val out = JsonNodeBuilders.object();
    try{
      val status = registry.publish(config.getStudyId(), analysisId);
      out.with("response", status.getOutputs());
      out.with("error", "");
    } catch(org.icgc.dcc.song.core.exceptions.ServerException se){
      val songError = se.getSongError();
      val errorId = songError.getErrorId();
      out.with("error", errorId);
      out.with("response", "");
    } catch(Throwable e){
      out.with("error", String.format("[%s]: %s", e.getClass().getSimpleName(), e.getMessage()));
      out.with("response", "");
    }
    return out.end();
  }


}
