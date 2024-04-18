package com.zyf.producer.tables.mysql.department;

import com.zyf.producer.base.公共消费者;
import com.zyf.producer.base.公共运行;
import com.zyf.producer.tables.mysql.MySql运行上下文;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Dept运行单表生产 extends 公共运行<MySql运行上下文, Dept生产者> {

    public static void main(String[] args) throws InterruptedException {
        new Dept运行单表生产().执行(args,
                Dept生产者::new,
                MySql运行上下文::new,
                mode -> {
                    final AtomicInteger deptConsumerCounter = new AtomicInteger(1);

                    // 并发
                    final Dept消费者[] Dept集群1 = 创建消费者数组(3, deptConsumerCounter, Dept消费者::new);

                    // 重复 -- add5次就重复5次，必须注意前一位大哥是x次，那么就死5*x次
                    final List<公共消费者<MySql运行上下文>[]> Dept总集群 = new ArrayList<>();
                    Dept总集群.add(Dept集群1);

                    mode.并发消费(Dept总集群);
                });
    }
}
