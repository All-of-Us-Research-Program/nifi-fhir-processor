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
    private String patient_ex_pretty_print;
    private String patient_ex_suppress_narratives;
    private String patient_ex_strip_versions;
    private String patient_ex_omit_id;
    private String patient_ex_standard_validate;
    private String patient_ex_server_url;
    private String patient_ex_json;
    private String patient_ex_xml;
    private String patient_ex_xml_parsed;
    private String patient_ex_xml_all_true;
    private String patient_ex_json_all_true;

    @Before
    public void init() throws IOException, URISyntaxException {
        // Generate a test runner to mock a processor in a flow
        runner = TestRunners.newTestRunner(MyProcessor.class);

        File patient_ex_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel.txt");
        File patient_ex_valid_resource_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel-parsed.txt");
        File patient_ex_summary_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel-summary-mode.txt");
        File patient_ex_pretty_print_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel-pretty-print.txt");
        File patient_ex_suppress_narratives_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel-suppress-narratives.txt");
        File patient_ex_strip_versions_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel-strip-versions.txt");
        File patient_ex_omit_id_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel-omit-id.txt");
        File patient_ex_standard_validate_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel-standard-validate.txt");
        File patient_ex_server_url_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel-server-url.txt");
        File patient_ex_json_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel-json.txt");
        File patient_ex_xml_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel-xml.txt");
        File patient_ex_xml_parsed_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel-xml-parsed.txt");
        File patient_ex_xml_all_true_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel-xml-all-true.txt");
        File patient_ex_json_all_true_file = new File(System.getProperty("user.dir") + "/src/test/java/org/apache/nifi/processors/fhir/patient-example-f201-roel-json-all-true.txt");

        Scanner s = new Scanner(patient_ex_file).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        patient_ex = result;

        s = new Scanner(patient_ex_valid_resource_file).useDelimiter("\\A");
        result = s.hasNext() ? s.next() : "";
        patient_ex_valid_resource = result;

        s = new Scanner(patient_ex_summary_file).useDelimiter("\\A");
        result = s.hasNext() ? s.next() : "";
        patient_ex_summary = result;

        s = new Scanner(patient_ex_pretty_print_file).useDelimiter("\\A");
        result = s.hasNext() ? s.next() : "";
        patient_ex_pretty_print = result;

        s = new Scanner(patient_ex_suppress_narratives_file).useDelimiter("\\A");
        result = s.hasNext() ? s.next() : "";
        patient_ex_suppress_narratives = result;

        s = new Scanner(patient_ex_strip_versions_file).useDelimiter("\\A");
        result = s.hasNext() ? s.next() : "";
        patient_ex_strip_versions = result;

        s = new Scanner(patient_ex_omit_id_file).useDelimiter("\\A");
        result = s.hasNext() ? s.next() : "";
        patient_ex_omit_id = result;

        s = new Scanner(patient_ex_standard_validate_file).useDelimiter("\\A");
        result = s.hasNext() ? s.next() : "";
        patient_ex_standard_validate = result;

        s = new Scanner(patient_ex_server_url_file).useDelimiter("\\A");
        result = s.hasNext() ? s.next() : "";
        patient_ex_server_url = result;

        s = new Scanner(patient_ex_json_file).useDelimiter("\\A");
        result = s.hasNext() ? s.next() : "";
        patient_ex_json = result;

        s = new Scanner(patient_ex_xml_file).useDelimiter("\\A");
        result = s.hasNext() ? s.next() : "";
        patient_ex_xml = result;

        s = new Scanner(patient_ex_xml_parsed_file).useDelimiter("\\A");
        result = s.hasNext() ? s.next() : "";
        patient_ex_xml_parsed = result;

        s = new Scanner(patient_ex_xml_all_true_file).useDelimiter("\\A");
        result = s.hasNext() ? s.next() : "";
        patient_ex_xml_all_true = result;

        s = new Scanner(patient_ex_json_all_true_file).useDelimiter("\\A");
        result = s.hasNext() ? s.next() : "";
        patient_ex_json_all_true = result;

    }


    @Test
    public void testValidPatientResource() throws IOException {

        // Get content from mock input stream
        InputStream content = new ByteArrayInputStream(patient_ex.getBytes());

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
    public void testInvalidPatientResource() throws IOException {

        // Get content from mock input stream
        InputStream content = new ByteArrayInputStream("{\"hello\":\"invalid resource\"}".getBytes());

        // Add the content to the runner
        runner.enqueue(content);

        // Run the enqueued content, it also takes an int = number of contents queued
        runner.run(1);

        // All results were processed with out failure
        runner.assertQueueEmpty();

        // If you need to read or do additional tests on results you can access the content
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(MyProcessor.FAILURE);
        assertTrue("1 failure match", results.size() == 1);
        MockFlowFile result = results.get(0);

        // Test attributes and content
        result.assertAttributeEquals(MyProcessor.RESOURCE_TYPE_ATTR, null);
        result.assertAttributeEquals(MyProcessor.VALID_ATTR, null);

    }


    @Test
    public void testSummaryMode() throws IOException {
        // Get content from mock input stream

        InputStream content = new ByteArrayInputStream(patient_ex.getBytes());

        // Set summary mode property to true
        runner.setProperty(MyProcessor.SET_SUMMARY_MODE, "true");

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

        // Set pretty print property to true
        runner.setProperty(MyProcessor.SET_PRETTY_PRINT, "true");

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
        result.assertContentEquals(patient_ex_pretty_print);

    }


    @Test
    public void testSuppressNarratives() throws IOException {
        // Get content from mock input stream

        InputStream content = new ByteArrayInputStream(patient_ex.getBytes());

        // Set suppress narratives property to true
        runner.setProperty(MyProcessor.SET_SUPPRESS_NARRATIVES, "true");

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
        result.assertContentEquals(patient_ex_suppress_narratives);

    }


    @Test
    public void testStripVersions() throws IOException {
        // Get content from mock input stream

        InputStream content = new ByteArrayInputStream(patient_ex.getBytes());

        // Set strip versions property to false
        runner.setProperty(MyProcessor.SET_STRIP_VERSIONS, "false");

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
        result.assertContentEquals(patient_ex_strip_versions);

    }


    @Test
    public void testOmitId() throws IOException {
        // Get content from mock input stream

        InputStream content = new ByteArrayInputStream(patient_ex.getBytes());

        // Set omit id property to true
        runner.setProperty(MyProcessor.SET_OMIT_ID, "true");

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
        result.assertContentEquals(patient_ex_omit_id);

    }


    @Test
    public void testStandardValidate() throws IOException {
        // Get content from mock input stream

        InputStream content = new ByteArrayInputStream(patient_ex.getBytes());

        // Set standard validate property to true
        runner.setProperty(MyProcessor.SET_STANDARD_VALIDATE, "true");

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
        result.assertContentEquals(patient_ex_standard_validate);

    }


    @Test
    public void testServerURL() throws IOException {
        // Get content from mock input stream

        InputStream content = new ByteArrayInputStream(patient_ex.getBytes());

        // Add properties
        runner.setProperty(MyProcessor.SET_SERVER_URL, "http://example.com/base");

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
        result.assertContentEquals(patient_ex_server_url);

    }


    @Test
    public void testParseXML() throws IOException {
        // Get content from mock input stream

        InputStream content = new ByteArrayInputStream(patient_ex_xml.getBytes());

        // Set parser type property to XML
        runner.setProperty(MyProcessor.PARSER_TYPE, "XML");

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
        result.assertContentEquals(patient_ex_xml_parsed);

    }


    @Test
    public void testAllPropertiesTrueXML() throws IOException {
        // Get content from mock input stream

        InputStream content = new ByteArrayInputStream(patient_ex_xml.getBytes());

        // Add properties -- true
        runner.setProperty(MyProcessor.SET_PRETTY_PRINT, "true");
        runner.setProperty(MyProcessor.SET_SUMMARY_MODE, "true");
        runner.setProperty(MyProcessor.SET_SUPPRESS_NARRATIVES, "true");
        runner.setProperty(MyProcessor.SET_STRIP_VERSIONS, "true");
        runner.setProperty(MyProcessor.SET_OMIT_ID, "true");
        runner.setProperty(MyProcessor.SET_STANDARD_VALIDATE, "true");
        runner.setProperty(MyProcessor.SET_SERVER_URL, "http://example.com/base");
        runner.setProperty(MyProcessor.PARSER_TYPE, "XML");

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
        result.assertContentEquals(patient_ex_xml_all_true);

    }


    @Test
    public void testAllPropertiesTrueJSON() throws IOException {
        // Get content from mock input stream

        InputStream content = new ByteArrayInputStream(patient_ex.getBytes());

        // Add properties -- true
        runner.setProperty(MyProcessor.SET_PRETTY_PRINT, "true");
        runner.setProperty(MyProcessor.SET_SUMMARY_MODE, "true");
        runner.setProperty(MyProcessor.SET_SUPPRESS_NARRATIVES, "true");
        runner.setProperty(MyProcessor.SET_STRIP_VERSIONS, "true");
        runner.setProperty(MyProcessor.SET_OMIT_ID, "true");
        runner.setProperty(MyProcessor.SET_STANDARD_VALIDATE, "true");
        runner.setProperty(MyProcessor.SET_SERVER_URL, "http://example.com/base");
        runner.setProperty(MyProcessor.PARSER_TYPE, "JSON");

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
        result.assertContentEquals(patient_ex_json_all_true);
    }

}
