package net.termat.geoply;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.termat.geo.WorldFile;
import net.termat.geo.image.DemTinInterpolation;
import net.termat.geo.image.ImageProcessingObserver;

public class DEMInterpoler {

	public static void main(String[] args){
		File ref=new File(args[0]);
		File out=new File(args[1]);
		List<File> list=new ArrayList<File>();
		for(int i=2;i<args.length;i++){
			File f=new File(args[i]);
			list.add(f);
		}
		try{
			BufferedImage img=ImageIO.read(ref);
			AffineTransform af=WorldFile.loadTFW(new File(ref.getAbsolutePath().replace(".png", ".pgw").replace(".jpg", ".jgw")));
			DemTinInterpolation app=new DemTinInterpolation(img,af);
			app.addObserver(new ImageProcessingObserver());
			for(File f : list){
				try{
					BufferedImage dem=ImageIO.read(f);
					AffineTransform at=WorldFile.loadTFW(new File(f.getAbsolutePath().replace(".png", ".pgw")));
					app.addDem(dem, at);
				}catch(Exception e){}
			}
			app.process();
			ImageIO.write(app.getImage(), "png", out);
			WorldFile.outTfw(af, new File(out.getAbsolutePath().replace(".png", "pgw")));
		}catch(IOException e){
			e.printStackTrace();
		}

	}

}
