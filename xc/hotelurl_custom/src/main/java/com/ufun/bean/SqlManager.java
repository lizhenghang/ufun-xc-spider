package com.ufun.bean;

import com.ufun.config.JDBCConfig;
import org.beetl.sql.core.*;
import org.beetl.sql.core.db.DBStyle;
import org.beetl.sql.core.db.MySqlStyle;
import org.beetl.sql.ext.DebugInterceptor;

/***
 * @author mayue
 * @mail dashingmy@163.com
 * @TIME 2019/3/19 17:07
 */
public class SqlManager {

    private SQLManager sqlManager;

    public SQLManager getSqlManager() {
        return sqlManager;
    }

    public void setSqlManager(SQLManager sqlManager) {
        this.sqlManager = sqlManager;
    }

}
