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

import com.epam.reportportal.service.LoggingContext;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.UUID;

import static com.epam.reportportal.soapui.service.TestBasedSoapUIServiceImpl.*;

/**
 * @author Kirill_Fedorovsky
 */

public class CustomLoggerService {

    public static void log(String message, String level) {
        if (TEST_CASE_ID != null) {
            LoggingContext loggingContext = CONTEXT_MAP.get(TEST_CASE_ID);
            loggingContext.emit(asFunction(message, level, Calendar.getInstance().getTime()));   
        }
    }

    public static void logFile(String message, File file, String level) throws IOException {
        if (TEST_CASE_ID != null) {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            SaveLogRQ.File rqFile = new SaveLogRQ.File();
            rqFile.setContent(fileContent);
            rqFile.setContentType(Files.probeContentType(file.toPath()));
            rqFile.setName(UUID.randomUUID().toString());
            LoggingContext loggingContext = CONTEXT_MAP.get(TEST_CASE_ID);
            loggingContext.emit(asFunctionFile(message, rqFile, level, Calendar.getInstance().getTime()));
        }
    }
}