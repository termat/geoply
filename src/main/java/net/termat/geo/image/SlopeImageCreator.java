package net.termat.geo.image;

import java.awt.Color;
import java.awt.image.BufferedImage;

import net.termat.geo.tool.Gradient;
import net.termat.geo.tool.GradientFactory;
import net.termat.geo.tool.Range;

/**
 * 傾斜量図作成クラス
 * @author t-matsuoka
 * @version 0.50
 */
public class SlopeImageCreator extends AbstractGeoImageCreator{
	private Gradient grad;
	private Range range;

	/**
	 *
	 * @param dem 標高PNG
	 */
	public SlopeImageCreator(BufferedImage dem){
		super(dem);
		super.setBackGroundColor(Color.WHITE);
		grad=GradientFactory.createGradient(new Color[]{Color.WHITE,new Color(128,128,128)});
		range=new Range(0,60);
	}

	public void setGradient(Gradient g){
		grad=g;
	}

	/**
	 * ピクセル単位での作成
	 * @param dx x方向解像度（dx(m)/pixel）
	 * @param dy y方向解像度（dy(m)/pixel）
	 */
	public void createImagePixel(double dx,double dy){
		double v=0;
		double ii=0;
		double n=Math.floor((dem.length-1)*(dem[0].length-1)/10)*10;
		for(int i=1;i<dem.length-1;i=i+1){
			for(int j=1;j<dem[i].length-1;j=j+1){
				v=((++ii)/n)*100;
				if(v%10==0)update("進捗："+v+"%");
				double[][] px=getPixelData(i,j);
				double sv=getSlopeVal(px,dx,dy);
				double deg=Math.toDegrees(Math.atan(sv));
				int rgb=grad.getColorByInt(range.getNormalValue(deg));
				image.setRGB(i, j, rgb);
			}
		}
	}

	/**
	 * メッシュ単位での作成
	 * @param size メッシュサイズ
	 * @param dx x方向解像度（dx(m)/pixel）
	 * @param dy y方向解像度（dy(m)/pixel）
	 */
	public void createImageCell(int size,double dx,double dy){
		int ww=dem.length;
		int hh=dem[0].length;
		Cell[][] cell=new Cell[ww/size][hh/size];
		int x=0;
		int y=0;
		dx=dx*size;
		dy=dy*size;
		for(int i=0;i<cell.length;i++){
			for(int j=0;j<cell[i].length;j++){
				cell[i][j]=new Cell();
				cell[i][j].x=new int[size];
				cell[i][j].y=new int[size];
				for(int m=0;m<size;m++){
					cell[i][j].y[m]=y;
					y=(y+1)%hh;
				}
				for(int m=0;m<size;m++){
					cell[i][j].x[m]=x+m;
				}
				if(y==0)x=x+size;
			}
		}
		for(int i=1;i<cell.length-1;i++){
			for(int j=1;j<cell[i].length-1;j++){
				double[][] px=getPixelDataCell(i,j,cell);
				double sv=getSlopeVal(px,dx,dy);
				double deg=Math.toDegrees(Math.atan(sv));
				int rgb=grad.getColorByInt(range.getNormalValue(deg));
				cell[i][j].setRGB(rgb);
			}
		}
	}


	private double getSlopeVal(double[][] p,double dx,double dy){
		double sx=(p[0][0]+p[1][0]+p[2][0]-(p[0][2]+p[1][2]+p[2][2]))/(6*dx);
		double sy=(p[0][0]+p[0][1]+p[0][2]-(p[2][0]+p[2][1]+p[2][2]))/(6*dy);
		return Math.sqrt(sx*sx+sy*sy);
	}
}
