//
//  GTUIManager.m
//  GTKit
//
//  Created   on 12-10-11.
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

#import "GTUIManager.h"
#import <QuartzCore/QuartzCore.h>
#import "GTUtility.h"
#import "GTImage.h"
#import "GTInputList.h"
#import "GTDetailView.h"
#import "GTUINavigationController.h"
#import "GTConfig.h"
#import "GTOutputList.h"


@implementation GTUIManager


#pragma mark - GTDebugShortcut

M_GT_DEF_SINGLETION(GTUIManager);

@synthesize hidden = _hidden;
@synthesize detailedIndex = _detailedIndex;
@synthesize inputExtended = _inputExtended;

@synthesize onOpenCallBack = _onOpenCallBack;
@synthesize onCloseCallBack = _onCloseCallBack;
@synthesize shouldAutorotate = _shouldAutorotate;

- (id)init
{
//	CGRect screenBound = [UIScreen mainScreen].bounds;
    CGRect screenBound = [[UIScreen mainScreen] fullScreenBounds];
	
    CGRect shortcutFrame;
	shortcutFrame.size.width = M_GT_LOGO_WIDTH;
	shortcutFrame.size.height = M_GT_LOGO_HEIGHT;
	shortcutFrame.origin.x = CGRectGetMaxX(screenBound) - shortcutFrame.size.width;
	shortcutFrame.origin.y = CGRectGetMaxY(screenBound) - shortcutFrame.size.height - M_GT_LOGO_HEIGHT;
    
	self = [super init];
	if ( self )
	{
        _detailedWindow = nil;
        _acWindow = nil;
        
        _detailedIndex = 0;
        
        _inputExtended = YES;
        
        _hidden = NO;
        _logoWindow = [[GTLogoWindow alloc] initWithFrame:shortcutFrame delegate:self];
        
        
	}
    
	return self;
}



-(void)dealloc
{
    
    M_GT_SAFE_FREE(_detailedWindow);
    
    [self releaseAcWindow];
    
    [super dealloc];
}

- (void)releaseAcWindow
{
    
    if (_acWindow) {
        [_acWindow stopTimer];
        [_acWindow release];
        _acWindow = nil;
    }
}


#pragma mark -

-(void)closeFloatingWindow
{
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
    [UIView setAnimationDelay:0.3f];
    [UIView setAnimationDelegate:self];
    
    [_acWindow setFrame:CGRectMake(_logoWindow.frame.origin.x + M_GT_LOGO_WIDTH/2, _logoWindow.frame.origin.y + M_GT_LOGO_HEIGHT/2, 0, 0)];
    
    [UIView commitAnimations];
    
    [self releaseAcWindow];
}


#pragma mark - GTDetailDelegate

-(void)onDetailedClose
{
    //?????????????????????????????????
    [UIApplication sharedApplication].statusBarHidden = [[GTConfig sharedInstance] appStatusBarHidden];
    [[[GTConfig sharedInstance] appKeyWindow] makeKeyWindow];
    [[GTConfig sharedInstance] setAppKeyWindow:nil];
    
    if (_detailedWindow) {
        //?????????????????????????????????????????????????????????
        _detailedWindow.hidden = YES;
        [_detailedWindow release];
        _detailedWindow = nil;
    }
    
    [_logoWindow setHidden:NO];
    //??????logo??????
    //???????????????????????????
    [_logoWindow setFrame:CGRectMake(_logoFrame.origin.x, _logoFrame.origin.y, M_GT_LOGO_WIDTH, M_GT_LOGO_HEIGHT)];
//    [_logoWindow setFrame:_logoFrame];
    
    //?????????????????????????????????????????????????????????????????????????????????GT????????????AC??????????????????????????????GW
    if (![[GTConfig sharedInstance] gatherSwitch] && [[GTConfig sharedInstance] userClicked]
        && [[GTOutputList sharedInstance] hasItemHistoryOn]) {
        [[GTConfig sharedInstance] setShowAC:YES];
    }
    
    BOOL showAC = [[GTConfig sharedInstance] showAC];
    if (showAC) {
        [self switchFloating:YES];
        //??????floating??????
        if (_acFrameBackup) {
            //???????????????????????????
            [_acWindow setFrame:CGRectMake(_acFrame.origin.x, _acFrame.origin.y, _acWindow.frame.size.width, [_acWindow height])];
//            [_acWindow setFrame:CGRectMake(_acFrame.origin.x, _acFrame.origin.y, _acWindow.frame.size.width, _acWindow.frame.size.height)];
        }
        
    } else {
        [self switchFloating:NO];
    }
    
    // navy add???????????????????????????????????????????????????[self layoutFrame]?????????????????????????????????logo??????????????????????????????
    if ([GTConfig sharedInstance].supportedInterfaceOrientations & UIInterfaceOrientationMaskLandscape) { // ??????????????????
        [[UIApplication sharedApplication] setStatusBarOrientation:[GTConfig sharedInstance].appStatusBarOrientation]; // ???????????????????????????
        [[GTConfig sharedInstance] setShouldAutorotate:self.shouldAutorotate]; // ???????????????????????????Rotate??????
        if (self.onCloseCallBack) {
            self.onCloseCallBack(); // ???????????????????????????????????????????????????????????????rootViewController???shouldAutorotate?????????????????????
        }
    }
    
    [self layoutFrame];
}

#pragma mark - GTLogoDelegate

-(void)switchFloating:(BOOL)showAC
{
    [_logoWindow setLogoFloating:showAC];
    if (showAC) {
        if (_acWindow == nil) {
            
        }
        CGFloat width = 160.0f;
        CGFloat height = 160.0f;
        _acWindow = [[GTACWindow alloc] initWithFrame:CGRectMake(0, 0, width, height) delegate:self];
        [self layoutFloatingFrame];
    } else {
        //???????????????
        [self closeFloatingWindow];
    }
}

- (void)onIconACWindow
{
    BOOL showAC = [[GTConfig sharedInstance] showAC];
    if (showAC) {
        [[GTConfig sharedInstance] setShowAC:NO];
        [self switchFloating:NO];
        [self layoutFrame];
    } else {
        [[GTConfig sharedInstance] setShowAC:YES];
        [self switchFloating:YES];
    }
    
}

- (void)onIconDetailWindow
{
    // navy add???????????????????????????????????????????????????????????????????????????????????????GTDetailedWindow
    if ([GTConfig sharedInstance].supportedInterfaceOrientations & UIInterfaceOrientationMaskLandscape) { // ??????????????????
        self.shouldAutorotate = [GTConfig sharedInstance].shouldAutorotate;
        UIInterfaceOrientation statusBarOrientation = [UIApplication sharedApplication].statusBarOrientation;
        if (statusBarOrientation == UIInterfaceOrientationLandscapeLeft || statusBarOrientation == UIInterfaceOrientationLandscapeRight) {
            if (self.onOpenCallBack) {
                self.onOpenCallBack(); // ???????????????????????????????????????????????????????????????rootViewController???shouldAutorotate??????NO
            }
            // ???????????????????????????????????????????????????
            [[GTConfig sharedInstance] setAppStatusBarOrientation:[UIApplication sharedApplication].statusBarOrientation];
            
            [[GTConfig sharedInstance] setShouldAutorotate:NO]; // ?????????NO??????????????????????????????????????????
            [[UIApplication sharedApplication] setStatusBarOrientation:UIInterfaceOrientationPortrait]; // ???????????????Portrait???UI???????????????
        }
    }

    //????????????????????????
    [[GTConfig sharedInstance] setAppStatusBarHidden:[UIApplication sharedApplication].statusBarHidden];
    [[GTConfig sharedInstance] setAppKeyWindow:[[UIApplication sharedApplication] keyWindow]];
    
    //??????logo??????
    _logoFrame = _logoWindow.frame;
    _acFrameBackup = YES;
    _acFrame = _acWindow.frame;
    if (_acWindow == nil) {
        _acFrameBackup = NO;
    }
    
    [_logoWindow setHidden:YES];
    
    [self switchFloating:NO];
    
    //??????????????????????????????
    [[GTConfig sharedInstance] setUserClicked:NO];
    
    if (_detailedWindow == nil) {
        if ([[GTUtility sharedInstance] systemVersion] >= 7) {
//            _detailedWindow = [[GTDetailedWindow alloc] initWithFrame:[[UIScreen mainScreen] applicationFrame] boardIndex:_detailedIndex delegate:self];
            _detailedWindow = [[GTDetailedWindow alloc] initWithFrame:[[UIScreen mainScreen] screenBounds] boardIndex:_detailedIndex delegate:self]; // navy modified

        } else {
//            _detailedWindow = [[GTDetailedWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds] boardIndex:_detailedIndex delegate:self];
            _detailedWindow = [[GTDetailedWindow alloc] initWithFrame:[[UIScreen mainScreen] fullScreenBounds] boardIndex:_detailedIndex delegate:self]; // navy modified
        }
    }
    _detailedWindow.hidden = NO;
    [_detailedWindow makeKeyAndVisible];
}

#pragma mark - GTFloatingDelegate

-(void)onACAdjust:(CGFloat)heightOffset
{
    [_logoWindow setFrame:CGRectMake(_logoWindow.frame.origin.x, _logoWindow.frame.origin.y + heightOffset, M_GT_LOGO_WIDTH, M_GT_LOGO_HEIGHT)];
}



-(void)onACEditWindow:(GTInputObject *)obj
{
    //????????????????????????
    [[GTConfig sharedInstance] setAppStatusBarHidden:[UIApplication sharedApplication].statusBarHidden];
    [[GTConfig sharedInstance] setAppKeyWindow:[[UIApplication sharedApplication] keyWindow]];

    //??????logo??????
    _logoFrame = _logoWindow.frame;
    _acFrame = _acWindow.frame;
    
    [_logoWindow setHidden:YES];
    [self switchFloating:NO];
    
//    CGRect screenBound = [UIScreen mainScreen].bounds;
    CGRect screenBound = [[UIScreen mainScreen] fullScreenBounds]; // navy modified
    _editWindow = [[UIWindow alloc] initWithFrame:screenBound];
    _editWindow.windowLevel = UIWindowLevelStatusBar + 200.0f;
    _editWindow.backgroundColor = [UIColor clearColor];
    
    GTParaInSelectBoard * board = [[GTParaInSelectBoard alloc] init];
    if ( board )
    {
        [board bindData:obj];
        [board setDelegate:self];
        GTUINavigationController*  navController;
        navController = [[GTUINavigationController alloc] initWithRootViewController:board];
        navController.navigationBar.barStyle = UIBarStyleBlackOpaque;
        
        UIBarButtonItem * btn;
        btn = [[[UIBarButtonItem alloc] initWithTitle:@"??????"
                                                style:UIBarButtonItemStylePlain
                                               target:self
                                               action:@selector(didLeftBarButtonTouched)] autorelease];
        board.navigationItem.leftBarButtonItem = btn;
        
        if ([_editWindow respondsToSelector:@selector(setRootViewController:)]) {
            _editWindow.rootViewController = navController;
        } else {
            
        }
        [_editWindow addSubview:navController.view];
        [_editWindow makeKeyAndVisible];
        
        _board = board;
        [navController release];
    }
    
}

-(void)didRotate:(BOOL)isPortrait
{
    [self layoutFrame];
}

#pragma mark - GTParaInSelectDelegate

- (void)onParaInSelectCancel
{
    [self onParaInSelectOK];
}

- (void)onParaInSelectOK
{
    [_board closeKeyBoard];
    
    M_GT_SAFE_FREE(_board);
    M_GT_SAFE_FREE(_editWindow);
    
    [self onDetailedClose];
}


#pragma mark - UIGestureRecognizerDelegate

- (void)handlePanOffset:(CGPoint)offset state:(UIGestureRecognizerState)state
{
    CGRect frame;
    frame = CGRectOffset(_acWindow.frame, offset.x, offset.y);
    
    //???????????????????????????
    [_acWindow setFrame:CGRectMake(frame.origin.x, frame.origin.y, frame.size.width, [_acWindow height])];
//    [_acWindow setFrame:frame];
    
    frame = CGRectOffset(_logoWindow.frame, offset.x, offset.y);
    //???????????????????????????
    [_logoWindow setFrame:CGRectMake(frame.origin.x, frame.origin.y, M_GT_LOGO_WIDTH, M_GT_LOGO_HEIGHT)];
//    [_logoWindow setFrame:frame];

    if (state == UIGestureRecognizerStateEnded) {
        [self layoutFrame];
    }
}

#define M_GT_EDAGE_HEIGHT 44
- (void)layoutLogoFrame
{
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
    [UIView setAnimationDelay:0.3f];
    [UIView setAnimationDelegate:self];
    //    [UIView setAnimationDidStopSelector:@selector(Shortcut_animationDidStop:finished:context:)];
    
    CGRect frame = _logoWindow.frame;
    
    if ([_logoWindow isPortrait]) {
        if (frame.origin.y < M_GT_EDAGE_HEIGHT)
        {
            frame.origin.y = M_GT_EDAGE_HEIGHT;
        }
        
        if (frame.origin.y > [UIScreen mainScreen].bounds.size.height - M_GT_LOGO_HEIGHT - M_GT_EDAGE_HEIGHT )
        {
            frame.origin.y = [UIScreen mainScreen].bounds.size.height - M_GT_LOGO_HEIGHT - M_GT_EDAGE_HEIGHT;
        }
        
        if (frame.origin.x  < (([UIScreen mainScreen].bounds.size.width - M_GT_LOGO_HEIGHT)/2) ) {
            frame.origin.x = 0.0f;
        } else {
            frame.origin.x = [UIScreen mainScreen].bounds.size.width - M_GT_LOGO_WIDTH;
        }
    } else {
        if (frame.origin.x < M_GT_EDAGE_HEIGHT)
        {
            frame.origin.x = M_GT_EDAGE_HEIGHT;
        }
        
        if (frame.origin.x > [UIScreen mainScreen].bounds.size.width - M_GT_LOGO_WIDTH - M_GT_EDAGE_HEIGHT )
        {
            frame.origin.x = [UIScreen mainScreen].bounds.size.width - M_GT_LOGO_WIDTH - M_GT_EDAGE_HEIGHT;
        }
        
        if (frame.origin.y  < (([UIScreen mainScreen].bounds.size.height - M_GT_LOGO_HEIGHT)/2) ) {
            frame.origin.y = 0.0f;
        } else {
            frame.origin.y = [UIScreen mainScreen].bounds.size.height - M_GT_LOGO_HEIGHT;
        }
    }
    
    //???????????????????????????
    [_logoWindow setFrame:CGRectMake(frame.origin.x, frame.origin.y, M_GT_LOGO_WIDTH, M_GT_LOGO_HEIGHT)];
//    [_logoWindow setFrame:frame];
    
    [UIView commitAnimations];
}

- (void)layoutFloatingFrame
{
    CGFloat x = 0;
    CGFloat y = 0;
    CGFloat width = _acWindow.frame.size.width;
    CGFloat height = _acWindow.frame.size.height;
    CGRect frame = _acWindow.frame;
    
    [_acWindow setFrame:CGRectMake(_logoWindow.frame.origin.x + M_GT_LOGO_WIDTH/2, _logoWindow.frame.origin.y + M_GT_LOGO_HEIGHT/2, 0, 0)];
    
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
    [UIView setAnimationDelay:0.3f];
    [UIView setAnimationDelegate:self];
    
    if ([_logoWindow isPortrait]) {
        //????????????
        frame.origin.x = _logoWindow.frame.origin.x - width + M_GT_IN_AC_WIDTH;
        frame.origin.y = _logoWindow.frame.origin.y - height + M_GT_IN_AC_HEIGHT + M_GT_IN_AC_OFFSET;
        
        //logo??????????????????
        x = _logoWindow.frame.origin.x + M_GT_LOGO_WIDTH - M_GT_IN_AC_WIDTH;
        y = _logoWindow.frame.origin.y + M_GT_LOGO_HEIGHT - M_GT_IN_AC_HEIGHT - M_GT_IN_AC_OFFSET;
        if ((x + width <= M_GT_SCREEN_WIDTH) && (y + height <= M_GT_SCREEN_HEIGHT)) {
            frame.origin.x = x;
            frame.origin.y = y;
        }
        
        //logo??????????????????
        x = _logoWindow.frame.origin.x - width + M_GT_IN_AC_WIDTH;
        y = _logoWindow.frame.origin.y + M_GT_LOGO_HEIGHT - M_GT_IN_AC_HEIGHT - M_GT_IN_AC_OFFSET;
        if ((x >= 0) && (y + height <= M_GT_SCREEN_HEIGHT)) {
            frame.origin.x = x;
            frame.origin.y = y;
        }
        
        //logo??????????????????
        x = _logoWindow.frame.origin.x + M_GT_LOGO_WIDTH - M_GT_IN_AC_WIDTH;
        y = _logoWindow.frame.origin.y - height + M_GT_IN_AC_HEIGHT + M_GT_IN_AC_OFFSET;
        if ((x + width <= M_GT_SCREEN_WIDTH) && (y >= 0)) {
            frame.origin.x = x;
            frame.origin.y = y;
        }
        
        //logo??????????????????
        x = _logoWindow.frame.origin.x - width + M_GT_IN_AC_WIDTH;
        y = _logoWindow.frame.origin.y - height + M_GT_IN_AC_HEIGHT + M_GT_IN_AC_OFFSET;
        if ((x >= 0) && (y >= 0)) {
            frame.origin.x = x;
            frame.origin.y = y;
        }
        
    } else {
        [_acWindow setIsPortrait:[_logoWindow isPortrait]];
        
        frame.size.height = width;
        frame.size.width = height;
        
        //????????????
        frame.origin.x = _logoWindow.frame.origin.x - height + M_GT_IN_AC_HEIGHT + M_GT_IN_AC_OFFSET;
        frame.origin.y = _logoWindow.frame.origin.y + M_GT_LOGO_WIDTH - M_GT_IN_AC_WIDTH;
        
        //logo??????????????????
        x = _logoWindow.frame.origin.x + M_GT_LOGO_WIDTH - M_GT_IN_AC_WIDTH - M_GT_IN_AC_OFFSET;
        y = _logoWindow.frame.origin.y - width + M_GT_IN_AC_HEIGHT;
        if ((x + height <= M_GT_SCREEN_WIDTH) && (y >= 0)) {
            frame.origin.x = x;
            frame.origin.y = y;
        }
        
        //logo??????????????????
        x = _logoWindow.frame.origin.x + M_GT_LOGO_WIDTH - M_GT_IN_AC_WIDTH - M_GT_IN_AC_OFFSET;
        y = _logoWindow.frame.origin.y + M_GT_LOGO_HEIGHT - M_GT_IN_AC_HEIGHT;
        if ((x + height <= M_GT_SCREEN_WIDTH) && (y + width <= M_GT_SCREEN_HEIGHT)) {
            frame.origin.x = x;
            frame.origin.y = y;
        }
        
        //logo??????????????????
        x = _logoWindow.frame.origin.x - height + M_GT_IN_AC_WIDTH + M_GT_IN_AC_OFFSET;
        y = _logoWindow.frame.origin.y - width + M_GT_IN_AC_HEIGHT;
        if ((x >= 0) && (y >= 0)) {
            frame.origin.x = x;
            frame.origin.y = y;
        }
        
        //logo??????????????????
        x = _logoWindow.frame.origin.x - height + M_GT_IN_AC_WIDTH + M_GT_IN_AC_OFFSET;
        y = _logoWindow.frame.origin.y + M_GT_LOGO_HEIGHT - M_GT_IN_AC_HEIGHT;
        if ((x >= 0) && (y + width <= M_GT_SCREEN_HEIGHT)) {
            frame.origin.x = x;
            frame.origin.y = y;
        }
        
        
    }
    
    //???????????????????????????
    [_acWindow setFrame:CGRectMake(frame.origin.x, frame.origin.y, frame.size.width, [_acWindow height])];
//    [_acWindow setFrame:frame];
    
    [UIView commitAnimations];
}

- (void)layoutLogoWithFloatingFrame
{
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
    [UIView setAnimationDelay:0.3f];
    [UIView setAnimationDelegate:self];
    
    CGRect frame = _logoWindow.frame;
    CGRect floatingFrame = _acWindow.frame;
       
    if ([_logoWindow isPortrait]) {
        //??????????????????
        if ((floatingFrame.origin.x - frame.size.width + M_GT_IN_AC_WIDTH >= 0 )
            && (floatingFrame.origin.y - frame.size.height + M_GT_IN_AC_HEIGHT + M_GT_IN_AC_OFFSET >= 0))
        {
            frame.origin.x = floatingFrame.origin.x - frame.size.width + M_GT_IN_AC_WIDTH;
            frame.origin.y = floatingFrame.origin.y - frame.size.height + M_GT_IN_AC_HEIGHT + M_GT_IN_AC_OFFSET;
        }
        
        
        //??????????????????
        if ((floatingFrame.origin.x + floatingFrame.size.width + frame.size.width <= M_GT_SCREEN_WIDTH + M_GT_IN_AC_WIDTH)
            && (floatingFrame.origin.y - frame.size.height + M_GT_IN_AC_HEIGHT + M_GT_IN_AC_OFFSET >= 0))
        {
            frame.origin.x = floatingFrame.origin.x + floatingFrame.size.width - M_GT_IN_AC_WIDTH;
            frame.origin.y = floatingFrame.origin.y - frame.size.height + M_GT_IN_AC_HEIGHT + M_GT_IN_AC_OFFSET;
        }
        
        
        //??????????????????
        if ((floatingFrame.origin.x - frame.size.width + M_GT_IN_AC_WIDTH >= 0 )
            && (floatingFrame.origin.y + floatingFrame.size.height + frame.size.height <= M_GT_SCREEN_HEIGHT + M_GT_IN_AC_HEIGHT + M_GT_IN_AC_OFFSET))
        {
            frame.origin.x = floatingFrame.origin.x - frame.size.width + M_GT_IN_AC_WIDTH;
            frame.origin.y = floatingFrame.origin.y + floatingFrame.size.height - M_GT_IN_AC_HEIGHT - M_GT_IN_AC_OFFSET;
        }
        
        
        //??????????????????
        if ((floatingFrame.origin.x + floatingFrame.size.width + frame.size.width <= M_GT_SCREEN_WIDTH + M_GT_IN_AC_WIDTH)
            && (floatingFrame.origin.y + floatingFrame.size.height + frame.size.height <= M_GT_SCREEN_HEIGHT + M_GT_IN_AC_HEIGHT + M_GT_IN_AC_OFFSET))
        {
            frame.origin.x = floatingFrame.origin.x + floatingFrame.size.width - M_GT_IN_AC_WIDTH;
            frame.origin.y = floatingFrame.origin.y + floatingFrame.size.height - M_GT_IN_AC_HEIGHT - M_GT_IN_AC_OFFSET;
        }
    } else {

        //??????????????????
        if ((floatingFrame.origin.x - frame.size.width + M_GT_IN_AC_WIDTH + M_GT_IN_AC_OFFSET >= 0 )
            && (floatingFrame.origin.y + floatingFrame.size.height + frame.size.height <= M_GT_SCREEN_HEIGHT + M_GT_IN_AC_WIDTH))
        {
            frame.origin.x = floatingFrame.origin.x - frame.size.width + M_GT_IN_AC_WIDTH + M_GT_IN_AC_OFFSET;
            frame.origin.y = floatingFrame.origin.y + floatingFrame.size.height - M_GT_IN_AC_HEIGHT;
        }
        
        //??????????????????
        if ((floatingFrame.origin.x - frame.size.width + M_GT_IN_AC_WIDTH + M_GT_IN_AC_OFFSET >= 0 )
            && (floatingFrame.origin.y - frame.size.height + M_GT_IN_AC_HEIGHT >= 0))
        {
            frame.origin.x = floatingFrame.origin.x - frame.size.width + M_GT_IN_AC_WIDTH + M_GT_IN_AC_OFFSET;
            frame.origin.y = floatingFrame.origin.y - frame.size.height + M_GT_IN_AC_HEIGHT;
        }
        
        //??????????????????
        if ((floatingFrame.origin.x + floatingFrame.size.width + frame.size.width <= M_GT_SCREEN_WIDTH + M_GT_IN_AC_WIDTH + M_GT_IN_AC_OFFSET)
            && (floatingFrame.origin.y + floatingFrame.size.height + frame.size.height <= M_GT_SCREEN_HEIGHT + M_GT_IN_AC_HEIGHT))
        {
            frame.origin.x = floatingFrame.origin.x + floatingFrame.size.width - M_GT_IN_AC_WIDTH - M_GT_IN_AC_OFFSET;
            frame.origin.y = floatingFrame.origin.y + floatingFrame.size.height - M_GT_IN_AC_HEIGHT;
        }
        
        //??????????????????
        if ((floatingFrame.origin.x + floatingFrame.size.width + frame.size.width <= M_GT_SCREEN_WIDTH + M_GT_IN_AC_WIDTH + M_GT_IN_AC_OFFSET)
            && (floatingFrame.origin.y - frame.size.height + M_GT_IN_AC_HEIGHT >= 0))
        {
            frame.origin.x = floatingFrame.origin.x + floatingFrame.size.width - M_GT_IN_AC_WIDTH - M_GT_IN_AC_OFFSET;
            frame.origin.y = floatingFrame.origin.y - frame.size.height + M_GT_IN_AC_HEIGHT;
        }
        
    }
    
    //???????????????????????????
    [_logoWindow setFrame:CGRectMake(frame.origin.x, frame.origin.y, M_GT_LOGO_WIDTH, M_GT_LOGO_HEIGHT)];
//    [_logoWindow setFrame:frame];
    
    [UIView commitAnimations];
}

- (void)layoutFrame
{
    BOOL showAC = [[GTConfig sharedInstance] showAC];
    if (showAC) {
        [self layoutLogoWithFloatingFrame];
        return;
    }
    
    [self layoutLogoFrame];
}



- (void)setGTHidden:(BOOL)hidden
{
    _hidden = hidden;
    
    [_logoWindow setHidden:hidden];
    
    if (hidden == YES) {
        BOOL showAC = [[GTConfig sharedInstance] showAC];
        if (showAC) {
            [[GTConfig sharedInstance] setShowAC:NO];
            [self switchFloating:NO];
            [self layoutFrame];
        }
    }
}


- (BOOL)getGTHidden
{
	return _hidden;
}

- (void)closeDetailedWindow
{
    [_detailedWindow onCloseWindow:nil];
}


- (void)setLogoFrame:(CGRect)frame
{
    //???????????????????????????
    [_logoWindow setFrame:CGRectMake(frame.origin.x, frame.origin.y, M_GT_LOGO_WIDTH, M_GT_LOGO_HEIGHT)];
//    [_logoWindow setFrame:frame];
    
    [self layoutFrame];
}
@end

#pragma mark - User Interface

void func_setLogoPoint(float x, float y)
{
    [[GTUIManager sharedInstance] setLogoFrame:CGRectMake(x, y, M_GT_LOGO_WIDTH, M_GT_LOGO_HEIGHT)];
}

void func_showGTAC()
{
    if ([[GTConfig sharedInstance] useGT]) {
        BOOL showAC = [[GTConfig sharedInstance] showAC];
        if (!showAC) {
            [[GTUIManager sharedInstance] onIconACWindow];
        }
    }
    
}

void func_hideGTAC()
{
    if ([[GTConfig sharedInstance] useGT]) {
        BOOL showAC = [[GTConfig sharedInstance] showAC];
        if (showAC) {
            [[GTUIManager sharedInstance] onIconACWindow];
        }
    }
    
}

void func_closeGTDetail()
{
    if ([[GTConfig sharedInstance] useGT]) {
        [[GTUIManager sharedInstance] closeDetailedWindow];
    }
    
}

void func_setLogoCallBack(void(* onOpenCallBack)(void), void(* onCloseCallBack)(void))
{
    [GTUIManager sharedInstance].onOpenCallBack = onOpenCallBack;
    [GTUIManager sharedInstance].onCloseCallBack = onCloseCallBack;
}

#endif
