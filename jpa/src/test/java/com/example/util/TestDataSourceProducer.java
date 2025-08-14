package com.example.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

@ApplicationScoped
public class TestDataSourceProducer {
  @Produces @ApplicationScoped
  public DataSource dataSource() {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"); // JPA側と同じDB名
    ds.setUser("sa");
    ds.setPassword("");
    return ds;
  }
}