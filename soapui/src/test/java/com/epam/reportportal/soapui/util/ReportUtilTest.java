package com.epam.reportportal.soapui.util;

import com.epam.reportportal.soapui.service.SoapUIService;
import com.eviware.soapui.model.testsuite.*;
import com.eviware.soapui.support.types.StringToObjectMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static com.epam.reportportal.soapui.listeners.RPProjectRunListener.RP_SERVICE;
import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ReportUtilTest {

	@Mock
	private TestRunContext testRunContext;

	@Mock
	private TestCase testCase;

	@Mock
	private TestSuite testSuite;

	@Mock
	private SoapUIService rpService;

	@Mock
	private TestCaseRunner testCaseRunner;

	@Mock
	private TestSuiteRunner testSuiteRunner;

	@BeforeEach
	public void setup() {
		when(testRunContext.getProperty(same(RP_SERVICE))).thenReturn(rpService);
	}

	@Test
	public void test_correct_testCase_reporting() {
		when(testCase.run(any(), eq(false))).thenReturn(testCaseRunner);

		ReportUtil.runTestCase(testRunContext, testCase);

		verify(rpService, times(1)).startTestCase(same(testCase), same(testRunContext));
		verify(testCase, times(1)).run(eq(new StringToObjectMap(singletonMap(RP_SERVICE, rpService))), eq(false));
		verify(rpService, times(1)).finishTestCase(same(testCaseRunner), same(testRunContext));
		verifyNoMoreInteractions(testCase, rpService);
	}

	@Test
	public void test_correct_testSuite_reporting() {
		when(testSuite.run(any(), eq(false))).thenReturn(testSuiteRunner);

		ReportUtil.runTestSuite(testRunContext, testSuite);

		verify(rpService, times(1)).startTestSuite(same(testSuite));
		verify(testSuite, times(1)).run(eq(new StringToObjectMap(singletonMap(RP_SERVICE, rpService))), eq(false));
		verify(rpService, times(1)).finishTestSuite(same(testSuiteRunner));
		verifyNoMoreInteractions(testSuite, rpService);
	}
}
