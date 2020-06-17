package com.epam.reportportal.soapui.util;

import com.epam.reportportal.soapui.listeners.RPProjectRunListener;
import com.epam.reportportal.soapui.service.SoapUIService;
import com.eviware.soapui.model.testsuite.*;
import com.eviware.soapui.support.types.StringToObjectMap;

import java.util.Collections;
import java.util.Map;

/**
 * An utility class to run a {@link TestSuite} and a {@link TestCase} from a groovy scripts inside SoapUI with a
 * ReportPortal reporter.
 *
 * <p>Usage:
 * <pre>{@code import com.epam.reportportal.soapui.util.ReportUtil;
 *
 * project = testRunner.getTestCase().getTestSuite().getProject();
 * testSuite = project.getTestSuiteByName("TestSuite 1");
 * testCase = testSuite.getTestCaseByName("TestCase 1");
 * ReportUtil.runTestCase(context, testCase);
 * }</pre></p>
 */
public class ReportUtil {

    private ReportUtil() {
    }

    /**
     * Run a {@link TestCase} and report it into ReportPortal. This will be done in sync mode only.
     *
     * @param context  a SoapUI context object
     * @param testCase a {@link TestCase} object
     * @return {@link TestCaseRunner} result object
     */
    public static TestCaseRunner runTestCase(TestRunContext context, TestCase testCase) {
        SoapUIService rpService = (SoapUIService) context.getProperty(RPProjectRunListener.RP_SERVICE);
        Map<String, SoapUIService> rpServiceMap = Collections.singletonMap(RPProjectRunListener.RP_SERVICE, rpService);
        rpService.startTestCase(testCase, context);
        TestCaseRunner runner = testCase.run(new StringToObjectMap(rpServiceMap), false);
        rpService.finishTestCase(runner, context);
        return runner;
    }

    /**
     * Run a {@link TestSuite} and report it into ReportPortal. This will be done in sync mode only.
     *
     * @param context   a SoapUI context object
     * @param testSuite a {@link TestSuite} object
     * @return {@link TestSuiteRunner} result object
     */
    public static TestSuiteRunner runTestSuite(TestRunContext context, TestSuite testSuite) {
        SoapUIService rpService = (SoapUIService) context.getProperty(RPProjectRunListener.RP_SERVICE);
        Map<String, SoapUIService> rpServiceMap = Collections.singletonMap(RPProjectRunListener.RP_SERVICE, rpService);
        rpService.startTestSuite(testSuite);
        TestSuiteRunner runner = testSuite.run(new StringToObjectMap(rpServiceMap), false);
        rpService.finishTestSuite(runner);
        return runner;
    }
}
