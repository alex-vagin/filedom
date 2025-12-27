package com.alexvagin.filedom.checkers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Predicate;

import lombok.Data;

@Data
public abstract class FileChecker implements Predicate<Path> {
    private static final Logger LOG = LoggerFactory.getLogger(FileChecker.class);
    private String[] extensions;
    public final long maxBytes = 1024L * 1024 * 1024;        // 1 GB

    public boolean checkPath(final Path path) {
        if (path == null) {
            LOG.error("Path is null");
            return false;
        }

        if (!Files.exists(path)) {
            LOG.error("File {} does not exist", path);
            return false;
        }

        if (!Files.isRegularFile(path)) {
            LOG.error("Not a regular file: {}", path);
            return false;
        }

        try {
            final long size = Files.size(path);
            if (size <= 0) {
                LOG.error("Empty file {}", path);
                return false;
            }

            if (size > maxBytes) {
                LOG.error("File {} is too large: limt is {}, actual size is {}", path, maxBytes, size);
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        return isMyExtenisonWitLog(path);
    }

    public boolean isMyExtenison(final Path path) {
        String fileExtension = getFileExtension(path);
        return Arrays.stream(extensions).anyMatch(fileExtension::equalsIgnoreCase);
    }

    public boolean isMyExtenisonWitLog(final Path path) {
        if (isMyExtenison(path)) {
            return true;
        }
        LOG.error("File extension {} not in allowed extension list: {}", getFileExtension(path), String.join(",", extensions));
        return false;
    }


    public static String getFileExtension(final Path path) {
        final String fileName = path.getFileName().toString();
        final int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }
}
