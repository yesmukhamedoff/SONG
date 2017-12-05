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
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.client.register.Registry;
import org.icgc.dcc.song.core.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static java.nio.file.Files.find;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Upload an analysis file, and get an upload id")
@Slf4j
public class UploadCommand extends Command {

  @Parameter(names = { "-f", "--files" })
  private List<String> fileNames = newArrayList();

  @Parameter(names = { "-d", "--directories" })
  private List<String> dirNames = newArrayList();

  @Parameter(names = { "-a", "--async" },description = "Enables asynchronous validation")
  boolean isAsyncValidation = false;

  @Parameter(names = { "-o", "--output" })
  private String outputFile;

  @NonNull
  private Registry registry;

  @Override
  public void run() throws IOException {
    streamUploadContent()
        .map(x -> registry.upload(x, isAsyncValidation))
        .map(x -> x.getOutputs())
        .map(x -> JsonUtils.readTree())
        .forEach(this::save);
  }

  private JsonNode  upload(String payload){
    
    val status = registry.upload(payload, isAsyncValidation);



  }

  private Stream<String> streamUploadContent() throws IOException {
    if (fileNames.size() == 0 && dirNames.size() == 0 ) {
      val json=getJson();
      return stream(json.toString());
    }

    return resolveFiles().stream()
        .map(this::convertToString);
  }

  private Set<File> resolveFiles(){
    val out = ImmutableSet.<File>builder();
    fileNames.stream()
        .map(File::new)
        .map(this::verifyIsFile)
        .forEach(out::add);

    dirNames.stream()
        .map(File::new)
        .flatMap(UploadCommand::streamFilesFromDir)
        .forEach(out::add);

    return out.build();
  }

  private File verifyIsFile(File f){
    checkArgument(f.exists(), "The input file '%s' does not exist", f.getName());
    checkArgument(f.isFile(), "The input path '%s' is not a file", f.getName());
    return f;
  }

  @SneakyThrows
  private String convertToString(File file){
    log.info("Converting file '{}", file.getAbsolutePath().toString());
    return Files.toString(file, Charsets.UTF_8);
  }

  @SneakyThrows
  private static Stream<File> streamFilesFromDir(File dir){
    checkArgument(dir.exists(), "The input path '%s' does not exist", dir.getName());
    checkArgument(dir.isDirectory(), "The input path '%s' is not a directory", dir.getName());
    return find(dir.toPath(),1, (p, attr) -> attr.isRegularFile(), FileVisitOption.FOLLOW_LINKS)
        .map(Path::toFile);
  }

}
