package com.zyf.producer.tables.sql.mysql.department;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import com.zyf.producer.base.sql.公共Sql消费者;
import com.zyf.producer.entitys.sql.mysql.Department;
import com.zyf.producer.enums.DbType;
import com.zyf.producer.tables.sql.mysql.MySql的Sql运行上下文;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DeptSql消费者 extends 公共Sql消费者<MySql的Sql运行上下文> {

    public static final Department DEPARTMENT = Department.instance;

    @Override
    public Db getDb() {
        return Db.use(DbType.MYSQL.getKey());
    }

    protected boolean 消费数据(MySql的Sql运行上下文 context) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("{} 执行消费, {}", this, context);
        }
        final Entity department = context.getDepartment();
        List<Entity> list = DEPARTMENT.getParents(context);

        Entity newObj = null;
        if (department != null || CollUtil.isNotEmpty(list)) {
            newObj = DEPARTMENT.createNew();
            DEPARTMENT.setRef(context, newObj);
        }

        if (newObj != null) {
            // 数据库写入
            this.写入数据库(newObj);
            context.setDepartment(newObj);
            return true;
        }
        return false;
    }
}
