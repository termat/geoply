package net.termat.geo.image;

import java.util.Observable;
import java.util.Observer;

/**
 * 画像処理Observerクラス
 * @author t-matsuoka
 * @version 0.50
 */
public class ImageProcessingObserver implements Observer{

	@Override
	public void update(Observable o, Object arg) {
		ImageProcessingObservable io=(ImageProcessingObservable)o;
		System.out.println(io.progress());
	}

}
