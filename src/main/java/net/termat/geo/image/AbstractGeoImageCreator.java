package net.termat.geo.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Observable;

import javax.imageio.ImageIO;

import net.termat.geo.ElevationPng;

/**
 * 地理情報画像処理の抽象クラス
 * @author t-matsuoka
 * @version 0.5
 */
public abstract class AbstractGeoImageCreator extends Observable implements ImageProcessingObservable{
	protected BufferedImage image;
	protected double[][] dem;
	protected String message;

	/**
	 *
	 * @param demImage 標高PNG画像
	 */
	public AbstractGeoImageCreator(BufferedImage demImage){
		BufferedImage dd=demImage;
		image=new BufferedImage(dd.getWidth(),dd.getHeight(),BufferedImage.TYPE_INT_RGB);
		dem=new double[dd.getWidth()][dd.getHeight()];
		for(int i=0;i<dd.getWidth();i++){
			for(int j=0;j<dd.getHeight();j++){
				dem[i][j]=ElevationPng.getZ(dd.getRGB(i, j));
			}
		}
	}

	/**
	 *
	 * @param demFile 標高PNG画像ファイル
	 * @throws IOException
	 */
	public AbstractGeoImageCreator(File demFile)throws IOException{
		BufferedImage dd=ImageIO.read(demFile);
		image=new BufferedImage(dd.getWidth(),dd.getHeight(),BufferedImage.TYPE_INT_RGB);
		dem=new double[dd.getWidth()][dd.getHeight()];
		for(int i=0;i<dd.getWidth();i++){
			for(int j=0;j<dd.getHeight();j++){
				dem[i][j]=ElevationPng.getZ(dd.getRGB(i, j));
			}
		}
	}

	protected void setBackGroundColor(Color c){
		Graphics2D g=(Graphics2D)image.getGraphics();
		g.setBackground(c);
		g.clearRect(0, 0, image.getWidth(), image.getHeight());
		g.dispose();
	}

	protected void write(File f)throws IOException{
		ImageIO.write(image, "png", f);
	}

	/**
	 * 画像を取得
	 * @return BufferedImage
	 */
	public BufferedImage getImage(){
		return image;
	}

	/**
	 * 画像を出力
	 *
	 * @param f ファイル
	 * @param ext 出力形式（"png"又は"jpg"）
	 * @throws Exception
	 */
	public void output(File f,String ext)throws Exception{
		ImageIO.write(image, ext, f);
		update("出力終了");
	}

	protected double[][] getPixelData(int x,int y){
		double[][] ret=new double[3][3];
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				ret[i][j]=dem[x+i-1][y+j-1];
			}
		}
		return ret;
	}

	protected double[][] getPixelDataCell(int x,int y,Cell[][] cell){
		double[][] ret=new double[3][3];
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				ret[i][j]=cell[x+i-1][y+j-1].getH();
			}
		}
		return ret;
	}

	protected void update(String mes){
		message=mes;
		setChanged();
		notifyObservers();
	}

	@Override
	public String progress() {
		return message;
	}

	protected class Cell{
		int[] x;
		int[] y;

		double getH(){
			double n=0;
			double v=0;
			for(int i=0;i<x.length;i++){
				for(int j=0;j<y.length;j++){
					if(Double.isNaN(dem[x[i]][y[j]])||dem[x[i]][y[j]]<=0)continue;
					n++;
					v +=dem[x[i]][y[j]];
				}
			}
			if(n==0){
				return 0;

			}else{
				return v/n;
			}
		};

		double getMinH(){
			double min=Double.MAX_VALUE;
			for(int i=0;i<x.length;i++){
				for(int j=0;j<y.length;j++){
					if(Double.isNaN(dem[x[i]][y[j]])||dem[x[i]][y[j]]<=0)continue;
					min=Math.min(min, dem[x[i]][y[j]]);
				}
			}
			if(min==Double.MAX_VALUE){
				return 0;
			}else{
				return min;
			}
		}

		double getMaxH(){
			double max=-Double.MAX_VALUE;
			for(int i=0;i<x.length;i++){
				for(int j=0;j<y.length;j++){
					if(Double.isNaN(dem[x[i]][y[j]])||dem[x[i]][y[j]]<=0)continue;
					max=Math.max(max, dem[x[i]][y[j]]);
				}
			}
			if(max==Double.MAX_VALUE){
				return 0;
			}else{
				return max;
			}
		}

		void setRGB(int rgb){
			for(int i=0;i<x.length;i++){
				for(int j=0;j<y.length;j++){
					image.setRGB(x[i], y[j], rgb);
				}
			}
		}

		public String toString(){
			StringBuffer buf=new StringBuffer();
			for(int i=0;i<x.length;i++){
				for(int j=0;j<y.length;j++){
					buf.append("["+x[i]+","+y[j]+"],");
				}
				buf.append("\n");
			}
			return buf.toString();
		}

		double dist(Cell cell,double dx,double dy){
			int x1=x[x.length/2];
			int y1=y[x.length/2];
			int x2=cell.x[cell.x.length/2];
			int y2=cell.y[cell.y.length/2];
			double xx=(x1-x2)*dx;
			double yy=(y1-y2)*dy;
			return Math.sqrt(xx*xx+yy*yy);
		}

		Point2D getCenter(){
			double xx=0;
			double yy=0;
			for(int i=0;i<x.length;i++)xx +=x[i];
			for(int i=0;i<y.length;i++)yy +=y[i];
			xx=xx/x.length;
			yy=yy/y.length;
			return new Point2D.Double(xx,yy);
		}
	}

}
