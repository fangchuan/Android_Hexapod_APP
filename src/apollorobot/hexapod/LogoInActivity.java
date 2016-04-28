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
	private InputStream is = null;// http������
	private Context mContext = this;
	private WifiManager wifi = null;
	
	private String Ip_address = "";
	private String Socketport = "";
	public int temp_connect;
	public String str_recive;
	public byte[] recive_buffer = new byte[1024];
	@Override
	// ��һ�δ���Activity�Ե���
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// �����������£������޷�Socket����
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads()
                .detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
                .penaltyLog().penaltyDeath().build());
        
		setContentView(R.layout.logoin_layout);
		VideoAddredit    = (EditText)findViewById(R.id.editIP);		 // ��Ƶ��ַ
		CtrlPortedit     = (EditText)findViewById(R.id.editCtrlPort);// Socket�˿�
//		ConnectButton    = (Button)findViewById(R.id.button_go);
		PortEdt          = (EditText)findViewById(R.id.videoportedit);
		// ���ã�����WIFI
		wifi             = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// ���wifi�ĵ�ǰ״̬
		int state        = wifi.getWifiState();

		// state = 2;  ����wifi�����ܴ򿪸ó���
		if (state != WifiManager.WIFI_STATE_ENABLED){
			Generic.showMsg(this, "���wifi", false);
			finish();
		}
		
	}
	// �����Ӱ���
	public void connectBtn(View v){
		String ip = VideoAddredit.getText().toString();
		String port = PortEdt.getText().toString();
		// port����Ϊ��
		if(!port.equals("") && checkAddr(ip ,Integer.valueOf(port))){
			String action = "http://" + ip + ":" + port + "/?action=stream";
			new ManualTask().execute(action);
		}else{
			Generic.showMsg(this, "����ip��port", true);
		}
	}

	// ����û������IP����ȷ��
	private boolean checkAddr(String ip, int port){
			if(ip.split("\\.").length != 4)
				return false;
			if(port < 1000 || port > 65535)
				return false;

			return true;	
	}
	
	// �ֶ������߳�
	private class ManualTask extends AsyncTask<String, Integer, String>{
		
		@Override
		protected String doInBackground(String... params) {
			is = http(params[0]);// ȡ��ÿһ��Ip������Http����
			if (is != null)
				MjpegInputStream.initInstance(is);
			return null;
		}

	// ��ת��MainActivity
	protected void onPostExecute(String result) {
		if (is != null) {
			Intent intent = new Intent();
			String Ip_address = VideoAddredit.getText().toString(); // Socket/http ��Ip��ַ
			String Socketport = CtrlPortedit.getText().toString();  // Socket �˿�
			// ��IP�Ͷ˿ڴ���Intent
			intent.putExtra("CtrlIp", Ip_address);
			intent.putExtra("CtrlPort", Socketport);
			intent.setClass(LogoInActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}else
		{
			Generic.showMsg(mContext, "����ʧ��", true);
		}
		super.onPostExecute(result);
	}
}
	// ����Http����
	private InputStream http(String url){
		HttpResponse res;
		// ����Http�ͻ���
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 300);
		try {
			//����Http��Get��ʽ����ͨ��
			HttpGet hg = new HttpGet(url);
			res = httpclient.execute(hg);
			return res.getEntity().getContent(); // ����Ӧ�л�ȡ��Ϣʵ������
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	// ���Menuʱ,�˷�����������
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
			   if((System.currentTimeMillis()-exitTime) > 2000) { //System.currentTimeMillis()���ۺ�ʱ���ã��϶�����2000     
					Toast.makeText(getApplicationContext(), "�ٰ�һ�η��ؼ���������",
								Toast.LENGTH_SHORT).show();
						exitTime = System.currentTimeMillis();  
			         }  
			         else {   // �˳�
			             finish();  
			             System.exit(0);  
			         }  
			                   
                    return true;  
           }  
           return super.onKeyDown(keyCode, event);  
    }
}
