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
package com.epam.reportportal.soapui.injection;

import com.epam.reportportal.guice.ListenerPropertyValue;
import com.epam.reportportal.guice.ReportPortalClientModule;
import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.restclient.endpoint.RestEndpoint;
import com.epam.reportportal.service.BatchedReportPortalService;
import com.epam.reportportal.service.IReportPortalService;
import com.epam.reportportal.utils.properties.ListenerProperty;
import com.epam.reportportal.soapui.service.ISoapUIService;
import com.epam.reportportal.soapui.service.SoapUIContext;
import com.epam.reportportal.soapui.service.SoapUIService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

/**
 * Guice module with sopaui client beans.
 * 
 * @author Raman_Usik
 */

public class SoapUIListenersModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ISoapUIService.class).to(SoapUIService.class).asEagerSingleton();
	}

	/**
	 * Provide particularly initialized soapui context
	 * 
	 * @param parameters
	 * @return SoapUIContext
	 */
	@Provides
	public SoapUIContext provideSoapUIContext(ListenerParameters parameters) {
		SoapUIContext soapUIContext = new SoapUIContext();
		soapUIContext.setLaunchName(parameters.getLaunchName());
		return soapUIContext;
	}

	/*
	 * In SoapUI context this bean should be prototype because in each launch
	 * run properties can be reloaded so new service should be build
	 */
	@Provides
	@Named("soapClientService")
	public IReportPortalService provideJUnitStyleService(RestEndpoint restEndpoint,
			@ListenerPropertyValue(ListenerProperty.PROJECT_NAME) String project,
			@ListenerPropertyValue(ListenerProperty.BATCH_SIZE_LOGS) String batchLogsSize) {
		return new BatchedReportPortalService(restEndpoint, ReportPortalClientModule.API_BASE, project, Integer.parseInt(batchLogsSize));
	}
}
