package org.icgc.dcc.song.server.model.entity.donor.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.entity.donor.Donor;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import static org.icgc.dcc.song.server.model.enums.Constants.DONOR_GENDER;
import static org.icgc.dcc.song.server.model.enums.Constants.validate;

@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DonorImpl extends Metadata implements Donor {

  @Column(name = TableAttributeNames.SUBMITTER_ID, nullable = false)
  private String donorSubmitterId;

  @Column(name = TableAttributeNames.GENDER, nullable = false)
  private String donorGender;

  public void setDonorGender(String gender) {
    validate(DONOR_GENDER, gender);
    this.donorGender = gender;
  }

  public static DonorImpl createDonorImpl(String donorSubmitterId, String donorGender){
    val d = new DonorImpl();
    d.setDonorGender(donorGender);
    d.setDonorSubmitterId(donorSubmitterId);
    return d;
  }

}
