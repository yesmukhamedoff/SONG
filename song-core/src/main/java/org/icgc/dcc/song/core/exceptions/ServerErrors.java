package org.icgc.dcc.song.core.exceptions;

import lombok.NonNull;
import org.springframework.http.HttpStatus;

import static com.google.common.base.Preconditions.checkArgument;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

public enum ServerErrors implements ServerError {

  STUDY_ID_DOES_NOT_EXIST(NOT_FOUND),
  UPLOAD_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  ANALYSIS_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  SEQUENCING_READ_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  VARIANT_CALL_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  INFO_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  INFO_REPOSITORY_UPDATE_RECORD(UNPROCESSABLE_ENTITY),
  INFO_REPOSITORY_DELETE_RECORD(UNPROCESSABLE_ENTITY),
  GENERATOR_CLOCK_MOVED_BACKWARDS(INTERNAL_SERVER_ERROR),
  PAYLOAD_PARSING(UNPROCESSABLE_ENTITY),
  UPLOAD_ID_NOT_FOUND(NOT_FOUND ),
  FILE_NOT_FOUND(NOT_FOUND ),
  INFO_NOT_FOUND(NOT_FOUND ),
  UPLOAD_ID_NOT_VALIDATED(CONFLICT),
  ANALYSIS_ID_NOT_CREATED(INTERNAL_SERVER_ERROR),
  ANALYSIS_ID_NOT_FOUND(NOT_FOUND),
  SEQUENCING_READ_NOT_FOUND(NOT_FOUND),
  VARIANT_CALL_NOT_FOUND(NOT_FOUND),
  UNAUTHORIZED_TOKEN(UNAUTHORIZED),
  SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
  UNPUBLISHED_FILE_IDS(CONFLICT),
  NOT_IMPLEMENTED_YET(NOT_IMPLEMENTED),
  SAMPLE_REPOSITORY_CREATE_RECORD(INTERNAL_SERVER_ERROR),
  SPECIMEN_RECORD_FAILED(INTERNAL_SERVER_ERROR),
  DONOR_RECORD_FAILED(INTERNAL_SERVER_ERROR),
  ANALYSIS_STATE_UPDATE_FAILED(INTERNAL_SERVER_ERROR),
  FILE_RECORD_FAILED(INTERNAL_SERVER_ERROR),
  SEARCH_TERM_SYNTAX(BAD_REQUEST),
  ANALYSIS_ID_COLLISION(CONFLICT),
  INFO_ALREADY_EXISTS(CONFLICT),
  VARIANT_CALL_CORRUPTED_DUPLICATE(INTERNAL_SERVER_ERROR),
  SEQUENCING_READ_CORRUPTED_DUPLICATE(INTERNAL_SERVER_ERROR),
  DUPLICATE_ANALYSIS_ATTEMPT(CONFLICT),
  STUDY_ALREADY_EXISTS(CONFLICT),
  UNKNOWN_ERROR(INTERNAL_SERVER_ERROR);

  private static final Character ERROR_ID_SEPARATOR = '.';
  private static final String REGEX = "[A-Z0-9_]+";

  private final String errorId;
  private final HttpStatus httpStatus;

  ServerErrors(@NonNull HttpStatus httpStatus){
    this.httpStatus = httpStatus;
    this.errorId = extractErrorId(this.name());
  }

  public String getErrorId() {
    return errorId;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public static String extractErrorId(@NonNull String errorId){
    checkArgument(errorId.matches(REGEX),
        "The errorId [%s] must follow the regex: %s", errorId, REGEX);
    return errorId.toLowerCase().replaceAll("_",".");
  }

}
