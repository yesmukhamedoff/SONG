package org.icgc.dcc.song.server.model.entity.file;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.enums.AccessTypes;
import org.icgc.dcc.song.server.model.enums.Constants;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import static org.icgc.dcc.song.server.model.enums.AccessTypes.resolveAccessType;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@MappedSuperclass
public class File extends Metadata {

  @Column(name = TableAttributeNames.NAME, nullable = false)
  private String fileName;

  @Column(name = TableAttributeNames.SIZE, nullable = false)
  private Long fileSize;

  @Column(name = TableAttributeNames.TYPE, nullable = false)
  private String fileType;

  @Column(name = TableAttributeNames.MD5, nullable = false)
  private String fileMd5sum;

  @Column(name = TableAttributeNames.ACCESS, nullable = false)
  private String fileAccess;

  public void setFileType(String type) {
    Constants.validate(Constants.FILE_TYPE, type);
    fileType = type;
  }

  public void setFileAccess(@NonNull AccessTypes access){
    this.fileAccess = access.toString();
  }

  public void setFileAccess(@NonNull String access){
    setFileAccess(resolveAccessType(access));
  }

  public void setWithFile(@NonNull File file){
    setFileName(file.getFileName());
    setFileAccess(file.getFileAccess());
    setFileMd5sum(file.getFileMd5sum());
    setFileSize(file.getFileSize());
    setFileType(file.getFileType());
    setInfo(file.getInfo());
  }

}
