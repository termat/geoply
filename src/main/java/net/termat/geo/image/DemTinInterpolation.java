package net.termat.geo.image;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.tinfour.common.Vertex;
import org.tinfour.interpolation.TriangularFacetInterpolator;
import org.tinfour.interpolation.VertexValuatorDefault;
import org.tinfour.standard.IncrementalTin;

import net.termat.geo.ElevationPng;

/**
 * 標高PNGの欠測地点のTIN補間を実行するクラス
 * @author t-matsuoka
 * @version 0.5
 * @see dependancy:Tinfour https://github.com/gwlucastrig/Tinfour
 */
public class DemTinInterpolation  extends Observable implements ImageProcessingObservable{
	private BufferedImage img;
	private AffineTransform af;
	private Rectangle2D rect;
	private IncrementalTin tin;
	private String message;
	private List<Vertex> list;
	private int index=0;

	/**
	 *
	 * @param png 標高PNG
	 */
	public DemTinInterpolation(BufferedImage ref,AffineTransform a){
		img=new BufferedImage(ref.getWidth(),ref.getHeight(),BufferedImage.TYPE_INT_RGB);
		setInitVal(img);
		af=a;
		Point2D topLeft=af.transform(new Point2D.Double(-20,-20), new Point2D.Double());
		Point2D bottomRight=af.transform(new Point2D.Double(img.getWidth()+40,img.getHeight()+40), new Point2D.Double());
		double xx=Math.min(topLeft.getX(), bottomRight.getX());
		double yy=Math.min(topLeft.getY(), bottomRight.getY());
		double ww=Math.max(topLeft.getX(), bottomRight.getX())-xx;
		double hh=Math.max(topLeft.getY(), bottomRight.getY())-yy;
		rect=new Rectangle2D.Double(xx,yy,ww,hh);
		list=new ArrayList<Vertex>();
	}

	public void addDem(BufferedImage dem,AffineTransform a){
		for(int i=0;i<dem.getWidth();i++){
			for(int j=0;j<dem.getHeight();j++){
				int rgb=dem.getRGB(i, j);
				if(rgb==ElevationPng.NA)continue;
				double zz=ElevationPng.getZ(rgb);
				if(Double.isNaN(zz))continue;
				Point2D pt=a.transform(new Point2D.Double(i,j), new Point2D.Double());
				if(rect.contains(pt)){
					Vertex v=new Vertex(pt.getX(),pt.getY(),zz,index++);
					list.add(v);
				}
			}
		}
		update("測点数："+Integer.toString(index)+"点");
	}

	/**
	 * 補間処理を実行
	 */
	public void process(){
		update("Delaunay分割中");
		tin=new IncrementalTin();
		tin.add(list, null);
		update("Delaunay分割終了");
		createImage();
	}

	/**
	 * 補間後の標高PNGを取得
	 * @return
	 */
	private void createImage(){
		TriangularFacetInterpolator tfi=new TriangularFacetInterpolator(tin);
		VertexValuatorDefault vvd=new VertexValuatorDefault();
		double n=img.getWidth()*img.getHeight();
		double ii=0;
		for(int i=0;i<img.getWidth();i++){
			for(int j=0;j<img.getHeight();j++){
				if((++ii/n*100)%10==0)update("補間進捗:"+Double.toString(ii/n*100)+"%");
				Point2D p=af.transform(new Point2D.Double(i,j), new Point2D.Double());
				double hh=tfi.interpolate(p.getX(), p.getY(), vvd);
				if(hh>=0)img.setRGB(i, j, ElevationPng.getRGB(hh));
			}
		}
	}

	public BufferedImage getImage(){
		return img;
	}

	private void setInitVal(BufferedImage img){
		for(int i=0;i<img.getWidth();i++){
			for(int j=0;j<img.getHeight();j++){
				img.setRGB(i, j, ElevationPng.NA);
			}
		}
	}

	private void update(String mes){
		message=mes;
		setChanged();
		notifyObservers();
	}

	@Override
	public String progress() {
		return message;
	}

}
