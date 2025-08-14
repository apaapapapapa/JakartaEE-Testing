package com.example.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.XADataSource;

import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;

import com.arjuna.ats.jdbc.TransactionalDriver;
import org.h2.jdbcx.JdbcDataSource;

public class TransactionalConnectionProvider implements ConnectionProvider {

  private final TransactionalDriver driver = new TransactionalDriver();
  private final XADataSource xaDs;

  public TransactionalConnectionProvider() {
    // H2 の XADataSource を生成して設定
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"); // 同一JVM内でDBを保持
    ds.setUser("sa");
    ds.setPassword("");
    this.xaDs = ds;
  }

  @Override
  public Connection getConnection() throws SQLException {
    Properties p = new Properties();
    // 重要: クラス名ではなくインスタンスを渡す
    p.put(TransactionalDriver.XADataSource, xaDs);
    // これで JTA に enlist されたコネクションが返る
    return driver.connect("jdbc:arjuna:", p);
  }

  @Override public void closeConnection(Connection conn) throws SQLException { if (conn != null) conn.close(); }
  @Override public boolean supportsAggressiveRelease() { return false; }

  @Override public boolean isUnwrappableAs(Class<?> a) { return a.isAssignableFrom(getClass()); }
  @Override public <T> T unwrap(Class<T> a) {
    if (isUnwrappableAs(a)) return a.cast(this);
    throw new UnknownUnwrapTypeException(a);
  }
}
