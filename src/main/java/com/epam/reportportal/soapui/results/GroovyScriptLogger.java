package com.epam.reportportal.soapui.results;

import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlGroovyScriptTestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;

import java.util.Collections;
import java.util.List;

/**
 * @author Andrei Varabyeu
 */
public class GroovyScriptLogger extends ResultLogger<TestStepResult> {

    public GroovyScriptLogger() {
        super(TestStepResult.class);
    }

    @Override
    protected List<SaveLogRQ> prepareLogs(String testId, TestStepResult result) {
        WsdlGroovyScriptTestStep step = ((WsdlGroovyScriptTestStep) result.getTestStep());
        return Collections.singletonList(prepareEntity(testId, "INFO", "Executing script:\n" + step.getScript()));
    }

    @Override
    public boolean supports(TestStepResult result) {
        return super.supports(result) && WsdlGroovyScriptTestStep.class
                .isAssignableFrom(result.getTestStep().getClass());
    }
}
