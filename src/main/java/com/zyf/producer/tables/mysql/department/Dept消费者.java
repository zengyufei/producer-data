package com.zyf.producer.tables.mysql.department;

import cn.hutool.core.util.IdUtil;
import cn.hutool.db.Db;
import com.zyf.producer.base.公共消费者;
import com.zyf.producer.entitys.mysql.DepartmentPo;
import com.zyf.producer.entitys.mysql.TenantPo;
import com.zyf.producer.enums.DbType;
import com.zyf.producer.tables.mysql.MySql运行上下文;
import com.zyf.producer.utils.DataUtil;
import com.zyf.producer.utils.IdWorker;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.StringJoiner;

@Slf4j
public class Dept消费者 extends 公共消费者<MySql运行上下文> {

    @Override
    public Db getDb() {
        return Db.use(DbType.MYSQL.getKey());
    }

    protected boolean 消费数据(MySql运行上下文 context) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("{} 执行消费, {}", this, context);
        }
        StringJoiner lj = new StringJoiner(" -> ");

        final TenantPo tenantPo = context.getTenantPo();
        final DepartmentPo departmentPo = context.getDepartmentPo();

        final String tenantId = Optional.ofNullable(tenantPo).map(TenantPo::getId).orElseGet(IdUtil::getSnowflakeNextIdStr);

        DepartmentPo newObj = null;
        // Dept运行单表生产
        if (departmentPo != null) {
            lj.add("departmentPo != null");
            newObj = new DepartmentPo();
            newObj.setId(IdWorker.getNextIdStr());
            newObj.setCreateDate(departmentPo.getCreateDate());
            newObj.setName(departmentPo.getName());
            newObj.setTenantId(tenantId);
        }
        // Tenant运行单表生产 or Dept运行获产
        else if (tenantPo != null) {
            lj.add("tenantPo != null");
            newObj = new DepartmentPo();
            setData(newObj, tenantId);
        }
        if (newObj != null) {
            // 数据库写入
            try {
                this.写入数据库(newObj);
            } catch (Exception e) {
                log.error("{} {}", lj, e.getMessage());
                throw new RuntimeException(e);
            }
            context.setDepartmentPo(newObj);
            return true;
        }
        return false;
    }

    private void setData(DepartmentPo newObj, String tenantId) {
        newObj.setId(IdWorker.getNextIdStr());
        newObj.setCreateDate(LocalDateTime.now());
        newObj.setName(DataUtil.随机部门名称());
        newObj.setTenantId(tenantId);
    }
}
