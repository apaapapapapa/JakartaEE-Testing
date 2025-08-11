package com.example.task.jpa;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.task.Task;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Stateless
@Transactional
public class TaskRepository {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private final EntityManager em;

    @Inject
    public TaskRepository(EntityManager em) {
        this.em = em;
    }

    // Default public constructor required by EJB spec
    public TaskRepository() {
        this.em = null;
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

    public Task update(int id, String title, java.time.LocalDate dueDate, boolean completed) {
        logger.log(Level.INFO, "Updating task {0} (full update)", title);
        Task ref = em.getReference(Task.class, id);
        ref.setTitle(title);
        ref.setDueDate(dueDate);
        ref.setCompleted(completed);
        return em.merge(ref);
    }

    public long countIncompleteTasks() {
        logger.info("Counting incomplete tasks");
        return em.createQuery("SELECT COUNT(t) FROM Task t WHERE t.completed = false", Long.class)
                .getSingleResult();
    }

    public List<Task> findByParentId(Integer parentId) {
        logger.info("Getting tasks by parentId " + parentId);
        if (parentId == null) {
            return em.createQuery("SELECT t FROM Task t WHERE t.parent IS NULL", Task.class)
                    .getResultList();
        } else {
            return em.createQuery("SELECT t FROM Task t WHERE t.parent.id = :parentId", Task.class)
                    .setParameter("parentId", parentId)
                    .getResultList();
        }
    }

    public List<Task> findRootTasks() {
        logger.info("Getting root tasks (parent is null)");
        return em.createQuery("SELECT t FROM Task t WHERE t.parent IS NULL", Task.class)
                .getResultList();
    }
    
}
