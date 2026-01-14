package rox.todd.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A runtime exception that can hold the reason and context of the exception.
 */
public class HomeyException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  public static final String CONTEXT_DELIM = " ~~~ ";
  public static enum GeneralError implements ExceptionReason { UNKNOWN }
  private ExceptionReason reason = GeneralError.UNKNOWN;
  private Map<String, Object> context;
  private String technicalMessage;
  private String userMessage;
  
  public HomeyException(String technicalMessage) {
    super(technicalMessage);
    this.technicalMessage = technicalMessage;
  }
  
  public HomeyException(String technicalMessage, String userMessage) {
    super(technicalMessage);
    this.technicalMessage = technicalMessage;
    this.userMessage = userMessage;
  }
  
  public HomeyException(String technicalMessage, String userMessage, Exception e) {
    super(technicalMessage, e);
    this.technicalMessage = technicalMessage;
    this.userMessage = userMessage;
  }
  
  public Map<String, Object> getContextMap() {
    return context == null ? context = new LinkedHashMap<>() : context;
  }
  
  public void setContextMap(Map<String, Object> context) {
    this.context = context;
  }

  public ExceptionReason getReason() {
    return reason;
  }

  public <A extends HomeyException> A setReason(ExceptionReason reason) {
    this.reason = reason;
    return me();
  }

  public String getTechnicalMessage() {
    return technicalMessage;
  }

  public <A extends HomeyException> A setTechnicalMessage(String technicalMessage) {
    this.technicalMessage = technicalMessage;
    return me();
  }

  public String getUserMessage() {
    return userMessage;
  }

  public <A extends HomeyException> A setUserMessage(String userMessage) {
    this.userMessage = userMessage;
    return me();
  }

  public <A extends HomeyException> A addContext(String key, Object value) {
    return addContext(key, value, false);
  }
  
  public <A extends HomeyException> A addContext(String key, Object value, boolean overrideExisting) {
    Map<String, Object> map = getContextMap();
    
    if(!map.containsKey(key) || overrideExisting) {
      map.put(key, value);
      return me();
    }

    for(int i=1; i<100; i++) { // Warning: max tries is 100
      String newKey = key + i;
      if(!map.containsKey(newKey)) {
        map.put(newKey, value);
        return me();
      }
    }

    return me();
  }
  
  @SuppressWarnings("unchecked")
  private <A extends HomeyException> A me() {
    return (A)this;
  }
  
  @SuppressWarnings("unchecked")
  public <T> T getContext(String key, T valueWhenNull) {
    Object val = getContextMap().get(key);
    return (T)(val == null ? valueWhenNull : val);
  }
  
  private boolean hasValue(String s) {
    return s != null && !s.isEmpty();
  }
  
  private String contextMapToString() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("reason", reason);
    if(hasValue(technicalMessage)) map.put("technicalMsg", technicalMessage);
    map.putAll(getContextMap());
    if(hasValue(userMessage)) map.put("userMsg", userMessage);
    
    String context = map.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(CONTEXT_DELIM));
    return context;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + " [" + contextMapToString() + "]";
  }
  
  public interface ExceptionReason{}

}