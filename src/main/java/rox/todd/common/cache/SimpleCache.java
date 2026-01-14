package rox.todd.common.cache;

import static java.lang.String.valueOf;
import static java.time.Instant.ofEpochMilli;
import static java.time.LocalDateTime.ofInstant;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.joining;

import java.io.Serializable;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * A simple thread safe cache.
 * @param <T>
 */
public class SimpleCache<T> {
  protected Map<Object, Object> map = new ConcurrentHashMap<>();
  protected Queue<ValueContainer<?>> expirationQueue = new PriorityQueue<>(10, (v1, v2)->(int)(v1.expirationTime - v2.expirationTime));
  protected long millisToLive = MINUTES.toMillis(2);
  
  public SimpleCache(){ }
  
  public SimpleCache(long millisToLive){
    this.millisToLive = millisToLive;
  }
  
  public void put(String key, T value){
    put(key, value, millisToLive);
  }

  public void put(String key, T value, long millisToLive){
    putValueContainer(new ValueContainer<T>(requireNonNull(key), value, millisToLive));
  }
  
  public T get(String... args){
    ValueContainer<T> valueHolder = getValueContatiner(toKey(args));
    return valueHolder == null ? null : valueHolder.value;
  }
  
  public boolean contains(String... args){
    return getValueContatiner(toKey(args)) != null;
  }
  
  public synchronized void clear() {
    map.clear();
    expirationQueue.clear();
  }
  
  public synchronized T remove(String key){
    ValueContainer<T> vc = getValueContatiner(key);
    if(vc == null) return null;
    
    map.remove(key);
    removeFromExpirationQueue(vc);
    
    return vc.value;
  }
  
  @SuppressWarnings("unchecked")
  protected ValueContainer<T> getValueContatiner(String key){
    removeExpired();
    return (ValueContainer<T>)map.get(key);
  }
  
  final synchronized protected void putValueContainer(ValueContainer<T> valueHolder){
    removeFromExpirationQueue(valueHolder); //Explicitly remove the value holder first. 
    map.put(valueHolder.key, valueHolder);
    expirationQueue.add(valueHolder);
  }
  
  private synchronized void removeFromExpirationQueue(ValueContainer<T> valueHolder){
    expirationQueue.remove(valueHolder);
  }
  
  public static String toKey(String... args){
    return Stream.of(args).collect(joining("~"));
  }
  
  private synchronized void removeExpired(){
    long now = System.currentTimeMillis();

    while(!expirationQueue.isEmpty() && expirationQueue.peek().expirationTime < now){
      map.remove(expirationQueue.poll().key);
    }
  }
  
  @Override
  public String toString(){
    return "{" + map.values().stream().map(v->valueOf(v)).collect(joining(", ")) + "}";
  }
  
  protected static class ValueContainer<T> implements Serializable{
    private static final long serialVersionUID = 1L;
    public String key;
    public T value;
    public long expirationTime;
    
    public ValueContainer(String key, T value, long millisToLive){
      this.key = key;
      this.value = value;
      this.expirationTime = System.currentTimeMillis() + millisToLive;
    }
    

    @Override
    public String toString(){
      String time = ofInstant(ofEpochMilli(expirationTime), ZoneId.systemDefault()).toString();
      return MessageFormat.format("key: {0}, id hash: {1,number,#}, value: {2}, expir: {3}", key, System.identityHashCode(this), value, time);
    }
    
    @Override
    public boolean equals(Object o){
      return o instanceof ValueContainer && ((ValueContainer<?>)o).key.equals(key);
    }
  }
}
