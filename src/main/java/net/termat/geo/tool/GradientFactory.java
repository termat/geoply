package net.termat.geo.tool;

import java.awt.Color;

public class GradientFactory {

	private GradientFactory(){}

	public static Gradient createGradient(Color[] colors){
		if(colors.length==2){
			LinerGradient ret=new LinerGradient(colors,1.0);
			return ret;
		}else if(colors.length>2){
			SplineGradient ret=new SplineGradient(colors,1.0);
			return ret;
		}else{
			throw new IllegalArgumentException();
		}
	}

	public static Gradient createGradient(Color[] colors,double order){
		if(colors.length==2){
			LinerGradient ret=new LinerGradient(colors,order);
			return ret;
		}else if(colors.length>2){
			SplineGradient ret=new SplineGradient(colors,order);
			return ret;
		}else{
			throw new IllegalArgumentException();
		}
	}

	private static class LinerGradient implements Gradient{
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private double[][] color;
		private double order=1.0;
		private Color nanColor=Color.BLACK;

		public LinerGradient(Color[] c,double o){
			color=new double[3][2];
			color[0][0]=(double)c[0].getRed();
			color[0][1]=(double)c[1].getRed();
			color[1][0]=(double)c[0].getGreen();
			color[1][1]=(double)c[1].getGreen();
			color[2][0]=(double)c[0].getBlue();
			color[2][1]=(double)c[1].getBlue();
			order=o;
		}

		@Override
		public Color getColor(double arg) {
			double val=Math.pow(arg, order);
			if(val<0)val=0;
			if(val>1.0)val=1.0;
			int r=(int)((color[0][1]-color[0][0])*val+color[0][0]);
			int g=(int)((color[1][1]-color[1][0])*val+color[1][0]);
			int b=(int)((color[2][1]-color[2][0])*val+color[2][0]);
			Color ret=new Color(r,g,b,255);
			return ret;
		}

		@Override
		public float[] getColorByFloat(double arg) {
			double val=Math.pow(arg, order);
			if(val<0)val=0;
			if(val>1.0)val=1.0;
			float r=(float)((color[0][1]-color[0][0])*val+color[0][0])/255.0f;
			float g=(float)((color[1][1]-color[1][0])*val+color[1][0])/255.0f;
			float b=(float)((color[2][1]-color[2][0])*val+color[2][0])/255.0f;
			return new float[]{r,g,b,1.0f};
		}

		@Override
		public int getColorByInt(double arg) {
			double val=Math.pow(arg, order);
			if(val<0)val=0;
			if(val>1.0)val=1.0;
			int r=(int)((color[0][1]-color[0][0])*val+color[0][0]);
			int g=(int)((color[1][1]-color[1][0])*val+color[1][0]);
			int b=(int)((color[2][1]-color[2][0])*val+color[2][0]);
			if(r<0)r=0;
			if(r>255)r=255;
			if(g<0)g=0;
			if(g>255)g=255;
			if(b<0)b=0;
			if(b>255)b=255;
			int ret=(255<<24)+(r<<16)+(g<<8)+b;
			return ret;
		}

		@Override
		public Color getColor(double val, int step) {
			int vv=(int)(val*1000);
			int dv=1000/step;
			int xx=vv/dv;
			return getColor(dv*(double)xx/1000);
		}

		@Override
		public void setNanColor(Color c){
			nanColor=c;
		}

		@Override
		public Color getNanColor(){
			return nanColor;
		}
	}

	private static class SplineGradient implements Gradient{
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private double[][] color;
		private double[] value;
		private Spline[] spline;
		private double order=1.0;
		private Color nanColor=Color.BLACK;

		public SplineGradient(Color[] c,double o){
			color=new double[3][c.length];
			value=new double[c.length];
			spline=new Spline[3];
			for(int i=0;i<c.length;i++){
				color[0][i]=(double)c[i].getRed();
				color[1][i]=(double)c[i].getGreen();
				color[2][i]=(double)c[i].getBlue();
				value[i]=(float)i/(float)(color.length-1);
			}
			spline[0]=new Spline(value,color[0],Spline.NONCYCLE);
			spline[1]=new Spline(value,color[1],Spline.NONCYCLE);
			spline[2]=new Spline(value,color[2],Spline.NONCYCLE);
			order=o;
		}

		public Color getColor(double v){
			if(Double.isNaN(v)||Double.isInfinite(v)){
				return nanColor;
//				throw new ArithmeticException("NaN");
			}else{
				double val=Math.pow(v, order);
				if(val<0)val=0;
				if(val>1.0)val=1.0;
				int r=(int)(spline[0].interpolate(val));
				int g=(int)(spline[1].interpolate(val));
				int b=(int)(spline[2].interpolate(val));
				if(r<0)r=0;
				if(r>255)r=255;
				if(g<0)g=0;
				if(g>255)g=255;
				if(b<0)b=0;
				if(b>255)b=255;
				Color ret=new Color(r,g,b,255);
				return ret;
			}
		}

		public float[] getColorByFloat(double v){
			if(Double.isNaN(v)||Double.isInfinite(v)){
				return new float[]{0,0,0,1.0f};
//				throw new ArithmeticException("NaN");
			}else{
				double val=Math.pow(v, order);
				if(val<0)val=0;
				if(val>1.0)val=1.0;
				float r=(float)(spline[0].interpolate(val))/255.0f;
				float g=(float)(spline[1].interpolate(val))/255.0f;
				float b=(float)(spline[2].interpolate(val))/255.0f;
				if(r<0.0f)r=0.0f;
				if(r>1.0f)r=1.0f;
				if(g<0.0f)g=0.0f;
				if(g>1.0f)g=1.0f;
				if(b<0.0f)b=0.0f;
				if(b>1.0f)b=1.0f;
				return new float[]{r,g,b,1.0f};
			}
		}

		@Override
		public int getColorByInt(double v) {
			if(Double.isNaN(v)||Double.isInfinite(v)){
				return (255<<24)+(0<<16)+(0<<8)+0;
//				throw new ArithmeticException("NaN");
			}else{
				double val=Math.pow(v, order);
				if(val<0)val=0;
				if(val>1.0)val=1.0;
				int r=(int)(spline[0].interpolate(val));
				int g=(int)(spline[1].interpolate(val));
				int b=(int)(spline[2].interpolate(val));
				if(r<0)r=0;
				if(r>255)r=255;
				if(g<0)g=0;
				if(g>255)g=255;
				if(b<0)b=0;
				if(b>255)b=255;
				int ret=(255<<24)+(r<<16)+(g<<8)+b;
				return ret;
			}
		}

		@Override
		public Color getColor(double val, int step) {
			int vv=(int)(val*1000);
			int dv=(1000/step);
			int xx=vv/dv;
			return getColor(dv*(double)xx/1000);
		}

		@Override
		public void setNanColor(Color c){
			nanColor=c;
		}

		@Override
		public Color getNanColor(){
			return nanColor;
		}
	}
}
