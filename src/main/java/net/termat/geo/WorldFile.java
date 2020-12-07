package net.termat.geo;

import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorldFile {

	private WorldFile(){}

    public static void outTfw(AffineTransform af,File out)throws IOException{
        BufferedWriter bw=new BufferedWriter(new FileWriter(out));
        bw.write(af.getScaleX()+"\n");
        bw.write(af.getShearX()+"\n");
        bw.write(af.getShearY()+"\n");
        bw.write(af.getScaleY()+"\n");
        bw.write(af.getTranslateX()+"\n");
        bw.write(af.getTranslateY()+"\n");
        bw.close();
    }

    public static AffineTransform loadTFW(File f) throws IOException{
		BufferedReader br=new BufferedReader(new FileReader(f));
		List<Double> dd=new ArrayList<Double>();
		String line=null;
		while((line=br.readLine())!=null){
			double d=Double.parseDouble(line);
			dd.add(d);
		}
		br.close();
		double[] p=new double[dd.size()];
		for(int i=0;i<p.length;i++){
			p[i]=dd.get(i);
		}
		return new AffineTransform(p);
    }
}
