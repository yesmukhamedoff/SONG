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
package org.icgc.dcc.song.server.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.server.model.entity.Donor;
import org.icgc.dcc.song.server.model.entity.composites.DonorWithSpecimens;
import org.icgc.dcc.song.server.service.DonorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/{studyId}")
@Api(tags = "Donor", description = "Create, read and delete donors")
public class DonorController {

  /**
   * Dependencies
   */
  @Autowired
  private final DonorService donorService;

  @ApiOperation(value = "CreateDonor", notes = "Creates a donor")
  @PostMapping(value = "/donors", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
  @ResponseBody
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public String create(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @RequestBody DonorWithSpecimens donor) {
    return donorService.create(donor);

  }

  @ApiOperation(value = "ReadDonor", notes = "Retrieves donor data for a donorId")
  @GetMapping(value = "/donors/{id}")
  @ResponseBody
  public Donor read(@PathVariable("id") String id) {
    return donorService.read(id);
  }

  /**
   * [DCC-5726] - updates disabled until back propagation updates due to business key updates is implemented
   */
//  @PutMapping(value = "/donors/{id}", consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
//  @ResponseBody
//  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
//  public String update(@PathVariable("study") String studyId, @PathVariable("id") String id, @RequestBody Donor donor) {
//    // TODO: [DCC-5642] Add checkRequest between path ID and Entity's ID
//    return donorService.update(donor);
//  }

  @ApiOperation(value = "DeleteDonors",
      notes = "Deletes donor data and all dependent specimens and samples for donorIds")
  @DeleteMapping(value = "/donors/{ids}")
  @PreAuthorize("@studySecurity.authorize(authentication, #studyId)")
  public String delete(
      @RequestHeader(value = AUTHORIZATION, required = false) final String accessToken,
      @PathVariable("studyId") String studyId,
      @PathVariable("ids") @ApiParam(value = "Comma separated list of donorIds", required = true)
          List<String> ids) {
    return donorService.delete(studyId, ids);
  }

}
