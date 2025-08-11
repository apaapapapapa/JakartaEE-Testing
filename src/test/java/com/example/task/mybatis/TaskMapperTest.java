package com.example.task.mybatis;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.example.util.MyBatisMapperProducer;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import java.util.List;
import com.example.task.Task;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;
import com.github.database.rider.junit5.DBUnitExtension;
import java.sql.Connection;
import java.sql.DriverManager;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

@DBRider
@ExtendWith({WeldJunit5Extension.class, DBUnitExtension.class})
class TaskMapperTest {

    @BeforeAll
    static void setupSchema() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MYSQL;USER=sa;PASSWORD=")) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase("db/changelog-master.xml", new ClassLoaderResourceAccessor(), database)) {
                liquibase.update("");
            }
        }
    }

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(
        SqlSessionFactoryProducer.class,
        MyBatisMapperProducer.class
    ).activate(RequestScoped.class)
    .build();

    @Inject
    SqlSessionFactoryProducer sessionFactoryProducer;

    @Inject
    TaskMapper taskMapper;


    @Test
    void helloTest(){
        assertNotNull(sessionFactoryProducer);
        assertNotNull(taskMapper);
    }

    @Test
    @DataSet("datasets/tasks.yml")
    void testFindAll() {
        List<Task> tasks = taskMapper.findAll();
        assertEquals(2, tasks.size());
        Task t1 = tasks.get(0);
        assertEquals(1, t1.getId());
        assertEquals("task1", t1.getTitle());
        assertFalse(t1.getCompleted());
        Task t2 = tasks.get(1);
        assertEquals(2, t2.getId());
        assertEquals("task2", t2.getTitle());
        assertTrue(t2.getCompleted());
    }
}
