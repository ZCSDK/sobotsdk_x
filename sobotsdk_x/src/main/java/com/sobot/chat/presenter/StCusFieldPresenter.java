package com.sobot.chat.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.activity.halfdialog.SobotChooseCityActivity;
import com.sobot.chat.activity.halfdialog.SobotCusFieldActivity;
import com.sobot.chat.activity.halfdialog.SobotDateTimeActivity;
import com.sobot.chat.api.apiUtils.GsonUtil;
import com.sobot.chat.api.model.SobotCusFieldConfig;
import com.sobot.chat.api.model.SobotFieldModel;
import com.sobot.chat.api.model.SobotProvinInfo;
import com.sobot.chat.listener.ISobotCusField;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.SobotAntoLineLayout;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Created by jinxl on 2018/1/3.
 */
public class StCusFieldPresenter {

    /**
     * 获取要提交给接口的自定义字段的json
     * 留言接口使用
     *
     * @param field
     * @return
     */
    public static String getSaveFieldVal(ArrayList<SobotFieldModel> field) {
        List<Map<String, String>> listModel = null;
        if (field != null && field.size() > 0) {
            listModel = new ArrayList<>();
            for (int i = 0; i < field.size(); i++) {
                Map<String, String> model = new HashMap<>();
                SobotCusFieldConfig cusFieldConfig = field.get(i).getCusFieldConfig();
                if (cusFieldConfig != null && !StringUtils.isEmpty(cusFieldConfig.getFieldId())
                        && !StringUtils.isEmpty(cusFieldConfig.getValue())) {
                    model.put("id", field.get(i).getCusFieldConfig().getFieldId());
                    model.put("value", field.get(i).getCusFieldConfig().getValue());
                    if (cusFieldConfig.getFieldType() == ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_REGION_TYPE) {
                        model.put("text", field.get(i).getCusFieldConfig().getText());
                    } else {
                        model.put("text", field.get(i).getCusFieldConfig().getShowName());
                    }
                    listModel.add(model);
                }
            }
        }
        if (listModel != null && listModel.size() > 0) {
            JSONArray jsonArray = new JSONArray(listModel);//把  List 对象  转成json数据
            return jsonArray.toString();
        }
        return null;
    }


    /**
     * 获取要提交给接口的自定义字段的json
     * 留言接口使用
     *
     * @param field
     * @return
     */
    public static Map getSaveFieldNameAndVal(ArrayList<SobotFieldModel> field) {
        if (field != null && field.size() > 0) {
            Map<String, String> model = new HashMap<>();
            for (int i = 0; i < field.size(); i++) {
                SobotCusFieldConfig cusFieldConfig = field.get(i).getCusFieldConfig();
                if (cusFieldConfig != null) {
                    model.put(field.get(i).getCusFieldConfig().getFieldName(), TextUtils.isEmpty(field.get(i).getCusFieldConfig().getShowName()) ? field.get(i).getCusFieldConfig().getValue() : field.get(i).getCusFieldConfig().getShowName());
                }
            }
            return model;
        }
        return null;
    }

    /**
     * 打开时间或日期选择器的逻辑
     *
     * @param act
     */
    public static void openTimePicker(Activity act, Fragment fragment, SobotCusFieldConfig cusFieldConfig) {
//        TextView textClick = (TextView) view.findViewById(R.id.work_order_customer_date_text_click);
//        String content = textClick.getText().toString();
//        KeyboardUtil.hideKeyboard(textClick);
        Intent intent = new Intent(act, SobotDateTimeActivity.class);
        intent.putExtra("cusFieldConfig", cusFieldConfig);
        if (fragment != null) {
            fragment.startActivityForResult(intent, cusFieldConfig.getFieldType());
        } else {
            act.startActivityForResult(intent, cusFieldConfig.getFieldType());
        }
    }

    /**
     * 获取要提交给接口的自定义字段的json
     * 询前表单使用
     *
     * @param field
     * @return
     */
    public static String getCusFieldVal(ArrayList<SobotFieldModel> field, final SobotProvinInfo.SobotProvinceModel finalData) {
        Map<String, String> tmpMap = new HashMap<>();
        if (field != null && field.size() > 0) {
            for (int i = 0; i < field.size(); i++) {
                SobotCusFieldConfig cusFieldConfig = field.get(i).getCusFieldConfig();
                if (cusFieldConfig != null && !StringUtils.isEmpty(cusFieldConfig.getFieldId())
                        && !StringUtils.isEmpty(cusFieldConfig.getValue())) {
                    tmpMap.put(field.get(i).getCusFieldConfig().getFieldId(), field.get(i).getCusFieldConfig().getValue());
                }
            }
        }
        if (finalData != null) {
            tmpMap.put("proviceId", finalData.provinceId);
            tmpMap.put("proviceName", finalData.provinceName);
            tmpMap.put("cityId", finalData.cityId);
            tmpMap.put("cityName", finalData.cityName);
            tmpMap.put("areaId", finalData.areaId);
            tmpMap.put("areaName", finalData.areaName);
        }
        if (tmpMap.size() > 0) {
            return GsonUtil.map2Json(tmpMap);
        }
        return null;
    }

    /**
     * 启动自定义字段下一级选择的逻辑
     *
     * @param act
     * @param cusFieldList
     */
    public static void startSobotCusFieldActivity(Activity act, SobotFieldModel cusFieldList) {
        startSobotCusFieldActivity(act, null, cusFieldList);
    }

    /**
     * 启动自定义字段下一级选择的逻辑
     *
     * @param act
     * @param cusFieldList
     */
    public static void startSobotCusFieldActivity(Activity act, Fragment fragment, SobotFieldModel cusFieldList) {
        SobotCusFieldConfig cusFieldConfig = cusFieldList.getCusFieldConfig();
        Intent intent = new Intent(act, SobotCusFieldActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("fieldType", cusFieldConfig.getFieldType());
        bundle.putSerializable("cusFieldConfig", cusFieldConfig);
        bundle.putSerializable("cusFieldList", cusFieldList);
        intent.putExtra("bundle", bundle);
        if (fragment != null) {
            fragment.startActivityForResult(intent, cusFieldConfig.getFieldType());
        } else {
            act.startActivityForResult(intent, cusFieldConfig.getFieldType());
        }
    }

    /**
     * 启动城市选择的act
     *
     * @param act
     * @param info     省的信息
     * @param cusField 字段的信息
     */
    public static void startChooseCityAct(Activity act, SobotProvinInfo info, SobotFieldModel cusField) {
        Intent intent = new Intent(act, SobotChooseCityActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("cusFieldConfig", cusField.getCusFieldConfig());
        bundle.putSerializable(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_PROVININFO, info);
        SobotCusFieldConfig cusFieldConfig = cusField.getCusFieldConfig();
        if (cusFieldConfig != null) {
            bundle.putSerializable(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_FIELD_ID, cusFieldConfig.getFieldId());
        }
        intent.putExtra(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA, bundle);
        act.startActivityForResult(intent, ZhiChiConstant.REQUEST_COCE_TO_CITY_INFO);
    }

    /**
     * 选择子集的回调
     *
     * @param context
     * @param data
     * @param field
     * @param post_customer_field
     */
    public static void onStCusFieldActivityResult(Context context, Intent data, ArrayList<SobotFieldModel> field, ViewGroup post_customer_field) {
        if (data != null && "CATEGORYSMALL".equals(data.getStringExtra("CATEGORYSMALL")) && -1 != data.getIntExtra("fieldType", -1)) {
            String value = data.getStringExtra("category_typeName");
            String id = data.getStringExtra("category_fieldId");
            if ("null".equals(id) || TextUtils.isEmpty(id)) {
                return;
            }
            String dataValue = data.getStringExtra("category_typeValue");
            if (field != null && !StringUtils.isEmpty(value) && !StringUtils.isEmpty(dataValue)) {
                for (int i = 0; i < field.size(); i++) {
                    SobotCusFieldConfig model = field.get(i).getCusFieldConfig();
                    if (model != null && model.getFieldId() != null && model.getFieldId().equals(id)) {
                        model.setChecked(true);
                        model.setValue(dataValue);
                        model.setId(id);
                        model.setShowName(value.endsWith(",") ? value.substring(0, value.length() - 1) : value);
                        View view = post_customer_field.findViewWithTag(model.getFieldId());
                        //多选，显示标签形式
                        SobotAntoLineLayout sobot_ll_labels = view.findViewById(R.id.sobot_ll_labels);
                        TextView textClick = (TextView) view.findViewById(R.id.work_order_customer_date_text_click);
                        textClick.setText(value.endsWith(",") ? value.substring(0, value.length() - 1) : value);
                        if (model.getFieldType() == ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CHECKBOX_TYPE) {
                            sobot_ll_labels.removeAllViews();
                            sobot_ll_labels.setVisibility(View.VISIBLE);
                            textClick.setVisibility(View.GONE);
                            String[] array = value.split(",");
                            for (int j = 0; j < array.length; j++) {
                                TextView itemView = (TextView) View.inflate(context, R.layout.sobot_post_msg_cusfield_checkbox_item, null);
                                itemView.setText(array[j]);
                                sobot_ll_labels.addView(itemView);
                            }
                        } else if(model.getFieldType() ==ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CASCADE_TYPE){
                            value = value.replace(",", "/");
                            textClick.setText(value.endsWith("/") ? value.substring(0, value.length() - 1) : value);
                        } else {
                            sobot_ll_labels.setVisibility(View.GONE);
                            textClick.setVisibility(View.VISIBLE);

                        }
                    }
                }
            } else {
                //还原样式
                View view = post_customer_field.findViewWithTag(id);
                TextView textClick = null;
                SobotAntoLineLayout sobot_ll_labels = null;
                if (view != null) {
                    sobot_ll_labels = view.findViewById(R.id.sobot_ll_labels);
                    textClick = view.findViewById(R.id.work_order_customer_date_text_click);
                    textClick.setText(value.endsWith(",") ? value.substring(0, value.length() - 1) : value);
                }
                if (StringUtils.isEmpty(dataValue)) {
                    for (int i = 0; i < field.size(); i++) {
                        //清空上次选中
                        SobotCusFieldConfig model = field.get(i).getCusFieldConfig();
                        if (model != null && model.getFieldId() != null && model.getFieldId().equals(id)) {
                            model.setChecked(false);
                            model.setValue(dataValue);
                            model.setId(id);
                            if (model.getFieldType() == ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CHECKBOX_TYPE && sobot_ll_labels != null && textClick != null) {
                                sobot_ll_labels.removeAllViews();
                                sobot_ll_labels.setVisibility(View.GONE);
                                textClick.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }


            }
        }
    }

    /**
     * 提交前将数据同步到最外层属性中
     *
     * @param sobot_container
     * @param field
     * @return String 自定义表单校验结果:为空,可以提交;不为空,说明自定义字段校验不通过，不能提交留言表单;
     */
    public static String formatCusFieldVal(Context context, ViewGroup sobot_container, List<SobotFieldModel> field) {
        if (field != null && field.size() != 0) {
            for (int j = 0; j < field.size(); j++) {
                if (field.get(j).getCusFieldConfig() == null) {
                    continue;
                }
                View view = sobot_container.findViewWithTag(field.get(j).getCusFieldConfig().getFieldId());
                if (view != null) {
                    if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_SINGLE_LINE_TYPE == field.get(j).getCusFieldConfig().getFieldType()) {
                        EditText singleContent = (EditText) view.findViewById(R.id.work_order_customer_field_text_single);
                        field.get(j).getCusFieldConfig().setValue(singleContent.getText() + "");
                        if (StringUtils.isNumber(field.get(j).getCusFieldConfig().getLimitOptions()) && field.get(j).getCusFieldConfig().getLimitOptions().contains("7")) {
                            if (!ScreenUtils.isEmail(singleContent.getText().toString().trim())) {
                                return field.get(j).getCusFieldConfig().getFieldName() + context.getResources().getString(R.string.sobot_email_dialog_hint);
                            }
                        }
                        if (StringUtils.isNumber(field.get(j).getCusFieldConfig().getLimitOptions()) && field.get(j).getCusFieldConfig().getLimitOptions().contains("8")) {
                            if (!ScreenUtils.isMobileNO(singleContent.getText().toString().trim())) {
                                return field.get(j).getCusFieldConfig().getFieldName() + context.getResources().getString(R.string.sobot_phone) + context.getResources().getString(R.string.sobot_input_type_err);
                            }
                        }
                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_MORE_LINE_TYPE == field.get(j).getCusFieldConfig().getFieldType()) {
                        EditText moreContent = (EditText) view.findViewById(R.id.work_order_customer_field_text_more_content);
                        field.get(j).getCusFieldConfig().setValue(moreContent.getText() + "");
                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_TYPE == field.get(j).getCusFieldConfig().getFieldType()
                            || ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_DATE_TYPE == field.get(j).getCusFieldConfig().getFieldType()) {
                        TextView textClick = (TextView) view.findViewById(R.id.work_order_customer_date_text_click);
                        field.get(j).getCusFieldConfig().setValue(textClick.getText() + "");
                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_NUMBER_TYPE == field.get(j).getCusFieldConfig().getFieldType()) {
                        EditText singleContent = (EditText) view.findViewById(R.id.work_order_customer_field_text_single);
                        field.get(j).getCusFieldConfig().setValue(singleContent.getText() + "");
                        if (StringUtils.isNumber(field.get(j).getCusFieldConfig().getLimitOptions()) && field.get(j).getCusFieldConfig().getLimitOptions().contains("3")) {
                            if (!StringUtils.isNumber(singleContent.getText().toString().trim())) {
                                return field.get(j).getCusFieldConfig().getFieldName() + context.getResources().getString(R.string.sobot_input_type_err);
                            }
                        }
                    }
                }
            }
        }
        return "";
    }

    public static void displayInNotch(Activity activity, final View view) {
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH) && view != null) {
            // 获取刘海屏信息
            NotchScreenManager.getInstance().getNotchInfo(activity, new INotchScreen.NotchScreenCallback() {
                @Override
                public void onResult(INotchScreen.NotchScreenInfo notchScreenInfo) {
                    if (notchScreenInfo.hasNotch) {
                        for (Rect rect : notchScreenInfo.notchRects) {
                            view.setPadding((rect.right > 110 ? 110 : rect.right), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                        }
                    }
                }
            });

        }
    }

    //创建工单自定义字段
    public static void addWorkOrderCusFields(final Activity activity, final Context context, final ArrayList<SobotFieldModel> cusFieldList, ViewGroup containerLayout, final ISobotCusField cusFieldInterface) {
        if (containerLayout != null) {
            containerLayout.setVisibility(View.VISIBLE);
            containerLayout.removeAllViews();
            if (cusFieldList != null && cusFieldList.size() != 0) {
                int size = cusFieldList.size();
                for (int i = 0; i < cusFieldList.size(); i++) {
                    final SobotFieldModel model = cusFieldList.get(i);
                    final SobotCusFieldConfig cusFieldConfig = model.getCusFieldConfig();
                    if (cusFieldConfig == null) {
                        continue;
                    }
                    View view = View.inflate(context, R.layout.sobot_post_msg_cusfield_list_item, null);
                    view.setTag(cusFieldConfig.getFieldId());
                    //多行
                    final EditText moreContent = (EditText) view.findViewById(R.id.work_order_customer_field_text_more_content);
                    displayInNotch(activity, moreContent);
                    //字段名
                    final TextView fieldName = (TextView) view.findViewById(R.id.work_order_customer_field_text_lable);
                    displayInNotch(activity, fieldName);
                    //选择
                    final TextView textClick = (TextView) view.findViewById(R.id.work_order_customer_date_text_click);
                    //多选的结果
                    final SobotAntoLineLayout sobot_ll_labels = view.findViewById(R.id.sobot_ll_labels);
                    //单行
                    final EditText singleContent = (EditText) view.findViewById(R.id.work_order_customer_field_text_single);
                    ImageView fieldImg = (ImageView) view.findViewById(R.id.work_order_customer_field_text_img);
                    displayInNotch(activity, singleContent);
                    displayInNotch(activity, moreContent);
                    displayInNotch(activity, textClick);

                    if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_SINGLE_LINE_TYPE == cusFieldConfig.getFieldType()) {
                        //单行文本
                        textClick.setVisibility(View.GONE);
                        fieldImg.setVisibility(View.GONE);
                        moreContent.setVisibility(View.GONE);
                        singleContent.setVisibility(View.VISIBLE);
                        if (1 == cusFieldConfig.getFillFlag()) {
                            fieldName.setText(Html.fromHtml("<font color='#f9676f'>*&nbsp;</font>" + cusFieldConfig.getFieldName()));
                        } else {
                            fieldName.setText(cusFieldConfig.getFieldName());
                        }

                        if (!StringUtils.isEmpty(cusFieldConfig.getLimitChar())) {
                            singleContent.setMaxLines(Integer.parseInt(cusFieldConfig.getLimitChar()));
                        }
                        singleContent.setSingleLine(true);
                        singleContent.setMaxEms(11);
                        singleContent.setInputType(EditorInfo.TYPE_CLASS_TEXT);
                        //限制方式  1禁止输入空格   2 禁止输入小数点  3 小数点后只允许2位  4 禁止输入特殊字符  5只允许输入数字 6最多允许输入字符  7判断邮箱格式  8判断手机格式
                        if (!StringUtils.isEmpty(cusFieldConfig.getLimitOptions())) {
                            if (cusFieldConfig.getLimitOptions().contains("6")) {
                                if (!StringUtils.isEmpty(cusFieldConfig.getLimitChar()))
                                    singleContent.setMaxLines(Integer.parseInt(cusFieldConfig.getLimitChar()));
                            }
                            if (cusFieldConfig.getLimitOptions().contains("5")) {
                                singleContent.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                            }
                            if (cusFieldConfig.getLimitOptions().contains("7")) {
                                singleContent.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                            }
                            if (cusFieldConfig.getLimitOptions().contains("8")) {
                                singleContent.setInputType(EditorInfo.TYPE_CLASS_PHONE);
                            }

                            singleContent.addTextChangedListener(new TextWatcher() {
                                private CharSequence temp;

                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                    temp = s;
                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    if (s.length() == 0)
                                        return;
                                    if (cusFieldConfig.getLimitOptions().contains("6")) {
                                        if (!StringUtils.isEmpty(cusFieldConfig.getLimitChar()) && temp.length() > Integer.parseInt(cusFieldConfig.getLimitChar())) {
                                            ToastUtil.showCustomToast(context, cusFieldConfig.getFieldName() + context.getResources().getString(R.string.sobot_only_can_write) + Integer.parseInt(cusFieldConfig.getLimitChar()) + context.getResources().getString(R.string.sobot_char_length));
                                            s.delete(temp.length() - 1, temp.length());
                                        }
                                    }
                                    if (cusFieldConfig.getLimitOptions().contains("4")) {
                                        String regex = "^[a-zA-Z0-9\u4E00-\u9FA5]+$";
                                        Pattern pattern = Pattern.compile(regex);
                                        Matcher match = pattern.matcher(s);
                                        boolean b = match.matches();
                                        if (!b) {
                                            ToastUtil.showCustomToast(context, cusFieldConfig.getFieldName() + context.getResources().getString(R.string.sobot_only_can_write) + context.getResources().getString(R.string.sobot_number_english_china));
                                            int ss = temp.length();
                                            s.delete(temp.length() - 1, temp.length());
                                        }
                                    }

                                }
                            });
                        }

                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_MORE_LINE_TYPE == cusFieldConfig.getFieldType()) {
                        //多行文本
                        textClick.setVisibility(View.GONE);
                        fieldImg.setVisibility(View.GONE);
                        moreContent.setVisibility(View.VISIBLE);
                        singleContent.setVisibility(View.GONE);
                        if (1 == cusFieldConfig.getFillFlag()) {
                            fieldName.setText(Html.fromHtml("<font color='#f9676f'>*&nbsp;</font>" + cusFieldConfig.getFieldName()));
                        } else {
                            fieldName.setText(cusFieldConfig.getFieldName());
                        }
                        moreContent.setInputType(EditorInfo.TYPE_CLASS_TEXT);
                        //设置EditText的显示方式为多行文本输入
                        moreContent.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                        //文本显示的位置在EditText的最上方
                        moreContent.setGravity(Gravity.TOP);
                        //改变默认的单行模式
                        moreContent.setSingleLine(false);
                        //水平滚动设置为False
                        moreContent.setHorizontallyScrolling(false);

                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_DATE_TYPE == cusFieldConfig.getFieldType()) {
                        //日期
                        textClick.setVisibility(View.VISIBLE);
                        fieldImg.setVisibility(View.VISIBLE);
                        fieldImg.setImageResource(R.drawable.sobot_cur_data);
                        singleContent.setVisibility(View.GONE);
                        moreContent.setVisibility(View.GONE);
                        fieldName.setText(cusFieldConfig.getFieldName());
                        if (1 == cusFieldConfig.getFillFlag()) {
                            fieldName.setText(Html.fromHtml("<font color='#f9676f'>*&nbsp;</font>" + cusFieldConfig.getFieldName()));
                        } else {
                            fieldName.setText(cusFieldConfig.getFieldName());
                        }

                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_TYPE == cusFieldConfig.getFieldType()) {
                        //时间
                        textClick.setVisibility(View.VISIBLE);
                        fieldImg.setVisibility(View.VISIBLE);
                        fieldImg.setImageResource(R.drawable.sobot_cur_time);
                        singleContent.setVisibility(View.GONE);
                        moreContent.setVisibility(View.GONE);
                        if (1 == cusFieldConfig.getFillFlag()) {
                            fieldName.setText(Html.fromHtml("<font color='#f9676f'>*&nbsp;</font>" + cusFieldConfig.getFieldName()));
                        } else {
                            fieldName.setText(cusFieldConfig.getFieldName());
                        }

                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_NUMBER_TYPE == cusFieldConfig.getFieldType()) {
                        //数值
                        textClick.setVisibility(View.GONE);
                        fieldImg.setVisibility(View.GONE);
                        singleContent.setVisibility(View.VISIBLE);
                        moreContent.setVisibility(View.GONE);
                        if (1 == cusFieldConfig.getFillFlag()) {
                            fieldName.setText(Html.fromHtml("<font color='#f9676f'>*&nbsp;</font>" + cusFieldConfig.getFieldName()));
                        } else {
                            fieldName.setText(cusFieldConfig.getFieldName());
                        }
                        singleContent.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                        //限制方式  1禁止输入空格   2 禁止输入小数点  3 小数点后只允许2位  4 禁止输入特殊字符  5只允许输入数字 6最多允许输入字符  7判断邮箱格式  8判断手机格式
                        if (!StringUtils.isEmpty(cusFieldConfig.getLimitOptions()) && "[3]".equals(cusFieldConfig.getLimitOptions())) {
                            singleContent.setInputType(InputType.TYPE_CLASS_NUMBER | 8194);
                            singleContent.addTextChangedListener(new TextWatcher() {

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before,
                                                          int count) {
                                    if (s.toString().contains(".")) {
                                        if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                                            s = s.toString().subSequence(0,
                                                    s.toString().indexOf(".") + 3);
                                            singleContent.setText(s);
                                            singleContent.setSelection(s.length());
                                        }
                                    }
                                    if (s.toString().trim().substring(0).equals(".")) {
                                        s = "0" + s;
                                        singleContent.setText(s);
                                        singleContent.setSelection(2);
                                    }

                                    if (s.toString().startsWith("0")
                                            && s.toString().trim().length() > 1) {
                                        if (!s.toString().substring(1, 2).equals(".")) {
                                            singleContent.setText(s.subSequence(0, 1));
                                            singleContent.setSelection(1);
                                            return;
                                        }
                                    }
                                }

                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count,
                                                              int after) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    // TODO Auto-generated method stub

                                }

                            });

                        } else {
                            singleContent.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                        }

                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_SPINNER_TYPE == cusFieldConfig.getFieldType() || ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_RADIO_TYPE == cusFieldConfig.getFieldType()) {
                        //下拉列表和单选框
                        textClick.setVisibility(View.VISIBLE);
                        fieldImg.setVisibility(View.VISIBLE);
                        singleContent.setVisibility(View.GONE);
                        moreContent.setVisibility(View.GONE);
                        if (1 == cusFieldConfig.getFillFlag()) {
                            fieldName.setText(Html.fromHtml("<font color='#f9676f'>*&nbsp;</font>" + cusFieldConfig.getFieldName()));
                        } else {
                            fieldName.setText(cusFieldConfig.getFieldName());
                        }

                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CHECKBOX_TYPE == cusFieldConfig.getFieldType()) {
                        //复选框
                        textClick.setVisibility(View.VISIBLE);
                        fieldImg.setVisibility(View.VISIBLE);
                        singleContent.setVisibility(View.GONE);
                        moreContent.setVisibility(View.GONE);
                        if (1 == cusFieldConfig.getFillFlag()) {
                            fieldName.setText(Html.fromHtml("<font color='#f9676f'>*&nbsp;</font>" + cusFieldConfig.getFieldName()));
                        } else {
                            fieldName.setText(cusFieldConfig.getFieldName());
                        }
                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CASCADE_TYPE == cusFieldConfig.getFieldType()) {
                        //级联
                        textClick.setVisibility(View.VISIBLE);
                        fieldImg.setVisibility(View.VISIBLE);
                        moreContent.setVisibility(View.GONE);
                        singleContent.setVisibility(View.GONE);
                        if (1 == cusFieldConfig.getFillFlag()) {
                            fieldName.setText(Html.fromHtml("<font color='#f9676f'>*&nbsp;</font>" + cusFieldConfig.getFieldName()));
                        } else {
                            fieldName.setText(cusFieldConfig.getFieldName());
                        }
                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_REGION_TYPE == cusFieldConfig.getFieldType()) {
                        //地区级联
                        textClick.setVisibility(View.VISIBLE);
                        fieldImg.setVisibility(View.VISIBLE);
                        singleContent.setVisibility(View.GONE);
                        moreContent.setVisibility(View.GONE);
                        fieldName.setText(cusFieldConfig.getFieldName());
                        if (1 == cusFieldConfig.getFillFlag()) {
                            fieldName.setText(Html.fromHtml("<font color='#f9676f'>*&nbsp;</font>" + cusFieldConfig.getFieldName()));
                        }
                        //赋值
                        if (!TextUtils.isEmpty(cusFieldConfig.getText())) {
                            textClick.setText(cusFieldConfig.getText());
                            textClick.setTag(cusFieldConfig.getValue());
                        }
                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_ZONE == cusFieldConfig.getFieldType()) {
                        //时区
                        textClick.setVisibility(View.VISIBLE);
                        fieldImg.setVisibility(View.VISIBLE);
                        singleContent.setVisibility(View.GONE);
                        moreContent.setVisibility(View.GONE);
//                        textClick.setHint(R.string.sobot_wo_select_hint);
                        //是否必填
                        if (1 == cusFieldConfig.getFillFlag()) {
                            fieldName.setText(Html.fromHtml("<font color='#f9676f'>*&nbsp;</font>" + cusFieldConfig.getFieldName()));
                        } else {
                            fieldName.setText(cusFieldConfig.getFieldName());
                        }
                        //赋值
                        if (!TextUtils.isEmpty(cusFieldConfig.getText())) {
                            textClick.setText(cusFieldConfig.getText());
                            textClick.setTag(cusFieldConfig.getValue());
                        }

                    }

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_MORE_LINE_TYPE == cusFieldConfig.getFieldType()) {//多行文本
                                moreContent.setVisibility(View.VISIBLE);

                                moreContent.setFocusableInTouchMode(true);
                                moreContent.setFocusable(true);
                                moreContent.requestFocus();
                            }

                            if (cusFieldInterface != null) {
                                cusFieldInterface.onClickCusField(v, cusFieldConfig, model);
                            }
                        }
                    });
                    containerLayout.addView(view);
                }
            }
        }
    }
}
