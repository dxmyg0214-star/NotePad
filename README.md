                     实验报告
课程   移动软件应用开发     实验名称 NotePad记事本应用 
专业____软件工程________  班级___2班 ______  学号__121052023072_____  姓名  李承焕

实验日期：  2025  年  12 月  4日

一、实验目的
基于NotePad应用实现功能扩展

二、实验内容
• 基本要求：
• NoteList界面中笔记条目增加时间戳显示
• 添加笔记查询功能（根据标题或内容查询）
• 附加功能：根据自身实际情况进行扩充（至少两项），以下是建议的扩展功能
• UI美化（如主题设定，更改记事本的背景，优化编辑器等）
• 导出笔记，笔记的云备份和恢复
• 添加代办功能
• 记事本的偏好设置功能
• 笔记分类
• 支持多类型笔记，如保存图片、语音、视频等（参考印象笔记）
• 语音搜索？
• 笔记便签
• OCR扫描
三、实验环境
Android studio
四、实验步骤
1：根据GitHub网址fork并clone代码，进行运行。

2：根据实验完成搜索功能以及时间戳功能：
 （1）时间戳功能：
     代码内容：数据库设计
在 NotePadProvider.java 的 DatabaseHelper.onCreate 方法中定义了数据库表结构，包含两个时间戳列：

![img.png](app/src/main/res/drawable/img.png)

## 时间戳设置逻辑
### 创建时间戳
在 NotePadProvider.insert 方法中，当插入新笔记时自动设置创建时间和修改时间：

![img_1.png](app/src/main/res/drawable/img_1.png)

### 修改时间戳
在 NoteEditor.updateNote 方法中，当更新笔记时手动更新修改时间：

![img_2.png](app/src/main/res/drawable/img_2.png)

## 时间戳显示控制（新增）
### 时间戳显示逻辑
在 NotesList.java 中，使用 ViewBinder 来格式化和显示时间戳：

![img_3.png](app/src/main/res/drawable/img_3.png)

## 时间戳配置
时间戳功能的默认排序和列名定义在 NotePad.java 中：

![img_4.png](app/src/main/res/drawable/img_4.png)

xml：

![img_5.png](app/src/main/res/drawable/img_5.png)

实现效果：

![img_6.png](app/src/main/res/drawable/img_6.png)


（2）笔记查询功能（根据标题或内容查询）：
## . 搜索视图创建（新增）
在 NotesList.java 的 onCreateOptionsMenu 方法中，创建了搜索视图并添加到菜单中：

![img_7.png](app/src/main/res/drawable/img_7.png)

## 搜索事件处理（新增）
设置了 OnQueryTextListener 来处理搜索提交和文本变化事件：

![img_8.png](app/src/main/res/drawable/img_8.png)

## . 查询逻辑实现（新增）
在 refreshNotesList 方法中，根据搜索关键词构建查询条件：

![img_9.png](app/src/main/res/drawable/img_9.png)

## 数据获取与展示（新增）
使用 managedQuery 方法从ContentProvider获取数据，并使用 SimpleCursorAdapter 展示：

![img_10.png](app/src/main/res/drawable/img_10.png)

##  数据提供层支持
NotePadProvider.java 中的 query 方法处理来自ContentResolver的查询请求，支持动态的selection和selectionArgs：

![img_11.png](app/src/main/res/drawable/img_11.png)

实现效果：

![img_12.png](app/src/main/res/drawable/img_12.png)

![img_13.png](app/src/main/res/drawable/img_13.png)

## 实现特点
1. 模糊搜索 ：使用SQL LIKE 运算符和通配符 % 实现模糊匹配
2. 多字段搜索 ：同时支持根据标题（ COLUMN_NAME_TITLE ）和内容（ COLUMN_NAME_NOTE ）进行搜索
3. 实时搜索 ：在用户输入时实时更新搜索结果
4. 内容提供者架构 ：通过ContentProvider实现数据访问的封装和统一


（3）记事本的偏好设置功能（新增）：
## 偏好设置的存储机制
### 核心实现文件：SettingsActivity.java 和 NotesList.java:

![img_14.png](app/src/main/res/drawable/img_14.png)

![img_15.png](app/src/main/res/drawable/img_15.png)

## 偏好设置界面实现
### 核心实现文件：settings_activity.xml 和 SettingsActivity.java:

![img_16.png](app/src/main/res/drawable/img_16.png)

## 设置项的实现细节
### 1. 背景色设置
核心实现代码 ：

![img_17.png](app/src/main/res/drawable/img_17.png)

![img_18.png](app/src/main/res/drawable/img_18.png)

### 字体大小设置
核心实现代码 ：

![img_19.png](app/src/main/res/drawable/img_19.png)


###  时间戳显示设置
核心实现代码 ：

![img_20.png](app/src/main/res/drawable/img_20.png)

## 偏好设置的应用
### 1. 在NotesList中应用设置
核心实现代码 ：

![img_21.png](app/src/main/res/drawable/img_21.png)


### 设置变更的通知机制
核心实现代码 ：
// 在SettingsActivity中，当设置变更时
setResult(RESULT_OK, new Intent().putExtra("refresh", true));

// 在NotesList中，处理返回结果
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
super.onActivityResult(requestCode, resultCode, data);
// 刷新列表（应用新设置）
if (resultCode == RESULT_OK && data != null && data.getBooleanExtra("refresh", false)) {
refreshNotesList();
}
}

实现效果：

![img_22.png](app/src/main/res/drawable/img_22.png)

![img_23.png](app/src/main/res/drawable/img_23.png)

![img_24.png](app/src/main/res/drawable/img_24.png)

![img_25.png](app/src/main/res/drawable/img_25.png)

![img_26.png](app/src/main/res/drawable/img_26.png)

![img_27.png](app/src/main/res/drawable/img_27.png)

## 实现特点
1.背景色设置：支持12种预设颜色选择
2.字体大小设置：支持4种预设字体大小（14sp、16sp、18sp、20sp）
3.时间戳显示设置：支持开关控制
4.设置持久化：使用SharedPreferences确保设置在应用重启后仍然有效
5.实时更新：设置变更后立即应用并更新界面

（4）ui美化（新增）：
## 自定义控件实现
### LinedEditText (带行线的编辑框)
- 功能 ：在文本编辑区域绘制蓝色行线，模拟纸张效果
- 关键代码 ：
  public static class LinedEditText extends EditText {
  private Rect mRect;
  private Paint mPaint;

  public LinedEditText(Context context, AttributeSet attrs) {
  super(context, attrs);
  mRect = new Rect();
  mPaint = new Paint();
  mPaint.setStyle(Paint.Style.STROKE);
  mPaint.setColor(0x800000FF); // 蓝色行线
  }

  @Override
  protected void onDraw(Canvas canvas) {
  int count = getLineCount();
  Rect r = mRect;
  Paint paint = mPaint;

        for (int i = 0; i < count; i++) {
            int baseline = getLineBounds(i, r);
            canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
        }
        super.onDraw(canvas);
  }
  }

## 背景样式设计
### 列表项背景 (list_item_bg.xml)
功能 ：为列表项提供圆角背景和状态变化效果

![img_28.png](app/src/main/res/drawable/img_28.png)

### 编辑器背景 (editor_bg.xml)
功能 ：为编辑区域提供浅灰色圆角背景和边框

![img_29.png](app/src/main/res/drawable/img_29.png)


### 按钮背景 (button_ok_bg.xml)
功能 ：为按钮提供蓝色圆角背景和按压效果

![img_30.png](app/src/main/res/drawable/img_30.png)

## 用户偏好设置的UI应用
### 背景颜色设置
功能 ：根据用户设置动态改变应用背景色

// 初始化偏好设置+应用背景色
sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
int bgColor = sp.getInt(KEY_BG_COLOR, DEFAULT_BG_COLOR);
getListView().setBackgroundColor(bgColor);
getWindow().getDecorView().setBackgroundColor(bgColor);

### 字体大小设置
功能 ：根据用户设置动态改变字体大小

// 处理标题字体大小
if (columnIndex == 1) {
int fontSize = sp.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE);
((TextView) view).setTextSize(fontSize);
((TextView) view).setText(cursor.getString(columnIndex));
return true;
}

## 主题样式设计
### 全局应用主题 (styles.xml)
功能 ：定义应用的全局主题样式

![img_31.png](app/src/main/res/drawable/img_31.png)

实现效果：

![img_32.png](app/src/main/res/drawable/img_32.png)


## 实现特点
1.自定义控件 ：实现了带行线的编辑框，提升编辑体验
2.背景样式 ：使用XML Drawable定义了圆角、状态变化等视觉效果
3.布局设计 ：优化了列表项和编辑界面的布局结构
3.用户偏好 ：支持背景色、字体大小和时间戳显示的自定义
4.主题样式 ：定义了全局应用主题和自定义ActionBar样式
5.交互体验 ：使用PopupMenu替代传统菜单，优化列表项间距

五、总结
### 功能实现总结
本实验实现的NotePad应用通过四大核心功能的实现，打造了一个功能完善、交互友好的记事本工具。应用充分利用Android平台特性，如ContentProvider数据管理、SharedPreferences偏好设置、自定义控件等技术，展现了规范的Android应用开发实践。
时间戳功能通过展示笔记最后修改时间，增强了笔记的可追溯性和管理效率；笔记查询功能实现了基于标题和内容的实时搜索，让用户能快速定位所需笔记；偏好设置功能提供了背景色、字体大小和时间戳显示的个性化配置，满足不同用户的使用习惯；UI美化则通过自定义控件、精心设计的背景样式和优化的布局，显著提升了应用的视觉体验和交互质感。
这些功能的有机结合，不仅满足了记事本应用的基本需求，更通过细节优化和用户体验设计，使应用具备了更高的实用性和易用性。

### 学习收获
通过本次NotePad应用的开发实验，我获得了以下几方面的学习收获：

1. Android技术栈的深入应用 ：系统掌握了ContentProvider进行数据管理、SharedPreferences实现偏好设置、自定义控件开发等核心Android开发技术，理解了各组件间的协同工作机制。
2. UI/UX设计理念的实践 ：学习了如何通过合理的布局设计、色彩搭配和交互反馈提升用户体验，认识到细节处理（如PopupMenu位置优化、点击效果反馈）对应用品质的重要性。
3. 问题解决与调试能力 ：在开发过程中，通过分析日志、调试代码解决了数据查询、UI渲染等问题，提升了排查和解决实际开发中遇到问题的能力。
4. 用户需求分析与实现能力 ：学会了如何将用户需求转化为具体的功能实现，理解了功能设计需要以用户体验为中心的重要原则。
本次实验不仅强化了我的Android开发技能，也让我对移动应用开发的整个流程和设计理念有了更深刻的理解，为未来开发更复杂的应用打下了坚实基础。
