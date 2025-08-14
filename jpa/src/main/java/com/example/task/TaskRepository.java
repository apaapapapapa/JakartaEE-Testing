package com.example.task;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;

@Stateless
@NoArgsConstructor
@Transactional
public class TaskRepository {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private EntityManager em;

    @Inject
    public TaskRepository(EntityManager em) {
        this.em = em;
    }

    public List<Task> findAll() {
        logger.info("Getting all task");
        return em.createQuery("SELECT c FROM Task c", Task.class).getResultList();
    }

    public Task create(Task task) {
        logger.info("Creating task" + task.getTitle());
        em.persist(task);
        return task;
    }

    public Optional<Task> findById(int id) {
        logger.log(Level.INFO, "Getting task by id {0}", id);
        return Optional.ofNullable(em.find(Task.class, id));
    }

    public void delete(int id) {
        logger.log(Level.INFO, "Deleting task by id {0}", id);
        var task = findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid task Id:" + id));
        em.remove(task);
    }

    public Task update(int id, String title) {
        logger.log(Level.INFO, "Updating task {0}", title);
        Task ref = em.getReference(Task.class, id);
        ref.setTitle(title);
        return em.merge(ref);
    }
    
}