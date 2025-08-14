package com.example.util;

import java.io.Reader;
import javax.sql.DataSource;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.example.task.mybatis.TaskMapper;

@ApplicationScoped
public class TestMyBatisBootstrap {

    private SqlSessionFactory sqlSessionFactory;
    private DataSource dataSource;

    @PostConstruct
    void init() {
        try (Reader r = Resources.getResourceAsReader("META-INF/mybatis-config-test.xml")) {
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(r);
            this.dataSource = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to bootstrap MyBatis", e);
        }
    }

    @Produces 
    @ApplicationScoped
    public DataSource dataSource() { return dataSource; }

    @Produces 
    @ApplicationScoped
    public SqlSessionFactory sqlSessionFactory() { return sqlSessionFactory; }

    @Produces 
    @RequestScoped
    public SqlSession sqlSession() { 
        return sqlSessionFactory.openSession(true);
    }

    public void close(@Disposes SqlSession session) {
        if (session != null) session.close();
    }

    // ★ 具体型で返す（CDIは型変数Producer不可）
    @Produces
    public TaskMapper taskMapper(SqlSession session) {
        return session.getMapper(TaskMapper.class);
    }
}