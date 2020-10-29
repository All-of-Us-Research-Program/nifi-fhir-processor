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

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.lang.*;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.instance.model.api.IBaseResource;

@Tags({"fhir","hapi","custom","aviva"})
@CapabilityDescription("FHIR processor using HAPI")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class MyProcessor extends AbstractProcessor {

    public static final PropertyDescriptor SET_PRETTY_PRINT = new PropertyDescriptor
            .Builder().name("SET_PRETTY_PRINT")
            .displayName("Set Pretty Print")
            .description("Parser will encode resources with human-readable spacing and newlines between elements.")
            .defaultValue("false")
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor SET_SUMMARY_MODE = new PropertyDescriptor
            .Builder().name("SET_SUMMARY_MODE")
            .displayName("Summary Mode")
            .description("Only elements marked by the FHIR specification as being summary elements will be included.")
            .defaultValue("false")
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor SET_SUPPRESS_NARRATIVES = new PropertyDescriptor
            .Builder().name("SET_SUPPRESS_NARRATIVES")
            .displayName("Suppress Narratives")
            .description("Narratives will not be included in the encoded values.")
            .defaultValue("false")
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor SET_STRIP_VERSIONS = new PropertyDescriptor
            .Builder().name("SET_STRIP_VERSIONS")
            .displayName("Strip Versions from References")
            .description("Resource references containing a version will have the version removed when the resource is encoded.")
            .defaultValue("true")
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor SET_OMIT_ID = new PropertyDescriptor
            .Builder().name("SET_OMIT_ID")
            .displayName("Omit Resource ID")
            .description("The ID of any resources being encoded will not be included in the output.")
            .defaultValue("false")
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor SET_SERVER_URL = new PropertyDescriptor
            .Builder().name("SET_SERVER_URL")
            .displayName("Server Base URL")
            .description("Set the server's base URL used by the parser.")
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor SET_STANDARD_VALIDATE = new PropertyDescriptor
            .Builder().name("SET_STANDARD_VALIDATE")
            .displayName("Standard Schema Validation")
            .description("Should the validator validate the resource against the base schema.")
            .defaultValue("false")
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor PARSER_TYPE = new PropertyDescriptor
            .Builder().name("PARSER_TYPE")
            .displayName("Parser encoding")
            .description("Specify encoding for parser to produce.")
            .defaultValue("JSON")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("SUCCESS")
            .description("success")
            .build();

    public static final Relationship FAILURE = new Relationship.Builder()
            .name("FAILURE")
            .description("failure")
            .build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    public static final String RESOURCE_TYPE_ATTR = "resourceType";
    public static final String VALID_ATTR = "valid";

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(SET_PRETTY_PRINT);
        descriptors.add(SET_SUMMARY_MODE);
        descriptors.add(SET_SUPPRESS_NARRATIVES);
        descriptors.add(SET_STRIP_VERSIONS);
        descriptors.add(SET_OMIT_ID);
        descriptors.add(SET_SERVER_URL);
        descriptors.add(SET_STANDARD_VALIDATE);
        descriptors.add(PARSER_TYPE);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(SUCCESS);
        relationships.add(FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    public boolean isPrettyPrint;
    public boolean isSummaryMode;
    public boolean isSuppressNarratives;
    public boolean isStripVersions;
    public boolean isOmitId;
    public boolean isStandardValidate;
    public String serverBaseURL;
    public String parserType;

    @OnScheduled
    public void onScheduled(final ProcessContext context) {

      isPrettyPrint = Boolean.parseBoolean(context.getProperty(SET_PRETTY_PRINT).getValue());
      isSummaryMode = Boolean.parseBoolean(context.getProperty(SET_SUMMARY_MODE).getValue());
      isSuppressNarratives = Boolean.parseBoolean(context.getProperty(SET_SUPPRESS_NARRATIVES).getValue());
      isStripVersions = Boolean.parseBoolean(context.getProperty(SET_STRIP_VERSIONS).getValue());
      isOmitId = Boolean.parseBoolean(context.getProperty(SET_OMIT_ID).getValue());
      isStandardValidate = Boolean.parseBoolean(context.getProperty(SET_STANDARD_VALIDATE).getValue());
      serverBaseURL = context.getProperty(SET_SERVER_URL).getValue();
      parserType = context.getProperty(PARSER_TYPE).getValue();

    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if ( flowFile == null ) {
            return;
        }

        String relationship = "y";
        final AtomicReference<String> str = new AtomicReference<>();

        // read in FF text
        session.read(flowFile, new InputStreamCallback() {
            @Override
            public void process(InputStream in) throws IOException {
                try {
                    Scanner s = new Scanner(in).useDelimiter("\\A");
                    String result = s.hasNext() ? s.next() : "";
                    str.set(result);
                } catch(Exception ex) {
                    getLogger().error("Failed to read string.");
                }
            }
        });
        String input = str.get();

        try {
            // make FHIR context, validator, and parser
            FhirContext ctx = FhirContext.forR4();
            FhirValidator validator = ctx.newValidator();
            validator.setValidateAgainstStandardSchema(isStandardValidate);

            IParser parser;
            if(parserType == "XML") {
              parser = ctx.newXmlParser();
            } else {
              parser = ctx.newJsonParser();
            }

            parser.setPrettyPrint(isPrettyPrint);
            parser.setSummaryMode(isSummaryMode);
            parser.setSuppressNarratives(isSuppressNarratives);
            parser.setStripVersionsFromReferences(isStripVersions);
            parser.setOmitResourceId(isOmitId);
            if(serverBaseURL!=null) {
              parser.setServerBaseUrl(serverBaseURL);
            }

            // validate resource
            IBaseResource resource = parser.parseResource(input);
            Boolean valid = validator.validateWithResult(resource).isSuccessful();

            if(!valid) {
                relationship="f";
            }

            flowFile = session.write(flowFile, new OutputStreamCallback() {
                @Override
                public void process(OutputStream out) throws IOException {
                    out.write(parser.encodeResourceToString(resource).getBytes());
                }
            });

            // add FF attributes
            flowFile = session.putAttribute(flowFile, RESOURCE_TYPE_ATTR, resource.fhirType());
            flowFile = session.putAttribute(flowFile, VALID_ATTR, Boolean.toString(valid));

        } catch(Exception e) {
            relationship="f";
            getLogger().error(e.toString());
        }

        // transfer FF
        if(relationship == "f") {
            session.transfer(flowFile, FAILURE);
        } else {
            session.transfer(flowFile, SUCCESS);
        }
    }
}
