package net.termat.geoply;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.termat.geo.WorldFile;
import net.termat.geo.image.CurveImageCreator;
import net.termat.geo.image.ImageUtil;
import net.termat.geo.image.SlopeImageCreator;
import net.termat.geo.tool.GradientFactory;

public class TerrainImageProcesser {

	public static void main(String[] args){
		File f=new File(args[1]);
		File out=new File(args[2]);
		int size=1;
		try{
			size=Integer.parseInt(args[3]);
		}catch(Exception e){}
		try{
			if(args[0].toLowerCase().equals("slope")){
				BufferedImage im=ImageIO.read(f);
				AffineTransform af=WorldFile.loadTFW(new File(f.getAbsolutePath().replace(".png", ".pgw")));
				SlopeImageCreator app=new SlopeImageCreator(im);
				if(size==1){
					app.createImagePixel(Math.abs(af.getScaleX()), Math.abs(af.getScaleY()));
				}else{
					app.createImageCell(size,Math.abs(af.getScaleX()), Math.abs(af.getScaleY()));
				}
				ImageIO.write(app.getImage(), "png", out);
				WorldFile.outTfw(af, new File(out.getAbsolutePath().replace(".png", ".pgw")));
			}else if(args[0].toLowerCase().equals("curveture")){
				BufferedImage im=ImageIO.read(f);
				AffineTransform af=WorldFile.loadTFW(new File(f.getAbsolutePath().replace(".png", ".pgw")));
				CurveImageCreator app=new CurveImageCreator(im,CurveImageCreator.Type.HORIZONTAL);
				if(size==1){
					app.createImagePixel(Math.abs(af.getScaleX()), Math.abs(af.getScaleY()));
				}else{
					app.createImageCell(size,Math.abs(af.getScaleX()), Math.abs(af.getScaleY()));
				}
				ImageIO.write(app.getImage(), "png", out);
				WorldFile.outTfw(af, new File(out.getAbsolutePath().replace(".png", ".pgw")));
			}else if(args[0].toLowerCase().equals("cs")){
				BufferedImage im=ImageIO.read(f);
				AffineTransform af=WorldFile.loadTFW(new File(f.getAbsolutePath().replace(".png", ".pgw")));
				SlopeImageCreator sl=new SlopeImageCreator(im);
				if(size==1){
					sl.createImagePixel(Math.abs(af.getScaleX()), Math.abs(af.getScaleY()));
				}else{
					sl.createImageCell(size,Math.abs(af.getScaleX()), Math.abs(af.getScaleY()));
				}
				int size2=1;
				try{
					size2=Integer.parseInt(args[4]);
				}catch(Exception e){}
				CurveImageCreator cu=new CurveImageCreator(im,CurveImageCreator.Type.HORIZONTAL);
				if(size==1){
					cu.createImagePixel(Math.abs(af.getScaleX()), Math.abs(af.getScaleY()));
				}else{
					cu.createImageCell(size2,Math.abs(af.getScaleX()), Math.abs(af.getScaleY()));
				}
				BufferedImage slope=sl.getImage();
				BufferedImage curve=cu.getImage();
				curve=ImageUtil.galcianFilter5(curve);
				BufferedImage cs=ImageUtil.mul(slope, curve);
				sl.setGradient(GradientFactory.createGradient(new Color[]{Color.WHITE,new Color(255,232,197)}));
				sl.createImagePixel(Math.abs(af.getScaleX()), Math.abs(af.getScaleY()));
				cs=ImageUtil.mul(cs, sl.getImage());
				ImageIO.write(cs, "png", out);
				WorldFile.outTfw(af, new File(out.getAbsolutePath().replace(".png", ".pgw")));
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

}
