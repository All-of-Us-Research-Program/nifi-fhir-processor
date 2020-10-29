/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.processors.fhir;


import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.util.MockFlowFile;
import static org.junit.Assert.*;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;


public class MyProcessorTest {

    private TestRunner runner;
    private String patient_ex;
    private String patient_ex_valid_resource;
    private String patient_ex_summary;

    @Before
    public void init() throws IOException, URISyntaxException {
        // Generate a test runner to mock a processor in a flow
        runner = TestRunners.newTestRunner(MyProcessor.class);

        File patient_ex_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel.txt");
        File patient_ex_valid_resource_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel-parsed.txt");
        File patient_ex_summary_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel-summary-mode.txt");

        Scanner s = new Scanner(patient_ex_file).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        patient_ex = result;

        s = new Scanner(patient_ex_valid_resource_file).useDelimiter("\\A");
        result = s.hasNext() ? s.next() : "";
        patient_ex_valid_resource = result;

        s = new Scanner(patient_ex_summary_file).useDelimiter("\\A");
        result = s.hasNext() ? s.next() : "";
        patient_ex_summary = result;

    }


    @Test
    public void testValidPatientResource() throws IOException {

        // Get content from mock input stream
        InputStream content = new ByteArrayInputStream(patient_ex.getBytes());

        // Add properties
        runner.setProperty(MyProcessor.SET_PRETTY_PRINT, "false");
        runner.setProperty(MyProcessor.SET_SUMMARY_MODE, "false");
        runner.setProperty(MyProcessor.SET_SUPPRESS_NARRATIVES, "false");
        runner.setProperty(MyProcessor.SET_STRIP_VERSIONS, "true");
        runner.setProperty(MyProcessor.SET_OMIT_ID, "false");
        runner.setProperty(MyProcessor.SET_SERVER_URL, "false");

        // Add the content to the runner
        runner.enqueue(content);

        // Run the enqueued content, it also takes an int = number of contents queued
        runner.run(1);

        // All results were processed with out failure
        runner.assertQueueEmpty();

        // If you need to read or do additional tests on results you can access the content
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(MyProcessor.SUCCESS);
        assertTrue("1 match", results.size() == 1);
        MockFlowFile result = results.get(0);

        // Test attributes and content
        result.assertAttributeEquals(MyProcessor.RESOURCE_TYPE_ATTR, "Patient");
        result.assertAttributeEquals(MyProcessor.VALID_ATTR, "true");
        result.assertContentEquals(patient_ex_valid_resource);

    }


    @Test
    public void testSummaryMode() throws IOException {
        // Get content from mock input stream

        InputStream content = new ByteArrayInputStream(patient_ex.getBytes());

        // Add properties
        runner.setProperty(MyProcessor.SET_PRETTY_PRINT, "false");
        runner.setProperty(MyProcessor.SET_SUMMARY_MODE, "true");
        runner.setProperty(MyProcessor.SET_SUPPRESS_NARRATIVES, "false");
        runner.setProperty(MyProcessor.SET_STRIP_VERSIONS, "true");
        runner.setProperty(MyProcessor.SET_OMIT_ID, "false");
        runner.setProperty(MyProcessor.SET_SERVER_URL, "false");

        // Add the content to the runner
        runner.enqueue(content);

        // Run the enqueued content, it also takes an int = number of contents queued
        runner.run(1);

        // All results were processed with out failure
        runner.assertQueueEmpty();

        // If you need to read or do additional tests on results you can access the content
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(MyProcessor.SUCCESS);
        assertTrue("1 match", results.size() == 1);
        MockFlowFile result = results.get(0);

        // Test attributes and content
        result.assertAttributeEquals(MyProcessor.RESOURCE_TYPE_ATTR, "Patient");
        result.assertAttributeEquals(MyProcessor.VALID_ATTR, "true");
        System.out.println(result);
        result.assertContentEquals(patient_ex_summary);

    }


    @Test
    public void testPrettyPrint() throws IOException {
        // Get content from mock input stream

        InputStream content = new ByteArrayInputStream(patient_ex.getBytes());

        // Add properties
        runner.setProperty(MyProcessor.SET_PRETTY_PRINT, "true");
        runner.setProperty(MyProcessor.SET_SUMMARY_MODE, "false");
        runner.setProperty(MyProcessor.SET_SUPPRESS_NARRATIVES, "false");
        runner.setProperty(MyProcessor.SET_STRIP_VERSIONS, "true");
        runner.setProperty(MyProcessor.SET_OMIT_ID, "false");
        runner.setProperty(MyProcessor.SET_SERVER_URL, "false");

        // Add the content to the runner
        runner.enqueue(content);

        // Run the enqueued content, it also takes an int = number of contents queued
        runner.run(1);

        // All results were processed with out failure
        runner.assertQueueEmpty();

        // If you need to read or do additional tests on results you can access the content
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(MyProcessor.SUCCESS);
        assertTrue("1 match", results.size() == 1);
        MockFlowFile result = results.get(0);

        // Test attributes and content
        result.assertAttributeEquals(MyProcessor.RESOURCE_TYPE_ATTR, "Patient");
        result.assertAttributeEquals(MyProcessor.VALID_ATTR, "true");
        System.out.println(result);
//        result.assertContentEquals(patient_ex);

    }
}
