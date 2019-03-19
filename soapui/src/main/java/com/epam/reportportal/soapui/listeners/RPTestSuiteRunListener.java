/*
 * Copyright (C) 2019 EPAM Systems
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
package com.epam.reportportal.soapui.listeners;

import com.epam.reportportal.soapui.service.SoapUIService;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.testsuite.*;

import static com.epam.reportportal.soapui.listeners.RPProjectRunListener.RP_SERVICE;

/**
 * Report portal related implementation of {@link TestSuiteRunListener}. This
 * listener should be used only with {@link RPTestRunListener} and
 * {@link RPProjectRunListener}.
 *
 * @author Raman_Usik
 */
public class RPTestSuiteRunListener implements TestSuiteRunListener {

	private static final String SUITE_ONLY = "suite_only";
	private SoapUIService service;

	@Override
	public void afterRun(TestSuiteRunner runner, TestSuiteRunContext context) {
		final Object suiteOnly = context.getProperty(SUITE_ONLY);
		if (null != suiteOnly && ((Boolean) suiteOnly)) {
			service.finishTestSuite(runner);
			service.finishLaunch();
		}
	}

	@Override
	public void beforeRun(TestSuiteRunner runner, TestSuiteRunContext context) {
		service = (SoapUIService) context.getProperty(RP_SERVICE);
		if (null == service) {
			try {
				service = RpServiceBuilder.build(context.getTestSuite().getProject());
				service.startLaunch();
				service.startTestSuite(context.getTestSuite());
			} catch (Throwable t) {
				SoapUI.log("ReportPortal plugin cannot be initialized. " + t.getMessage());
				service = SoapUIService.NOP_SERVICE;
			}
			context.setProperty(SUITE_ONLY, true);
			context.setProperty(RP_SERVICE, service);
		}
	}

	@Override
	public void beforeTestCase(TestSuiteRunner paramTestSuiteRunner, TestSuiteRunContext paramTestSuiteRunContext, TestCase paramTestCase) {
		service.startTestCase(paramTestCase, paramTestSuiteRunContext);
	}

	@Override
	public void afterTestCase(TestSuiteRunner paramTestSuiteRunner, TestSuiteRunContext paramTestSuiteRunContext,
			TestCaseRunner paramTestCaseRunner) {
		service.finishTestCase(paramTestCaseRunner, paramTestSuiteRunContext);
	}
}