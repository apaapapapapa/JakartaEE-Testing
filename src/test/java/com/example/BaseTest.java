package com.example;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import jakarta.inject.Inject;

import org.jboss.weld.junit5.WeldJunit5Extension;
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
    private static final AtomicBoolean SCHEMA_INITIALIZED = new AtomicBoolean(false);

    protected static final Class<?>[] WELD_CORE_BEANS = {
        TestMyBatisBootstrap.class
    };

    @Inject
    DataSource dataSource;

    public ConnectionHolder connectionHolder() {
        return () -> dataSource.getConnection();
    }

    @BeforeAll
    void setupSchema() throws Exception {
        // ここで一度きりにする
        if (!SCHEMA_INITIALIZED.compareAndSet(false, true)) {
            return;
        }
        try (Connection c = dataSource.getConnection()) {
            var db = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(c));
            try (Liquibase lb = new Liquibase(CHANGELOG_PATH,
                    new ClassLoaderResourceAccessor(), db)) {
                lb.update(new Contexts(), new LabelExpression());
            }
        }
    }
}
