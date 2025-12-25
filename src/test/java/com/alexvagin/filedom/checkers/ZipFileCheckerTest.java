package com.alexvagin.filedom.checkers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZipFileCheckerTest {

    @Test
    void test() {
        ZipFileChecker zipFileChecker = new ZipFileChecker();
        boolean result = zipFileChecker.test(this.getClass().getClassLoader().getResourceAsStream("test.zip"));
        assertTrue(result);
    }
}