package com.zyf.producer.base.sql;

import cn.hutool.db.Entity;
import com.zyf.producer.enums.DbType;
import com.zyf.producer.tables.sql.mysql.MySql的Sql运行上下文;

import java.util.List;
import java.util.Optional;

public abstract class BaseSqlEntity {

    public abstract DbType getDbType();

    public abstract String getDataSql();

    public abstract Entity setNewId(Entity entity);

    public abstract Entity createNew();

    public abstract String getId(Entity entity);

    public abstract void setRef(MySql的Sql运行上下文 context, Entity entity);

    public abstract List<Entity> getParents(MySql的Sql运行上下文 context);

    public Entity sendContext(MySql的Sql运行上下文 context) {
        return sendContext(context, Optional.empty());
    }

    public abstract Entity sendContext(MySql的Sql运行上下文 context, Optional<Entity> entity);

    public abstract Entity takeContext(MySql的Sql运行上下文 context);
}
