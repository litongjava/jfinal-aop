package nexus.iojfinal.aop.context;

import lombok.Data;
import nexus.iojfinal.aop.scanner.ComponentScanner;

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
