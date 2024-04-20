package com.zyf.producer.tables.bean.mysql.department;

import cn.hutool.db.Db;
import com.zyf.producer.base.bean.公共Bean获产者;
import com.zyf.producer.entitys.bean.mysql.DepartmentPo;
import com.zyf.producer.enums.DbType;
import com.zyf.producer.tables.bean.mysql.MySql的Bean运行上下文;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class DeptBean获产者 extends 公共Bean获产者<MySql的Bean运行上下文> {

    /**
     * 查询速率
     */
    private static final int RATE = 100;

    public DeptBean获产者() {
        try {
            // 持续查询租户数据，直到生产数据量>=命令行参数total值
            流式查询数据(
                    Db.use(DbType.MYSQL.getKey()),
                    "SELECT * FROM department",
                    RATE,
                    DepartmentPo.class,
                    departmentPo -> {
                        final MySql的Bean运行上下文 context = new MySql的Bean运行上下文();
                        context.setDepartmentPo(departmentPo);
                        return context;
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Consumer<MySql的Bean运行上下文> 推送前设置值(MySql的Bean运行上下文 context) throws Exception {
        return temp -> {
            final DepartmentPo departmentPo = temp.getDepartmentPo();
            context.setDepartmentPo(departmentPo);
        };
    }

}
