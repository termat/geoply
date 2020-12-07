package net.termat.geo.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import net.termat.geo.tool.Gradient;
import net.termat.geo.tool.GradientFactory;
import net.termat.geo.tool.Range;

/**
 * 曲率図作成クラス
 * @author t-matsuoka
 * @version 0.50
 */
public class CurveImageCreator extends AbstractGeoImageCreator{
	private Gradient grad;
	private Range range;
	public enum Type{VERTICAL,HORIZONTAL}
	private Type type;

	/**
	 *
	 * @param dem 標高PNG
	 */
	public CurveImageCreator(BufferedImage dem,Type type){
		super(dem);
		super.setBackGroundColor(Color.WHITE);
		grad=GradientFactory.createGradient(new Color[]{Color.BLUE,Color.WHITE,Color.RED});
		range=new Range(-0.06,0.06);
		this.type=type;
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
				double sv=getCurveVal(px,dx,dy,type);
				int rgb=grad.getColorByInt(range.getNormalValue(sv));
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
		dx=dx*size;
		dy=dy*size;
		int x=0;
		int y=0;
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
		double v=0;
		double ii=0;
		double n=Math.floor((cell.length-1)*(cell[0].length-1)/10)*10;
		for(int i=1;i<cell.length-1;i++){
			for(int j=1;j<cell[i].length-1;j++){
				v=((++ii)/n)*100;
				if(v%10==0)update("進捗："+v+"%");
				double[][] px=getPixelDataCell(i,j,cell);
				double sv=getCurveVal(px,dx,dy,type);
				int rgb=grad.getColorByInt(range.getNormalValue(sv));
				cell[i][j].setRGB(rgb);
			}
		}
	}

	private static double getCurveVal(double[][] p,double dx,double dy,Type type){
		double scale=1.0;
		double z1=p[0][0];
		double z2=p[0][1];
		double z3=p[0][2];
		double z4=p[1][0];
		double z5=p[1][1];
		double z6=p[1][2];
		double z7=p[2][0];
		double z8=p[2][1];
		double z9=p[2][2];
		double a=(z1+z3+z4+z6+z7+z9)/(6*dx*dx)-(z2+z5+z8)/(3*dx*dx);
		double b=(z1+z2+z3+z7+z8+z9)/(6*dy*dy)-(z4+z5+z6)/(3*dy*dy);
		double c=(z3+z7-z1-z9)/(4*dx*dy);
		double d=(z3+z6+z9-z1-z4-z7)/(6*dx);
		double ee=(z1+z2+z3-z7-z8-z9)/(6*dy);
//		double f=(2*(z2+z4+z6+z8)-(z1+z3+z7+z9)+(5* z5))/9;
		if(type==Type.VERTICAL){
			double prfdenom = Math.round(Math.pow(10, 7)*((Math.sqrt(ee) + Math.sqrt(d))*Math.pow((1 + Math.sqrt(d) + Math.sqrt(ee)),1.5)))*Math.pow(10,-7);
			if(prfdenom==0){
				if((a>0&&b>0)||(a<0&&b<0)){
					return -(a+b)*scale;
				}else{
					return 0.0;
				}
			}else{
				double profile=scale*-2*(a*Math.sqrt(d)+b*Math.sqrt(ee)+c*d*ee)/prfdenom;	//垂直曲率
				return profile;
			}
		}else{
			double plndenom = Math.round(Math.pow(10,7)*Math.pow(Math.sqrt(ee) + Math.sqrt(d),1.5))*Math.pow(10,-7);
			if(plndenom==0){
				if((a>0&&b>0)||(a<0&&b<0)){
					return -(a+b)*scale;
				}else{
					return 0.0;
				}
			}else{
				double plan=scale*-2*(b*Math.sqrt(d)+a*Math.sqrt(ee)-c*d*ee)/plndenom;		//平面曲率
				return plan;
			}
		}
	}

	public static BufferedImage aveFilter(BufferedImage img,int num){
		float[] operator=new float[num*num];
		for(int i=0;i<operator.length;i++){
			operator[i]=1.0f/(float)operator.length;
		}
		Kernel blur=new Kernel(num,num,operator);
		ConvolveOp convop=new ConvolveOp(blur,ConvolveOp.EDGE_NO_OP,null);
		BufferedImage bimg=convop.filter(img,null);
		return bimg;
	}
}
