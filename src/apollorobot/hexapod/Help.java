package apollorobot.hexapod;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TextView;


public class Help extends Activity {
	private TextView msg = null;							// 文本显示组件
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		msg = (TextView) super.findViewById(R.id.msg);	// 找到组件
		Resources res = super.getResources();					// 操作资源
		InputStream input = res.openRawResource(R.raw.usage);	// 读取资源ID
		Scanner scan = new Scanner(input);						// 实例化Scanner
		StringBuffer buf = new StringBuffer();					// 接收数据
		while (scan.hasNext()) {								// 循环读取
			buf.append(scan.next()).append("\n");				// 保存数据
		}
		scan.close();											// 关闭输入流
		try {													// 关闭输入流
			input.close() ;
		} catch (IOException e) {
			e.printStackTrace();
		}
		msg.setText(buf.toString());						// 设置文字
	}
}
