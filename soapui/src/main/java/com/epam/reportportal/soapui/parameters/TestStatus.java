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
package com.epam.reportportal.soapui.parameters;

import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;

public enum TestStatus {
	FINISHED("PASSED"),
	FAILED("FAILED"),
	PASS("PASSED"),
	FAIL("FAILED"),
	CANCELED("SKIPPED"),
	OK("PASSED"),
	WARNING("PASSED"),
	UNKNOWN("PASSED");
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