package rox.todd.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class TestUtil {

  public static TestConfig getConfig(Map<String, String> configMap) {
    return new TestConfig(configMap);
  }
  
  /**
   * Creates a bunch of dirs or files.
   * NOTE: a file name MUST contain a dot, or it will be assumed to be a directory.
   * @param root The root path
   * @param filesOrDirs strings representing file/directory paths
   * @throws IOException
   */
  public static void make(Path root, String... filesOrDirs) throws IOException {
    
    for(String fod : filesOrDirs) {
      var isFile = fod.contains(".");
      var path = Paths.get(fod);
      var dirs = root.resolve(isFile ? path.getParent() : path).toFile();
      
      if(!dirs.exists() && !dirs.mkdirs()) throw new IllegalStateException("Could not create: " + dirs);
      
      if(isFile) {
        File file = root.resolve(path).toFile();
        if(!file.exists() && !file.createNewFile()) throw new IllegalStateException("Could not create: " + file);
      }
    }
  }
  
  private static class TestConfig extends Config{
    public TestConfig(Map<String, String> configMap) {
      manageConfigMap(configMap);
    }
  }
}
