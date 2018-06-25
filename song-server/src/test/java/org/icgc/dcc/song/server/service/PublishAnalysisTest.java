package org.icgc.dcc.song.server.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.common.core.util.stream.Collectors;
import org.icgc.dcc.song.core.utils.RandomGenerator;
import org.icgc.dcc.song.server.converter.FileConverter;
import org.icgc.dcc.song.server.model.ScoreObject;
import org.icgc.dcc.song.server.model.analysis.AbstractAnalysis;
import org.icgc.dcc.song.server.model.analysis.Analysis;
import org.icgc.dcc.song.server.model.entity.file.impl.File;
import org.icgc.dcc.song.server.model.enums.AccessTypes;
import org.icgc.dcc.song.server.model.enums.AnalysisStates;
import org.icgc.dcc.song.server.model.enums.AnalysisTypes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.song.core.utils.RandomGenerator.createRandomGenerator;
import static org.icgc.dcc.song.core.utils.RandomGenerator.randomList;
import static org.icgc.dcc.song.server.model.enums.AnalysisStates.PUBLISHED;
import static org.icgc.dcc.song.server.model.enums.AnalysisStates.UNPUBLISHED;
import static org.icgc.dcc.song.server.model.enums.FileTypes.BAM;
import static org.icgc.dcc.song.server.model.enums.FileTypes.VCF;
import static org.icgc.dcc.song.server.utils.AnalysisGenerator.createAnalysisGenerator;
import static org.icgc.dcc.song.server.utils.TestConstants.DEFAULT_STUDY_ID;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
@Transactional
public class PublishAnalysisTest {

  private static final int MAX_FILES = 10;
  private static final FileConverter FILE_CONVERTER = Mappers.getMapper(FileConverter.class);
  private static final List<File> EMPTY_FILE_LIST = ImmutableList.of();
  private static final List<ScoreObject> EMPTY_SCORE_OBJECT_LIST = ImmutableList.of();
  private static final String DEFAULT_ACCESS_TOKEN = "myAccessToken";

  @Autowired
  AnalysisService service;

  @Autowired
  FileService fileService;

  @Autowired
  StudyService studyService;

  private final RandomGenerator randomGenerator = createRandomGenerator(PublishAnalysisTest.class.getSimpleName());

  /**
   * State
   */
  private List<File> testFiles;
  private AbstractAnalysis testAnalysis;
  private String testAnalysisId;
  private String testStudyId;

  @Before
  public void beforeTest(){
    assertThat(studyService.isStudyExist(DEFAULT_STUDY_ID)).isTrue();
    val randomGenerator = createRandomGenerator(PublishAnalysisTest.class.getSimpleName());
    val analysisGenerator = createAnalysisGenerator(DEFAULT_STUDY_ID, service, randomGenerator);

    this.testAnalysis = analysisGenerator.createDefaultRandomAnalysis(randomGenerator.randomEnum(AnalysisTypes.class));

    // Delete any previous files
    fileService.securedDelete(DEFAULT_STUDY_ID,
        testAnalysis.getFile().stream()
            .map(File::getObjectId)
            .collect(toList()));

    this.testFiles = generateFiles(MAX_FILES, testAnalysis );
    assertThat(testFiles).hasSize(MAX_FILES);
  }

  private int getSomeOf(int numExisting){
    return randomGenerator.generateRandomIntRange(1, numExisting);
  }

  @Test
  public void testNormal(){
    val existingFilesWithMd5 = this.testFiles;
    val existingFilesMissingMd5 = EMPTY_FILE_LIST;
    val nonExistingFiles = EMPTY_FILE_LIST;
    val mockTestData = setupTest(existingFilesWithMd5, existingFilesMissingMd5, nonExistingFiles);

    //Ignoring undefined md5s
    assertThat(service.readState(testAnalysisId)).isEqualTo(UNPUBLISHED);
    service.publish(DEFAULT_ACCESS_TOKEN, testStudyId, testAnalysisId, true );
    assertThat(service.readState(testAnalysisId)).isEqualTo(PUBLISHED);

    //Not ignoring undefined md5s
    service.securedUpdateState(testStudyId, testAnalysisId, UNPUBLISHED);
    assertThat(service.readState(testAnalysisId)).isEqualTo(UNPUBLISHED);
    service.publish(DEFAULT_ACCESS_TOKEN, testStudyId, testAnalysisId, false );
    assertThat(service.readState(testAnalysisId)).isEqualTo(PUBLISHED);
  }

  // all exist, some defined md5s.
  // all exist and no defined md5s

  // some exist and of those, all defined md5s
  // some exist and of those, some defined md5s
  // some exist and of those, no defined md5s

  // none exist

	@Test
	public void testIdeal(){
    val existingFilesWithMd5 = this.testFiles;
    val existingFilesMissingMd5 = EMPTY_FILE_LIST;
    val nonExistingFiles = EMPTY_FILE_LIST;
    setupTest(existingFilesWithMd5, existingFilesMissingMd5, nonExistingFiles);

    //Ignoring undefined md5s
    assertThat(service.readState(testAnalysisId)).isEqualTo(UNPUBLISHED);
    service.publish(DEFAULT_ACCESS_TOKEN, testStudyId, testAnalysisId, true );
    assertThat(service.readState(testAnalysisId)).isEqualTo(PUBLISHED);

    //Not ignoring undefined md5s
    service.securedUpdateState(testStudyId, testAnalysisId, UNPUBLISHED);
    assertThat(service.readState(testAnalysisId)).isEqualTo(UNPUBLISHED);
    service.publish(DEFAULT_ACCESS_TOKEN, testStudyId, testAnalysisId, false );
	}


	@Test
	public void testMismatchingSize(){
    val existingFilesWithMd5 = this.testFiles;
    val existingFilesMissingMd5 = EMPTY_FILE_LIST;
    val nonExistingFiles = EMPTY_FILE_LIST;
    setupTest(existingFilesWithMd5, existingFilesMissingMd5, nonExistingFiles);

    //Ignoring undefined md5s
    assertThat(service.readState(testAnalysisId)).isEqualTo(UNPUBLISHED);
    service.publish(DEFAULT_ACCESS_TOKEN, testStudyId, testAnalysisId, true );
    assertThat(service.readState(testAnalysisId)).isEqualTo(PUBLISHED);

    //Not ignoring undefined md5s
    service.securedUpdateState(testStudyId, testAnalysisId, UNPUBLISHED);
    assertThat(service.readState(testAnalysisId)).isEqualTo(UNPUBLISHED);
    service.publish(DEFAULT_ACCESS_TOKEN, testStudyId, testAnalysisId, false );
	}


	@Test
	public void testMismatchingMd5(){
	}


	@Test
	public void testMismatchingMd5AndSize(){
	}


	@Test
	public void testSomeUndefinedMd5(){
	}


	@Test
	public void testSomeUndefinedMismatchingSize(){
	}


	@Test
	public void testSomeUndefinedMismatchingMd5(){
	}


	@Test
	public void testSomeUndefinedMismatchingMd5AndSize(){
	}


	@Test
	public void testAllUndefined(){
	}


	@Test
	public void testAllUndefinedMismatchingSize(){
	}


	@Test
	public void testSomeExisting(){
	}


	@Test
	public void testNoneExisting(){
	}




  @Test
  public void testPublishAdvanced(){
    val maxSize = 10;
    // 2 datas: all md5s, mixed undefined, all undefined CROSS all exist, some exist, non exist
    //


    val a = analysisGenerator.createDefaultRandomAnalysis(randomGenerator.randomEnum(AnalysisTypes.class));

    // Delete any previous files
    fileService.securedDelete(DEFAULT_STUDY_ID, a.getFile().stream().map(File::getObjectId).collect(toList()));

    // generate new files
    val files = generateFiles(maxSize, a );
    val allExist = files.size();
    val someExist = randomGenerator.generateRandomIntRange(2, allExist);
    val noneExist = 0;

    // all exist and all defined md5s
    val allDefinedMd5s1 = allExist;
    val mockScoreService1 = createMockScoreService(files, allExist, allDefinedMd5s1);
    ReflectionTestUtils.setField(service, "scoreService", mockScoreService1);
    service.publish("sdf", DEFAULT_STUDY_ID, a.getAnalysisId() )



  }

  @Value
  @Builder
  public static class MockTestData {
    private List<File> existingFilesMismatchingMd5;
    private List<File> existingFilesMatchingMd5;
    private List<File> existingFilesMissingMd5;
    private List<File> nonExistingFiles;
    private List<ScoreObject> existingScoreObjectsWithMd5;
    private List<ScoreObject> existingScoreObjectsMissingMd5;

    public List<String> getAllFileObjectIds(){
      val list = Lists.<File>newArrayList();
      list.addAll(existingFilesMissingMd5);
      list.addAll(existingFilesMismatchingMd5);
      list.addAll(existingFilesMatchingMd5);
      list.addAll(nonExistingFiles);
      return list.stream().map(File::getObjectId).collect(toImmutableList());
    }

  }
  enum RangeType{
    ALL,
    SOME,
    NONE;
  }

  @Value
  @Builder
  public static class ExistingCollection{
    private List<File> existingFiles;
    private List<File> nonExistingFiles;
  }

  private ExistingCollection generateExistingCollection(RangeType existingRange){
    List<File> nonExistingFiles = EMPTY_FILE_LIST;
    List<File> existingFiles = EMPTY_FILE_LIST;
    if(existingRange == RangeType.ALL){
      existingFiles = testFiles;
      nonExistingFiles = EMPTY_FILE_LIST;
    } else if (existingRange == RangeType.SOME){
      val numOfSome = randomGenerator.generateRandomIntRange(2, MAX_FILES);
      existingFiles = randomGenerator.randomSublist(testFiles, numOfSome);
      val finalExistingFiles = existingFiles;
      nonExistingFiles = testFiles.stream()
          .filter(x -> !finalExistingFiles.contains(x))
          .collect(toImmutableList());
    }
    return ExistingCollection.builder()
        .existingFiles(existingFiles)
        .nonExistingFiles(nonExistingFiles)
        .build();
  }

  private List<File> filterDefinedMd5Files(List<File> existingFiles, RangeType definedMd5Range){
    if (definedMd5Range == RangeType.ALL){
      return existingFiles;
    } else if (definedMd5Range == RangeType.SOME){
      val size = randomGenerator.generateRandomIntRange(1, existingFiles.size());
      return randomGenerator.randomSublist(existingFiles, size);
    } else if (definedMd5Range == RangeType.NONE){
      return EMPTY_FILE_LIST;
    }
    throw new IllegalStateException("should not be here");
  }


  private MockTestData generateMockTestData(RangeType existingRange, RangeType definedMd5Range,
        RangeType matchingMd5Range, RangeType matchingSizeRange){
    List<File> existingMismatchingMD5Files;
    List<File> existingMatchingMD5Files;
    List<File> existingUndefinedMD5Files;
    List<File> nonExistingFiles;

    val existingCollection = generateExistingCollection(existingRange);
    val definedMd5Files =  filterDefinedMd5Files(existingCollection.getExistingFiles(), definedMd5Range);
    val matchingMd5Files = filterMatchingMd5Files(definedMd5Files, matchingMd5Range);
    val matchingSizeFiles = filterMatchingSizeFiles(existingCollection.getExistingFiles(), matchingSizeRange);


  }

  public MockTestData setupTest(RangeType existingRange, RangeType definedMd5Range,
      RangeType matchingMd5Range, RangeType matchingSizeRange){
  }

  public MockTestData setupTest(List<File> existingFilesWithMd5, List<File> existingFilesMissingMd5,
      List<File> nonExistingFiles){
    val mockScoreService = mock(ScoreService.class);
    val existingScoreObjectsWithMd5 = existingFilesWithMd5.stream()
        .map(FILE_CONVERTER::toScoreObject)
        .collect(toImmutableList());
    val existingScoreObjects = newArrayList(existingScoreObjectsWithMd5);

    val existingScoreObjectsMissingMd5 = existingFilesWithMd5.stream()
        .map(FILE_CONVERTER::copyFile)
        .peek(x -> x.setFileMd5sum(null))
        .map(FILE_CONVERTER::toScoreObject)
        .collect(toImmutableList());
    existingScoreObjects.addAll(existingScoreObjectsMissingMd5);

    for(val scoreObject : existingScoreObjectsWithMd5){
      when(mockScoreService.downloadObject(anyString(), scoreObject.getObjectId())).thenReturn(scoreObject);
      when(mockScoreService.isObjectExist(anyString(), scoreObject.getObjectId())).thenReturn(true);
    }

    for(val file: nonExistingFiles){
      when(mockScoreService.isObjectExist(anyString(), file.getObjectId())) .thenReturn(false);
    }

    ReflectionTestUtils.setField(service, "scoreService", mockScoreService);

    return MockTestData.builder()
        .existingFilesWithMd5(existingFilesWithMd5)
        .existingFilesMissingMd5(existingFilesMissingMd5)
        .nonExistingFiles(nonExistingFiles)
        .existingScoreObjectsMissingMd5(existingScoreObjectsMissingMd5)
        .existingScoreObjectsWithMd5(existingScoreObjectsWithMd5)
        .mockScoreService(mockScoreService)
        .build();
  }

  public ScoreService createMockScoreService(List<File> files, int numExisting, int numWithDefinedMd5){
    val mockScoreService = mock(ScoreService.class);
    val fileConverter = Mappers.getMapper(FileConverter.class);
    // select 'numExisting' amount to actually exist
    val fileSublist = randomGenerator.randomSublist(files, numExisting).stream()
        .map(fileConverter::copyFile)
        .collect(toList());

    // of those existing, choose 'numWithDefinedMd5' amount to have undefined md5sums
    randomGenerator.randomSublist(fileSublist, numExisting-numWithDefinedMd5).forEach(x -> x.setFileMd5sum(null));
    val scoreObjects = fileSublist.stream()
        .map(fileConverter::toScoreObject)
        .collect(toImmutableList());

    // Mock behaviour  for existing scoreObjects
    for(val scoreObject: scoreObjects){
      when(mockScoreService.downloadObject(anyString(), scoreObject.getObjectId()))
          .thenReturn(scoreObject);
      when(mockScoreService.isObjectExist(anyString(), scoreObject.getObjectId()))
          .thenReturn(true);
    }

    // Mock behaviour  for non-existing scoreObjects
    for (val file: files){
      val isNonExistentFile = fileSublist.contains(file);
      if (isNonExistentFile){
        when(mockScoreService.isObjectExist(anyString(), file.getObjectId()))
            .thenReturn(false);
      }
    }
    return mockScoreService;
  }

  public List<File> generateFiles(int maxSize, AbstractAnalysis a){
    return randomList(() -> generateFile(a), maxSize);
  }

  public File generateFile(AbstractAnalysis a){
    val analysisType = AnalysisTypes.resolveAnalysisType(a.getAnalysisType()) ;
    String fileType = null;
    String fileName = randomGenerator.generateRandomUUIDAsString()+".";

    if (analysisType == AnalysisTypes.SEQUENCING_READ){
      fileType = BAM.toString();
      fileName += BAM.getExtension();
    } else if (analysisType == AnalysisTypes.VARIANT_CALL){
      fileType = VCF.toString();
      fileName += VCF.getExtension()+".gz";
    }
    val file = File.builder()
        .studyId(a.getStudy())
        .analysisId(a.getAnalysisId())
        .fileType(fileType)
        .fileAccess(randomGenerator.randomEnum(AccessTypes.class).toString())
        .fileMd5sum(randomGenerator.generateRandomMD5())
        .fileName(fileName)
        .fileSize((long)randomGenerator.generateRandomIntRange(1,100000))
        .objectId(randomGenerator.generateRandomUUIDAsString())
        .build();
    fileService.create(a.getAnalysisId(), a.getStudy(), file);
    return file;
  }



}
