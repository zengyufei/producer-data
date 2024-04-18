package com.zyf.producer.tables.mysql.tenant;

import cn.hutool.core.util.StrUtil;
import com.zyf.producer.base.公共生产者;
import com.zyf.producer.base.状态;
import com.zyf.producer.entitys.mysql.TenantPo;
import com.zyf.producer.tables.mysql.MySql运行上下文;
import com.zyf.producer.utils.CommonUtil;
import com.zyf.producer.utils.DataUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Tenant生产者 extends 公共生产者<MySql运行上下文> {

    @Override
    protected 状态 数据设置属性(int seqNo, MySql运行上下文 context) throws Exception {
        final TenantPo newObj = new TenantPo();
        newObj.setName(DataUtil.随机省份());
        context.setTenantPo(newObj);
        return 状态.push;
    }

}
