# Request Annotation Baseline

## Scope

Request annotation governance applies to Java classes that match both conditions:

- File name ends with `Request.java`
- File path is under an API request package, currently `sandwish-admin-api/src/main/java/com/github/thundax/modules/*/request/`

Current scan result:

- Total Request classes: 43
- Admin API Request classes: 43
- Front API Request classes: 0
- Shared, business, infra Request classes: 0

If future frontend or shared request models are added under API request packages, they should be included in the same rule unless a separate TODO explicitly narrows the scope.

## Required Class Annotations

Each Request class should have exactly these class-level annotations:

- `@Getter`
- `@Setter`
- `@ApiModel`
- `@JsonInclude(JsonInclude.Include.NON_NULL)`
- `@JsonIgnoreProperties(ignoreUnknown = true)`

Field-level annotations, such as `@ApiModelProperty`, are outside this baseline and should not be checked by the Request class-level rule.

## Current Gaps

`sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/request/PersonalAvatarUploadRequest.java`

- Has `@Getter`
- Has `@Setter`
- Has `@ApiModel`
- Missing `@JsonInclude(JsonInclude.Include.NON_NULL)`
- Missing `@JsonIgnoreProperties(ignoreUnknown = true)`

No Request class currently has extra class-level annotations outside the required set.
