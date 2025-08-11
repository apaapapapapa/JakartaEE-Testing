package com.example.task.mybatis;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.cdi.SessionFactoryProvider;

import java.io.IOException;
import java.io.InputStream;

@Dependent
public class SqlSessionFactoryProducer {

    @Produces
    @ApplicationScoped
    @SessionFactoryProvider
    public SqlSessionFactory produceFactory() throws IOException {
        InputStream fileStream = Resources.getResourceAsStream("META-INF/mybatis-config.xml");
        return new SqlSessionFactoryBuilder().build(fileStream);
    }
}
