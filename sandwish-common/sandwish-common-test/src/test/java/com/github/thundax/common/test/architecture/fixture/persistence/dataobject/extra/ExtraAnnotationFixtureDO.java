package com.github.thundax.common.test.architecture.fixture.persistence.dataobject.extra;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Deprecated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("extra_annotation_fixture")
public class ExtraAnnotationFixtureDO {

    private String id;
}
