package rox.todd.common.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rox.todd.common.cache.AutoRefreshCache.ValueFetcher;

public class AutoRefreshCacheTest {
  protected static final long NO_SLEEP = -1L;
  private AutoRefreshCache<String> cache;
  
  @BeforeEach
  public void before(){
    cache = new AutoRefreshCache<>(200, new Fetcher());
  }

  @Test
  public void test(){
    check("A", "A-0", NO_SLEEP);
    check("B", "B-0", NO_SLEEP);
    check("A", "A-1", 600);
    check("B", "B-1", NO_SLEEP);
  }
  
  @Test
  public void testWithThreads() throws Throwable{
    //Test that multiple threads using the cache works.
    SimpleCacheTest.testWithThreads(50, id->{
      check(id+"", id + "-0", NO_SLEEP);
      check(id+"", id + "-1", 600);
    });
  }
  
  protected void check(String key, String value, long millisToSleep){
    if(millisToSleep != NO_SLEEP) sleep(millisToSleep);
    expect(key, value);
  }
  
  protected void expect(String key, String expectedValue){
    String value = cache.get(key);
    assertEquals(expectedValue, value);
  }
  
  protected static void sleep(long millis){
    try { Thread.sleep(millis); }catch (InterruptedException e) {}
  }
  
  //Supplies a value made up of the key and the amount of times the supplier has been called for that key.
  private class Fetcher implements ValueFetcher<String>{
    private Map<String, Integer> countMap = new ConcurrentHashMap<>();
    
    @Override
    public String fetchValue(String... args) throws Exception {
      String key = SimpleCache.toKey(args);
      Integer count = countMap.containsKey(key) ? countMap.get(key) : 0;
      countMap.put(key, count + 1);      
      return key + "-" + count;
    }
  }
}
