# NiFi FHIR Validation Custom Processor

### To run in NiFi:

1. Copy the nar file to your NiFi library: `cp nifi-fhir-nar/target/nifi-fhir-nar-1.0-SNAPSHOT.nar $NIFI_HOME/lib`
3. From the `$NIFI_HOME` directory, re-start NiFi: `bin/nifi.sh run`
4. Make a process with `MyProcessor`, and customize desired properties.

##### Notes:
- This processor uses the [HAPI FHIR](https://hapifhir.io/hapi-fhir/) library.
- Processor has 2 relationships: Success and Failure. The processor fails if the FHIR resource is not valid or if it is formatted wrong, and displays an error log with the `DataFormatException`.
- Processor reads JSON and XML encoded resources, and is using FHIR R4 formatting.
