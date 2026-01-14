package rox.todd.common.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SimpleCacheTest {
  protected static final long NO_SLEEP = -1L;
  protected static final long USE_DEFAULT_TTL = -1L;
  private SimpleCache<Long> cache;
  
  @BeforeEach
  public void before(){
    cache = new SimpleCache<>(200);
  }

  @Test
  public void testGeneralUse(){
    cacheAndVerify("1", 1L,   NO_SLEEP, USE_DEFAULT_TTL); //Test with the default time.
    cacheAndVerify("2", 2L,   NO_SLEEP, 600); //Test with a custom time.
    cacheAndVerify("3", null, NO_SLEEP, USE_DEFAULT_TTL); //Test that you can enter null as a value.
    
    verifyValue("1", null, 400); //After 400 millis, this value should have expired.
    verifyValue("2", 2L,   NO_SLEEP);//This value has a TTL of 600, so should still be present.
  }
  
  @Test
  public void testClear(){
    cacheAndVerify("1", 1L, NO_SLEEP, 6000);
    cache.clear();
    verifyValue("1", null, NO_SLEEP); //Check that value got removed.
  }
  
  @Test
  public void testRemove(){
    cacheAndVerify("1", 1L, NO_SLEEP, 6000);
    cache.remove("1");
    verifyValue("1", null, NO_SLEEP); //Check that value got removed.
    
    cacheAndVerify("1", 1L, NO_SLEEP, 6000);
    cacheAndVerify("1", 1L, NO_SLEEP, 200);
    
    verifyValue("1", 1L, NO_SLEEP); //Check that the value is still there...
    verifyValue("1", null, 300); //but has now expired after 200ms.
  }
  
  @Test
  public void testGeneralUseWithThreads() throws Throwable{
    //Test that multiple threads using the cache works.
    testWithThreads(50, id->{
      cacheAndVerify(id+"", (long)id, NO_SLEEP, USE_DEFAULT_TTL);
      verifyValue(id+"", null, 400);
    });
  }
  
  @Test
  public void testRemoveWithThreads() throws Throwable{
    cacheAndVerify("1", 1L, NO_SLEEP, 6000);
    testWithThreads(50, id->{
      cache.remove("1");
      verifyValue("1", null, NO_SLEEP);
    });
  }
  
  @Test
  public void testExpirationQueueWithThreads() throws Throwable{
    testWithThreads(50, id->{
      Random r = new Random(hashCode());
      sleep(r.nextInt(60));
      
      if(id == 40) cacheAndVerify("2", 2L, NO_SLEEP, 60000); //One thread adds "2".
      else if(id % 2 == 0) cache.remove("1"); //Half will remove "1".
      else cache.put("1", 1L, 60000); //Half will add "1".
      
      sleep(r.nextInt(60));
      cache.get("1");  //Not testing anything with this, just causing some background noise.
    });
    
    verifyValue("2", 2L, NO_SLEEP);
    //Make sure the map is the same size as the queue and the queue contains the same instances that are in the map.
    assertEquals(cache.map.size(), cache.expirationQueue.size());
    for(Object valueHolder : cache.map.values()) assertTrue(cache.expirationQueue.stream().anyMatch(v->v==valueHolder));
  }
  
  public static void testWithThreads(int numberOfThreads, Worker worker) throws Throwable{
    //Test that multiple threads using the cache works.
    CyclicBarrier startingLine = new CyclicBarrier(numberOfThreads);
    List<CacheAdder> adders = new ArrayList<>(numberOfThreads);
    
    for(int i=0; i<numberOfThreads; i++){
      CacheAdder adder = new CacheAdder(i, startingLine, worker);
      adders.add(adder);
      adder.start();
    }
    
    for(CacheAdder adder : adders){
      adder.join();
      if(adder.throwable != null) throw adder.throwable;
    }
  }
  

  protected void cacheAndVerify(String key, Long value, long millisToSleep, long millisToLive){
    if(millisToLive == USE_DEFAULT_TTL) cache.put(key, value);
    else cache.put(key, value, millisToLive);
    
    verifyValue(key, value, millisToSleep);
  }
  
  protected void verifyValue(String key, Long value, long millisToSleep){
    if(millisToSleep != NO_SLEEP) sleep(millisToSleep);
    expect(key, value);
  }
  
  protected void expect(String key, Long expectedValue){
    Long value = cache.get(key);
    Assertions.assertEquals(expectedValue, value);
  }
  
  protected static void sleep(long millis){
    try { Thread.sleep(millis); }catch (InterruptedException e) {}
  }
  
  @FunctionalInterface
  public interface Worker{
    public void doWork(int id) throws Exception;
  }
  
  public static class CacheAdder extends Thread{
    public Throwable throwable;
    public int id;
    public CyclicBarrier startingLine;
    public Worker worker;
    
    public CacheAdder(int id, CyclicBarrier startingLine, Worker worker) {
      this.id = id;
      this.startingLine = startingLine;
      this.worker = worker;
    }

    @Override
    public void run(){
      try{
        startingLine.await(3, TimeUnit.SECONDS);
        worker.doWork(id);
      }catch(Throwable t){
        throwable = t;
      }
    }
  }
}
