package org.icgc.dcc.song.server.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.song.schema.FileOuterClass;

import java.util.Map;

public class EntityFactory {
  private final static ObjectMapper MAPPER  = new ObjectMapper();

  public Struct.Builder createStruct(){
    return Struct.newBuilder();
  }

  public FileOuterClass.FileEntity.Builder createFileEntity(){
    return FileOuterClass.FileEntity.newBuilder();
  }

  public FileOuterClass.File.Builder createFile(){
    return FileOuterClass.File.newBuilder();
  }

  @SneakyThrows
  public Map<String, Object> convertToMap(Struct s){
    val json = JsonFormat.printer().print(s);
    val o = new ObjectMapper();
    val j = o.readTree(json);
    return (Map<String, Object>)o.convertValue(j, Map.class);
  }

  @SneakyThrows
  public Struct convertToStruct(Map<String, Object> map){
    val j = MAPPER.valueToTree(map);
    return convertToStruct(j);
  }

  @SneakyThrows
  public Struct convertToStruct(JsonNode jsonNode){
    val json = MAPPER.writeValueAsString(jsonNode);
    val jp = JsonFormat.parser();
    val out = Struct.newBuilder();
    jp.merge(json, out );
    return out.build();
  }

}
