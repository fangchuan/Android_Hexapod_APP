package apollorobot.hexapod;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;



import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;

public class MjpegInputStream extends DataInputStream implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private final byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };
	private final byte[] EOF_MARKER = { (byte) 0xFF, (byte) 0xD9 };
	private final String CONTENT_LENGTH = "Content-Length";
	private final static int HEADER_MAX_LENGTH = 100;
	private final static int FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH;
	private int mContentLength = -1;
	private static MjpegInputStream mis = null;
	
	
	public static void initInstance(InputStream is){
		if(mis == null)
			mis = new MjpegInputStream(is);
		
	}
	
	public static MjpegInputStream getInstance(){
		if(mis != null)
			return mis;
		
		return null;
	}
	// 关闭输入流
	public static void closeInstance(){
		try {
			mis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mis = null;
	}

	private MjpegInputStream(InputStream in) {
		super(new BufferedInputStream(in, FRAME_MAX_LENGTH));
	}

	private int getEndOfSeqeunce(DataInputStream in, byte[] sequence)
			throws IOException {
		int seqIndex = 0;
		byte c;
		for (int i = 0; i < FRAME_MAX_LENGTH; i++) {
			c = (byte) in.readUnsignedByte();
			if (c == sequence[seqIndex]) {
				seqIndex++;
				if (seqIndex == sequence.length)
					return i + 1;
			} else
				seqIndex = 0;
		}
		return -1;
	}

	private int getStartOfSequence(DataInputStream in, byte[] sequence)
			throws IOException {
		int end = getEndOfSeqeunce(in, sequence);
		return (end < 0) ? (-1) : (end - sequence.length);
	}
	// 获得数据帧的长度
	private int parseContentLength(byte[] headerBytes) throws IOException,
			NumberFormatException {
		ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
		Properties props = new Properties();
		props.load(headerIn);
		return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
	}
	// 读视频流
	public Bitmap readMjpegFrame() throws IOException {
		mark(FRAME_MAX_LENGTH);
		int headerLen = getStartOfSequence(this, SOI_MARKER);
		reset();
		byte[] header = new byte[headerLen];
		readFully(header);
		String s = new String(header);
		try {
			mContentLength = parseContentLength(header);
		} catch (NumberFormatException e) {
			return null;
		}
		byte[] frameData = new byte[mContentLength];
		readFully(frameData);
		
		return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
	}
	
	
}
