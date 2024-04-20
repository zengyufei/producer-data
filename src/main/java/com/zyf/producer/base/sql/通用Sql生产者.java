package com.zyf.producer.base.sql;

import com.zyf.producer.base.状态;
import com.zyf.producer.tables.sql.mysql.MySql的Sql运行上下文;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class 通用Sql生产者 extends 公共Sql生产者<MySql的Sql运行上下文> {

    private BaseSqlEntity baseSqlEntity;

    public 通用Sql生产者(BaseSqlEntity baseSqlEntity) {
        this.baseSqlEntity = baseSqlEntity;
    }

    @Override
    protected 状态 数据设置属性(int seqNo, MySql的Sql运行上下文 context) throws Exception {
        if (log.isDebugEnabled()) {
            log.info("{} 执行生产 {}", this, context);
        }
        baseSqlEntity.sendContext(context);
        return 状态.push;
    }

}
