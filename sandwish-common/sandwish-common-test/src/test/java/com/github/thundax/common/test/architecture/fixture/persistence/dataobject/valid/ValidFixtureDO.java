package com.github.thundax.common.test.architecture.fixture.persistence.dataobject.valid;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("valid_fixture")
public class ValidFixtureDO {

    private String id;
}
