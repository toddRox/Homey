package rox.todd.common.util;

import static java.util.stream.Collectors.toSet;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import rox.todd.common.util.Const.ConfigPropery;
import rox.todd.common.util.Enums.Service;

@org.springframework.stereotype.Service
public class Config {
  private static final Logger log = LoggerFactory.getLogger(Config.class);
  private Map<String, String> configMap;
  private Set<Service> activeServices;

  @PostConstruct
  private void initialize() {
    var props = new Properties();
    var configFile = System.getenv(Const.ENV_CONFIG_FILE);
    var configMap_tmp = new HashMap<String, String>();

    log.info("Reading config from: " + configFile);    
    
    try (FileInputStream fis = new FileInputStream(configFile)) {
      props.load(fis);
      
      for (Map.Entry<Object, Object> entry : props.entrySet()) {
        String val = entry.getValue().toString().trim();
        if(!val.isEmpty()) configMap_tmp.put(entry.getKey().toString(), val);
      }
      
      manageConfigMap(configMap_tmp);
      
    } catch (IOException e) {
      throw new RuntimeException("Failed to load configuration: " + configFile, e);
    }
  }
  
  void manageConfigMap(Map<String, String> configMap_tmp) {
    this.configMap = Map.copyOf(configMap_tmp);
    
    var activeServices_tmp = new HashSet<Service>();
    var activeServicesSplit = get(ConfigPropery.activeServices, "").split(",", -1);
    activeServices_tmp.addAll(Stream.of(activeServicesSplit).map(Service::valueOf).collect(toSet()));
    
    this.activeServices = Set.copyOf(activeServices_tmp);
  }
  
  public String get(String key) {
    return configMap.get(key);
  }

  public String get(String key, String defaultValue) {
    return configMap.getOrDefault(key, defaultValue);
  }
  
  public boolean isActive(Service s) {
    return activeServices.contains(s);
  }
  
  public String getOrException(String key) {
    String val = get(key);
    if(val == null) throw new IllegalStateException("Missing configuration for key: " + key);
    return val;
  }
}