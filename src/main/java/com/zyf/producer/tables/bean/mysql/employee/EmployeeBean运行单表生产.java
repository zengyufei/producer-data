package com.zyf.producer.tables.bean.mysql.employee;

import com.zyf.producer.base.bean.公共Bean消费者;
import com.zyf.producer.base.bean.公共Bean运行;
import com.zyf.producer.tables.bean.mysql.MySql的Bean运行上下文;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class EmployeeBean运行单表生产 extends 公共Bean运行<MySql的Bean运行上下文, EmployeeBean生产者> {

    public static void main(String[] args) throws InterruptedException {
        new EmployeeBean运行单表生产().执行(args,
                EmployeeBean生产者::new,
                MySql的Bean运行上下文::new,
                mode -> {
                    final AtomicInteger 员工计数器 = new AtomicInteger(1);

                    // 并发数组，创建消费者数组抢占竞争式消费
                    final EmployeeBean消费者[] Employee集群1 = 创建消费者数组(3, 员工计数器, EmployeeBean消费者::new);

                    // add几次就重复几次，仅对于Employee而言的次数
                    final List<公共Bean消费者<MySql的Bean运行上下文>[]> Employee总集群 = new ArrayList<>();
                    Employee总集群.add(Employee集群1);

                    mode.并发消费(Employee总集群);
                });
    }
}
