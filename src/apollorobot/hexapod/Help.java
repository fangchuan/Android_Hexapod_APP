package apollorobot.hexapod;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TextView;


public class Help extends Activity {
	private TextView msg = null;							// �ı���ʾ���
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		msg = (TextView) super.findViewById(R.id.msg);	// �ҵ����
		Resources res = super.getResources();					// ������Դ
		InputStream input = res.openRawResource(R.raw.usage);	// ��ȡ��ԴID
		Scanner scan = new Scanner(input);						// ʵ����Scanner
		StringBuffer buf = new StringBuffer();					// ��������
		while (scan.hasNext()) {								// ѭ����ȡ
			buf.append(scan.next()).append("\n");				// ��������
		}
		scan.close();											// �ر�������
		try {													// �ر�������
			input.close() ;
		} catch (IOException e) {
			e.printStackTrace();
		}
		msg.setText(buf.toString());						// ��������
	}
}
