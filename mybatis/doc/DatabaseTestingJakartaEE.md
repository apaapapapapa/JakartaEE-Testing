# Jakarta EE における JUnit 単体テストの手法

Jakarta EE を JUnit でユニットテストする場合、そのままでは CDI などのコンテナ機能は起動しません。そこで次のいずれかで補います。

- **手動 DI：** テスト側でオブジェクトを生成し、コンストラクタインジェクション等で対象クラスに渡す

- **CDI 起動系 OSS：** Weld JUnit5 や CDI-Unit などで 実際に CDI コンテナを起動し、@Inject を再現する（大半は OSS がインスタンス化を担うが、一部はテスト用の補助実装が必要）

小さなクラス（ドメインロジック中心）なら前者で十分なことが多い一方、@Produces/インターセプタ/イベントやコンテナ管理トランザクションを扱う場合は後者が有効です。

いずれの方法でも、コンテナが提供する資源の代替（例：EntityManager や DataSource のテスト用プロデューサ、@Transactional 相当のトランザクション境界、FacesContext のスタブなど）をテスト側で用意する必要があります。

本ページでは、この「テスト用の代替実装（スタブ/モック/プロデューサ等）」の設計と実装にフォーカスして解説します。

## Mapper 編

MyBatis の Mapper は SqlSession から生成されます。
したがってテストでも、以下のどちらかのルートで「正しい SqlSession」を用意し、そこから mapper = session.getMapper(XxxMapper.class) を得るのが本筋です。

- **A. 手動DI（最小構成）：** テスト側で SqlSessionFactory と SqlSession を直接用意して Mapper を取得
- **B. CDI起動（Weld JUnit5）：** CDI の @Produces で SqlSession／Mapper を供給し、@Inject で受け取る

以降、両アプローチを解説します。

### A. 手動DIでの最小構成

**ポイント**

- ユニット寄りのテスト（SQLは薄め、ロジック中心）や、CDIを起動したくないケースに向きます。
- SqlSessionFactory は使い回し、SqlSession はテストごとに新規作成→クローズが原則。
- トランザクション制御はテストスコープで明示（openSession(false)＋rollback() など）。
  - ただし@Transactional等を用いている場合はコミットやロールバックはそちらの機能に移譲されるため、その場合、以下の実装はあくまでSQLの妥当性を確認するテストと考える（Transaction観点は別で紹介）

例：JUnit 5 + H2（インメモリ）

```java
package com.example.task.mybatis;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import javax.sql.DataSource;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;
import org.junit.jupiter.api.*;

class TaskMapperManualTest {

    static SqlSessionFactory factory;
    static DataSource dataSource;

    SqlSession session;
    TaskMapper mapper;

    @BeforeAll
    static void setupFactory() throws Exception {
        try (Reader r = Resources.getResourceAsReader("META-INF/mybatis-config-test.xml")) {
            factory = new SqlSessionFactoryBuilder().build(r);
            dataSource = factory.getConfiguration().getEnvironment().getDataSource();
        }
        // ★ スキーマ作成や初期データ投入はここ（JDBCやLiquibase/DBRider等）
    }

    @BeforeEach
    void openSession() {
        // 自動コミットON
        session = factory.openSession(true);
        mapper = session.getMapper(TaskMapper.class);
    }

    @AfterEach
    void tearDown() {
        if (session != null) {
            //session.rollback();
            session.close();
        }
    }

    @Test
    void findById_returnsTask() {
        // 例: 事前投入済みのID=1を想定
        var task = mapper.findById(1);
        assertNotNull(task);
        assertEquals(1, task.getId());
    }
}
```

最小限の MyBatis 設定（例）：

```xml
<!-- META-INF/mybatis-config-test.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
  <environments default="development">
    <environment id="development">
      <transactionManager type="JDBC"/>
      <dataSource type="POOLED">
        <property name="driver" value="org.h2.Driver"/>
        <property name="url"
                  value="jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MYSQL;USER=sa;PASSWORD="/>
      </dataSource>
    </environment>
  </environments>
  <mappers>
    <mapper class="com.example.task.mybatis.TaskMapper"/>
  </mappers>
</configuration>

```

> SQLを検証したい“Mapperテスト”では本物の DB/DDL/データ投入が必要です（Mockito 等で TaskMapper をモックしても SQL は検証できません）。

### B. CDIを起動して @Inject で受け取る（Weld JUnit5）

コンテナ挙動に寄せたい／インジェクションを広く再現したい場合、CDI を起動します。
以下はご提示の TestMyBatisBootstrap をそのまま使う前提です。

```java
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

    @Produces @ApplicationScoped
    public DataSource dataSource() { return dataSource; }

    @Produces @ApplicationScoped
    public SqlSessionFactory sqlSessionFactory() { return sqlSessionFactory; }

    @Produces @RequestScoped
    public SqlSession sqlSession() { return sqlSessionFactory.openSession(true); }

    public void close(@Disposes SqlSession session) {
        if (session != null) session.close();
    }

    // ★ 具体型で返す（CDIは型変数Producer不可）
    @Produces
    public TaskMapper taskMapper(SqlSession session) {
        return session.getMapper(TaskMapper.class);
    }
}
```

Weld JUnit5 での起動例

```java
package com.example.task.mybatis;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.ibatis.session.SqlSession;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

@ExtendWith(WeldJunit5Extension.class)
class TaskMapperCdiTest {

    // TestMyBatisBootstrap の @Produces 群を読み込む
    @WeldSetup
    WeldInitiator weld = WeldInitiator
            .from(TestMyBatisBootstrap.class)
            .activate(RequestScoped.class, ApplicationScoped.class)
            .build();

    @Inject
    TaskMapper mapper; // ★ そのまま注入できる

    // 必要ならセッションを受け取って明示制御も可能
    @Inject
    SqlSession session;

    @BeforeEach
    void before() {
        // ★ init データ投入（JDBC/DBRider/Liquibaseなど）
    }

    @AfterEach
    void after() {
        // openSession(true) の場合は明示制御不要。
        // ロールバック方式にしたいなら openSession(false) に変え、
        // ここで session.rollback() する。
    }

    @Test
    void select_basic() {
        var task = mapper.findById(1);
        assertNotNull(task);
    }
}
```

#### 設計の要点（この @Produces 群が解決していること）

- SqlSessionFactory は重いので @ApplicationScoped で1つだけ作る。
- SqlSession はスレッドセーフでないため共有しない：@RequestScoped で「テスト1ケース ≒ 1セッション」にする。
- @Disposes で必ずクローズ。Weld JUnit5 では RequestScoped の終了時に呼ばれ、リークを防げる。
- TaskMapper は **「具体のインタフェース型」**で @Produces。（CDI は型変数を戻す Producer を受け付けません）

### どちらを選ぶ？（使い分け早見表）

| 観点 | 手動DI | CDI起動（Weld JUnit5） |
| --- | --- | --- |
| セットアップの軽さ | ◎ | ○ |
| コンテナに近い挙動（`@Inject` 再現など） | △ | ◎ |
| トランザクション/スコープの再現度 | △ | ○〜◎ |
| 学習コスト | 低 | 中 |
| 適性 | 単純な Mapper 検証／高速な小粒テスト | 実運用に近い配線／他 Bean と併用 |

### よくある落とし穴と回避策

- SqlSession を @ApplicationScoped で共有しない
  - 👉 セッションはスレッドセーフではありません。@RequestScoped or テストメソッド内ローカルで。

- セッションを閉じ忘れる
  - 👉 手動DIでは try/finally で close()。CDIでは @Disposes を用意し、RequestScoped を有効化。
- TaskMapper を Mockito でモックして SQL を“テストした気”になる
  - 👉 それはMapper利用側のロジックのテストです。SQLやマッピングを確認したいなら実DB＋本物の Mapper を使う。
- オートコミットのままデータを汚す
  - 👉 ロールバック方式（openSession(false)）にするか、DatabaseRider等を用いてテスト用DBをテストごと再作成する戦略に。

## JPA 編

### @Transactional
