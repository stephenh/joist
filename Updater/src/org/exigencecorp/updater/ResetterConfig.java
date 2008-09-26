package org.exigencecorp.updater;

import javax.sql.DataSource;

public abstract class ResetterConfig {

    public abstract DataSource getDataSource();

    public abstract String getDatabaseName();

    public abstract String getUsername();

    public abstract String getPassword();

}
