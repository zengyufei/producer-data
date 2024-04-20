package com.zyf.producer.entitys.bean.mysql;


import com.zyf.producer.annotations.TableField;
import com.zyf.producer.annotations.TableId;
import com.zyf.producer.annotations.TableName;
import com.zyf.producer.enums.IdType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("department")
public class DepartmentPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField(value = "name")
    private String name;


    @TableField(value = "create_date")
    private LocalDateTime createDate;

    @TableField(value = "tenant_id")
    private String tenantId;

}
