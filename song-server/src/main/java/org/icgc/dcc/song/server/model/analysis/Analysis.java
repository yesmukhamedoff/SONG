package org.icgc.dcc.song.server.model.analysis;

public interface Analysis {

  void setAnalysisState(String state);

  String getAnalysisType();

  String getAnalysisId();

  String getStudyId();

  String getAnalysisState();

  void setAnalysisId(String analysisId);

}
