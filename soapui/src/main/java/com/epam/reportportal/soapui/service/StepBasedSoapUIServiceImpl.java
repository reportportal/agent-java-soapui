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
package com.epam.reportportal.soapui.service;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.service.Launch;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.soapui.parameters.TestItemType;
import com.epam.reportportal.soapui.parameters.TestStatus;
import com.epam.reportportal.soapui.parameters.TestStepType;
import com.epam.reportportal.soapui.results.ResultLogger;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.*;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import io.reactivex.Maybe;
import rp.com.google.common.base.Function;
import rp.com.google.common.base.StandardSystemProperty;
import rp.com.google.common.base.Strings;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link StepBasedSoapUIServiceImpl}
 *
 * @author Andrei Varabyeu
 */
public class StepBasedSoapUIServiceImpl implements SoapUIService {

	private static final String LINE_SEPARATOR = StandardSystemProperty.LINE_SEPARATOR.value();
	private static final Map<String, Maybe<String>> ITEMS = new ConcurrentHashMap<String, Maybe<String>>();
	protected static final String ID = "rp_id";

	protected final SoapUIContext context;
	protected final ListenerParameters parameters;
	protected final List<ResultLogger<?>> resultLoggers;

	protected ReportPortal reportPortal;
	protected Launch launch;

	public StepBasedSoapUIServiceImpl(ListenerParameters parameters, List<ResultLogger<?>> resultLoggers) {
		this.context = new SoapUIContext();
		this.parameters = parameters;
		this.resultLoggers = resultLoggers;
	}

	public void startLaunch() {
		StartLaunchRQ rq = new StartLaunchRQ();
		rq.setName(this.parameters.getLaunchName());
		rq.setStartTime(Calendar.getInstance().getTime());
		rq.setTags(parameters.getTags());
		rq.setMode(parameters.getLaunchRunningMode());
		rq.setDescription(parameters.getDescription());

		this.reportPortal = ReportPortal.builder().withParameters(parameters).build();
		this.launch = reportPortal.newLaunch(rq);
		context.setLaunchFailed(false);
	}

	public void finishLaunch() {
		if (null != launch) {
			FinishExecutionRQ rq = new FinishExecutionRQ();
			rq.setEndTime(Calendar.getInstance().getTime());
			if (context.isTestCanceled()) {
				rq.setStatus(TestStatus.FAILED.getResult());
			} else {
				rq.setStatus(context.isLaunchFailed() ? TestStatus.FAILED.getResult() : TestStatus.FINISHED.getResult());
			}

			this.launch.finish(rq);
		}
	}

	public void startTestSuite(TestSuite testSuite) {
		if (null != launch) {
			StartTestItemRQ rq = new StartTestItemRQ();
			rq.setName(testSuite.getName());
			rq.setStartTime(Calendar.getInstance().getTime());
			rq.setType(TestItemType.TEST_SUITE.getValue());

			Maybe<String> rs = this.launch.startTestItem(rq);
			testSuite.setPropertyValue(ID, toStringId(rs));
		}
	}

	public void finishTestSuite(TestSuiteRunner testSuiteContext) {
		if (null != launch) {
			FinishTestItemRQ rq = new FinishTestItemRQ();
			rq.setEndTime(Calendar.getInstance().getTime());
			rq.setStatus(TestStatus.fromSoapUI(testSuiteContext.getStatus()));
			if (testSuiteContext.getStatus().equals(Status.FAILED)) {
				context.setLaunchFailed(true);
			}

			this.launch.finishTestItem(fromStringId(testSuiteContext.getTestSuite().getPropertyValue(ID)), rq);
		}
	}

	public void startTestCase(TestCase testCase, PropertyExpansionContext propertyContext) {
		Maybe<String> id = startItem(testCase.getName(),
				TestItemType.TEST_CASE,
				fromStringId(testCase.getTestSuite().getPropertyValue(ID))
		);
		testCase.setPropertyValue(ID, toStringId(id));
	}

	protected Maybe<String> startItem(String name, TestItemType type, Maybe<String> parentId) {
		if (null != launch) {
			StartTestItemRQ rq = new StartTestItemRQ();
			rq.setName(name);
			rq.setStartTime(Calendar.getInstance().getTime());
			rq.setType(type.getValue());

			return this.launch.startTestItem(parentId, rq);
		} else {
			return Maybe.empty();
		}
	}

	public void finishTestCase(TestCaseRunner testCaseContext, PropertyExpansionContext propertyContext) {
		if (null != launch) {
			FinishTestItemRQ rq = new FinishTestItemRQ();
			rq.setEndTime(Calendar.getInstance().getTime());
			rq.setStatus(TestStatus.fromSoapUI(testCaseContext.getStatus()));
			this.launch.finishTestItem(fromStringId(testCaseContext.getTestCase().getPropertyValue(ID)), rq);
		}
	}

	public void startTestStep(TestStep testStep, TestCaseRunContext context) {
		if (null != launch) {
			if (testStep.getPropertyValue(ID) != null) {
				return;
			}
			this.context.setTestCanceled(false);
			StartTestItemRQ rq = new StartTestItemRQ();
			rq.setName(testStep.getName());
			rq.setDescription(TestStepType.getStepType(testStep.getClass()));
			rq.setStartTime(Calendar.getInstance().getTime());
			rq.setType(TestItemType.TEST_STEP.getValue());
			Maybe<String> rs = this.launch.startTestItem(fromStringId(testStep.getTestCase().getPropertyValue(ID)), rq);
			context.setProperty(ID, rs);
		}
	}

	public void finishTestStep(TestStepResult testStepContext, TestCaseRunContext paramTestCaseRunContext) {
		if (null != launch) {
			Maybe<String> testId = (Maybe<String>) paramTestCaseRunContext.getProperty(ID);

			String logStepData = getLogStepData(testStepContext);
			if (!Strings.isNullOrEmpty(logStepData)) {
				ReportPortal.emitLog(logStepData, "INFO", Calendar.getInstance().getTime());
			}
			for (final SaveLogRQ rq : getStepLogReport(testStepContext)) {
				ReportPortal.emitLog(new Function<String, SaveLogRQ>() {
					@Override
					public SaveLogRQ apply(String id) {
						rq.setTestItemId(id);
						return rq;
					}
				});
			}

			if (TestStepStatus.FAILED.equals(testStepContext.getStatus())) {
				ReportPortal.emitLog(getStepError(testStepContext), "ERROR", Calendar.getInstance().getTime());
			}

			FinishTestItemRQ rq = new FinishTestItemRQ();
			rq.setEndTime(Calendar.getInstance().getTime());
			if (TestStepStatus.CANCELED.equals(testStepContext.getStatus())) {
				context.setTestCanceled(true);
			}
			rq.setStatus(TestStatus.fromSoapUIStep(testStepContext.getStatus()));

			this.launch.finishTestItem(testId, rq);
		}
	}

	protected String getLogStepData(TestStepResult testStepContext) {
		final StringWriter logData = new StringWriter();
		PrintWriter logWriter = new PrintWriter(logData);
		testStepContext.writeTo(logWriter);
		return logData.toString();
	}

	protected List<SaveLogRQ> getStepLogReport(TestStepResult testStepContext) {
		List<SaveLogRQ> rqs = new ArrayList<SaveLogRQ>();
		for (ResultLogger<?> resultLogger : resultLoggers) {
			if (resultLogger.supports(testStepContext)) {
				rqs.addAll(resultLogger.buildLogs(testStepContext));
			}
		}
		return rqs;
	}

	protected String getStepError(TestStepResult testStepContext) {

		String message;
		if (testStepContext.getError() != null) {
			message = "Exception: " + testStepContext.getError().getMessage() + LINE_SEPARATOR
					+ this.getStackTraceContext(testStepContext.getError());
		} else {
			StringBuilder messages = new StringBuilder();
			for (String messageLog : testStepContext.getMessages()) {
				messages.append(messageLog);
				messages.append(LINE_SEPARATOR);
			}
			message = messages.toString();
		}
		return message;
	}

	protected String getStackTraceContext(Throwable e) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < e.getStackTrace().length; i++) {
			result.append(e.getStackTrace()[i]);
			result.append(LINE_SEPARATOR);
		}
		return result.toString();
	}

	protected synchronized String toStringId(Maybe<String> id) {
		String tempID = UUID.randomUUID().toString();
		ITEMS.put(tempID, id);
		return tempID;
	}

	protected synchronized Maybe<String> fromStringId(String tempId) {
		return ITEMS.get(tempId);
	}
}