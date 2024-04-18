package com.zyf.producer.tables.mysql.tenant;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.db.Db;
import com.zyf.producer.base.公共消费者;
import com.zyf.producer.entitys.mysql.TenantPo;
import com.zyf.producer.enums.DbType;
import com.zyf.producer.tables.mysql.MySql运行上下文;
import com.zyf.producer.utils.DataUtil;
import com.zyf.producer.utils.IdWorker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Tenant消费者 extends 公共消费者<MySql运行上下文> {

    @Override
    public Db getDb() {
        return Db.use(DbType.MYSQL.getKey());
    }

    @Override
    protected boolean 消费数据(MySql运行上下文 context) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("{} 执行消费, {}", this, context);
        }
        final TenantPo tenantPo = context.getTenantPo();
        if (tenantPo != null) {
            final String nextIdStr = IdWorker.getNextIdStr();
            final TenantPo newObj = BeanUtil.copyProperties(tenantPo, TenantPo.class);
            newObj.setId(nextIdStr);
            newObj.setName(DataUtil.随机省份());
            this.写入数据库(newObj);

            context.setTenantPo(newObj);
            return true;
        }
        return false;
    }
}
