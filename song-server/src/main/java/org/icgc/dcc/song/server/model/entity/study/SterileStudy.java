package org.icgc.dcc.song.server.model.entity.study;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = TableNames.STUDY)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SterileStudy extends AbstractStudyEntity {

  public static SterileStudy createSterileStudy(@NonNull String id, @NonNull StudyRequest request){
    val s = new SterileStudy();
    s.setName(request.getName());
    s.setDescription(request.getDescription());
    s.setOrganization(request.getOrganization());
    s.setStudyId(id);
    return s;
  }

  public static SterileStudy createSterileStudy(String id, String name, String organization, String description){
    return createSterileStudy(id, createStudyRequest(name, organization, description));
  }


}
