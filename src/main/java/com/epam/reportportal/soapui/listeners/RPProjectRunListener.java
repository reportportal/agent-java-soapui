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

import com.epam.reportportal.soapui.service.SoapUIService;
import com.epam.reportportal.soapui.service.SoapUIServiceImpl;
import com.epam.ta.reportportal.log4j.appender.ReportPortalAppender;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.testsuite.*;
import org.apache.log4j.SimpleLayout;

import java.util.Enumeration;

/**
 * Report portal related implementation of {@link ProjectRunListener}. This
 * listener should be used only with {@link RPTestSuiteRunListener} and
 * {@link RPTestRunListener}.
 *
 * @author Raman_Usik
 */
public class RPProjectRunListener implements ProjectRunListener {

	public static final String APPENDER_NAME = "ReportPortalAppender";
	public static final String BASE_APPENDER_NAME = "REPORTPORTAL";
	static final String RP_SERVICE = "rp_service";

	private SoapUIService service;

	@Override
	public void beforeRun(ProjectRunner runner, ProjectRunContext context) {
		try {
			service = new SoapUIServiceImpl(context.getProject());
			defineLogger();
		} catch (Throwable t) {
			SoapUI.log("ReportPortal plugin cannot be initialized. " + t.getMessage());
			service = SoapUIService.NOP_SERVICE;
		}
		service.startLaunch();
		context.setProperty(RP_SERVICE, service);

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
	 * Instantiate, initialize report portal log4j appender and add it to all
	 * groovy.log logger.
	 */
	private void defineLogger() {
		@SuppressWarnings("rawtypes")

		ReportPortalAppender soapUIAppender = new ReportPortalAppender();
		soapUIAppender.setName(APPENDER_NAME);
		soapUIAppender.setLayout(new SimpleLayout());
		Enumeration loggers = org.apache.log4j.Logger.getRootLogger().getLoggerRepository().getCurrentLoggers();

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
