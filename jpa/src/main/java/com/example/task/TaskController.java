package com.example.task;

import java.util.List;
import java.util.Optional;

import jakarta.ejb.EJBException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;

@RequestScoped
public class TaskController {

    @Inject
    TaskRepository taskRepository;

    public List<Task> loadAll() {
        return taskRepository.findAll();
    }

    public Task add(String title) {

        final Task newTask = new Task();
        newTask.setTitle(title);

        return taskRepository.create(newTask);

    }

    public Task delete(int id) {

        // IDが存在するか確認
        Optional<Task> task = taskRepository.findById(id);
        
        if (!task.isPresent()) {
            throw new EJBException("指定されたIDのタスクが見つかりません: " + id);
        }
        
        taskRepository.delete(id);

        return task.get();
        
    }

    public Task update(int id, String title) {
        try {
            return taskRepository.update(id, title);
        } catch (EntityNotFoundException enf) {
            throw new EJBException(enf);
        }
    }
}
