package com.zyf.producer.tables.mysql.tenant;

import com.zyf.producer.base.公共消费者;
import com.zyf.producer.base.公共运行;
import com.zyf.producer.tables.mysql.MySql运行上下文;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Tenant运行单表生产 extends 公共运行<MySql运行上下文, Tenant生产者> {

    public static void main(String[] args) throws InterruptedException {
        new Tenant运行单表生产().执行(args,
                Tenant生产者::new,
                MySql运行上下文::new,
                mode -> {
                    final AtomicInteger 租户计数器 = new AtomicInteger(1);

                    final Tenant消费者[] Tenant集群1 = 创建消费者数组(3, 租户计数器, Tenant消费者::new);

                    // 重复 -- add5次就重复5次，必须注意前一位大哥是x次，那么就死5*x次
                    final List<公共消费者<MySql运行上下文>[]> Tenant总集群 = new ArrayList<>();
                    Tenant总集群.add(Tenant集群1);

                    // 先并发处理tenant，然后生成一对多dept数据
                    mode.并发消费(Tenant总集群)
                    ;
                });
    }
}
