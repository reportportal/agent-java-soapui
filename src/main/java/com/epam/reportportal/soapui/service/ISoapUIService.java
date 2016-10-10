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
package com.epam.reportportal.soapui.service;

import com.epam.reportportal.service.IReportPortalService;
import com.eviware.soapui.model.testsuite.*;

/**
 * Describes all operations for soapui RP listener service
 * 
 * @author Raman_Usik
 */

public interface ISoapUIService {

	/**
	 * Start current launch
	 */
	void startLaunch();

	/**
	 * Finish current launch
	 */
	void finishLaunch();

	/**
	 * Start test suite event handler
	 * 
	 * @param testSuite
	 */
	void startTestSuite(TestSuite testSuite);

	/**
	 * Finish test suite event handler
	 * 
	 * @param testSuiteContext
	 */
	void finishTestSuite(TestSuiteRunner testSuiteContext);

	/**
	 * Start test case event handler (Start 'testItem' with type 'test')
	 * 
	 * @param testCase
	 */
	void startTestCase(TestCase testCase);

	/**
	 * Finish test case event handler (Finish 'testItem' with type 'test')
	 * 
	 * @param testCaseContext
	 */
	void finishTestCase(TestCaseRunner testCaseContext);

	/**
	 * Start test item event handler
	 * 
	 * @param testStep
	 */
	void startTestStep(TestStep testStep);

	/**
	 * Finish test suite event handler
	 * 
	 * @param testStepContext
	 */
	void finishTestStep(TestStepResult testStepContext);

	/**
	 * Report log message with reason of test fail.
	 * 
	 * @param testStepContext
	 */
	void errorDuringExecution(TestStepResult testStepContext);

	/**
	 * Reinitialize service with new properties. Properties can reloaded in
	 * soapui properties.
	 */
	void reinitService();

	/**
	 * Provide low level client service. This service can be used for appender
	 * initialization.
	 * 
	 * @return IJunitStyleService
	 */
	IReportPortalService getServiceClient();

}
