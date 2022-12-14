package com.tencent.wstt.gt.analysis4.analysis;

import com.tencent.wstt.gt.GTConfig;
import com.tencent.wstt.gt.dao.DetailListData;
import com.tencent.wstt.gt.analysis4.GTRAnalysis;
import com.tencent.wstt.gt.analysis4.GTRAnalysisResult;
import com.tencent.wstt.gt.analysis4.obj.DiskIOInfo;
import com.tencent.wstt.gt.analysis4.obj.LogInfo;
import com.tencent.wstt.gt.analysis4.util.LogUtil;

import java.util.HashMap;

public class IOAnalysis {
    GTRAnalysisResult gtrAnalysisResult = null;

    public IOAnalysis(GTRAnalysisResult gtrAnalysisResult) {
        this.gtrAnalysisResult = gtrAnalysisResult;
    }

    public void onCollectLog(String log, long time) {
        LogInfo logInfo = LogUtil.onCollectLog(log, time);
        if (logInfo == null) {
            return;
        }

        if (logInfo.tag.contains("GTR_DATA_TAG")) {
            String[] data = logInfo.logContent.split(GTConfig.separatorFile);
            int tid;
            int fd;
            int size;
            long timeStart;
            long timeEnd;
            String path;

            if (data[0].contains("file_open")) {
                tid = Integer.parseInt(data[1]);
                fd = Integer.parseInt(data[2]);
                path = data[3];
                timeStart = Long.parseLong(data[4]);
                timeEnd = Long.parseLong(data[5]);
                onFileOpen(tid, fd, path, timeStart, timeEnd);
            } else if (data[0].contains("file_write") ||
                    data[0].contains("file_pwrite64")) {
                tid = Integer.parseInt(data[1]);
                fd = Integer.parseInt(data[2]);
                size = Integer.parseInt(data[3]);
                timeStart = Long.parseLong(data[4]);
                timeEnd = Long.parseLong(data[5]);
                onFileRead(tid, fd, size, timeStart, timeEnd);
            } else if (data[0].contains("file_read") ||
                    data[0].contains("file_pread64")) {
                tid = Integer.parseInt(data[1]);
                fd = Integer.parseInt(data[2]);
                size = Integer.parseInt(data[3]);
                timeStart = Long.parseLong(data[4]);
                timeEnd = Long.parseLong(data[5]);
                onFileWrite(tid, fd, size, timeStart, timeEnd);
            }
        }
    }

    private HashMap<Integer, String> threadNames = new HashMap<>();//??????ID???????????????????????????
    private HashMap<Integer, String> fileNames = new HashMap<>();//??????ID???????????????????????????
    private HashMap<Integer, String> filePaths = new HashMap<>();//??????ID??????????????????????????????

    public void onFileOpen(int tid, int fd, String path, long start, long end) {

    }

    public void onFileWrite(int tid, int fd, int size, long start, long end) {

    }

    public void onFileRead(int tid, int fd, int size, long start, long end) {
    }

    public void addFileInfo(DiskIOInfo diskIOInfo) {
    }


    private HashMap<Integer, String> dbNames = new HashMap<>();//DB ID???DB?????????????????????
    private HashMap<Integer, String> dbPaths = new HashMap<>();//DB ID???DB?????????????????????

    public void onSQLiteDatabase_beginTransaction(int dbHashCode, String threadName, int threadId, long start, long end) {
        long takeTime = end - start;
        String dbName = dbNames.get(dbHashCode) == null ? "" : dbNames.get(dbHashCode);
        String action = "beginTransaction";
        checkOnMainThread(dbName, action, takeTime, threadId);
    }

    public void onSQLiteDatabase_endTransaction(int dbHashCode, String threadName, int threadId, long start, long end) {
        long takeTime = end - start;
        String dbName = dbNames.get(dbHashCode) == null ? "" : dbNames.get(dbHashCode);
        String action = "endTransaction";
        checkOnMainThread(dbName, action, takeTime, threadId);
    }

    public void onSQLiteDatabase_enableWriteAheadLogging(int dbHashCode, String threadName, int threadId, long start, long end) {
        long takeTime = end - start;
        String dbName = dbNames.get(dbHashCode) == null ? "" : dbNames.get(dbHashCode);
        String action = "enableWriteAheadLogging";
        checkOnMainThread(dbName, action, takeTime, threadId);
    }

    public void onSQLiteDatabase_openDatabase(int dbHashCode, String path, String threadName, int threadId, long start, long end) {
        String[] tempStringArray = path.split("/");
        dbPaths.put(dbHashCode, path);
        dbNames.put(dbHashCode, tempStringArray[tempStringArray.length - 1]);

        long takeTime = end - start;
        String dbName = dbNames.get(dbHashCode) == null ? "" : dbNames.get(dbHashCode);
        String action = "openDatabase";
        checkOnMainThread(dbName, action, takeTime, threadId);
    }

    public void onSQLiteDatabase_rawQueryWithFactory(int dbHashCode, String sql, String threadName, int threadId, long start, long end) {
        long takeTime = end - start;
        String dbName = dbNames.get(dbHashCode) == null ? "" : dbNames.get(dbHashCode);
        String action = sql;
        checkOnMainThread(dbName, action, takeTime, threadId);
    }

    public void onSQLiteStatement_execute(int dbHashCode, String sql, String threadName, int threadId, long start, long end) {
        long takeTime = end - start;
        String dbName = dbNames.get(dbHashCode) == null ? "" : dbNames.get(dbHashCode);
        String action = sql;
        checkOnMainThread(dbName, action, takeTime, threadId);
    }

    public void onSQLiteStatement_executeInsert(int dbHashCode, String sql, String threadName, int threadId, long start, long end) {
        long takeTime = end - start;
        String dbName = dbNames.get(dbHashCode) == null ? "" : dbNames.get(dbHashCode);
        String action = sql;
        checkOnMainThread(dbName, action, takeTime, threadId);
    }

    public void onSQLiteStatement_executeUpdateDelete(int dbHashCode, String sql, String threadName, int threadId, long start, long end) {
        long takeTime = end - start;
        String dbName = dbNames.get(dbHashCode) == null ? "" : dbNames.get(dbHashCode);
        String action = sql;
        checkOnMainThread(dbName, action, takeTime, threadId);
    }


    private void checkOnMainThread(String dbName, String action, long takeTime, int threadId) {
        gtrAnalysisResult.dbIONum++;
        DetailListData detailListData;
        if (gtrAnalysisResult.mainThreadId != -1 && gtrAnalysisResult.mainThreadId == threadId) {
            gtrAnalysisResult.mainThreadDBIONum++;
            detailListData = new DetailListData("????????????" + dbName + "\n??????:" + action + "\n??????:" + takeTime + "ms\n??????:?????????", DetailListData.Error);
        } else {
            detailListData = new DetailListData("????????????" + dbName + "\n??????:" + action + "\n??????:" + takeTime + "ms\n??????:" + threadId, DetailListData.Normal);
        }
        gtrAnalysisResult.allDBIOListData.add(detailListData);

        GTRAnalysis.refreshIOInfo();
    }
}
