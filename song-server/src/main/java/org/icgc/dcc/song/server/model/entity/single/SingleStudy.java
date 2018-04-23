package org.icgc.dcc.song.server.model.entity.single;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.icgc.dcc.song.server.model.entity.AbstractStudy;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = TableNames.STUDY)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SingleStudy extends AbstractStudy {

}
