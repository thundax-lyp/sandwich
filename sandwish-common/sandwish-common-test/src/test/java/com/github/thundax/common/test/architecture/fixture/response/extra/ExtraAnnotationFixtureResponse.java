package com.github.thundax.common.test.architecture.fixture.response.extra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Deprecated
@ApiModel(value = "ExtraAnnotationFixtureResponse", description = "extra annotation fixture response")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtraAnnotationFixtureResponse implements Serializable {

    private String id;
}
