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
package org.icgc.dcc.song.server.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.icgc.dcc.song.core.utils.JsonUtils;
import org.icgc.dcc.song.server.model.Upload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.nio.file.Files;

import static java.lang.String.format;
import static java.lang.System.out;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
@FlywayTest
@ActiveProfiles({"dev", "secure", "test"})
public class UploadServiceTest {

  @Autowired
  UploadService uploadService;

  @Test
  public void testAsyncSequencingRead(){
    testSequencingRead(true);
  }

  @Test
  public void testSyncSequencingRead(){
    testSequencingRead(false);
  }

  @Test
  public void testAsyncVariantCall(){
    testVariantCall(true);
  }

  @Test
  public void testSyncVariantCall(){
    testVariantCall(false);
  }

  @Test
  public void testSyncUploadNoCreated(){
    val fileName = "sequencingRead.json";
    val study="ABC123";
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(study, json, false );
    log.info(format("Got uploadStatus='%s'",uploadStatus));
    val uploadId = fromStatus(uploadStatus,"uploadId");

    val upload = uploadService.read(uploadId);

    assertThat(upload.getState()).isNotEqualTo("CREATED"); //Since validation done synchronously, validation cannot ever return with the CREATED state
  }

  @SneakyThrows
  public String fromStatus( ResponseEntity<String> uploadStatus, String key) {
    val uploadId = JsonUtils.readTree(uploadStatus.getBody()).at("/"+key).asText("");
    return uploadId;
  }

  @Test
  public void testSyncUpload(){
    val fileName = "sequencingRead.json";
    val study="ABC123";
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(study, json, false );
    val uploadId = fromStatus(uploadStatus,"uploadId");
    val upload = uploadService.read(uploadId);
    assertThat(upload.getState()).isEqualTo("VALIDATED");
  }


  @Test
  public void testASyncUpload(){
    val fileName = "sequencingRead.json";
    val study="ABC123";
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(study, json, true );
    val uploadId = fromStatus(uploadStatus,"uploadId");
    val upload = uploadService.read(uploadId);
    assertThat(upload.getState()).isEqualTo("CREATED");
  }

  @SneakyThrows
  @Test public void testAsyncUpdate() {
    val fileName="updateAnalysisTest.json";
    val study="ABC123";
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(study, json, false );
    val uploadId = fromStatus(uploadStatus,"uploadId");
    log.info(format("UploadStatus='%s'",uploadStatus));

    val json2 = json.replace("MUSE variant call pipeline","Muslix popcorn");
    assertThat(json).isNotEqualTo(json2);
    val uploadStatus2 = uploadService.upload(study, json2, true);
    val uploadId2 =  fromStatus(uploadStatus,"uploadId");
    val status2 = fromStatus(uploadStatus2, "status");
    val replaced = fromStatus(uploadStatus2, "replaced");

    assertThat(replaced).isEqualTo(json);
    assertThat(uploadId).isEqualTo(uploadId2);
    assertThat(status2).isEqualTo("WARNING: replaced content for analysisSubmitterId 'A0001'");
    val upload = uploadService.read(uploadId2);
    assertThat(upload.getPayload()).isEqualTo(json2);
    assertThat(upload.getState()).isEqualTo("UPDATED");

    // test validation
    val finalState = validate(uploadId);
    assertThat(finalState).isEqualTo("VALIDATED");

    // test save
    val response = uploadService.save(study,uploadId);
    assertThat(response.getStatusCode()).isEqualTo(OK);
  }

  @SneakyThrows
  @Test public void testSyncUpdate() {
    val fileName="variantCallWithSubmitterId.json";
    val study="ABC123";
    val json = readFile(fileName);
    val uploadStatus = uploadService.upload(study, json, false );
    log.info(format("UploadStatus='%s'",uploadStatus));
    val uploadId = fromStatus(uploadStatus,"uploadId");
    val status = fromStatus(uploadStatus, "status");
    assertThat(status).isEqualTo("ok");

    val json2 = json.replace("silver bullet","golden hammer");
    assertThat(json).isNotEqualTo(json2);
    val uploadStatus2 = uploadService.upload(study, json2, false);
    val uploadId2 =  fromStatus(uploadStatus,"uploadId");


    val upload = uploadService.read(uploadId2);
    assertThat(upload.getPayload()).isEqualTo(json2);
    assertThat(upload.getState()).isEqualTo("VALIDATED");

    // test save
    val response = uploadService.save(study,uploadId);
    assertThat(response.getStatusCode()).isEqualTo(OK);
  }

  @SneakyThrows
  private void testSequencingRead(final boolean isAsyncValidation) {
    test("sequencingRead.json", isAsyncValidation);
  }

  @SneakyThrows
  private void testVariantCall(final boolean isAsyncValidation) {
    test("variantCall.json", isAsyncValidation);
  }

  @SneakyThrows
  private String readFile(String name) {
    return new String(Files.readAllBytes(new java.io.File("..", name).toPath()));
  }

  private String read(String uploadId) {
    Upload status = uploadService.read(uploadId);
    return status.getState();
  }


  private String validate(String uploadId) throws InterruptedException {
    String state=read(uploadId);
    // wait for the server to finish
    while(state.equals("CREATED") || state.equals("UPDATED")) {
      Thread.sleep(50);
      state=read(uploadId);
    }
    return state;
  }

  @SneakyThrows
  private void test(String fileName, boolean isAsyncValidation) {
    val study="ABC123";
    val json = readFile(fileName);

    // test upload
    val uploadStatus=uploadService.upload(study, json, isAsyncValidation);
    assertThat(uploadStatus.getStatusCode()).isEqualTo(OK);
    val uploadId= fromStatus(uploadStatus,"uploadId");
    log.info(format("UploadId='%s'",uploadId));
    assertThat(uploadId.startsWith("UP")).isTrue();

    val initialState = read(uploadId);
    if (isAsyncValidation){
      // test create for Asynchronous case
      assertThat(initialState).isEqualTo("CREATED");
    } else {
      assertThat(initialState).isEqualTo("VALIDATED"); //Synchronous should always return VALIDATED
    }

    // test validation
    val finalState = validate(uploadId);
    assertThat(finalState).isEqualTo("VALIDATED");

    // test save
   val response = uploadService.save(study,uploadId);
   assertThat(response.getStatusCode()).isEqualTo(OK);
  }

}