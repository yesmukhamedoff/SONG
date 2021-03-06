/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package bio.overture.song.client.model;

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
