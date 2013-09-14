package com.ecwidizer.api;

import java.util.List;

/**
 * Created by basil on 13.09.13.
 */
public class CreateProductRequest {
    public int ownerid;
    public String name, description;
    public Double price, weight;
    public List<String> images;
}
