package com.example.task;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named
@ViewScoped
public class TaskBean implements Serializable {

    private static final String TASK_PREFIX = "Task ";

    private transient List<Task> allTasks;

    private final TaskController controller;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private LocalDate dueDate;

    @Getter
    @Setter
    private boolean completed;

    // サブタスク用: 親タスクID
    @Getter
    @Setter
    private Integer parentId;

    // サブタスク追加フォーム用プロパティ
    @Getter
    @Setter
    private String subtaskTitle;

    @Getter
    @Setter
    private LocalDate subtaskDueDate;

    @Getter
    @Setter
    private boolean subtaskCompleted;

    @PostConstruct
    public void postConstruct() {
        refresh();
    }

    @Inject
    public TaskBean(TaskController controller) {
        this.controller = controller;
    }

    public TaskBean() {
        // Required by the framework (e.g., for proxying or serialization)
        this.controller = null;
    }

    public void delete() {
        runWithMessage(() -> {
            controller.delete(id);
            refresh();
            addMessage(TASK_PREFIX + id + " deleted");
        }, "Error deleting the Task by Id.");
    }

    public void deleteById(int id) {
        runWithMessage(() -> {
            controller.delete(id);
            refresh();
            addMessage(TASK_PREFIX + id + " deleted");
        }, "Error deleting the Task by Id.");
    }

    public void add() {
        runWithMessage(() -> {
            controller.add(title, dueDate, completed);
            refresh();
            addMessage("Task with title " + title + " created");
        }, "Error adding a new Task.");
    }

    public void update() {
        runWithMessage(() -> {
            controller.update(id, title, dueDate, completed);
            refresh();
            addMessage(TASK_PREFIX + id + " updated");
        }, "Error updating the Task by Id.");
    }

    // サブタスク追加
    public void addSubtask() {
        runWithMessage(() -> {
            controller.addSubtask(parentId, subtaskTitle, subtaskDueDate, subtaskCompleted);
            refresh();
            addMessage("Subtask with title " + subtaskTitle + " created (parentId=" + parentId + ")");
            // フォーム値クリア
            subtaskTitle = null;
            subtaskDueDate = null;
            subtaskCompleted = false;
            parentId = null;
        }, "Error adding a new Subtask.");
    }

    // 指定した親タスクの子タスク一覧を取得
    public List<Task> getSubtasks(int parentId) {
        return controller.findSubtasks(parentId);
    }

    // Task型引数のオーバーロード（JSFバインディング用）
    public List<Task> getSubtasks(Task parentTask) {
        if (parentTask == null) return null;
        return controller.findSubtasks(parentTask.getId());
    }

    // ルートタスク一覧を取得
    public List<Task> getRootTasks() {
        return controller.findRootTasks();
    }

    // JSFバインディング用（index.xhtmlの#{taskBean.rootTasks}）
    public List<Task> getRootTasksProperty() {
        return getRootTasks();
    }

    private void runWithMessage(Runnable action, String errorMessage) {
        try {
            action.run();
        } catch (Exception e) {
            addMessage(errorMessage + " " + e.getLocalizedMessage());
        }
    }

    public void refresh() {
        this.allTasks = controller.loadAll();
    }

    public List<Task> getAllTasks() {
        return allTasks;
    }

    /**
     * 全タスクの進捗率（パーセンテージ, 0.0～100.0）を返す
     */
    public double getProgressRate() {
        return controller.calculateProgressRate();
    }

    private void addMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(message));
    }

    /**
     * 遅延リスク高タスクの割合（%）を返す
     */
    public double getDelayRiskRate() {
        return controller.calculateDelayRiskRate();
    }

    /**
     * 遅延リスク高タスクのリストを返す
     */
    public List<Task> getHighRiskTasks() {
        return controller.findHighRiskTasks();
    }
}
