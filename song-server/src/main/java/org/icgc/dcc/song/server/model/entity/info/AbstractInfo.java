package org.icgc.dcc.song.server.model.entity.info;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import java.util.Map;

import static org.icgc.dcc.song.server.repository.CustomJsonType.CUSTOM_JSON_TYPE_PKG_PATH;

@ToString(callSuper = true)
@Data
@MappedSuperclass
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class AbstractInfo  {

  @Id
  @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
  private String id;

  @Column(name = TableAttributeNames.ID_TYPE, nullable = false)
  private String idType;

  @Type(type = CUSTOM_JSON_TYPE_PKG_PATH)
  private Map<String, Object> info;

  @Entity
  @Table(name = TableNames.INFO)
  @Where(clause = ModelAttributeNames.ID_TYPE+" = '"+TableNames.STUDY+"'" )
  public static class StudyInfo extends AbstractInfo {

  }

  @Entity
  @Table(name = TableNames.INFO)
  @Where(clause = ModelAttributeNames.ID_TYPE+" = '"+TableNames.DONOR+"'" )
  public static class DonorInfo extends AbstractInfo {


  }

  @Entity
  @Table(name = TableNames.INFO)
  @Where(clause = ModelAttributeNames.ID_TYPE+" = '"+TableNames.SPECIMEN+"'" )
  public static class SpecimenInfo extends AbstractInfo { }

  @Entity
  @Table(name = TableNames.INFO)
  @Where(clause = ModelAttributeNames.ID_TYPE+" = '"+TableNames.SAMPLE+"'" )
  public static class SampleInfo extends AbstractInfo { }

}
