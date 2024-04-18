package com.zyf.producer.tables.mysql.department;

import cn.hutool.db.Db;
import com.zyf.producer.base.Sql生产者;
import com.zyf.producer.entitys.mysql.DepartmentPo;
import com.zyf.producer.enums.DbType;
import com.zyf.producer.tables.mysql.MySql运行上下文;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class Dept获产者 extends Sql生产者<MySql运行上下文> {

    /**
     * 查询速率
     */
    private static final int RATE = 100;

    public Dept获产者() {
        try {
            // 持续查询租户数据，直到生产数据量>=命令行参数total值
            流式查询数据(
                    Db.use(DbType.MYSQL.getKey()),
                    "SELECT * FROM department",
                    RATE,
                    DepartmentPo.class,
                    departmentPo -> {
                        final MySql运行上下文 context = new MySql运行上下文();
                        context.setDepartmentPo(departmentPo);
                        return context;
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Consumer<MySql运行上下文> 推送前设置值(MySql运行上下文 context) throws Exception {
        return temp -> {
            final DepartmentPo departmentPo = temp.getDepartmentPo();
            context.setDepartmentPo(departmentPo);
        };
    }

}
