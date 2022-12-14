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
package com.tencent.wstt.gt.activity;

import java.util.LinkedList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.wstt.gt.OutPara;
import com.tencent.wstt.gt.R;
import com.tencent.wstt.gt.api.utils.Env;
import com.tencent.wstt.gt.api.utils.NetUtils;
import com.tencent.wstt.gt.log.GTGWInternal;
import com.tencent.wstt.gt.log.GWSaveEntry;
import com.tencent.wstt.gt.log.LogUtils;
import com.tencent.wstt.gt.manager.ClientManager;
import com.tencent.wstt.gt.manager.OpPerfBridge;
import com.tencent.wstt.gt.ui.model.TagTimeEntry;
import com.tencent.wstt.gt.utils.StringUtil;
import com.tencent.wstt.gt.utils.ToastUtil;
import com.tencent.wstt.gt.views.GTOutParaPerfDialog;
import com.tencent.wstt.gt.views.GTPerfDetailView;

/**
 * ????????????????????????????????????????????????????????????????????????
 */
public class GTOpSinglePerfActivity extends GTBaseActivity {
	
	private TagTimeEntry dataSet; // ????????????????????????
	private String about = ""; // ????????????
	private OutPara op; // ????????????????????????????????????????????????????????????????????????????????????

	private ImageButton btn_back;
//	private ImageButton btn_save;
	private ImageButton btn_delete;
	private EditText et_savePath1;
	private EditText et_savePath2;
	private EditText et_savePath3;
	private EditText et_saveTestDesc;
	private AlertDialog dlg_save;

	private TextView tvTitle;
	private TextView tvValue;
	private TextView tvTimes;
	private TextView tvMin;
	private TextView tvMax;
	private TextView tvAve;
	private TextView tvWarningCnt;
	private TextView tvWaringArea;

	private TextView tvKey;
	private TextView tvAbout;

	private EditText etUpperInterval;
	private EditText etUpperValue;
	private EditText etLowerValue;
	
	private LinearLayout ll_fold;
	private ImageView img_bottom_arrow;

	// ?????????????????????
	private int lastdataSetLength = 0;

	LinkedList<String> chartData = new LinkedList<String>();

	GTPerfDetailView chartView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gt_perf_op_detail);
		
		Intent intent = this.getIntent();
		
		Bundle extras = intent.getExtras();
		final String name = extras.getString("name");
		final String alias = extras.getString("alias");
		final String client = extras.getString("client");
		
		// ??????????????????????????????
		dataSet = OpPerfBridge.getProfilerData(name);
		// ??????????????????????????????????????????
		op = ClientManager.getInstance().getClient(client).getOutPara(name);

		// ???onCreat????????????????????????????????????????????????????????????
		if (null == dataSet)
		{
//			finish();
//			return;
			dataSet = new TagTimeEntry(null);
			dataSet.setName(name);
			dataSet.setExkey(ClientManager.getInstance().getClientKey(client));
		}

		about = dataSet.getDesc();
		
		tvTitle = (TextView)findViewById(R.id.perf_detail_title);
		tvTitle.setText(alias);
		
		tvKey = (TextView)findViewById(R.id.op_perf_detail_key);
		tvKey.setText(name);
		
		tvAbout = (TextView)findViewById(R.id.op_perf_detail_about);
		tvAbout.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				GTOutParaPerfDialog dialog =
						new GTOutParaPerfDialog(v.getContext(), alias, name, about);
				dialog.show();
			}
		});
		
		btn_back = (ImageButton)findViewById(R.id.perf_detail_back);
		btn_back.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}
		});
		
//		btn_save = (ImageButton)findViewById(R.id.perf_detail_save);
//		btn_save.setOnClickListener(new OnClickListener() {
//
//			public void onClick(View v) {
//				String lastSaveLog = GTGWInternal.getLastSaveFolder();
//				if (lastSaveLog != null && lastSaveLog.contains(".")
//						&& lastSaveLog.endsWith(LogUtils.TLOG_POSFIX))
//				{
//					lastSaveLog = lastSaveLog.substring(0, lastSaveLog.lastIndexOf("."));
//				}
//				et_savePath3.setText(lastSaveLog.trim());
//				dlg_save.show();
//			}
//		});
		
		btn_delete = (ImageButton)findViewById(R.id.perf_detail_delete);
		btn_delete.setOnClickListener(showDeleteDlg);
		
		// ?????????
		final LinearLayout ll_warnArea = (LinearLayout)findViewById(R.id.op_perf_detail_warnarea);
		img_bottom_arrow = (ImageView)findViewById(R.id.bottom_arrow);
		if (dataSet.getThresholdEntry().isEnable())
		{
			img_bottom_arrow.setBackgroundResource(R.drawable.unfold_arrow);
			ll_warnArea.setVisibility(View.VISIBLE);
		}
		else
		{
			img_bottom_arrow.setBackgroundResource(R.drawable.fold_arrow);
			ll_warnArea.setVisibility(View.GONE);
		}
		
		// ??????????????????
		tvWaringArea = (TextView)findViewById(R.id.op_perf_detail_interval_toast);
		if (!dataSet.getThresholdEntry().isEnable())
		{
			tvWaringArea.setText(getString(R.string.warning_title_disable));
		}
		
		// ?????????
		ll_fold = (LinearLayout)findViewById(R.id.warning_fold);
		ll_fold.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(dataSet.getThresholdEntry().isEnable()){
					dataSet.getThresholdEntry().setEnable(false);
					img_bottom_arrow.setBackgroundResource(R.drawable.fold_arrow);
					ll_warnArea.setVisibility(View.GONE);
					tvWaringArea.setText(getString(R.string.warning_title_disable));
				}else{
					dataSet.getThresholdEntry().setEnable(true);
					img_bottom_arrow.setBackgroundResource(R.drawable.unfold_arrow);
					ll_warnArea.setVisibility(View.VISIBLE);
					tvWaringArea.setText(getString(R.string.warning_title));
				}
			}
		});
		
		etUpperInterval = (EditText)findViewById(R.id.op_perf_detail_upper_interval);
		etUpperValue = (EditText)findViewById(R.id.op_perf_detail_upper_value);
		etLowerValue = (EditText)findViewById(R.id.op_perf_detail_lower_value);
		
		// ??????????????????????????????????????????????????????
		if (!op.hasMonitorOnce)
		{
			etUpperInterval.setEnabled(false);
			etUpperValue.setEnabled(false);
			etLowerValue.setEnabled(false);
//			isfoldWarnArea = true;
//			img_bottom_arrow.setBackgroundResource(R.drawable.unfold_arrow);
//			ll_warnArea.setVisibility(View.GONE);
		}
		else
		{
			etUpperInterval.setInputType(EditorInfo.TYPE_CLASS_PHONE);
			etUpperValue.setInputType(EditorInfo.TYPE_CLASS_PHONE);
			etLowerValue.setInputType(EditorInfo.TYPE_CLASS_PHONE);
			
			etUpperInterval.setOnKeyListener(thresholdKeyListener);
			etUpperValue.setOnKeyListener(thresholdKeyListener);
			etLowerValue.setOnKeyListener(thresholdKeyListener);
			
			int upperDuration = dataSet.getThresholdEntry().getduration();
			if (upperDuration != Integer.MAX_VALUE)
			{
				etUpperInterval.setText(Integer.toString(upperDuration));
			}
			
			double upperValue = dataSet.getThresholdEntry().getUpperValue();
			if (upperValue != Integer.MAX_VALUE)
			{
				etUpperValue.setText(Double.toString(upperValue));
			}
			
			double lowerValue = dataSet.getThresholdEntry().getLowerValue();
			if (lowerValue != Integer.MIN_VALUE)
			{
				etLowerValue.setText(Double.toString(lowerValue));
			}
		}

		RelativeLayout rl_save = (RelativeLayout) LayoutInflater.from(this).inflate(
				R.layout.gt_dailog_save_gw, null, false);
		ImageButton btn_cleanSavePath = (ImageButton) rl_save.findViewById(R.id.save_clean);
		btn_cleanSavePath.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				et_savePath3.setText("");
			}
		});
		
		et_savePath3 = (EditText) rl_save.findViewById(R.id.save_editor);
		String lastSaveLog = GTGWInternal.getLastSaveFolder();
		if (lastSaveLog != null && lastSaveLog.contains(".")
				&& lastSaveLog.endsWith(LogUtils.TLOG_POSFIX))
		{
			lastSaveLog = lastSaveLog.substring(0, lastSaveLog.lastIndexOf("."));
		}
		et_savePath3.setText(lastSaveLog);

		et_savePath1 = (EditText) rl_save.findViewById(R.id.save_editor_folder_parent1);
		et_savePath1.setText(Env.CUR_APP_NAME);
		et_savePath2 = (EditText) rl_save.findViewById(R.id.save_editor_folder_parent2);
		et_savePath2.setText(Env.CUR_APP_VER);

		et_saveTestDesc = (EditText) rl_save.findViewById(R.id.save_editor_desc);
		
		dlg_save = new Builder(this)
		.setTitle(getString(R.string.save))
		.setView(rl_save)
		.setPositiveButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				})
		.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String path1 = et_savePath1.getText().toString().trim();
				if (!StringUtil.isLetter(path1))
				{
					ToastUtil.ShowShortToast(
							GTOpSinglePerfActivity.this, getString(R.string.save_folder_valid));
					return;
				}

				String path2 = et_savePath2.getText().toString().trim();
				if (!StringUtil.isLetter(path1))
				{
					ToastUtil.ShowShortToast(
							GTOpSinglePerfActivity.this, getString(R.string.save_folder_valid));
					return;
				}

				String path3 = et_savePath3.getText().toString().trim();
				if (!StringUtil.isLetter(path3))
				{
					ToastUtil.ShowShortToast(
							GTOpSinglePerfActivity.this, getString(R.string.save_folder_valid));
					return;
				}

				String testDesc = et_saveTestDesc.getText().toString().trim();

				GWSaveEntry saveEntry = new GWSaveEntry(path1, path2, path3, testDesc);
				GTGWInternal.saveGWData(saveEntry, dataSet);
				dialog.dismiss();
			}
		}).create();
		
		tvValue = (TextView) findViewById(R.id.op_perf_detail_value);
		tvTimes = (TextView) findViewById(R.id.bh_perf_detail_times);
		tvMin = (TextView) findViewById(R.id.bh_perf_detail_min);
		tvMax = (TextView) findViewById(R.id.bh_perf_detail_max);
		tvAve = (TextView) findViewById(R.id.bh_perf_detail_ave);
		tvWarningCnt = (TextView) findViewById(R.id.bh_perf_detail_warning_cnt);

		if (op == null)
		{
			tvValue.setText(dataSet.getLastValue());
		}
		else
		{
			tvValue.setText(op.getValue());
		}
		
		TagTimeEntry anchorEntry = dataSet;

		if (dataSet.getSubTagEntrys().length > 0)
		{
			anchorEntry = dataSet.getSubTagEntrys()[0];
		}

		tvTimes.setText(anchorEntry.getRecordSizeText());
		tvMin.setText(anchorEntry.getMin());
		tvMax.setText(anchorEntry.getMax());
		tvAve.setText(anchorEntry.getAve());
		tvWarningCnt.setText(Integer.toString(
				anchorEntry.getThresholdEntry().getUpperWariningCount()
				+ anchorEntry.getThresholdEntry().getLowerWariningCount()));
		
		LinearLayout ll_chart = (LinearLayout) findViewById(R.id.bh_perf_detail_chart);
		
		chartView = new GTPerfDetailView(this, dataSet);
		chartView.setInput(0);
		ll_chart.addView(chartView);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		chartView.postInvalidate(); // ????????????x???y?????????

		// ????????????????????????
		handler.post(task);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public void onStop() {
		handler.removeCallbacks(task);
		super.onStop();
	}

	/*
	 * ?????????????????????????????????
	 */
	private void createRealtimeData() { 
		
		TagTimeEntry anchorEntry = dataSet;

		if (dataSet.getSubTagEntrys().length > 0)
		{
			anchorEntry = dataSet.getSubTagEntrys()[0];
		}
		
		if (anchorEntry.getRecordSize() == lastdataSetLength)
		{
			return;
		}
		
		lastdataSetLength = anchorEntry.getRecordSize();
		int start = lastdataSetLength > GTPerfDetailView.xMax ? lastdataSetLength - GTPerfDetailView.xMax : 0;
		chartView.setInput(start);
		chartView.postInvalidate();
	}
	
	private int delaytime = 1000;
	private Handler handler = new Handler();
	private Runnable task = new Runnable() {
		public void run() {
			TagTimeEntry anchorEntry = dataSet;

			if (dataSet.getSubTagEntrys().length > 0)
			{
				anchorEntry = dataSet.getSubTagEntrys()[0];
			}

			tvTimes.setText(anchorEntry.getRecordSizeText());
			tvMin.setText(anchorEntry.getMin());
			tvMax.setText(anchorEntry.getMax());
			tvAve.setText(anchorEntry.getAve());
			tvWarningCnt.setText(Integer.toString(
					anchorEntry.getThresholdEntry().getUpperWariningCount()
					+ anchorEntry.getThresholdEntry().getLowerWariningCount()));

			if (op == null)
			{
				tvValue.setText(dataSet.getLastValue());
			}
			else
			{
				tvValue.setText(op.getValue());
			}

			if (chartView.isAutoRefresh())
			{
				createRealtimeData();
			}

			handler.postDelayed(this, delaytime);
		}
	};
	
	private void cancelFilterMsgInput(View v) {
		InputMethodManager imm = (InputMethodManager) v.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
	}
	
	private OnClickListener showDeleteDlg = new OnClickListener() {
		public void onClick(View v) {
			
			if (null == dataSet || dataSet.getRecordSize() == 0)
			{
				return;
			}
			
			AlertDialog.Builder builder = new Builder(GTOpSinglePerfActivity.this);
			builder.setMessage(getString(R.string.clear_and_reset_tip));
			builder.setTitle(getString(R.string.clear_and_reset));
			builder.setPositiveButton(getString(R.string.cancel),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.setNegativeButton(getString(R.string.ok),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// UI????????????dataSet
							dataSet.clear();
							tvTimes.setText("");
							tvMin.setText("");
							tvMax.setText("");
							tvAve.setText("");
							tvWarningCnt.setText("");
							tvValue.setText("");

							// ???????????????????????????????????????????????????????????????????????????????????????
							chartView.setAutoRefresh(true);
							chartView.setInput(0);
							chartView.postInvalidate();

							// ????????????????????????????????????reset
							String key = dataSet.getName();
							NetUtils.clearNetValue(key);

							dialog.dismiss();
						}
					});
			builder.show();
		}
	};
	
	private OnKeyListener thresholdKeyListener = new OnKeyListener() {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_ENTER:
				
				cancelFilterMsgInput(v);
				
				String sUpperDuration = etUpperInterval.getText().toString();
				String sUpperValue = etUpperValue.getText().toString();
				String sLowerValue = etLowerValue.getText().toString();
				
				int duration = dataSet.getThresholdEntry().getduration();
				double upperValue = dataSet.getThresholdEntry().getUpperValue();
				double lowerValue = dataSet.getThresholdEntry().getLowerValue();
				
				try
				{
					if (!"".equals(sUpperDuration))
					{
						duration = Integer.parseInt(sUpperDuration);
					}
					else // ??????????????????????????????
					{
						duration = Integer.MAX_VALUE;
					}

					if (!"".equals(sUpperValue))
					{
						upperValue = Double.parseDouble(sUpperValue);
					}
					else // ??????????????????????????????
					{
						upperValue = Integer.MAX_VALUE;
					}
					
					if (!"".equals(sLowerValue))
					{
						lowerValue = Double.parseDouble(sLowerValue);
					}
					else // ??????????????????????????????
					{
						lowerValue = Integer.MIN_VALUE;
					}

					dataSet.getThresholdEntry().setThreshold(
							duration, upperValue, lowerValue);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					ToastUtil.ShowLongToast(
							GTOpSinglePerfActivity.this, getString(R.string.digit_valid));
				}

				return true;
			}
			return false;
		}
	};
}
