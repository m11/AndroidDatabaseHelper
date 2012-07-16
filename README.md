AndroidDatabaseHelper
=====================

## 概要
AndroidのSQLiteデータベースを使いやすくしてみたい！
ということではじめてみました。

## 新規テーブル作成
* DatabaseTableクラスを継承する
* コンストラクタでaddColumnメソッドを呼び、カラムを追加する
 * カラムは任意の型で作成する
 * たとえばStringColumnは文字列のカラムである
 * コンストラクタの引数はカラム名である

```
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import jp.m11.android.androiddatabasehelper.DatabaseTable;
import jp.m11.android.androiddatabasehelper.column.LongColumn;
import jp.m11.android.androiddatabasehelper.column.StringColumn;

public class TableA extends DatabaseTable {
	public final static String TABLE_NAME = "TableA";

	public TableA() {
		this.addColumn( new StringColumn( "title" ) );
		this.addColumn( new StringColumn( "body" ) );
		this.addColumn( new LongColumn( "counter" ) );
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	public void upgrade(Context context, SQLiteDatabase database) {
	}
}
```

## 新規データベース作成
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

## データベースクリティカルセクション
マルチスレッドで同時にアクセスした場合例外が発生するためクリティカルセクションを設けました。
書き込み専用でデータベースを開いた状態で読み込み専用で同時にアクセスしても例外は発生しないようなので、1つのデータベースあたり、読み込み用と書き込み用の2つのクリティカルセクションを用意しました。
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
