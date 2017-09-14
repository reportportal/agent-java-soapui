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

import com.epam.reportportal.service.LoggingContext;
import com.epam.reportportal.soapui.parameters.TestItemType;
import com.epam.reportportal.soapui.parameters.TestStatus;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.*;
import io.reactivex.Maybe;
import rp.com.google.common.base.Function;
import rp.com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.epam.reportportal.utils.markdown.MarkdownUtils.asMarkdown;

/**
 * Default implementation of {@link TestBasedSoapUIServiceImpl}
 *
 * @author Raman_Usik
 */
public class TestBasedSoapUIServiceImpl extends StepBasedSoapUIServiceImpl implements SoapUIService {

	private static final Map<String, LoggingContext> CONTEXT_MAP = new ConcurrentHashMap<String, LoggingContext>();
	private static final String LEVEL_INFO = "INFO";

	@Override
	public void startTestCase(TestCase testCase, PropertyExpansionContext propertyContext) {
		Maybe<String> id = startItem(testCase.getName(), TestItemType.TEST_STEP,
				fromStringId(testCase.getTestSuite().getPropertyValue(ID)));
		testCase.setPropertyValue(ID, toStringId(id));

		LoggingContext loggingContext = LoggingContext.init(id, client);
		CONTEXT_MAP.put(testCase.getId(), loggingContext);
	}

	public void startTestStep(TestStep testStep, TestCaseRunContext context) {
		String log = asMarkdown(String.format("# ===========STEP '%s' STARTED===========", testStep.getName()));

		LoggingContext loggingContext = CONTEXT_MAP.get(testStep.getTestCase().getId());
		loggingContext.emit(asFunction(log, LEVEL_INFO, Calendar.getInstance().getTime()));
	}

	public void finishTestStep(TestStepResult testStepContext, TestCaseRunContext paramTestCaseRunContext) {

		LoggingContext loggingContext = CONTEXT_MAP.get(testStepContext.getTestStep().getTestCase().getId());
		String logStepData = getLogStepData(testStepContext);
		if (!Strings.isNullOrEmpty(logStepData)) {
			loggingContext.emit(asFunction(logStepData, LEVEL_INFO, Calendar.getInstance().getTime()));

		}

		for (final SaveLogRQ rq : getStepLogReport(testStepContext)) {
			loggingContext.emit(new Function<String, SaveLogRQ>() {
				@Override
				public SaveLogRQ apply(@Nullable String id) {
					rq.setTestItemId(id);
					return rq;
				}
			});
		}

		if (TestStepResult.TestStepStatus.FAILED.equals(testStepContext.getStatus())) {
			loggingContext.emit(asFunction(getStepError(testStepContext), "ERROR", Calendar.getInstance().getTime()));
		}

		if (TestStepResult.TestStepStatus.CANCELED.equals(testStepContext.getStatus())) {
			context.setTestCanceled(true);
		}

		String log = asMarkdown(String.format("# ===========STEP '%s' %s===========", testStepContext.getTestStep().getName(),
				TestStatus.fromSoapUIStep(testStepContext.getStatus())));
		loggingContext.emit(asFunction(log, LEVEL_INFO, Calendar.getInstance().getTime()));

	}

	@Override
	public void finishTestCase(TestCaseRunner testCaseContext, PropertyExpansionContext propertyContext) {
		CONTEXT_MAP.get(testCaseContext.getTestCase().getId()).completed().blockingAwait();

		super.finishTestCase(testCaseContext, propertyContext);

	}

	private Function<String, SaveLogRQ> asFunction(final String message, final String level, final Date time) {
		return new Function<String, SaveLogRQ>() {
			@Override
			public SaveLogRQ apply(@Nullable String id) {
				SaveLogRQ rq = new SaveLogRQ();
				rq.setLevel(level);
				rq.setLogTime(time);
				rq.setTestItemId(id);
				rq.setMessage(message);

				return rq;
			}
		};

	}
}
