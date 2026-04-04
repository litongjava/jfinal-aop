package nexus.iojfinal.proxy;

/**
 * Callback
 */
@FunctionalInterface
public interface Callback {
  public Object call(Object[] args) throws Throwable;
}
