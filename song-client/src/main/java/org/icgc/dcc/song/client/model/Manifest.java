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
package org.icgc.dcc.song.client.model;

import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;

@Data
public class Manifest {

  // Refer to [DCC-5706] for two tabs reason
  private static final String TWO_TABS = "\t\t";
  private static final String NEWLINE = "\n";
  private static final String EMPTY = "";

  final String analysisId;
  final Collection<ManifestEntry> entries;

  public Manifest(String uploadId) {
    this.analysisId = uploadId;
    this.entries = new ArrayList<ManifestEntry>();
  }

  public void add(ManifestEntry m) {
    entries.add(m);
  }

  @Override
  public String toString() {
    return analysisId +
        TWO_TABS +
        NEWLINE +
        entries.stream()
            .map(e -> e.toString() + NEWLINE)
            .reduce(EMPTY, (a, b) -> a + b);
  }

  public void addAll(@NonNull Collection<? extends ManifestEntry> collect) {
    entries.addAll(collect);
  }

}
