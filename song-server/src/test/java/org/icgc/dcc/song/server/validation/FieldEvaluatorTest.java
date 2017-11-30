package org.icgc.dcc.song.server.validation;

import lombok.val;
import org.icgc.dcc.song.core.exceptions.ServerException;
import org.icgc.dcc.song.server.deserializers.FieldEvaluator;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.JSON_FIELD_DOES_NOT_EXIST;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.REQUIRED_JSON_FIELD_IS_EMPTY;
import static org.icgc.dcc.song.core.exceptions.ServerErrors.REQUIRED_JSON_FIELD_IS_NULL;
import static org.icgc.dcc.song.server.deserializers.FieldEvaluator.createOptionalFieldEvaluator;
import static org.icgc.dcc.song.server.deserializers.FieldEvaluator.createRequiredFieldEvaluator;
import static org.icgc.dcc.song.server.utils.TestFiles.getJsonNodeFromClasspath;

public class FieldEvaluatorTest {

  private static FieldEvaluator optionalFieldEvaluator;
  private static FieldEvaluator requiredFieldEvaluator;

  @BeforeClass
  public static void init(){
    val testNode = getJsonNodeFromClasspath("documents/fieldEvaluatorFixture.json");
    optionalFieldEvaluator = createOptionalFieldEvaluator(testNode);
    requiredFieldEvaluator = createRequiredFieldEvaluator(testNode);
  }

  @Test
  public void testOptionalString(){
    assertThat(optionalFieldEvaluator.getString("normalStringField")).isEqualTo("somethingNormal");
    assertThat(optionalFieldEvaluator.getString("nullStringField")).isNull();
    assertThat(optionalFieldEvaluator.getString("missingStringField")).isNull();


    // Empty strings are illegal
    val emptyStringEx = catchThrowable(() -> optionalFieldEvaluator.getString("emptyStringField") );
    assertThat(emptyStringEx).isInstanceOf(ServerException.class);
    val emptyStringServerException = (ServerException)emptyStringEx;
    assertThat(emptyStringServerException.getSongError().getErrorId()).isEqualTo(REQUIRED_JSON_FIELD_IS_EMPTY.getErrorId());
  }


  @Test
  public void testOptionalBoolean() {
    assertThat(optionalFieldEvaluator.getBoolean("trueBooleanField")).isTrue();
    assertThat(optionalFieldEvaluator.getBoolean("falseBooleanField")).isFalse();
    assertThat(optionalFieldEvaluator.getBoolean("nullBooleanField")).isNull();
    assertThat(optionalFieldEvaluator.getBoolean("missingBooleanField")).isNull();
  }


  @Test
  public void testOptionalLong() {
    assertThat(optionalFieldEvaluator.getLong("normalLongField")).isEqualTo(1234L);
    assertThat(optionalFieldEvaluator.getLong("nullLongField")).isNull();
    assertThat(optionalFieldEvaluator.getLong("missingLongField")).isNull();
  }

  @Test
  public void testOptionalInteger() {
    assertThat(optionalFieldEvaluator.getInt("normalIntegerField")).isEqualTo(5678);
    assertThat(optionalFieldEvaluator.getInt("nullIntegerField")).isNull();
    assertThat(optionalFieldEvaluator.getInt("missingIntegerField")).isNull();
  }

  @Test
  public void testRequiredString(){
    assertThat(optionalFieldEvaluator.getString("normalStringField")).isEqualTo("somethingNormal");
    val nullStringEx = catchThrowable(() -> requiredFieldEvaluator.getString("nullStringField"));
    val missingStringEx = catchThrowable(() -> requiredFieldEvaluator.getString("missingStringField"));
    val emptyStringEx = catchThrowable(() -> requiredFieldEvaluator.getString("emptyStringField") );
    assertThat(nullStringEx).isInstanceOf(ServerException.class);
    assertThat(missingStringEx).isInstanceOf(ServerException.class);
    assertThat(emptyStringEx).isInstanceOf(ServerException.class);

    val nullStringServerException = (ServerException)nullStringEx;
    val missingStringServerException = (ServerException)missingStringEx;
    val emptyStringServerException = (ServerException)emptyStringEx;

    // Empty strings are illegal
    assertThat(nullStringServerException.getSongError().getErrorId()).isEqualTo(REQUIRED_JSON_FIELD_IS_NULL.getErrorId());
    assertThat(missingStringServerException.getSongError().getErrorId()).isEqualTo(JSON_FIELD_DOES_NOT_EXIST.getErrorId());
    assertThat(emptyStringServerException.getSongError().getErrorId()).isEqualTo(REQUIRED_JSON_FIELD_IS_EMPTY.getErrorId());
  }


  @Test
  public void testRequiredBoolean() {
    assertThat(requiredFieldEvaluator.getBoolean("trueBooleanField")).isTrue();
    assertThat(requiredFieldEvaluator.getBoolean("falseBooleanField")).isFalse();

    val nullBooleanEx = catchThrowable(() -> requiredFieldEvaluator.getBoolean("nullBooleanField"));
    val nullBooleanServerException = (ServerException)nullBooleanEx;
    assertThat(nullBooleanServerException.getSongError().getErrorId()).isEqualTo(REQUIRED_JSON_FIELD_IS_NULL.getErrorId());

    val missingBooleanEx = catchThrowable(() -> requiredFieldEvaluator.getBoolean("missingBooleanField"));
    val missingBooleanServerException = (ServerException)missingBooleanEx;
    assertThat(missingBooleanServerException.getSongError().getErrorId()).isEqualTo(JSON_FIELD_DOES_NOT_EXIST.getErrorId());
  }


  @Test
  public void testRequiredLong() {
    assertThat(requiredFieldEvaluator.getLong("normalLongField")).isEqualTo(1234L);

    val nullLongEx = catchThrowable(() -> requiredFieldEvaluator.getLong("nullLongField"));
    val nullLongServerException = (ServerException)nullLongEx;
    assertThat(nullLongServerException.getSongError().getErrorId()).isEqualTo(REQUIRED_JSON_FIELD_IS_NULL.getErrorId());

    val missingLongEx = catchThrowable(() -> requiredFieldEvaluator.getLong("missingLongField"));
    val missingLongServerException = (ServerException)missingLongEx;
    assertThat(missingLongServerException.getSongError().getErrorId()).isEqualTo(JSON_FIELD_DOES_NOT_EXIST.getErrorId());
  }

  @Test
  public void testRequiredInteger() {
    assertThat(requiredFieldEvaluator.getInt("normalIntegerField")).isEqualTo(5678);

    val nullIntegerEx = catchThrowable(() -> requiredFieldEvaluator.getInt("nullIntegerField"));
    val nullIntegerServerException = (ServerException)nullIntegerEx;
    assertThat(nullIntegerServerException.getSongError().getErrorId()).isEqualTo(REQUIRED_JSON_FIELD_IS_NULL.getErrorId());

    val missingIntegerEx = catchThrowable(() -> requiredFieldEvaluator.getInt("missingIntegerField"));
    val missingIntegerServerException = (ServerException)missingIntegerEx;
    assertThat(missingIntegerServerException.getSongError().getErrorId()).isEqualTo(JSON_FIELD_DOES_NOT_EXIST.getErrorId());

  }


}
