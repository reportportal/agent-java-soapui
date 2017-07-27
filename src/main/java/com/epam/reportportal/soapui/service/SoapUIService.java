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

import java.util.Calendar;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.listeners.ListenersUtils;
import com.epam.reportportal.listeners.ReportPortalListenerContext;
import com.epam.reportportal.service.IReportPortalService;
import com.epam.reportportal.utils.TagsParser;
import com.epam.reportportal.utils.properties.ListenerProperty;
import com.epam.reportportal.utils.properties.PropertiesLoader;
import com.epam.reportportal.soapui.injection.SoapUIInjectorProvider;
import com.epam.reportportal.soapui.parameters.TestItemType;
import com.epam.reportportal.soapui.parameters.TestStatus;
import com.epam.reportportal.soapui.parameters.TestStepType;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.eviware.soapui.model.testsuite.*;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Default implementation of {@link ISoapUIService}
 * 
 * @author Raman_Usik
 *
 */
public class SoapUIService implements ISoapUIService {
	private final Logger logger = LoggerFactory.getLogger(SoapUIService.class);
	// private final org.apache.log4j.Logger log =
	// org.apache.log4j.Logger.getLogger(SoapUIService.class);
	private static final String ID = "id";

	@Inject
	@Named("soapClientService")
	private IReportPortalService serviceClient;

	@Inject
	private SoapUIContext soapUIContext;

	private Set<String> tags;
	private Mode mode;
	private String description;

	@Inject
	public SoapUIService(ListenerParameters parameters) {
		logger.debug(parameters.toString());
		tags = parameters.getTags();
		mode = parameters.getMode();
		description = parameters.getDescription();
	}

	/**
	 * Reload properties from SoapUI, reinitialize {@link SoapUIContext}, tags
	 * and {@link IReportPortalService} with reloaded properties.
	 * 
	 * User can change properties in soapui, so re initialization with new
	 * properties is required.
	 */
	@Override
	public void reinitService() {
		Properties properties = PropertiesLoader.reloadFromSoapUI(PropertiesLoader.getProperties());
		soapUIContext.setLaunchName(properties.getProperty(ListenerProperty.LAUNCH_NAME.getPropertyName()));
		this.tags = TagsParser.parseAsSet(properties.getProperty(ListenerProperty.LAUNCH_TAGS.getPropertyName()));
		this.mode = ListenersUtils.getLaunchMode(properties.getProperty(ListenerProperty.MODE.getPropertyName()));
		this.description = properties.getProperty(ListenerProperty.DESCRIPTION.getPropertyName());
		this.serviceClient = SoapUIInjectorProvider.getInstance()
				.getBean(Key.get(IReportPortalService.class, Names.named("soapClientService")));
	}

	@Override
	public void startLaunch() {
		StartLaunchRQ rq = new StartLaunchRQ();
		rq.setName(soapUIContext.getLaunchName());
		rq.setStartTime(Calendar.getInstance().getTime());
		rq.setTags(tags);
		rq.setMode(mode);
		if (!Strings.isNullOrEmpty(description)) {
			rq.setDescription(description);
		}
		EntryCreatedRS rs = null;
		try {
			rs = serviceClient.startLaunch(rq);
		} catch (Exception e) {
			ListenersUtils.handleException(e, logger, "Unable start the launch: '" + soapUIContext.getLaunchName() + "'");
		}
		if (rs != null) {
			soapUIContext.setLaunchId(rs.getId());
			soapUIContext.setLaunchFailed(false);
		}
	}

	@Override
	public void finishLaunch() {
		FinishExecutionRQ rq = new FinishExecutionRQ();
		rq.setEndTime(Calendar.getInstance().getTime());
		if (soapUIContext.isTestCanceled()) {
			rq.setStatus(TestStatus.FAILED.getResult());
		} else {
			rq.setStatus(soapUIContext.isLaunchFailed() ? TestStatus.FAILED.getResult() : TestStatus.FINISHED.getResult());
		}
		try {
			serviceClient.finishLaunch(soapUIContext.getLaunchId(), rq);
		} catch (Exception e) {
			ListenersUtils.handleException(e, logger, "Unable finish the launch: '" + soapUIContext.getLaunchId() + "'");
		}
	}

	@Override
	public void startTestSuite(TestSuite testSuite) {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId(soapUIContext.getLaunchId());
		rq.setName(testSuite.getName());
		rq.setStartTime(Calendar.getInstance().getTime());
		rq.setType(TestItemType.TEST_SUITE.getValue());
		EntryCreatedRS rs = null;
		try {
			rs = serviceClient.startRootTestItem(rq);
		} catch (Exception e) {
			ListenersUtils.handleException(e, logger, "Unable start test suite: '" + testSuite.getName() + "'");
		}
		if (rs != null) {
			testSuite.setPropertyValue(ID, rs.getId());
		}

	}

	@Override
	public void finishTestSuite(TestSuiteRunner testSuiteContext) {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(Calendar.getInstance().getTime());
		rq.setStatus(TestStatus.fromSoapUI(testSuiteContext.getStatus()));
		if (testSuiteContext.getStatus().equals(Status.FAILED)) {
			soapUIContext.setLaunchFailed(true);
		}
		try {
			serviceClient.finishTestItem(testSuiteContext.getTestSuite().getPropertyValue(ID), rq);
		} catch (Exception e) {
			ListenersUtils.handleException(e, logger,
					"Unable finish test suite: '" + String.valueOf(testSuiteContext.getTestSuite().getPropertyValue(ID)) + "'");
		}
	}

	@Override
	public void startTestCase(TestCase testCase) {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setName(testCase.getName());
		rq.setStartTime(Calendar.getInstance().getTime());
		rq.setLaunchId(soapUIContext.getLaunchId());
		rq.setType(TestItemType.TEST_CASE.getValue());
		EntryCreatedRS rs = null;
		try {
			rs = serviceClient.startTestItem(testCase.getTestSuite().getPropertyValue(ID), rq);
		} catch (Exception e) {
			ListenersUtils.handleException(e, logger, "Unable start test: '" + testCase.getName() + "'");
		}
		if (rs != null) {
			testCase.setPropertyValue(ID, rs.getId());
		}
	}

	@Override
	public void finishTestCase(TestCaseRunner testCaseContext) {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(Calendar.getInstance().getTime());
		rq.setStatus(TestStatus.fromSoapUI(testCaseContext.getStatus()));
		try {
			serviceClient.finishTestItem(testCaseContext.getTestCase().getPropertyValue(ID), rq);
		} catch (Exception e) {
			ListenersUtils.handleException(e, logger, "Unable finish test: '" + testCaseContext.getTestCase().getPropertyValue(ID) + "'");
		}

	}

	@Override
	public void startTestStep(TestStep testStep) {
		if (testStep.getPropertyValue(ID) != null) {
			return;
		}
		soapUIContext.setTestCanceled(false);
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setName(testStep.getName());
		rq.setLaunchId(soapUIContext.getLaunchId());
		rq.setDescription(TestStepType.getStepType(testStep.getClass()));
		rq.setStartTime(Calendar.getInstance().getTime());
		rq.setType(TestItemType.TEST_STEP.getValue());
		EntryCreatedRS rs = null;
		try {
			rs = serviceClient.startTestItem(testStep.getTestCase().getPropertyValue(ID), rq);
		} catch (Exception e) {
			ListenersUtils.handleException(e, logger, "Unable start test method: '" + testStep.getName() + "'");
		}
		if (rs != null) {
			ReportPortalListenerContext.setRunningNowItemId(rs.getId());
		}
	}

	@Override
	public void finishTestStep(TestStepResult testStepContext) {
		String testId = ReportPortalListenerContext.getRunningNowItemId();
		ReportPortalListenerContext.setRunningNowItemId(null);
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(Calendar.getInstance().getTime());
		if (TestStepStatus.CANCELED.equals(testStepContext.getStatus())) {
			soapUIContext.setTestCanceled(true);
		}
		rq.setStatus(TestStatus.fromSoapUIStep(testStepContext.getStatus()));
		try {
			serviceClient.finishTestItem(testId, rq);
		} catch (Exception e) {
			ListenersUtils.handleException(e, logger, "Unable finish test method: '" + testId + "'");
		}
	}

	@Override
	public void errorDuringExecution(TestStepResult testStepContext) {
		SaveLogRQ slrq = new SaveLogRQ();
		slrq.setTestItemId(ReportPortalListenerContext.getRunningNowItemId());
		slrq.setLevel("ERROR");
		slrq.setLogTime(Calendar.getInstance().getTime());
		String message = "";
		if (testStepContext.getError() != null) {
			message = "Exception: " + testStepContext.getError().getMessage() + System.getProperty("line.separator")
					+ this.getStackTraceContext(testStepContext.getError());
		} else {
			StringBuilder messages = new StringBuilder();
			for (String messageLog : testStepContext.getMessages()) {
				messages.append(messageLog);
				messages.append(System.getProperty("line.separator"));
			}
			message = messages.toString();
		}
		// log.info(message);
		slrq.setLogTime(Calendar.getInstance().getTime());
		slrq.setMessage(message);
		try {
			serviceClient.log(slrq);
		} catch (Exception e1) {
			ListenersUtils.handleException(e1, logger, "Unnable to send message to Report Portal");
		}
	}

	private String getStackTraceContext(Throwable e) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < e.getStackTrace().length; i++) {
			result.append(e.getStackTrace()[i]);
			result.append(System.getProperty("line.separator"));
		}
		return result.toString();
	}

	@Override
	public IReportPortalService getServiceClient() {
		return serviceClient;
	}
}
