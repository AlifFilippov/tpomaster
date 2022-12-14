//
//  GTParaOutApi.h
//  GTKit
//
//  Created by  on 13-2-21.
// Tencent is pleased to support the open source community by making
// Tencent GT (Version 2.4 and subsequent versions) available.
//
// Notwithstanding anything to the contrary herein, any previous version
// of Tencent GT shall not be subject to the license hereunder.
// All right, title, and interest, including all intellectual property rights,
// in and to the previous version of Tencent GT (including any and all copies thereof)
// shall be owned and retained by Tencent and subject to the license under the
// Tencent GT End User License Agreement (http://gt.qq.com/wp-content/EULA_EN.html).
//
// Copyright (C) 2015 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the MIT License (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy of the License at
//
// http://opensource.org/licenses/MIT
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.
//
//
#ifdef __OBJC__
#import "GTParaOutApiForOC.h"
#endif

#ifdef GT_DEBUG_DISABLE

//------------------------ DISABLE GT BEGIN --------------------------

#define GT_OUT_GATHER_SWITCH_SET(on)
#define GT_OUT_MONITOR_INTERVAL_SET(interval)
#define GT_OUT_REGISTER(key,alias)
#define GT_OUT_GET(key,writeToLog)
#define GT_OUT_SET(key,writeToLog,...)
#define GT_OUT_WRITE_TO_LOG(key,writeToLog)
#define GT_OUT_HISTORY_CHECKED_SET(key,selected)
#define GT_OUT_WARNING_OUT_OF_RANGE_SET(key,lastingTime,lowerValue,upperValue)
#define GT_OUT_HISTORY_CLEAR(key)
#define GT_OUT_HISTORY_SAVE(key,fileName)
#define GT_OUT_HISTORY_ALL_SAVE(dirName)

#define GT_OUT_DEFAULT_ON_AC(key1,key2,key3)
#define GT_OUT_DEFAULT_ON_DISABLED(...)
#define GT_OUT_DEFAULT_ALL_ON_DISABLED

//------------------------ DISABLE GT END ----------------------------

#else

//------------------------ FOR C Language BEGIN ------------------------
/**
 * @brief   ??????????????????????????????????????????
 * @ingroup GT????????????????????????
 *
 * @param on [bool] ?????????false, false:??????output???????????? true:??????output????????????
 * @return
 *
 * Example Usage:
 * @code
 *    //??????output????????????
 *    GT_OUT_GATHER_SWITCH_SET(true);
 * @endcode
 */
#define GT_OUT_GATHER_SWITCH_SET(on) func_setGatherSwitch(on)
extern void func_setGatherSwitch(bool on);

/**
 * @brief   ???????????????CPU???MEM???NET?????????
 * @ingroup GT????????????????????????
 *
 * @param interval [double] ??????????????????s???????????????0.1(0.1s) ?????????:10(10s)
 * @return
 *
 * Example Usage:
 * @code
 *    //??????output????????????
 *    GT_OUT_MONITOR_INTERVAL_SET(5);
 * @endcode
 */
#define GT_OUT_MONITOR_INTERVAL_SET(interval) func_setMonitorInterval(interval)
extern void func_setMonitorInterval(double interval);

/**
 * @brief   ??????????????????
 * @ingroup GT????????????????????????
 *
 * @param key [const char *] ?????????????????????key
 * @param alias [const char *] key?????????(??????), ?????????????????????????????????,???????????????????????????
 * @return
 *
 * Example Usage:
 * @code
 *    //??????????????????
 *    GT_OUT_REGISTER("fileTransferResult", "REST");
 * @endcode
 */
#define GT_OUT_REGISTER(key,alias) func_addOutputForString(key,alias)
extern void func_addOutputForString(const char *key, const char *alias);

/**
 * @brief   ?????????????????????
 * @ingroup GT????????????????????????
 *
 * @param key [const char *] ?????????????????????key
 * @param writeToLog [bool] ????????????????????????????????? true:??????????????????, false:???????????????
 * @return
 *
 * Example Usage:
 * @code
 *    //??????????????????
 *    const char* result = GT_OUT_GET("fileTransferResult", true);
 * @endcode
 */
#define GT_OUT_GET(key,writeToLog) func_getOutputForString(key,writeToLog)
extern const char* func_getOutputForString(const char *key, bool writeToLog);


/**
 * @brief   ?????????????????????
 * @ingroup GT????????????????????????
 *
 * @param key [const char *] ?????????????????????key
 * @param writeToLog [bool] ????????????????????????????????? true:??????????????????, false:???????????????
 * @param ... [const char *]
 * @return
 *
 * Example Usage:
 * @code
 *    //??????????????????
 *    GT_OUT_SET("fileTransferResult", true, "%s: success", __FUNCTION__);
 * @endcode
 */
#define GT_OUT_SET(key,writeToLog,...) func_setOutputForString(key,writeToLog,__VA_ARGS__)
extern void func_setOutputForString(const char *key, bool writeToLog, const char * format,...);

/**
 * @brief   ??????????????????????????????????????????
 * @ingroup GT????????????????????????
 *
 * @param key [const char *] ?????????????????????key
 * @param writeToLog [bool] ????????????????????????????????? true:??????????????????, false:???????????????
 * @return
 *
 * Example Usage:
 * @code
 *    //???????????????????????????LOG???
 *    GT_OUT_WRITE_TO_LOG("App Smoothness", true);
 * @endcode
 */
#define GT_OUT_WRITE_TO_LOG(key,writeToLog) func_setOutputWriteToLog(key,writeToLog)
extern void func_setOutputWriteToLog(const char *key, bool writeToLog);

/**
 * @brief   ??????????????????????????????????????????????????????
 * @ingroup GT????????????????????????
 *
 * @param key [const char *] ?????????????????????key
 * @param selected [bool] ???????????????????????????????????????????????? true:??????, false:?????????
 * @return
 *
 * Example Usage:
 * @code
 *    //??????????????????????????????????????????????????????
 *    GT_OUT_HISTORY_CHECKED_SET("fileTransferResult", true);
 * @endcode
 */
#define GT_OUT_HISTORY_CHECKED_SET(key,selected) func_setOutputHistoryChecked(key,selected)
extern void func_setOutputHistoryChecked(const char *key, bool selected);

/**
 * @brief   ??????????????????????????????????????????
 * @ingroup GT????????????????????????
 *
 * @param key [const char *] ?????????????????????key
 * @param lastingTime [double] ?????????????????????????????????lastingTime???????????????????????????????????????????????????
 * @param lowerValue [double] ?????????
 * @param upperValue [double] ?????????
 * @return
 *
 * Example Usage:
 * @code
 *    //????????????????????????????????????5?????????????????????[20,60]????????????????????????
 *    GT_OUT_WARNING_OUT_OF_RANGE_SET("App Smoothness", 5, 20, 60);
 * @endcode
 */
#define GT_OUT_WARNING_OUT_OF_RANGE_SET(key,lastingTime,lowerValue,upperValue) func_setWarningOutOfRange(key,lastingTime,lowerValue,upperValue)
extern void func_setWarningOutOfRange(const char *key, double lastingTime, double lowerValue, double upperValue);

/**
 * @brief   ??????????????????????????????
 * @ingroup GT????????????????????????
 *
 * @param key [const char *] ?????????????????????key
 * @return
 *
 * Example Usage:
 * @code
 *    //??????????????????
 *    GT_OUT_HISTORY_CLEAR("App Smoothness");
 * @endcode
 */
#define GT_OUT_HISTORY_CLEAR(key) func_clearOutputHistory(key)
extern void func_clearOutputHistory(const char *key);


/**
 * @brief   ??????????????????????????????
 * @ingroup GT????????????????????????
 *
 * @param key [const char *] ?????????????????????key
 * @param fileName [const char *] ???????????????
 * @return
 *
 * Example Usage:
 * @code
 *    //??????????????????
 *    GT_OUT_HISTORY_SAVE("App Smoothness", "SM");
 * @endcode
 */
#define GT_OUT_HISTORY_SAVE(key,fileName) func_saveOutputHistory(key,fileName)
extern void func_saveOutputHistory(const char *key, const char *fileName);


/**
 * @brief   ????????????????????????????????????
 * @ingroup GT????????????????????????
 *
 * @param dirName [const char *] ???????????????
 * @return
 *
 * Example Usage:
 * @code
 *    //??????????????????
 *    GT_OUT_HISTORY_ALL_SAVE("dir");
 * @endcode
 */
#define GT_OUT_HISTORY_ALL_SAVE(dirName) func_saveOutputHistoryAll(dirName)
extern void func_saveOutputHistoryAll(const char *dirName);


/**
 * @brief   ??????????????????????????????????????????
 * @ingroup GT????????????????????????
 *
 * @param key1 [const char *] ?????????????????????key
 * @param key2 [const char *] ?????????????????????key
 * @param key3 [const char *] ?????????????????????key
 * @return
 *
 * Example Usage:
 * @code
 *    //??????fileTransferResult??????????????????
 *    GT_OUT_DEFAULT_ON_AC("fileTransferResult", NULL, NULL);
 * @endcode
 */
#define GT_OUT_DEFAULT_ON_AC(key1,key2,key3) func_defaultOutputOnAC(key1,key2,key3)
extern void func_defaultOutputOnAC(const char *key1, const char *key2, const char *key3);


/**
 * @brief   key????????????????????????????????????
 * @ingroup GT????????????????????????
 *
 * @param ... [const char *] ?????????????????????key??????, ???????????????NULL
 * @return
 *
 * Example Usage:
 * @code
 *    GT_OUT_DEFAULT_ON_DISABLED("ResendCount", NULL); //??????ResendCount??????
 * @endcode
 */
#define GT_OUT_DEFAULT_ON_DISABLED(...) func_defaultOutputOnDisabled(__VA_ARGS__)
extern void func_defaultOutputOnDisabled(const char * format,...);



/**
 * @brief   ??????????????????????????????
 * @ingroup GT????????????????????????
 *
 * Example Usage:
 * @code
 *    //??????????????????????????????
 *    GT_OUT_DEFAULT_ALL_ON_DISABLED;
 * @endcode
 */
#define GT_OUT_DEFAULT_ALL_ON_DISABLED func_defaultOutputAllOnDisabled()
extern void func_defaultOutputAllOnDisabled();


//------------------------ FOR C Language END ------------------------


#endif

