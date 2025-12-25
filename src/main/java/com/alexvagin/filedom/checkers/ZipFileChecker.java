package com.alexvagin.filedom.checkers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipFileChecker extends FileChecker {
	private static final Logger log = LoggerFactory.getLogger(ZipFileChecker.class);

	@Override
	public boolean test(Path zipFile) {
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(zipFile.toFile()))) {
			return test(inputStream);
		} catch (IOException e) {
			log.error("Unable to open ZIP file", e);
            return false;
        }
    }

	public boolean test(final InputStream inputStream) {
		try (ZipInputStream zis = new ZipInputStream(inputStream)) {
			for (ZipEntry zipEntry = zis.getNextEntry(); zipEntry != null; zipEntry = zis.getNextEntry()) {
				if (zipEntry.isDirectory()) {
					log.warn("zipEntry is directory - {}", zipEntry.getName());
				} else {
					byte[] buffer = new byte[1024 * 64];
					int len;
					CRC32 crc = new CRC32();

					while ((len = zis.read(buffer)) > 0) {
						crc.update(buffer, 0, len);
					}

					if (zipEntry.getCrc() != crc.getValue()) {
						log.error("zipEntry {} - invalid CRC32", zipEntry.getName());
						zis.closeEntry();
						return false;
					}
				}
			}
			zis.closeEntry();
			return true;
		} catch (IOException | IllegalArgumentException e) {
			log.error("Unable to read ZIP file", e);
		}
		return false;
	}
}
