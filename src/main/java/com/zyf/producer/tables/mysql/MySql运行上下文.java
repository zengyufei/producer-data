package com.zyf.producer.tables.mysql;

import com.zyf.producer.base.BaseContext;
import com.zyf.producer.entitys.mysql.DepartmentPo;
import com.zyf.producer.entitys.mysql.EmployeePo;
import com.zyf.producer.entitys.mysql.TenantPo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MySql运行上下文 extends BaseContext {
    private TenantPo tenantPo;
    private DepartmentPo departmentPo;
    private EmployeePo employeePo;

}
