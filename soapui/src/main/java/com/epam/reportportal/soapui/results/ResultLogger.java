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
package com.epam.reportportal.soapui.results;

import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.eviware.soapui.model.testsuite.TestStepResult;

import java.util.Calendar;
import java.util.List;

/**
 * Converter of {@link TestStepResult} to list of loggable entities
 *
 * @author Andrei Varabyeu
 */
public abstract class ResultLogger<T> {

	private Class<T> resultsType;

	public ResultLogger(Class<T> resultsType) {
		this.resultsType = resultsType;
	}

	abstract protected List<SaveLogRQ> prepareLogs(T result);

	public final List<SaveLogRQ> buildLogs(TestStepResult result) {
		//noinspection unchecked
		return prepareLogs((T) result);
	}

	public boolean supports(TestStepResult result) {
		return resultsType.isAssignableFrom(result.getClass());
	}

	protected final SaveLogRQ prepareEntity(String level, String message) {
		SaveLogRQ logRQ = new SaveLogRQ();
		logRQ.setLevel(level);
		logRQ.setLogTime(Calendar.getInstance().getTime());
		logRQ.setMessage(message);
		return logRQ;
	}
}