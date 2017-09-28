package org.icgc.dcc.song.importer.convert;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.icgc.dcc.song.importer.model.PortalFileMetadata;

import static org.icgc.dcc.common.core.json.JsonNodeBuilders.object;
import static org.icgc.dcc.song.importer.convert.FileConverter.getFileId;
import static org.icgc.dcc.song.importer.convert.FileConverter.getIndexFileObjectId;

public class InfoConverter {

  private static final String PARENT_OBJECT_ID = "parentObjectId";
  private static final String INDEX_OBJECT_ID = "indexObjectId";

  public static JsonNode buildIndexFileInfo(@NonNull PortalFileMetadata portalFileMetadata){
    return object()
        .with(PARENT_OBJECT_ID, getFileId(portalFileMetadata))
        .end();
  }

  public static JsonNode buildFileInfo(@NonNull PortalFileMetadata portalFileMetadata){
    return object()
        .with(INDEX_OBJECT_ID, getIndexFileObjectId(portalFileMetadata))
        .end();
  }

}
