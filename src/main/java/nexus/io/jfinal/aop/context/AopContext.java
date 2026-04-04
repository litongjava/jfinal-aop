package nexus.io.jfinal.aop.context;

import lombok.Data;
import nexus.io.jfinal.aop.scanner.ComponentScanner;

@Data
public class AopContext {
  private static AopContext me = new AopContext();

  public static AopContext me() {
    return me;
  }

  private AopContext() {
    
  }
  
  private ComponentScanner componentScanner;
}
