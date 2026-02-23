package rox.todd.service;

import static java.lang.Thread.ofVirtual;
import static java.text.MessageFormat.format;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import rox.todd.common.util.Config;
import rox.todd.common.util.Enums;
import rox.todd.common.util.Util;
import rox.todd.common.util.Const.ConfigPropery;

/**
 * A class of wifi related services.
 */
@Service
public class WifiService {
  private static final Logger log = LoggerFactory.getLogger(WifiService.class);
  private static final Pattern hostAndPortPattern = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,5}$");
  private Config config;
  private List<String> sshCmd;
  
  @Autowired
  public WifiService(Config config) {
    this.config = config;
  }
  
  @PostConstruct
  private void initialize() {
    if(!config.isActive(Enums.Service.WIFI)) {
      log.info("Skipping initialization for: " + Enums.Service.WIFI);
      return;
    }
    
    try {
      String hostAndPort = config.getOrException(ConfigPropery.routerSshHostAndPort);
      if(!hostAndPortPattern.matcher(hostAndPort).matches()) {
        throw new IllegalArgumentException("Invalid routerSshHostAndPort: " + hostAndPort);
      }
      String[] split = hostAndPort.split(":");
      sshCmd = List.of("ssh", "root@" + split[0], "-p", split[1]);
    }
    catch(Exception e) {
      throw new IllegalStateException("Error initializing TaskManager.", e);
    }
  }

  /**
   * Turn wifi on/off.
   * @param on The wifi action to take.
   * @param durationMinutes When turning wifi on, the duration it should stay on (or -1 for infinite).
   */
  public void changeWifi(boolean on, int durationMinutes) {
    List<String> cmd = new ArrayList<>(sshCmd);
    cmd.add(format("ifconfig ath0 {0}; ifconfig ath1 {0}; ifconfig ath1.1 {0};", on ? "up" : "down"));
    
    run(cmd);
    
    if(on && durationMinutes > 0) ofVirtual().start(()->turnOffWifi(durationMinutes));
  }
  
  private void turnOffWifi(int minutes) {
    try {
      Thread.sleep(MINUTES.toMillis(minutes));
      List<String> cmd = new ArrayList<>(sshCmd);
      cmd.add(format("ifconfig ath0 {0}; ifconfig ath1 {0}; ifconfig ath1.1 {0};", "down"));
      run(cmd);
    }
    catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  private void run(List<String> command) {
    try {
      int result = Util.runCommand(null, null, log, command.toArray(new String[0]));
      if(result != 0) throw new RuntimeException("Unexpected result: " + result);
    }
    catch(Exception e) {
      throw new RuntimeException("Error running command: " + command, e);
    }
  }
}
