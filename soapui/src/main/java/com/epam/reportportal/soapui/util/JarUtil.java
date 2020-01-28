package com.epam.reportportal.soapui.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class JarUtil {

	private JarUtil() {
		//static only
	}

	public static boolean loadFromJar(Path jarPath, Path targetPath, String resource) throws IOException {
		JarFile jar = new JarFile(jarPath.toFile());
		if (!Files.isDirectory(targetPath)) {
			Files.createDirectories(targetPath);
		}
		return copyResource(jar, targetPath.toFile(), resource);
	}

	private static boolean copyResource(JarFile jarFile, File destination, String resource) {
		return jarFile.stream().filter(jarEntry -> resource.equals(jarEntry.getName())).findFirst().map(entry -> {
			try {
				copyEntry(entry, jarFile, destination, resource);
			} catch (IOException e) {
				return false;
			}
			return true;
		}).orElse(Boolean.FALSE);
	}

	private static void copyEntry(JarEntry entry, JarFile jarFile, File destination, String resource) throws IOException {
		if (!entry.isDirectory()) {
			try (InputStream entryInputStream = jarFile.getInputStream(entry)) {
				FileUtils.copyInputStreamToFile(entryInputStream, new File(destination, resource));
			}
		} else {
			Files.createDirectories(Paths.get(destination.getAbsolutePath(), resource));
		}
	}
}
