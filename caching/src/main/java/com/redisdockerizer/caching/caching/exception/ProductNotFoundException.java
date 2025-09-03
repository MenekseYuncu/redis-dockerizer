package com.redisdockerizer.caching.caching.exception;

import java.io.Serial;

public class ProductNotFoundException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -2610009822476322150L;

  public ProductNotFoundException() {
        super("Product not found.");
    }
}
