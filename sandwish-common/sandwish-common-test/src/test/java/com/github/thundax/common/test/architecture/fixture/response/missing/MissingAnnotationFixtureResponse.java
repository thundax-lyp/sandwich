package com.github.thundax.common.test.architecture.fixture.response.missing;

import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "MissingAnnotationFixtureResponse", description = "missing annotation fixture response")
public class MissingAnnotationFixtureResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
}
