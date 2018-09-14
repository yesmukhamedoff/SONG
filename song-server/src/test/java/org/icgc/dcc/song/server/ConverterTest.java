package org.icgc.dcc.song.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.schema.FileOuterClass;
import org.icgc.dcc.song.schema.StudyOuterClass;
import org.icgc.dcc.song.server.config.ConverterConfig;
import org.icgc.dcc.song.server.converter.FileMapper;
import org.icgc.dcc.song.server.converter.LegacyEntityConverter;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.legacy.LegacyEntity;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ConverterTest {

  private static final ConverterConfig CONVERTER_CONFIG = new ConverterConfig();
  private LegacyEntityConverter legacyEntityConverter = CONVERTER_CONFIG.legacyEntityConverter();
  private FileMapper fileMapper = CONVERTER_CONFIG.fileMapper();

  @Test
  public void testR(){
    val study = StudyOuterClass.Study.newBuilder()
        .setId("ABC333")
        .setDescription("something")
        .setName("SomeName")
        .build();
    log.info("sdf");

  }

  @Test
  public void testLegacyEntityConversion(){
    val legacyEntity = LegacyEntity.builder()
        .access("controlled")
        .fileName("fn1")
        .gnosId("an1")
        .id("fi1")
        .projectCode("ABC123")
        .build();

    val legacyDto = legacyEntityConverter.convertToLegacyDto(legacyEntity);
    assertThat(legacyDto).isEqualToComparingFieldByField(legacyEntity);
    assertThat(isObjectsEqual(legacyDto, legacyEntity)).isFalse();

    val legacyEntityCopy = legacyEntityConverter.convertToLegacyEntity(legacyEntity);
    assertThat(legacyEntityCopy).isEqualToComparingFieldByField(legacyEntity);
    assertThat(isObjectsEqual(legacyEntityCopy, legacyEntity)).isFalse();

    val legacyEntityCopy2 = legacyEntityConverter.convertToLegacyEntity(legacyDto);
    assertThat(legacyEntityCopy2).isEqualToComparingFieldByField(legacyDto);
    assertThat(isObjectsEqual(legacyEntityCopy2, legacyDto)).isFalse();

    val legacyDtoCopy = legacyEntityConverter.convertToLegacyDto(legacyDto);
    assertThat(legacyDtoCopy).isEqualToComparingFieldByField(legacyDto);
    assertThat(isObjectsEqual(legacyDtoCopy, legacyDto)).isFalse();
  }

  @Test
  @SneakyThrows
  public void testPro(){
    val mym = FileOuterClass.File.newBuilder()
        .setAnalysisId("sdf")
        .setFileAccess("controlled")
        .build();
    val f = JsonFormat.printer();
    val out = f.print(mym);
    val f2 = FileOuterClass.File.newBuilder();
    JsonFormat.parser().ignoringUnknownFields().merge(out, f2);

    val info = Struct.newBuilder()
        .putFields("k1", Value.newBuilder().setNumberValue(20.3).build())
        .putFields("k2", Value.newBuilder()
            .setListValue(ListValue.newBuilder().addValues(
                Value.newBuilder().setStringValue("someemelement").build())
                .build())
            .build())
        .putFields("k3",
            Value.newBuilder().setStructValue(
                Struct.newBuilder()
                    .putFields("a1", Value.newBuilder().setStringValue("someA1").build())
                    .putFields("a2", Value.newBuilder().setStringValue("someA2").build())
                    .build())
                .build())
    .build();

    val k = FileOuterClass.FileEntity.newBuilder()
        .setAnalysisId("sdfsdf")
        .setFileSize(22334L)
        .setInfo(info)
        .build();
    val out2 = f.print(k);
    val f3 = FileOuterClass.FileEntity.newBuilder();
    JsonFormat.parser().merge(out2,f3);
    log.info("file: {}", out );
  }

  @Test
  @SneakyThrows
  public void testwer(){
    val file = File.builder()
        .analysisId("sdfsdf")
        .fileAccess("controlled")
        .fileMd5sum("23890ujsdkf89y234")
        .fileName("soemthing.vcf.gz")
        .fileSize(234234L)
        .fileType("VCF")
        .objectId("238o7nksdf89")
        .studyId("ABC123")
        .build();

    val infoProto = Struct.newBuilder()
        .putFields("k1", Value.newBuilder().setNumberValue(20.3).build())
        .putFields("k2", Value.newBuilder()
            .setListValue(ListValue.newBuilder().addValues(
                Value.newBuilder().setStringValue("someemelement").build())
                .build())
            .build())
        .putFields("k3",
            Value.newBuilder().setStructValue(
                Struct.newBuilder()
                    .putFields("a1", Value.newBuilder().setStringValue("someA1").build())
                    .putFields("a2", Value.newBuilder().setStringValue("someA2").build())
                    .build())
                .build())
        .build();
    val jsonString = JsonFormat.printer().print(infoProto);
    val info  = new ObjectMapper().readTree(jsonString);
    file.setInfo(info);
    val protoFile = fileMapper.convertToProtobufFileEntity(file);
    log.info("sdf");

  }

  private boolean isObjectsEqual(Object o1, Object o2){
    return o1 == o2;
  }

}
