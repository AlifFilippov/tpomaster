/*
 * Tencent is pleased to support the open source community by making
 * Tencent GT (Version 2.4 and subsequent versions) available.
 *
 * Notwithstanding anything to the contrary herein, any previous version
 * of Tencent GT shall not be subject to the license hereunder.
 * All right, title, and interest, including all intellectual property rights,
 * in and to the previous version of Tencent GT (including any and all copies thereof)
 * shall be owned and retained by Tencent and subject to the license under the
 * Tencent GT End User License Agreement (http://gt.qq.com/wp-content/EULA_EN.html).
 * 
 * Copyright (C) 2015 THL A29 Limited, a Tencent company. All rights reserved.
 * 
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://opensource.org/licenses/MIT
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.tencent.wstt.gt.plugin.gps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.tencent.wstt.gt.GTApp;
import com.tencent.wstt.gt.R;
import com.tencent.wstt.gt.api.utils.Env;
import com.tencent.wstt.gt.plugin.BaseService;
import com.tencent.wstt.gt.utils.FileUtil;
import com.tencent.wstt.gt.utils.GTUtils;
import com.tencent.wstt.gt.utils.ToastUtil;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

/**
 * GPS????????????
 */
public class GTGPSReplayEngine extends BaseService {
	private static GTGPSReplayEngine INSTANCE;
	private MockGpsProvider mMockGpsProviderTask = null;
	private LocationManager locationManager = null;

	private List<GPSReplayListener> listeners;
	private boolean isReplay = false;
	public String selectedItem;
	public int selectedItemPos = -1;
	
	/*??????gps??????????????????*/
	public int mGPSFileLength = 0;
	/*??????gps?????????????????????*/
	public int index = 0;
	/*????????????*/
	public int mreplayspeed;

	private static final String GPS_MOCK_ACTION = "com.tencent.wstt.gt.ACTION_GPS_MOCK";

	public static GTGPSReplayEngine getInstance() {
		if (null == INSTANCE) {
			INSTANCE = new GTGPSReplayEngine();
		}
		return INSTANCE;
	}

	private GTGPSReplayEngine()
	{
		listeners = new ArrayList<GPSReplayListener>();
	}

	public synchronized void addListener(GPSReplayListener listener)
	{
		listeners.add(listener);
	}

	public synchronized void removeListener(GPSReplayListener listener)
	{
		listeners.remove(listener);
	}

	synchronized public boolean isReplay()
	{
		return isReplay;
	}

	@Override
	public void onCreate(Context context) {
		super.onCreate(context);
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	public void onStart(Intent intent) {
		super.onStart(intent);
		if (null == intent)
		{
			return;
		}
				
		selectedItemPos = intent.getIntExtra("seq", -1);
		int progess = Math.min(100, intent.getIntExtra("progress", 0));
		mreplayspeed = intent.getIntExtra("replayspeed", 1);
		index = getGPSFileLength() * progess / 100;
		if (-1 == selectedItemPos)
		{
			selectedItem = intent.getStringExtra("filename");
			if (null == selectedItem)
			{
				// ???????????????????????????????????????????????????????????????
				String lng = intent.getStringExtra("lng");
				String lat = intent.getStringExtra("lat");
				if (lng == null || lat == null)
				{
					// ??????????????????????????????????????????????????????
					for (GPSReplayListener listener : listeners)
					{
						listener.onReplayFail(
								GTApp.getContext().getString(R.string.pi_gps_replay_tip));
					}
				}
				else
				{
					replay(lng, lat);
				}
			}
			else
			{
				// ??????????????????
				replay(selectedItem);
			}
		}
		else // ???????????????
		{
			// ???????????????????????????????????????????????????
			ArrayList<String> items = GTGPSUtils.getGPSFileList();
			if (items.size() > 0 && items.size() > selectedItemPos && !items.get(0).equals("empty"))
			replay(items.get(selectedItemPos));
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopReplay(); // ????????????
	}

	@Override
	public IBinder onBind() {
		return null;
	}

	private boolean isAllowMock()
	{
		if (!locationManager.isProviderEnabled(MockGpsProvider.GPS_MOCK_PROVIDER)) {
			try
			{
				locationManager.addTestProvider(MockGpsProvider.GPS_MOCK_PROVIDER,
						false, false, false, false, true, false, false, 0, 5);
				locationManager.setTestProviderEnabled(
						MockGpsProvider.GPS_MOCK_PROVIDER, true);
			}
			catch(SecurityException e)
			{
				// ???????????????????????????????????????????????????????????????GPS????????????????????????????????????
				for (GPSReplayListener listener : listeners)
				{
					listener.onReplayFail(
							GTApp.getContext().getString(R.string.pi_gps_warn_tip));
				}
				return false;
			}
		}
		return true;
	}

	/*
	 * ???????????????
	 */
	private void replay(String sLng, String sLat)
	{
		if (! isAllowMock())
		{
			return;
		}

		if (locationManager.isProviderEnabled(MockGpsProvider.GPS_MOCK_PROVIDER)) {
			try {
				double lng = Double.parseDouble(sLng);
				double lat = Double.parseDouble(sLat);

				// check?????????????????????
				if (lng < -180 || lng > 180 || lat < -90 || lat > 90)
				{
					// ???????????????
					for (GPSReplayListener listener : listeners)
					{
						listener.onReplayFail(
								GTApp.getContext().getString(R.string.pi_gps_warn_tip2));
					}
					return;
				}
				// ????????????AsyncTask?????????????????????
				StringBuilder sb = new StringBuilder();
				sb.append(lng);
				sb.append(",");
				sb.append(lat);
				sb.append(",");
				sb.append(0);
				sb.append(",");
				sb.append(0);
				sb.append(",");
				sb.append(0);
				sb.append(",");
				sb.append(0); // ??????????????????????????????
				sb.append(",");
				sb.append(0);
				sb.append(",");
				sb.append(0);
				String[] coordinates = new String[]{sb.toString()};

				if (mMockGpsProviderTask == null) {
					String replayRecordFileName = GTUtils.getSaveDate() + "_.gps";
					mMockGpsProviderTask = new MockGpsProvider(replayRecordFileName);
				}
				isReplay = true;
				mMockGpsProviderTask.execute(coordinates);
			} catch (Exception e) {
				isReplay = false;
				return;
			}
		}
	}

	/*
	 * ????????????
	 */
	private void replay(String fileName)
	{
		if (! isAllowMock())
		{
			return;
		}

		if (locationManager.isProviderEnabled(MockGpsProvider.GPS_MOCK_PROVIDER)) {
			BufferedReader reader= null;
			try {
				List<String> data = new ArrayList<String>();

				File f = new File(Env.ROOT_GPS_FOLDER, fileName);
				InputStream is = new FileInputStream(f);
				reader = new BufferedReader(new InputStreamReader(is));

				String line = null;
				while ((line = reader.readLine()) != null) {
					data.add(line);
				}
				if (data.size() == 0)
				{
					// ???????????????
					for (GPSReplayListener listener : listeners)
					{
						listener.onReplayFail(
								GTApp.getContext().getString(R.string.pi_gps_warn_tip2));
					}
					return;
				}
				mGPSFileLength = data.size();
				// ????????????AsyncTask?????????????????????
				String[] coordinates = new String[mGPSFileLength];
				data.toArray(coordinates);

				if (mMockGpsProviderTask == null) {
					mMockGpsProviderTask = new MockGpsProvider(selectedItem);
				}
				isReplay = true;
				mMockGpsProviderTask.execute(coordinates);
			} catch (Exception e) {
				isReplay = false;
				return;
			}
			finally
			{
				FileUtil.closeReader(reader);
			}
		}
	}

	private void stopReplay()
	{
		isReplay = false;
		try {
			stopMockLocation();
		} catch (Exception e) {
		}
	}

	private void sendMockBroadcast(Context context, String type)
	{
		Intent intent = new Intent();
		intent.setAction(GPS_MOCK_ACTION);
		intent.putExtra("type", type);
		context.sendBroadcast(intent);
	}

	/**
	 * add on 20140630
	 * ????????????????????????????????????????????????????????????????????????GPS??????????????????
	 */
	public void stopMockLocation()
	{
		try {
			mMockGpsProviderTask.cancel(true);
			mMockGpsProviderTask = null;
		} catch (Exception e) {
		}

		try {
			LocationManager locationManager = (LocationManager)
					GTApp.getContext().getSystemService(Context.LOCATION_SERVICE);
			locationManager
					.removeTestProvider(MockGpsProvider.GPS_MOCK_PROVIDER);
		} catch (Exception e) {
		}

		sendMockBroadcast(GTApp.getContext(), "stop");
	}
	
	/**
	 * ????????????GPS???????????????????????????
	 */
	public double getPercentage() {
		if (mGPSFileLength != 0 && index != 0) {
			return ((double) index / (double) mGPSFileLength);
		}
		return 0.0;
	}
	
	/**
	 * ????????????GPS????????????
	 */
	public int getReplaySpeed() {		
		return mreplayspeed;
	}

	/**
	 * ????????????GPS?????????????????????
	 */
	public int getGPSFileLength() {
		return mGPSFileLength;
	}

	private class MockGpsProvider extends AsyncTask<String, Integer, Void> {
		public static final String LOG_TAG = "GpsMockProvider";
		public static final String GPS_MOCK_PROVIDER = LocationManager.GPS_PROVIDER;
		public String orgiFileName;

//		public Integer index = 0;

		public MockGpsProvider(String fileName)
		{
			this.orgiFileName = fileName;
		}

		@Override
		protected Void doInBackground(String... data) {

			boolean hasMockEnd = false;

			double nowtimeStamp;
			List<Long> timezones = new ArrayList<Long>();
			
			sendMockBroadcast(GTApp.getContext(), "start");

			for (GPSReplayListener listener : listeners)
			{
				listener.onReplayStart();
			}


			/*
			 * ?????????????????????1????????????
			 */
			while(true)
			{
				if (!isReplay()) {
					break;
				}
				String str = data[index];// ?????????????????????????????????index?????????????????????
				if (index < data.length - 1) // ?????????????????????????????????
				{
					// add on 20141216 ??????index++????????????????????????????????????
					timezones.add(System.currentTimeMillis());
					if (index + mreplayspeed > data.length - 1) {
						index++;
					} else {
						index += mreplayspeed;
					}
					
				}
				// add on 20150108 ????????????????????????????????????????????????????????????????????????
				else if (!hasMockEnd) {
					sendMockBroadcast(GTApp.getContext(), "end");
					hasMockEnd = true;

					for (GPSReplayListener listener : listeners)
					{
						listener.onReplayEnd();
					}
				}

				Location location = new Location(GPS_MOCK_PROVIDER);
				
				try {
					String[] parts = str.split(",");
					nowtimeStamp = Double.valueOf(parts[6]);
					location.setTime(System.currentTimeMillis());
					location.setLatitude(Double.valueOf(parts[1]));
					location.setLongitude(Double.valueOf(parts[0]));
					location.setAccuracy((Float.valueOf(parts[2])));
					location.setAltitude(Double.valueOf(parts[7]));
					location.setBearing(Float.valueOf(parts[3]));
					location.setSpeed(Float.valueOf(parts[4]));
					/*
					 * ??????setElapsedRealtimeNanos???Android4.0???????????????
					 * ????????????Android4.0??????????????????????????????????????????location??????
					 * location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
					 */
					try
					{
						Method method = Location.class.getMethod("makeComplete");
						if (method != null) {
							method.invoke(location);
						}
					}
					catch (NoSuchMethodException e) {
						// Andorid4.0?????????????????????????????????????????????
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				catch (Exception e) {
					break;
				}

				Log.i(LOG_TAG, location.toString());

				// ????????????????????????
				try
				{
					locationManager.addTestProvider(
							LocationManager.GPS_PROVIDER,
							"requiresNetwork" == "", "requiresSatellite" == "",
							"requiresCell" == "", "hasMonetaryCost" == "",
							"supportsAltitude" == "supportsAltitude",
							"supportsSpeed" == "supportsSpeed",
							"supportsBearing" == "supportsBearing",
							android.location.Criteria.POWER_LOW,
							android.location.Criteria.ACCURACY_FINE);
					locationManager.setTestProviderStatus(GPS_MOCK_PROVIDER,
							LocationProvider.AVAILABLE, null,
							System.currentTimeMillis());
					locationManager.setTestProviderLocation(GPS_MOCK_PROVIDER,
							location);
				}
				catch(Exception e)
				{
					// ????????????????????????????????????????????????
					ToastUtil.ShowLongToast(GTApp.getContext(),
							GTApp.getContext().getString(R.string.pi_gps_warn_tip));
					break;
				}

				// ?????????????????????????????????2s
				if (index == data.length - 1)
				{
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {

					}
				}
				else
				{
					// ???????????????????????????????????????2s???
					int interval = 2000;
					try {
						if (index < data.length) {
							String next = data[index];
							String[] parts = next.split(",");
							interval = (int) ((Double.valueOf(parts[6]) - nowtimeStamp) * 1000);
							
							if (interval <= 0) // ???????????????????????????????????????????????????????????????
							{
								interval = 2000;
							}
						}
						Log.i("interval", String.valueOf(interval));
						if (mreplayspeed == 1)
						{
							Thread.sleep(interval);
						}
						else
						{
							Thread.sleep(1000);
						}
						if (Thread.currentThread().isInterrupted())
							throw new InterruptedException("");
					} catch (InterruptedException e) {
						break;
					}
				}	
			}

			index = 0;
			for (GPSReplayListener listener : listeners)
			{
				listener.onReplayStop();
			}

			/*
			 * add on 20141226 ????????????????????????????????????????????????????????????????????????????????????????????????????????????
			 * TODO ??????????????????GT/Log/gpsreplay/<??????????????????>_log.gps
			 */
			File folder = new File(Env.S_ROOT_LOG_FOLDER + "gpsreplay/");
			folder.mkdirs();
			// GT/Log/gpsreplay/xxxx_log.gps
			String recordFileName = orgiFileName;
			if (orgiFileName.toLowerCase(Locale.ENGLISH).endsWith(".gps"))
			{
				recordFileName = orgiFileName.substring(0, orgiFileName.length() - 4) + "_log.gps";
			}
			
			File f = new File(folder, recordFileName);
			
			BufferedWriter bw = null;

			try {
				f.createNewFile();
				bw = new BufferedWriter(new FileWriter(f));
				for(int i = 0; i < timezones.size(); i++)
				{
					String relayLine = timezones.get(i) + "," + data[i].trim() + "\r\n";
					bw.write(relayLine);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			finally
			{
				FileUtil.closeWriter(bw);
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			Log.d(LOG_TAG, "onProgressUpdate():" + values[0]);
		}
	}
}
