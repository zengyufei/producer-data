package com.zyf.producer.entitys;

import cn.hutool.core.util.IdUtil;
import cn.hutool.db.Entity;
import com.zyf.producer.base.BaseSqlEntity;
import com.zyf.producer.enums.DbType;
import com.zyf.producer.tables.mysql.MySql的Sql运行上下文;
import com.zyf.producer.utils.DataUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Department extends BaseSqlEntity {

    public final static String TABLE_NAME = "department";

    private Department() {
    }

    public static final Department instance;

    static {
        instance = new Department.DepartmentHolder().getInstance();
    }

    private static class DepartmentHolder {
        private final Department instance = new Department();

        public Department getInstance() {
            return instance;
        }
    }

    @Override
    public DbType getDbType() {
        return DbType.MYSQL;
    }

    @Override
    public String getDataSql() {
        return "SELECT * FROM department";
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
        entity.set("name", DataUtil.随机部门名称());
        entity.set("create_date", DataUtil.随机过去一年内日期时间());
        entity.set("tenant_id", IdUtil.getSnowflakeNextIdStr());
        return entity;
    }

    @Override
    public String getId(Entity entity) {
        return entity.getStr("id");
    }

    @Override
    public void setRef(MySql的Sql运行上下文 context, Entity entity) {
        final Entity tenant = context.getTenant();
        entity.set("tenant_id", Optional.ofNullable(tenant)
                .map(Tenant.instance::getId)
                .orElseGet(IdUtil::getSnowflakeNextIdStr));
    }

    @Override
    public List<Entity> getParents(MySql的Sql运行上下文 context) {
        return Arrays.asList(context.getTenant());
    }

    @Override
    public Entity sendContext(MySql的Sql运行上下文 context, Optional<Entity> entity) {
        final Entity department = entity.orElseGet(this::createNew);
        context.setDepartment(department);
        return department;
    }

    @Override
    public Entity takeContext(MySql的Sql运行上下文 context) {
        return context.getDepartment();
    }
}
