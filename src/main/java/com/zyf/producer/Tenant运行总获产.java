package com.zyf.producer;

import com.zyf.producer.base.公共消费者;
import com.zyf.producer.base.公共运行;
import com.zyf.producer.tables.mysql.MySql运行上下文;
import com.zyf.producer.tables.mysql.department.Dept消费者;
import com.zyf.producer.tables.mysql.employee.Employee消费者;
import com.zyf.producer.tables.mysql.tenant.Tenant消费者;
import com.zyf.producer.tables.mysql.tenant.Tenant生产者;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Tenant运行总获产 extends 公共运行<MySql运行上下文, Tenant生产者> {

    public static void main(String[] args) throws InterruptedException {
        new Tenant运行总获产().执行(args,
                Tenant生产者::new,
                MySql运行上下文::new,
                mode -> {
                    final AtomicInteger 租户计数器 = new AtomicInteger(1);
                    final AtomicInteger 部门计数器 = new AtomicInteger(1);
                    final AtomicInteger 员工计数器 = new AtomicInteger(1);

                    // 并发
                    final Tenant消费者[] Tenant集群1 = 创建消费者数组(3, 租户计数器, Tenant消费者::new);
                    final Tenant消费者[] Tenant集群2 = 创建消费者数组(3, 租户计数器, Tenant消费者::new);

                    final Dept消费者[] Dept集群1 = 创建消费者数组(3, 部门计数器, Dept消费者::new);
                    final Dept消费者[] Dept集群2 = 创建消费者数组(3, 部门计数器, Dept消费者::new);
                    final Dept消费者[] Dept集群3 = 创建消费者数组(3, 部门计数器, Dept消费者::new);
                    final Dept消费者[] Dept集群4 = 创建消费者数组(3, 部门计数器, Dept消费者::new);

                    final Employee消费者[] Employee集群1 = 创建消费者数组(3, 员工计数器, Employee消费者::new);
                    final Employee消费者[] Employee集群2 = 创建消费者数组(3, 员工计数器, Employee消费者::new);
                    final Employee消费者[] Employee集群3 = 创建消费者数组(3, 员工计数器, Employee消费者::new);
                    final Employee消费者[] Employee集群4 = 创建消费者数组(3, 员工计数器, Employee消费者::new);

                    // add几次就重复几次，仅对于Tenant而言的次数
                    final List<公共消费者<MySql运行上下文>[]> Tenant总集群 = new ArrayList<>();
                    Tenant总集群.add(Tenant集群1);
                    Tenant总集群.add(Tenant集群2);

                    // 会均匀分配到 Tenant集群1 和 Tenant集群2 下面挂着
                    // 如： Tenant集群1 -> (串联)Dept集群1 -> (串联)Dept集群2  = 那么Dept会重复2次
                    // 如： Tenant集群2 -> (串联)Dept集群3 -> (串联)Dept集群4  = 那么Dept会重复2次
                    // 如果不是Tenant的倍数，可能会分配不均，但是这也是你个人的策略选择，请注意这点！
                    // 如： Tenant集群1 -> (串联)Dept集群1 -> (串联)Dept集群2 -> (串联)Dept集群3  = 那么Dept会重复3次
                    // 如： Tenant集群2 -> (串联)Dept集群4 -> (串联)Dept集群5  = 那么Dept会重复2次
                    final List<公共消费者<MySql运行上下文>[]> Dept总集群 = new ArrayList<>();
                    Dept总集群.add(Dept集群1);
                    Dept总集群.add(Dept集群2);
                    Dept总集群.add(Dept集群3);
                    Dept总集群.add(Dept集群4);

                    // 会均匀分配到 Dept集群3 和 Dept集群4 下面挂着（因为上面 Dept集群3 和 Dept集群4 是链条最后节点）
                    // 如： Dept集群3 -> (串联)Employee集群1 -> (串联)Employee集群2  = 那么Employee会重复2次
                    // 如： Dept集群4 -> (串联)Employee集群3 -> (串联)Employee集群4  = 那么Employee会重复2次
                    // 如果不是Dept的倍数，可能会分配不均，但是这也是你个人的策略选择，请注意这点！
                    // 如： Dept集群3 -> (串联)Employee集群1 -> (串联)Employee集群2 -> (串联)Employee集群3  = 那么Employee会重复3次
                    // 如： Dept集群4 -> (串联)Employee集群4 -> (串联)Employee集群5  = 那么Employee会重复2次
                    final List<公共消费者<MySql运行上下文>[]> Employee总集群 = new ArrayList<>();
                    Employee总集群.add(Employee集群1);
                    Employee总集群.add(Employee集群2);
                    Employee总集群.add(Employee集群3);
                    Employee总集群.add(Employee集群4);

                    mode.并发消费(Tenant总集群)
                            .并发消费(Dept总集群)
                            .并发消费(Employee总集群);

                });
    }
}
