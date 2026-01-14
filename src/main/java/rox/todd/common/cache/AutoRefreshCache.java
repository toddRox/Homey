package rox.todd.common.cache;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rox.todd.common.cache.SimpleCache.ValueContainer;
import rox.todd.common.exception.HomeyException;

/**
 * A cache that automatically refreshes it's values after expiration.
 * @param <T> The type of value being cached
 */
public class AutoRefreshCache<T> {
  private static final Logger log = LoggerFactory.getLogger(AutoRefreshCache.class);
  private SimpleCache<T> simpleCache;
  private ValueFetcher<T> valueFetcher;
  
  public AutoRefreshCache(ValueFetcher<T> valueFetcher){
    this.simpleCache = new SimpleCache<T>();
    this.valueFetcher = Objects.requireNonNull(valueFetcher);
  }
  
  public AutoRefreshCache(long millisToLive, ValueFetcher<T> valueFetcher){
    this.simpleCache = new SimpleCache<T>(millisToLive);
    this.valueFetcher = Objects.requireNonNull(valueFetcher);
  }
  
  public T get(String... args){
    String key = SimpleCache.toKey(args);
    ValueContainer<T> valueHolder = simpleCache.getValueContatiner(key);
    
    if(valueHolder == null){
      synchronized(valueFetcher) {
        if((valueHolder = simpleCache.getValueContatiner(key)) == null) {
          try {
            T value = valueFetcher.fetchValue(args);
            valueHolder = new ValueContainer<T>(key, value, simpleCache.millisToLive);
            simpleCache.putValueContainer(valueHolder);
            valueFetcher.afterValueFetch(value, args);
            log.trace("Refreshing cache: " + (value == null ? "null" : value.getClass().getSimpleName()));
          }
          catch (Exception e) {
            throw new HomeyException(e.getMessage(), "", e).addContext("key", key);
          }
        }
      }
    }
    
    return valueHolder.value;
  }

  @FunctionalInterface
  public interface ValueFetcher<T>{
    public T fetchValue(String... args) throws Exception;
    public default void afterValueFetch(T t, String... args) throws Exception{}
  }

}
