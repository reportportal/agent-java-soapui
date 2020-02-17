package com.epam.reportportal.soapui.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class JarUtilTest {

	private static final String EXTENSION_FOLDER = "." + File.separator + "ext";
	private static final String AGENT_JAR_NAME = "agent-java-soapui.jar";
	private static final String AGENT_PROPERTIES_FILE = "agent.properties";

	@Test
	public void loadFromJar() throws IOException {
		boolean loaded = JarUtil.loadFromJar(Paths.get(EXTENSION_FOLDER, AGENT_JAR_NAME),
				Paths.get(EXTENSION_FOLDER),
				AGENT_PROPERTIES_FILE
		);
		Assert.assertTrue(loaded);
		File file = new File(EXTENSION_FOLDER, AGENT_PROPERTIES_FILE);
		Assert.assertTrue(file.exists());
		file.delete();
	}
}