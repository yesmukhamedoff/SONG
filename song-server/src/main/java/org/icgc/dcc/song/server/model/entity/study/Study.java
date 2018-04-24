package org.icgc.dcc.song.server.model.entity.study;

public interface Study {

  String getName();

  String getOrganization();

  String getDescription();

  void setName(String name);

  void setOrganization(String organization);

  void setDescription(String description);
}
