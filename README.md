
AndroidDatabaseHelper Ver0.01
=====================

## 概要
AndroidのSQLiteデータベースを操作するクラス郡です。

主要なクラスは以下のとおりです
* Database
 * 1つのデータベースを表現するクラスです
* Table
 * 1つのテーブルを表現するクラスです
* StiTable
 * 1つのテーブルを複数のデータベーステーブルクラスで共有できるクラスです
* DatabaseCriticalSection
 * マルチスレッドのアプリケーションにおいて、データベースへの同時アクセスを抑止する機能です
 * シングルスレッドで使用する場合必要ありません

## 依存
このプロジェクトはAndroidUtilsに依存します。
* https://github.com/m11/AndroidUtils

## データベース構築
### 新規テーブル作成
* DatabaseTableクラスを継承する
* コンストラクタでaddColumnメソッドを呼び、カラムを追加する
 * カラムは任意の型で作成する
 * たとえばStringColumnは文字列のカラムである
 * コンストラクタの引数はカラム名である

```
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import jp.m11.android.androiddatabasehelper.Table;
import jp.m11.android.androiddatabasehelper.column.LongColumn;
import jp.m11.android.androiddatabasehelper.column.StringColumn;

public class TableA extends Table {
	public TableA() {
		this.addColumn( new StringColumn( "title" ) );
		this.addColumn( new StringColumn( "body" ) );
		this.addColumn( new LongColumn( "counter" ) );
	}

	@Override
	public String getTableName() {
		return "table_a";
	}

	@Override
	public void upgrade(Context context, SQLiteDatabase database) {
	}
}
```

### 新規データベース作成
* Databaseクラスを継承する
* コンストラクタでaddTableメソッドを呼び、データベースにテーブルを追加する
* getDatabaseNameをオーバライドし、データベース名を返すようにする
* getVersionをオーバライドし、データベースバージョンを返すようにする

```
import jp.m11.android.androiddatabasehelper.Database;
import android.content.Context;

public class SampleDatabase extends Database {
	public SampleDatabase( Context context ) {
		super( context );
		this.addTable( TableA.class );
		this.addTable( TableB.class );
	}

	@Override
	public String getDatabaseName() {
		return "sample_database";
	}

	@Override
	public int getVersion() {
		return 1;
	}
}
```

### データベースを開く
データベースを書き込み用として開くには以下のように記述します。
読み込み専用で開く場合は、getWritableDatabaseメソッドではなく、getReadableDatabaseメソッドを使用してください。

マルチスレッドでデータベースにアクセスする場合は、次に紹介するデータベースクリティカルセクションの利用をお勧めします。

```
SampleDatabase sampleDatabase = new SampleDatabase( context );
SQLiteDatabase database = null;

database = sampleDatabase.getWritableDatabase();
//ここでデータベースにアクセスする
database.close();
```

### データベースクリティカルセクション
マルチスレッドで同時にアクセスした場合例外が発生するためクリティカルセクションを設けました。
書き込み専用でデータベースを開いた状態で読み込み専用で同時にアクセスしても例外は発生しないようなので、1つのデータベースあたり、読み込み用と書き込み用の2つのクリティカルセクションを用意しています。
使い方は以下のとおりです。
* DatabaseCriticalSectionクラスのgetInstanceメソッドでクリティカルセクションのインスタンスを取得する
* 書き込み用
 * enterWritableメソッドで書き込み用クリティカルセクションに入る
 * leaveWritableメソッドで書き込み用クリティカルセクションから出る
* 読み込み用
 * enterReadableメソッドで読み込み用クリティカルセクションに入る
 * leaveReadableメソッドで書き込み用クリティカルセクションから出る

```
DatabaseCriticalSection cs = DatabaseCriticalSection.getInstance( context, SampleDatabase.class );
SQLiteDatabase database = null;

database = cs.enterWritable( this );
//ここでデータベースにアクセスする
cs.leaveWritable();
```

## レコード操作
ここでは、SQLiteDatabaseのインスタンスはdatabase変数に既に取得しているものとして説明します。

### レコード挿入
レコードの挿入を行うには、まずRecordクラスのインスタンスを作成し、以下に示すいずれかのカラムを取得するメソッドでカラムを取得します。
* getStringColumn
* getLongColumn
* getDoubleColumn
カラムのインスタンスを取得したら、setValueメソッドで値を設定します。
値の設定が終わったらinsertメソッドでレコードのインスタンスをデータベースに挿入します。
ここで、idカラムに自動でレコードIDが割り振られます(AUTOINCREMENT)。

```
Record record = new Record( TableA.class );
record.getStringColumn( "title" ).setValue( "Hello World" );
record.getStringColumn( "body" ).setValue( "test body." );
record.getLongColumn( "counter" ).setValue( 1 );
record.insert( database );
```

### レコード選択
最初のレコードを取得するには以下のように記述します。
```
Record record = new Record( TableA.class );
record.first( database );
```

最後のレコードを取得するには以下のように記述します。
```
Record record = new Record( TableA.class );
record.last( database );
```

ID(ここでは123とします。)を指定してレコードを取得するには以下のように記述します。
```
Record record = new Record( TableA.class );
record.find( database, 123 );
```

特定の条件を指定してレコードを取得するには以下のように記述します。
```
Record record = new Record( TableA.class );
record.find( database, "title = ?", new String[]{ "Hello World" } );
```

## JSON変換
WebAPIなどとの連携に便利なJSON変換機能を有しています。
JSONに変換するにはtoJsonメソッドを、JSONから情報を読み込むにはfromJsonメソッドを使用します。
toJsonメソッドの戻り値はJSONObjectクラスであるため、JSON文字列を取得するにはさらにtoStringメソッドを呼び出してください。

最初のレコードをJSON文字列に変換するには以下のように記述します。
```
Record record = new Record( TableA.class );
String json = null;

record.first( database );
json = record.toJson().toString();
```

JSON文字列から値を読み込むには以下のように記述します。
```
Record record = new Record( TableA.class );
String json = "{\"title\":\"test title\", \"body\": \"body text\", \"counter\":523}";

record.fromJson( json );
```

## 初期レコード設定
標準で、初回のデータベース作成時に初期レコードの投入を行います。
この機能は純粋にデータベースに対し、insertを行っているため、大量の情報を記述すると処理に時間がかかります。
大量の初期レコードが必要な場合は別の方法をご検討ください。
この機能は、開発時、設定情報などの初期レコードの値を容易に定義できるようにするための機能です。

初期レコードはXMLで記述します。
XMLの配置場所は以下の通りです。
```
asests/database/fixtures/{データベース名}/{テーブル名}.xml
```

初期値は以下のように定義します。
カラムのタグを記載しなかった場合はJavaコード中の初期値もしくはnullになります。
```
<?xml version="1.0" encoding="UTF-8"?>
<database_fixtures>
	<table name="table_a">
		<record>
			<title type="String">タイトル1</title>
			<body type="String">本文1</body>
			<counter type="Long">21345</counter>
		</record>
		<record>
			<title type="String">タイトル2</title>
			<body type="String">本文2</body>
			<counter type="Long">21343235</counter>
		</record>
	</table>
</database_fixtures>
```

## ライセンス
未定です。