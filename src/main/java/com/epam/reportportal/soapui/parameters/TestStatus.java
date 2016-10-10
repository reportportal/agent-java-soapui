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
package com.epam.reportportal.soapui.parameters;

import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;

public enum TestStatus {
	FINISHED("PASSED"), FAILED("FAILED"), CANCELED("SKIPPED"), OK("PASSED"), UNKNOWN("PASSED");

	private String value;

	TestStatus(String result) {
		this.value = result;
	}

	public String getResult() {
		return value;
	}

	public static String fromSoapUI(Status status) {
		return valueOf(status.toString()).getResult();
	}

	public static String fromSoapUIStep(TestStepStatus status) {
		return valueOf(status.toString()).getResult();
	}
}
