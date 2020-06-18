package com.epam.reportportal.soapui.service;

import com.epam.reportportal.soapui.listeners.RpServiceBuilder;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.eviware.soapui.model.TestPropertyHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class SoapUiServiceTest {

	private static final String AGENT_JAR_NAME_PROPERTY = "agent.jar.name";
	private static final String JAR_NAME = "agent-java-soapui-testing.jar";
	private static final String AGENT_PROPERTIES_FILE = "agent.properties";

	private static final Map<String, Pattern> RESOLVED_PROPERTIES = new HashMap<>();

	@Mock
	private TestPropertyHolder testPropertyHolder;

	@BeforeAll
	public static void initKeys() {
		RESOLVED_PROPERTIES.put("os", Pattern.compile("^.+\\|.+\\|.+$"));
		RESOLVED_PROPERTIES.put("jvm", Pattern.compile("^.+\\|.+\\|.+$"));
		RESOLVED_PROPERTIES.put("agent", Pattern.compile("^test-agent\\|test-1\\.0$"));
	}

	@BeforeEach
	public void init() {
		when(testPropertyHolder.getPropertyNames()).thenReturn(new String[] { AGENT_JAR_NAME_PROPERTY });
		when(testPropertyHolder.getPropertyValue(AGENT_JAR_NAME_PROPERTY)).thenReturn(JAR_NAME);

	}

	@AfterEach
	public void cleanUp() {
		File file = new File("." + File.separator + "/ext/" + AGENT_PROPERTIES_FILE);
		if (file.exists()) {
			file.delete();
		}
	}

	@Test
	public void test() throws IOException {
		StepBasedSoapUIServiceImpl soapUIService = (StepBasedSoapUIServiceImpl) RpServiceBuilder.build(testPropertyHolder);
		Set<ItemAttributesRQ> attributes = soapUIService.parameters.getAttributes();
		assertThat(attributes, notNullValue());
		assertThat(attributes, hasSize(3));
		attributes.forEach(attribute -> {
			assertThat(attribute.isSystem(), equalTo(Boolean.TRUE));

			Pattern pattern = getPattern(attribute);
			assertThat(pattern, notNullValue());
			assertThat(attribute.getValue(), matchesPattern(pattern));
		});

	}

	private Pattern getPattern(ItemAttributesRQ attribute) {
		return ofNullable(RESOLVED_PROPERTIES.get(attribute.getKey())).orElse(null);
	}

}
