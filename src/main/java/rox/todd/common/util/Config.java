package rox.todd.common.util;

import static java.util.stream.Collectors.toSet;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import rox.todd.common.util.Const.ConfigPropery;

@Service
public class Config {
  private static final Logger log = LoggerFactory.getLogger(Config.class);
  private final Map<String, String> configMap = new ConcurrentHashMap<>();
  private Set<Enums.Service> activeServices = new HashSet<>();      

  @PostConstruct
  private void initialize() {
    Properties props = new Properties();
    String configFile = System.getenv(Const.ENV_CONFIG_FILE);

    log.info("Reading config from: " + configFile);    
    
    try (FileInputStream fis = new FileInputStream(configFile)) {
      
      props.load(fis);
      for (Map.Entry<Object, Object> entry : props.entrySet()) {
        String val = entry.getValue().toString().trim();
        if(!val.isEmpty()) configMap.put(entry.getKey().toString(), val);
      }
      
      activeServices = Stream.of(get(ConfigPropery.activeServices, "").split(",", -1)).map(s->Enums.Service.valueOf(s)).collect(toSet());
    } catch (IOException e) {
      throw new RuntimeException("Failed to load configuration: " + configFile, e);
    }
  }

  public String get(String key) {
    return configMap.get(key);
  }

  public String get(String key, String defaultValue) {
    return configMap.getOrDefault(key, defaultValue);
  }
  
  public boolean isActive(Enums.Service s) {
    return activeServices.contains(s);
  }
  
  public String getOrException(String key) {
    String val = get(key);
    if(val == null) throw new IllegalStateException("Missing configuration for key: " + key);
    return val;
  }
}