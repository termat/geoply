# geoply
地理院タイルから三次元地形モデルを生成するコマンドラインツールです。

### TileImageGetter
Geojsonで指定した範囲の地理院タイル画像を取得・結合するツール
以下のコマンドで実行します。

>java -classpath geoply-jar-with-dependencies.jar net.termat.geoply.TileImageGetter <jsonファイルのpath>

なお、取得条件はjsonで指定します。
(例)  
```
{
	"num":2,   //平面直角座標系のNo.
	"geojson":"C:/Workspace/java/geoply/aso.geojson",    //取得する領域のgeojson
	"zoom":15,  //取得する画像のズームレベル
	"url":["https://cyberjapandata.gsi.go.jp/xyz/dem5a_png/",  //取得するタイルのurlのリスト
			"https://cyberjapandata.gsi.go.jp/xyz/dem5a_png/",　　 //上位URLのタイル画像が無い場合は下位URLを検索する
			"https://cyberjapandata.gsi.go.jp/xyz/dem5a_png/"],
	"out":"C:/Workspace/java/geoply/",   //出力先
	"name":"dem",  //出力ファイル名のprefix
	"ext":"png"    //取得するタイル画像の拡張子
}
```  
### DEMInterpoler
TileImageGetterで取得したDEM画像（標高PNG）を指定の参照ファイル（及びワールドファイル）と同じ解像度に補間します。
以下のコマンドで実行します。

>java -classpath geoply-jar-with-dependencies.jar net.termat.geoply.DEMInterpoler <参照ファイルのpath> <出力先のpath> <読み込むDEM画像>・・・

### TerrainImageProcesser
TileImageGetterで取得したDEM画像（標高PNG）から、傾斜量図、曲率図、CS立体図を生成します。
以下のコマンドで実行します。<作成画像>は[slope.curve,cs]のいずれか入力します。

>java -classpath geoply-jar-with-dependencies.jar net.termat.geoply.TerrainImageProcesser <作成画像> <DEM画像のpath> <出力先のpath>

### PlyCreator
DEM画像とテクスチャ画像から三次元地形モデル（PLY形式）を生成します。
なお、DEM画像とテクスチャ画像は同サイズで、同じ値のワールドファイル（アフィン変換）を持つ必要があります。

>java -classpath geoply-jar-with-dependencies.jar net.termat.geoply.PlyCreator <テクスチャ画像のpath> <DEM画像のパス> <出力先のpath>

### 依存ライブラリ
JTS https://github.com/locationtech/jts
TINFOUR https://github.com/gwlucastrig/Tinfour
GSON https://github.com/google/gson

