package org.icgc.dcc.song.server.model.enums;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class TableNames {

	public static final String STUDY						=	"Study";
	public static final String DONOR						=	"Donor";
	public static final String SPECIMEN					=	"Specimen";
	public static final String SAMPLE						=	"Sample";
	public static final String ANALYSIS					=	"Analysis";
	public static final String FILE							=	"File";
	public static final String SAMPLESET				=	"Sampleset";
	public static final String SEQUENCINGREAD		=	"sequencingread";
	public static final String VARIANTCALL			=	"variantcall";
	public static final String UPLOAD						=	"Upload";
	public static final String INFO							=	"Info";

}
