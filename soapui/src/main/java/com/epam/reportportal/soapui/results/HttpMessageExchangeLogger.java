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