package rox.todd.common.util;

import static java.nio.file.Files.readAttributes;
import static java.util.Arrays.stream;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;

import rox.todd.common.exception.HomeyException;

public class Enums{
  
  
  public enum Service {WIFI, IMAGE}
  
  public enum PathSort {
    DATE_ASC((p1, p2)->dateCompare(p1, p2)),
    DATE_DEC((p1, p2)->dateCompare(p2, p1));
    
    private Comparator<Path> comparator;

    private PathSort(Comparator<Path> comparator) {
      this.comparator = comparator;
    }
    
    public Comparator<Path> getComparator() {
      return comparator;
    }
    
    private static int dateCompare(Path p1, Path p2) {
      try {
        long time1 = readAttributes(p1, BasicFileAttributes.class).creationTime().toMillis();
        long time2 = readAttributes(p2, BasicFileAttributes.class).creationTime().toMillis();
        return Long.compare(time1, time2);
      }
      catch(Exception e) {
        throw new HomeyException(null, null, e);
      }
    }
  }
  
  public enum AllowedImageType {
    GIF("image/gif", ".gif"), JPG("image/jpg", ".jpeg", ".jpg"), PNG("image/png", ".png");
    
    private String mimeType;
    private String[] extensions;
    
    private AllowedImageType(String mimeType, String... extensions) {
      this.mimeType = mimeType;
      this.extensions = extensions;
    }
    
    public String getMimeType() {
      return mimeType;
    }
    
    public boolean suffixMatches(String s) {
      if(s == null) return false;
      return stream(extensions).anyMatch(e->s.toLowerCase().endsWith(e));
    }
    
    public static AllowedImageType toAllowedImageType(String s) {
      return stream(AllowedImageType.values()).filter(it->it.suffixMatches(s)).findFirst().orElse(null);
    }
  }
}