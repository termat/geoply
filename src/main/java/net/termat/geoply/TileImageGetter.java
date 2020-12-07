package net.termat.geoply;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.locationtech.jts.io.ParseException;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import net.termat.geo.GeojsonData;
import net.termat.geo.TileReader;
import net.termat.geo.WorldFile;

public class TileImageGetter {
	private BufferedImage img;
	private AffineTransform af;

	public TileImageGetter(int num,String geojson_path,int zoom,String[] url,String ext){
		try{
			GeojsonData gd=loadGeoJson(new File(geojson_path));
			Rectangle2D rect=gd.getBounds();
			img=gerImage(num,zoom,url,rect,ext);
			af=createTransform(rect,img);
		}catch(IOException ie){
			ie.printStackTrace();
		}catch(ParseException pe){
			pe.printStackTrace();
		}
	}

	public void out(File img_path,File af_path) throws IOException{
		ImageIO.write(img, "png", img_path);
		WorldFile.outTfw(af,af_path);
	}

	private GeojsonData loadGeoJson(File f) throws IOException, ParseException{
		GeojsonData vec=new GeojsonData();
		vec.loadGeoJsonFile(f);
		return vec;
	}

	private AffineTransform createTransform(Rectangle2D rect,BufferedImage img){
		return TileReader.createTfwTransform(rect, img);
	}

	public static BufferedImage gerImage(int num,int zoom,String[] url,Rectangle2D rect,String ext) throws IOException{
		BufferedImage img=TileReader.getGSIImage(url,rect,num,zoom,ext);
		return img;
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args){
		File f=new File(args[0]);
		Gson gson=new Gson();
		try {
			Map<String,Object> map=gson.fromJson(new FileReader(f), Map.class);
			int num=((Number)map.get("num")).intValue();
			String geojson=(String)map.get("geojson");
			int zoom=((Number)map.get("zoom")).intValue();
			List<String> url=((List<String>)map.get("url"));
			String out=(String)map.get("out");
			String name=(String)map.get("name");
			String ext=(String)map.get("ext");
			TileImageGetter app=new TileImageGetter(num,geojson,zoom,url.toArray(new String[url.size()]),ext);
			File img=new File(out+"/"+name+".png");
			File af=new File(out+"/"+name+".pgw");
			app.out(img, af);
		} catch (JsonSyntaxException | JsonIOException | IOException e) {
			e.printStackTrace();
		}
	}


}
