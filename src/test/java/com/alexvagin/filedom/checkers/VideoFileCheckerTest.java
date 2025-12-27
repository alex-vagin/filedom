package com.alexvagin.filedom.checkers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
// TODO: fix test for non-windows environments
class VideoFileCheckerTest {
    @Disabled
    @Test
    void test() {
        VideoFileChecker videoFileChecker = VideoFileChecker.builder()
                .pathToFFMPEG("ffmpeg.exe")
                .build();

        String resource = this.getClass().getClassLoader().getResource("test.mp4").getPath().substring(1);

        boolean result = videoFileChecker.test(Path.of(resource));
        assertTrue(result);
    }
}