/*
 * Copyright (C) 2018 EPAM Systems
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
package com.epam.reportportal.soapui.listeners;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.soapui.results.GroovyScriptLogger;
import com.epam.reportportal.soapui.results.HttpMessageExchangeLogger;
import com.epam.reportportal.soapui.results.ResultLogger;
import com.epam.reportportal.soapui.service.SoapUIService;
import com.epam.reportportal.soapui.service.StepBasedSoapUIServiceImpl;
import com.epam.reportportal.soapui.service.TestBasedSoapUIServiceImpl;
import com.epam.reportportal.utils.properties.PropertiesLoader;
import com.eviware.soapui.model.TestPropertyHolder;
import rp.com.google.common.base.Strings;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class RpServiceBuilder {

	private static final String REPORTER_TYPE_PROPERTY = "rp.reporter.type";

	public static SoapUIService build(TestPropertyHolder contextProperties) {

		Properties properties = convertProperties(contextProperties);

		PropertiesLoader propertiesLoader = PropertiesLoader.load();
		propertiesLoader.overrideWith(properties);
		propertiesLoader.validate();

		List<ResultLogger<?>> resultLoggers = Arrays.asList(new HttpMessageExchangeLogger(), new GroovyScriptLogger());
		String listenerType = properties.getProperty(REPORTER_TYPE_PROPERTY);

		return ListenerType.fromString(listenerType).newOne(new ListenerParameters(propertiesLoader), resultLoggers);

	}

	private static Properties convertProperties(TestPropertyHolder params) {
		Properties properties = new Properties();
		for (String key : params.getPropertyNames()) {
			final String value = params.getPropertyValue(key);
			if (null != value) {
				properties.put(key, value);
			}

		}
		return properties;
	}

	enum ListenerType {
		TEST_BASED {
			@Override
			SoapUIService newOne(ListenerParameters parameters, List<ResultLogger<?>> resultLoggers) {
				return new TestBasedSoapUIServiceImpl(parameters, resultLoggers);
			}
		},
		STEP_BASED {
			@Override
			SoapUIService newOne(ListenerParameters parameters, List<ResultLogger<?>> resultLoggers) {
				return new StepBasedSoapUIServiceImpl(parameters, resultLoggers);
			}
		};

		abstract SoapUIService newOne(ListenerParameters parameters, List<ResultLogger<?>> resultLoggers);

		static ListenerType fromString(String type) {
			if (Strings.isNullOrEmpty(type)) {
				return STEP_BASED;
			}

			for (ListenerType listenerType : values()) {
				if (listenerType.name().equalsIgnoreCase(type)) {
					return listenerType;
				}
			}

			return STEP_BASED;
		}

	}
}
