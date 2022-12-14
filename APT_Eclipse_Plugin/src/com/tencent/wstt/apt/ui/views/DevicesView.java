/**
 * Tencent is pleased to support the open source community by making APT available.
 * Copyright (C) 2014 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */


package com.tencent.wstt.apt.ui.views;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.tencent.wstt.apt.action.DumpHprofAction;
import com.tencent.wstt.apt.action.GCAction;
import com.tencent.wstt.apt.action.GetPMAPInfoAction;
import com.tencent.wstt.apt.action.GetSMAPInfoAction;
import com.tencent.wstt.apt.action.StartTestAction;
import com.tencent.wstt.apt.adb.DDMSUtil;
import com.tencent.wstt.apt.cmdparse.GetDeviceInfo;
import com.tencent.wstt.apt.console.APTConsoleFactory;
import com.tencent.wstt.apt.console.StatusBar;
import com.tencent.wstt.apt.data.Constant;
import com.tencent.wstt.apt.data.APTState.APTEventEnum;
import com.tencent.wstt.apt.data.Constant.PhoneState;
import com.tencent.wstt.apt.data.APTState;
import com.tencent.wstt.apt.data.DeviceInfo;
import com.tencent.wstt.apt.data.PCInfo;
import com.tencent.wstt.apt.data.PkgInfo;
import com.tencent.wstt.apt.data.TestSence;
import com.tencent.wstt.apt.file.APTLogFileParse;
import com.tencent.wstt.apt.file.APTLogFileParse.APTLogFileHeader;
import com.tencent.wstt.apt.file.APTLogFileParse.JfreeChartDatas;
import com.tencent.wstt.apt.statistics.actions.CopyAllFromTableViewAction;
import com.tencent.wstt.apt.util.GetAdbPathUtil;
import com.tencent.wstt.apt.util.GetCurCheckedStateUtil;


/**
 * @Description * ???????????? ???1?????????????????????????????? ???2???????????????????????????
 * @date 2013???11???10??? ??????6:14:13
 * 
 */
public class DevicesView extends ViewPart {

	
	public static final String ID = "com.tencent.wstt.apt.ui.views.DevicesView";
	
	private Text pkgNameText;
	public Button addBtn;
	
	public TableViewer targetPkgTableViewer;
	public TableViewer sourcePkgTableViewer;
	private CheckboxTableViewer ctv;//????????????tableviewer??????checkbox
	

	// ?????????action
	public Action testAction;
	public Action refreshAction;
	public Action openResultDirAction;
	public Action openLogWithChartAction;
	
	// ???????????????action
	private Action copyAction;
	private Action addPkgAction;
	private Action removePkgAction;
	private Action pmapAction;
	private Action smapAction;
	private Action dumpHprofAction;
	private Action gcAction;

	// ????????????????????????
	public static final String[] TARGET_COLUMN_NAME = { "??????????????????", "PID"};
	public static final String[] SOURCE_COLUMN_NAME = { "??????????????????", "PID"};

	//????????????tableviewer???????????????????????????
	private boolean isSupportAddOrDeleteOper = true;
	
	//????????????tableviewer??????checkbox??????
	private boolean isSupportCheckChangePer = true;
	
	public void setAddAndDelOperEnable(boolean isEnable)
	{
		isSupportAddOrDeleteOper = isEnable;
	}
	
	public void setCheckChangeEnable(boolean isEnable)
	{
		isSupportCheckChangePer = isEnable;
	}
	
	public DevicesView() {
		initAPT();
	}

	@Override
	//?????????view??????????????????????????????????????????????????????????????????????????????????????????????????????
	//???API?????????????????????APT?????????????????????????????????
	//??????????????????????????????????????????????????????
	//??????Activator?????????????????????????????????????????????????????????????????????close???????????????
	//???????????????????????????????????????eclipse?????????????????????????????????????????????????????????????????????
	//???????????????????????????
	public void createPartControl(Composite parent) {
			
		parent.setLayout(new FormLayout());
		
		createSubTitle(parent);
		
		createTargetPkgTableView(parent);
		// ?????????tableviewer
		createTableView(parent);
		// ????????????????????????????????????
		createMenuAndToolBar();
		
		refreshGetPkgInfo();
			
		}

	@Override
	public void setFocus() {
		
	}

	@Override
	public void dispose()
	{

	}
	/**
	 * ??????view?????????title setPartName???viewPart????????????protected??????
	 */
	public void setPartName(String name) {
		super.setPartName(name);
	}

	
	public void getTargetPkgInfoList()
	{
		int pkgNumber = targetPkgTableViewer.getTable().getItemCount();
		
		TestSence.getInstance().pkgInfos.clear();
		//????????????????????????
		for(int i = 0; i < pkgNumber; i++)
		{
			PkgInfo item = (PkgInfo)targetPkgTableViewer.getTable().getItem(i).getData();
			TestSence.getInstance().pkgInfos.add(item);
		}
	}
	
	/**
	* @Title: getPkgChecked  
	* @Description:   
	* @return 
	* boolean[] 
	* @throws
	 */
	public boolean[] getPkgChecked()
	{
		int count = targetPkgTableViewer.getTable().getItemCount();
		if(count == 0)
		{
			return null;
		}
		boolean[] result = new boolean[count];
		for(int i = 0; i < count; i++)
		{
			result[i] = targetPkgTableViewer.getTable().getItem(i).getChecked();
		}
		return result;
	}


	/**
	 * ????????????????????????????????????
	* @Title: clearTargetPkgTableViewer  
	* @Description:    
	* void 
	* @throws
	 */
	public void clearTargetPkgTableViewerForNotUIThread()
	{
		targetPkgTableViewer.getTable().getDisplay().asyncExec(new Runnable() {	
			@Override
			public void run() {
				targetPkgTableViewer.getTable().removeAll();
			}
		});	
	}
	
	public void clearTargetPkgTableViewerForUIThread()
	{
		targetPkgTableViewer.getTable().removeAll();
	}
	
	/**
	 * ????????????PKG???PID???
	* @Title: updatePkgPid  
	* @Description:   
	* @param index
	* @param pid 
	* void 
	* @throws
	 */
	public void updatePkgPid(final int index, final String pid)
	{
		targetPkgTableViewer.getTable().getDisplay().asyncExec(new Runnable() {	
			@Override
			public void run() {
				targetPkgTableViewer.getTable().getItem(index).setText(1, pid);
			}
		});
	}
	/**
	 * 
	 * @param parent
	 */
	private void createSubTitle(Composite parent) {
		FormData pkgNameLabelFromData = new FormData();
		pkgNameLabelFromData.top = new FormAttachment(0, 5);
		pkgNameLabelFromData.left = new FormAttachment(0, 5);
		pkgNameLabelFromData.right = new FormAttachment(100, -50);
		pkgNameLabelFromData.bottom = new FormAttachment(parent, 30);// ????????????
		pkgNameText = new Text(parent, SWT.BORDER);
		pkgNameText.setToolTipText("??????????????????????????????????????????????????????");
		pkgNameText.setBackground(new Color(Display.getCurrent(), 192, 192, 192));
		pkgNameText.setLayoutData(pkgNameLabelFromData);
		
		FormData addBtnFromData = new FormData();
		addBtnFromData.top = new FormAttachment(0, 5);
		addBtnFromData.left = new FormAttachment(pkgNameText, 5);
		addBtnFromData.right = new FormAttachment(100, -5);
		addBtnFromData.bottom = new FormAttachment(parent, 30);// ????????????
	
		addBtn = new Button(parent, SWT.NONE);
		addBtn.setText("??????");
		addBtn.setLayoutData(addBtnFromData);
		addBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(isSupportAddOrDeleteOper)
				{
					APTState.getInstance().DealWithEventBefore(APTEventEnum.CONFIGRURE_OPER);
					String text = pkgNameText.getText().trim();
					if(text.equals(""))
					{
						APTConsoleFactory.getInstance().APTPrint("????????????????????????");
						return;
					}
					
					PkgInfo dataItem = new PkgInfo();
					dataItem.contents[PkgInfo.NAME_INDEX] = text;
					
					dataItem.contents[PkgInfo.PID_INDEX] = Constant.PID_NOT_EXSIT;
					
					addDataItem(targetPkgTableViewer, dataItem);
					APTState.getInstance().DealWithEventAfter(APTEventEnum.CONFIGRURE_OPER);
				}
				else
				{
					APTConsoleFactory.getInstance().APTPrint("Operation forbid");
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {				
			}
		});
	}

	/**
	 * ????????????????????????
	* @Title: createTargetPkgTableView  
	* @Description:   
	* @param parent 
	* void 
	* @throws
	 */
	private void createTargetPkgTableView(Composite parent) {
		targetPkgTableViewer = new TableViewer(parent, SWT.FULL_SELECTION
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK);
		ctv = new CheckboxTableViewer(targetPkgTableViewer.getTable());
		ctv.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if(isSupportCheckChangePer)
				{
					GetCurCheckedStateUtil.update();
				}
				else
				{
					APTConsoleFactory.getInstance().APTPrint("Operation forbid");
				}
			}
		});
		
		// ?????????????????? 
		for (int i = 0; i < TARGET_COLUMN_NAME.length; i++) {
			new TableColumn(targetPkgTableViewer.getTable(), SWT.LEFT)
					.setText(TARGET_COLUMN_NAME[i]);
		}
		targetPkgTableViewer.getTable().getColumn(0).setWidth(200);
		targetPkgTableViewer.getTable().getColumn(1).setWidth(50);
		// ??????????????????????????????
		targetPkgTableViewer.getTable().setHeaderVisible(true);
		targetPkgTableViewer.getTable().setLinesVisible(true);
		

		targetPkgTableViewer.setContentProvider(new ViewContentProvider());
		targetPkgTableViewer.setLabelProvider(new ViewLabelProvider());
		
		
		targetPkgTableViewer.getTable().addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {

			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if(isSupportAddOrDeleteOper)
				{
					APTState.getInstance().DealWithEventBefore(APTEventEnum.CONFIGRURE_OPER);
					TableItem[] selectData = targetPkgTableViewer.getTable().getSelection();
					if(selectData == null || selectData.length == 0)
					{
						return;
					}
					
					PkgInfo itemData = (PkgInfo)selectData[0].getData();

					targetPkgTableViewer.remove(itemData);
					APTState.getInstance().DealWithEventAfter(APTEventEnum.CONFIGRURE_OPER);
				}
				else
				{
					APTConsoleFactory.getInstance().APTPrint("Operation forbid");
				}
				
			}
		});
		
		
		targetPkgTableViewer.getTable().setToolTipText("?????????????????????????????????????????????");
		
		FormData tableViewFormData = new FormData();
		tableViewFormData.left = new FormAttachment(0, 5);
		tableViewFormData.right = new FormAttachment(100, -5);
		tableViewFormData.top = new FormAttachment(pkgNameText, 10);
		tableViewFormData.height = 125;
		Table table = targetPkgTableViewer.getTable();
		table.setLayoutData(tableViewFormData);
		
	}
	
	/**
	 * ?????????tableviewer
	 * ??????????????????
	 * @param parent
	 */
	private void createTableView(Composite parent) {
		sourcePkgTableViewer = new TableViewer(parent, SWT.FULL_SELECTION
				| SWT.H_SCROLL | SWT.V_SCROLL);
		// ?????????????????? 
		for (int i = 0; i < SOURCE_COLUMN_NAME.length; i++) {
			new TableColumn(sourcePkgTableViewer.getTable(), SWT.LEFT)
					.setText(SOURCE_COLUMN_NAME[i]);
		}
		sourcePkgTableViewer.getTable().getColumn(0).setWidth(200);
		sourcePkgTableViewer.getTable().getColumn(1).setWidth(50);
		// ??????????????????????????????
		sourcePkgTableViewer.getTable().setHeaderVisible(true);
		sourcePkgTableViewer.getTable().setLinesVisible(true);

		sourcePkgTableViewer.setSorter(new APTTableSorter());
		// ???????????????????????????????????????
		for (int i = 0; i < SOURCE_COLUMN_NAME.length; i++) {
			final int j = i;
			TableColumn column = sourcePkgTableViewer.getTable().getColumn(j);
			column.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
//					// ??????????????????????????????????????????
					((APTTableSorter) sourcePkgTableViewer.getSorter()).doSort(j);
					// ??????????????????
					sourcePkgTableViewer.refresh();
				}
			});
		}

		sourcePkgTableViewer.setContentProvider(new ViewContentProvider());
		sourcePkgTableViewer.setLabelProvider(new ViewLabelProvider());
		
		
		sourcePkgTableViewer.getTable().addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {		
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if(isSupportAddOrDeleteOper)
				{
					//APTConsoleFactory.getInstance().APTPrint("start ");
					APTState.getInstance().DealWithEventBefore(APTEventEnum.CONFIGRURE_OPER);
					//APTConsoleFactory.getInstance().APTPrint("1 ");
					// ???????????????????????????
					IStructuredSelection iss = (IStructuredSelection) sourcePkgTableViewer
							.getSelection();
					//APTConsoleFactory.getInstance().APTPrint("2 ");
					if(iss == null)
					{
						//APTConsoleFactory.getInstance().APTPrint("iss == null");
						return ;
					}
					
					
					PkgInfo itemData = (PkgInfo) iss.getFirstElement();
					//APTConsoleFactory.getInstance().APTPrint("3");
					addDataItem(targetPkgTableViewer, itemData);
					//APTConsoleFactory.getInstance().APTPrint("4");
					APTState.getInstance().DealWithEventAfter(APTEventEnum.CONFIGRURE_OPER);
					//APTConsoleFactory.getInstance().APTPrint("5");
				}
				else
				{
					APTConsoleFactory.getInstance().APTPrint("Operation forbid");
				}
				
			}
		});
		
		sourcePkgTableViewer.getTable().setToolTipText("??????????????????????????????????????????????????????????????????");
		
		FormData tableViewFormData = new FormData();
		tableViewFormData.left = new FormAttachment(0, 5);
		tableViewFormData.right = new FormAttachment(100, -5);
		tableViewFormData.top = new FormAttachment(targetPkgTableViewer.getTable(), 10);
		tableViewFormData.bottom = new FormAttachment(100, -5);
		Table table = sourcePkgTableViewer.getTable();
		table.setLayoutData(tableViewFormData);
	}

	/**
	 * ?????????????????????????????????????????????
	 * 
	 */
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return (Object[]) parent;
		}

	}

	/**
	 * ?????????????????????????????????????????????
	 * 
	 */
	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return ((PkgInfo) obj).contents[index];
		}

		public Image getColumnImage(Object obj, int index) {
			return null;
		}

		public Image getImage(Object obj) {
			return null;
		}
	}


	/**
	 * ????????????????????????????????????
	 */
	private void createMenuAndToolBar() {
		makeActions();
		createLocalToolBar();
		createContextMenu();
	}

	/**
	 * ??????????????????
	 */
	private void createLocalToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(testAction);
		manager.add(refreshAction);
		manager.add(openLogWithChartAction);
		manager.add(openResultDirAction);
		
	}

	/**
	 * ?????????????????????
	 */
	private void createContextMenu() {
		createSourContextMenu();
		createTargetContextMenu();
	}
	
	private void createSourContextMenu()
	{
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				
				DevicesView.this.fillSourContextMenu(manager);
			}
		});
		
		Menu menu = menuMgr.createContextMenu(sourcePkgTableViewer.getControl());
		sourcePkgTableViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, sourcePkgTableViewer);
	}

	/**
	 * ?????????????????????
	 * 
	 * @param manager
	 */
	private void fillSourContextMenu(IMenuManager manager) {
		manager.add(addPkgAction);
		manager.add(copyAction);
		manager.add(pmapAction);
		manager.add(dumpHprofAction);
		manager.add(gcAction);
		manager.add(smapAction);
	}
	
	
	private void createTargetContextMenu()
	{
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				
				DevicesView.this.fillTargetContextMenu(manager);
			}
		});
		
		Menu menu = menuMgr.createContextMenu(targetPkgTableViewer.getControl());
		targetPkgTableViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, targetPkgTableViewer);
	}

	/**
	 * ?????????????????????
	 * 
	 * @param manager
	 */
	private void fillTargetContextMenu(IMenuManager manager) {
		manager.add(removePkgAction);
	}

	/**
	 * ?????????action
	 */
	private void makeActions() {
		
		/**
		 * ????????????
		 */
		testAction = new StartTestAction();
		testAction.setText("Start");
		testAction.setToolTipText("????????????");
		testAction.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Constant.PLUGIN_ID,
						"icons/start.png"));
		
		
		/**
		 * ????????????
		 */
		refreshAction = new Action(){
			public void run() {
				refreshGetPkgInfo();
			}};
		refreshAction.setText("??????????????????????????????");
		refreshAction.setToolTipText("??????????????????????????????");
		refreshAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Constant.PLUGIN_ID, "icons/refresh.png"));
		
		
		/**
		 * ??????log??????
		 */
		openResultDirAction = new Action() {
			public void run() {
				try {
					if(PCInfo.OSName.toLowerCase().indexOf("window") != -1)
					{
						Runtime.getRuntime().exec("explorer.exe " + Constant.LOG_FOLDER_ON_PC + File.separator + TestSence.getInstance().curDir);
					}
					else
					{
						Runtime.getRuntime().exec("open " + Constant.LOG_FOLDER_ON_PC + File.separator + TestSence.getInstance().curDir);
					}
					
				} catch (IOException e) {
					e.printStackTrace();
					APTConsoleFactory.getInstance().APTPrint("?????????????????????????????????");
				}
			}
		};
		
		openResultDirAction.setText("??????????????????????????????");
		openResultDirAction.setToolTipText("??????????????????????????????");
		openResultDirAction.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Constant.PLUGIN_ID,
						"icons/open_pc.png"));
		
		
		/**
		 * ??????log
		 */
		openLogWithChartAction = new Action()
		{
			public void run()
			{	
				FileDialog dialog = new FileDialog(sourcePkgTableViewer.getControl().getShell(), SWT.OPEN);
				dialog.setFilterPath(Constant.LOG_FOLDER_ON_PC);//??????????????????
				final String fileName = dialog.open();//??????????????????(??????+?????????)
				if(fileName == null)
				{
					return;
				}
				final APTLogFileHeader afh = APTLogFileParse.pareseAPTLogFileHeader(fileName);
				if(afh == null)
				{
					APTConsoleFactory.getInstance().APTPrint("????????????????????????");
					return;
				}
				
				//?????????????????????????????????????????????
				APTState.getInstance().DealWithEventBefore(APTEventEnum.OPENLOG_OPER);
				APTConsoleFactory.getInstance().APTPrint("?????????log??????,?????????......");
				final CPUView cpuViewPart  = (CPUView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(CPUView.ID);
				final MemoryView memViewPart  = (MemoryView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(MemoryView.ID);
			
				cpuViewPart.setCpuTableViewerFilter(null);
				cpuViewPart.setJiffiesTableViewerFilter(null);
				memViewPart.setTableViewerFilter(null);
				
				Thread parseLogThread = new Thread(new Runnable() {			
					@Override
					public void run() {
						final JfreeChartDatas datas = APTLogFileParse.getData(fileName, afh);
						if(datas == null)
						{
							APTConsoleFactory.getInstance().APTPrint("?????????????????????");
							return;
						}
						
						if(datas.monitorItem.equals(Constant.TEXT_ITEM_TITLES[Constant.CPU_INDEX]))
						{
							cpuViewPart.cpuRealTimeChart.fillData(datas);
						}
						else
						{
							memViewPart.memRealTimeChart.fillData(datas);
						}
					}
				});
				
				parseLogThread.start();
				try {
					//TODO ??????????????????????????????????????????????????????????????????????????????
					parseLogThread.join();

					for(int i = 0; i < afh.pkgNames.length; i++)
					{
						PkgInfo item = new PkgInfo();
						item.contents[PkgInfo.NAME_INDEX] = afh.pkgNames[i];
						item.contents[PkgInfo.PID_INDEX] = "-1";
						addDataItem(targetPkgTableViewer, item);
					}
					APTState.getInstance().DealWithEventAfter(APTEventEnum.OPENLOG_OPER);
					//APTConsoleFactory.getInstance().APTPrint("??????log??????");
					GetCurCheckedStateUtil.update();
					APTConsoleFactory.getInstance().APTPrint("log????????????");
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				
			}
		};
		openLogWithChartAction.setText("???JFreechart??????log");
		openLogWithChartAction.setToolTipText("???JFreechart??????log");
		openLogWithChartAction.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Constant.PLUGIN_ID,
						"icons/generate_chart.png"));
		
		
		/**
		 * ????????????
		 */
		copyAction = new CopyAllFromTableViewAction(sourcePkgTableViewer);
		copyAction.setText("??????");
		copyAction.setToolTipText("?????????????????????????????????????????????");
		ImageDescriptor descriptor = AbstractUIPlugin
				.imageDescriptorFromPlugin(Constant.PLUGIN_ID, "icons/save.png");
		copyAction.setImageDescriptor(descriptor);
		
		
		/**
		 * ????????????
		 */
		addPkgAction = new Action(){
			@Override
			public void run() {
				if(isSupportAddOrDeleteOper)
				{
					super.run();
					APTState.getInstance().DealWithEventBefore(APTEventEnum.CONFIGRURE_OPER);
					IStructuredSelection iss = (IStructuredSelection) sourcePkgTableViewer
							.getSelection();
					if(iss == null)
					{
						return ;
					}
					
					PkgInfo itemData = (PkgInfo) iss.getFirstElement();

					addDataItem(targetPkgTableViewer, itemData);
					APTState.getInstance().DealWithEventAfter(APTEventEnum.CONFIGRURE_OPER);
				}
				else
				{
					APTConsoleFactory.getInstance().APTPrint("Operation forbid");
				}
				
			}};
		addPkgAction.setText("??????");
		addPkgAction.setToolTipText("????????????????????????????????????");
		addPkgAction.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Constant.PLUGIN_ID, "icons/add.png"));
		
		
		/**
		 * ????????????
		 */
		removePkgAction = new Action(){

			@Override
			public void run() {
				if(isSupportAddOrDeleteOper)
				{
					super.run();
					APTState.getInstance().DealWithEventBefore(APTEventEnum.CONFIGRURE_OPER);
					TableItem[] selectData = targetPkgTableViewer.getTable().getSelection();
					if(selectData == null || selectData.length == 0)
					{
						return;
					}
					
					PkgInfo itemData = (PkgInfo)selectData[0].getData();
					
					targetPkgTableViewer.remove(itemData);
					APTState.getInstance().DealWithEventAfter(APTEventEnum.CONFIGRURE_OPER);	
				}
				else
				{
					APTConsoleFactory.getInstance().APTPrint("Operation forbid");
				}

			}};
		removePkgAction.setText("??????");
		removePkgAction.setToolTipText("???????????????????????????????????????");
		removePkgAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Constant.PLUGIN_ID, "icons/remove.png"));
		
		
		pmapAction = new GetPMAPInfoAction(sourcePkgTableViewer);
		pmapAction.setText("PMAP");
		pmapAction.setToolTipText("PMAP");
		pmapAction.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Constant.PLUGIN_ID, "icons/pmap.png"));
		
		smapAction = new GetSMAPInfoAction(sourcePkgTableViewer);
		smapAction.setText("SMAP");
		smapAction.setToolTipText("SMAP");
		smapAction.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Constant.PLUGIN_ID, "icons/pmap.png"));
		
		dumpHprofAction = new DumpHprofAction();
		dumpHprofAction.setText("DumpHprof");
		dumpHprofAction.setToolTipText("DumpHprof");
		dumpHprofAction.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Constant.PLUGIN_ID, "icons/dump.png"));
		
		gcAction = new GCAction();
		gcAction.setText("GC");
		gcAction.setToolTipText("GC");
		gcAction.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Constant.PLUGIN_ID, "icons/gc.png"));

	}
	
	public class APTTableSorter extends ViewerSorter {
		private static final int ASCENDING = 0;
		private static final int DESCENDING = 1;

		private int order;// ???????????????????????????
		private int column;// ??????????????????

		public void doSort(int column) {
			// ??????????????????????????????????????????
			if (column == this.column) {
				order = 1 - order;
			} else {// ?????????????????????????????????????????????
				this.column = column;
				order = ASCENDING;
			}
		}

		// ?????????????????????????????????????????????-1,0,1????????????
		public int compare(Viewer viewer, Object e1, Object e2) {
			int result = 0;
			PkgInfo item1 = (PkgInfo) e1;
			PkgInfo item2 = (PkgInfo) e2;

			if (item1 == null || item2 == null) {
				return 0;
			}
			/*
			 * ??????????????? ??????????????????????????????????????????????????????????????????
			 */
			if (isNumeric(item1.contents[column])
					&& isNumeric(item2.contents[column])) {
				result = (int) (Long.parseLong(item2.contents[column]) - Long
						.parseLong(item1.contents[column]));
			} else {
				result = item1.contents[column]
						.compareTo(item2.contents[column]);
			}
			// ?????????????????????
			if (order == DESCENDING) {
				result = -result;
			}
			return result;
		}

		/**
		 * ??????????????????????????????
		 * 
		 * @param str
		 * @return
		 */
		private boolean isNumeric(String str) {
			Pattern pattern = Pattern.compile("[0-9]*");
			return pattern.matcher(str).matches();
		}
	}


	public boolean addDataItem(TableViewer viewer, PkgInfo item)
	{
		if(viewer == null || item == null)
		{
			APTConsoleFactory.getInstance().APTPrint("viewer == null || item == null");
			return false;
		}
		
		else
		{
			int sourcesLen = viewer.getTable().getItemCount();
			TableItem[] tableItems = viewer.getTable().getItems();

			if(sourcesLen == Constant.MAX_PKG_NUMBER)
			{
				APTConsoleFactory.getInstance().APTPrint("??????APT????????????" + Constant.MAX_PKG_NUMBER + "???????????????");
				return false;
			}
			for(int i = 0; i < sourcesLen; i++)
			{
				if(((PkgInfo)tableItems[i].getData()).contents[PkgInfo.NAME_INDEX].equalsIgnoreCase(item.contents[PkgInfo.NAME_INDEX]))
				{
					APTConsoleFactory.getInstance().APTPrint("??????????????????????????????");
					return false;
				}
			}
			viewer.add(item);
			viewer.getTable().getItem(getIndexByPkgName(item.contents[PkgInfo.NAME_INDEX], targetPkgTableViewer)).setChecked(true);
			APTConsoleFactory.getInstance().APTPrint("6");
			return true;
		}	
	}
	
	private int getIndexByPkgName(String pkgName, TableViewer viewer)
	{
		if(pkgName == null || viewer == null)
		{
			return -1;
		}
		
		TableItem[] tableItems = viewer.getTable().getItems();
		if(tableItems == null)
		{
			return -1;
		}
		
		int len = tableItems.length;
		
		for(int i = 0; i < len; i++)
		{
			PkgInfo element = (PkgInfo)tableItems[i].getData();
			if(pkgName.equals(element.contents[PkgInfo.NAME_INDEX]))
			{
				return i;
			}
		}
		return -1;
	}

	/**
	 * ??????????????????
	* @Title: refreshGetPkgInfo  
	* @Description:    
	* void 
	* @throws
	 */
	public void refreshGetPkgInfo() {	
		Thread getDeviceInfoThread = new GetDeviceInfoThread();
		getDeviceInfoThread.start();
	}
	
	/**
	 * ????????????????????????????????????APT??????????????????????????????
	* @ClassName: GetDeviceInfoThread  
	* @Description:  
	* @date 2013-4-15 ??????8:46:25  
	*
	 */
	class GetDeviceInfoThread extends Thread 
	{
		public void run() {
			synchronized (this) 
			{	
				if (DeviceInfo.getInstance().state == PhoneState.STATE_NOT_ADB) 
				{
					testAction.setEnabled(false);
					return;
				}
				APTConsoleFactory.getInstance().APTPrint("????????????????????????...");
				GetDeviceInfo.getDeviceInfo();
				
				PhoneState state = DeviceInfo.getInstance().state;
				//???????????????????????????if??????
				if (state == PhoneState.STATE_NOT_ADB) {
					APTConsoleFactory.getInstance().APTPrint("??????android sdk???????????????????????????");
					testAction.setEnabled(false);
					PlatformUI.getWorkbench().getDisplay()
							.asyncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog.openWarning(PlatformUI
											.getWorkbench().getDisplay()
											.getActiveShell(), "??????",
											"??????android sdk???????????????????????????,????????????APT");
								}
							});

				}

				else if (state == PhoneState.STATE_NOT_FOUND_PHONE) {
					APTConsoleFactory.getInstance().APTPrint("???????????????");
				}

				else if (state == PhoneState.STATE_MULTI_FOUND_PHONE) {
					APTConsoleFactory.getInstance().APTPrint("??????????????????????????????");
				}

				else {
					APTConsoleFactory.getInstance().APTPrint("????????????????????????");
					if (!sourcePkgTableViewer.getTable().getDisplay()
							.isDisposed()) {
						sourcePkgTableViewer.getTable().getDisplay()
								.asyncExec(new Runnable() {

									@Override
									public void run() {
										//????????????????????????????????????
										sourcePkgTableViewer
												.setInput(DeviceInfo
														.getInstance().pkgList
														.toArray());
										
										//??????????????????????????????pkg???PID??????
										int pkgNumber = targetPkgTableViewer.getTable().getItemCount();
										List<PkgInfo> curPkgInfos = DeviceInfo.getInstance().pkgList;
										
										// ??????????????????????????????????????????PID??????
										for(int i = 0; i < pkgNumber; i++)
										{
											String pkgName = targetPkgTableViewer.getTable().getItem(i).getText(0);
											String pid = getPidByPkgName(pkgName, curPkgInfos);
											targetPkgTableViewer.getTable().getItem(i).setText(1, pid);
										}
									}
								});
					}
				}
			}
		}
	}
	
	
	private String getPidByPkgName(String pkgName, List<PkgInfo> list)
	{
		String result = "-1";
		if(pkgName == null || list == null)
		{
			return result;
		}
		for(int i = 0; i < list.size(); i++)
		{
			if(list.get(i).contents[PkgInfo.NAME_INDEX].equalsIgnoreCase(pkgName))
			{
				return list.get(i).contents[PkgInfo.PID_INDEX];
			}
		}
		return result;
	}
	
	/**
	 * APT????????????????????????
	 */
	private void initAPT()
	{
		// ???????????????
		APTConsoleFactory.getInstance().openConsole();
		// ??????????????????
		StatusBar.getInstance().init();
		APTState.getInstance().setInitState();

		// ????????????APT?????????????????????
		PCInfo.OSName = System.getProperty("os.name");

		if (!GetAdbPathUtil.getAdbPath()) {
			DeviceInfo.getInstance().state = PhoneState.STATE_NOT_ADB;
			// TODO ??????????????????????????????????????????
			String info = "?????????ADT????????????Eclipse Preferences?????????Android SDK Location??????????????????APT";
			APTConsoleFactory.getInstance().APTPrint(info);
			MessageDialog.openWarning(PlatformUI.getWorkbench().getDisplay()
					.getActiveShell(), "??????", info);
		}
		else
		{
			//apt
		}
		DDMSUtil.init();
		APTConsoleFactory.getInstance().APTPrint("initAPT complete");
	}

}
