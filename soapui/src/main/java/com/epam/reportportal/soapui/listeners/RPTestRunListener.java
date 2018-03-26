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

import com.epam.reportportal.soapui.service.SoapUIService;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;

import static com.epam.reportportal.soapui.listeners.RPProjectRunListener.RP_SERVICE;

/**
 * Report portal related implementation of {@link TestRunListener}.
 * This listener should be used only with {@link RPTestSuiteRunListener} and   {@link RPProjectRunListener}.
 *
 * @author Raman_Usik
 * @author Andrei Varabyeu
 */
public class RPTestRunListener implements TestRunListener {

    private SoapUIService service;

    public void beforeRun(TestCaseRunner runner,
            TestCaseRunContext context) {
        service = (SoapUIService) context.getProperty(RP_SERVICE);
        if (null == service) {
            service = SoapUIService.NOP_SERVICE;
        }
    }

    @Override
    public void afterRun(TestCaseRunner runner,
            TestCaseRunContext context) {
    }

    @Override
    public void beforeStep(TestCaseRunner paramTestCaseRunner,
            TestCaseRunContext context, TestStep testStep) {
        service.startTestStep(testStep, context);
    }

    @Override
    public void afterStep(TestCaseRunner paramTestCaseRunner,
            TestCaseRunContext paramTestCaseRunContext,
            TestStepResult paramTestStepResult) {
        service.finishTestStep(paramTestStepResult, paramTestCaseRunContext);
    }

    @Override
    @Deprecated
    public void beforeStep(TestCaseRunner paramTestCaseRunner,
            TestCaseRunContext paramTestCaseRunContext) {

    }

}
