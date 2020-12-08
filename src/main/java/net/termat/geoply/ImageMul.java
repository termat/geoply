package net.termat.geoply;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.termat.geo.WorldFile;
import net.termat.geo.image.ImageUtil;

public class ImageMul {
	public static void main(String[] args){
		try{
			File out=new File(args[0]);
			BufferedImage bi=null;
			AffineTransform af=null;
			for(int i=1;i<args.length;i++){
				File in=new File(args[i]);
				try{
					BufferedImage im=ImageIO.read(in);
					if(bi==null){
						bi=im;
						af=WorldFile.loadTFW(new File(in.getAbsolutePath().replace(".png", ".pgw").replace(".jpg", ".igw")));
					}else{
						bi=ImageUtil.mul(bi, im);
					}
				}catch(IOException ie){
					ie.printStackTrace();
				}
			}
			ImageIO.write(bi, "png", out);
			WorldFile.outTfw(af, new File(out.getAbsolutePath().replace(".png", ".pgw")));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
