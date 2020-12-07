package net.termat.geo;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

public class TileReader {
    private static final double L=85.05112877980659;

    public static BufferedImage getGSIImage(String[] base_url,Rectangle2D plane,int map_num,int zoom,String ext){
    	Shape sp=getLonLatShape(map_num, plane.getX(), plane.getY(), plane.getWidth(), plane.getHeight());
        Rectangle2D rect=sp.getBounds2D();
        long[] topLeft=lonlatToPixel(zoom,rect.getX(),rect.getY());
        long[] bottomRight=lonlatToPixel(zoom,rect.getX()+rect.getWidth(),rect.getY()+rect.getHeight());
        Set<Long> x=new HashSet<Long>();
        Set<Long> y=new HashSet<Long>();
        for(long i=topLeft[1];i<=bottomRight[1];i++){
            x.add((long)Math.ceil(i/256));
        }
        for(long i=bottomRight[2];i<=topLeft[2];i++){
            y.add((long)Math.ceil(i/256));
        }
        Long[] xx=x.toArray(new Long[x.size()]);
        Long[] yy=y.toArray(new Long[y.size()]);
        Arrays.sort(xx);
        Arrays.sort(yy);
        BufferedImage im=new BufferedImage(xx.length*256,yy.length*256,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=im.createGraphics();
        for(int i=0;i<xx.length;i++){
            for(int j=0;j<yy.length;j++){
            	for(int k=0;k<base_url.length;k++){
                    try{
                        String url=base_url[k]+Integer.toString(zoom)+"/"+Long.toString(xx[i])+"/"+Long.toString(yy[j])+"."+ext;
                        BufferedImage tmp=ImageIO.read(new URL(url));
                        g.drawImage(tmp, i*256, j*256, null);
                        break;
                    }catch(Exception e){}
            	}
            }
        }
        g.dispose();
        long ww=bottomRight[1]-topLeft[1];
        long hh=topLeft[2]-bottomRight[2];
        if(ww%2!=0)ww++;
        if(hh%2!=0)hh++;
        Rectangle2D rect2=new Rectangle2D.Double(topLeft[1],bottomRight[2],ww,hh);
    	int px=(int)Math.ceil(topLeft[1]/256)*256;
    	int py=(int)Math.ceil(bottomRight[2]/256)*256;
        im=createSubImage(im,rect2,px,py);
        return im;
    }

    private static BufferedImage createSubImage(BufferedImage src,Rectangle2D rect,int x,int y){
    	BufferedImage dst=new BufferedImage((int)rect.getWidth(),(int)rect.getHeight(),BufferedImage.TYPE_INT_RGB);
    	int dx=(int)rect.getX();
    	int dy=(int)rect.getY();
    	for(int i=x;i<x+src.getWidth();i++){
    		for(int j=y;j<y+src.getHeight();j++){
    			if(rect.contains(i, j)){
    				int xx=i-dx;
    				int yy=j-dy;
    				dst.setRGB(xx, yy, src.getRGB(i-x, j-y));
    			}
    		}
    	}
    	return dst;
    }

    public static long[] lonlatToPixel(int zoom,double lon,double lat){
        long x=(long)(Math.pow(2, zoom+7)*(lon/180.0+1.0));
        long y=(long)((Math.pow(2, zoom+7)/Math.PI)*(-atanh(Math.sin(Math.toRadians(lat)))+atanh(Math.sin(Math.toRadians(L)))));
        return new long[]{(long)zoom,x,y};
    }

    private static double atanh(double v){
        return 0.5*Math.log((1.0+v)/(1.0-v));
    }

    public static AffineTransform createTfwTransform(Rectangle2D rectXY,BufferedImage img){
        double sx=rectXY.getWidth()/img.getWidth();
        double sy=rectXY.getHeight()/img.getHeight();
        double x=rectXY.getX();
        double y=rectXY.getY()+rectXY.getHeight();
        AffineTransform af=new AffineTransform(new double[]{sx,0,0,-sy,x,y});
        return af;
    }

    public static Shape getLonLatShape(int num,double x,double y,double w,double h){
        Point2D p1=LonLatXY.xyToLonlat(num, x, y);
        Point2D p2=LonLatXY.xyToLonlat(num, x+w, y);
        Point2D p3=LonLatXY.xyToLonlat(num, x+w, y+h);
        Point2D p4=LonLatXY.xyToLonlat(num, x, y+h);
        GeneralPath gp=new GeneralPath();
        gp.moveTo(p1.getX(),p1.getY());
        gp.lineTo(p2.getX(), p2.getY());
        gp.lineTo(p3.getX(), p3.getY());
        gp.lineTo(p4.getX(), p4.getY());
        gp.closePath();
        return gp;
    }

}
