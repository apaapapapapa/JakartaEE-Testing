package com.example.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.example.task.mybatis.TaskMapper;

@ApplicationScoped
public class MyBatisMapperProducer {

    private final SqlSessionFactory sqlSessionFactory;

    @Inject
    public MyBatisMapperProducer(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /** テスト1回分のスコープでセッションを開く（auto-commit=true でも false でもお好みで） */
    @Produces
    @RequestScoped
    public SqlSession produceSqlSession() {
        return sqlSessionFactory.openSession(true);
    }

    public void close(@Disposes SqlSession session) {
        if (session != null) session.close();
    }

    @Produces
    public TaskMapper produceTaskMapper(SqlSession session) {
        return session.getMapper(TaskMapper.class);
    }
}