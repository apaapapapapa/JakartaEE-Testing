package com.example;

import java.sql.Connection;
import java.sql.DriverManager;

import org.jboss.weld.junit5.WeldJunit5Extension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.database.rider.junit5.DBUnitExtension;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

@ExtendWith({WeldJunit5Extension.class, DBUnitExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    private static final String CHANGELOG_PATH = "db/changelog/db.changelog-master.yaml";

    @BeforeAll
    static void initSchema() throws Exception {
    try (Connection c = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")) {
        Database db = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(new JdbcConnection(c));
        try (Liquibase lb = new Liquibase(CHANGELOG_PATH,
            new ClassLoaderResourceAccessor(), db)) {
        lb.update((String) null);
        }
    }
    }
}
