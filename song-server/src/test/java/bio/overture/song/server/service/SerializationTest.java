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
package bio.overture.song.server.service;

import bio.overture.song.core.utils.JsonUtils;
import bio.overture.song.server.model.analysis.AbstractAnalysis;
import bio.overture.song.server.model.analysis.SequencingReadAnalysis;
import bio.overture.song.server.model.analysis.VariantCallAnalysis;
import bio.overture.song.server.model.entity.Donor;
import bio.overture.song.server.model.entity.FileEntity;
import bio.overture.song.server.model.entity.composites.DonorWithSpecimens;
import bio.overture.song.server.model.experiment.SequencingRead;
import lombok.SneakyThrows;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SerializationTest {

  private static final String FILEPATH = "src/test/resources/fixtures/";

  @Test
  @SneakyThrows
  public void testConvertValue() {
    val json = "{}";

    @SuppressWarnings("rawtypes")
    val m = JsonUtils.fromJson(json, Map.class);
    assertThat(Collections.emptyMap()).isEqualTo(m);
  }

  @Test
  @SneakyThrows
  public void testDonorSpecimens() {
    val donorId = "DO1234";
    val submitter = "1234";
    val study = "X2345-QRP";
    val gender = "female";

    val single = String.format(
        "{'donorId':'%s','donorSubmitterId':'%s','studyId':'%s','donorGender':'%s',"
            + "'roses':'red','violets':'blue'}",
        donorId, submitter, study, gender);
    val metadata = JsonUtils.fromSingleQuoted("{'roses':'red','violets':'blue'}");
    val json = JsonUtils.fromSingleQuoted(single);
    val donor = JsonUtils.fromJson(json, DonorWithSpecimens.class);
    Assertions.assertThat(donor.getDonorId()).isEqualTo(donorId);
    Assertions.assertThat(donor.getDonorSubmitterId()).isEqualTo(submitter);
    Assertions.assertThat(donor.getStudyId()).isEqualTo(study);
    Assertions.assertThat(donor.getDonorGender()).isEqualTo(gender);
    assertThat(donor.getSpecimens()).isEqualTo(Collections.emptyList());
    Assertions.assertThat(donor.getInfoAsString()).isEqualTo(metadata);
  }

  @Test
  public void testDonorToJson() {
    val donor = new Donor();
    val json = JsonUtils.toJson(donor);

    val expected =
        "{'donorId':null,'donorSubmitterId':null,'studyId':null,'donorGender':null,"
            + "'info':{}}";
    val expectedJson = JsonUtils.fromSingleQuoted(expected);
    assertThat(json).isEqualTo(expectedJson);
  }

  @Test
  public void testDonorSettings() {
    val donor = new Donor();
    donor.setDonorId(null);
    val json = JsonUtils.toJson(donor);
    System.err.printf("json='%s'\n", json);
    val expected =
        "{'donorId':null,'donorSubmitterId':null,'studyId':null,'donorGender':null,"
            + "'info':{}}";
    val expectedJson = JsonUtils.fromSingleQuoted(expected);
    assertThat(json).isEqualTo(expectedJson);
  }

  @Test
  public void testDonorValues() {
    val id = "DO000123";
    val submitterId = "123";
    val studyId = "X23-CA";
    val gender = "male";
    val metadata = "";

    val donor = Donor.builder()
        .donorId(id)
        .donorSubmitterId(submitterId)
        .studyId(studyId)
        .donorGender(gender)
        .build();
    donor.setInfo(metadata);

    val json = JsonUtils.toJson(donor);

    val expected = String.format(
        "{'donorId':'%s','donorSubmitterId':'%s','studyId':'%s','donorGender':'%s',"
            + "'info':{%s}}",
        id, submitterId, studyId, gender, metadata);
    val expectedJson = JsonUtils.fromSingleQuoted(expected);
    assertThat(json).isEqualTo(expectedJson);
  }

  @Test
  public void testInvalidValues() {
    val id = "DO000123";
    val submitterId = "123";
    val studyId = "X23-CA";
    val gender = "potatoes";

    boolean failed = false;
    try {
      val donor = Donor.builder()
          .donorId(id)
          .donorSubmitterId(submitterId)
          .studyId(studyId)
          .donorGender(gender)
          .build();
    } catch (IllegalArgumentException e) {
      failed = true;
    }

    assertThat(failed).isTrue();

  }

  @Test
  public void testSequencingReadToJSON() {
    val id="AN1";
    val aligned=true;
    val alignmentTool="BigWrench";
    val insertSize=25L;
    val libraryStrategy="Other";
    val pairedEnd = false;
    val genome="Castor Canadiansis";
    //val metadata = JsonUtils.fromSingleQuoted("'sequencingTool': 'NanoporeSeq123'");
    val metadata = "";

    val sequencingRead = SequencingRead.builder()
        .analysisId(id)
        .aligned(aligned)
        .alignmentTool(alignmentTool)
        .insertSize(insertSize)
        .libraryStrategy(libraryStrategy)
        .pairedEnd(pairedEnd)
        .referenceGenome(genome)
        .build();
    val json = JsonUtils.toJson(sequencingRead);

    val expected = String.format("{'analysisId':'%s','aligned':%s,'alignmentTool':'%s'," +
            "'insertSize':%s,'libraryStrategy':'%s','pairedEnd':%s,'referenceGenome':'%s','info':{%s}}",
            id, aligned, alignmentTool, insertSize, libraryStrategy, pairedEnd, genome, metadata);
    val expectedJson = JsonUtils.fromSingleQuoted(expected);
    assertThat(json).isEqualTo(expectedJson);
  }

  @Test
  public void testSequencingReadFromJson() {
    val id="AN1";
    val aligned=true;
    val alignmentTool="BigWrench";
    val insertSize=25L;
    val libraryStrategy="Other";
    val genome="Castor Canadiansis";
    val pairedEnd = false;

    val metadata = "";

    val sequencingRead1 = SequencingRead.builder()
        .analysisId(id)
        .aligned(aligned)
        .alignmentTool(alignmentTool)
        .insertSize(insertSize)
        .libraryStrategy(libraryStrategy)
        .pairedEnd(pairedEnd)
        .referenceGenome(genome)
        .build();

    val singleQuotedJson = String.format("{'analysisId':'%s','aligned':%s,'alignmentTool':'%s'," +
                    "'insertSize':%s,'libraryStrategy':'%s','pairedEnd':%s, 'referenceGenome': '%s', 'info':{%s}}", id, aligned, alignmentTool,
            insertSize, libraryStrategy, pairedEnd, genome, metadata);
    val json = JsonUtils.fromSingleQuoted(singleQuotedJson);

    val sequencingRead2 = JsonUtils.fromJson(json, SequencingRead.class);


    assertThat(sequencingRead1).isEqualToComparingFieldByField(sequencingRead2);

  }

  @Test
  public void testListFile() throws IOException {
    val singleQuotedJson = "{'file':[ { 'objectId': 'FI12345', 'fileName':'dna3.bam', 'fileMd5':'A1B2C3D4E5F6'}," +
            "{'objectId': 'FI34567', 'fileName': 'dna7.fasta', 'fileType':'BAM', 'fileSize':1234, 'fileMd5': 'F1E2D3'}]}";

    val json = JsonUtils.fromSingleQuoted(singleQuotedJson);
    val root=JsonUtils.readTree(json);
    val files=root.get("file");
    String fileJson=JsonUtils.toJson(files);

    List<FileEntity> f = Arrays.asList(JsonUtils.fromJson(fileJson, FileEntity[].class));

    assertThat(f.size()).isEqualTo(2);
    assertThat(f.get(0).getFileName()).isEqualTo("dna3.bam");
  }

  @SneakyThrows
  private String readFile(String name) {
    return new String(Files.readAllBytes(new java.io.File("..", name).toPath()));
  }

  @Test
  public void testSequencingReadAnalysisFromJson() throws IOException {
    val json = readFile(FILEPATH + "sequencingRead.json");
    val analysis = JsonUtils.fromJson(json, AbstractAnalysis.class);

    System.out.printf("*** Analysis object='%s'\n",analysis);
    assertThat(analysis.getAnalysisType()).isEqualTo("sequencingRead");
    assertThat(analysis.getFile().size()).isEqualTo(2);
    Assertions.assertThat(analysis.getSample().get(0).getDonor().getDonorSubmitterId()).isEqualTo("internal_donor_123456789-00");

    assertThat(analysis).isInstanceOf(SequencingReadAnalysis.class);
    val r = ((SequencingReadAnalysis) analysis).getExperiment();

    Assertions.assertThat(r.getLibraryStrategy()).isEqualTo("WXS");
    Assertions.assertThat(r.getInsertSize()).isEqualTo(900);
    Assertions.assertThat(r.getAlignmentTool()).isEqualTo("MUSE variant call pipeline");
  }

  @Test
  public void testVariantCallAnalysisFromJson() throws IOException {
    val json =readFile(FILEPATH + "variantCall.json");
    val analysis = JsonUtils.fromJson(json, AbstractAnalysis.class);
    System.out.printf("*** Analysis object='%s'\n",analysis);
    assertThat(analysis.getAnalysisType()).isEqualTo("variantCall");

    assertThat(analysis).isInstanceOf(VariantCallAnalysis.class);
    VariantCallAnalysis v = (VariantCallAnalysis) analysis;
    System.out.printf("VariantCall object='%s'\n", v);
    
    assertThat(analysis.getFile().size()).isEqualTo(2);
    Assertions.assertThat(analysis.getSample().get(0).getDonor().getDonorSubmitterId()).isEqualTo("internal_donor_123456789-00");
  }

}
