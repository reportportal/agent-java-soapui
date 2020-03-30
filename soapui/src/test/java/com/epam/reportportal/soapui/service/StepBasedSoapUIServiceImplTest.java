package com.epam.reportportal.soapui.service;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.service.Launch;
import com.epam.reportportal.service.ReportPortal;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.*;
import io.reactivex.Maybe;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.epam.reportportal.soapui.service.StepBasedSoapUIServiceImpl.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class StepBasedSoapUIServiceImplTest {

	private final String PROJECT_NAME = "Project Name";

	private final String SUITE_NAME = "Suite Name";
	private final String SUITE_CODE_REF = PROJECT_NAME + "." + SUITE_NAME;

	private final String TEST_CASE_NAME = "Test Case Name";
	private final String TEST_CASE_CODE_REF = SUITE_CODE_REF + "." + TEST_CASE_NAME;

	private final String TEST_STEP_NAME = "Test Step Name";
	private final String TEST_STEP_CODE_REF = TEST_CASE_CODE_REF + "." + TEST_STEP_NAME;

	@Mock
	private Project project;

	@Mock
	private TestSuite testSuite;

	@Mock
	private TestCase testCase;

	@Mock
	private TestStep testStep;

	@Mock
	private TestStep rpPropertiesStep;

	@Mock
	private PropertyExpansionContext propertyExpansionContext;

	@Mock
	private TestCaseRunContext testCaseRunContext;

	@Mock
	private Launch launch;

	@Mock
	private ReportPortal reportPortalMock;

	@Mock
	private ListenerParameters listenerParameters;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		when(listenerParameters.getBaseUrl()).thenReturn("http://example.com");
		when(listenerParameters.getApiKey()).thenReturn("key");
		when(listenerParameters.getProjectName()).thenReturn("project");
		when(listenerParameters.getLaunchName()).thenReturn("launch");
		when(listenerParameters.getIoPoolSize()).thenReturn(5);

		when(reportPortalMock.newLaunch(any())).thenReturn(launch);
		when(launch.start()).thenReturn(createIdMaybe("launch Id"));

		when(project.getName()).thenReturn(PROJECT_NAME);

		when(testSuite.getName()).thenReturn(SUITE_NAME);
		when(testSuite.getProject()).thenReturn(project);

		when(testCase.getName()).thenReturn(TEST_CASE_NAME);
		when(testCase.getTestSuite()).thenReturn(testSuite);

		when(testStep.getName()).thenReturn(TEST_STEP_NAME);
		when(testStep.getTestCase()).thenReturn(testCase);
	}

	@Test
	public void suiteCodeRefAndTestCaseIdWithoutPropertiesTest() {
		StepBasedSoapUIServiceImpl service = new StepBasedSoapUIServiceImpl(listenerParameters, new ArrayList<>()) {
			@Override
			protected ReportPortal buildReportPortal(ListenerParameters parameters) {
				return reportPortalMock;
			}
		};

		ArgumentCaptor<StartTestItemRQ> suiteRqCaptor = ArgumentCaptor.forClass(StartTestItemRQ.class);

		when(launch.startTestItem(any())).thenReturn(createIdMaybe("Suite id"));

		service.startLaunch();
		service.startTestSuite(testSuite);

		verify(launch, times(1)).startTestItem(suiteRqCaptor.capture());

		StartTestItemRQ request = suiteRqCaptor.getValue();

		Assert.assertEquals(SUITE_NAME, request.getName());
		Assert.assertEquals(SUITE_CODE_REF, request.getCodeRef());
		Assert.assertEquals(SUITE_CODE_REF, request.getTestCaseId());
	}

	@Test
	public void suiteCodeRefAndTestCaseIdWithPropertiesTest() {
		StepBasedSoapUIServiceImpl service = new StepBasedSoapUIServiceImpl(listenerParameters, new ArrayList<>()) {
			@Override
			protected ReportPortal buildReportPortal(ListenerParameters parameters) {
				return reportPortalMock;
			}
		};

		ArgumentCaptor<StartTestItemRQ> suiteRqCaptor = ArgumentCaptor.forClass(StartTestItemRQ.class);

		final String customTestCaseId = "Custom suite TestCaseId";
		TestProperty testProperty = mock(TestProperty.class);
		when(testProperty.getValue()).thenReturn(customTestCaseId);

		Map<String, TestProperty> suiteProperties = new HashMap<>();
		suiteProperties.put(TEST_CASE_ID_PROPERTY, testProperty);

		when(testSuite.getProperties()).thenReturn(suiteProperties);
		when(launch.startTestItem(any())).thenReturn(createIdMaybe("Suite id"));

		service.startLaunch();
		service.startTestSuite(testSuite);

		verify(launch, times(1)).startTestItem(suiteRqCaptor.capture());

		StartTestItemRQ request = suiteRqCaptor.getValue();

		Assert.assertEquals(SUITE_NAME, request.getName());
		Assert.assertEquals(SUITE_CODE_REF, request.getCodeRef());
		Assert.assertEquals(customTestCaseId, request.getTestCaseId());

	}

	@Test
	public void testCaseCodeRefAndTestCaseIdWithoutPropertiesTest() {
		StepBasedSoapUIServiceImpl service = new StepBasedSoapUIServiceImpl(listenerParameters, new ArrayList<>()) {
			@Override
			protected ReportPortal buildReportPortal(ListenerParameters parameters) {
				return reportPortalMock;
			}
		};

		ArgumentCaptor<StartTestItemRQ> testCaseRqCaptor = ArgumentCaptor.forClass(StartTestItemRQ.class);

		when(launch.startTestItem(any())).thenReturn(createIdMaybe("Suite id"));
		when(launch.startTestItem(any(), any())).thenReturn(createIdMaybe("Test case id"));
		when(testSuite.getPropertyValue(ID)).thenReturn("Suite id");

		service.startLaunch();
		service.startTestSuite(testSuite);
		service.startTestCase(testCase, propertyExpansionContext);

		verify(launch, times(1)).startTestItem(any(), testCaseRqCaptor.capture());

		StartTestItemRQ request = testCaseRqCaptor.getValue();

		Assert.assertEquals(TEST_CASE_NAME, request.getName());
		Assert.assertEquals(TEST_CASE_CODE_REF, request.getCodeRef());
		Assert.assertEquals(TEST_CASE_CODE_REF, request.getTestCaseId());
	}

	@Test
	public void testCaseCodeRefAndTestCaseIdWithPropertiesTest() {
		StepBasedSoapUIServiceImpl service = new StepBasedSoapUIServiceImpl(listenerParameters, new ArrayList<>()) {
			@Override
			protected ReportPortal buildReportPortal(ListenerParameters parameters) {
				return reportPortalMock;
			}
		};

		ArgumentCaptor<StartTestItemRQ> testCaseRqCaptor = ArgumentCaptor.forClass(StartTestItemRQ.class);

		final String customTestCaseId = "Custom test case TestCaseId";
		TestProperty testProperty = mock(TestProperty.class);
		when(testProperty.getValue()).thenReturn(customTestCaseId);

		Map<String, TestProperty> testCaseProperties = new HashMap<>();
		testCaseProperties.put(TEST_CASE_ID_PROPERTY, testProperty);

		when(testCase.getProperties()).thenReturn(testCaseProperties);
		when(launch.startTestItem(any())).thenReturn(createIdMaybe("Suite id"));
		when(launch.startTestItem(any(), any())).thenReturn(createIdMaybe("Test case id"));
		when(testSuite.getPropertyValue(ID)).thenReturn("Suite id");

		service.startLaunch();
		service.startTestSuite(testSuite);
		service.startTestCase(testCase, propertyExpansionContext);

		verify(launch, times(1)).startTestItem(any(), testCaseRqCaptor.capture());

		StartTestItemRQ request = testCaseRqCaptor.getValue();

		Assert.assertEquals(TEST_CASE_NAME, request.getName());
		Assert.assertEquals(TEST_CASE_CODE_REF, request.getCodeRef());
		Assert.assertEquals(customTestCaseId, request.getTestCaseId());
	}

	@Test
	public void testStepCodeRefAndTestCaseIdWithoutPropertiesTest() {
		StepBasedSoapUIServiceImpl service = new StepBasedSoapUIServiceImpl(listenerParameters, new ArrayList<>()) {
			@Override
			protected ReportPortal buildReportPortal(ListenerParameters parameters) {
				return reportPortalMock;
			}
		};

		ArgumentCaptor<StartTestItemRQ> testStepRqCaptor = ArgumentCaptor.forClass(StartTestItemRQ.class);

		Maybe<String> suiteId = createIdMaybe("Suite id");
		when(launch.startTestItem(any())).thenReturn(suiteId);
		Maybe<String> testCaseId = createIdMaybe("Test case id");
		when(launch.startTestItem(eq(suiteId), any())).thenReturn(testCaseId);
		when(launch.startTestItem(eq(testCaseId), any())).thenReturn(createIdMaybe("Test step id"));

		ArgumentCaptor<String> idsCaptor = ArgumentCaptor.forClass(String.class);
		doNothing().when(testSuite).setPropertyValue(any(), idsCaptor.capture());
		doNothing().when(testCase).setPropertyValue(any(), idsCaptor.capture());

		service.startLaunch();
		service.startTestSuite(testSuite);

		when(testSuite.getPropertyValue(ID)).thenReturn(idsCaptor.getAllValues().get(0));

		service.startTestCase(testCase, propertyExpansionContext);
		when(testCase.getPropertyValue(ID)).thenReturn(idsCaptor.getAllValues().get(1));

		service.startTestStep(testStep, testCaseRunContext);

		verify(launch, times(1)).startTestItem(eq(testCaseId), testStepRqCaptor.capture());

		StartTestItemRQ request = testStepRqCaptor.getValue();

		Assert.assertEquals(TEST_STEP_NAME, request.getName());
		Assert.assertEquals(TEST_STEP_CODE_REF, request.getCodeRef());
		Assert.assertEquals(TEST_STEP_CODE_REF, request.getTestCaseId());
	}

	@Test
	public void testStepCodeRefAndTestCaseIdWithPropertiesTest() {
		StepBasedSoapUIServiceImpl service = new StepBasedSoapUIServiceImpl(listenerParameters, new ArrayList<>()) {
			@Override
			protected ReportPortal buildReportPortal(ListenerParameters parameters) {
				return reportPortalMock;
			}
		};

		ArgumentCaptor<StartTestItemRQ> testStepRqCaptor = ArgumentCaptor.forClass(StartTestItemRQ.class);

		Maybe<String> suiteId = createIdMaybe("Suite id");
		when(launch.startTestItem(any())).thenReturn(suiteId);
		Maybe<String> testCaseId = createIdMaybe("Test case id");
		when(launch.startTestItem(eq(suiteId), any())).thenReturn(testCaseId);
		when(launch.startTestItem(eq(testCaseId), any())).thenReturn(createIdMaybe("Test step id"));

		ArgumentCaptor<String> idsCaptor = ArgumentCaptor.forClass(String.class);
		doNothing().when(testSuite).setPropertyValue(any(), idsCaptor.capture());
		doNothing().when(testCase).setPropertyValue(any(), idsCaptor.capture());

		service.startLaunch();
		service.startTestSuite(testSuite);

		when(testSuite.getPropertyValue(ID)).thenReturn(idsCaptor.getAllValues().get(0));

		final String customTestCaseId = "Custom test step TestCaseId";
		TestProperty testProperty = mock(TestProperty.class);
		when(testProperty.getValue()).thenReturn(customTestCaseId);

		Map<String, TestProperty> stepProperties = new HashMap<>();
		stepProperties.put(TEST_CASE_ID_PROPERTY + "." + testStep.getName(), testProperty);
		when(rpPropertiesStep.getProperties()).thenReturn(stepProperties);

		when(testCase.getTestStepByName(RP_ITEM_PROPERTIES)).thenReturn(rpPropertiesStep);
		service.startTestCase(testCase, propertyExpansionContext);
		when(testCase.getPropertyValue(ID)).thenReturn(idsCaptor.getAllValues().get(1));

		service.startTestStep(testStep, testCaseRunContext);

		verify(launch, times(1)).startTestItem(eq(testCaseId), testStepRqCaptor.capture());

		StartTestItemRQ request = testStepRqCaptor.getValue();

		Assert.assertEquals(TEST_STEP_NAME, request.getName());
		Assert.assertEquals(TEST_STEP_CODE_REF, request.getCodeRef());
		Assert.assertEquals(customTestCaseId, request.getTestCaseId());

	}

	private Maybe<String> createIdMaybe(String id) {
		return Maybe.create(emitter -> {
			emitter.onSuccess(id);
			emitter.onComplete();
		});
	}

}