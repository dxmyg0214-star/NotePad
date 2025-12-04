package com.example.android.notepad;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends Activity {

    // 偏好设置Key
    private static final String PREF_NAME = "NotePadPrefs";
    private static final String KEY_BG_COLOR = "bg_color";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_SHOW_TIMESTAMP = "show_timestamp";


    private static final int DEFAULT_BG_COLOR = Color.WHITE;
    private static final int DEFAULT_FONT_SIZE = 16;
    private static final boolean DEFAULT_SHOW_TIMESTAMP = true;

    // 可选背景色
    private final List<Integer> colorOptions = Arrays.asList(
            Color.WHITE, Color.parseColor("#F5F5F5"), Color.parseColor("#E8E8E8"),
            Color.parseColor("#FFF8E1"), Color.parseColor("#E1F5FE"), Color.parseColor("#E8F5E8"),
            Color.parseColor("#FCE4EC"), Color.parseColor("#F3E5F5"), Color.parseColor("#FFEBEE"),
            Color.parseColor("#FFF3E0"), Color.parseColor("#E0F2F1"), Color.parseColor("#F1F8E9")
    );

    // 可选字体大小
    private final List<Integer> fontSizeOptions = Arrays.asList(14, 16, 18, 20);
    private List<String> fontSizeLabels;
    private SharedPreferences sp;
    private View currentBgColorView;
    private TextView currentFontSizeView;
    private Switch switchShowTimestamp;
    // 新增：绑定整行布局
    private View settingShowTimestampRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        // 初始化字体大小标签
        fontSizeLabels = Arrays.asList(
                getString(R.string.font_size_small),
                getString(R.string.font_size_medium),
                getString(R.string.font_size_large),
                getString(R.string.font_size_xlarge)
        );

        // 初始化SharedPreferences
        sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // 绑定控件
        currentBgColorView = findViewById(R.id.current_bg_color);
        currentFontSizeView = (TextView) findViewById(R.id.current_font_size); // 强转TextView
        switchShowTimestamp = (Switch) findViewById(R.id.switch_show_timestamp); // 强转Switch
        // 新增：绑定时间戳整行布局
        settingShowTimestampRow = findViewById(R.id.setting_show_timestamp);
        // 加载当前设置
        loadCurrentSettings();
        // 背景色设置点击事件
        findViewById(R.id.setting_bg_color).setOnClickListener(v -> showColorPickerDialog());
        // 字体大小设置点击事件
        findViewById(R.id.setting_font_size).setOnClickListener(v -> showFontSizeDialog());

        // 新增：整行点击事件
        settingShowTimestampRow.setOnClickListener(v -> {
            // 取反当前Switch状态
            boolean newState = !switchShowTimestamp.isChecked();
            switchShowTimestamp.setChecked(newState);
        });

        // 时间戳开关事件 —— 核心修复：确保状态保存+主动返回刷新
        switchShowTimestamp.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 同步保存状态
            sp.edit().putBoolean(KEY_SHOW_TIMESTAMP, isChecked).commit();
            Toast.makeText(this, "时间戳" + (isChecked ? "已显示" : "已隐藏"), Toast.LENGTH_SHORT).show();
            // 主动通知列表页刷新
            setResult(RESULT_OK, new Intent().putExtra("refresh", true));
        });
    }

    // 加载当前设置
    private void loadCurrentSettings() {
        // 背景色
        int bgColor = sp.getInt(KEY_BG_COLOR, DEFAULT_BG_COLOR);
        currentBgColorView.setBackgroundResource(R.drawable.bg_color_preview); // 先设边框shape
        currentBgColorView.setBackgroundColor(bgColor); // 再覆盖选中的背景色
        // 字体大小
        int fontSize = sp.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE);
        currentFontSizeView.setText(fontSize + "sp");
        // 时间戳开关
        boolean showTimestamp = sp.getBoolean(KEY_SHOW_TIMESTAMP, DEFAULT_SHOW_TIMESTAMP);
        switchShowTimestamp.setChecked(showTimestamp);
    }

    // 显示颜色选择弹窗
    private void showColorPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_bg_color);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_color_picker, null);
        GridView colorGrid = (GridView) dialogView.findViewById(R.id.color_grid);
        colorGrid.setAdapter(new ColorGridAdapter());

        // 颜色选择事件
        colorGrid.setOnItemClickListener((parent, view, position, id) -> {
            int selectedColor = colorOptions.get(position);
            // 保存设置（commit立即生效）
            sp.edit().putInt(KEY_BG_COLOR, selectedColor).commit();
            currentBgColorView.setBackgroundResource(R.drawable.bg_color_preview);
            currentBgColorView.setBackgroundColor(selectedColor);
            Toast.makeText(this, "背景色已设置", Toast.LENGTH_SHORT).show();
            ((AlertDialog) parent.getTag()).dismiss();
            setResult(RESULT_OK, new Intent().putExtra("refresh", true));
        });
        AlertDialog dialog = builder.setView(dialogView).create();
        colorGrid.setTag(dialog);
        dialog.show();
    }

    // 显示字体大小选择弹窗
    private void showFontSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_font_size);
        builder.setItems(fontSizeLabels.toArray(new String[0]), (dialog, which) -> {
            int selectedSize = fontSizeOptions.get(which);
            // 保存设置（commit立即生效）
            sp.edit().putInt(KEY_FONT_SIZE, selectedSize).commit();
            currentFontSizeView.setText(selectedSize + "sp");
            Toast.makeText(this, "字体大小已设置", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK, new Intent().putExtra("refresh", true));
        });

        builder.show();
    }

    // 颜色网格适配器
    private class ColorGridAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return colorOptions.size();
        }
        @Override
        public Object getItem(int position) {
            return colorOptions.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View colorView = new View(SettingsActivity.this);
            colorView.setLayoutParams(new GridView.LayoutParams(60, 60));
            colorView.setBackgroundColor(colorOptions.get(position));
            colorView.setPadding(2, 2, 2, 2);
            colorView.setBackgroundResource(android.R.drawable.edit_text);
            colorView.setBackgroundColor(colorOptions.get(position));
            return colorView;
        }
    }
}