package com.zyf.producer.tables.mysql;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import com.zyf.producer.base.BaseSqlEntity;
import com.zyf.producer.base.Sql获产者;
import com.zyf.producer.enums.DbType;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
public class 通用Sql获产者 extends Sql获产者<MySql的Sql运行上下文> {

    /**
     * 查询速率
     */
    private static final int RATE = 100;

    private BaseSqlEntity baseSqlEntity;

    public 通用Sql获产者(BaseSqlEntity baseSqlEntity) {
        this.baseSqlEntity = baseSqlEntity;
        queryData();
    }

    private void queryData() {
        try {
            // 持续查询租户数据，直到生产数据量>=命令行参数total值
            流式查询数据(
                    Db.use(baseSqlEntity.getDbType().getKey()),
                    baseSqlEntity.getDataSql(),
                    RATE,
                    entity -> {
                        final MySql的Sql运行上下文 context = new MySql的Sql运行上下文();
                        baseSqlEntity.sendContext(context, Optional.ofNullable(entity));
                        return context;
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Consumer<MySql的Sql运行上下文> 推送前设置值(MySql的Sql运行上下文 context) throws Exception {
        return tempContext -> {
            final Entity entity = baseSqlEntity.takeContext(context);
            baseSqlEntity.sendContext(context, Optional.ofNullable(entity));
        };
    }

}
