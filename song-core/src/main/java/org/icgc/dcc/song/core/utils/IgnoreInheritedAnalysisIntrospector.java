package org.icgc.dcc.song.core.utils;

import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IgnoreInheritedAnalysisIntrospector<T> extends JacksonAnnotationIntrospector {

  @NonNull private final Class<T> type;

  @Override
  public boolean hasIgnoreMarker(final AnnotatedMember m) {
    return m.getDeclaringClass() == type || super.hasIgnoreMarker(m);
  }

  public static <T> IgnoreInheritedAnalysisIntrospector<T> createIgnoreInheritedIntrospector(Class<T> type) {
    return new IgnoreInheritedAnalysisIntrospector<T>(type);
  }

}
