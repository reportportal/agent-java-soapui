/*
 * Copyright 2017 EPAM Systems
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
