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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
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
    public void init() {
        // Generate a test runner to mock a processor in a flow
        runner = TestRunners.newTestRunner(MyProcessor.class);

        // Content to be mock a json file
        patient_ex = "{\n" +
                "  \"resourceType\": \"Patient\",\n" +
                "  \"id\": \"f201\",\n" +
                "  \"text\": {\n" +
                "    \"status\": \"generated\",\n" +
                "    \"div\": \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><p><b>Generated Narrative with Details</b></p><p><b>id</b>: f201</p><p><b>identifier</b>: BSN = 123456789 (OFFICIAL), BSN = 123456789 (OFFICIAL)</p><p><b>active</b>: true</p><p><b>name</b>: Roel(OFFICIAL)</p><p><b>telecom</b>: ph: +31612345678(MOBILE), ph: +31201234567(HOME)</p><p><b>gender</b>: male</p><p><b>birthDate</b>: 13/03/1960</p><p><b>deceased</b>: false</p><p><b>address</b>: Bos en Lommerplein 280 Amsterdam 1055RW NLD (HOME)</p><p><b>maritalStatus</b>: Legally married <span>(Details : {SNOMED CT code '36629006' = 'Legal marriage', given as 'Legally married'}; {http://terminology.hl7.org/CodeSystem/v3-MaritalStatus code 'M' = 'Married)</span></p><p><b>multipleBirth</b>: false</p><p><b>photo</b>: </p><h3>Contacts</h3><table><tr><td>-</td><td><b>Relationship</b></td><td><b>Name</b></td><td><b>Telecom</b></td></tr><tr><td>*</td><td>Wife <span>(Details : {SNOMED CT code '127850001' = 'Wife', given as 'Wife'}; {http://terminology.hl7.org/CodeSystem/v2-0131 code 'N' = 'Next-of-Kin; {http://terminology.hl7.org/CodeSystem/v3-RoleCode code 'WIFE' = 'wife)</span></td><td>Ariadne Bor-Jansma</td><td>ph: +31201234567(HOME)</td></tr></table><h3>Communications</h3><table><tr><td>-</td><td><b>Language</b></td><td><b>Preferred</b></td></tr><tr><td>*</td><td>Dutch <span>(Details : {urn:ietf:bcp:47 code 'nl-NL' = 'Dutch (Region=Netherlands)', given as 'Dutch'})</span></td><td>true</td></tr></table><p><b>managingOrganization</b>: <a>AUMC</a></p></div>\"\n" +
                "  },\n" +
                "  \"identifier\": [\n" +
                "    {\n" +
                "      \"use\": \"official\",\n" +
                "      \"type\": {\n" +
                "        \"text\": \"BSN\"\n" +
                "      },\n" +
                "      \"system\": \"urn:oid:2.16.840.1.113883.2.4.6.3\",\n" +
                "      \"value\": \"123456789\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"use\": \"official\",\n" +
                "      \"type\": {\n" +
                "        \"text\": \"BSN\"\n" +
                "      },\n" +
                "      \"system\": \"urn:oid:2.16.840.1.113883.2.4.6.3\",\n" +
                "      \"value\": \"123456789\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"active\": true,\n" +
                "  \"name\": [\n" +
                "    {\n" +
                "      \"use\": \"official\",\n" +
                "      \"text\": \"Roel\",\n" +
                "      \"family\": \"Bor\",\n" +
                "      \"given\": [\n" +
                "        \"Roelof Olaf\"\n" +
                "      ],\n" +
                "      \"prefix\": [\n" +
                "        \"Drs.\"\n" +
                "      ],\n" +
                "      \"suffix\": [\n" +
                "        \"PDEng.\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"telecom\": [\n" +
                "    {\n" +
                "      \"system\": \"phone\",\n" +
                "      \"value\": \"+31612345678\",\n" +
                "      \"use\": \"mobile\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"system\": \"phone\",\n" +
                "      \"value\": \"+31201234567\",\n" +
                "      \"use\": \"home\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"gender\": \"male\",\n" +
                "  \"birthDate\": \"1960-03-13\",\n" +
                "  \"deceasedBoolean\": false,\n" +
                "  \"address\": [\n" +
                "    {\n" +
                "      \"use\": \"home\",\n" +
                "      \"line\": [\n" +
                "        \"Bos en Lommerplein 280\"\n" +
                "      ],\n" +
                "      \"city\": \"Amsterdam\",\n" +
                "      \"postalCode\": \"1055RW\",\n" +
                "      \"country\": \"NLD\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"maritalStatus\": {\n" +
                "    \"coding\": [\n" +
                "      {\n" +
                "        \"system\": \"http://snomed.info/sct\",\n" +
                "        \"code\": \"36629006\",\n" +
                "        \"display\": \"Legally married\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"system\": \"http://terminology.hl7.org/CodeSystem/v3-MaritalStatus\",\n" +
                "        \"code\": \"M\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"multipleBirthBoolean\": false,\n" +
                "  \"photo\": [\n" +
                "    {\n" +
                "      \"contentType\": \"image/jpeg\",\n" +
                "      \"url\": \"Binary/f006\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"contact\": [\n" +
                "    {\n" +
                "      \"relationship\": [\n" +
                "        {\n" +
                "          \"coding\": [\n" +
                "            {\n" +
                "              \"system\": \"http://snomed.info/sct\",\n" +
                "              \"code\": \"127850001\",\n" +
                "              \"display\": \"Wife\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0131\",\n" +
                "              \"code\": \"N\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"system\": \"http://terminology.hl7.org/CodeSystem/v3-RoleCode\",\n" +
                "              \"code\": \"WIFE\"\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ],\n" +
                "      \"name\": {\n" +
                "        \"use\": \"usual\",\n" +
                "        \"text\": \"Ariadne Bor-Jansma\"\n" +
                "      },\n" +
                "      \"telecom\": [\n" +
                "        {\n" +
                "          \"system\": \"phone\",\n" +
                "          \"value\": \"+31201234567\",\n" +
                "          \"use\": \"home\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"communication\": [\n" +
                "    {\n" +
                "      \"language\": {\n" +
                "        \"coding\": [\n" +
                "          {\n" +
                "            \"system\": \"urn:ietf:bcp:47\",\n" +
                "            \"code\": \"nl-NL\",\n" +
                "            \"display\": \"Dutch\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      \"preferred\": true\n" +
                "    }\n" +
                "  ],\n" +
                "  \"managingOrganization\": {\n" +
                "    \"reference\": \"Organization/f201\",\n" +
                "    \"display\": \"AUMC\"\n" +
                "  }\n" +
                "}";

        patient_ex_valid_resource ="{\"resourceType\":\"Patient\",\"id\":\"f201\",\"text\":{\"status\":\"generated\",\"div\":\"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><p><b>Generated Narrative with Details</b></p><p><b>id</b>: f201</p><p><b>identifier</b>: BSN = 123456789 (OFFICIAL), BSN = 123456789 (OFFICIAL)</p><p><b>active</b>: true</p><p><b>name</b>: Roel(OFFICIAL)</p><p><b>telecom</b>: ph: +31612345678(MOBILE), ph: +31201234567(HOME)</p><p><b>gender</b>: male</p><p><b>birthDate</b>: 13/03/1960</p><p><b>deceased</b>: false</p><p><b>address</b>: Bos en Lommerplein 280 Amsterdam 1055RW NLD (HOME)</p><p><b>maritalStatus</b>: Legally married <span>(Details : {SNOMED CT code '36629006' = 'Legal marriage', given as 'Legally married'}; {http://terminology.hl7.org/CodeSystem/v3-MaritalStatus code 'M' = 'Married)</span></p><p><b>multipleBirth</b>: false</p><p><b>photo</b>: </p><h3>Contacts</h3><table><tr><td>-</td><td><b>Relationship</b></td><td><b>Name</b></td><td><b>Telecom</b></td></tr><tr><td>*</td><td>Wife <span>(Details : {SNOMED CT code '127850001' = 'Wife', given as 'Wife'}; {http://terminology.hl7.org/CodeSystem/v2-0131 code 'N' = 'Next-of-Kin; {http://terminology.hl7.org/CodeSystem/v3-RoleCode code 'WIFE' = 'wife)</span></td><td>Ariadne Bor-Jansma</td><td>ph: +31201234567(HOME)</td></tr></table><h3>Communications</h3><table><tr><td>-</td><td><b>Language</b></td><td><b>Preferred</b></td></tr><tr><td>*</td><td>Dutch <span>(Details : {urn:ietf:bcp:47 code 'nl-NL' = 'Dutch (Region=Netherlands)', given as 'Dutch'})</span></td><td>true</td></tr></table><p><b>managingOrganization</b>: <a>AUMC</a></p></div>\"},\"identifier\":[{\"use\":\"official\",\"type\":{\"text\":\"BSN\"},\"system\":\"urn:oid:2.16.840.1.113883.2.4.6.3\",\"value\":\"123456789\"},{\"use\":\"official\",\"type\":{\"text\":\"BSN\"},\"system\":\"urn:oid:2.16.840.1.113883.2.4.6.3\",\"value\":\"123456789\"}],\"active\":true,\"name\":[{\"use\":\"official\",\"text\":\"Roel\",\"family\":\"Bor\",\"given\":[\"Roelof Olaf\"],\"prefix\":[\"Drs.\"],\"suffix\":[\"PDEng.\"]}],\"telecom\":[{\"system\":\"phone\",\"value\":\"+31612345678\",\"use\":\"mobile\"},{\"system\":\"phone\",\"value\":\"+31201234567\",\"use\":\"home\"}],\"gender\":\"male\",\"birthDate\":\"1960-03-13\",\"deceasedBoolean\":false,\"address\":[{\"use\":\"home\",\"line\":[\"Bos en Lommerplein 280\"],\"city\":\"Amsterdam\",\"postalCode\":\"1055RW\",\"country\":\"NLD\"}],\"maritalStatus\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"36629006\",\"display\":\"Legally married\"},{\"system\":\"http://terminology.hl7.org/CodeSystem/v3-MaritalStatus\",\"code\":\"M\"}]},\"multipleBirthBoolean\":false,\"photo\":[{\"contentType\":\"image/jpeg\",\"url\":\"Binary/f006\"}],\"contact\":[{\"relationship\":[{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"127850001\",\"display\":\"Wife\"},{\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0131\",\"code\":\"N\"},{\"system\":\"http://terminology.hl7.org/CodeSystem/v3-RoleCode\",\"code\":\"WIFE\"}]}],\"name\":{\"use\":\"usual\",\"text\":\"Ariadne Bor-Jansma\"},\"telecom\":[{\"system\":\"phone\",\"value\":\"+31201234567\",\"use\":\"home\"}]}],\"communication\":[{\"language\":{\"coding\":[{\"system\":\"urn:ietf:bcp:47\",\"code\":\"nl-NL\",\"display\":\"Dutch\"}]},\"preferred\":true}],\"managingOrganization\":{\"reference\":\"Organization/f201\",\"display\":\"AUMC\"}}";

        patient_ex_summary = "{\"resourceType\":\"Patient\",\"id\":\"f201\",\"meta\":{\"tag\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v3-ObservationValue\",\"code\":\"SUBSETTED\",\"display\":\"Resource encoded in summary mode\"}]},\"identifier\":[{\"use\":\"official\",\"type\":{\"text\":\"BSN\"},\"system\":\"urn:oid:2.16.840.1.113883.2.4.6.3\",\"value\":\"123456789\"},{\"use\":\"official\",\"type\":{\"text\":\"BSN\"},\"system\":\"urn:oid:2.16.840.1.113883.2.4.6.3\",\"value\":\"123456789\"}],\"active\":true,\"name\":[{\"use\":\"official\",\"text\":\"Roel\",\"family\":\"Bor\",\"given\":[\"Roelof Olaf\"],\"prefix\":[\"Drs.\"],\"suffix\":[\"PDEng.\"]}],\"telecom\":[{\"system\":\"phone\",\"value\":\"+31612345678\",\"use\":\"mobile\"},{\"system\":\"phone\",\"value\":\"+31201234567\",\"use\":\"home\"}],\"gender\":\"male\",\"birthDate\":\"1960-03-13\",\"deceasedBoolean\":false,\"address\":[{\"use\":\"home\",\"line\":[\"Bos en Lommerplein 280\"],\"city\":\"Amsterdam\",\"postalCode\":\"1055RW\",\"country\":\"NLD\"}],\"managingOrganization\":{\"reference\":\"Organization/f201\",\"display\":\"AUMC\"}}";
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
}
