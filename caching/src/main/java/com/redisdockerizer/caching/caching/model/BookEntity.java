package com.redisdockerizer.caching.caching.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookEntity {

    private String id;


    private String title;


    private String author;

    private int publishedYear;

}
