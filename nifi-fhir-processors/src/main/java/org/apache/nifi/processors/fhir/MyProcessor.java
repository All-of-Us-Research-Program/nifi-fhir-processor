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

import java.io.IOException;
import java.io.InputStream;

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

    public static final PropertyDescriptor MY_PROPERTY = new PropertyDescriptor
            .Builder().name("MY_PROPERTY")
            .displayName("My property")
            .description("Example Property")
            .required(false)
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

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(MY_PROPERTY);
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

    @OnScheduled
    public void onScheduled(final ProcessContext context) {

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
            getLogger().error("Failed to read json string.");
          }
        }
      });
      String input = str.get();

      try {
        // make FHIR context, validator, and parser
        FhirContext ctx = FhirContext.forR4();
        FhirValidator validator = ctx.newValidator();
        IParser parser = ctx.newJsonParser();

        // validate resource
        IBaseResource resource = parser.parseResource(input);
        Boolean valid = validator.validateWithResult(resource).isSuccessful();

        if(!valid) {
          relationship="f";
        }

        // add FF attributes
        // in PutFile processor, directory can be ./${resourceType} to route by resource type
        flowFile = session.putAttribute(flowFile, "resourceType", resource.fhirType());
        flowFile = session.putAttribute(flowFile, "valid", Boolean.toString(valid));
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
