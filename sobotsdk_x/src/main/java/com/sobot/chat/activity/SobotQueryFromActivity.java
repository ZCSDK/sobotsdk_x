package com.sobot.chat.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotChatBaseActivity;
import com.sobot.chat.activity.halfdialog.SobotPostRegionActivity;
import com.sobot.chat.api.model.CommonModel;
import com.sobot.chat.api.model.SobotCityResult;
import com.sobot.chat.api.model.SobotConnCusParam;
import com.sobot.chat.api.model.SobotCusFieldConfig;
import com.sobot.chat.api.model.SobotFieldModel;
import com.sobot.chat.api.model.SobotProvinInfo;
import com.sobot.chat.api.model.SobotQueryFormModel;
import com.sobot.chat.application.MyApplication;
import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.listener.ISobotCusField;
import com.sobot.chat.presenter.StCusFieldPresenter;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.dialog.SobotDialogUtils;
import com.sobot.chat.widget.kpswitch.util.KeyboardUtil;
import com.sobot.chat.widget.toast.CustomToast;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.StringResultCallBack;

import java.util.ArrayList;

/**
 * @author Created by jinxl on 2018/1/4.
 * 询前表单
 */
public class SobotQueryFromActivity extends SobotChatBaseActivity implements ISobotCusField, View.OnClickListener {
    private Bundle mIntentBundleData;
    private SobotConnCusParam param;
    private SobotQueryFormModel mQueryFormModel;
    private String mUid;
    private ArrayList<SobotFieldModel> mField;
    private SobotProvinInfo.SobotProvinceModel mFinalData;

    private LinearLayout sobot_container;
    private TextView sobot_tv_doc;
    private TextView sobot_btn_submit;
    private TextView sobot_tv_safety;
    //防止多次提交
    private boolean isSubmitting = false;

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_query_from;
    }

    protected void initBundleData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mIntentBundleData = getIntent().getBundleExtra(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA);
        } else {
            mIntentBundleData = savedInstanceState.getBundle(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA);
        }
        if (mIntentBundleData != null) {
            initIntent(mIntentBundleData);
        }
    }

    private void initIntent(Bundle mIntentBundleData) {
        mQueryFormModel = (SobotQueryFormModel) mIntentBundleData.getSerializable(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_FIELD);
        param = (SobotConnCusParam) mIntentBundleData.getSerializable(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_CONNCUSPARAM);
        mUid = mIntentBundleData.getString(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_UID);
        if (mQueryFormModel != null) {
            mField = mQueryFormModel.getField();
        }
    }

    @Override
    protected void initView() {
        showLeftMenu(  true);
        sobot_btn_submit =   findViewById(R.id.sobot_btn_submit);
        sobot_btn_submit.setText(R.string.sobot_btn_queryfrom_submit_text);
        sobot_btn_submit.setOnClickListener(this);
        if (ThemeUtils.isChangedThemeColor(this)) {
            Drawable d = getResources().getDrawable(R.drawable.sobot_normal_btn_bg);
            sobot_btn_submit.setBackground(ThemeUtils.applyColorToDrawable(d, ThemeUtils.getThemeColor(this)));
        }
        sobot_container = (LinearLayout) findViewById(R.id.sobot_container);
        sobot_tv_doc = (TextView) findViewById(R.id.sobot_tv_doc);
        sobot_tv_safety = (TextView) findViewById(R.id.sobot_tv_safety);
        if (mQueryFormModel != null) {
            setTitle(mQueryFormModel.getFormTitle());
            HtmlTools.getInstance(getSobotBaseActivity()).setRichText(sobot_tv_doc, mQueryFormModel.getFormDoc(), R.color.sobot_color_link);
            if (!TextUtils.isEmpty(mQueryFormModel.getFormSafety())) {
                sobot_tv_safety.setVisibility(View.VISIBLE);
                sobot_tv_safety.setText(mQueryFormModel.getFormSafety());
            } else {
                sobot_tv_safety.setVisibility(View.GONE);
            }
        }
        displayInNotch(sobot_tv_doc);
        StCusFieldPresenter.addWorkOrderCusFields(SobotQueryFromActivity.this, SobotQueryFromActivity.this, mField, sobot_container, SobotQueryFromActivity.this);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //销毁前缓存数据
        outState.putBundle(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA, mIntentBundleData);
        super.onSaveInstanceState(outState);
    }


    private void submit() {
        //提交信息
        if (isSubmitting) {
            return;
        }
        isSubmitting = true;
        zhiChiApi.submitForm(SobotQueryFromActivity.this, mUid, StCusFieldPresenter.getCusFieldVal(mField, mFinalData), new StringResultCallBack<CommonModel>() {
            @Override
            public void onSuccess(CommonModel data) {
                isSubmitting = false;
                if (data != null && "1".equals(data.getCode())) {
                    ToastUtil.showCustomToast(getBaseContext(), getResources().getString(R.string.sobot_leavemsg_success_tip),
                            R.drawable.sobot_icon_success);
                    saveIntentWithFinish();
                } else if (data != null && "0".equals(data.getCode())) {
                    ToastUtil.showToast(getApplicationContext(), data.getMsg());
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                isSubmitting = false;
                ToastUtil.showToast(getApplicationContext(), des);
            }
        });
    }

    private void saveIntentWithFinish() {
        // 保存返回值 并且结束当前页面
        try {
            KeyboardUtil.hideKeyboard(sobot_container);
            Intent intent = new Intent();
            intent.putExtra(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_CONNCUSPARAM, param);
            setResult(ZhiChiConstant.REQUEST_COCE_TO_QUERY_FROM, intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 检查字段是否都填写
     *
     * @param field
     * @return
     */
    private boolean checkInput(ArrayList<SobotFieldModel> field) {
        if (field != null && field.size() != 0) {
            for (int i = 0; i < field.size(); i++) {
                if (field.get(i).getCusFieldConfig() != null) {
                    if (1 == field.get(i).getCusFieldConfig().getFillFlag()) {
                        if ("city".equals(field.get(i).getCusFieldConfig().getFieldId())) {
                            if (field.get(i).getCusFieldConfig().getProvinceModel() == null) {
                                ToastUtil.showToast(getApplicationContext(), field.get(i).getCusFieldConfig().getFieldName() + "  " + getResources().getString(R.string.sobot__is_null));
                                return false;
                            }
                        } else if (TextUtils.isEmpty(field.get(i).getCusFieldConfig().getValue())) {
                            ToastUtil.showToast(getApplicationContext(), field.get(i).getCusFieldConfig().getFieldName() + "  " + getResources().getString(R.string.sobot__is_null));
                            return false;
                        }
                    }

                    if ("email".equals(field.get(i).getCusFieldConfig().getFieldId())
                            && !TextUtils.isEmpty(field.get(i).getCusFieldConfig().getValue())
                            && !ScreenUtils.isEmail(field.get(i).getCusFieldConfig().getValue())) {
                        String msg = getResources().getString(R.string.sobot_email_dialog_hint);
                        ToastUtil.showToast(getApplicationContext(), msg);
                        return false;
                    }

                    if ("tel".equals(field.get(i).getCusFieldConfig().getFieldId())
                            && !TextUtils.isEmpty(field.get(i).getCusFieldConfig().getValue())
                            && !ScreenUtils.isMobileNO(field.get(i).getCusFieldConfig().getValue())) {
                        ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.sobot_phone) + getResources().getString(R.string.sobot_input_type_err));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        backPressed();
    }

    private void backPressed() {
        setResult(ZhiChiConstant.REQUEST_COCE_TO_QUERY_FROM_CANCEL, new Intent());
        finish();
    }

    @Override
    public void onClickCusField(final View view, final SobotCusFieldConfig field, final SobotFieldModel cusField) {
        switch (field.getFieldType()) {
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_DATE_TYPE:
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_TYPE:
                StCusFieldPresenter.openTimePicker(SobotQueryFromActivity.this, null, field);
                break;
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_SPINNER_TYPE:
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_RADIO_TYPE:
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CHECKBOX_TYPE:
                StCusFieldPresenter.startSobotCusFieldActivity(SobotQueryFromActivity.this, cusField);
                break;
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CASCADE_TYPE:
                LogUtils.i("点击了城市");
                SobotDialogUtils.startProgressDialog(SobotQueryFromActivity.this);
                zhiChiApi.queryCity(SobotQueryFromActivity.this, null, null, new StringResultCallBack<SobotCityResult>() {
                    @Override
                    public void onSuccess(SobotCityResult result) {
                        SobotDialogUtils.stopProgressDialog(SobotQueryFromActivity.this);
                        SobotProvinInfo data = result.getData();
                        if (data.getProvinces() != null && data.getProvinces().size() > 0) {
                            // 启动城市选择
                            StCusFieldPresenter.startChooseCityAct(SobotQueryFromActivity.this, data, cusField);
                        }
                    }

                    @Override
                    public void onFailure(Exception e, String des) {
                        SobotDialogUtils.stopProgressDialog(SobotQueryFromActivity.this);
                        ToastUtil.showToast(getApplicationContext(), des);
                    }
                });
                break;
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_REGION_TYPE:
                final SobotCusFieldConfig cusFieldConfig = cusField.getCusFieldConfig();
                if (cusFieldConfig != null) {
                    Intent intent = new Intent(SobotQueryFromActivity.this, SobotPostRegionActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("selectedIds", cusFieldConfig.getValue());
                    bundle.putString("selectedText", cusFieldConfig.getShowName());
                    bundle.putSerializable("cusFieldConfig", cusFieldConfig);
                    intent.putExtra("bundle", bundle);
                    startActivityForResult(intent, cusFieldConfig.getFieldType());
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        HttpUtils.getInstance().cancelTag(this);
        MyApplication.getInstance().deleteActivity(this);
        SobotDialogUtils.stopProgressDialog(SobotQueryFromActivity.this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        StCusFieldPresenter.onStCusFieldActivityResult(SobotQueryFromActivity.this, data, mField, sobot_container);

        if (data != null) {
            switch (requestCode) {
                case ZhiChiConstant.REQUEST_COCE_TO_CITY_INFO:
                    String fieldId = data.getStringExtra(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_FIELD_ID);
                    mFinalData = (SobotProvinInfo.SobotProvinceModel) data.getSerializableExtra(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_PROVININFO);
                    if (mField != null && mFinalData != null && !TextUtils.isEmpty(fieldId)) {

                        for (int i = 0; i < mField.size(); i++) {
                            SobotCusFieldConfig model = mField.get(i).getCusFieldConfig();
                            if (model != null && fieldId.equals(model.getFieldId())) {
                                model.setChecked(true);
                                model.setProvinceModel(mFinalData);
                                View view = sobot_container.findViewWithTag(fieldId);
                                if (view != null) {
                                    TextView textClick = (TextView) view.findViewById(R.id.work_order_customer_date_text_click);
                                    String pStr = mFinalData.provinceName == null ? "" : mFinalData.provinceName;
                                    String cStr = mFinalData.cityName == null ? "" : mFinalData.cityName;
                                    String aStr = mFinalData.areaName == null ? "" : mFinalData.areaName;
                                    String str = pStr + cStr + aStr;
                                    textClick.setText(str);
                                }
                            }
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == sobot_btn_submit) {
            //自定义表单校验结果:为空,校验通过,可以提交;不为空,说明自定义字段校验不通过，不能提交留言表单;
            String checkCustomFieldResult = StCusFieldPresenter.formatCusFieldVal(SobotQueryFromActivity.this, sobot_container, mField);
            if (!TextUtils.isEmpty(checkCustomFieldResult)) {
                return;
            }
            if (!checkInput(mField)) {
                return;
            }

            submit();
        }
    }
}