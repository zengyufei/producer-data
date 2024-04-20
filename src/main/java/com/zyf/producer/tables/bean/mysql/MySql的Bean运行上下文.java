package com.zyf.producer.tables.bean.mysql;

import com.zyf.producer.base.bean.BaseBeanContext;
import com.zyf.producer.entitys.bean.mysql.DepartmentPo;
import com.zyf.producer.entitys.bean.mysql.EmployeePo;
import com.zyf.producer.entitys.bean.mysql.TenantPo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MySql的Bean运行上下文 extends BaseBeanContext {
    private TenantPo tenantPo;
    private DepartmentPo departmentPo;
    private EmployeePo employeePo;

}
