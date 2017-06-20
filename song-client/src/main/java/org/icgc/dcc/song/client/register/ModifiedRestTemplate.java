package org.icgc.dcc.song.client.register;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Extension of RestTemplate that has additional methods
 */
public class ModifiedRestTemplate extends RestTemplate {

  public ModifiedRestTemplate() {
  }

  public ModifiedRestTemplate(ClientHttpRequestFactory requestFactory) {
    super(requestFactory);
  }

  public ModifiedRestTemplate(
      List<HttpMessageConverter<?>> messageConverters) {
    super(messageConverters);
  }

  /**
   * Similar to getForEntity, this performs a PUT and returns a ResponseEntity
   */
  public <T> ResponseEntity<T> putForEntity(String url, Object request, Class<T> responseType, Object... urlVariables) throws RestClientException {
    RequestCallback requestCallback = this.httpEntityCallback(request);
    ResponseExtractor<ResponseEntity<T>> responseExtractor = this.responseEntityExtractor(responseType);
    return (ResponseEntity)this.execute(url, HttpMethod.PUT, requestCallback, responseExtractor, (Object[])urlVariables);
  }

}
