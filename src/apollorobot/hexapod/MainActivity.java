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
//			if(msg.what == UPDATE_UI)//������Ƭ
////				temperaturetextView.setText("����"+ str_recive + "��c");
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
    	// �ҵ�ǰ�������Ұ���
    	backBtn         = (Button)findViewById(R.id.backBtn);
    	leftBtn          = (Button)findViewById(R.id.leftBtn);
    	rightBtn         = (Button)findViewById(R.id.rightBtn);
    	stampBtn        = (Button)findViewById(R.id.stampBtn);// ����
    	SocketConnectBtn = (Button)findViewById(R.id.socketconnectBtn);// Socket���Ӱ���
    	turnheadleftbtn = (Button) findViewById(R.id.btnHeadLeft);
    	turnheadrightbtn = (Button) findViewById(R.id.btnHeadRight);
    	speedseekbar  = (SeekBar)findViewById(R.id.seekBar1);// ����������Ϣ��������
    	
    	// ��ô�������Intent
    	Intent intent = getIntent(); 
    	// ȡ��LogoInActivity��������ip�Ͷ˿�
    	final String ipformLogoIn   = intent.getStringExtra("CtrlIp");
    	final String portformLogoIn = intent.getStringExtra("CtrlPort");
    	if (mis != null) {
    		mjpegView.setSource(mis);// ����������Դ
    		mjpegView.setDisplayMode(MjpegView.KEEP_SCALE_MODE);// ������ʾģʽΪ��׼
    		mjpegView.startPlay();  // ��ʼ������Ƶ
    	}

    // ΪSocket�����󶨼�����
    SocketConnectBtn.setOnClickListener(new OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		int port = Integer.parseInt(portformLogoIn);// �˿ڱ���������
    		try {
    			socket = new Socket();// ����Scoket����
    			socket.connect(new InetSocketAddress(ipformLogoIn, port), 1000);// Socket��ip�Ͷ˿�
    			// ���ӳɹ�
    			if (socket.isConnected() == true) {
    				// ��������������
    				os = socket.getOutputStream();
    				is = socket.getInputStream();
    				is_connect = true; // �������ӱ��
    				Toast.makeText(MainActivity.this, "Socket���ӳɹ���", Toast.LENGTH_LONG).show();
    				// ����һ���߳�,���ڽ��շ����������� 	
    				  ReciveThread rThread = new ReciveThread();
    				  new Thread(rThread, "ReciveThread").start();
    				}
    			} catch(Exception e) {
    				e.printStackTrace();
    				Toast.makeText(MainActivity.this, "Socket����ʧ��,�����������ã�", Toast.LENGTH_LONG).show();
    				}
    			}
    	});

    // ��ǰ
    forwardBtn.setOnTouchListener(new OnTouchListener(){

    	public boolean onTouch(View v, MotionEvent event) {
            byte[] cmd = COMM_FORWARD;
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:// ����,�� direction:run
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
    // ���
    backBtn.setOnTouchListener(new OnTouchListener(){

    		public boolean onTouch(View v, MotionEvent event) {
    	        byte[] cmd = COMM_BACKWARD;
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:// ����,�� direction:run
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
    // ����
    leftBtn.setOnTouchListener(new OnTouchListener(){

    	public boolean onTouch(View v, MotionEvent event) {
            byte[] cmd = COMM_LEFT;
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:// ����,�� direction:stop
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
    // ����
    rightBtn.setOnTouchListener(new OnTouchListener(){

    	public boolean onTouch(View v, MotionEvent event) {
            byte[] cmd = COMM_RIGHT;
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:// ����,���ַ���direction:right
                SendCommand(cmd);
                break;
            case MotionEvent.ACTION_UP: // �ɿ�,���ַ���direction:stop
            	SendCommand(COMM_STOP);
            	break;
            default:
                break;
            }
            return false;
        }
    	});

    // ���
    stampBtn.setOnTouchListener(new OnTouchListener(){
       public boolean onTouch(View v, MotionEvent event) {
    	   
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:// ����,����buzzer:on
                SendCommand(COMM_STAMP);
                break;
            case MotionEvent.ACTION_UP:  // // �ɿ�,����buzzer:off
            	SendCommand(COMM_STOP);
            default:
                break;
            }
            return false;
        }
     });
    
    //ͷ����ת
    turnheadleftbtn.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			SendCommand(COMM_LEFTHEAD);
		}
	});
    
    //ͷ����ת
    turnheadrightbtn.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			SendCommand(COMM_RIGHTHEAD);
		}
	});
//    // �󶨽�����
//    speedseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
//
//    	@Override
//    	public void onProgressChanged(SeekBar arg0, int process, boolean arg2) {
//    		SendCommand("speed:"+ process);
//    		}
//    	@Override
//    	public void onStartTrackingTouch(SeekBar seekBar) {
//    		System.out.println("�϶���ʼ...");
//    	}
//    	@Override
//    	public void onStopTrackingTouch(SeekBar seekBar) {
//    		System.out.println("�϶�ֹͣ...");
//    	}
//    	} );
    }
    
 
  
	// �����߳�
	class ReciveThread implements Runnable {   
        public void run() {  
             while (!Thread.currentThread().isInterrupted()) {    
                  Message message = new Message();   
                  message.what = UPDATE_UI;   
//                  handler.sendMessage(message);   
                  try {   
                	  
                          if (is_connect){
                              temp_connect = is.read(recive_buffer,0,recive_buffer.length);
                              // ��ȡSocket�ɹ�
                              if (temp_connect != -1){
                                  str_recive = new String(recive_buffer ,0,temp_connect);
                                  str_recive.trim();// ȥ��ǰ��ո��
                                  System.out.println("recive datas: "+ str_recive);   
//                                  temperaturetextView.setText("����"+ str_recive + "��c");
                              }
                              else {
                              	// shutdown Socket
                              	socket.shutdownInput();
                              	socket.shutdownOutput();
                              	// �ر�����/�����
                                  is.close();
                                  os.close();
                                  // �ر�Socket
                                  socket.close();
                                  // ���ñ��
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
   } // �����߳�
}
	// �����ַ�����Socket
	protected void SendCommand(byte[] command) {
//        System.out.println("SendCommand: " + Thread.currentThread().getName());
		System.out.println("SendCommand: " + command);
        try{
        	os.write(command);// дSocket
            os.flush();					 // ˢ�»�����
            }catch (IOException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
    }
		
	// ��Ƶ������ʧ
	protected void onDestroy() {
		if (mjpegView != null)
				mjpegView.stopPlay();// ֹͣ����
			super.onDestroy();
	}
	private long exitTime = 0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		   if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)  {  
               if((System.currentTimeMillis() - exitTime) > 2000)  //System.currentTimeMillis()���ۺ�ʱ���ã��϶�����2000   
                     {  
                          Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�����",Toast.LENGTH_SHORT).show();                                  
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
