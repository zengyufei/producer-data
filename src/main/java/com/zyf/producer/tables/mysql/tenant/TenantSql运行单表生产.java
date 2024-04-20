package com.zyf.producer.tables.mysql.tenant;

import com.zyf.producer.base.公共Sql消费者;
import com.zyf.producer.base.公共Sql运行;
import com.zyf.producer.entitys.Tenant;
import com.zyf.producer.tables.mysql.MySql的Sql运行上下文;
import com.zyf.producer.tables.mysql.通用Sql生产者;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TenantSql运行单表生产 extends 公共Sql运行<MySql的Sql运行上下文, 通用Sql生产者> {

    public static void main(String[] args) throws InterruptedException {
        new TenantSql运行单表生产().执行(args,
                ()->new 通用Sql生产者(Tenant.instance),
                MySql的Sql运行上下文::new,
                mode -> {
                    final AtomicInteger 租户计数器 = new AtomicInteger(1);

                    final TenantSql消费者[] Tenant集群1 = 创建消费者数组(3, 租户计数器, TenantSql消费者::new);

                    final List<公共Sql消费者<MySql的Sql运行上下文>[]> Tenant总集群 = new ArrayList<>();
                    Tenant总集群.add(Tenant集群1);

                    mode.并发消费(Tenant总集群)
                    ;
                });
    }
}
