package com.zyf.producer.entitys.sql.mysql;

import cn.hutool.core.util.IdUtil;
import cn.hutool.db.Entity;
import com.zyf.producer.base.sql.BaseSqlEntity;
import com.zyf.producer.enums.DbType;
import com.zyf.producer.tables.sql.mysql.MySql的Sql运行上下文;
import com.zyf.producer.utils.DataUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Employee extends BaseSqlEntity {

    public final static String TABLE_NAME = "employee";

    private Employee() {
    }

    public static final Employee instance;

    static {
        instance = new EmployeeHolder().getInstance();
    }

    private static class EmployeeHolder {
        private final Employee instance = new Employee();

        public Employee getInstance() {
            return instance;
        }
    }


    @Override
    public DbType getDbType() {
        return DbType.MYSQL;
    }

    @Override
    public String getDataSql() {
        return "select * from employee";
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
        entity.set("age", Integer.parseInt(DataUtil.随机年龄()));
        entity.set("entry_date", DataUtil.随机过去一年内日期时间());
        entity.set("department_id", IdUtil.getSnowflakeNextIdStr());
        entity.set("tenant_id", IdUtil.getSnowflakeNextIdStr());
        return entity;
    }

    @Override
    public String getId(Entity entity) {
        return entity.getStr("id");
    }

    @Override
    public void setRef(MySql的Sql运行上下文 context, Entity entity) {
        final String tenantId = Optional.ofNullable(context.getTenant())
                .map(Tenant.instance::getId)
                .orElseGet(IdUtil::getSnowflakeNextIdStr);
        final String departmentId = Optional.ofNullable(context.getDepartment())
                .map(Department.instance::getId)
                .orElseGet(IdUtil::getSnowflakeNextIdStr);
        entity.set("department_id", departmentId);
        entity.set("tenant_id", tenantId);
    }

    @Override
    public List<Entity> getParents(MySql的Sql运行上下文 context) {
        final Entity tenant = context.getTenant();
        final Entity department = context.getDepartment();
        return Arrays.asList(tenant, department);
    }

    @Override
    public Entity sendContext(MySql的Sql运行上下文 context, Optional<Entity> entity) {
        final Entity employee = entity.orElseGet(this::createNew);
        context.setDepartment(employee);
        return employee;
    }

    @Override
    public Entity takeContext(MySql的Sql运行上下文 context) {
        return context.getDepartment();
    }
}
