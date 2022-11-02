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
package com.epam.reportportal.soapui.service;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.service.LoggingContext;
import com.epam.reportportal.soapui.listeners.RpServiceBuilder;
import com.epam.reportportal.soapui.parameters.TestItemType;
import com.epam.reportportal.soapui.parameters.TestStatus;
import com.epam.reportportal.soapui.results.ResultLogger;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.*;
import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;
import rp.com.google.common.base.Strings;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.epam.reportportal.utils.markdown.MarkdownUtils.asMarkdown;

/**
 * Default implementation of {@link TestBasedSoapUIServiceImpl}
 *
 * @author Raman_Usik
 */
public class TestBasedSoapUIServiceImpl extends StepBasedSoapUIServiceImpl implements SoapUIService {

    public static final Map<String, LoggingContext> CONTEXT_MAP = new ConcurrentHashMap<String, LoggingContext>();
    private static final String LEVEL_INFO = "INFO";

    public TestBasedSoapUIServiceImpl(ListenerParameters parameters, List<ResultLogger<?>> resultLoggers) {
        super(parameters, resultLoggers);
    }

    @Override
    public void startTestCase(TestCase testCase, PropertyExpansionContext propertyContext) {
        Maybe<String> id = startItem(testCase, TestItemType.TEST_STEP, fromStringId(testCase.getTestSuite().getPropertyValue(ID)));
        testCase.setPropertyValue(ID, toStringId(id));

        LoggingContext loggingContext = LoggingContext.init(launchId, id, reportPortal.getClient(), Schedulers.from(executor));
        CONTEXT_MAP.put(testCase.getId(), loggingContext);
    }

    public void startTestStep(TestStep testStep, TestCaseRunContext context) {
        if (!RpServiceBuilder.REPORTER_DISABLE) {
            String log = asMarkdown(String.format("# ===========STEP '%s' STARTED===========", testStep.getName()));

            LoggingContext loggingContext = CONTEXT_MAP.get(getTestCaseId(context.getTestCase()));
            loggingContext.emit(asFunction(log, LEVEL_INFO, Calendar.getInstance().getTime()));
        }
    }

    public void finishTestStep(TestStepResult testStepContext, TestCaseRunContext paramTestCaseRunContext) {
        LoggingContext loggingContext = CONTEXT_MAP.get(getTestCaseId(paramTestCaseRunContext.getTestCase()));

        if (!RpServiceBuilder.REPORTER_DISABLE) {
            String logStepData = getLogStepData(testStepContext);
            if (!Strings.isNullOrEmpty(logStepData)) {
                loggingContext.emit(asFunction(logStepData, LEVEL_INFO, Calendar.getInstance().getTime()));
            }
            for (final SaveLogRQ rq : getStepLogReport(testStepContext)) {
                loggingContext.emit((Function<String, SaveLogRQ>) id -> {
                    rq.setItemUuid(id);
                    return rq;
                });
            }
        }

        if (TestStepResult.TestStepStatus.FAILED.equals(testStepContext.getStatus())) {
            loggingContext.emit(asFunction(getStepError(testStepContext), "ERROR", Calendar.getInstance().getTime()));
        }

        if (TestStepResult.TestStepStatus.CANCELED.equals(testStepContext.getStatus())) {
            context.setTestCanceled(true);
        }

        if (!RpServiceBuilder.REPORTER_DISABLE) {
            String log = asMarkdown(String.format("# ===========STEP '%s' %s===========", testStepContext.getTestStep().getName(), TestStatus.fromSoapUIStep(testStepContext.getStatus())));
            loggingContext.emit(asFunction(log, LEVEL_INFO, Calendar.getInstance().getTime()));
        }
    }

    @Override
    public void finishTestCase(TestCaseRunner testCaseContext, PropertyExpansionContext propertyContext) {
        CONTEXT_MAP.get(getTestCaseId(testCaseContext.getTestCase())).completed().blockingAwait();

        super.finishTestCase(testCaseContext, propertyContext);
    }

    public static Function<String, SaveLogRQ> asFunction(final String message, final String level, final Date time) {
        return id -> {
            SaveLogRQ rq = new SaveLogRQ();
            rq.setLevel(level);
            rq.setLogTime(time);
            rq.setItemUuid(id);
            rq.setMessage(message);

            return rq;
        };
    }

    public static Function<String, SaveLogRQ> asFunctionFile(final String message, final SaveLogRQ.File file, final String level, final Date time) {
        return id -> {
            SaveLogRQ rq = new SaveLogRQ();
            rq.setLevel(level);
            rq.setLogTime(time);
            rq.setItemUuid(id);
            rq.setMessage(message);
            rq.setFile(file);

            return rq;
        };
    }

    private String getTestCaseId(TestCase testCase) {
        return testCase.getId();
    }
}