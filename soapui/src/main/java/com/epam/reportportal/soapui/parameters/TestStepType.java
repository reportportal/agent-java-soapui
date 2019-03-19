/*
 * Copyright (C) 2019 EPAM Systems
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
package com.epam.reportportal.soapui.parameters;

import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.ManualTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlDelayTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlGotoTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlGroovyScriptTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlPropertiesTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlRunTestCaseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.ProPlaceholderStepFactory.WsdlProPlaceholderTestStep;
import com.eviware.soapui.model.testsuite.TestStep;

public enum TestStepType {
    //formatter:off
    AMF_REQUEST(AMFRequestTestStep.class),
    HTTP_REQUEST(HttpTestRequestStep.class),
    JDBC_REQUEST(JdbcRequestTestStep.class),
    MANUAL_STEP(ManualTestStep.class),
    PROPERTY_TRANSFER(PropertyTransfersTestStep.class),
    PLACEHOLDER_STEP(WsdlProPlaceholderTestStep.class),
    REST_REQUEST(RestTestRequestStep.class),
    DELAY_STEP(WsdlDelayTestStep.class),
    GOTO_STEP(WsdlGotoTestStep.class),
    GROOVY_SCRIPT(WsdlGroovyScriptTestStep.class),
    MOCK_RESPONSE(WsdlMockResponseTestStep.class),
    PROPERTIES_STEP(WsdlPropertiesTestStep.class),
    TEST_CASE(WsdlRunTestCaseTestStep.class),
    SOAP_REQUEST(WsdlTestRequestStep.class);
    //formatter:on

    private static final String DEFAULT = "DEFAULT_STEP";
    private Class<? extends TestStep> clazz;

    TestStepType(Class<? extends TestStep> clazz) {
        this.clazz = clazz;
    }

    public static String getStepType(Class<? extends TestStep> testStepClass) {
        for (TestStepType testType : TestStepType.values()) {
            if (testType.clazz.equals(testStepClass)) {
                return testType.toString();
            }
        }
        return DEFAULT;
    }
}