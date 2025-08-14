# Database Rider（DBRider）を用いたJUnitの単体テスト自動化

## 環境概要

- Language / Build
  - Java 17
  - Maven
- Database
  - H2DB
- Testing
  - JUnit 5
  - Database Rider（DBRider）
  - Liquibase

### テストツールの役割

- JUnit 5
  - テストの実行基盤（ランナー、アサーション）を提供します。
- [Database Rider（DBRider）](https://database-rider.github.io/getting-started/)
  - テストデータの投入（@DataSet）、DB状態の検証（@ExpectedDataSet）、テスト前後のクリーンアップを行います。
  - データセットはYAML/JSON/CSVなどで記述できます。
- [Liquibase](https://docs.liquibase.com/)
  - スキーマのマイグレーション／バージョニングを行います（DDL適用、差分管理、rollback等）。
  - 実行順序は「Liquibase → DBRider」（まずスキーマを作り、その上にテストデータを載せて検証）とします。

## 必要な依存関係

```xml
<!-- バージョンは最新安定版に読み替え可 -->
<properties>
    <version.junit>5.12.1</version.junit>
    <version.database.rider>1.44.0</version.database.rider>
    <version.liquibase>5.0.1.Final</version.liquibase>
    <version.h2db>2.3.232</version.h2db>
    <version.surefire>3.5.2</version.surefire>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.junit</groupId>
            <artifactId>junit-bom</artifactId>
            <version>${version.junit}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-params</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-engine</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Database Rider（DBRider） -->
    <dependency>
        <groupId>com.github.database-rider</groupId>
        <artifactId>rider-junit5</artifactId>
        <version>${version.database.rider}</version>
        <classifier>jakarta</classifier>
        <scope>test</scope>
    </dependency>

    <!-- Liquibase -->
    <dependency>
        <groupId>org.liquibase</groupId>
        <artifactId>liquibase-core</artifactId>
        <version>${version.liquibase}</version>
        <scope>test</scope>
    </dependency>

    <!-- Database（実際に案件で利用しているDBを指定） -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>${version.h2db}</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>${version.surefire}</version>
        </plugin>
    </plugins>
</build>
```

## 必要な設定ファイル

- **DBRider データセット**  
  例）`src/test/resources/datasets/` 配下に `given-*.yml`（投入用）, `expected-*.yml`（期待値）を配置

- **（任意）DBRider 共通設定 YAML**  
  大文字小文字の扱い、空カラムの許可設定等。  
  例）`src/test/resources/dbunit.yml`

- **Liquibase チェンジログ**  
  例）`src/test/resources/db/changelog/changelog-master.xml`

```txt
# 参考: 配置パス例
src/test/resources/
├─ db/
│  └─ changelog/
│     ├─ db.changelog-master.yaml
│     └─ db.changelog-1.0.0.yaml
├─ datasets/
│  ├─ given-tasks.yml
│  └─ expected-tasks.yml
└─ dbunit.yml
```

### DBRider データセット

```yml
# File: src/test/resources/datasets/given-tasks.yml
# 事前投入データ（@DataSet で読み込む）
Task:
  - title: "task1"
    completed: false
```

```yml
# File: src/test/resources/datasets/expected-tasks.yml
# 期待値データ（@ExpectedDataSet で照合）
Task:
  - title: "task1"
    completed: false
  - title: "task2"
    completed: true
```

### （任意）DBRider 共通設定 YAML

```yml
# File: src/test/resources/dbunit.yml
# DBRider/DBUnit 共通設定
cacheConnection: false

properties:
  caseSensitiveTableNames: false
  allowEmptyFields: true
```

### Liquibase チェンジログ

```yml
# File: src/test/resources/db/changelog/db.changelog-master.yaml
databaseChangeLog:
  - include:
      file: db/changelog/db.changelog-1.0.0.yaml
      relativeToChangelogFile: false
```

```yml
# File: src/test/resources/db/changelog/db.changelog-1.0.0.yaml
databaseChangeLog:
  - property:
      name: schema.name
      value: PUBLIC

  - changeSet:
      id: create-task
      author: you
      changes:
        - createTable:
            schemaName: "${schema.name}"
            tableName: task
            remarks: "Hierarchical tasks"
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: title
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
              - column:
                  name: due_date
                  type: DATE
              - column:
                  name: completed
                  type: BOOLEAN
                  defaultValueBoolean: false
              - column:
                  name: parent_id
                  type: BIGINT

        - addForeignKeyConstraint:
            baseTableSchemaName: "${schema.name}"
            baseTableName: task
            baseColumnNames: parent_id
            referencedTableSchemaName: "${schema.name}"
            referencedTableName: task
            referencedColumnNames: id
            constraintName: fk_task_parent
            onDelete: SET NULL

        - createIndex:
            schemaName: "${schema.name}"
            tableName: task
            indexName: idx_task_parent_id
            columns:
              - column:
                  name: parent_id

      rollback:
        - dropTable:
            schemaName: "${schema.name}"
            tableName: task
```

> Liquibase は db.changelog-master.yaml → db.changelog-1.0.0.yaml の順で読み込み、スキーマとテーブルを作成します。

## テスト用の補助クラス

- **テスト基底クラス**  
  例）`src/test/java/com/example/` 配下にBaseTest.javaを配置
  各テストクラスは、BaseTestを継承

- **（必要に応じて）CDI 用 DataSource プロデューサ**  
  Database RiderおよびLiquibaseの利用にDataSourceが必要でそれのBean登録を行う

### テスト基底クラス

```java
import java.sql.Connection;

import javax.sql.DataSource;
import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.junit5.DBUnitExtension;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;


@ExtendWith({DBUnitExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    private static final String CHANGELOG_PATH = "db/changelog/db.changelog-master.yaml";

    @Inject
    DataSource dataSource;

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
```

### （必要に応じて）CDI 用 DataSource プロデューサ

以下はMybatisを利用した例。

```java
import java.io.Reader;
import javax.sql.DataSource;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.example.task.mybatis.TaskMapper;

@ApplicationScoped
public class TestMyBatisBootstrap {

    private SqlSessionFactory sqlSessionFactory;
    private DataSource dataSource;

    @PostConstruct
    void init() {
        try (Reader r = Resources.getResourceAsReader("META-INF/mybatis-config-test.xml")) {
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(r);
            this.dataSource = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to bootstrap MyBatis", e);
        }
    }

    @Produces 
    @ApplicationScoped
    public DataSource dataSource() { return dataSource; }

    @Produces 
    @ApplicationScoped
    public SqlSessionFactory sqlSessionFactory() { return sqlSessionFactory; }

    @Produces 
    @RequestScoped
    public SqlSession sqlSession() { 
        return sqlSessionFactory.openSession(true);
    }

    public void close(@Disposes SqlSession session) {
        if (session != null) session.close();
    }

}
```

## Database Rider の主要アノテーションの使い方

ここでは **JUnit 5 + H2 + Liquibase + Database Rider（DBRider）** を前提に、  
テスト実装でよく使うアノテーションをまとめます。

> - 事前準備  
>   - スキーマは Liquibase で用意（`@BeforeAll` 等で実行）  
>   - DBRider の共通設定は `src/test/resources/dbunit.yml` に記述（推奨）  
>   - データセットは `src/test/resources/datasets/` 配下に配置

---

### 1) `@DBRider` — DBRider を有効化

テストクラスに付与すると、DBRider の JUnit 5 拡張が有効になり、  
`@DataSet` / `@ExpectedDataSet` 等が利用可能になります。

```java
// src/test/java/.../UserRepositoryTest.java
import com.github.database.rider.junit5.api.DBRider;

@DBRider
class UserRepositoryTest {
  // 個々のテストで @DataSet や @ExpectedDataSet を利用
}
```

> メモ：DB への接続は ConnectionHolder（または DataSource 経由）で提供します。
すでに Liquibase 実行用に JDBC URL を持っているなら、同じものを使い回すのが簡単です。

### 2) `@DataSet` — 事前データの投入

最も使用頻度が高いアノテーションです。テスト開始前に DB に既知の状態を作ります。

```java
// 例: users テーブルに1件投入し、その後テストコードで追加・更新・削除を行う
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;

@DBRider
class UserRepositoryTest extends JdbcBootstrap {

  @Test
  @DataSet(value = "datasets/given-users.yml", cleanBefore = true) // 先にクリアしてから投入
  void findById_returnsSeededUser() {
    var user = repository.findById(1L);
    assertEquals("Alice", user.getName());
  }

```

> - よく使う指定
>   - value：読み込むデータセットファイル（クラスパス基準）
>   - cleanBefore：投入前に表をクリーン（テスト独立性が高い）
>   - cleanAfter：テスト後にクリーン（テスト間の汚染を避ける）
>   - strategy：投入戦略（デフォルトは概ね CLEAN_INSERT 相当）
>   - INSERT / REFRESH / UPDATE 相当の戦略も指定可能
> - ベストプラクティス
>   - テストは独立させる：cleanBefore = true を基本とし、副作用を遮断
>   - データセットは最小限に：テストに必要な列・行のみ記載（過剰な列はメンテコスト増）

### 3) `@ExpectedDataSet` — 実行後の状態を丸ごと照合

テスト実行後の DB の状態を宣言的に検証します。
可変な列（created_at など）はデータセットに書かないか、ignoreCols で無視します。

```java
import com.github.database.rider.core.api.dataset.ExpectedDataSet;

@DBRider
class UserRepositoryInsertTest extends JdbcBootstrap {

  @Test
  @DataSet(value = "datasets/given-users.yml", cleanBefore = true)
  @ExpectedDataSet(value = "datasets/expected-users.yml", orderBy = "id")
  void insert_increasesRowAndMatchesExpected() {
    // given-users.yml で 1件投入済み。ここで2件目を追加する想定。
    repository.insert(new User(null, "Bob", "bob@example.com"));

    // JPA を使う場合はここで flush() を忘れないこと
    // entityManager.flush();
  }
}
```

> - よく使う指定
>   - value：期待値データセットファイル
>   - orderBy：比較時の並び順（不安定な順序を安定化）
> - Tips
>   - 期待値は最終状態のみを記述（差分ではなくスナップショット）
>   - 不要列は書かないのが基本

### 4) `@ExportDataSet` — DB の状態をファイルに出力（フィクスチャ生成に便利）

手作業で YAML を書く代わりに、実 DB の状態をそのままエクスポートできます。
最初のテスト作成や「正解データのひな形」作成に便利です。

```java
import com.github.database.rider.core.api.dataset.ExportDataSet;

@DBRider
class SnapshotTest {

  @Test
  @DataSet(value = "datasets/given-users.yml", cleanBefore = true)
  @ExportDataSet(outputName = "target/datasets/snapshot-after-insert.yml")
  void snapshot_afterInsert() {
    repository.insert(new User(null, "Carol", "carol@example.com"));
    // テスト終了時の DB 状態が target/datasets/snapshot-after-insert.yml に書き出される
  }
}
```

> 使いどころ
> 「まずは適当な操作をして正解スナップショットを自動採取」→後から要らない列を削って**期待値（expected-*.yml）** に流用

### 5) よくある失敗と回避策

#### a. 大文字・小文字・スキーマ不一致でテーブルが見つからない

dbunit.yml で caseSensitiveTableNames: false、schema: PUBLIC を設定（H2 例）

以後、データセット側は users: のように小文字で統一できる

#### b. 並び順の違いで比較が落ちる

@ExpectedDataSet(orderBy = "id") を付与（複合キーなら "id, created_at" のように列名カンマ区切り）

#### c. 可変列（タイムスタンプ等）で毎回失敗

期待値に列を書かない。

または @ExpectedDataSet(ignoreCols = "created_at") を利用

#### d. JPA で比較前に反映されていない

比較直前に entityManager.flush() を呼ぶ

トランザクション境界（テストランナーや拡張の設定）にも注意

#### e. テスト間のデータ汚染

基本は cleanBefore = true

必要に応じて cleanAfter = true も追加
