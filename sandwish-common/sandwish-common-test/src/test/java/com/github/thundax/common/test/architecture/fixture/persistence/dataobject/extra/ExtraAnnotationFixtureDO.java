package com.github.thundax.common.test.architecture.fixture.persistence.dataobject.extra;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("extra_annotation_fixture")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtraAnnotationFixtureDO {

    private String id;
}
