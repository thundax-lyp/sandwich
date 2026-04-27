package com.github.thundax.common.test.architecture.fixture.request.valid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "ValidFixtureRequest", description = "valid fixture request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidFixtureRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
}
