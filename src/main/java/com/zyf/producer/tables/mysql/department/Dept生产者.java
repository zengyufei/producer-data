package com.zyf.producer.tables.mysql.department;

import cn.hutool.core.util.IdUtil;
import com.zyf.producer.base.公共生产者;
import com.zyf.producer.base.状态;
import com.zyf.producer.entitys.mysql.DepartmentPo;
import com.zyf.producer.entitys.mysql.TenantPo;
import com.zyf.producer.tables.mysql.MySql运行上下文;
import com.zyf.producer.utils.DataUtil;
import com.zyf.producer.utils.IdWorker;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
public class Dept生产者 extends 公共生产者<MySql运行上下文> {

    @Override
    protected 状态 数据设置属性(int seqNo, MySql运行上下文 context) throws Exception {
        if (log.isDebugEnabled()) {
            log.info("{} 执行生产 {}", this, context);
        }

        final DepartmentPo newObj = new DepartmentPo();
        newObj.setId(IdWorker.getNextIdStr());
        newObj.setName(DataUtil.随机部门名称());
        newObj.setCreateDate(LocalDateTime.now());
        newObj.setTenantId(IdWorker.getNextIdStr());

        context.setDepartmentPo(newObj);
        return 状态.push;
    }

}
