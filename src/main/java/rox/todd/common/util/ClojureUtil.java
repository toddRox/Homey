package rox.todd.common.util;

import static java.text.MessageFormat.format;

import org.springframework.stereotype.Service;

import clojure.java.api.Clojure;
import clojure.lang.AFn;
import clojure.lang.IFn;
import jakarta.annotation.PostConstruct;

@Service
public class ClojureUtil {
  
  @PostConstruct
  public void initialize() {
    //Verify all Clojure functions exist.
    for(ClojureFunction cf : ClojureFunction.values()) cf.name();
  }
  
  public static AFn funct(Function f) {
    return new FunctionImpl(f);
  }

  @FunctionalInterface
  public interface Function {
    Object apply(Object... args);
  }

  private static class FunctionImpl extends AFn {
    private final Function fn;

    public FunctionImpl(Function fn) {
      this.fn = fn;
    }
    
    @Override
    public Object invoke() {
      return fn.apply();
    }

    @Override
    public Object invoke(Object arg) {
      return fn.apply(arg);
    }

    @Override
    public Object invoke(Object arg1, Object arg2) {
      return fn.apply(arg1, arg2);
    }

    @Override
    public Object applyTo(clojure.lang.ISeq args) {
      return fn.apply(clojure.lang.RT.toArray(args));
    }
  }
  
  public static IFn getClojureFunction(String fileName, String functionName) {

    try {
      IFn require = Clojure.var("clojure.core", "require");
      require.invoke(Clojure.read(fileName));
      return Clojure.var(fileName, functionName);
    }
    catch(Exception e) {
      throw new IllegalArgumentException(format("Error finding function {0}.{1}.", fileName, functionName), e);
    }
  }

  public enum ClojureFunction {
    Search("rox.todd.util", "search"),
    Clj2java("rox.todd.util", "clj2java"),
    ;

    private ClojureFunction(String file, String functionName) {
      function = getClojureFunction(file, functionName);
    }

    private IFn function;
    
    @SuppressWarnings("unchecked")
    public <T> T run(Object... args) {
      Object cljResult = invoke(args);
      Object javaResult = Clj2java.invoke(cljResult);
      return (T) javaResult;
    }

    private Object invoke(Object... a) {
      return switch (a.length) { 
        case 0  -> function.invoke();
        case 1  -> function.invoke(a[0]);
        case 2  -> function.invoke(a[0], a[1]);
        case 3  -> function.invoke(a[0], a[1], a[2]);
        case 4  -> function.invoke(a[0], a[1], a[2], a[3]);
        case 5  -> function.invoke(a[0], a[1], a[2], a[3], a[4]);
        case 6  -> function.invoke(a[0], a[1], a[2], a[3], a[4], a[5]);
        case 7  -> function.invoke(a[0], a[1], a[2], a[3], a[4], a[5], a[6]);
        case 8  -> function.invoke(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7]);
        case 9  -> function.invoke(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7], a[8]);
        case 10 -> function.invoke(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7], a[8], a[9]);
        case 11 -> function.invoke(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7], a[8], a[9], a[10]);
        case 12 -> function.invoke(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7], a[8], a[9], a[10], a[11]);
        default -> throw new IllegalArgumentException("Cannot (yet) invoke using " + a.length + " argumenets.");
      };
    }
  }
}
