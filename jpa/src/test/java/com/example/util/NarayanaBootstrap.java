package com.example.util;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

@ApplicationScoped
public class NarayanaBootstrap {

  @PostConstruct
  void start() { // 触るだけで初期化される
      com.arjuna.ats.jta.TransactionManager.transactionManager();
  }

  @Produces 
  @ApplicationScoped
  public UserTransaction userTx() {
      return com.arjuna.ats.jta.UserTransaction.userTransaction();
  }

  @Produces 
  @ApplicationScoped
  public TransactionManager tm() {
      return com.arjuna.ats.jta.TransactionManager.transactionManager();
  }
}