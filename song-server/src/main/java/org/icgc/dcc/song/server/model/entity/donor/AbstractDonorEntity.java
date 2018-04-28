package org.icgc.dcc.song.server.model.entity.donor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.icgc.dcc.song.server.model.entity.donor.impl.DonorImpl;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonPropertyOrder({
    ModelAttributeNames.DONOR_ID,
    ModelAttributeNames.DONOR_SUBMITTER_ID,
    ModelAttributeNames.STUDY_ID,
    ModelAttributeNames.DONOR_GENDER,
    ModelAttributeNames.SPECIMENS,
    ModelAttributeNames.INFO })
@JsonInclude(JsonInclude.Include.ALWAYS)
public abstract class AbstractDonorEntity extends DonorImpl implements DonorEntity {

  @Id
  @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
  private String donorId;

}
