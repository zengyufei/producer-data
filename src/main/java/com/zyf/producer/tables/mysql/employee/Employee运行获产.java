package com.zyf.producer.tables.mysql.employee;

import com.zyf.producer.base.公共消费者;
import com.zyf.producer.base.公共运行;
import com.zyf.producer.tables.mysql.MySql运行上下文;
import com.zyf.producer.tables.mysql.department.Dept消费者;
import com.zyf.producer.tables.mysql.department.Dept获产者;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Employee运行获产 extends 公共运行<MySql运行上下文, Dept获产者> {

    public static void main(String[] args) throws InterruptedException {
        new Employee运行获产().执行(args,
                Dept获产者::new,
                MySql运行上下文::new,
                mode -> {
                    final AtomicInteger deptConsumerCounter = new AtomicInteger(1);

                    // 并发
                    final Dept消费者[] Dept集群1 = 创建消费者数组(3, deptConsumerCounter, Dept消费者::new);
                    final Employee消费者[] Employee集群1 = 创建消费者数组(3, deptConsumerCounter, Employee消费者::new);
                    final Employee消费者[] Employee集群2 = 创建消费者数组(3, deptConsumerCounter, Employee消费者::new);

                    // 重复 -- add5次就重复5次，必须注意前一位大哥是x次，那么就死5*x次
                    final List<公共消费者<MySql运行上下文>[]> Dept总集群 = new ArrayList<>();
                    Dept总集群.add(Dept集群1); // 如果Dept总集群只有一个Dept集群1 可省略 mode.并发重复消费(Dept总集群)

                    // 重复 -- add5次就重复5次，必须注意前一位大哥是x次，那么就死5*x次
                    final List<公共消费者<MySql运行上下文>[]> Employee总集群 = new ArrayList<>();
                    Employee总集群.add(Employee集群1);
                    Employee总集群.add(Employee集群2);
                    // 部门消费计数器最终值会打印 Dept总集群.size() * Employee总集群.size()

                    mode.并发消费(Dept总集群)
                            .并发消费(Employee总集群);
                    // 如果Dept总集群只有一个Dept集群1 可省略 mode.并发重复消费(Dept总集群)
                    // mode.并发重复消费(Employee总集群)
                });
    }
}
