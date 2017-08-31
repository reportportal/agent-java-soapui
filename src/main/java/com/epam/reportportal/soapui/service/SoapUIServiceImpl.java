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

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.service.ReportPortalClient;
import com.epam.reportportal.soapui.injection.SoapUIInjector;
import com.epam.reportportal.soapui.parameters.TestItemType;
import com.epam.reportportal.soapui.parameters.TestStatus;
import com.epam.reportportal.soapui.parameters.TestStepType;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.testsuite.*;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import io.reactivex.Maybe;
import rp.com.google.common.base.StandardSystemProperty;
import rp.com.google.common.base.Strings;
import rp.com.google.inject.Inject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link SoapUIServiceImpl}
 *
 * @author Raman_Usik
 */
public class SoapUIServiceImpl implements SoapUIService {

	private static final String LINE_SEPARATOR = StandardSystemProperty.LINE_SEPARATOR.value();
	private static final Map<String, Maybe<String>> ITEMS = new ConcurrentHashMap<String, Maybe<String>>();
	private static final String ID = "rp_id";

	@Inject
	private SoapUIContext context;
	@Inject
	private ListenerParameters parameters;
	@Inject
	private ReportPortalClient client;

	private ReportPortal reportPortal;

	public SoapUIServiceImpl(TestPropertyHolder propertyHolder) {
		SoapUIInjector.newOne(propertyHolder).injectMembers(this);
	}

	public void startLaunch() {
		StartLaunchRQ rq = new StartLaunchRQ();
		rq.setName(context.getLaunchName());
		rq.setStartTime(Calendar.getInstance().getTime());
		rq.setTags(parameters.getTags());
		rq.setMode(parameters.getLaunchRunningMode());

		this.reportPortal = ReportPortal.startLaunch(client, parameters, rq);

		context.setLaunchFailed(false);
	}

	public void finishLaunch() {
		FinishExecutionRQ rq = new FinishExecutionRQ();
		rq.setEndTime(Calendar.getInstance().getTime());
		if (context.isTestCanceled()) {
			rq.setStatus(TestStatus.FAILED.getResult());
		} else {
			rq.setStatus(context.isLaunchFailed() ? TestStatus.FAILED.getResult() : TestStatus.FINISHED.getResult());
		}

		reportPortal.finishLaunch(rq);

	}

	public void startTestSuite(TestSuite testSuite) {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setName(testSuite.getName());
		rq.setStartTime(Calendar.getInstance().getTime());
		rq.setType(TestItemType.TEST_SUITE.getValue());

		Maybe<String> rs = reportPortal.startTestItem(rq);
		testSuite.setPropertyValue(ID, toStringId(rs));
	}

	public void finishTestSuite(TestSuiteRunner testSuiteContext) {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(Calendar.getInstance().getTime());
		rq.setStatus(TestStatus.fromSoapUI(testSuiteContext.getStatus()));
		if (testSuiteContext.getStatus().equals(Status.FAILED)) {
			context.setLaunchFailed(true);
		}

		reportPortal.finishTestItem(fromStringId(testSuiteContext.getTestSuite().getPropertyValue(ID)), rq);

	}

	public void startTestCase(TestCase testCase) {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setName(testCase.getName());
		rq.setStartTime(Calendar.getInstance().getTime());
		rq.setType(TestItemType.TEST_CASE.getValue());

		final Maybe<String> parentId = fromStringId(testCase.getTestSuite().getPropertyValue(ID));

		Maybe<String> id;
		if (null == parentId) {
			id = reportPortal.startTestItem(rq);
		} else {
			id = reportPortal.startTestItem(parentId, rq);
		}

		testCase.setPropertyValue(ID, toStringId(id));

	}

	public void finishTestCase(TestCaseRunner testCaseContext) {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(Calendar.getInstance().getTime());
		rq.setStatus(TestStatus.fromSoapUI(testCaseContext.getStatus()));
		reportPortal.finishTestItem(fromStringId(testCaseContext.getTestCase().getPropertyValue(ID)), rq);
	}

	public void startTestStep(TestStep testStep, TestCaseRunContext context) {
		if (testStep.getPropertyValue(ID) != null) {
			return;
		}
		this.context.setTestCanceled(false);
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setName(testStep.getName());
		rq.setDescription(TestStepType.getStepType(testStep.getClass()));
		rq.setStartTime(Calendar.getInstance().getTime());
		rq.setType(TestItemType.TEST_STEP.getValue());
		Maybe<String> rs = reportPortal.startTestItem(fromStringId(testStep.getTestCase().getPropertyValue(ID)), rq);
		context.setProperty(ID, rs);
	}

	public void finishTestStep(TestStepResult testStepContext, TestCaseRunContext paramTestCaseRunContext) {
		Maybe<String> testId = (Maybe<String>) paramTestCaseRunContext.getProperty(ID);

		final StringWriter logData = new StringWriter();
		PrintWriter logWriter = new PrintWriter(logData);
		testStepContext.writeTo(logWriter);

		final String logString = logData.toString();
		if (!Strings.isNullOrEmpty(logString)) {
			ReportPortal.emitLog(logString, "INFO", Calendar.getInstance().getTime());

		}

		if (TestStepStatus.FAILED.equals(testStepContext.getStatus())) {
			logStepError(testStepContext);
		}

		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(Calendar.getInstance().getTime());
		if (TestStepStatus.CANCELED.equals(testStepContext.getStatus())) {
			context.setTestCanceled(true);
		}
		rq.setStatus(TestStatus.fromSoapUIStep(testStepContext.getStatus()));

		reportPortal.finishTestItem(testId, rq);

	}

	private void logStepError(TestStepResult testStepContext) {

		String message;
		if (testStepContext.getError() != null) {
			message = "Exception: " + testStepContext.getError().getMessage() + LINE_SEPARATOR + this
					.getStackTraceContext(testStepContext.getError());
		} else {
			StringBuilder messages = new StringBuilder();
			for (String messageLog : testStepContext.getMessages()) {
				messages.append(messageLog);
				messages.append(LINE_SEPARATOR);
			}
			message = messages.toString();
		}

		ReportPortal.emitLog(message, "ERROR", Calendar.getInstance().getTime());
	}

	private String getStackTraceContext(Throwable e) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < e.getStackTrace().length; i++) {
			result.append(e.getStackTrace()[i]);
			result.append(LINE_SEPARATOR);
		}
		return result.toString();
	}

	private synchronized String toStringId(Maybe<String> id) {
		String tempID = UUID.randomUUID().toString();
		ITEMS.put(tempID, id);
		return tempID;
	}

	private synchronized Maybe<String> fromStringId(String tempId) {
		SoapUI.log("TEMP ID: " + tempId);
		SoapUI.log("ALL: " + ITEMS);
		Maybe<String> id = ITEMS.get(tempId);
		//		ITEMS.invalidate(tempId);
		return id;

	}
}
