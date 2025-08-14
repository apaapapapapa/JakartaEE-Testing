package com.example;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import com.example.util.TestMyBatisBootstrap;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.junit5.DBUnitExtension;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

@ExtendWith({WeldJunit5Extension.class, DBUnitExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    private static final String CHANGELOG_PATH = "db/changelog/db.changelog-master.yaml";
    private static final Set<String> INITIALIZED_URLS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Inject
    DataSource dataSource;

    private static final Class<?>[] WELD_CORE_BEANS = {
        TestMyBatisBootstrap.class
    };

    protected Class<?>[] additionalWeldBeans() {
        return new Class<?>[0];
    }

    private Class<?>[] mergedWeldBeans() {
        return Arrays.stream(new Class<?>[][] { WELD_CORE_BEANS, additionalWeldBeans() })
                .flatMap(Arrays::stream)
                .distinct()
                .toArray(Class<?>[]::new);
    }

    private static final Class<? extends Annotation>[] coreActivatedScopes() {
        return new Class[]{RequestScoped.class};
    }

    protected Class<? extends Annotation>[] additionalActivatedScopes() {
        return new Class[0];
    }

    private Class<? extends Annotation>[] mergedActivatedScopes() {
        return Arrays.stream(new Class[][]{coreActivatedScopes(), additionalActivatedScopes()})
                .flatMap(Arrays::stream)
                .distinct()
                .toArray(Class[]::new);
    }

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(mergedWeldBeans())
            .activate(mergedActivatedScopes())
            .build();

    /**
     * Database Rider、Liquibase用のDB接続情報
     */
    protected final ConnectionHolder connectionHolder() {
        return () -> dataSource.getConnection();
    }

    /**
     * Liquibaseでスキーマ生成
     */
    @BeforeAll
    void setupSchema() throws Exception {
        try (Connection c = dataSource.getConnection()) {
            String url = c.getMetaData().getURL();
            if (!INITIALIZED_URLS.add(url)) {
                return;
            }
            var db = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(c));

            try (Liquibase lb = new Liquibase(CHANGELOG_PATH, new ClassLoaderResourceAccessor(), db)) {
                try {
                    lb.update(new Contexts(), new LabelExpression());
                } catch (liquibase.exception.LockException e) {
                    lb.forceReleaseLocks();
                    lb.update(new Contexts(), new LabelExpression());
                }
            }
        }
    }
}
