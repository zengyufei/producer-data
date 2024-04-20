package com.zyf.producer.tables.bean.mysql.employee;

import com.zyf.producer.base.bean.公共Bean消费者;
import com.zyf.producer.base.bean.公共Bean运行;
import com.zyf.producer.tables.bean.mysql.MySql的Bean运行上下文;
import com.zyf.producer.tables.bean.mysql.department.DeptBean消费者;
import com.zyf.producer.tables.bean.mysql.department.DeptBean获产者;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class EmployeeBean运行获产 extends 公共Bean运行<MySql的Bean运行上下文, DeptBean获产者> {

    public static void main(String[] args) throws InterruptedException {
        new EmployeeBean运行获产().执行(args,
                DeptBean获产者::new,
                MySql的Bean运行上下文::new,
                mode -> {
                    final AtomicInteger deptConsumerCounter = new AtomicInteger(1);

                    // 并发数组，创建消费者数组抢占竞争式消费
                    final DeptBean消费者[] Dept集群1 = 创建消费者数组(3, deptConsumerCounter, DeptBean消费者::new);
                    final EmployeeBean消费者[] Employee集群1 = 创建消费者数组(3, deptConsumerCounter, EmployeeBean消费者::new);
                    final EmployeeBean消费者[] Employee集群2 = 创建消费者数组(3, deptConsumerCounter, EmployeeBean消费者::new);

                    // add几次就重复几次，仅对于Dept而言的次数
                    final List<公共Bean消费者<MySql的Bean运行上下文>[]> Dept总集群 = new ArrayList<>();
                    Dept总集群.add(Dept集群1); // 如果Dept总集群只有一个Dept集群1 可省略 mode.并发重复消费(Dept总集群)

                    // 会均匀分配到 Dept集群3 和 Dept集群4 下面挂着（因为上面 Dept集群3 和 Dept集群4 是链条最后节点）
                    // 如： Dept集群3 -> (串联)Employee集群1 -> (串联)Employee集群2  = 那么Employee会重复2次
                    // 如： Dept集群4 -> (串联)Employee集群3 -> (串联)Employee集群4  = 那么Employee会重复2次
                    // 如果不是Dept的倍数，可能会分配不均，但是这也是你个人的策略选择，请注意这点！
                    // 如： Dept集群3 -> (串联)Employee集群1 -> (串联)Employee集群2 -> (串联)Employee集群3  = 那么Employee会重复3次
                    // 如： Dept集群4 -> (串联)Employee集群4 -> (串联)Employee集群5  = 那么Employee会重复2次
                    final List<公共Bean消费者<MySql的Bean运行上下文>[]> Employee总集群 = new ArrayList<>();
                    Employee总集群.add(Employee集群1);
                    Employee总集群.add(Employee集群2);

                    mode.并发消费(Dept总集群)
                            .并发消费(Employee总集群);
                });
    }
}
