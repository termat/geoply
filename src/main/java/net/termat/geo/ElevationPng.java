package net.termat.geo;

import java.awt.Color;

/**
 * 標高PNGユーティリティクラス
 * @author t-matsuoka
 * @version 0.5
 */
public class ElevationPng {

	private static final int P8=256;
	private static final int P16=65536;
	private static final int P23=8388608;
	private static final int P24=16777216;
	private static final double U=0.01;

	private ElevationPng(){}

	/* 標高PNG NA値*/
	public static int NA=P23;

	/* RGB値をIntに変換*/
	private static int rgb2Int(int[] c){
		return new Color(c[0],c[1],c[2]).getRGB();
	}

	/**
	 * 標高値を標高PNG画素値に変換
	 * @param z 標高値（ｍ）
	 * @return 標高PNG画素値
	 */
	public static int getRGB(double z){
		if(Double.isNaN(z))return P23;
		return rgb2Int(getRGBColor(z));
	}

	/* 標高値をRGB:int[]に変換 */
	private static int[] getRGBColor(double z){
		if(z<=0)return new int[]{128,0,0};
		int i=(int)Math.round(z*100);
		int r=i >> 16;
		int g=i-(r << 16) >> 8;
		int b=i-((r << 16)+(g << 8));
		return new int[]{r,g,b};
	}

	/**
	 * 標高PNG画素値を標高値に変換
	 * @param intColor 標高PNG画素値
	 * @return 標高値（ｍ）
	 */
	public static double getZ(int intColor){
		Color c=new Color(intColor);
		int r=c.getRed();
		int g=c.getGreen();
		int b=c.getBlue();
		int x=r*P16+g*P8+b;
		if(x<P23){
			return U*(double)x;
		}else if(x>P23){
			return U*(double)(x-P24);
		}else{
			return Double.NaN;
		}
	}
}
