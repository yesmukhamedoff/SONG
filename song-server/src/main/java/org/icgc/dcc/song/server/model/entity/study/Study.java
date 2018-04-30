package org.icgc.dcc.song.server.model.entity.study;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@MappedSuperclass
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Study extends Metadata {

  @Column(name = TableAttributeNames.NAME, nullable = false)
  private String name;

  @Column(name = TableAttributeNames.ORGANIZATION, nullable = false)
  private String organization;

  @Column(name = TableAttributeNames.DESCRIPTION, nullable = false)
  private String description;

  public void setWithStudy(@NonNull Study study){
    setName(study.getName());
    setOrganization(study.getOrganization());
    setDescription(study.getDescription());
    setInfo(study.getInfo());
  }

  public static Study createStudy(String name, String organization, String description){
    val s = new Study();
    s.setDescription(description);
    s.setOrganization(organization);
    s.setName(name);
    return s;
  }

}
