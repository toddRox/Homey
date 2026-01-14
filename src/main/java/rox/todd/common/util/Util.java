package rox.todd.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nullable;

public class Util {
  private static final Logger log = LoggerFactory.getLogger(Util.class);
  
  public static int runCommand(@Nullable File dir, @Nullable StringBuilder sb, @Nullable Logger log, String... cmd) throws InterruptedException, IOException {
    if(log != null) log.debug("cmd: " + Arrays.toString(cmd));
    
    ProcessBuilder pb = new ProcessBuilder(cmd).redirectErrorStream(true);
    if(dir != null) pb.directory(dir);
    Process p = pb.start();
    
    try(BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))){
      String line;
      while ((line = in.readLine()) != null) {
        if(sb != null) sb.append(line);
      }
    }

    return p.waitFor();
  }
  
  public static boolean hasValue(String s) {
    return s != null && !s.isEmpty();
  }
  
  public static String string(Object o) {
    try {
      return o == null ? "" : o.toString();
    }
    catch(Exception e) {
      log.error(e.getMessage(), e);
      return "";
    }
  }
}
