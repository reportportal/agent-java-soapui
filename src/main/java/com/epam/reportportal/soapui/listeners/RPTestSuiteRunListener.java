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
import com.epam.reportportal.soapui.service.ISoapUIService;
import com.eviware.soapui.model.testsuite.*;

/**
 * Report portal related implementation of {@link TestSuiteRunListener}. This
 * listener should be used only with {@link RPTestRunListener} and
 * {@link RPProjectRunListener}.
 * 
 * @author Raman_Usik
 *
 */
public class RPTestSuiteRunListener implements TestSuiteRunListener {

	private ISoapUIService service = SoapUIInjectorProvider.getInstance().getBean(ISoapUIService.class);

	@Override
	public void afterRun(TestSuiteRunner arg0, TestSuiteRunContext arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeRun(TestSuiteRunner arg0, TestSuiteRunContext arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTestCase(TestSuiteRunner paramTestSuiteRunner, TestSuiteRunContext paramTestSuiteRunContext, TestCase paramTestCase) {
		service.startTestCase(paramTestCase);
	}

	@Override
	public void afterTestCase(TestSuiteRunner paramTestSuiteRunner, TestSuiteRunContext paramTestSuiteRunContext,
			TestCaseRunner paramTestCaseRunner) {
		service.finishTestCase(paramTestCaseRunner);
	}

}
