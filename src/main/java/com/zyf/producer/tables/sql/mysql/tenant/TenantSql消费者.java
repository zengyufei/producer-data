package com.zyf.producer.tables.sql.mysql.tenant;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import com.zyf.producer.base.sql.公共Sql消费者;
import com.zyf.producer.entitys.sql.mysql.Tenant;
import com.zyf.producer.enums.DbType;
import com.zyf.producer.tables.sql.mysql.MySql的Sql运行上下文;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TenantSql消费者 extends 公共Sql消费者<MySql的Sql运行上下文> {

    public static final Tenant TENANT = Tenant.instance;

    @Override
    public Db getDb() {
        return Db.use(DbType.MYSQL.getKey());
    }

    @Override
    protected boolean 消费数据(MySql的Sql运行上下文 context) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("{} 执行消费, {}", this, context);
        }

        final Entity tenant = context.getTenant();
        List<Entity> list = TENANT.getParents(context);

        Entity newObj = null;
        if (tenant != null || CollUtil.isNotEmpty(list)) {
            newObj = TENANT.createNew();
            TENANT.setRef(context, newObj);
        }

        if (newObj != null) {
            // 数据库写入
            this.写入数据库(newObj);
            context.setTenant(newObj);
            return true;
        }
        return false;
    }
}
