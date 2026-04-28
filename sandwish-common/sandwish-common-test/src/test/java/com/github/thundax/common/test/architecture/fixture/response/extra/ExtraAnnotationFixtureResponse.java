package com.github.thundax.common.test.architecture.fixture.response.extra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@ApiModel(value = "ExtraAnnotationFixtureResponse", description = "extra annotation fixture response")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtraAnnotationFixtureResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
}
