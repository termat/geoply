package net.termat.geoply;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.termat.geo.Geology;
import net.termat.geo.LonLatXY;
import net.termat.geo.WorldFile;
import net.termat.geo.image.ImageUtil;

public class GeologyGetter {

	public static void main(String[] args){
		Integer num=Integer.parseInt(args[0]);
		File in=new File(args[1]);
		File out=new File(args[2]);
		try{
			BufferedImage img=ImageIO.read(in);
			AffineTransform af=WorldFile.loadTFW(new File(in.getAbsolutePath().replace(".png", ".pgw").replace(".jpg", ".jgw")));
			Point2D p1=af.transform(new Point2D.Double(0,0), new Point2D.Double());
			Point2D p2=af.transform(new Point2D.Double(img.getWidth(),img.getHeight()), new Point2D.Double());
			double minx=Math.min(p1.getX(), p2.getX());
			double maxx=Math.max(p1.getX(), p2.getX());
			double miny=Math.min(p1.getY(), p2.getY());
			double maxy=Math.max(p1.getY(), p2.getY());
			Point2D ll1=LonLatXY.xyToLonlat(num, minx, miny);
			Point2D ll2=LonLatXY.xyToLonlat(num, maxx, maxy);
			BufferedImage bi=Geology.getGeologyImage(
					Math.min(ll1.getY(), ll2.getY()),
					Math.max(ll1.getY(), ll2.getY()),
					Math.min(ll1.getX(), ll2.getX()),
					Math.max(ll1.getX(), ll2.getX()));
			bi=ImageUtil.scale(bi, (double)img.getWidth()/(double)bi.getWidth(), (double)img.getHeight()/(double)bi.getHeight(),Color.BLACK);
			ImageIO.write(bi, "png", out);
			WorldFile.outTfw(af, new File(out.getAbsolutePath().replace(".png", ".pgw")));
			List<Map<String,Object>> map=Geology.getRegend(
					(float)Math.min(ll1.getY(), ll2.getY()),
					(float)Math.max(ll1.getY(), ll2.getY()),
					(float)Math.min(ll1.getX(), ll2.getX()),
					(float)Math.max(ll1.getX(), ll2.getX()));
			Gson gson=new GsonBuilder().setPrettyPrinting().create();
			BufferedWriter bw=new BufferedWriter(new FileWriter(new File(out.getAbsolutePath().replace(".png", ".json"))));
			bw.write(gson.toJson(map));
			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
