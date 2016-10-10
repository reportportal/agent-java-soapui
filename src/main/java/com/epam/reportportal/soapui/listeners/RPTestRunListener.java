/*
 * Copyright 2016 EPAM Systems
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
package com.epam.reportportal.soapui.listeners;

import com.epam.reportportal.soapui.injection.SoapUIInjectorProvider;
import com.epam.reportportal.soapui.parameters.TestStatus;
import com.epam.reportportal.soapui.service.ISoapUIService;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;

/**
 * Report portal related implementation of {@link TestRunListener}.
 * This listener should be used only with {@link RPTestSuiteRunListener} and   {@link RPProjectRunListener}. 
 * 
 * @author Raman_Usik
 *
 */
public class RPTestRunListener implements TestRunListener {
	
	private ISoapUIService service = SoapUIInjectorProvider.getInstance().getBean(ISoapUIService.class);

	public void beforeRun(TestCaseRunner paramTestCaseRunner,
			TestCaseRunContext paramTestCaseRunContext) {
	}

	@Override
	public void afterRun(TestCaseRunner paramTestCaseRunner,
			TestCaseRunContext paramTestCaseRunContext) {
	}

	@Override
	public void beforeStep(TestCaseRunner paramTestCaseRunner,
			TestCaseRunContext paramTestCaseRunContext, TestStep paramTestStep) {
		service.startTestStep(paramTestStep);
	}

	@Override
	public void afterStep(TestCaseRunner paramTestCaseRunner,
			TestCaseRunContext paramTestCaseRunContext,
			TestStepResult paramTestStepResult) {
		if (TestStatus.FAILED.getResult().equals(
				paramTestStepResult.getStatus().toString())) {
			service.errorDuringExecution(paramTestStepResult);
		}
		service.finishTestStep(paramTestStepResult);
		if (TestStatus.CANCELED.getResult().equals(
				paramTestStepResult.getStatus().toString())) {
			//stopTesting();
		}
	}

	@Override
	@Deprecated
	public void beforeStep(TestCaseRunner paramTestCaseRunner,
			TestCaseRunContext paramTestCaseRunContext) {

	}

}
