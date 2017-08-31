package com.epam.reportportal.soapui.service;

import com.eviware.soapui.model.testsuite.*;

/**
 * Wrapper around ReportPortal Client
 *
 * @author Andrei Varabyeu
 */
public interface SoapUIService {

	void startLaunch();

	void finishLaunch();

	void startTestSuite(TestSuite testSuite);

	void finishTestSuite(TestSuiteRunner testSuiteContext);

	void startTestCase(TestCase testCase);

	void finishTestCase(TestCaseRunner testCaseContext);

	void startTestStep(TestStep testStep, TestCaseRunContext context);

	void finishTestStep(TestStepResult testStepContext, TestCaseRunContext paramTestCaseRunContext);

	/**
	 * NOP implementation for the cases when ReportPortal client cannot be initialized
	 */
	SoapUIService NOP_SERVICE = new SoapUIService() {
		@Override
		public void startLaunch() {

		}

		@Override
		public void finishLaunch() {

		}

		@Override
		public void startTestSuite(TestSuite testSuite) {

		}

		@Override
		public void finishTestSuite(TestSuiteRunner testSuiteContext) {

		}

		@Override
		public void startTestCase(TestCase testCase) {

		}

		@Override
		public void finishTestCase(TestCaseRunner testCaseContext) {

		}

		@Override
		public void startTestStep(TestStep testStep, TestCaseRunContext context) {

		}

		@Override
		public void finishTestStep(TestStepResult testStepContext, TestCaseRunContext paramTestCaseRunContext) {

		}

	};
}
