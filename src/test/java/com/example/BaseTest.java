package com.example;

import java.sql.Connection;

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
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;


@ExtendWith({WeldJunit5Extension.class, DBUnitExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

private static final String CHANGELOG_PATH = "db/changelog/db.changelog-master.yaml";

    // 全クラス共通でBean登録するクラス
    protected static final Class<?>[] WELD_CORE_BEANS = {
        TestMyBatisBootstrap.class
    };

    @Inject
    DataSource dataSource; // ← MyBatis 由来の DataSource をCDIで共有

    // ★ DBRider が拾う接続供給口
    public ConnectionHolder connectionHolder() {
        return () -> dataSource.getConnection();
    }

    /**
     * Liquibase でスキーマを初期化
     */
    @BeforeAll
    void setupSchema() throws Exception {
        try (Connection c = dataSource.getConnection()) {
            Database db = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(c));
            try (Liquibase lb = new Liquibase(CHANGELOG_PATH,
                    new ClassLoaderResourceAccessor(), db)) {
                lb.update(new Contexts(), new LabelExpression());
            }
        }
    }

}
