# NiFi FHIR Validation Custom Processor

### Tags:
HAPI, FHIR, validate, EHR

### Properties:

| property                       | allowable values         | default value | description                                                                                          |
|--------------------------------|--------------------------|---------------|------------------------------------------------------------------------------------------------------|
| Set Pretty Print               | boolean                  | false         | Parser will encode resources with human-readable spacing and newlines between elements.              |
| Summary Mode                   | boolean                  | false         | Only elements marked by the FHIR specification as being summary elements will be included.           |
| Suppress Narratives            | boolean                  | false         | Narratives will not be included in the encoded values.                                               |
| Strip Versions from References | boolean                  | true          | Resource references containing a version will have the version removed when the resource is encoded. |
| Omit Resource ID               | boolean                  | false         | The ID of any resources being encoded will not be included in the output.                            |
| Server Base URL                | String                   | none          | Set the server's base URL used by the parser.                                                        |
| Parser Encoding                | String (`JSON` or `XML`) | `JSON`        | Specify encoding for parser to produce.                                                              |
| Standard Schema Validation     | boolean                  | false         | Should the validator validate the resource against the base schema.                                  |



### Relationships:

| Name    | Description                                               |
|---------|-----------------------------------------------------------|
| success | Any resource that is syntactially and schematically valid |
| failure | Any resource that is not valid or formatted incorrectly   |



### Reads Attributes:
None specified.


### Writes Attributes:
**valid**: outcome of validator  
**resourceType**: type of FHIR resource parsed


### Input Requirement:
This processor requires an incoming relationship.


### Notes:
- This processor uses the [HAPI FHIR](https://hapifhir.io/hapi-fhir/) library.
- Processor is using FHIR R4 formatting.

