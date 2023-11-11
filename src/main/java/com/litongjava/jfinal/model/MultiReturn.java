package com.litongjava.jfinal.model;

import lombok.Data;

@Data
public class MultiReturn<R1, R2, R3> {
  private int Ralue;
  private boolean ok;
  private Exception e;
  private R1 r1;
  private R2 r2;
  private R3 r3;

  public MultiReturn(boolean ok, R1 R1) {
    this.ok = ok;
    this.r1 = R1;
  }

  public MultiReturn(boolean ok, R1 r1, R2 r2) {
    this.ok = ok;
    this.r1 = r1;
    this.r2 = r2;
  }

  public MultiReturn(boolean ok, R1 r1, R2 r2, R3 r3) {
    this.ok = ok;
    this.r1 = r1;
    this.r2 = r2;
    this.r3 = r3;
  }
}
