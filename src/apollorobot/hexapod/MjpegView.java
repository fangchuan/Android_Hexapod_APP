package apollorobot.hexapod;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MjpegView extends SurfaceView implements SurfaceHolder.Callback {
	// ͼ����ʾģʽ
	public final static int STANDARD_MODE = 1;//��׼�ߴ�
	public final static int KEEP_SCALE_MODE = 4;//���ֿ�߱���
	public final static int FULLSCREEN_MODE = 8;//ȫ��

	private Context mContext = null;
	private MjpegViewThread mvThread = null; // ��Ⱦ�߳�
	private MjpegInputStream mIs = null;

	private boolean bRun = false;
	private boolean bsurfaceIsCreate = false;
	
	private int dispWidth; // MjpegView�Ŀ��
	private int dispHeight;// MjpegView�ĸ߶�
	private int displayMode;

	public MjpegView(Context context) {
		super(context);
		init(context);
	}

	public MjpegView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		mvThread = new MjpegViewThread(holder, context);
		setFocusable(true);
		
		displayMode = MjpegView.KEEP_SCALE_MODE;
	}

	public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
		mvThread.setSurfaceSize(w, h);
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		bsurfaceIsCreate = false;
		
	}

	public void surfaceCreated(SurfaceHolder holder) {
		bsurfaceIsCreate = true;
	}

	public void setSource(MjpegInputStream source) {
		mIs = source;
	}
	
	/**
	 * ��ʼ�����߳�
	 */
	public void startPlay() {
		if (mIs != null) {
			bRun = true;
			mvThread.start();
		}
	}

	/**
	 * ֹͣ�����߳�
	 */
	public void stopPlay() {
		bRun = false;
		boolean retry = true;
		while (retry) {
			try {
				mvThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
		
		//�߳�ֹͣ��ر�Mjpeg��(����Ҫ)
		mIs.closeInstance();
	}
	
	public Bitmap getBitmap(){
		return mvThread.getBitmap();
	}

	public void setDisplayMode(int s) {
		displayMode = s;
	}
	
	public int getDisplayMode() {
		return displayMode;
	}

	public class MjpegViewThread extends Thread {
		private SurfaceHolder mSurfaceHolder = null;
//		private int frameCounter = 0;
//		private long start = 0;
		private Canvas c = null;
//		private Bitmap overlayBitmap = null;
		private Bitmap mjpegBitmap = null;
		private PorterDuffXfermode mode = null;

		public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) {
			mSurfaceHolder = surfaceHolder;
			mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
		}
		
		public Bitmap getBitmap(){
			return mjpegBitmap;
		}

		/**
		 * ����ͼ��ߴ�
		 * @param bmw bitmap��
		 * @param bmh bitmap��
		 * @return ͼ�����
		 */
		private Rect destRect(int bmw, int bmh) {
			int tempx;
			int tempy;
			if (displayMode == MjpegView.STANDARD_MODE) {
				tempx = (dispWidth / 2) - (bmw / 2);
				tempy = (dispHeight / 2) - (bmh / 2);
				return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
			}
			if (displayMode == MjpegView.KEEP_SCALE_MODE) {
				float bmasp = (float) bmw / (float) bmh;
				bmw = dispWidth;
				bmh = (int) (dispWidth / bmasp);
				if (bmh > dispHeight) {
					bmh = dispHeight;
					bmw = (int) (dispHeight * bmasp);
				}
				tempx = (dispWidth / 2) - (bmw / 2);
				tempy = (dispHeight / 2) - (bmh / 2);
				return new Rect(0, 0, bmw + 0, bmh + 0);
			}
			if (displayMode == MjpegView.FULLSCREEN_MODE)
				return new Rect(0, 0, dispWidth, dispHeight);
			return null;
		}

		public void setSurfaceSize(int width, int height) {
			synchronized (mSurfaceHolder) {
				dispWidth = width;
				dispHeight = height;
			}
		}


		public void run() {
//			start = System.currentTimeMillis();
			Rect destRect;
			Paint p = new Paint();
//			String fps = "";
			while (bRun) {
				if (bsurfaceIsCreate) {
					c = mSurfaceHolder.lockCanvas();
						try {
							mjpegBitmap = mIs.readMjpegFrame();
							synchronized (mSurfaceHolder){
							destRect = destRect(mjpegBitmap.getWidth(),
									mjpegBitmap.getHeight());
							}
							/**
							 * ���ڱ����ٺ�Ͳ���������
							 */
							if(c !=null){
								c.drawPaint(new Paint());
								c.drawBitmap(mjpegBitmap, null, destRect, p);
							mSurfaceHolder.unlockCanvasAndPost(c);// �ͷŻ���
							}
						} catch (IOException e) {
						
					}
						
				}else {
					try {
						Thread.sleep(500);//�߳����ߣ��ó�����
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

	
	}
	
}
