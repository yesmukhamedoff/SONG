package org.icgc.dcc.song.server.security;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.Collections;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Slf4j
public class DownloadStudyScopeStrategy {

  private static final String SCOPE_STRATEGY = "song.%s.%s";

  @Value("${auth.server.downloadScope}")
  protected String downloadScope;

  public boolean authorize(@NonNull Authentication authentication, @NonNull final String studyId) {
    log.info("Checking authorization with study id {}", studyId);

    // if not OAuth2, then no scopes available at all
    Set<String> grantedScopes = Collections.emptySet();
    if (authentication instanceof OAuth2Authentication) {
      OAuth2Authentication o2auth = (OAuth2Authentication) authentication;
      grantedScopes = getScopes(o2auth);
    }

    return verify(grantedScopes, studyId);
  }

  protected void setAuthorizeScope(String scopeStr) {
    downloadScope = scopeStr;
  }

  protected String getAuthorizeScope() {
    return downloadScope;
  }

  private Set<String> getScopes(@NonNull OAuth2Authentication o2auth) {
    return o2auth.getOAuth2Request().getScope();
  }

  private boolean verify(@NonNull Set<String> grantedScopes, @NonNull final String studyId) {
    val strategy = format(SCOPE_STRATEGY, studyId.toUpperCase(), downloadScope);
    val check = grantedScopes.stream().filter(s -> s.equals(strategy)).collect(toList());
    return !check.isEmpty();
  }


}
