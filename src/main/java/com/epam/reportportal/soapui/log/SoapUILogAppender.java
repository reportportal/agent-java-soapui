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
package com.epam.reportportal.soapui.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.epam.reportportal.exception.InternalReportPortalClientException;
import com.epam.reportportal.listeners.ReportPortalListenerContext;
import com.epam.reportportal.message.ReportPortalMessage;
import com.epam.reportportal.service.IReportPortalService;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;

/**
 * Log4j appender for report portal
 * 
 * @author Andrei Varabyeu
 * 
 */

// TODO consider to merge with report portal log4 appender (agreed)
// FIXME remove after testing. Functionality moved to main RP Log4j appender
// class!
@Deprecated
public class SoapUILogAppender extends AppenderSkeleton {

	private static final Logger logger = Logger.getLogger(SoapUILogAppender.class);

	private IReportPortalService reportPortalService;
	private AppenderService appenderService;

	public SoapUILogAppender() {
		this.appenderService = new AppenderService();
	}

	public void init(IReportPortalService junitStyleService) {
		this.reportPortalService = junitStyleService;
	}

	@Override
	protected void append(LoggingEvent event) {
		if (reportPortalService == null) {
			logger.error("Unable send log message to report portal, reason: logger hasn't initialized.");
		}
		String currentItemId = ReportPortalListenerContext.getRunningNowItemId();
		if (null == currentItemId) {
			return;
		}
		ReportPortalMessage message = null;
		if (event.getMessage().getClass().equals(ReportPortalMessage.class)) {
			message = (ReportPortalMessage) event.getMessage();
			event = appenderService.buildNewEvent(event, message.getMessage());
		}
		if (this.layout == null) {
			throw new InternalReportPortalClientException("Layout hasn't set. Please set layout.");
		}
		String log = this.layout.format(event);
		SaveLogRQ saveLogRQ = appenderService.buildSaveLogRQ(event, currentItemId, log);
		appenderService.sendLogToRP(reportPortalService, message, saveLogRQ);

	}

	@Override
	public void close() {
	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

}
