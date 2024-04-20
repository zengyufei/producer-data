package com.zyf.producer.tables.sql.mysql.department;

import com.zyf.producer.base.sql.公共Sql消费者;
import com.zyf.producer.base.sql.公共Sql运行;
import com.zyf.producer.base.sql.通用公共Sql获产者;
import com.zyf.producer.entitys.sql.mysql.Tenant;
import com.zyf.producer.tables.sql.mysql.MySql的Sql运行上下文;
import com.zyf.producer.tables.sql.mysql.tenant.TenantSql消费者;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DeptSql运行获产 extends 公共Sql运行<MySql的Sql运行上下文, 通用公共Sql获产者> {

    public static void main(String[] args) throws InterruptedException {
        new DeptSql运行获产().执行(args,
                () -> new 通用公共Sql获产者(Tenant.instance),
                MySql的Sql运行上下文::new,
                mode -> {
                    final AtomicInteger 租户计数器 = new AtomicInteger(1);
                    final AtomicInteger 部门消费计数器 = new AtomicInteger(1);

                    // 并发数组，创建消费者数组抢占竞争式消费
                    final TenantSql消费者[] Tenant集群1 = 创建消费者数组(3, 租户计数器, TenantSql消费者::new);

                    final DeptSql消费者[] Dept集群1 = 创建消费者数组(3, 部门消费计数器, DeptSql消费者::new);
                    final DeptSql消费者[] Dept集群2 = 创建消费者数组(3, 部门消费计数器, DeptSql消费者::new);

                    // add几次就重复几次，仅对于Tenant而言的次数
                    final List<公共Sql消费者<MySql的Sql运行上下文>[]> Tenant总集群 = new ArrayList<>();
                    Tenant总集群.add(Tenant集群1);

                    // 会均匀分配到 Tenant集群1 和 Tenant集群2 下面挂着
                    // 如： Tenant集群1 -> (串联)Dept集群1 -> (串联)Dept集群2  = 那么Dept会重复2次
                    // 如： Tenant集群2 -> (串联)Dept集群3 -> (串联)Dept集群4  = 那么Dept会重复2次
                    // 如果不是Tenant的倍数，可能会分配不均，但是这也是你个人的策略选择，请注意这点！
                    // 如： Tenant集群1 -> (串联)Dept集群1 -> (串联)Dept集群2 -> (串联)Dept集群3  = 那么Dept会重复3次
                    // 如： Tenant集群2 -> (串联)Dept集群4 -> (串联)Dept集群5  = 那么Dept会重复2次
                    final List<公共Sql消费者<MySql的Sql运行上下文>[]> Dept总集群 = new ArrayList<>();
                    Dept总集群.add(Dept集群1);
                    Dept总集群.add(Dept集群2);

                    mode.并发消费(Tenant总集群)
                            .并发消费(Dept总集群)
                    ;
                });
    }
}
