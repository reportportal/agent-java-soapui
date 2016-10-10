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

import java.util.Date;

import org.apache.log4j.spi.LoggingEvent;

import com.epam.reportportal.message.ReportPortalMessage;
import com.epam.reportportal.restclient.endpoint.exception.RestEndpointIOException;
import com.epam.reportportal.service.IReportPortalService;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.google.common.base.Throwables;

/**
 * log4j report portal appender service
 * 
 */
class AppenderService {

	public AppenderService() {
	}

	/**
	 * Save log to report portal
	 * 
	 * @param reportPortalService
	 * @param project
	 * @param message
	 * @param saveLogRQ
	 */
	public void sendLogToRP(IReportPortalService reportPortalService, ReportPortalMessage message, SaveLogRQ saveLogRQ) {
		try {
			reportPortalService.log(saveLogRQ);
		} catch (RestEndpointIOException e) {
			Throwables.propagateIfPossible(e);
		}
	}

	/**
	 * Build {@link SaveLogRQ} object using current test item id and<br>
	 * current {@link LoggingEvent} object.
	 * 
	 * @param event
	 * @param currentItemId
	 * @return
	 */
	public SaveLogRQ buildSaveLogRQ(LoggingEvent event, String currentItemId, String message) {
		SaveLogRQ saveLogRQ = new SaveLogRQ();
		saveLogRQ.setMessage(message);
		saveLogRQ.setLogTime(new Date(event.getTimeStamp()));
		saveLogRQ.setTestItemId(currentItemId);
		saveLogRQ.setLevel(event.getLevel().toString());
		return saveLogRQ;
	}

	/**
	 * Build {@link LoggingEvent} object using existing object and set new
	 * message.<br>
	 * This method can be used for creating new {@link LoggingEvent} object with
	 * out message wrapper.<br>
	 * 
	 * @param event
	 * @param message
	 * @return
	 */
	public LoggingEvent buildNewEvent(LoggingEvent event, String message) {
		return new LoggingEvent(event.getFQNOfLoggerClass(), event.getLogger(), event.getTimeStamp(), event.getLevel(), message,
				event.getThreadName(), event.getThrowableInformation(), event.getNDC(), event.getLocationInformation(),
				event.getProperties());
	}

}
