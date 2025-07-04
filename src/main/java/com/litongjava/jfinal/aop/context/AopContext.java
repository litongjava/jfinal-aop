package com.litongjava.jfinal.aop.context;

import com.litongjava.jfinal.aop.scanner.ComponentScanner;

import lombok.Data;

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
