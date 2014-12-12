package com.gionji.gionjihome;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.gionji.gionjihome.U4Xdevice.EkironjiDevice;
import com.gionji.gionjihome.net.UDPClientBroadcastAsyncTask;
import com.gionji.gionjihome.net.UDPClientBroadcastAsyncTask.IPAddressServerListener;
import com.gionji.gionjihome.net.UDPSendCommandThread;
import com.gionji.gionjihome.xmas.LedActivity;
import com.gionji.gionjihome.xmas.RelayActivity;
import com.gionji.gionjihome.xmas.VideoActivity;

public class GHMainActivity extends Activity implements OnClickListener{

	ImageButton[] mImageButtons = new ImageButton[3];
	TextView[] mTextViews       = new TextView[3];
	
	private boolean[] activeButtons      = {true, true, true};
	private String[]  buttonLabels = {"Led","Switch","Video"};
	
	String ghSsid    = "";
	String ipAddress = "";
	String ghId      = "";
	
	public static EkironjiDevice mEkironjiDevice = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_udooxmas);
		
		// function buttons
		mImageButtons[0] = (ImageButton) findViewById(R.id.imageButton1);
		mImageButtons[1] = (ImageButton) findViewById(R.id.imageButton2);
		mImageButtons[2] = (ImageButton) findViewById(R.id.imageButton3);
		
		// buttons labels
		mTextViews[0] = (TextView) findViewById(R.id.textView1);
		mTextViews[1] = (TextView) findViewById(R.id.textView2);
		mTextViews[2] = (TextView) findViewById(R.id.textView3);

		// getting buttons reference and setting onclicklistener
		for(int i=0; i<mImageButtons.length; i++){
			mImageButtons[i].setOnClickListener(this);
			if(activeButtons[i])
				mImageButtons[i].setAlpha(1F);
			else
				mImageButtons[i].setAlpha(0.4F);
		}
		
		// instant
		mEkironjiDevice = new EkironjiDevice(null);
		
		// Check wifi connection
		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled()){
			//wifi is enabled
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if (mWifi.isConnected()) {
				searchUdooOverWifi();
			}
			else{
				startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			}
		}
		else{
			startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.ghmain, menu);
		return true;
	}

	// option to search udoo over wifi again
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			searchUdooOverWifi();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		
		// check if UDOO was found over wifi
		if(!mEkironjiDevice.isUdooPresent()){
			Toast.makeText(getApplicationContext(),
					"No Udoo found over Wifi, try search again in options menu", Toast.LENGTH_SHORT)
					.show();				
			return;
		}
		
		switch(v.getId()){
		case R.id.imageButton1:		
			startActivity(new Intent(this, LedActivity.class));
			break;
		case R.id.imageButton2:	
			startActivity(new Intent(this, VideoActivity.class));
			break;
		case R.id.imageButton3:	
			startActivity(new Intent(this,  RelayActivity.class));
			break;
		}
	}

	private void searchUdooOverWifi(){
		UDPClientBroadcastAsyncTask task = new UDPClientBroadcastAsyncTask(this);
		task.setIPAddressServerListener(new IPAddressServerListener() {
			@Override
			public void IPAddressServerFounded(String response) {

				Toast.makeText(getApplicationContext(),
						"UDOO found! ip: " +response, Toast.LENGTH_SHORT)
						.show();	
				
				mEkironjiDevice.setIpAddress(ipAddress);
			}

			@Override
			public void IPAddressServerFailed() {
				Toast.makeText(getApplicationContext(),
						"UDOO not found :-(", Toast.LENGTH_SHORT).show();
			}
		});
		task.setProgressDialogMessage("Wait until Udoo was found...");
		task.execute();
		
	}
		
	static public String getIpAddress(String msg){
		if (msg.contains("@"))
			return msg.split("@")[1];
		else
			return msg;
	}
	
	
}
