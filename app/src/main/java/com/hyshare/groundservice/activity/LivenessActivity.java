package com.hyshare.groundservice.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.TextureView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyshare.groundservice.R;
import com.hyshare.groundservice.base.BaseActivity;
import com.hyshare.groundservice.model.BaseModel;
import com.hyshare.groundservice.util.BitmapUtil;
import com.hyshare.groundservice.util.ToastUtil;
import com.megvii.livenessdetection.DetectionConfig;
import com.megvii.livenessdetection.DetectionFrame;
import com.megvii.livenessdetection.Detector;
import com.megvii.livenessdetection.FaceQualityManager;
import com.megvii.livenessdetection.bean.FaceIDDataStruct;
import com.megvii.livenessdetection.bean.FaceInfo;
import com.megvii.livenesslib.FaceMask;
import com.megvii.livenesslib.util.ConUtil;
import com.megvii.livenesslib.util.DialogUtil;
import com.megvii.livenesslib.util.ICamera;
import com.megvii.livenesslib.util.IDetection;
import com.megvii.livenesslib.util.IMediaPlayer;
import com.megvii.livenesslib.util.Screen;
import com.megvii.livenesslib.util.SensorUtil;
import com.megvii.livenesslib.view.CircleProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LivenessActivity extends BaseActivity
        implements Camera.PreviewCallback, Detector.DetectionListener, TextureView.SurfaceTextureListener {

    private String TAG = getClass().getSimpleName();

    private TextureView camerapreview;
    private FaceMask mFaceMask;// 画脸位置的类（调试时会用到）
    private ProgressBar mProgressBar;// 网络上传请求验证时出现的ProgressBar
    private LinearLayout headViewLinear;// "请在光线充足的情况下进行检测"这个视图
    private RelativeLayout rootView;// 根视图
    private TextView timeOutText;
    private RelativeLayout timeOutRel;
    private CircleProgressBar mCircleProgressBar;

    private Detector mDetector;// 活体检测器
    private ICamera mICamera;// 照相机工具类
    private Handler mainHandler;
    private HandlerThread mHandlerThread = new HandlerThread("videoEncoder");
    private Handler mHandler;
    private JSONObject jsonObject;
    private IMediaPlayer mIMediaPlayer;// 多媒体工具类
    private IDetection mIDetection;
    private DialogUtil mDialogUtil;

    private TextView promptText;
    private boolean isHandleStart;// 是否开始检测
    private FaceQualityManager mFaceQualityManager;
    private SensorUtil sensorUtil;

    private boolean isSuccess;

    public static void startAct(Context context) {
        Intent intent = new Intent(context, LivenessActivity.class);
        context.startActivity(intent);
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_liveness;
    }

    @Override
    public void initUI() {
//        mToolbarBinding.title.setText("人脸识别");
        initView();
        initDat();
    }

    @Override
    public void initData() {

    }

    protected void initView() {
        sensorUtil = new SensorUtil(getApplicationContext());
        Screen.initialize(this);
        mainHandler = new Handler();
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mIMediaPlayer = new IMediaPlayer(this);
        mDialogUtil = new DialogUtil(this);
        rootView = findViewById(com.megvii.livenesslib.R.id.liveness_layout_rootRel);
        mIDetection = new IDetection(this, rootView);
        mFaceMask = findViewById(com.megvii.livenesslib.R.id.liveness_layout_facemask);
        mICamera = new ICamera();
        promptText = findViewById(com.megvii.livenesslib.R.id.liveness_layout_promptText);
        camerapreview = findViewById(com.megvii.livenesslib.R.id.liveness_layout_textureview);
        camerapreview.setSurfaceTextureListener(this);
        mProgressBar = findViewById(com.megvii.livenesslib.R.id.liveness_layout_progressbar);
        mProgressBar.setVisibility(View.INVISIBLE);
        headViewLinear = findViewById(com.megvii.livenesslib.R.id.liveness_layout_bottom_tips_head);
        headViewLinear.setVisibility(View.VISIBLE);
        timeOutRel = findViewById(com.megvii.livenesslib.R.id.detection_step_timeoutRel);
        timeOutText = findViewById(com.megvii.livenesslib.R.id.detection_step_timeout_garden);
        mCircleProgressBar = findViewById(com.megvii.livenesslib.R.id.detection_step_timeout_progressBar);

        mIDetection.viewsInit();
    }

    /**
     * 初始化数据
     */
    protected void initDat() {
        // 初始化活体检测器
        DetectionConfig config = new DetectionConfig.Builder().build();
        mDetector = new Detector(this, config);
        boolean initSuccess = mDetector.init(this, ConUtil.readModel(this), "");
        if (!initSuccess) {
            mDialogUtil.showDialog(getString(com.megvii.livenesslib.R.string.meglive_detect_initfailed));
        }

        // 初始化动画
        new Thread(new Runnable() {
            @Override
            public void run() {
                mIDetection.animationInit();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isHandleStart = false;
        // 打开照相机
        Camera mCamera = mICamera.openCamera(this);
        if (mCamera != null) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(1, cameraInfo);
            mFaceMask.setFrontal(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
            // 获取到相机分辨率对应的显示大小，并把这个值复制给camerapreview
            RelativeLayout.LayoutParams layout_params = mICamera.getLayoutParam();
            camerapreview.setLayoutParams(layout_params);
            mFaceMask.setLayoutParams(layout_params);
            // 初始化人脸质量检测管理类
            mFaceQualityManager = new FaceQualityManager(1 - 0.5f, 0.5f);
            mIDetection.mCurShowIndex = -1;

        } else {
            mDialogUtil.showDialog(getString(com.megvii.livenesslib.R.string.meglive_camera_initfailed));
        }
    }

    /**
     * 开始检测
     */
    private void handleStart() {
        if (isHandleStart)
            return;
        isHandleStart = true;
        // 开始动画
        Animation animationIN = AnimationUtils.loadAnimation(LivenessActivity.this, com.megvii.livenesslib.R.anim.liveness_rightin);
        Animation animationOut = AnimationUtils.loadAnimation(LivenessActivity.this, com.megvii.livenesslib.R.anim.liveness_leftout);
        headViewLinear.startAnimation(animationOut);
        mIDetection.mAnimViews[0].setVisibility(View.VISIBLE);
        mIDetection.mAnimViews[0].startAnimation(animationIN);
        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                timeOutRel.setVisibility(View.VISIBLE);
            }
        });
        // 开始活体检测
        mainHandler.post(mTimeoutRunnable);

        jsonObject = new JSONObject();
    }

    private Runnable mTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            // 倒计时开始
            initDetecteSession();
            if (mIDetection.mDetectionSteps != null)
                changeType(mIDetection.mDetectionSteps.get(0), 10);
        }
    };

    private void initDetecteSession() {
        if (mICamera.mCamera == null)
            return;

        mProgressBar.setVisibility(View.INVISIBLE);
        mIDetection.detectionTypeInit();

        mCurStep = 0;
        mDetector.reset();
        mDetector.changeDetectionType(mIDetection.mDetectionSteps.get(0));
    }

    /**
     * 照相机预览数据回调 （PreviewCallback的接口回调方法）
     */
    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        Camera.Size previewsize = camera.getParameters().getPreviewSize();
        // 活体检测器检测
        mDetector.doDetection(data, previewsize.width, previewsize.height, 360 - mICamera.getCameraAngle(this));
    }

    /**
     * 活体验证成功 （DetectionListener的接口回调方法）
     */
    @Override
    public Detector.DetectionType onDetectionSuccess(final DetectionFrame validFrame) {
        mIMediaPlayer.reset();
        mCurStep++;
        mFaceMask.setFaceInfo(null);

        if (mCurStep == mIDetection.mDetectionSteps.size()) {
            mProgressBar.setVisibility(View.VISIBLE);
            getLivenessData();
//            handleResult(R.string.verify_success);
        } else
            changeType(mIDetection.mDetectionSteps.get(mCurStep), 10);

        // 检测器返回值：如果不希望检测器检测则返回DetectionType.DONE，如果希望检测器检测动作则返回要检测的动作
        return mCurStep >= mIDetection.mDetectionSteps.size() ? Detector.DetectionType.DONE
                : mIDetection.mDetectionSteps.get(mCurStep);
    }

    private void getLivenessData() {
        final FaceIDDataStruct idDataStruct = mDetector.getFaceIDDataStruct();
        String delta = idDataStruct.delta;
        byte[] imageBestData = null;
        for (String key : idDataStruct.images.keySet()) {
            byte[] data = idDataStruct.images.get(key);

            if (key.equals("image_best")) {
                imageBestData = data;// 这是最好的一张图片
            } else if (key.equals("image_env")) {
                byte[] imageEnvData = data;// 这是一张全景图
            } else {
                // 其余为其他图片，根据需求自取
            }
        }

        //上传活体检测结果到服务器
        verifyFace(delta, imageBestData);
    }

    private void verifyFace(String delta, byte[] imageBestData) {

        Map<String, Object> param = new HashMap<>();
        param.put("delta", delta);
        param.put("image_best", BitmapUtil.convertByteToString(imageBestData));
        JSONObject object = new JSONObject(param);

        getApiService().faceVerify(parseRequest(object.toString()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseModel<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BaseModel<String> stringBaseModel) {
                        if (stringBaseModel.getCode() == 1) {
                            isSuccess = true;
                        } else {
                            isSuccess = false;
                        }
                        FaceResultActivity.startAct(LivenessActivity.this,isSuccess);
                        finish();
                    }
                });

    }


    /**
     * 活体检测失败 （DetectionListener的接口回调方法）
     */
    @Override
    public void onDetectionFailed(final Detector.DetectionFailedType type) {
        int resourceID = com.megvii.livenesslib.R.string.liveness_detection_failed;
        switch (type) {
            case ACTIONBLEND:
                resourceID = com.megvii.livenesslib.R.string.liveness_detection_failed_action_blend;
                break;
            case NOTVIDEO:
                resourceID = com.megvii.livenesslib.R.string.liveness_detection_failed_not_video;
                break;
            case TIMEOUT:
                resourceID = com.megvii.livenesslib.R.string.liveness_detection_failed_timeout;
                break;

        }
        handleResult(resourceID);
    }

    /**
     * 活体验证中（这个方法会持续不断的回调，返回照片detection信息） （DetectionListener的接口回调方法）
     */
    @Override
    public void onFrameDetected(long timeout, DetectionFrame detectionFrame) {
        faceOcclusion(detectionFrame);
        handleNotPass(timeout);
        mFaceMask.setFaceInfo(detectionFrame);
//        if (sensorUtil.isVertical()) {
//            faceOcclusion(detectionFrame);
//            handleNotPass(timeout);
//            mFaceMask.setFaceInfo(detectionFrame);
//        } else {
//            if (sensorUtil.Y == 0)
//                promptText.setText(R.string.meglive_getpermission_motion);
//                promptText.setText(R.string.meglive_phone_vertical);
//            else
//        }
    }

    /**
     * 照镜子环节
     * 流程：1,先从返回的DetectionFrame中获取FaceInfo。在FaceInfo中可以先判断这张照片上的人脸是否有被遮挡的状况
     * ，入股有直接return
     * 2,如果没有遮挡就把SDK返回的DetectionFramed传入人脸质量检测管理类mFaceQualityManager中获取FaceQualityErrorType的list
     * 3.通过返回的list来判断这张照片上的人脸是否合格。
     * 如果返回list为空或list中FaceQualityErrorType的对象数量为0则表示这张照片合格开始进行活体检测
     */
    private void faceOcclusion(DetectionFrame detectionFrame) {
        mFailFrame++;
        if (detectionFrame != null) {
            FaceInfo faceInfo = detectionFrame.getFaceInfo();
            if (faceInfo != null) {
                if (faceInfo.eyeLeftOcclusion > 0.5 || faceInfo.eyeRightOcclusion > 0.5) {
                    if (mFailFrame > 10) {
                        mFailFrame = 0;
                        promptText.setText(com.megvii.livenesslib.R.string.meglive_keep_eyes_open);
                    }
                    return;
                }
                if (faceInfo.mouthOcclusion > 0.5) {
                    if (mFailFrame > 10) {
                        mFailFrame = 0;
                        promptText.setText(com.megvii.livenesslib.R.string.meglive_keep_mouth_open);
                    }
                    return;
                }
                boolean faceTooLarge = faceInfo.faceTooLarge;
                mIDetection.checkFaceTooLarge(faceTooLarge);
            }
        }
        // 从人脸质量检测管理类中获取错误类型list
        faceInfoChecker(mFaceQualityManager.feedFrame(detectionFrame));
    }

    private int mFailFrame = 0;

    public void faceInfoChecker(List<FaceQualityManager.FaceQualityErrorType> errorTypeList) {
        if (errorTypeList == null || errorTypeList.size() == 0)
            handleStart();
        else {
            String infoStr = "";
            FaceQualityManager.FaceQualityErrorType errorType = errorTypeList.get(0);
            if (errorType == FaceQualityManager.FaceQualityErrorType.FACE_NOT_FOUND) {
                infoStr = getString(com.megvii.livenesslib.R.string.face_not_found);
            } else if (errorType == FaceQualityManager.FaceQualityErrorType.FACE_POS_DEVIATED) {
                infoStr = getString(com.megvii.livenesslib.R.string.face_not_found);
            } else if (errorType == FaceQualityManager.FaceQualityErrorType.FACE_NONINTEGRITY) {
                infoStr = getString(com.megvii.livenesslib.R.string.face_not_found);
            } else if (errorType == FaceQualityManager.FaceQualityErrorType.FACE_TOO_DARK) {
                infoStr = getString(com.megvii.livenesslib.R.string.face_too_dark);
            } else if (errorType == FaceQualityManager.FaceQualityErrorType.FACE_TOO_BRIGHT) {
                infoStr = getString(com.megvii.livenesslib.R.string.face_too_bright);
            } else if (errorType == FaceQualityManager.FaceQualityErrorType.FACE_TOO_SMALL) {
                infoStr = getString(com.megvii.livenesslib.R.string.face_too_small);
            } else if (errorType == FaceQualityManager.FaceQualityErrorType.FACE_TOO_LARGE) {
                infoStr = getString(com.megvii.livenesslib.R.string.face_too_large);
            } else if (errorType == FaceQualityManager.FaceQualityErrorType.FACE_TOO_BLURRY) {
                infoStr = getString(com.megvii.livenesslib.R.string.face_too_blurry);
            } else if (errorType == FaceQualityManager.FaceQualityErrorType.FACE_OUT_OF_RECT) {
                infoStr = getString(com.megvii.livenesslib.R.string.face_out_of_rect);
            }

            if (mFailFrame > 10) {
                mFailFrame = 0;
                promptText.setText(infoStr);
            }
        }
    }

    /**
     * 跳转Activity传递信息
     */
    private void handleResult(final int resID) {
        String resultString = getResources().getString(resID);
        try {
            jsonObject.put("result", resultString);
            jsonObject.put("resultcode", resID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent();
        intent.putExtra("result", false);
        setResult(RESULT_OK, intent);

        finish();
    }

    private int mCurStep = 0;// 检测动作的次数

    public void changeType(final Detector.DetectionType detectiontype, long timeout) {
        // 动画切换
        mIDetection.changeType(detectiontype, timeout);
        mFaceMask.setFaceInfo(null);

        // 语音播放
        if (mCurStep == 0) {
            mIMediaPlayer.doPlay(mIMediaPlayer.getSoundRes(detectiontype));
        } else {
            mIMediaPlayer.doPlay(com.megvii.livenesslib.R.raw.meglive_well_done);
            mIMediaPlayer.setOnCompletionListener(detectiontype);
        }
    }

    public void handleNotPass(final long remainTime) {
        if (remainTime > 0) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    timeOutText.setText(remainTime / 1000 + "");
                    mCircleProgressBar.setProgress((int) (remainTime / 100));
                }
            });
        }
    }

    private boolean mHasSurface = false;

    /**
     * TextureView启动成功后 启动相机预览和添加活体检测回调
     * （TextureView.SurfaceTextureListener的接口回调方法）
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mHasSurface = true;
        doPreview();
        // 添加活体检测回调 （本Activity继承了DetectionListener）
        mDetector.setDetectionListener(this);
        // 添加相机预览回调（本Activity继承了PreviewCallback）
        mICamera.actionDetect(this);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    /**
     * TextureView销毁后 （TextureView.SurfaceTextureListener的接口回调方法）
     */
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mHasSurface = false;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private void doPreview() {
        if (!mHasSurface)
            return;

        mICamera.startPreview(camerapreview.getSurfaceTexture());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mainHandler.removeCallbacksAndMessages(null);
        mICamera.closeCamera();
        mIMediaPlayer.close();

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDetector != null)
            mDetector.release();
        mDialogUtil.onDestory();
        mIDetection.onDestroy();
        sensorUtil.release();

        if (isSuccess) {
//            PresenterManager.key(ReserveOrderFragment.class).onDo(BaseEventContacts.reserveorder_opendoor);
            ToastUtil.toast("成功！");
        }
    }
}