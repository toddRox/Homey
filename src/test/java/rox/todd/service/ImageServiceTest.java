package rox.todd.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static rox.todd.common.util.TestUtil.getConfig;
import static rox.todd.common.util.TestUtil.make;
import static rox.todd.common.util.Util.string;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import rox.todd.common.util.Const.ConfigPropery;
import rox.todd.common.util.Enums.Service;
import rox.todd.model.DirectoryData;

public class ImageServiceTest {
  private static final Logger log = LoggerFactory.getLogger(ImageServiceTest.class);
  private ImageService is;
  private Path workSpace;

  @BeforeEach
  public void before() throws IOException{
    workSpace = Files.createTempDirectory("ImageServiceTest_" + UUID.randomUUID());
    log.info("Working in: " + workSpace);
    make(workSpace, "thumbs", "images");
    
    is = new ImageService(getConfig(Map.of(
        ConfigPropery.activeServices, Service.IMAGE.toString(),
        ConfigPropery.baseImageDir, workSpace.resolve("images").toString(),
        ConfigPropery.baseThumbnailDir, workSpace.resolve("thumbs").toString()
    )));
    
    is.initialize();
  }
  
  @AfterEach
  public void after() throws IOException{
    if(workSpace != null) {
      FileSystemUtils.deleteRecursively(workSpace);
      workSpace = null;
    }
  }
  
  private void makeImages(String dir, String... names) throws IOException {
    for(String n : names) make(workSpace, "images/" + dir + "/" + n);
  }
  
  @Test
  public void testGetDirectoryData() throws IOException {
    //Test best case
    makeImages("2000", "1.jpg", "2.jpg", "3.jpg");
    makeImages("2003", "1.jpg", "2.jpg", "3.jpg", "4.jpg");
    eval(is.getDirectoryData(false), Map.of(2000, 3, 2003, 4));
    
    //Test the test, making sure there is a  failure when counts don't match
    eval(is.getDirectoryData(false), Map.of(2000, 1, 2003, 99), "expected: <99> but was: <4>");
    
    //Test non image files and other directories are ignore.
    makeImages("2003", "1.txt", "aNonNumericDir/99.jpg");
    eval(is.getDirectoryData(false), Map.of(2000, 3, 2003, 4));
  }

  private void eval(List<DirectoryData> actual, Map<Integer, Integer> expected) {
    assertEquals(expected.size(), actual.size());
    
    for(DirectoryData d : actual) {
      Long expectedCount = expected.getOrDefault(d.year(), -1).longValue();
      if(expectedCount == -1) throw new IllegalStateException("There was no count found for year: " + d.year());
      assertEquals(expectedCount, d.fileCount());
    }
  }
  
  private void eval(List<DirectoryData> actual, Map<Integer, Integer> expected, String exceptionMsgSubstring) {
    try {
      eval(actual, expected);
      throw new IllegalStateException("An exception was expected with the substring: " + exceptionMsgSubstring);
    }
    catch(Throwable e) {
      if(!string(e.getMessage()).contains(exceptionMsgSubstring)) {
        throw new IllegalStateException("The expected substring '" + exceptionMsgSubstring + "' was not found in the exception.", e);
      }
    }
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
}
