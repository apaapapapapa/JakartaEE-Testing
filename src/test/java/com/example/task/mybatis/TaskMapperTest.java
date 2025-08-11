package com.example.task.mybatis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;

import com.example.BaseTest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.List;

import com.example.task.Task;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;

@DBRider
class TaskMapperTest extends BaseTest {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(
        Arrays.stream(WELD_CORE_BEANS).toArray(Class[]::new)
    ).activate(RequestScoped.class).build();

    @Inject
    TaskMapper taskMapper;

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
