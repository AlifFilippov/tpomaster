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
package com.tencent.wstt.gt.manager;

import java.util.concurrent.LinkedBlockingQueue;

import com.tencent.wstt.gt.GTApp;
import com.tencent.wstt.gt.GTConfig;
import com.tencent.wstt.gt.OutPara;
import com.tencent.wstt.gt.R;
import com.tencent.wstt.gt.activity.GTMainActivity;
import com.tencent.wstt.gt.service.GTServiceController;
import com.tencent.wstt.gt.ui.model.TagTimeEntry;
import com.tencent.wstt.gt.ui.model.WarningEntry;
import com.tencent.wstt.gt.utils.NotificationHelper;

public class OpWarningManager {
	private LinkedBlockingQueue<WarningEntry> queue;
	private Thread warningThread;
	private boolean started;

	private static OpWarningManager INSTANCE = new OpWarningManager();

	private OpWarningManager() {
		queue = new LinkedBlockingQueue<WarningEntry>(100);
		start();
	}

	public static OpWarningManager getInstance()
	{
		return INSTANCE;
	}

	public void add(WarningEntry e) {
		queue.offer(e);
	}

	public synchronized void start() {
		if (!started) {
			started = true;
			warningThread = new Thread(consumer, getClass().getSimpleName());
			warningThread.start();
		}
	}

	public synchronized void stop() {
		if (started) {
			started = false;
			warningThread = null;
			add(new WarningEntry(null, 0, 0)); // ????????????????????????????????????????????????
		}
	}

	private Runnable consumer = new Runnable() {
		@Override
		public void run() {
			while (started) {
				try {
					WarningEntry e = queue.take(); // ???????????????
					if (!started) {
						// ????????????
						queue.clear();
						return;
					}
					TagTimeEntry src = e.src;
					while (src.getParent() != null && src.getParent() instanceof TagTimeEntry)
					{
						src = (TagTimeEntry) src.getParent();
					}
					Client client = ClientManager.getInstance().getClient(src.getExkey());
					String ov_name = src.getName();
					String version_type = "Release";
					if (1 == GTConfig.VERSION_TYPE) {
						version_type = "Develop";
					}
					GTMainActivity.notification = NotificationHelper
							.genNotification(GTApp.getContext(), 0,
									R.drawable.gt_entrlogo, "GT", 0,
									"Version: " + version_type + " "
											+ GTConfig.VERSION, ov_name +" exceeds the threshold",
									GTMainActivity.class,
									true, false,
									NotificationHelper.DEFAULT_VB);
					
					NotificationHelper.notify(GTApp.getContext(), 10,
							GTMainActivity.notification);
					OutPara ov = client.getOutPara(ov_name);
					ov.setAlert(true);
					GTServiceController.INSTANCE.show_alert = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	};
}
