package com.codework.urlvalidator.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class URLClassTest {

    URLClass urlClass;

    @BeforeEach
    void setUp() {
        urlClass = new URLClass();
    }

    @Test
    void getId() {

        Long idValue = 1L;
        urlClass.setId(idValue);

        assertEquals(idValue, urlClass.getId());
    }

    @Test
    void getCheckURL() {
    }

    @Test
    void getCount() {
    }
}