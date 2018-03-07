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

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class})
@ActiveProfiles({"dev","test"})
public class StudyWithDonorsServiceTest {

  @Autowired
  private StudyWithDonorsService service;

  @Test
  public void testReadWithChildren(){
    val d = service.readWithChildren("ABC123");
    assertThat(d.getDonors().size()).isGreaterThanOrEqualTo(1);

    val opt = d.getDonors().stream().filter(x -> x.getDonor().getDonorId().equals("DO1")).findFirst();
    assertThat(opt.isPresent()).isTrue();
    val donorWithSpecimens = opt.get();
    assertThat(donorWithSpecimens.getSpecimens()).hasSize(2);

    val specimenWithSamples1 = donorWithSpecimens.getSpecimens().get(0);
    assertThat(specimenWithSamples1.getSpecimen().getSpecimenId()).isEqualTo("SP1");
    assertThat(specimenWithSamples1.getSamples()).hasSize(2);

    val sample11 = specimenWithSamples1.getSamples().get(0);
    assertThat(sample11.getSampleId()).isEqualTo("SA1");

    val sample12 = specimenWithSamples1.getSamples().get(1);
    assertThat(sample12.getSampleId()).isEqualTo("SA11");


    val specimenWithSamples2 = donorWithSpecimens.getSpecimens().get(1);
    assertThat(specimenWithSamples2.getSpecimen().getSpecimenId()).isEqualTo("SP2");
    assertThat(specimenWithSamples2.getSamples()).hasSize(1);

    val sample21 = specimenWithSamples2.getSamples().get(0);
    assertThat(sample21.getSampleId()).isEqualTo("SA21");
  }

}