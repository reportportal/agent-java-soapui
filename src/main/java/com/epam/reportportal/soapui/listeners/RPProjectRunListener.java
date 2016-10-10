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
package com.epam.reportportal.soapui.listeners;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.SimpleLayout;

import com.epam.reportportal.service.IReportPortalService;
import com.epam.reportportal.utils.properties.SoapUIPropertiesHolder;
import com.epam.ta.reportportal.log4j.appender.ReportPortalAppender;
import com.epam.reportportal.soapui.injection.SoapUIInjectorProvider;
import com.epam.reportportal.soapui.service.ISoapUIService;
import com.eviware.soapui.model.testsuite.*;

/**
 * Report portal related implementation of {@link ProjectRunListener}. This
 * listener should be used only with {@link RPTestSuiteRunListener} and
 * {@link RPTestRunListener}.
 * 
 * @author Raman_Usik
 *
 */
public class RPProjectRunListener implements ProjectRunListener {

	public static final String APPENDER_NAME = "ReportPortalAppender";
	public static final String BASE_APPENDER_NAME = "REPORTPORTAL";

	private ISoapUIService service;

	@Override
	public void beforeRun(ProjectRunner paramProjectRunner, ProjectRunContext paramProjectRunContext) {
		prepare(paramProjectRunContext);
		service.reinitService();
		// appender should use the same end point service as listener otherwise
		// properties mismatch is possible
		defineLogger(service.getServiceClient());
		service.startLaunch();
	}

	@Override
	public void afterRun(ProjectRunner paramProjectRunner, ProjectRunContext paramProjectRunContext) {
		service.finishLaunch();
	}

	@Override
	public void beforeTestSuite(ProjectRunner paramProjectRunner, ProjectRunContext paramProjectRunContext, TestSuite paramTestSuite) {
		service.startTestSuite(paramTestSuite);
	}

	@Override
	public void afterTestSuite(ProjectRunner paramProjectRunner, ProjectRunContext paramProjectRunContext,
			TestSuiteRunner paramTestSuiteRunner) {
		service.finishTestSuite(paramTestSuiteRunner);
	}

	/**
	 * Prepare soapui service for running:
	 * <li>load soap ui properties
	 * <li>instantiate soapui service
	 * 
	 * @param paramProjectRunContext
	 */
	private void prepare(ProjectRunContext paramProjectRunContext) {
		SoapUIPropertiesHolder.setSoapUIProperties(convertProperties(paramProjectRunContext.getProject().getProperties()));
		service = SoapUIInjectorProvider.getInstance().getBean(ISoapUIService.class);
	}

	private Map<String, String> convertProperties(Map<String, TestProperty> params) {
		Map<String, String> properties = new HashMap<String, String>();
		for (String key : params.keySet()) {
			properties.put(key, params.get(key).getValue());
		}
		return properties;
	}

	/**
	 * Instantiate, initialize report portal log4j appender and add it to all
	 * groovy.log logger.
	 * 
	 * @param junitStyleService
	 */
	private void defineLogger(IReportPortalService junitStyleService) {
		@SuppressWarnings("rawtypes")
		Enumeration loggers = org.apache.log4j.Logger.getRootLogger().getLoggerRepository().getCurrentLoggers();
		// SoapUILogAppender soapUIAppender = new SoapUILogAppender();
		ReportPortalAppender soapUIAppender = new ReportPortalAppender();
		// soapUIAppender.init(junitStyleService);
		soapUIAppender.setName(APPENDER_NAME);
		soapUIAppender.setLayout(new SimpleLayout());
		while (loggers.hasMoreElements()) {
			org.apache.log4j.Logger logger = (org.apache.log4j.Logger) loggers.nextElement();
			if (logger.getAppender(BASE_APPENDER_NAME) != null) {
				/*
				 * Report portal soapui log4j appender compatible only with
				 * groovy.log appender because this logger used for logging user
				 * logs from groovy scripts. Using soapui log4j appender with
				 * other appender unsafe because they may be not synchronized
				 * with listener(logs can be logged to report portal only if
				 * appender started test step)
				 */
				logger.removeAppender(BASE_APPENDER_NAME);
				if (logger.getName().equals("groovy.log")) {
					logger.addAppender(soapUIAppender);
				}
			}
		}
	}
}
