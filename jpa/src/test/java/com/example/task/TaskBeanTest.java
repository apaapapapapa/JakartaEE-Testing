// package com.example.task;

// import com.example.util.MockFacesContext;
// import com.github.database.rider.core.api.dataset.DataSet;
// import com.github.database.rider.junit5.api.DBRider;

// import io.github.cdiunit.AdditionalClasses;
// import io.github.cdiunit.InRequestScope;
// import io.github.cdiunit.junit5.CdiJUnit5Extension;
// import jakarta.enterprise.context.RequestScoped;
// import jakarta.enterprise.inject.Produces;
// import jakarta.faces.context.FacesContext;
// import jakarta.inject.Inject;
// import jakarta.persistence.EntityManager;
// import jakarta.persistence.Persistence;
// import jakarta.transaction.Transactional;

// import org.junit.jupiter.api.*;
// import org.junit.jupiter.api.extension.ExtendWith;

// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.argThat;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;

// /**
//  * TaskBean のテストクラス.
//  */
// @ExtendWith({.class})
// @AdditionalClasses({TaskController.class, TaskRepository.class})
// @Disabled
// class TaskBeanTest {

//     @Produces
//     @RequestScoped
//     EntityManager createEntityManager() {
//         return Persistence.createEntityManagerFactory("TaskPersistenceUnit").createEntityManager();
//     }

//     @Inject
//     TaskController taskController;

//     @Inject
//     TaskRepository taskRepository;

//     private FacesContext facesContext;

//     private TaskBean taskBean;

//     @BeforeEach
//     void setUp() {

//         // Mock FacesContext
//         facesContext = MockFacesContext.mock();

//         taskBean = new TaskBean(taskController);

//     }

//     @AfterEach
//     void tearDown() {

//         facesContext.release();
//     }

//     /**
//      * TaskBean の add() メソッドのテスト.
//      */
//     @InRequestScope
//     @Transactional
//     @DBRider
//     //@DataSet(value = "datasets/empty-tasks.yml", transactional = true) // テスト前に tasks テーブルを空にする
//     void testAddTask() {

//         // Arrange
//         String taskTitle = "新しいタスク";
//         taskBean.setTitle(taskTitle);

//         // Act
//         taskBean.add();

//         // Assert
//         List<Task> allTasks = taskBean.getAllTasks();
//         assertNotNull(allTasks);
//         assertEquals(1, allTasks.size());
//         assertEquals(taskTitle, allTasks.get(0).getTitle());

//         verify(facesContext).addMessage(eq(null), argThat(message ->
//                 message.getSummary().equals("Task with title " + taskTitle + " created")));
//     }

//     /**
//      * TaskBean の delete() メソッドのテスト. 
//      */
//     @InRequestScope
//     @Transactional
//     @DBRider
//     @DataSet(value = "datasets/existing-tasks.yml", transactional = true) // テスト前に既存のタスクをロード
//     void testDeleteTask() {

//         // Arrange
//         String taskId = "1";
//         taskBean.setId(taskId);

//         // Act
//         taskBean.delete();

//         // Assert
//         List<Task> allTasks = taskBean.getAllTasks();
        
//         assertNotNull(allTasks);
//         assertEquals(1, allTasks.size());
//         assertEquals("初期タスク2", allTasks.get(0).getTitle());

//         verify(facesContext).addMessage(eq(null), argThat(message ->
//                 message.getSummary().equals("Task " + taskId + " deleted")));
//     }

//     /**
//      * TaskBean の update() メソッドのテスト.
//      */
//     @InRequestScope
//     @Transactional
//     @DBRider
//     @DataSet(value = "datasets/existing-tasks.yml", transactional = true)
//     void testUpdateTask() {
//         // Arrange
//         String taskId = "1";
//         String updatedTitle = "更新されたタスクタイトル";
//         taskBean.setId(taskId);
//         taskBean.setTitle(updatedTitle);

//         // Act
//         taskBean.update();

//         // Assert
//         List<Task> allTasks = taskBean.getAllTasks();
//         assertNotNull(allTasks);
//         assertEquals(2, allTasks.size());
//         assertEquals(updatedTitle, allTasks.get(0).getTitle());

//         verify(facesContext).addMessage(eq(null), argThat(message ->
//                 message.getSummary().equals("Task " + taskId + " updated")));
//     }

// }