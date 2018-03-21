/*
 * Copyright (C) 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.reportportal.soapui.service;

import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
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

	void startTestCase(TestCase testCase, PropertyExpansionContext propertyContext);

	void finishTestCase(TestCaseRunner testCaseContext, PropertyExpansionContext propertyContext);

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
		public void startTestCase(TestCase testCase, PropertyExpansionContext propertyContext) {

		}

		@Override
		public void finishTestCase(TestCaseRunner testCaseContext, PropertyExpansionContext propertyContext) {

		}

		@Override
		public void startTestStep(TestStep testStep, TestCaseRunContext context) {

		}

		@Override
		public void finishTestStep(TestStepResult testStepContext, TestCaseRunContext paramTestCaseRunContext) {

		}

	};
}
