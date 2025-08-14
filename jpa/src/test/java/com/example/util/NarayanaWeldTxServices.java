package com.example.util;

import org.jboss.weld.transaction.spi.TransactionServices;

import com.arjuna.ats.jta.TransactionManager;

import jakarta.transaction.Synchronization;
import jakarta.transaction.UserTransaction;

public class NarayanaWeldTxServices implements TransactionServices {

  @Override 
  public void registerSynchronization(Synchronization sync) {
    try { 
        TransactionManager.transactionManager()
            .getTransaction().registerSynchronization(sync);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  @Override 
  public boolean isTransactionActive() {
    try { 
        return TransactionManager.transactionManager()
            .getTransaction() != null; }
    catch (Exception e) { return false; }
  }
  @Override 
  public UserTransaction getUserTransaction() {
    return com.arjuna.ats.jta.UserTransaction.userTransaction();
  }

  @Override 
  public void cleanup() {}

}