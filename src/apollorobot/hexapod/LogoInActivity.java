package apollorobot.hexapod;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LogoInActivity extends Activity {
	private EditText VideoAddredit = null;
	private EditText CtrlPortedit = null;
	private EditText PortEdt = null;
	private InputStream is = null;// http输入流
	private Context mContext = this;
	private WifiManager wifi = null;
	
	private String Ip_address = "";
	private String Socketport = "";
	public int temp_connect;
	public String str_recive;
	public byte[] recive_buffer = new byte[1024];
	@Override
	// 第一次创建Activity试调用
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 必须设置以下，否则无法Socket连接
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads()
                .detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
                .penaltyLog().penaltyDeath().build());
        
		setContentView(R.layout.logoin_layout);
		VideoAddredit    = (EditText)findViewById(R.id.editIP);		 // 视频地址
		CtrlPortedit     = (EditText)findViewById(R.id.editCtrlPort);// Socket端口
//		ConnectButton    = (Button)findViewById(R.id.button_go);
		PortEdt          = (EditText)findViewById(R.id.videoportedit);
		// 设置，开启WIFI
		wifi             = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// 获得wifi的当前状态
		int state        = wifi.getWifiState();

		// state = 2;  不开wifi，不能打开该程序
		if (state != WifiManager.WIFI_STATE_ENABLED){
			Generic.showMsg(this, "请打开wifi", false);
			finish();
		}
		
	}
	// 绑定连接按键
	public void connectBtn(View v){
		String ip = VideoAddredit.getText().toString();
		String port = PortEdt.getText().toString();
		// port不能为空
		if(!port.equals("") && checkAddr(ip ,Integer.valueOf(port))){
			String action = "http://" + ip + ":" + port + "/?action=stream";
			new ManualTask().execute(action);
		}else{
			Generic.showMsg(this, "请检查ip和port", true);
		}
	}

	// 检查用户输入的IP的正确性
	private boolean checkAddr(String ip, int port){
			if(ip.split("\\.").length != 4)
				return false;
			if(port < 1000 || port > 65535)
				return false;

			return true;	
	}
	
	// 手动连接线程
	private class ManualTask extends AsyncTask<String, Integer, String>{
		
		@Override
		protected String doInBackground(String... params) {
			is = http(params[0]);// 取出每一组Ip，发起Http请求
			if (is != null)
				MjpegInputStream.initInstance(is);
			return null;
		}

	// 跳转到MainActivity
	protected void onPostExecute(String result) {
		if (is != null) {
			Intent intent = new Intent();
			String Ip_address = VideoAddredit.getText().toString(); // Socket/http 的Ip地址
			String Socketport = CtrlPortedit.getText().toString();  // Socket 端口
			// 将IP和端口传进Intent
			intent.putExtra("CtrlIp", Ip_address);
			intent.putExtra("CtrlPort", Socketport);
			intent.setClass(LogoInActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}else
		{
			Generic.showMsg(mContext, "连接失败", true);
		}
		super.onPostExecute(result);
	}
}
	// 发起Http请求
	private InputStream http(String url){
		HttpResponse res;
		// 创建Http客户端
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 300);
		try {
			//采用Http的Get方式进行通信
			HttpGet hg = new HttpGet(url);
			res = httpclient.execute(hg);
			return res.getEntity().getContent(); // 从响应中获取消息实体内容
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	// 点击Menu时,此方法将被激发
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent();
		intent.setClass(LogoInActivity.this, Help.class);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		startActivity(intent);
		return true;
	}
	private long exitTime = 0;
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {  
			   if((System.currentTimeMillis()-exitTime) > 2000) { //System.currentTimeMillis()无论何时调用，肯定大于2000     
					Toast.makeText(getApplicationContext(), "再按一次返回键退至桌面",
								Toast.LENGTH_SHORT).show();
						exitTime = System.currentTimeMillis();  
			         }  
			         else {   // 退出
			             finish();  
			             System.exit(0);  
			         }  
			                   
                    return true;  
           }  
           return super.onKeyDown(keyCode, event);  
    }
}
