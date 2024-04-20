package com.zyf.producer.tables.mysql;

import cn.hutool.db.Entity;
import com.zyf.producer.base.BaseSqlContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MySql的Sql运行上下文 extends BaseSqlContext {

    Entity tenant;
    Entity department;
    Entity employee;

}
