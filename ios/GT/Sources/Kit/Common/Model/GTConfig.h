//
//  GTConfig.h
//  GTKit
//
//  Created   on 13-6-26.
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
#ifndef GT_DEBUG_DISABLE
#import <Foundation/Foundation.h>
#import "GTDebugDef.h"
#import "GTUtility.h"

typedef enum {
    GTACSwitchProfiler = 0,
	GTACSwitchGW
} GTACSwitchIndex;


#define M_GT_SYS_DIR            @"sys"
#define M_GT_SYS_PARA_DIR       @"sys/para"
#define M_GT_SYS_PARA_DIR_CSTR   "sys/para"

#define M_GT_LOG_TIME_DIR       @"Profiler"
#define M_GT_LOG_COMMON_DIR     @"Log"
#define M_GT_CRASH_DIR          @"Plugin/crash"
#define M_GT_NSLOG_DIR          @"Plugin/nslog"
#define M_GT_PCAP_DIR           @"Plugin/pcap"
#define M_GT_PARA_OUT_DIR       @"Para"
#define M_GT_TEST_DIR           @"test"

#define M_GT_FILE_TYPE_TXT      @"txt"
#define M_GT_FILE_TYPE_TXT_CSTR  "txt"
#define M_GT_FILE_TYPE_LOG      @"log"
#define M_GT_FILE_TYPE_CSV      @"csv"
#define M_GT_FILE_TYPE_PCAP     @"pcap"

@interface NSString (TimeCategory)

//??????????????????HH:mm:ss.SSS
+ (NSString *)stringWithTimeEx:(NSTimeInterval)time;

//??????????????????HH:mm:ss
+ (NSString *)stringWithTime:(NSTimeInterval)time;

//??????????????????yyyy-MM-dd
+ (NSString *)stringWithDate:(NSTimeInterval)time;

//??????????????????yyyy-MM-dd HH:mm:ss.SSS
+ (NSString *)stringWithDateEx:(NSTimeInterval)time;

- (NSTimeInterval)timeValue;
+ (NSString *)timeString:(NSTimeInterval)t;
+ (NSString *)stringFromDate:(NSDate *)date;

@end


@interface GTConfig : NSObject
{
    BOOL             _useGT;            //????????????????????????GT
    BOOL             _hasReported;      //?????????????????????????????????????????????????????????????????????
    NSDate *         _startTime;
    NSTimeInterval   _watchTime;
    
    BOOL             _appStatusBarHidden;
    UIInterfaceOrientation _appStatusBarOrientation;
    UIWindow        *_appKeyWindow;
    BOOL             _showAC;           //?????????????????????
    BOOL             _userClicked;      //???????????????????????????????????????????????????????????????????????????????????????
    NSTimeInterval   _acInterval;       //????????????????????????1.2?????????????????????
    
    NSTimeInterval   _monitorInterval;  //????????????????????????????????????1???
    
    
    BOOL             _gatherSwitch;     //????????????
    NSTimer         *_promptTimer;      //?????????????????????
    NSTimer         *_closeTimer;       //?????????????????????
    NSThread        *_fileOpThread;     //?????????????????????????????????
    long long        _paraSysDiskSize;  //??????Byte

    
    BOOL            _shouldAutorotate;
    NSUInteger      _supportedInterfaceOrientations;
    
    float           _acHeaderHeight;
    NSUInteger      _acSwtichIndex;
    
    NSDateFormatter *_formatter;
    
    BOOL            _showAlert;
    
    NSInteger       _secondsFromGMT;    //??????????????????
    NSString        *_homeDirectory;    //????????????
}

M_GT_AS_SINGLETION(GTConfig)

@property (nonatomic, assign) BOOL useGT;
@property (nonatomic, assign) BOOL hasReported;
@property (nonatomic, retain) NSDate* startTime;
@property (nonatomic, assign) NSTimeInterval watchTime;
@property (nonatomic, assign) BOOL appStatusBarHidden;
@property (nonatomic, assign) UIInterfaceOrientation appStatusBarOrientation;
@property (nonatomic, retain) UIWindow* appKeyWindow;
@property (nonatomic, assign) BOOL shouldAutorotate;
@property (nonatomic, assign) NSUInteger supportedInterfaceOrientations;

@property (nonatomic, assign) BOOL showAC;
@property (nonatomic, assign) BOOL userClicked;
@property (nonatomic, assign) NSTimeInterval acInterval;
@property (nonatomic, assign) NSTimeInterval monitorInterval;
@property (nonatomic, assign) BOOL gatherSwitch;

@property (nonatomic, assign) float acHeaderHeight;
@property (nonatomic, assign) NSUInteger acSwtichIndex;

@property (nonatomic, assign) NSDateFormatter  *formatter;
@property (nonatomic, assign) BOOL showAlert;
@property (nonatomic, assign) NSInteger secondsFromGMT;

- (NSString *)sysDir;
- (NSString *)sysDirByCreated;

- (NSString *)usrDir;
- (NSString *)usrDirByCreated;

- (void)dirCreateIfNotExists:(NSString *)dirPath;

- (NSString *)pathForDir:(NSString *)dir fileName:(NSString *)fileName ofType:(NSString *)ext;
- (NSString *)pathForDirByCreated:(NSString *)dir fileName:(NSString *)fileName ofType:(NSString *)ext;

@end


#endif
