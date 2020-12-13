package net.termat.geo;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Geology {
	private static final String map_url="https://gbank.gsj.jp/seamless/v2/api/1.2/map.png?";
	private static final String regend_url="https://gbank.gsj.jp/seamless/v2/api/1.2/legend.json?";

	private static Map<Color,Map<String,Object>> regendMap=getRegendMap();

	private static String getMapRest(double lat1,double lat2,double lon1,double lon2){
		StringBuffer buf=new StringBuffer();
		buf.append("box=");
		buf.append(Double.toString(lat1)+",");
		buf.append(Double.toString(lon1)+",");
		buf.append(Double.toString(lat2)+",");
		buf.append(Double.toString(lon2));
		return buf.toString();
	}

	private static Map<Color,Map<String,Object>> getRegendMap(){
		Map<Color,Map<String,Object>> ret=new HashMap<Color,Map<String,Object>>();
		URL url = Geology.class.getResource("legendCsv.txt");
		try{
			BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream(),"UTF-8"));
			String line=null;
			br.readLine();
			while((line=br.readLine())!=null){
				String[] str=line.split("\t");
				Map<String,Object> prop=new HashMap<String,Object>();
				prop.put("symbol", str[1]);
				prop.put("age", str[5]);
				prop.put("group", str[7]);
				prop.put("lithology", str[9]);
				prop.put("type", new Integer(Integer.parseInt(str[11])));
				int r=Integer.parseInt(str[2]);
				int g=Integer.parseInt(str[3]);
				int b=Integer.parseInt(str[4]);
				ret.put(new Color(r,g,b), prop);
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return ret;
	}

	public static List<Map<String,Object>> getGeologyJson(double lat1,double lat2,double lon1,double lon2,int coordSys)throws IOException{
		BufferedImage img=getGeologyImage(lat1,lat2,lon1,lon2);
		Point2D xy1=LonLatXY.lonlatToXY(coordSys, lon1, lat1);
		Point2D xy2=LonLatXY.lonlatToXY(coordSys, lon2, lat2);
		double xmin=Math.min(xy1.getX(), xy2.getX());
		double xmax=Math.max(xy1.getX(), xy2.getX());
		double ymin=Math.min(xy1.getY(), xy2.getY());
		double ymax=Math.max(xy1.getY(), xy2.getY());
		double width=(double)img.getWidth();
		double height=(double)img.getHeight();
		double sx=(xmax-xmin)/width;
		double sy=(ymax-ymin)/height;
		AffineTransform af=new AffineTransform(new double[]{sx,0,0,-sy,xmin,ymax});
		List<Map<String,Object>>  list=vectorlize(img,af);
		return list;
	}

	public static BufferedImage getGeologyImage(double lat1,double lat2,double lon1,double lon2)throws IOException{
		String url=map_url+getMapRest(lat1,lat2,lon1,lon2)+"&z=13&layer=g";
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        OkHttpClient client = new OkHttpClient();
        Response res = client.newCall(request).execute();
        InputStream is=res.body().byteStream();
        return ImageIO.read(is);
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String,Object>> getRegend(float lat1,float lat2,float lon1,float lon2)throws IOException{
		Gson gson=new Gson();
		String url=regend_url+getMapRest(lat1,lat2,lon1,lon2);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        OkHttpClient client = new OkHttpClient();
        Response res = client.newCall(request).execute();
        List<Map<String,Object>> ll=(List<Map<String,Object>>)gson.fromJson(res.body().charStream(), List.class);
        return ll;
	}

	private static Color[] getColors(BufferedImage img){
		Set<Color> colors=new HashSet<Color>();
		Set<Integer> check=new HashSet<Integer>();
		for(int i=0;i<img.getWidth();i++){
			for(int j=0;j<img.getHeight();j++){
				int tgb=img.getRGB(i, j);
				if(check.contains(tgb))continue;
				Color c=new Color(tgb);
				colors.add(c);
			}
		}
		return colors.toArray(new Color[colors.size()]);
	}

	private static List<Map<String,Object>>  vectorlize(BufferedImage img,AffineTransform af){
		List<Map<String,Object>> ret=new ArrayList<Map<String,Object>>();
		Color[] col=getColors(img);
		for(Color c : col){
			Map<String,Object> prop=null;
			if(regendMap.containsKey(c)){
				prop=regendMap.get(c);
			}else{
				prop=new HashMap<String,Object>();
				prop.put("symbol", "不明");
				prop.put("age", "不明");
				prop.put("group", "その他");
				prop.put("lithology", "その他");
				prop.put("type", 0);
			}
			int[][] check=new int[img.getWidth()][img.getHeight()];
			int ct=0;
			for(int i=0;i<img.getWidth();i++){
				for(int j=0;j<img.getHeight();j++){
					Color c2=new Color(img.getRGB(i, j));
					if(c2.equals(c)){
						check[i][j]=1;
						ct++;
					}
				}
			}
			if(ct==0)continue;
			List<Area> ll=VectorlizeUtility.vectorlize(check);
			Map<String,Object> m=new HashMap<String,Object>();
			m.put("type", "Feature");
			m.put("properties", prop);
			Map<String,Object> ge=new HashMap<String,Object>();
			m.put("geometry", ge);
			ge.put("type","MultiPolygon");
			List<List<List<double[]>>> coord=new ArrayList<List<List<double[]>>>();
			for(Area a : ll){
				coord.add(VectorlizeUtility.coordinates(a, af));
			}
			ge.put("coordinates", coord);
			ret.add(m);
		}
		return ret;
	}
/*
	public static List<Map<String,Object>>  vectorlize(BufferedImage img,List<Map<String,Object>> json,AffineTransform af){
		List<Map<String,Object>> ret=new ArrayList<Map<String,Object>>();
		for(Map<String,Object> o : json){
			int r=((Number)o.get("r")).intValue();
			int g=((Number)o.get("g")).intValue();
			int b=((Number)o.get("b")).intValue();
			String age=(String)o.get("formationAge_ja");
			String geo=(String)o.get("group_ja");
			String lithology=(String)o.get("lithology_ja");
			int[][] check=new int[img.getWidth()][img.getHeight()];
			int ct=0;
			for(int i=0;i<img.getWidth();i++){
				for(int j=0;j<img.getHeight();j++){
					Color c=new Color(img.getRGB(i, j));
					if(c.getRed()==r&&c.getGreen()==g&&c.getBlue()==b){
						check[i][j]=1;
						ct++;
					}
				}
			}
			if(ct==0)continue;
			List<Area> ll=VectorlizeUtility.vectorlize(check);
			Map<String,Object> m=new HashMap<String,Object>();
			m.put("type", "Feature");
			Map<String,Object> p=new HashMap<String,Object>();
			p.put("age", age);
			p.put("geology", geo);
			p.put("lithology", lithology);
			m.put("properties", p);
			Map<String,Object> ge=new HashMap<String,Object>();
			m.put("geometry", ge);
			ge.put("type","MultiPolygon");
			List<List<List<double[]>>> coord=new ArrayList<List<List<double[]>>>();
			for(Area a : ll){
				coord.add(VectorlizeUtility.coordinates(a, af));
			}
			ge.put("coordinates", coord);
			ret.add(m);
		}
		return ret;
	}
*/
}
