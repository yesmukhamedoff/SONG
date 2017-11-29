package org.icgc.dcc.song.server.model.analysis;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.icgc.dcc.song.server.model.experiment.SequencingRead;

@EqualsAndHashCode(callSuper=false)
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class SequencingReadAnalysis extends Analysis {

    SequencingRead experiment;

    @JsonGetter
    public String getAnalysisType() {
        return "sequencingRead";
    }

    public static SequencingReadAnalysis create(String id, String study, String state ) {
        val s = new SequencingReadAnalysis();
        s.setAnalysisId(id);
        s.setStudy(study);
        s.setAnalysisState(state);
        return s;
    }

}
