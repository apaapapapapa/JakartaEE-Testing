package com.example.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import jakarta.enterprise.inject.spi.BeanManager;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class TestJpaProducers {

  @Inject BeanManager bm;

  @Produces @ApplicationScoped
  public EntityManagerFactory emf() {
    Map<String,Object> props = new HashMap<>();
    // JPA から CDI を使えるように
    props.put("jakarta.persistence.bean.manager", bm);
    return Persistence.createEntityManagerFactory("testPU", props);
  }

  @Produces @RequestScoped
  public EntityManager em(EntityManagerFactory emf) { return emf.createEntityManager(); }

  public void close(@Disposes EntityManager em) { if (em.isOpen()) em.close(); }
  public void close(@Disposes EntityManagerFactory emf) { if (emf.isOpen()) emf.close(); }
}