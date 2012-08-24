package joist.domain.orm.queries.columns;

import java.sql.ResultSet;
import java.sql.SQLException;

import joist.domain.DomainObject;
import joist.domain.Shim;
import joist.domain.orm.queries.Alias;
import joist.domain.orm.queries.Where;

public class StringAliasColumn<T extends DomainObject> extends AliasColumn<T, String, String> {

  /** Converts the database {@code defaultValue} into the field initializer string. */
  public static String defaultValue(String defaultValue) {
    return "\"" + defaultValue + "\"";
  }

  public StringAliasColumn(Alias<T> alias, String name, Shim<T, String> shim) {
    super(alias, name, shim);
  }

  public Where like(String pattern) {
    return new Where(this.getQualifiedName() + " like ?", pattern);
  }

  @Override
  public void setJdbcValue(T instance, ResultSet rs, int i) throws SQLException {
    this.setJdbcValue(instance, rs.getString(i));
  }

}
