package net.termat.geo.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 画像処理ユーティリティクラス
 * @author t-matsuoka
 * @version 0.5
 */
public class ImageUtil {

	private ImageUtil(){}

	/**
	 * BufferedImageをbyte[]に変換
	 * @param img BufferedImage
	 * @param ext 画像の拡張子
	 * @return byte[]
	 * @throws IOException
	 */
	public static byte[] bi2Bytes(BufferedImage img,String ext)throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( img, ext, baos );
		baos.flush();
		return baos.toByteArray();
	}

	/**
	 * byte[]をBufferedImageに変換
	 * @param raw byte[]
	 * @return BufferedImage
	 * @throws IOException
	 */
	public static BufferedImage bytes2Bi(byte[] raw)throws IOException{
		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		BufferedImage img=ImageIO.read(bais);
		return img;
	}

	/**
	 * 画像を乗算
	 * @param im1 画像1
	 * @param im2 画像2
	 * @return
	 */
	public static BufferedImage mul(BufferedImage im1,BufferedImage im2){
		int w=im1.getWidth();
		int h=im1.getHeight();
		BufferedImage ret=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				Color c1=new Color(im1.getRGB(i, j));
				Color c2=new Color(im2.getRGB(i, j));
				ret.setRGB(i, j, mul(c1,c2).getRGB());
			}
		}
		return ret;
	}

	private static Color mul(Color c1,Color c2){
		float[] f1=c1.getRGBComponents(new float[4]);
		float[] f2=c2.getRGBComponents(new float[4]);
		for(int i=0;i<f1.length;i++){
			f1[i]=f1[i]*f2[i];
		}
		return new Color(f1[0],f1[1],f1[2],f1[3]);
	}

	/**
	 * 3×3ガルシアンフィルタを適用
	 * @param img 画像
	 * @return
	 */
	public static BufferedImage galcianFilter3(BufferedImage img){
		final float[] operator={
				0.0625f, 0.125f, 0.0625f,
				0.125f, 0.25f, 0.125f,
				0.0625f, 0.125f, 0.0625f};
		return acceptFilter(img,operator,3);
	}

	/**
	 * 5×5ガルシアンフィルタを適用
	 * @param img 画像
	 * @return
	 */
	public static BufferedImage galcianFilter5(BufferedImage img){
		final float[] operator={
				0.00390625f,0.015625f,0.0234375f,0.015625f,0.00390625f,
				0.015625f,0.0625f,0.09375f,0.0625f,0.015625f,
				0.0234375f,0.09375f,0.140625f,0.09375f,0.0234375f,
				0.015625f,0.0625f,0.09375f,0.0625f,0.015625f,
				0.00390625f,0.015625f,0.0234375f,0.015625f,0.00390625f};
		return acceptFilter(img,operator,5);
	}

	/**
	 * 画像フィルタを適用
	 * @param img	画像
	 * @param operator	オペレータ
	 * @param size ピクセルサイズ
	 * @return
	 */
	public static BufferedImage acceptFilter(BufferedImage img,float[] operator,int size){
		Kernel blur=new Kernel(size,size,operator);
		ConvolveOp convop=new ConvolveOp(blur,ConvolveOp.EDGE_NO_OP,null);
		BufferedImage bimg=convop.filter(img,null);
		return bimg;
	}

	/**
	 * 平均化フィルタを適用
	 * @param img 画像
	 * @param num ピクセルサイズ
	 * @return
	 */
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
