package com.example.task.mybatis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

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
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;

@DBRider
class TaskMapperTest extends BaseTest {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(
        Arrays.stream(WELD_CORE_BEANS).toArray(Class[]::new)
    ).activate(RequestScoped.class).build();

    @Inject TaskMapper taskMapper;

    // データセットのパスは定数で一元化
    private static final String DS_TASKS = "datasets/tasks.yml";
    private static final String DS_AFTER_INSERT = "datasets/expected_tasks_after_insert.yml";


    @Test
    @DataSet(value = DS_TASKS, cleanBefore = true)
    void testFindAll() {
        List<Task> tasks = taskMapper.findAll();
        assertEquals(2, tasks.size());
        assertTask(tasks.get(0), 1, "task1", false);
        assertTask(tasks.get(1), 2, "task2", true);
    }

    @Test
    @DataSet(value = DS_TASKS, cleanBefore = true)
    void testFindById() {
        var t = taskMapper.findById(1);
                    System.out.println(t.getTitle());
        assertTask(t, 1, "task1", false);
    }

    @Test
    @DataSet(value = DS_TASKS, cleanBefore = true)
    void findById_miss_returns_null() {
        assertNull(taskMapper.findById(999));
    }


    @Test
    @DataSet(value = DS_TASKS, cleanBefore = true, cleanAfter = true)
    @ExpectedDataSet(value = DS_AFTER_INSERT)
    void create_inserts_two_rows() {
        taskMapper.create(newTask("task3", false));
        taskMapper.create(newTask("task4", true));
        // 期待状態は @ExpectedDataSet に委ねる。ここでの追加アサートは最小限でOK
        var tasks = taskMapper.findAll();

        assertEquals(4, tasks.size());
        assertTask(tasks.get(2), 3, "task3", false);
        assertTask(tasks.get(3), 4, "task4", true);
    }

    // --- helpers ---
    private Task newTask(String title, boolean completed) {
        Task t = new Task();
        t.setTitle(title);
        t.setCompleted(completed);
        return t;
    }

    private void assertTask(Task t, int id, String title, boolean completed) {
        assertEquals(id, t.getId());
        assertEquals(title, t.getTitle());
        if (completed) assertTrue(Boolean.TRUE.equals(t.getCompleted()));
        else           assertFalse(Boolean.TRUE.equals(t.getCompleted()));
    }
}
