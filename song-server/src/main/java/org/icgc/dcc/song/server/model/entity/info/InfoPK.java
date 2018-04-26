package org.icgc.dcc.song.server.model.entity.info;

import lombok.Data;
import lombok.NonNull;
import lombok.val;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class InfoPK implements Serializable{

  private String id;
  private String idType;

  public static InfoPK createInfoPK(@NonNull String id, @NonNull String idType){
    val i = new InfoPK();
    i.setId(id);
    i.setIdType(idType);
    return i;
  }

}
