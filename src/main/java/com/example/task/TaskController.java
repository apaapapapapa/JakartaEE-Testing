package com.example.task;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.task.jpa.TaskRepository;

import jakarta.ejb.EJBException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;

@RequestScoped
public class TaskController {

    private final TaskRepository taskRepository;

    @Inject
    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskController() {
        this.taskRepository = null;
    }

    /**
     * 遅延リスク高タスクの割合（%）を返す
     * ・未完了
     * ・期日が今日から3日以内
     */
    public double calculateDelayRiskRate() {
        List<Task> allTasks = taskRepository.findAll();
        if (allTasks.isEmpty()) {
            return 0.0;
        }
        long highRiskCount = allTasks.stream()
            .filter(t -> t.getCompleted() == null || !t.getCompleted())
            .filter(t -> {
                LocalDate today = LocalDate.now();
                LocalDate due = t.getDueDate();
                return due != null && !due.isBefore(today) && !due.isAfter(today.plusDays(3));
            })
            .count();
        return (highRiskCount * 100.0) / allTasks.size();
    }

    /**
     * 遅延リスク高タスクのリストを返す
     * ・未完了
     * ・期日が今日から3日以内
     */
    public List<Task> findHighRiskTasks() {
        List<Task> allTasks = taskRepository.findAll();
        LocalDate today = LocalDate.now();
        return allTasks.stream()
            .filter(t -> t.getCompleted() == null || !t.getCompleted())
            .filter(t -> {
                LocalDate due = t.getDueDate();
                return due != null && !due.isBefore(today) && !due.isAfter(today.plusDays(3));
            })
            .toList();
    }

    public List<Task> loadAll() {
        return taskRepository.findAll();
    }

    // サブタスク: 親タスクIDを指定して子タスクを追加
    public Task addSubtask(int parentId, String title, LocalDate dueDate, boolean completed) {
        validateTaskInput(title, dueDate);

        Task parent = taskRepository.findById(parentId)
            .orElseThrow(() -> new EJBException("親タスクが見つかりません: " + parentId));

        Task subtask = new Task();
        subtask.setTitle(title);
        subtask.setDueDate(dueDate);
        subtask.setCompleted(completed);
        subtask.setParent(parent);

        return taskRepository.create(subtask);
    }

    // サブタスク: 親タスクIDで子タスク一覧を取得
    public List<Task> findSubtasks(int parentId) {
        return taskRepository.findByParentId(parentId);
    }

    // サブタスク: ルートタスク（親がnull）のみ取得
    public List<Task> findRootTasks() {
        return taskRepository.findRootTasks();
    }

    public Task add(String title, LocalDate dueDate, boolean completed) {
        validateTaskInput(title, dueDate);

        final Task newTask = new Task();
        newTask.setTitle(title);
        newTask.setDueDate(dueDate);
        newTask.setCompleted(completed);

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

    public Task update(int id, String title, LocalDate dueDate, boolean completed) {
        validateTaskInput(title, dueDate);
        try {
            return taskRepository.update(id, title, dueDate, completed);
        } catch (EntityNotFoundException enf) {
            throw new EJBException(enf);
        }
    }

    private void validateTaskInput(String title, LocalDate dueDate) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("タイトルは必須です。");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("期限(dueDate)は必須です。");
        }
        if (dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("期限(dueDate)は今日以降の日付を指定してください。");
        }
    }

    /**
     * 全タスクの進捗率（完了/全体 * 100.0）を計算する
     * @return 進捗率（パーセンテージ, 0.0～100.0）
     */
    public double calculateProgressRate() {
        List<Task> allTasks = taskRepository.findAll();
        if (allTasks.isEmpty()) {
            return 0.0;
        }
        long completedCount = allTasks.stream()
            .filter(t -> t.getCompleted() != null && t.getCompleted())
            .count();
        return (completedCount * 100.0) / allTasks.size();
    }
}
