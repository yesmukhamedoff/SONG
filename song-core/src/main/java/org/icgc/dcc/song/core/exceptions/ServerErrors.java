/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
  DONOR_DOES_NOT_EXIST(NOT_FOUND),
  SPECIMEN_DOES_NOT_EXIST(NOT_FOUND),
  SAMPLE_DOES_NOT_EXIST(NOT_FOUND),
  UPLOAD_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  ANALYSIS_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  SEQUENCING_READ_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  VARIANT_CALL_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  INFO_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  INFO_REPOSITORY_UPDATE_RECORD(UNPROCESSABLE_ENTITY),
  INFO_REPOSITORY_DELETE_RECORD(UNPROCESSABLE_ENTITY),
  FILE_REPOSITORY_UPDATE_RECORD(UNPROCESSABLE_ENTITY),
  DONOR_REPOSITORY_UPDATE_RECORD(UNPROCESSABLE_ENTITY),
  SAMPLE_REPOSITORY_UPDATE_RECORD(UNPROCESSABLE_ENTITY),
  SPECIMEN_REPOSITORY_UPDATE_RECORD(UNPROCESSABLE_ENTITY),
  STUDY_REPOSITORY_CREATE_RECORD(UNPROCESSABLE_ENTITY),
  FILE_REPOSITORY_DELETE_RECORD(UNPROCESSABLE_ENTITY),
  DONOR_REPOSITORY_DELETE_RECORD(UNPROCESSABLE_ENTITY),
  SPECIMEN_REPOSITORY_DELETE_RECORD(UNPROCESSABLE_ENTITY),
  SAMPLE_REPOSITORY_DELETE_RECORD(UNPROCESSABLE_ENTITY),
  GENERATOR_CLOCK_MOVED_BACKWARDS(INTERNAL_SERVER_ERROR),
  DONOR_ID_IS_CORRUPTED(INTERNAL_SERVER_ERROR),
  SAMPLE_ID_IS_CORRUPTED(INTERNAL_SERVER_ERROR),
  SPECIMEN_ID_IS_CORRUPTED(INTERNAL_SERVER_ERROR),
  PAYLOAD_PARSING(UNPROCESSABLE_ENTITY),
  UPLOAD_ID_NOT_FOUND(NOT_FOUND ),
  FILE_NOT_FOUND(NOT_FOUND ),
  INFO_NOT_FOUND(NOT_FOUND ),
  UPLOAD_ID_NOT_VALIDATED(CONFLICT),
  ANALYSIS_ID_NOT_CREATED(INTERNAL_SERVER_ERROR),
  ANALYSIS_MISSING_FILES(INTERNAL_SERVER_ERROR),
  ANALYSIS_MISSING_SAMPLES(INTERNAL_SERVER_ERROR),
  ANALYSIS_ID_NOT_FOUND(NOT_FOUND),
  SEQUENCING_READ_NOT_FOUND(NOT_FOUND),
  VARIANT_CALL_NOT_FOUND(NOT_FOUND),
  UNAUTHORIZED_TOKEN(UNAUTHORIZED),
  SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
  UNPUBLISHED_FILE_IDS(CONFLICT),
  NOT_IMPLEMENTED_YET(NOT_IMPLEMENTED),
  SAMPLE_REPOSITORY_CREATE_RECORD(INTERNAL_SERVER_ERROR),
  FILE_REPOSITORY_CREATE_RECORD(INTERNAL_SERVER_ERROR),
  SPECIMEN_RECORD_FAILED(INTERNAL_SERVER_ERROR),
  DONOR_RECORD_FAILED(INTERNAL_SERVER_ERROR),
  ANALYSIS_STATE_UPDATE_FAILED(INTERNAL_SERVER_ERROR),
  SEARCH_TERM_SYNTAX(BAD_REQUEST),
  ANALYSIS_ID_COLLISION(CONFLICT),
  INFO_ALREADY_EXISTS(CONFLICT),
  SAMPLE_ALREADY_EXISTS(CONFLICT),
  DONOR_ALREADY_EXISTS(CONFLICT),
  SPECIMEN_ALREADY_EXISTS(CONFLICT),
  VARIANT_CALL_CORRUPTED_DUPLICATE(INTERNAL_SERVER_ERROR),
  SEQUENCING_READ_CORRUPTED_DUPLICATE(INTERNAL_SERVER_ERROR),
  DUPLICATE_ANALYSIS_ATTEMPT(CONFLICT),
  STUDY_ALREADY_EXISTS(CONFLICT),
  UNKNOWN_ERROR(INTERNAL_SERVER_ERROR);

  private static final String ERROR_ID_SEPARATOR = ".";
  private static final String ENUM_NAME_SEPARATOR = "_";
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
    return errorId.toLowerCase().replaceAll(ENUM_NAME_SEPARATOR, ERROR_ID_SEPARATOR);
  }

}
