package com.zyf.producer.entitys.sql.mysql;

import cn.hutool.core.util.IdUtil;
import cn.hutool.db.Entity;
import com.zyf.producer.base.sql.BaseSqlEntity;
import com.zyf.producer.enums.DbType;
import com.zyf.producer.tables.sql.mysql.MySql的Sql运行上下文;
import com.zyf.producer.utils.DataUtil;

import java.util.List;
import java.util.Optional;

public class Tenant extends BaseSqlEntity {

    private Tenant() {
    }

    public static final Tenant instance;

    static {
        instance = new Tenant.TenantHolder().getInstance();
    }

    private static class TenantHolder {
        private final Tenant instance = new Tenant();

        public Tenant getInstance() {
            return instance;
        }
    }

    public final static String TABLE_NAME = "tenant";

    @Override
    public DbType getDbType() {
        return DbType.MYSQL;
    }

    @Override
    public String getDataSql() {
        return "select * from tenant";
    }

    @Override
    public Entity setNewId(Entity entity) {
        entity.set("id", IdUtil.getSnowflakeNextIdStr());
        return entity;
    }

    @Override
    public Entity createNew() {
        final Entity entity = Entity.create(TABLE_NAME);
        entity.set("id", IdUtil.getSnowflakeNextIdStr());
        entity.set("name", DataUtil.随机省份());
        return entity;
    }

    @Override
    public String getId(Entity entity) {
        return entity.getStr("id");
    }

    @Override
    public void setRef(MySql的Sql运行上下文 context, Entity entity) {

    }

    @Override
    public List<Entity> getParents(MySql的Sql运行上下文 context) {
        return null;
    }

    @Override
    public Entity sendContext(MySql的Sql运行上下文 context, Optional<Entity> entity) {
        final Entity tenant = entity.orElseGet(this::createNew);
        context.setTenant(tenant);
        return tenant;
    }

    @Override
    public Entity takeContext(MySql的Sql运行上下文 context) {
        return context.getTenant();
    }

}
