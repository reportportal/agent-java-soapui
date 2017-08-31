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

import com.epam.reportportal.guice.ListenerPropertyBinder;
import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.soapui.results.GroovyScriptLogger;
import com.epam.reportportal.soapui.results.HttpMessageExchangeLogger;
import com.epam.reportportal.soapui.results.ResultLogger;
import com.epam.reportportal.soapui.service.SoapUIContext;
import com.epam.reportportal.utils.properties.ListenerProperty;
import com.epam.reportportal.utils.properties.PropertiesLoader;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.TestPropertyHolder;
import rp.com.google.inject.*;
import rp.com.google.inject.name.Named;
import rp.com.google.inject.name.Names;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Guice module with soapUI client beans.
 *
 * @author Raman_Usik
 */

class SoapUIListenersModule implements Module {

	private Properties properties;

	SoapUIListenersModule(TestPropertyHolder contextProperties) {
		this.properties = convertProperties(contextProperties);
	}

	@Override
	public void configure(Binder binder) {
		SoapUI.log("BINDING STARTED!");
		PropertiesLoader propertiesLoader = PropertiesLoader.load();
		propertiesLoader.overrideWith(properties);

		SoapUI.log("PROPERTIES ARE LOADED!");
		binder.bind(ListenerParameters.class).toInstance(new ListenerParameters(propertiesLoader));



		Names.bindProperties(binder, properties);
		for (final ListenerProperty listenerProperty : ListenerProperty.values()) {
			System.out.println("BINDING: " + listenerProperty);
			SoapUI.log("BINDING: " + listenerProperty);
			binder.bind(Key.get(String.class, ListenerPropertyBinder.named(listenerProperty))).toProvider(new Provider<String>() {
				@Override
				public String get() {
					SoapUI.log("GETTING PROPERTY!!: " + listenerProperty);
					return properties.getProperty(listenerProperty.getPropertyName());
				}
			});
		}
		binder.bind(PropertiesLoader.class).toInstance(propertiesLoader);

	}

	/**
	 * Provide particularly initialized soapUI context
	 *
	 * @param parameters ReportPortal listener parameters
	 * @return SoapUIContext
	 */
	@Provides
	public SoapUIContext provideSoapUIContext(ListenerParameters parameters) {
		SoapUIContext soapUIContext = new SoapUIContext();
		soapUIContext.setLaunchName(parameters.getLaunchName());
		return soapUIContext;
	}

	@Provides
	@Named("resultLoggers")
	@Singleton
	public List<ResultLogger<?>> provideResultLoggers() {
		return Arrays.asList(new HttpMessageExchangeLogger(), new GroovyScriptLogger());
	}

	private Properties convertProperties(TestPropertyHolder params) {
		Properties properties = new Properties();
		for (String key : params.getPropertyNames()) {
			final String value = params.getPropertyValue(key);
			if (null != value) {
				SoapUI.log(String.format("%s: %s", key, value));
				properties.put(key, value);
			}

		}
		return properties;
	}

}
