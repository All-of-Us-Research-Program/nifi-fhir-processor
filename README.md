# NiFi FHIR Validation Custom Processor

### To run in NiFi:

1. Run `mvn clean install`
2. Copy the generated nar to the NiFi library: `cp nifi-fhir-nar/target/nifi-fhir-nar-1.0-SNAPSHOT.nar $NIFI_HOME/lib`
3. From the `$NIFI_HOME` directory, re-start NiFi: `bin/nifi.sh run`
4. Make a process with `GetFile --> FHIR Validator --> PutFile` where the destination directory is `${whatever_directory_you_want}/${resourceType}`

##### Notes:
- This processor uses the HAPI FHIR library. Some HAPI jars may need to be added to `$NIFI_HOME`.
- Processor has 2 relationships: Success and Failure. The processor fails if the FHIR resource is not valid or if it is formatted wrong, and displays an error log with the `DataFormatException`.
- Processor only reads JSON, and is using FHIR R4 formatting.
