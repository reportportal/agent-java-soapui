package com.epam.reportportal.soapui.util;

import com.epam.reportportal.utils.properties.SystemAttributesExtractor;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class AgentUtil {

	private static final String AGENT_JAR_NAME_PROPERTY = "agent.jar.name";
	private static final String EXTENSION_FOLDER = "." + File.separator + "ext";
	private static final String AGENT_JAR_NAME_PATTERN = "^agent-java-soapui.*\\.jar$";

	private AgentUtil() {
		//static only
	}

	public static Optional<String> resolveJarName(Properties properties) {
		return ofNullable(properties.getProperty(AGENT_JAR_NAME_PROPERTY)).map(name -> ofNullable(Paths.get(EXTENSION_FOLDER, name)
				.getFileName()).map(Path::toString)).orElseGet(AgentUtil::resolveJarDefaultName);
	}

	private static Optional<String> resolveJarDefaultName() {
		try (Stream<Path> pathStream = Files.list(Paths.get(EXTENSION_FOLDER))) {
			return pathStream.filter(path -> ofNullable(path.getFileName()).map(fileName -> Pattern.matches(AGENT_JAR_NAME_PATTERN,
					fileName.toString()
			)).orElse(Boolean.FALSE)).findFirst().map(Path::getFileName).map(Path::toString);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public static Set<ItemAttributesRQ> resolveSystemAttributes(String agentJarName, String agentPropertiesFile) {
		try {
			if (JarUtil.loadFromJar(Paths.get(EXTENSION_FOLDER, agentJarName), Paths.get(EXTENSION_FOLDER), agentPropertiesFile)) {
				return SystemAttributesExtractor.extract(Paths.get(EXTENSION_FOLDER, agentPropertiesFile));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.emptySet();
	}
}
