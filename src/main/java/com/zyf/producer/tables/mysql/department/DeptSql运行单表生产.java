package com.zyf.producer.tables.mysql.department;

import com.zyf.producer.base.公共Sql消费者;
import com.zyf.producer.base.公共Sql运行;
import com.zyf.producer.entitys.Department;
import com.zyf.producer.entitys.Tenant;
import com.zyf.producer.tables.mysql.MySql的Sql运行上下文;
import com.zyf.producer.tables.mysql.通用Sql生产者;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DeptSql运行单表生产 extends 公共Sql运行<MySql的Sql运行上下文, 通用Sql生产者> {

    public static void main(String[] args) throws InterruptedException {
        new DeptSql运行单表生产().执行(args,
                ()->new 通用Sql生产者(Department.instance),
                MySql的Sql运行上下文::new,
                mode -> {
                    final AtomicInteger deptConsumerCounter = new AtomicInteger(1);

                    final DeptSql消费者[] Dept集群1 = 创建消费者数组(3, deptConsumerCounter, DeptSql消费者::new);

                    final List<公共Sql消费者<MySql的Sql运行上下文>[]> Dept总集群 = new ArrayList<>();
                    Dept总集群.add(Dept集群1);

                    mode.并发消费(Dept总集群);
                });
    }
}
