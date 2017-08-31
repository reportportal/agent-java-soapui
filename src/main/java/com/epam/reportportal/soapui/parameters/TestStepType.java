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
