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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.common.core.util.Joiners;
import org.icgc.dcc.song.client.cli.Status;
import org.icgc.dcc.song.client.register.Registry;
import org.icgc.dcc.song.core.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static java.nio.file.Files.find;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

@RequiredArgsConstructor
@Parameters(separators = "=", commandDescription = "Upload an analysis file, and get an upload id")
@Slf4j
public class UploadCommand extends Command {

  private static final String UPLOAD_ID = "uploadId";

  @Parameter(names = { "-f", "--files" }, description = "Files to use for upload")
  private List<String> fileNames = newArrayList();

  @Parameter(names = { "-d", "--directories" }, description = "Directories to search for files to upload. Will not search recursively")
  private List<String> dirNames = newArrayList();

  @Parameter(names = { "-a", "--async" },description = "Enables asynchronous validation")
  boolean isAsyncValidation = false;

  @Parameter(names = { "-o", "--output" },required = true, description = "Path to the output file. The output file contains a list of uploadIds for the corresponding input files")
  private String outputFile;

  @Parameter(names = { "-n", "--dry" }, description = "Dry run. Used to check the formatting of the input files")
  private boolean dry = false;

  @NonNull
  private Registry registry;

  @SneakyThrows
  @Override
  public void run() throws IOException {
    val opt = streamUploadContent();
    if (opt.isPresent()){
      val stream = opt.get();
      val writer = Files.newWriter(new File(outputFile),Charsets.UTF_8);
      val uploadIds = stream.map(x -> registry.upload(x, isAsyncValidation))
          .map(Status::getOutputs)
          .map(UploadCommand::getUploadIdFromStatus)
          .collect(toImmutableList());
      for (val uploadId : uploadIds){
        writer.write(uploadId);
        writer.write("\n");
      }
      writer.close();
    }
  }


  private static String getUploadIdFromStatus(String json){
    val node = convertToJsonNode(json);
    checkArgument(node.hasNonNull(UPLOAD_ID), "The field '%s' does not exist", UPLOAD_ID);
    return node.path(UPLOAD_ID).textValue();
  }

  @SneakyThrows
  private static JsonNode convertToJsonNode(String jsonString){
    return JsonUtils.readTree(jsonString);
  }

  private Optional<Stream<String>> streamUploadContent() throws IOException {
    if (fileNames.size() == 0 && dirNames.size() == 0 ) {
      val json=getJson();
      return Optional.of(stream(json.toString()));
    }

    val files = resolveFiles();
    checkFiles(files);
    if (dry){
      return Optional.empty();
    } else {
      return Optional.of(files.stream()
          .map(UploadCommand::convertToString));
    }

  }

  @Value
  public static class FileError{
    @NonNull private final String filename;
    @NonNull private final String exceptionType;
    @NonNull private final String exceptionMessage;

    public static FileError createFileError(String filename, String exceptionType,
        String exceptionMessage) {
      return new FileError(filename, exceptionType, exceptionMessage);
    }
  }

  private Set<File> resolveFiles(){
    val out = ImmutableSet.<File>builder();
    val filesWithIssues = ImmutableList.<File>builder();
    fileNames.stream()
        .map(File::new)
        .map(UploadCommand::verifyIsFile)
        .forEach(out::add);

    dirNames.stream()
        .map(File::new)
        .flatMap(UploadCommand::streamFilesFromDir)
        .forEach(out::add);
    return out.build();
  }

  private static void checkFiles(Iterable<File> files) {
    val errors = Lists.newArrayList();
    for (val file : files) {
      try {
        JsonUtils.readTree(convertToString(file));
      } catch (Exception e) {
        val fileError =
            FileError.createFileError(file.getAbsolutePath(), e.getClass().getCanonicalName(), e.getMessage());
        errors.add(fileError);
      }
    }
    if (errors.size() > 0){
      throw new RuntimeException("The following files could not be parsed by the jsonParser:\n"+ Joiners.NEWLINE.join(errors));
    }
  }

  private static File verifyIsFile(File f){
    checkArgument(f.exists(), "The input file '%s' does not exist", f.getName());
    checkArgument(f.isFile(), "The input path '%s' is not a file", f.getName());
    return f;
  }

  @SneakyThrows
  private static String convertToString(File file){
    return Files.toString(file, Charsets.UTF_8);
  }

  @SneakyThrows
  private static Stream<File> streamFilesFromDir(File dir){
    checkArgument(dir.exists(), "The input path '%s' does not exist", dir.getName());
    checkArgument(dir.isDirectory(), "The input path '%s' is not a directory", dir.getName());
    return find(dir.toPath(),1, (p, attr) -> attr.isRegularFile())
        .map(Path::toFile);
  }

}
