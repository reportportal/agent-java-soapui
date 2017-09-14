/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/agent-java-soapui
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
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
