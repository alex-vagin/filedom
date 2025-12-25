package com.alexvagin.filedom.checkers;

import lombok.Builder;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

@Builder
public class VideoFileChecker extends FileChecker {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(VideoFileChecker.class);
    @Builder.Default private String pathToFFMPEG = "C:\\Program Files\\ffmpeg\\bin\\ffmpeg.exe";

    public VideoFileChecker(final String pathToFFMPEG) {
        this.pathToFFMPEG = pathToFFMPEG;
    }

    @Override
    public boolean test(Path path) {
        ProcessBuilder pb = new ProcessBuilder(pathToFFMPEG, "-i", path.toString(), "-f", "null", "-");
        int exitCode = 0;
        try {
            exitCode = pb.start().waitFor();
            if (exitCode != 0) {
                LOG.error("ffmpeg reported error for file {}: exit code {}", path, exitCode);
            }
        } catch (InterruptedException | IOException e) {
            LOG.error("ffmpeg run exception", e);
            return false;
        }
        return exitCode == 0;
    }
}
