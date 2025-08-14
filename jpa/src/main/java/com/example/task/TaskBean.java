package com.example.task;

import java.io.Serializable;
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

    private List<Task> allTasks;

    private TaskController controller;

    @Getter
    @Setter
    private String title;
    
    @Getter
    @Setter
    private String id;

    @PostConstruct
    public void postConstruct() {
        refresh();
    }

    @Inject
    public TaskBean(TaskController controller){
        this.controller = controller;
    }

    public void delete() {
        try {
            controller.delete(Integer.parseInt(id));
            refresh();
            addMessage("Task " + id + " deleted");
        } catch (Exception e) {
            addMessage("Error deleting the Task by Id. " + e.getLocalizedMessage());
        }
    }

    public void add() {
        try {
            controller.add(title);
            refresh();
            addMessage("Task with title " + title + " created");
        } catch (Exception e) {
            addMessage("Error adding a new Task. " + e.getLocalizedMessage());
        }
    }

    public void update() {
        try {
            controller.update(Integer.parseInt(id), title);
            refresh();
            addMessage("Task " + id + " updated");
        } catch (Exception e) {
            addMessage("Error updating the Task by Id. " + e.getLocalizedMessage());
        }
    }

    public void refresh() {
        this.allTasks = controller.loadAll();
    }

    public List<Task> getAllTasks() {
        return allTasks;
    }

    private void addMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(message));
    }
}