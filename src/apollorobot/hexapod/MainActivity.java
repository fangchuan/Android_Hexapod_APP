package apollorobot.hexapod;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class MainActivity extends Activity {

	protected static final int UPDATE_UI = 0;
	public static MainActivity instance = null;
	private MjpegInputStream mis = null;
	private MjpegView mjpegView = null;
	private Button forwardBtn = null;
	private Button backBtn = null;
	private Button leftBtn = null;
	private Button rightBtn = null;
	private Button stampBtn = null;
	private Button SocketConnectBtn = null;
	private Button turnheadleftbtn = null;
	private Button turnheadrightbtn = null;
	private InputStream is = null;
	private OutputStream os = null;
	public boolean is_connect = false;
	public int temp_connect;
	public String str_recive;
	public Socket socket;
	public byte[] recive_buffer = new byte[1024];
	protected boolean Is_press = true;
	private SeekBar speedseekbar = null;
//	private String command = "direction:stop";
	protected int UPDATEUI = 1;
	
	private byte[] COMM_FORWARD =  { (byte) 0xAA, (byte) 0x01, (byte) 0x10,(byte) 0x07, (byte) 0x01, (byte) 0x48, (byte) 0xBB };// 
	private byte[] COMM_BACKWARD = { (byte) 0xAA, (byte) 0x01, (byte) 0x10,(byte) 0x07, (byte) 0x02, (byte) 0x48, (byte) 0xBB };//
	private byte[] COMM_LEFT =     { (byte) 0xAA, (byte) 0x01, (byte) 0x10,(byte) 0x07, (byte) 0x03, (byte) 0x48, (byte) 0xBB };//
	private byte[] COMM_RIGHT =    { (byte) 0xAA, (byte) 0x01, (byte) 0x10,(byte) 0x07, (byte) 0x04, (byte) 0x48, (byte) 0xBB };//
	private byte[] COMM_STOP =     { (byte) 0xAA, (byte) 0x01, (byte) 0x10,(byte) 0x07, (byte) 0x05, (byte) 0x48, (byte) 0xBB };//
    private byte[] COMM_STAMP =    { (byte) 0xAA, (byte) 0x01, (byte) 0x10,(byte) 0x07, (byte) 0x06, (byte) 0x48, (byte) 0xBB };//
    private byte[] COMM_LEFTHEAD = { (byte) 0xAA, (byte) 0x01, (byte) 0x10,(byte) 0x07, (byte) 0x09, (byte) 0x48, (byte) 0xBB };//
    private byte[] COMM_RIGHTHEAD= { (byte) 0xAA, (byte) 0x01, (byte) 0x10,(byte) 0x07, (byte) 0x0A, (byte) 0x48, (byte) 0xBB };//
//	private Handler handler = new Handler() {
//		public void handleMessage(Message msg) {
//			if(msg.what == UPDATE_UI)//更新照片
////				temperaturetextView.setText("车温"+ str_recive + "°c");
//		}
//	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    	instance = this;
    	mis = MjpegInputStream.getInstance();
    	mjpegView        = (MjpegView) findViewById(R.id.mjpegview);
    	forwardBtn       = (Button)findViewById(R.id.fowardBtn);
    	// 找到前、后、左、右按键
    	backBtn         = (Button)findViewById(R.id.backBtn);
    	leftBtn          = (Button)findViewById(R.id.leftBtn);
    	rightBtn         = (Button)findViewById(R.id.rightBtn);
    	stampBtn        = (Button)findViewById(R.id.stampBtn);// 报警
    	SocketConnectBtn = (Button)findViewById(R.id.socketconnectBtn);// Socket连接按键
    	turnheadleftbtn = (Button) findViewById(R.id.btnHeadLeft);
    	turnheadrightbtn = (Button) findViewById(R.id.btnHeadRight);
    	speedseekbar  = (SeekBar)findViewById(R.id.seekBar1);// 进度条发消息给服务器
    	
    	// 获得传进来的Intent
    	Intent intent = getIntent(); 
    	// 取出LogoInActivity传进来的ip和端口
    	final String ipformLogoIn   = intent.getStringExtra("CtrlIp");
    	final String portformLogoIn = intent.getStringExtra("CtrlPort");
    	if (mis != null) {
    		mjpegView.setSource(mis);// 设置数据来源
    		mjpegView.setDisplayMode(MjpegView.KEEP_SCALE_MODE);// 设置显示模式为标准
    		mjpegView.startPlay();  // 开始播放视频
    	}

    // 为Socket按键绑定监听器
    SocketConnectBtn.setOnClickListener(new OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		int port = Integer.parseInt(portformLogoIn);// 端口必须是整型
    		try {
    			socket = new Socket();// 创建Scoket连接
    			socket.connect(new InetSocketAddress(ipformLogoIn, port), 1000);// Socket绑定ip和端口
    			// 连接成功
    			if (socket.isConnected() == true) {
    				// 获得输出，输入流
    				os = socket.getOutputStream();
    				is = socket.getInputStream();
    				is_connect = true; // 设置连接标记
    				Toast.makeText(MainActivity.this, "Socket连接成功！", Toast.LENGTH_LONG).show();
    				// 创建一个线程,用于接收服务器端数据 	
    				  ReciveThread rThread = new ReciveThread();
    				  new Thread(rThread, "ReciveThread").start();
    				}
    			} catch(Exception e) {
    				e.printStackTrace();
    				Toast.makeText(MainActivity.this, "Socket连接失败,请检查网络设置！", Toast.LENGTH_LONG).show();
    				}
    			}
    	});

    // 向前
    forwardBtn.setOnTouchListener(new OnTouchListener(){

    	public boolean onTouch(View v, MotionEvent event) {
            byte[] cmd = COMM_FORWARD;
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:// 按下,发 direction:run
                SendCommand(cmd);
                break;
            case MotionEvent.ACTION_UP:
            	SendCommand(COMM_STOP);
            	break;
            default:
                break;
            }
            return false;
        }
    	});
    // 向后
    backBtn.setOnTouchListener(new OnTouchListener(){

    		public boolean onTouch(View v, MotionEvent event) {
    	        byte[] cmd = COMM_BACKWARD;
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:// 按下,发 direction:run
                    SendCommand(cmd);
                    break;
                case MotionEvent.ACTION_UP:
                	SendCommand(COMM_STOP);
                	break;
                default:
                    break;
                }
                return false;
            }
    		});	
    // 向左
    leftBtn.setOnTouchListener(new OnTouchListener(){

    	public boolean onTouch(View v, MotionEvent event) {
            byte[] cmd = COMM_LEFT;
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:// 按下,发 direction:stop
                SendCommand(cmd);
                break;
            case MotionEvent.ACTION_UP:
            	SendCommand(COMM_STOP);
            	break;
            default:
                break;
            }
            return false;
        }
    });
    // 向右
    rightBtn.setOnTouchListener(new OnTouchListener(){

    	public boolean onTouch(View v, MotionEvent event) {
            byte[] cmd = COMM_RIGHT;
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:// 按下,发字符串direction:right
                SendCommand(cmd);
                break;
            case MotionEvent.ACTION_UP: // 松开,发字符串direction:stop
            	SendCommand(COMM_STOP);
            	break;
            default:
                break;
            }
            return false;
        }
    	});

    // 跺脚
    stampBtn.setOnTouchListener(new OnTouchListener(){
       public boolean onTouch(View v, MotionEvent event) {
    	   
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:// 按下,发：buzzer:on
                SendCommand(COMM_STAMP);
                break;
            case MotionEvent.ACTION_UP:  // // 松开,发：buzzer:off
            	SendCommand(COMM_STOP);
            default:
                break;
            }
            return false;
        }
     });
    
    //头向左转
    turnheadleftbtn.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			SendCommand(COMM_LEFTHEAD);
		}
	});
    
    //头向右转
    turnheadrightbtn.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			SendCommand(COMM_RIGHTHEAD);
		}
	});
//    // 绑定进度条
//    speedseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
//
//    	@Override
//    	public void onProgressChanged(SeekBar arg0, int process, boolean arg2) {
//    		SendCommand("speed:"+ process);
//    		}
//    	@Override
//    	public void onStartTrackingTouch(SeekBar seekBar) {
//    		System.out.println("拖动开始...");
//    	}
//    	@Override
//    	public void onStopTrackingTouch(SeekBar seekBar) {
//    		System.out.println("拖动停止...");
//    	}
//    	} );
    }
    
 
  
	// 接收线程
	class ReciveThread implements Runnable {   
        public void run() {  
             while (!Thread.currentThread().isInterrupted()) {    
                  Message message = new Message();   
                  message.what = UPDATE_UI;   
//                  handler.sendMessage(message);   
                  try {   
                	  
                          if (is_connect){
                              temp_connect = is.read(recive_buffer,0,recive_buffer.length);
                              // 读取Socket成功
                              if (temp_connect != -1){
                                  str_recive = new String(recive_buffer ,0,temp_connect);
                                  str_recive.trim();// 去掉前后空格符
                                  System.out.println("recive datas: "+ str_recive);   
//                                  temperaturetextView.setText("车温"+ str_recive + "°c");
                              }
                              else {
                              	// shutdown Socket
                              	socket.shutdownInput();
                              	socket.shutdownOutput();
                              	// 关闭输入/输出流
                                  is.close();
                                  os.close();
                                  // 关闭Socket
                                  socket.close();
                                  // 设置标记
                                  is_connect = false;
                              }
                          }
                       Thread.sleep(100); 
                       } 
                   catch (InterruptedException e) {   
                       Thread.currentThread().interrupt();   
                  }   
                  catch (Exception e) {
                      e.printStackTrace();
                  }
             }
   } // 接收线程
}
	// 发送字符串到Socket
	protected void SendCommand(byte[] command) {
//        System.out.println("SendCommand: " + Thread.currentThread().getName());
		System.out.println("SendCommand: " + command);
        try{
        	os.write(command);// 写Socket
            os.flush();					 // 刷新缓冲区
            }catch (IOException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
    }
		
	// 视频窗口消失
	protected void onDestroy() {
		if (mjpegView != null)
				mjpegView.stopPlay();// 停止播放
			super.onDestroy();
	}
	private long exitTime = 0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		   if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)  {  
               if((System.currentTimeMillis() - exitTime) > 2000)  //System.currentTimeMillis()无论何时调用，肯定大于2000   
                     {  
                          Toast.makeText(getApplicationContext(), "再按一次退出程序",Toast.LENGTH_SHORT).show();                                  
                          exitTime = System.currentTimeMillis();  
                     }  
                     else {  
                         finish();  
                         System.exit(0);  
                    }  
                                   
                    return true;  
         }  
		
		return super.onKeyDown(keyCode, event);
	}
}
