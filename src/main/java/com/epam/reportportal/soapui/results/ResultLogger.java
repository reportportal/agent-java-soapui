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
