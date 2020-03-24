package com.epam.reportportal.soapui.service;

import com.epam.reportportal.soapui.listeners.RpServiceBuilder;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.eviware.soapui.model.TestPropertyHolder;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;
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

	@BeforeClass
	public static void initKeys() {
		RESOLVED_PROPERTIES.put("os", Pattern.compile("^.+\\|.+\\|.+$"));
		RESOLVED_PROPERTIES.put("jvm", Pattern.compile("^.+\\|.+\\|.+$"));
		RESOLVED_PROPERTIES.put("agent", Pattern.compile("^test-agent\\|test-1\\.0$"));
	}

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(testPropertyHolder.getPropertyNames()).thenReturn(new String[] { JAR_NAME });
		when(testPropertyHolder.getPropertyValue(AGENT_JAR_NAME_PROPERTY)).thenReturn(JAR_NAME);

	}

	@After
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
		Assert.assertNotNull(attributes);
		Assert.assertEquals(3, attributes.size());
		attributes.forEach(attribute -> {
			Assert.assertTrue(attribute.isSystem());

			Pattern pattern = getPattern(attribute);
			Assert.assertNotNull(pattern);
			Assert.assertTrue(pattern.matcher(attribute.getValue()).matches());
		});

	}

	private Pattern getPattern(ItemAttributesRQ attribute) {
		return ofNullable(RESOLVED_PROPERTIES.get(attribute.getKey())).orElse(null);
	}

}
