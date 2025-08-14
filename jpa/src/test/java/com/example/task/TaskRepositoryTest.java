package com.example.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.*;

import com.example.BaseTest;
import com.example.util.TestDataSourceProducer;

@Disabled
class TaskRepositoryTxTest extends BaseTest {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(
        WeldInitiator.createWeld()
        .addServices(new com.example.util.NarayanaWeldTxServices()) // ここで直接登録
        .addBeanClass(com.example.util.NarayanaWeldTxServices.class)
        .addBeanClass(com.example.util.TestJpaProducers.class)
        .addBeanClass(com.example.task.TaskRepository.class)
        .addBeanClass(TestDataSourceProducer.class)
    ).activate(jakarta.enterprise.context.RequestScoped.class)
    .inject(this)
    .build();

  @Inject TaskRepository repo;

  @Test
  void create_and_findAll_commits() {
    Task t = new Task();
    t.setTitle("t1");
    repo.create(t); // @Transactional(TaskRepository クラス) が効く

    assertThat(repo.findAll()).extracting(Task::getTitle).contains("t1");
  }

  // 明示 rollback の例（補助用に Tx付きメソッドを用意）
  @Transactional
  void saveAndFail() { 
    Task t = new Task(); t.setTitle("will_rollback");
    repo.create(t);
    throw new RuntimeException("boom");
  }

  @Test
  void rollback_on_exception() {
    assertThrows(RuntimeException.class, this::saveAndFail);
    assertThat(repo.findAll()).extracting(Task::getTitle)
      .doesNotContain("will_rollback");
  }
}