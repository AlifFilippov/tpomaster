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
package com.tencent.wstt.gt.data.local;

import com.tencent.wstt.gt.QueryPerfEntry;

/**
 * 注意local的不是给aidl调用的，不需要实现Parcelable接口
 * 暂无此性能需求，备用的扩展类
 */
public class LocalStringDataPerfEntry {
	long logTime;
	String data;
	QueryPerfEntry queryEntry;
	
	public long getLogTime() {
		return logTime;
	}
	
	public String getData() {
		return data;
	}
	
	public QueryPerfEntry getQueryEntry() {
		return queryEntry;
	}
	
	public void setLogTime(long logTime) {
		this.logTime = logTime;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setQueryEntry(QueryPerfEntry queryEntry) {
		this.queryEntry = queryEntry;
	}
}
