package com.zyf.producer.tables.sql.mysql.employee;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import com.zyf.producer.base.sql.公共Sql消费者;
import com.zyf.producer.entitys.sql.mysql.Employee;
import com.zyf.producer.tables.sql.mysql.MySql的Sql运行上下文;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public class EmployeeSql消费者 extends 公共Sql消费者<MySql的Sql运行上下文> {

    public static final Employee EMPLOYEE = Employee.instance;

    @Override
    public Db getDb() {
        return Db.use(EMPLOYEE.getDbType().getKey());
    }

    protected boolean 消费数据(MySql的Sql运行上下文 context) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("{} 执行消费, {}", this, context);
        }

        final Entity oldObj = context.getEmployee();
        List<Entity> list = EMPLOYEE.getParents(context);

        Entity newObj = null;
        if (oldObj != null || CollUtil.isNotEmpty(list)) {
            newObj = EMPLOYEE.createNew();
            EMPLOYEE.setRef(context, newObj);
        }

        if (newObj != null) {
            // 数据库写入
            this.写入数据库(newObj);
            EMPLOYEE.sendContext(context, Optional.of(newObj));
            return true;
        }
        return false;
    }
}
