# command

mvn clean install -Pwildfly-bootable-jar

# Coverage (JaCoCo)

カバレッジレポートの生成・確認手順:

1. カバレッジレポート生成

```sh
mvn clean test jacoco:report
```

2. レポートの確認

`target/site/jacoco/index.html` をブラウザで開くと、テストカバレッジの詳細なHTMLレポートが確認できます。
