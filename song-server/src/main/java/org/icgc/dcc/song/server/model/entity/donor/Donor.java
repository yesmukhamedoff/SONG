package org.icgc.dcc.song.server.model.entity.donor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import static org.icgc.dcc.song.server.model.enums.Constants.DONOR_GENDER;
import static org.icgc.dcc.song.server.model.enums.Constants.validate;

@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Donor extends Metadata {

  @Column(name = TableAttributeNames.SUBMITTER_ID, nullable = false)
  private String donorSubmitterId;

  @Column(name = TableAttributeNames.GENDER, nullable = false)
  private String donorGender;

  public void setDonorGender(String gender) {
    validate(DONOR_GENDER, gender);
    this.donorGender = gender;
  }

  public void setWithDonor(@NonNull Donor donor){
    setDonorSubmitterId(donor.getDonorSubmitterId());
    setDonorGender(donor.getDonorGender());
    setInfo(donor.getInfo());
  }

  public static Donor createDonor(String donorSubmitterId, String donorGender){
    val d = new Donor();
    d.setDonorSubmitterId(donorSubmitterId);
    d.setDonorGender(donorGender);
    return d;
  }

}
