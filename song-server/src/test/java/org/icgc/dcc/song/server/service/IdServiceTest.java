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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.id.client.core.IdClient;
import org.icgc.dcc.id.client.util.HashIdClient;
import org.icgc.dcc.song.core.exceptions.ServerException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ActiveProfiles("dev")
public class IdServiceTest {
  @Autowired
  private IdClient idClient;

  private static final String SUBMITTER_ID_1 = "AN8899";
  private static final String SUBMITTER_ID_2 = "AN112233";

  @Test
  public void testUndefinedAnalysisId(){
    val idService = createHashIdService();

    val id1 = idService.resolveAnalysisId("",false);
    assertThat(id1).isNotNull();

    val id2 = idService.resolveAnalysisId("",false);
    assertThat(id2).isNotNull();
    assertThat(id1).isNotEqualTo(id2);

    val id3 = idService.resolveAnalysisId(null,false);
    assertThat(id1).isNotNull();
    assertThat(id1).isNotEqualTo(id3);
  }

  @Test
  public void testAnalysisIdNormal(){
    val idService = createHashIdService();

    val id1 = idService.resolveAnalysisId(SUBMITTER_ID_1,false);
    assertThat(id1).isEqualTo(SUBMITTER_ID_1);

    val id2 = idService.resolveAnalysisId(SUBMITTER_ID_2,false);
    assertThat(id2).isEqualTo(SUBMITTER_ID_2);
    assertThat(id1).isNotEqualTo(id2);
  }

  @Test
  public void testIgnoreAnalysisIdCollision(){
    val idService = createHashIdService();

    val id1 = idService.resolveAnalysisId(SUBMITTER_ID_1,false);
    assertThat(id1).isEqualTo(SUBMITTER_ID_1);

    val id2 = idService.resolveAnalysisId(SUBMITTER_ID_1,true);
    assertThat(id2).isEqualTo(SUBMITTER_ID_1);
    assertThat(id1).isEqualTo(id2);
  }

  @Test
  public void testAnalysisIdCollision(){
    val idService = createHashIdService();

    val id1 = idService.resolveAnalysisId(SUBMITTER_ID_1,false);
    assertThat(id1).isEqualTo(SUBMITTER_ID_1);
    try{
      val id2 = idService.resolveAnalysisId(SUBMITTER_ID_1,false);
    } catch(ServerException e){
      val songError = e.getSongError();
      assertThat(songError.getErrorId()).isEqualTo("analysis.id.already.exists");
      assertThat(songError.getHttpStatusCode()).isEqualTo(409);
      assertThat(songError.getHttpStatusName()).isEqualTo("CONFLICT");
      assertThat(songError.getMessage()).contains(format("[IdService] - Collision detected for analysisId '%s'",SUBMITTER_ID_1));
      return;
    }
    fail("No exception was thrown, but should have been thrown since ignoreAnalysisIdCollisions=false and"
        + " the same id was attempted to be created");
  }


  @Test
  public void testGenerateDonorId() {
    val submittedDonorId="CGP_donor_1337237";
    val study="BRCA-EU";
    val id= idClient.createDonorId(submittedDonorId, study);
    assertThat(id).isEqualTo("DO217962");
  }

  @Test
  public void testGenerateSpecimenId() {
    val submittedSpecimenId="CGP_specimen_1387555";
    val study="BRCA-EU";
    val id= idClient.createSpecimenId(submittedSpecimenId, study);
    assertThat(id).isEqualTo("SP117136");
  }

  @Test
  public void testGenerateSampleId() {
    val submittedSampleId="PD4982a";
    val study="BRCA-EU";
    val id = idClient.createSampleId(submittedSampleId, study);
    assertThat(id).isEqualTo("SA542735");
  }

  @Test
  public void testGenerateFileId() {
    val analysisId="efcf90ee-53ae-4f9f-b29a-e0a83ca70272";
    val fileName="f5c9381090a53c54358feb2ba5b7a3d7.bam";

    val id  = idClient.getObjectId(analysisId, fileName);

  }

  private static final IdService createHashIdService(){
    return new IdService(new HashIdClient(true));
  }

}
