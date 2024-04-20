package com.zyf.producer.tables.bean.mysql.tenant;

import cn.hutool.db.Db;
import com.zyf.producer.base.bean.公共Bean获产者;
import com.zyf.producer.entitys.bean.mysql.TenantPo;
import com.zyf.producer.enums.DbType;
import com.zyf.producer.tables.bean.mysql.MySql的Bean运行上下文;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class TenantBean获产者 extends 公共Bean获产者<MySql的Bean运行上下文> {

    /**
     * 查询速率
     */
    private static final int RATE = 100;

    public TenantBean获产者() {
        try {
            // 持续查询租户数据，直到生产数据量>=命令行参数total值
            流式查询数据(
                    Db.use(DbType.MYSQL.getKey()),
                    "SELECT * FROM tenant",
                    RATE,
                    TenantPo.class,
                    tenantPo -> {
                        final MySql的Bean运行上下文 context = new MySql的Bean运行上下文();
                        context.setTenantPo(tenantPo);
                        return context;
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Consumer<MySql的Bean运行上下文> 推送前设置值(MySql的Bean运行上下文 context) throws Exception {
        return temp -> {
            final TenantPo sqlTenantPo = temp.getTenantPo();
            context.setTenantPo(sqlTenantPo);
        };
    }

}
