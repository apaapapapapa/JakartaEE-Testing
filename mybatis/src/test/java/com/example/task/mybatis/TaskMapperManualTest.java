package com.example.task.mybatis;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.sql.Connection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;
import org.junit.jupiter.api.*;

import com.example.task.Task;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

class TaskMapperManualTest {

    private static final String CHANGELOG_PATH = "db/changelog/db.changelog-master.yaml";
    private static final Set<String> INITIALIZED_URLS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    static SqlSessionFactory factory;
    static DataSource dataSource;

    SqlSession session;
    TaskMapper mapper;

    @BeforeAll
    static void setupFactory() throws Exception {
        try (Reader r = Resources.getResourceAsReader("META-INF/mybatis-config-test.xml")) {
            factory = new SqlSessionFactoryBuilder().build(r);
            dataSource = factory.getConfiguration().getEnvironment().getDataSource();
        }
        
        try (Connection c = dataSource.getConnection()) {
            String url = c.getMetaData().getURL();
            if (!INITIALIZED_URLS.add(url)) {
                return;
            }
            var db = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(c));

            try (Liquibase lb = new Liquibase(CHANGELOG_PATH, new ClassLoaderResourceAccessor(), db)) {
                try {
                    lb.update(new Contexts(), new LabelExpression());
                } catch (liquibase.exception.LockException e) {
                    lb.forceReleaseLocks();
                    lb.update(new Contexts(), new LabelExpression());
                }
            }
        }
    }

    @BeforeEach
    void openSession() {
        // 自動コミットON
        session = factory.openSession(true);
        mapper = session.getMapper(TaskMapper.class);
    }

    @AfterEach
    void tearDown() {
        if (session != null) {
            session.close();
        }
    }

    @Test
    void findById_returnsTask() {
        Task t = new Task();
        t.setTitle(CHANGELOG_PATH);
        // 例: 事前投入済みのID=1を想定
        mapper.create(t);
        var task = mapper.findById(1);
        assertNotNull(task);
        assertEquals(1, task.getId());
    }
}