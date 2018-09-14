package org.icgc.dcc.song.server.converter;

import org.icgc.dcc.song.schema.FileOuterClass;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = EntityFactory.class)
public interface FileMapper {

  @Mapping(source = "fileMd5sum", target = "fileMd5Sum")
  FileOuterClass.File.Builder convertToProtobufFile(org.icgc.dcc.song.server.model.entity.File file);

  @Mapping(source = "fileMd5sum", target = "fileMd5Sum")
  FileOuterClass.FileEntity.Builder convertToProtobufFileEntity(org.icgc.dcc.song.server.model.entity.File file);



}
