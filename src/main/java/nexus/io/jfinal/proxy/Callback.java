package nexus.io.jfinal.proxy;

/**
 * Callback
 */
@FunctionalInterface
public interface Callback {
  public Object call(Object[] args) throws Throwable;
}
