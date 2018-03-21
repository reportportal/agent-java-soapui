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
package com.epam.reportportal.soapui.results;

import com.epam.reportportal.utils.markdown.MarkdownUtils;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlGroovyScriptTestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;

import java.util.Arrays;
import java.util.List;

/**
 * @author Andrei Varabyeu
 */
public class GroovyScriptLogger extends ResultLogger<TestStepResult> {

	public GroovyScriptLogger() {
		super(TestStepResult.class);
	}

	@Override
	protected List<SaveLogRQ> prepareLogs(TestStepResult result) {
		WsdlGroovyScriptTestStep step = ((WsdlGroovyScriptTestStep) result.getTestStep());
		return Arrays.asList(prepareEntity("INFO", "Executing script:"),
				prepareEntity("INFO", MarkdownUtils.asCode("groovy", step.getScript())));
	}

	@Override
	public boolean supports(TestStepResult result) {
		return super.supports(result) && WsdlGroovyScriptTestStep.class.isAssignableFrom(result.getTestStep().getClass());
	}

}
