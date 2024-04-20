package com.zyf.producer.tables.bean.mysql.tenant;

import com.zyf.producer.base.bean.公共Bean生产者;
import com.zyf.producer.base.状态;
import com.zyf.producer.entitys.bean.mysql.TenantPo;
import com.zyf.producer.tables.bean.mysql.MySql的Bean运行上下文;
import com.zyf.producer.utils.DataUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantBean生产者 extends 公共Bean生产者<MySql的Bean运行上下文> {

    @Override
    protected 状态 数据设置属性(int seqNo, MySql的Bean运行上下文 context) throws Exception {
        final TenantPo newObj = new TenantPo();
        newObj.setName(DataUtil.随机省份());
        context.setTenantPo(newObj);
        return 状态.push;
    }

}
