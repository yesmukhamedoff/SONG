package org.icgc.dcc.song.client.errors;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.song.core.exceptions.ServerException;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.icgc.dcc.common.core.util.Joiners.NEWLINE;
import static org.icgc.dcc.song.core.exceptions.SongError.parseErrorResponse;

@Slf4j
public class ServerResponseErrorHandler extends DefaultResponseErrorHandler{

  @Override
  public void handleError(ClientHttpResponse clientHttpResponse) throws IOException, ServerException {
    val httpStatusCode = clientHttpResponse.getStatusCode();
    val br = new BufferedReader(new InputStreamReader(clientHttpResponse.getBody()));
    val body = NEWLINE.join(br.lines().iterator());
    val songError = parseErrorResponse(httpStatusCode,body);
    throw new ServerException(songError);
  }

}