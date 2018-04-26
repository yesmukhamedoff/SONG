package org.icgc.dcc.song.server.model.entity.study;

import lombok.NonNull;

public interface Study  {

  String getName();

  String getOrganization();

  String getDescription();

  void setName(String name);

  void setOrganization(String organization);

  void setDescription(String description);

  default void setWithStudy(@NonNull Study study){
    setDescription(study.getDescription());
    setName(study.getName());
    setOrganization(study.getOrganization());
  }

}
