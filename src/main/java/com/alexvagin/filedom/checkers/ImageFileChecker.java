package com.alexvagin.filedom.checkers;

import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

@Data
public class ImageFileChecker extends FileChecker {
    private static final Logger LOG = LoggerFactory.getLogger(ImageFileChecker.class);

    public static final int  MAX_WIDTH = 20000;                    // 20k px
    public static final int  MAX_HEIGHT = 20000;                   // 20k px
    public static final long MAX_PIXELS = 100_000_000L;            // 100 MP (защита от decompression bomb)

    static {
        ImageIO.scanForPlugins();
    }

    public ImageFileChecker() {
        setExtensions(new String[] {"jpeg", "jpg", "png", "tiff", "tif"});
    }

    @Override
    public boolean test(final Path path) {
        return validate(path).ok;
    }

    public Result validate(Path path) {
        if (!checkPath(path)) {
            return Result.builder().build();
        }

        try (ImageInputStream in = ImageIO.createImageInputStream(Files.newInputStream(path))) {
            if (in == null) {
                LOG.error("Unable to open ImageInputStream");
                return Result.builder().build();
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                LOG.error("No ImageIO reader found (unsupported or not an image) {}", path);
                return Result.builder().build();
            }

            ImageReader reader = readers.next();
            String rawFormat = safeLower(reader.getFormatName());
            String normalizedFormat = normalizeFormat(rawFormat);

            if (normalizedFormat == null || ! Arrays.asList(getExtensions()).contains(normalizedFormat)) {
                reader.dispose();
                LOG.error("Unsupported format {}, file-{}", normalizedFormat, path);
                return Result.builder().build();
            }

            reader.setInput(in, true, true);

            final int width = reader.getWidth(0);
            final int height = reader.getHeight(0);

            if (width <= 0 || height <= 0) {
                reader.dispose();
                LOG.error("Invalid image dimensions. width={}, height={}, file-{}", width, height, path);
                return Result.builder().build();
            }

            if (width > MAX_WIDTH || height > MAX_HEIGHT) {
                reader.dispose();
                LOG.error("Image dimensions exceeds limit. width={}, height={}, file-{}", width, height, path);
                return Result.builder().build();
            }

            long pixels = (long) width * (long) height;
            if (pixels > MAX_PIXELS) {
                reader.dispose();
                LOG.error("Too many pixels (possible decompression bomb). pixels={}, file-{}", pixels, path);
                return Result.builder().build();
            }

            BufferedImage img = reader.read(0);
            reader.dispose();

            if (img == null) {
                LOG.error("Decoded image is null. file-{}", path);
                return Result.builder().build();
            }

            return Result.builder().ok(true).format(normalizedFormat).width(img.getWidth()).height(img.getHeight()).build();
        } catch (IOException e) {
            LOG.error("Open image file exception. file: {}, exception: {}", path, e.getMessage());
        }

        return Result.builder().build();
    }

    private static String normalizeFormat(String f) {
        if (f == null) return null;
        return switch (f) {
            case "jpg" -> "jpeg";
            case "tif" -> "tiff";
            default -> f;
        };
    }

    private static String safeLower(String s) {
        return s == null ? null : s.toLowerCase(Locale.ROOT);
    }

    private static long safeSize(Path p) {
        try { return (p != null && Files.exists(p)) ? Files.size(p) : -1; }
        catch (IOException ignored) { return -1; }
    }

    @Builder
    public static class Result {
        @Builder.Default private boolean ok = false;
        @Builder.Default private String format = "";   // jpeg/png/tiff (normalized
        @Builder.Default private int width = 0;
        @Builder.Default private int height = 0;
    }
}
