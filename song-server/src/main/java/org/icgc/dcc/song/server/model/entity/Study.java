package org.icgc.dcc.song.server.model.entity;

public interface Study {

  String getStudyId();
  String getOrganization();
  String getName();
  String getDescription();

  void setStudyId(String id);
  void setOrganization(String organization);
  void setName(String name);
  void setDescription(String description);

}
