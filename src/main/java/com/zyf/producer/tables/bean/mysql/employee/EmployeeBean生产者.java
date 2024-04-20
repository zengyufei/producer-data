package com.zyf.producer.tables.bean.mysql.employee;

import com.zyf.producer.base.bean.公共Bean生产者;
import com.zyf.producer.base.状态;
import com.zyf.producer.entitys.bean.mysql.EmployeePo;
import com.zyf.producer.tables.bean.mysql.MySql的Bean运行上下文;
import com.zyf.producer.utils.DataUtil;
import com.zyf.producer.utils.IdWorker;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class EmployeeBean生产者 extends 公共Bean生产者<MySql的Bean运行上下文> {

    @Override
    protected 状态 数据设置属性(int seqNo, MySql的Bean运行上下文 context) throws Exception {
        if (log.isDebugEnabled()) {
            log.info("{} 执行生产 {}", this, context);
        }

        final EmployeePo newObj = new EmployeePo();
        newObj.setId(IdWorker.getNextIdStr());
        newObj.setName(DataUtil.随机中文姓名());
        newObj.setEntryDate(LocalDateTime.now());
        newObj.setAge(Integer.parseInt(DataUtil.随机年龄()));
        newObj.setDepartmentId(IdWorker.getNextIdStr());
        newObj.setTenantId(IdWorker.getNextIdStr());
        return 状态.push;
    }

}
