package com.alexvagin.filedom.converters;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Rar2Zip {
    private static final Logger LOG = LoggerFactory.getLogger(Rar2Zip.class);
    private static Path path = Path.of("Z:\\G\\!\\1\\!rar");

    public static void main(String[] args) throws IOException {
        try (Stream<Path> rarFilesStream = Files.walk(path).filter(Files::isRegularFile).filter(p -> p.getFileName().toString().toLowerCase().endsWith(".rar"))) {
            rarFilesStream.map(Rar2Zip::extractArchive).forEach(System.out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path extractArchive(Path rarPath) {
        boolean toDelete = true;
        Path zipPath = getZipPath(rarPath);

        try (Archive arch = new Archive(rarPath.toFile());
             ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipPath.toFile())))) {
            if (arch.isEncrypted()) {
                LOG.warn("rarPath is encrypted cannot extreact");
                toDelete = false;
                return null;
            }

            FileHeader fh = arch.nextFileHeader();
            while (fh != null) {
                if (fh.isEncrypted()) {
                    LOG.warn("file is encrypted cannot extract: {}", fh.getFileNameString());
                    toDelete = false;
                    continue;
                }
                //LOG.info("extracting: {}", fh.getFileNameString());

                try {
                    if (fh.isDirectory()) {
                        LOG.warn("archive {} contains directory: {}", rarPath, fh.getFileNameString());
                        toDelete = false;
                        //DO nothing
                        //createDirectory(fh, destination);
                    } else {
                        ZipEntry zipEntry = new ZipEntry(fh.getFileName());
                        zipOutputStream.putNextEntry(zipEntry);
                        arch.extractFile(fh, zipOutputStream);
                        zipOutputStream.closeEntry();
                    }
                } catch (RarException e) {
                    LOG.error("error extracting the file", e);
                    toDelete = false;
                }

                fh = arch.nextFileHeader();
            }
        } catch (RarException | IOException e) {
            LOG.error("RAR", e);
            toDelete = false;
        } finally {
            if (toDelete) {
                deleteFile(rarPath);
            }
        }

        return zipPath;
    }

    public static void deleteFile(final Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            LOG.error("delete failed", e);
        }
    }

    private static Path getZipPath(final Path rarPath) {
        String fullFileName = rarPath.toAbsolutePath().normalize().toString();
        return Path.of(fullFileName.substring(0, fullFileName.lastIndexOf(".")).concat(".zip"));
    }
}
