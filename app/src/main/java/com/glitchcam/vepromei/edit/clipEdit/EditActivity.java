package com.glitchcam.vepromei.edit.clipEdit;

import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;
import android.widget.ImageView;

import com.meicam.sdk.NvsAVFileInfo;
import com.glitchcam.vepromei.R;
import com.glitchcam.vepromei.base.BaseActivity;
import com.glitchcam.vepromei.edit.adapter.AssetRecyclerViewAdapter;
import com.glitchcam.vepromei.edit.adapter.SpaceItemDecoration;
import com.glitchcam.vepromei.edit.clipEdit.adjust.AdjustActivity;
import com.glitchcam.vepromei.edit.clipEdit.correctionColor.CorrectionColorActivity;
import com.glitchcam.vepromei.edit.clipEdit.photo.DurationActivity;
import com.glitchcam.vepromei.edit.clipEdit.photo.PhotoMovementActivity;
import com.glitchcam.vepromei.edit.clipEdit.speed.SpeedActivity;
import com.glitchcam.vepromei.edit.clipEdit.spilt.SpiltActivity;
import com.glitchcam.vepromei.edit.clipEdit.trim.TrimActivity;
import com.glitchcam.vepromei.edit.clipEdit.volume.VolumeActivity;
import com.glitchcam.vepromei.edit.data.AssetInfoDescription;
import com.glitchcam.vepromei.edit.data.BackupData;
import com.glitchcam.vepromei.edit.filter.ClipFilterActivity;
import com.glitchcam.vepromei.edit.grallyRecyclerView.GrallyAdapter;
import com.glitchcam.vepromei.edit.grallyRecyclerView.GrallyScaleHelper;
import com.glitchcam.vepromei.edit.interfaces.OnGrallyItemClickListener;
import com.glitchcam.vepromei.edit.interfaces.OnItemClickListener;
import com.glitchcam.vepromei.edit.view.CustomTitleBar;
import com.glitchcam.vepromei.selectmedia.SelectMediaActivity;
import com.glitchcam.vepromei.utils.AppManager;
import com.glitchcam.vepromei.utils.Constants;
import com.glitchcam.vepromei.utils.ScreenUtils;
import com.glitchcam.vepromei.utils.Util;
import com.glitchcam.vepromei.utils.dataInfo.AnimationInfo;
import com.glitchcam.vepromei.utils.dataInfo.ClipInfo;
import com.glitchcam.vepromei.utils.dataInfo.TimelineData;
import com.glitchcam.vepromei.utils.dataInfo.TransitionInfo;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EditActivity extends BaseActivity {
    /*
     * ??????
     * Clip crop
     * */
    public static final int CLIPTRIM_REQUESTCODE = 101;
    /*
     * ??????
     * Clip spilt
     * */
    public static final int CLIPSPILTPOINT_REQUESTCODE = 102;
    /*
     * ??????
     * Color correction
     * */
    public static final int CLIPCORRECTIONCOLOR_REQUESTCODE = 103;
    /*
     * ??????
     * Adjustment
     * */
    public static final int CLIPADJUST_REQUESTCODE = 104;
    /*
     * ??????
     * speed
     * */
    public static final int CLIPSPEED_REQUESTCODE = 105;
    /*
     * ??????
     * volume
     * */
    public static final int CLIPVOLUME_REQUESTCODE = 106;
    /*
     * ????????????
     * Add video
     * */
    public static final int ADDVIDEO_REQUESTCODE = 107;
    /*
     * ????????????
     * Picture duration
     * */
    public static final int PHOTODURATION_REQUESTCODE = 108;
    /*
     * ????????????
     * Picture movement
     * */
    public static final int PHOTOMOVE_REQUESTCODE = 109;

    private CustomTitleBar mEditCustomTitleBar;
    private RecyclerView mGrallyRecyclerView;
    private RecyclerView mEffectRecyclerView;

    private int[] ImageId_Video = {
            R.drawable.trim,
            R.drawable.ratio,
            R.drawable.copy,
            R.drawable.speed,
            R.drawable.division,
            R.drawable.amend,
            R.drawable.filter,
            R.drawable.volume,
            R.drawable.delete
    };

    private int[] ImageId_Image = {
            R.drawable.speed,
            R.drawable.ratio,
            R.drawable.amend,
            R.drawable.filter,
            R.drawable.copy,
            R.drawable.delete
    };

    private AssetRecyclerViewAdapter mAssetRecycleAdapter;
    private ArrayList<AssetInfoDescription> mArrayAssetInfoVideo = new ArrayList<>();
    private ArrayList<AssetInfoDescription> mArrayAssetInfoImage = new ArrayList<>();
    private GrallyAdapter mGrallyAdapter;
    private ArrayList<ClipInfo> mClipInfoArray = new ArrayList<>();
    private GrallyScaleHelper mGrallyScaleHelper;
    private int mCurrentPos = 0;
    private ImageView mEditCommitButton;
    private boolean m_waitFlag;
    private int mAddVideoPostion = 0;
    private boolean mIsImage = false;
    private ArrayList<TransitionInfo> mTransitionInfoArray;
    /**
     *
     * clip ????????????????????? ????????????????????? ????????? ?????????????????????????????? ????????????????????????
     * ?????????????????????????????????
     */
    private ConcurrentHashMap<Integer, AnimationInfo> mVideoClipFxMap = new ConcurrentHashMap<>();
    @Override
    protected int initRootView() {
        return R.layout.activity_edit;
    }

    @Override
    protected void initViews() {
        mEditCustomTitleBar = findViewById(R.id.title_bar);
        mGrallyRecyclerView = findViewById(R.id.editClipRecyclerView);
        mEffectRecyclerView = findViewById(R.id.effectRecyclerView);
        mEditCommitButton = findViewById(R.id.edit_commitButton);
    }

    @Override
    protected void initTitle() {
        mEditCustomTitleBar.setTextCenter(getResources().getString(R.string.edit));
        mEditCustomTitleBar.setBackImageVisible(View.GONE);
    }

    @Override
    protected void initData() {
        mTransitionInfoArray = TimelineData.instance().cloneTransitionsData();
        mClipInfoArray = TimelineData.instance().cloneClipInfoData();
        BackupData.instance().setClipIndex(0);
        BackupData.instance().setClipInfoData(mClipInfoArray);
        String[] AssetNameVideo = getResources().getStringArray(R.array.effectNamesVideo);
        for (int i = 0; i < AssetNameVideo.length; i++) {
            mArrayAssetInfoVideo.add(new AssetInfoDescription(AssetNameVideo[i], ImageId_Video[i]));
        }
        String[] AssetNameImage = getResources().getStringArray(R.array.effectNamesImage);
        for (int i = 0; i < AssetNameImage.length; i++) {
            mArrayAssetInfoImage.add(new AssetInfoDescription(AssetNameImage[i], ImageId_Image[i]));
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mGrallyRecyclerView.setLayoutManager(linearLayoutManager);
        mGrallyAdapter = new GrallyAdapter(getApplicationContext());
        mGrallyAdapter.setClipInfoArray(mClipInfoArray);
        mGrallyRecyclerView.setAdapter(mGrallyAdapter);

        ItemTouchHelper.Callback callback = new com.glitchcam.vepromei.edit.grallyRecyclerView.ItemTouchHelper(mGrallyAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mGrallyRecyclerView);
        mGrallyScaleHelper = new GrallyScaleHelper();
        mGrallyScaleHelper.attachToRecyclerView(mGrallyRecyclerView);
        /*
         * ????????????
         * Effect list
         * */
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mEffectRecyclerView.setLayoutManager(layoutManager);
        mAssetRecycleAdapter = new AssetRecyclerViewAdapter(this);
        mEffectRecyclerView.setAdapter(mAssetRecycleAdapter);
        mEffectRecyclerView.addItemDecoration(new SpaceItemDecoration(ScreenUtils.dip2px(this, 6), ScreenUtils.dip2px(this, 8)));
        if (mClipInfoArray.size() > 0) {
            ClipInfo clipInfo = mClipInfoArray.get(0);
            updateOperateMenu(clipInfo);
        }
        //clip ?????????????????????
        ConcurrentHashMap<Integer, AnimationInfo> fxMap = TimelineData.instance().getmAnimationFxMap();
        mVideoClipFxMap.putAll(fxMap);
    }

    @Override
    protected void initListener() {
        mAssetRecycleAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                if (m_waitFlag) {
                    return;
                }

                if (!mIsImage) {
                    switch (pos) {
                        case 0://trim
                            m_waitFlag = true;
                            AppManager.getInstance().jumpActivityForResult(AppManager.getInstance().currentActivity(),
                                    TrimActivity.class, null, EditActivity.CLIPTRIM_REQUESTCODE);
                            break;
                        case 1: // ratio
                            m_waitFlag = true;
                            AppManager.getInstance().jumpActivityForResult(AppManager.getInstance().currentActivity(),
                                    AdjustActivity.class, null, EditActivity.CLIPADJUST_REQUESTCODE);
                            break;
                        case 2: // copy media asset
                            m_waitFlag = true;
                            copyMediaAsset();
                            break;
                        case 3: // speed
                            m_waitFlag = true;
                            AppManager.getInstance().jumpActivityForResult(AppManager.getInstance().currentActivity(),
                                    SpeedActivity.class, null, EditActivity.CLIPSPEED_REQUESTCODE);
                            break;
                        case 4: // split
                            m_waitFlag = true;
                            AppManager.getInstance().jumpActivityForResult(AppManager.getInstance().currentActivity(),
                                    SpiltActivity.class, null, EditActivity.CLIPSPILTPOINT_REQUESTCODE);
                            break;
                        case 5: // brightness
                            m_waitFlag = true;
                            AppManager.getInstance().jumpActivityForResult(AppManager.getInstance().currentActivity(),
                                    CorrectionColorActivity.class, null, EditActivity.CLIPCORRECTIONCOLOR_REQUESTCODE);
                            break;
                        case 6: // filter
                            m_waitFlag = true;
                            AppManager.getInstance().jumpActivityForResult(AppManager.getInstance().currentActivity(),
                                    ClipFilterActivity.class, null, EditActivity.CLIPCORRECTIONCOLOR_REQUESTCODE);
                            break;
                        case 7: // volume
                            AppManager.getInstance().jumpActivityForResult(AppManager.getInstance().currentActivity(),
                                    VolumeActivity.class, null, EditActivity.CLIPVOLUME_REQUESTCODE);
                            break;
                        case 8: // delete
                            deleteMediaAsset();
                            break;
                        default:
                            break;
                    }
                } else {
                    switch (pos) {
                        case 0: // Duration
                            m_waitFlag = true;
                            AppManager.getInstance().jumpActivityForResult(AppManager.getInstance().currentActivity(),
                                    DurationActivity.class, null, EditActivity.PHOTODURATION_REQUESTCODE);
                            break;
                        case 1: // Movement
                            m_waitFlag = true;
                            AppManager.getInstance().jumpActivityForResult(AppManager.getInstance().currentActivity(),
                                    PhotoMovementActivity.class, null, EditActivity.PHOTOMOVE_REQUESTCODE);
                            break;
                        case 2: // Color correction
                            m_waitFlag = true;
                            AppManager.getInstance().jumpActivityForResult(AppManager.getInstance().currentActivity(),
                                    CorrectionColorActivity.class, null, EditActivity.CLIPCORRECTIONCOLOR_REQUESTCODE);
                            break;
                        case 3: // Filter
                            m_waitFlag = true;
                            AppManager.getInstance().jumpActivityForResult(AppManager.getInstance().currentActivity(),
                                    ClipFilterActivity.class, null, EditActivity.CLIPCORRECTIONCOLOR_REQUESTCODE);
                            break;
                        case 4: // copy
                            copyMediaAsset();
                            break;
                        case 5: // delete
                            deleteMediaAsset();
                            break;
                        default:
                            break;
                    }
                }
            }
        });

        mGrallyAdapter.setOnItemSelectedListener(new OnGrallyItemClickListener() {
            @Override
            public void onLeftItemClick(View view, int pos) {
                reAddMediaAsset(pos);
            }

            @Override
            public void onRightItemClick(View view, int pos) {
                reAddMediaAsset(pos);
            }

            @Override
            public void onItemMoved(int fromPosition, int toPosition) {
                // Collections.swap(mClipInfoArray,fromPosition,toPosition);
                swapAnimationInfo(fromPosition,toPosition);
            }

            @Override
            public void onItemDismiss(int position) {
                mClipInfoArray.remove(position);
            }

            @Override
            public void removeall() {
                mClipInfoArray.clear();
            }
        });

        mGrallyScaleHelper.setOnItemSelectedListener(new GrallyScaleHelper.OnGrallyItemSelectListener() {
            @Override
            public void onItemSelect(int pos) {
                if (pos < 0 || pos >= mClipInfoArray.size()) {
                    return;
                }
                mCurrentPos = pos;
                BackupData.instance().setClipIndex(mCurrentPos);
                ClipInfo clipInfo = mClipInfoArray.get(pos);
                updateOperateMenu(clipInfo);
            }
        });

        mEditCommitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimelineData.instance().setClipInfoData(mClipInfoArray);
                TimelineData.instance().setTransitionInfoArray(mTransitionInfoArray);
                TimelineData.instance().setmAnimationFxMap(mVideoClipFxMap);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                AppManager.getInstance().finishActivity();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
    }

    //
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (data == null) {
            return;
        }

        ArrayList<ClipInfo> addCipInfoList = BackupData.instance().getAddClipInfoList();
        switch (requestCode) {
            case CLIPTRIM_REQUESTCODE:
                break;
            case CLIPSPILTPOINT_REQUESTCODE:
                int spiltPosition = data.getIntExtra("spiltPosition", -1);
                if (spiltPosition != -1) {
                    if ((mTransitionInfoArray != null) && (!mTransitionInfoArray.isEmpty())) {
                        if (spiltPosition <= mTransitionInfoArray.size()) {
                            mTransitionInfoArray.add(spiltPosition, new TransitionInfo());
                        }
                    }
                }
                break;
            case CLIPCORRECTIONCOLOR_REQUESTCODE:
                break;
            case CLIPADJUST_REQUESTCODE:
                break;
            case CLIPSPEED_REQUESTCODE:
                break;
            case CLIPVOLUME_REQUESTCODE:
                break;
            case ADDVIDEO_REQUESTCODE:
                break;
            case PHOTODURATION_REQUESTCODE:
                break;
            case PHOTOMOVE_REQUESTCODE:
                break;
            default:
                break;
        }

        mClipInfoArray = BackupData.instance().getClipInfoData();
        if (addCipInfoList.size() > 0) {
            //????????????????????????????????????  ??? ??????????????????????????????????????????
            addNewAnimationInfo(false,mAddVideoPostion,addCipInfoList.size());
            mClipInfoArray.addAll(mAddVideoPostion, addCipInfoList);
            BackupData.instance().setClipInfoData(mClipInfoArray);
            BackupData.instance().clearAddClipInfoList();
            /*
             * ????????????????????????????????????
             * Add default transitions for new material
             * */
            if (mTransitionInfoArray != null) {
                ArrayList<TransitionInfo> temp = new ArrayList<>();
                int maxTransitionCount = mClipInfoArray.size() - mTransitionInfoArray.size() - 1;
                for (int i = 0; i < maxTransitionCount; i++) {
                    TransitionInfo transitionInfo = new TransitionInfo();
                    temp.add(transitionInfo);
                }
                if (mAddVideoPostion <= mTransitionInfoArray.size()) {
                    mTransitionInfoArray.addAll(mAddVideoPostion, temp);
                }
            }
        }
        mGrallyAdapter.setClipInfoArray(mClipInfoArray);
        mGrallyAdapter.notifyDataSetChanged();
        ClipInfo clipInfo = mClipInfoArray.get(mCurrentPos);
        updateOperateMenu(clipInfo);



    }

    @Override
    protected void onResume() {
        super.onResume();
        m_waitFlag = false;
    }

    private void reAddMediaAsset(int pos) {
        mAddVideoPostion = pos;
        Bundle bundle = new Bundle();
        bundle.putInt("visitMethod", Constants.FROMCLIPEDITACTIVITYTOVISIT);
        BackupData.instance().clearAddClipInfoList();
        AppManager.getInstance().jumpActivityForResult(EditActivity.this, SelectMediaActivity.class, bundle, ADDVIDEO_REQUESTCODE);
    }

    private void copyMediaAsset() {
        if (mClipInfoArray.size() == 0) {
            return;
        }
        int count = mClipInfoArray.size();
        if (mCurrentPos < 0 || mCurrentPos > count) {
            return;
        }
        mClipInfoArray.add(mCurrentPos, mClipInfoArray.get(mCurrentPos).clone());

        //???????????????????????????
        addNewAnimationInfo(true,mCurrentPos,1);

        /*
         * ????????????
         * Add transition
         * */
        if (mTransitionInfoArray != null && mTransitionInfoArray.size()>=mCurrentPos) {
            mTransitionInfoArray.add(mCurrentPos, new TransitionInfo());
        }
        mGrallyAdapter.setSelectPos(mCurrentPos);
        mGrallyAdapter.setClipInfoArray(mClipInfoArray);
        mGrallyAdapter.notifyDataSetChanged();
        /*
         * ??????????????????????????????
         * Copy move to next position
         * */
        mGrallyRecyclerView.smoothScrollBy(mGrallyScaleHelper.getmOnePageWidth(), 0);
        //????????????

    }

    private void deleteMediaAsset() {
        if (mClipInfoArray.size() == 0) {
            return;
        }

        if (mClipInfoArray.size() == 1) {
            String[] deleteVideoTips = getResources().getStringArray(R.array.video_delete_tips);
            Util.showDialog(EditActivity.this, deleteVideoTips[0], deleteVideoTips[1]);
            return;
        }
        int clipCount = mClipInfoArray.size();
        if (mCurrentPos < 0 || mCurrentPos >= clipCount) {
            return;
        }
        /*
         * ?????????????????????
         * delete material and transition
         * */
        mClipInfoArray.remove(mCurrentPos);

        //??????????????????item
        removeNewAnimationInfo(mCurrentPos);

        if ((mTransitionInfoArray != null) && !mTransitionInfoArray.isEmpty() && mTransitionInfoArray.size()>mCurrentPos) {
            mTransitionInfoArray.remove((mCurrentPos - 1 >= 0) ? (mCurrentPos - 1) : 0);
        }
        if (mCurrentPos == clipCount - 1) {
            mCurrentPos--;
        }
        mGrallyAdapter.setClipInfoArray(mClipInfoArray);
        mGrallyAdapter.setSelectPos(mCurrentPos);
        mGrallyAdapter.notifyDataSetChanged();
        mGrallyScaleHelper.resetCurrentOffset(mCurrentPos);
        BackupData.instance().setClipIndex(mCurrentPos);
        /*
         * ??????????????????
         * Update operation menu
         * */
        ClipInfo clipInfo = mClipInfoArray.get(mCurrentPos);
        updateOperateMenu(clipInfo);
    }

    private boolean isImage(ClipInfo clipInfo) {
        if (clipInfo != null) {
            NvsAVFileInfo avFileInfo = mStreamingContext.getAVFileInfo(clipInfo.getFilePath());
            if (avFileInfo != null) {
                if (avFileInfo.getAVFileType() == NvsAVFileInfo.AV_FILE_TYPE_IMAGE) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateOperateMenu(ClipInfo clipInfo) {
        if (mStreamingContext != null && clipInfo != null) {
            mIsImage = isImage(clipInfo);
            if (mAssetRecycleAdapter != null) {
                mAssetRecycleAdapter.updateData(mIsImage ? mArrayAssetInfoImage : mArrayAssetInfoVideo);
            }
        }
    }

    /**
     * ??????????????????item
     * ??????????????????????????????????????????????????????????????????????????????key+size ????????????key
     * ?????????????????????????????????????????????????????????mCurrentPosition ??????
     *
     * @param addItem ????????????????????????
     * @param mCurrentPos ??????????????????????????????
     * @param size ???????????????
     */
    private void addNewAnimationInfo(boolean addItem ,int mCurrentPos,int size) {
        //????????????????????????????????????????????????
        if(null != mVideoClipFxMap && mVideoClipFxMap.size()>0 ){
            //??????????????????????????????????????????;
            ConcurrentHashMap<Integer, AnimationInfo> tempMap = new ConcurrentHashMap<>();
            Set<Integer>keySet = mVideoClipFxMap.keySet();
            for(Integer key : keySet){
                if(key >= mCurrentPos){
                    //??????????????????????????????key+size ????????????key
                    AnimationInfo animationInfo = mVideoClipFxMap.get(key);
                    tempMap.put((key+size),animationInfo);
                }else{
                    AnimationInfo animationInfo = mVideoClipFxMap.get(key);
                    tempMap.put(key,animationInfo);
                }
            }
            if(addItem){
                AnimationInfo animationInfo = mVideoClipFxMap.get(mCurrentPos);
                AnimationInfo newOne = new AnimationInfo();
                newOne.setmAssetType(animationInfo.getmAssetType());
                newOne.setmAnimationIn(animationInfo.getmAnimationIn());
                newOne.setmAnimationOut(animationInfo.getmAnimationOut());
                newOne.setmPackageId(animationInfo.getmPackageId());
                tempMap.put(mCurrentPos,newOne);
            }
            //??????????????????
            mVideoClipFxMap.clear();
            mVideoClipFxMap.putAll(tempMap);
        }

    }

    /**
     * ??????????????????item
     * ??????????????????????????????????????????key  ????????????key????????? mCurrentPos
     * ???????????? key = ?????????mCurrentPos ???item
     * ?????????mCurrentPos ??? ???key  ?????????-1 ??????
     * @param mCurrentPos ??????????????????index
     */
    private void removeNewAnimationInfo(int mCurrentPos) {
        //????????????????????????????????????????????????
        if(null != mVideoClipFxMap && mVideoClipFxMap.size()>0){
            //??????????????????????????????????????????;
            ConcurrentHashMap<Integer, AnimationInfo> tempMap = new ConcurrentHashMap<>();

            //??????????????????
            if(mVideoClipFxMap.containsKey(mCurrentPos)){
                 mVideoClipFxMap.remove(mCurrentPos);
             }

            //???????????? ???????????????????????????mCurrentPos??????????????? key-1
            Set<Integer>keySet = mVideoClipFxMap.keySet();
            for(Integer key : keySet){
                //key ????????????map
                //???????????????item ?????? key-1??????
                if(key > mCurrentPos){
                    AnimationInfo animationInfo = mVideoClipFxMap.get(key);
                    tempMap.put((key-1),animationInfo);
                }else{
                    AnimationInfo animationInfo = mVideoClipFxMap.get(key);
                    tempMap.put(key,animationInfo);
                }
            }
            //????????????
            mVideoClipFxMap.clear();
            mVideoClipFxMap.putAll(tempMap);
        }

    }

    /**
     * ????????????item ??????
     * @param fromPosition ????????????
     * @param toPosition ????????????
     */
    private void swapAnimationInfo(int fromPosition,int toPosition) {
        //????????????????????????????????????????????????
        if(null != mVideoClipFxMap && mVideoClipFxMap.size()>0 && mVideoClipFxMap.containsKey(fromPosition) &&mVideoClipFxMap.containsKey(toPosition) ){
            AnimationInfo animationInfoFrom = mVideoClipFxMap.get(fromPosition);
            AnimationInfo animationInfoTo = mVideoClipFxMap.get(toPosition);
            mVideoClipFxMap.put(fromPosition,animationInfoTo);
            mVideoClipFxMap.put(toPosition,animationInfoFrom);
        }

    }
}
