package com.zyf.producer.tables.bean.mysql.employee;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.db.Db;
import com.zyf.producer.base.bean.公共Bean消费者;
import com.zyf.producer.entitys.bean.mysql.DepartmentPo;
import com.zyf.producer.entitys.bean.mysql.EmployeePo;
import com.zyf.producer.entitys.bean.mysql.TenantPo;
import com.zyf.producer.enums.DbType;
import com.zyf.producer.tables.bean.mysql.MySql的Bean运行上下文;
import com.zyf.producer.utils.DataUtil;
import com.zyf.producer.utils.IdWorker;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
public class EmployeeBean消费者 extends 公共Bean消费者<MySql的Bean运行上下文> {

    @Override
    public Db getDb() {
        return Db.use(DbType.MYSQL.getKey());
    }

    protected boolean 消费数据(MySql的Bean运行上下文 context) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("{} 执行消费, {}", this, context);
        }

        final TenantPo tenantPo = context.getTenantPo();
        final DepartmentPo departmentPo = context.getDepartmentPo();
        final EmployeePo employeePo = context.getEmployeePo();

        final String tenantId = Optional.ofNullable(tenantPo).map(TenantPo::getId).orElseGet(IdUtil::getSnowflakeNextIdStr);
        final String deptId = Optional.ofNullable(departmentPo).map(DepartmentPo::getId).orElseGet(IdUtil::getSnowflakeNextIdStr);

        EmployeePo newObj = null;
        // Employee运行
        if (employeePo != null) {
            newObj = BeanUtil.copyProperties(employeePo, EmployeePo.class);
            setData(newObj, tenantId, deptId);
        }
        // EmployeeBean运行获产
        else if (departmentPo != null) {
            newObj = new EmployeePo();
            setData(newObj, tenantId, deptId);
        }
        // TenantBean运行单表生产 or DeptBean运行获产
        else if (tenantPo != null) {
            newObj = new EmployeePo();
            setData(newObj, tenantId, deptId);
        }

        if (newObj != null) {
            // 数据库写入
            this.写入数据库(newObj);
            return true;
        }
        return false;
    }

    private static void setData(EmployeePo newObj, String tenantId, String deptId) {
        newObj.setId(IdWorker.getNextIdStr());
        newObj.setName(DataUtil.随机中文姓名());
        newObj.setEntryDate(LocalDateTime.now());
        newObj.setAge(Integer.parseInt(DataUtil.随机年龄()));
        newObj.setDepartmentId(deptId);
        newObj.setTenantId(tenantId);
    }
}
