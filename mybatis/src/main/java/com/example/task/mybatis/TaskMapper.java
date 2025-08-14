package com.example.task.mybatis;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.mybatis.cdi.Mapper;

import com.example.task.Task;

/**
 * MyBatis mapper interface for Task entity.
 */
@Mapper
public interface TaskMapper {

    List<Task> findAll();

    void create(Task task);

    Task findById(@Param("id") int id);

    void delete(@Param("id") int id);

    void update(Map<String, Object> params);

    long countIncompleteTasks();

    List<Task> findByParentId(@Param("parentId") Integer parentId);

    List<Task> findRootTasks();
}
