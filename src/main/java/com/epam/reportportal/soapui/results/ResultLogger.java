package com.epam.reportportal.soapui.results;

import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.eviware.soapui.model.testsuite.TestStepResult;

import java.util.Calendar;
import java.util.List;

/**
 * @author Andrei Varabyeu
 */
public abstract class ResultLogger<T> {

    private Class<T> resultsType;

    public ResultLogger(Class<T> resultsType) {
        this.resultsType = resultsType;
    }

    abstract protected List<SaveLogRQ> prepareLogs(String testId, T result);

    public final List<SaveLogRQ> buildLogs(String testId, TestStepResult result) {
        //noinspection unchecked
        return prepareLogs(testId, (T) result);
    }

    public boolean supports(TestStepResult result) {
        return resultsType.isAssignableFrom(result.getClass());
    }

    protected final SaveLogRQ prepareEntity(String testId, String level, String message) {
        SaveLogRQ logRQ = new SaveLogRQ();
        logRQ.setLevel(level);
        logRQ.setTestItemId(testId);
        logRQ.setLogTime(Calendar.getInstance().getTime());
        logRQ.setMessage(message);
        return logRQ;
    }

}
