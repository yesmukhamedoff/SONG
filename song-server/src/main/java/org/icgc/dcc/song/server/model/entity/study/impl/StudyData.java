package org.icgc.dcc.song.server.model.entity.study.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.entity.study.Study;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper=true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StudyData extends Metadata implements Study {

  @Column(name = TableAttributeNames.NAME, nullable = false)
  private String name;

  @Column(name = TableAttributeNames.ORGANIZATION, nullable = false)
  private String organization;

  @Column(name = TableAttributeNames.DESCRIPTION, nullable = false)
  private String description;

  public static StudyData createStudyData(String name, String organization, String description) {
    val s = new StudyData();
    s.setDescription(description);
    s.setName(name);
    s.setOrganization(organization);
    return s;
  }

}
