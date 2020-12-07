package net.termat.geo;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * ベクターデータクラス
 *
 * @author t-matsuoka
 * @version 0.1
 *
 */

public class GeojsonData{
	private static GeoJsonReader reader=new GeoJsonReader();
	private static GeoJsonWriter writer=new GeoJsonWriter();
	protected List<Geometry> geometry;
	protected List<Shape> shape;
	protected List<Map<String,Object>> map;
	protected String crs;
	protected String name;

	public GeojsonData(){}

	public void init(String name){
		geometry=new ArrayList<Geometry>();
		shape=new ArrayList<Shape>();
		map=new ArrayList<Map<String,Object>>();
		this.name=name;
	}

	public void addFeauture(Shape sp,Map<String,Object> prop){
		shape.add(sp);
		if(sp instanceof Rectangle2D){
			geometry.add(rectToPoly((Rectangle2D)sp));
		}else{
			geometry.add(shpToPoly(sp));
		}
		map.add(prop);
	}

	/**
	 *
	 * @param geojson GeoJson文字列（座標系は平面直角座標とする）
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public GeojsonData(String geojson)throws Exception{
		Gson gson = new GsonBuilder().create();
		Map<String,Object> mp=gson.fromJson(geojson, Map.class);
		name=mp.get("name").toString();
		crs=gson.toJson(mp.get("crs"));
		List list=(List)mp.get("features");
		map=new ArrayList<Map<String,Object>>();
		geometry=new ArrayList<Geometry>();
		for(Object o : list){
			Map<String,Object> m=(Map<String,Object>)o;
			String json=gson.toJson(m.get("geometry"));
			map.add((Map<String,Object>)m.get("properties"));
			Geometry ge=parseGeoJson(json);
			geometry.add(ge);
		}
		 ShapeWriter writer=new ShapeWriter();
		 shape=new ArrayList<Shape>();
		 for(Geometry g : geometry){
			 shape.add(shpToGp(writer.toShape(g)));
		 }
	}

	/**
	 * バウンディングボックスを取得
	 * @return バウンディングボックス:Rectange2D
	 */
	public Rectangle2D getBounds(){
		Rectangle2D ret=null;
		for(Shape s : shape){
			if(ret==null){
				ret=s.getBounds2D();
			}else{
				ret=ret.createUnion(s.getBounds2D());
			}
		}
		return ret;
	}

	/**
	 * 新規のベクターデータにGeometryを登録
	 *
	 * @param name 名前
	 * @param shapes Shapeオブジェクト(Geometry）のリスト
	 * @param properties Mapオブジェクト（Property）のリスト
	 */
	public void init(String name,List<Shape> shapes,List<Map<String,Object>> properties){
		this.name=name;
		shape=shapes;
		map=properties;
		geometry=new ArrayList<Geometry>();
		ShapeReader sr=new ShapeReader(new GeometryFactory());
		AffineTransform af=AffineTransform.getScaleInstance(1.0, 1.0);
		for(Shape s : shape){
			Geometry g=sr.read(s.getPathIterator(af));
			geometry.add(g);
		}
	}

	/**
	 * GeoJsonファイルをロード
	 *
	 * @param f GeoJsonファイル
	 * @throws ParseException
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void loadGeoJsonFile(File f)throws IOException, ParseException{
		StringBuffer buf=new StringBuffer();
		String line=null;
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
		while((line=br.readLine())!=null){
			buf.append(line+"\n");
		}
		br.close();
		String json=buf.toString();
		Gson gson = new GsonBuilder().create();
		Map<String,Object> mp=gson.fromJson(json, Map.class);
		if(mp.containsKey("name")){
			name=mp.get("name").toString();
		}else{
			name=f.getName();
		}
		if(mp.containsKey("crs")){
			crs=gson.toJson(mp.get("crs"));
		}
		List list=(List)mp.get("features");
		map=new ArrayList<Map<String,Object>>();
		geometry=new ArrayList<Geometry>();
		for(Object o : list){
			Map<String,Object> m=(Map<String,Object>)o;
			String jx=gson.toJson(m.get("geometry"));
			map.add((Map<String,Object>)m.get("properties"));
			Geometry ge=parseGeoJson(jx);
			geometry.add(ge);
		}
		 ShapeWriter writer=new  ShapeWriter();
		 shape=new ArrayList<Shape>();
		 for(Geometry g : geometry){
//			 shape.add(writer.toShape(g));
			 shape.add(shpToGp(writer.toShape(g)));
		 }
	}

	/**
	 * GeoJson文字列を取得
	 *
	 * @return GeoJson文字列:String
	 */
	@SuppressWarnings("unchecked")
	public String getGeoJson(boolean prettyPrinting){
		Gson gson;
		if(prettyPrinting){
			gson=new GsonBuilder().setPrettyPrinting()
					.registerTypeAdapter(Long.class, new JsonDeserializer<Long>() {
			            @Override
			            public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			                return json == null ? null : json.getAsLong();
			            }
			        })
			        .create();
		}else{
			gson=new GsonBuilder()
					.registerTypeAdapter(Long.class, new JsonDeserializer<Long>() {
			            @Override
			            public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			                return json == null ? null : json.getAsLong();
			            }
			        })
			        .create();
		}
		Map<String,Object> root=new HashMap<String,Object>();
		root.put("type","FeatureCollection");
		root.put("name", name);
		if(crs!=null&&!crs.isEmpty()){
			root.put("crs",gson.fromJson(crs, Map.class));
		}
		List<Object> fe=new ArrayList<Object>();
		root.put("features", fe);
		for(int i=0;i<geometry.size();i++){
			Map<String,Object> ob=new HashMap<String,Object>();
			ob.put("type", "Feature");
			ob.put("properties", map.get(i));
			String json=writer.write(geometry.get(i));
			Map<String,Object> mm=gson.fromJson(json, Map.class);
			mm.remove("crs");
			ob.put("geometry", mm);
			fe.add(ob);
		}
		String json=gson.toJson(root);
		return json;
	}

	public String getGeoJsonAtPoint(){
		Gson gson=new GsonBuilder().setPrettyPrinting()
				.registerTypeAdapter(Long.class, new JsonDeserializer<Long>() {
		            @Override
		            public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		                return json == null ? null : json.getAsLong();
		            }
		        })
		        .create();
		Map<String,Object> root=new HashMap<String,Object>();
		root.put("type","FeatureCollection");
		root.put("name", name);
		if(crs!=null&&!crs.isEmpty()){
			root.put("crs",gson.fromJson(crs, Map.class));
		}
		List<Object> fe=new ArrayList<Object>();
		root.put("features", fe);
		for(int i=0;i<geometry.size();i++){
			Map<String,Object> ob=new HashMap<String,Object>();
			ob.put("type", "Feature");
			ob.put("properties", map.get(i));
			Map<String,Object> mm=new HashMap<String,Object>();
			mm.put("type", "Point");
			Rectangle2D c=shape.get(i).getBounds2D();
			mm.put("coordinates", new double[]{c.getX(),c.getY()});
			mm.remove("crs");
			ob.put("geometry", mm);
			fe.add(ob);
		}
		String json=gson.toJson(root);
		return json;
	}

	/**
	 * Geometryに含まれるi番目のShapeオブジェクトを取得
	 *
	 * @param i 番号
	 * @return
	 */
	public Shape getShape(int i){
		return shape.get(i);
	}

	/**
	 * Geometryに含まれるShapeオブジェクトのリストを取得
	 *
	 * @return Shapeオブジェクトのリスト
	 */
	public List<Shape> getShapes(){
		return shape;
	}

	/**
	 * Geometryのリストを取得
	 *
	 * @return Geometryのリスト
	 */
	public List<Geometry> getGeometrys(){
		return geometry;
	}

	/**
	 * Geometryに含まれるi番目のGeometryオブジェクトを取得
	 *
	 * @param i 番号
	 * @return Geometry
	 */
	public Geometry getGeometry(int i){
		return geometry.get(i);
	}

	/**
	 * i番目のGeometryのプロパティを取得
	 *
	 * @param i 番号
	 * @return プロパティ
	 */
	public Map<String,Object> getProperty(int i){
		return map.get(i);
	}

	/**
	 * 全てのプロパティのリストを取得
	 *
	 * @return プロパティのリスト
	 */
	public List<Map<String,Object>> getProperties(){
		return map;
	}

	public static Geometry parseGeoJson(String json)throws IOException,ParseException{
		return reader.read(json);
	}

	/**
	 * ベクターデータのCRS（平面直角座標系）を設定
	 *
	 * @param coodId 平面直角座標系
	 */
	public void setCoordSys(int coodId){
		crs="{ 'type': 'name', 'properties': { 'name': 'urn:ogc:def:crs:EPSG::"+Integer.toString(coodId+6668)+"'}}";
	}

	public static Map<String,Object> getCooedCRS(int coordId){
		Map<String,Object> ret=new HashMap<String,Object>();
		ret.put("type","name");
		Map<String,Object> p=new HashMap<String,Object>();
		ret.put("properties", p);
		String crs="urn:ogc:def:crs:EPSG::"+Integer.toString(coordId+6668);
		p.put("name", crs);
		return ret;
	}

	public String getCrs() {
		return crs;
	}

	public void setCrs(String crs) {
		this.crs = crs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void outputGeoJson(File f,boolean prettyPrinting)throws IOException{
		BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),"UTF-8"));
		bw.write(getGeoJson(prettyPrinting));
		bw.flush();
		bw.close();
	}

	public void removeProperty(String key){
		for(Map<String,Object> obj : map){
			obj.remove(key);
		}
	}

	public void changePropertyName(String src_name,String dst_name){
		for(Map<String,Object> obj : map){
			Object o=obj.remove(src_name);
			obj.put(dst_name, o);
		}
	}

	public void transPropetyValue(String prop_name,Map<Object,Object> o){
		for(Map<String,Object> m : map){
			Object obj=m.get(prop_name);
			if(o.containsKey(obj)){
				m.put(prop_name, o.get(obj));
			}
		}
	}

	public void removeData(int row){
		shape.remove(row);
		geometry.remove(row);
		map.remove(row);
	}

	public void createNewProperty(String name,Object initvalue){
		for(Map<String,Object> m : map){
			m.put(name, initvalue);
		}
	}

	public int getPointContainPolyAtId(Point2D p){
		return getPointContainPolyAtId(p.getX(),p.getY());
	}

	public int getPointContainPolyAtId(double x,double y){
		int ret=-1;
		for(int i=0;i<shape.size();i++){
			Shape sp=shape.get(i);
			if(sp.contains(x, y)){
				ret=i;
				break;
			}
		}
		return ret;
	}

	private static Polygon rectToPoly(Rectangle2D r){
		List<Coordinate> coord=new ArrayList<Coordinate>();
		GeometryFactory gf=new GeometryFactory();
		coord.add(new Coordinate(r.getX(),r.getY()));
		coord.add(new Coordinate(r.getX()+r.getWidth(),r.getY()));
		coord.add(new Coordinate(r.getX()+r.getWidth(),r.getY()+r.getHeight()));
		coord.add(new Coordinate(r.getX(),r.getY()+r.getHeight()));
		coord.add(new Coordinate(r.getX(),r.getY()));
		return gf.createPolygon(coord.toArray(new Coordinate[coord.size()]));
	}

	private static Polygon shpToPoly(Shape gp){
		AffineTransform af=AffineTransform.getScaleInstance(1.0, 1.0);
		PathIterator pi=gp.getPathIterator(af);
		List<Coordinate> coord=new ArrayList<Coordinate>();
		GeometryFactory gf=new GeometryFactory();
		double[] p=new double[6];
		while(!pi.isDone()){
			switch(pi.currentSegment(p)){
				case PathIterator.SEG_MOVETO:
					coord.add(new Coordinate(p[0],p[1]));
					break;
				case PathIterator.SEG_LINETO:
					coord.add(new Coordinate(p[0],p[1]));
					break;
				case PathIterator.SEG_QUADTO:
					coord.add(new Coordinate(p[2],p[3]));
					break;
				case PathIterator.SEG_CUBICTO:
					coord.add(new Coordinate(p[4],p[5]));
					break;
				case PathIterator.SEG_CLOSE:
					Coordinate c=coord.get(0);
					coord.add(new Coordinate(c.x,c.y));
					break;
			}
			pi.next();
		}
		return gf.createPolygon(coord.toArray(new Coordinate[coord.size()]));
	}

	private static GeneralPath shpToGp(Shape sp){
		AffineTransform af=AffineTransform.getScaleInstance(1.0, 1.0);
		PathIterator pi=sp.getPathIterator(af);
		GeneralPath gp=new GeneralPath();
		double[] p=new double[6];
		while(!pi.isDone()){
			switch(pi.currentSegment(p)){
				case PathIterator.SEG_MOVETO:
					gp.moveTo(p[0],p[1]);
					break;
				case PathIterator.SEG_LINETO:
					gp.lineTo(p[0],p[1]);
					break;
				case PathIterator.SEG_CLOSE:
					gp.closePath();
					break;
			}
			pi.next();
		}
		return gp;
	}
}
