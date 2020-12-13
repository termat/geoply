package net.termat.geo;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class VectorlizeUtility {

	public static List<Area> vectorlize(int[][] map){
		List<Area> list=new ArrayList<Area>();
		for(int i=0;i<map.length;i++){
			List<Rectangle2D> rect=new ArrayList<Rectangle2D>();
			for(int j=0;j<map[i].length;j++){
				if(map[i][j]==1){
					Rectangle2D r=new Rectangle2D.Double(i,j,1.00001,1.00001);
					if(j>0&&map[i][j-1]==1){
						rect.get(rect.size()-1).add(r);
					}else{
						rect.add(r);
					}
				}
			}
			if(list.size()==0){
				for(Rectangle2D r:rect){
					Area a=new Area(r);
					list.add(a);
				}
			}else{
				for(Rectangle2D r: rect){
					boolean flg=true;
					for(Area a:list){
						if(contain(a,r)){
							a.add(new Area(r));
							flg=false;
							break;
						}
					}
					if(flg){
						list.add(new Area(r));
					}
				}
			}
		}
		return list;
	}

	private static boolean contain(Shape sp,Rectangle2D r){
		if(sp.intersects(r))return true;
		if(sp.contains(new Point2D.Double(r.getX(),r.getY())))return true;
		if(sp.contains(new Point2D.Double(r.getX()+r.getWidth(),r.getY())))return true;
		if(sp.contains(new Point2D.Double(r.getX()+r.getWidth(),r.getY()+r.getHeight())))return true;
		if(sp.contains(new Point2D.Double(r.getX(),r.getY()+r.getHeight())))return true;

		return false;
	}

	public static List<List<double[]>> coordinates(Shape sp,AffineTransform af){
		List<List<double[]>> ret=new ArrayList<List<double[]>>();
		PathIterator pi=sp.getPathIterator(af);
		double[] p=new double[6];
		List<double[]> tmp=new ArrayList<double[]>();
		while(!pi.isDone()){
			switch(pi.currentSegment(p)){
				case PathIterator.SEG_MOVETO:
				case PathIterator.SEG_LINETO:
				case PathIterator.SEG_QUADTO:
				case PathIterator.SEG_CUBICTO:
					tmp.add(new double[]{p[0],p[1]});
					break;
				case PathIterator.SEG_CLOSE:
					tmp.add(tmp.get(0));
					ret.add(tmp);
					tmp=new ArrayList<double[]>();
					break;
			}
			pi.next();
		}
		return ret;
	}
}
