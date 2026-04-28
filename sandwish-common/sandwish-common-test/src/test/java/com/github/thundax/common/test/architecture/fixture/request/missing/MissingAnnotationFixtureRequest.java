package com.github.thundax.common.test.architecture.fixture.request.missing;

import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "MissingAnnotationFixtureRequest", description = "missing annotation fixture request")
public class MissingAnnotationFixtureRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
}
