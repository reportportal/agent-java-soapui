package com.epam.reportportal.soapui.results;

import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.eviware.soapui.impl.wsdl.submit.HttpMessageExchange;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import rp.com.google.common.base.Strings;

import java.util.Arrays;
import java.util.List;

/**
 * @author Andrei Varabyeu
 */
public class HttpMessageExchangeLogger extends ResultLogger<HttpMessageExchange> {

    public HttpMessageExchangeLogger() {
        super(HttpMessageExchange.class);
    }

    @Override
    protected List<SaveLogRQ> prepareLogs(String testId, HttpMessageExchange result) {
        final HttpResponse testRS = (HttpResponse) result.getResponse();
        return Arrays.asList(
                prepareEntity(testId, "REQUEST", testRS.getRequestHeaders().toString(), testRS.getRequestContent()),
                prepareEntity(testId, "RESPONSE", testRS.getResponseHeaders().toString(), testRS.getContentAsString()));
    }

    private SaveLogRQ prepareEntity(String testId, String prefix, String headers, String body) {
        StringBuilder rqLog = new StringBuilder();
        rqLog
                .append(prefix).append("\n")
                .append("HEADERS:\n")
                .append(headers);

        if (!Strings.isNullOrEmpty(body)) {
            rqLog.append("BODY:\n")
                    .append(body);
        }
        return prepareEntity(testId, "INFO", rqLog.toString());

    }

}
