package com.zyf.producer.base;

import com.zyf.producer.annotations.TableField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 抽象实体
 */
@Getter
@Setter
public class BaseEntity implements Serializable {

    /**
     * 创建者
     */
    @TableField(value = "CREATE_BY")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME")
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    @TableField(value = "UPDATE_BY")
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "UPDATE_TIME")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志位
     */
    @TableField(value = "DEL_FLAG")
    private Integer delFlag;

}
