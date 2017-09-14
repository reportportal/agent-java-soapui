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

import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.eviware.soapui.impl.wsdl.submit.HttpMessageExchange;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import rp.com.google.common.base.Strings;

import java.util.Arrays;
import java.util.List;

import static rp.com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Andrei Varabyeu
 */
public class HttpMessageExchangeLogger extends ResultLogger<HttpMessageExchange> {

    public HttpMessageExchangeLogger() {
        super(HttpMessageExchange.class);
    }

    @Override
    protected List<SaveLogRQ> prepareLogs(HttpMessageExchange result) {
        final HttpResponse testRS = (HttpResponse) result.getResponse();
        return Arrays.asList(
                prepareEntity( "REQUEST", testRS.getRequestHeaders().toString(), testRS.getRequestContent()),
                prepareEntity("RESPONSE", testRS.getResponseHeaders().toString(), testRS.getContentAsString()));
    }

    private SaveLogRQ prepareEntity(String prefix, String headers, String body) {
        StringBuilder rqLog = new StringBuilder();
        rqLog
                .append(prefix).append("\n")
                .append("HEADERS:\n")
                .append(headers);

        if (!isNullOrEmpty(body)) {
            rqLog.append("BODY:\n")
                    .append(body);
        }
        return prepareEntity("INFO", rqLog.toString());

    }

}
