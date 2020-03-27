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
import com.epam.reportportal.service.item.TestCaseIdEntry;
import com.epam.reportportal.soapui.parameters.TestItemType;
import com.epam.reportportal.soapui.parameters.TestStatus;
import com.epam.reportportal.soapui.parameters.TestStepType;
import com.epam.reportportal.soapui.results.ResultLogger;
import com.epam.reportportal.utils.AttributeParser;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.*;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import io.reactivex.Maybe;
import rp.com.google.common.base.StandardSystemProperty;
import rp.com.google.common.base.Strings;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

/**
 * Default implementation of {@link StepBasedSoapUIServiceImpl}
 *
 * @author Andrei Varabyeu
 */
public class StepBasedSoapUIServiceImpl implements SoapUIService {

	private static final String LINE_SEPARATOR = StandardSystemProperty.LINE_SEPARATOR.value();
	private static final Map<String, Maybe<String>> ITEMS = new ConcurrentHashMap<String, Maybe<String>>();
	protected static final String ID = "rp_id";
	protected static final String RP_PROPERTY_SEPARATOR = ".";
	protected static final String ITEM_ATTRIBUTES_PROPERTY = "rp.item.attributes";
	protected static final String TEST_CASE_ID_PROPERTY = "rp.case.id";
	protected static final String CODE_REF_SEPARATOR = ".";
	protected static final String RP_ITEM_PROPERTIES = "RP item properties";

	protected final SoapUIContext context;
	protected final ListenerParameters parameters;
	protected final List<ResultLogger<?>> resultLoggers;

	protected ReportPortal reportPortal;
	protected Launch launch;
	protected Maybe<String> launchId;

	public StepBasedSoapUIServiceImpl(ListenerParameters parameters, List<ResultLogger<?>> resultLoggers) {
		this.context = new SoapUIContext();
		this.parameters = parameters;
		this.resultLoggers = resultLoggers;
	}

	public void startLaunch() {
		StartLaunchRQ rq = new StartLaunchRQ();
		rq.setName(this.parameters.getLaunchName());
		rq.setStartTime(Calendar.getInstance().getTime());
		rq.setAttributes(parameters.getAttributes());
		rq.setMode(parameters.getLaunchRunningMode());
		rq.setDescription(parameters.getDescription());

		this.reportPortal = buildReportPortal(parameters);
		this.launch = reportPortal.newLaunch(rq);
		this.launchId = launch.start();
		context.setLaunchFailed(false);
	}

	protected ReportPortal buildReportPortal(ListenerParameters parameters) {
		return ReportPortal.builder().withParameters(parameters).build();
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

			String codeRef = getCodeRef(testSuite);
			rq.setCodeRef(codeRef);
			TestCaseIdEntry testCaseIdEntry = getTestCaseId(testSuite.getProperties(), TEST_CASE_ID_PROPERTY, codeRef);
			rq.setTestCaseId(testCaseIdEntry.getId());

			rq.setAttributes(getItemAttributes(testSuite.getProperties(), ITEM_ATTRIBUTES_PROPERTY));

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
		Maybe<String> id = startItem(testCase, TestItemType.TEST_CASE, fromStringId(testCase.getTestSuite().getPropertyValue(ID)));
		testCase.setPropertyValue(ID, toStringId(id));
	}

	protected Maybe<String> startItem(TestCase testCase, TestItemType type, Maybe<String> parentId) {
		if (null != launch) {
			StartTestItemRQ rq = new StartTestItemRQ();
			rq.setName(testCase.getName());
			rq.setStartTime(Calendar.getInstance().getTime());
			rq.setType(type.getValue());

			String codeRef = getCodeRef(testCase);
			rq.setCodeRef(codeRef);
			TestCaseIdEntry testCaseIdEntry = getTestCaseId(testCase.getProperties(), TEST_CASE_ID_PROPERTY, codeRef);
			rq.setTestCaseId(testCaseIdEntry.getId());

			rq.setAttributes(getItemAttributes(testCase.getProperties(), ITEM_ATTRIBUTES_PROPERTY));

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
		if (null != launch && !RP_ITEM_PROPERTIES.equals(testStep.getName())) {
			if (testStep.getPropertyValue(ID) != null) {
				return;
			}
			this.context.setTestCanceled(false);
			StartTestItemRQ rq = new StartTestItemRQ();
			rq.setName(testStep.getName());
			rq.setDescription(TestStepType.getStepType(testStep.getClass()));
			rq.setStartTime(Calendar.getInstance().getTime());
			rq.setType(TestItemType.TEST_STEP.getValue());

			String codeRef = getCodeRef(testStep);
			rq.setCodeRef(codeRef);
			TestCaseIdEntry testCaseIdEntry = ofNullable(testStep.getTestCase().getTestStepByName(RP_ITEM_PROPERTIES)).map(
					testCaseProperties -> getTestCaseId(testCaseProperties.getProperties(),
							TEST_CASE_ID_PROPERTY + RP_PROPERTY_SEPARATOR + testStep.getName(),
							codeRef
					)).orElseGet(() -> new TestCaseIdEntry(codeRef));
			rq.setTestCaseId(testCaseIdEntry.getId());

			ofNullable(testStep.getTestCase().getTestStepByName(RP_ITEM_PROPERTIES)).ifPresent(stepProperties -> rq.setAttributes(
					getItemAttributes(stepProperties.getProperties(),
							ITEM_ATTRIBUTES_PROPERTY + RP_PROPERTY_SEPARATOR + testStep.getName()
					)));

			Maybe<String> rs = this.launch.startTestItem(fromStringId(testStep.getTestCase().getPropertyValue(ID)), rq);
			context.setProperty(ID, rs);
		}
	}

	protected String getCodeRef(TestSuite testSuite) {
		return testSuite.getProject().getName() + CODE_REF_SEPARATOR + testSuite.getName();
	}

	protected String getCodeRef(TestCase testCase) {
		String testSuiteCodeRef = getCodeRef(testCase.getTestSuite());
		return testSuiteCodeRef + CODE_REF_SEPARATOR + testCase.getName();
	}

	protected String getCodeRef(TestStep testStep) {
		String testCaseCodeRef = getCodeRef(testStep.getTestCase());
		return testCaseCodeRef + CODE_REF_SEPARATOR + testStep.getName();
	}

	protected TestCaseIdEntry getTestCaseId(Map<String, TestProperty> properties, String testCaseIdPropertyKey, String codeRef) {
		return retrieveProperty(properties, testCaseIdPropertyKey).map(testCaseId -> new TestCaseIdEntry(testCaseId.getValue())).
				orElseGet(() -> new TestCaseIdEntry(codeRef));
	}

	protected Set<ItemAttributesRQ> getItemAttributes(Map<String, TestProperty> properties, String attributesPropertyKey) {
		return retrieveProperty(properties, attributesPropertyKey).map(TestProperty::getValue)
				.map(AttributeParser::parseAsSet)
				.orElseGet(Collections::emptySet);
	}

	private Optional<TestProperty> retrieveProperty(Map<String, TestProperty> properties, String propertyKey) {
		return ofNullable(properties.get(propertyKey));
	}

	public void finishTestStep(TestStepResult testStepContext, TestCaseRunContext paramTestCaseRunContext) {
		if (null != launch && !RP_ITEM_PROPERTIES.equals(testStepContext.getTestStep().getName())) {
			Maybe<String> testId = (Maybe<String>) paramTestCaseRunContext.getProperty(ID);

			String logStepData = getLogStepData(testStepContext);
			if (!Strings.isNullOrEmpty(logStepData)) {
				ReportPortal.emitLog(logStepData, "INFO", Calendar.getInstance().getTime());
			}
			for (final SaveLogRQ rq : getStepLogReport(testStepContext)) {
				ReportPortal.emitLog((Function<String, SaveLogRQ>) id -> {
					rq.setItemUuid(id);
					return rq;
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