package com.example.android.notepad;
import com.example.android.notepad.NotePad;
import android.app.ListActivity;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleCursorAdapter;
import android.widget.SearchView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NotesList extends ListActivity {

    // For logging and debugging
    private static final String TAG = "NotesList";

    /**
     * The columns needed by the cursor adapter
     */
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
    };

    // 新增：包含时间戳的查询投影
    private static final String[] PROJECTION_WITH_TIME = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE // 2：修改时间列
    };

    /** The index of the title column */
    private static final int COLUMN_INDEX_TITLE = 1;

    // 新增：搜索关键词存储
    private String mSearchQuery = "";

    // 新增：偏好设置相关常量
    private static final String PREF_NAME = "NotePadPrefs";
    private static final String KEY_BG_COLOR = "bg_color";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_SHOW_TIMESTAMP = "show_timestamp";
    private static final int DEFAULT_BG_COLOR = Color.WHITE;
    private static final int DEFAULT_FONT_SIZE = 16;
    private static final boolean DEFAULT_SHOW_TIMESTAMP = true;
    private SharedPreferences sp;
    // 新增：时间戳格式化
    private SimpleCursorAdapter.ViewBinder mTimeStampBinder = new SimpleCursorAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            // 处理标题字体大小
            if (columnIndex == 1) {
                int fontSize = sp.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE);
                ((TextView) view).setTextSize(fontSize);
                ((TextView) view).setText(cursor.getString(columnIndex));
                return true;
            }
            // 处理时间戳列（索引2）
            else if (columnIndex == 2) {
                TextView tv = (TextView) view;
                boolean showTimestamp = sp.getBoolean(KEY_SHOW_TIMESTAMP, DEFAULT_SHOW_TIMESTAMP);
                if (showTimestamp) {
                    long timeMillis = cursor.getLong(columnIndex);
                    SimpleDateFormat sdf = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                    );
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                    String formattedTime = sdf.format(new Date(timeMillis));
                    tv.setTextSize(sp.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE) - 2);
                    tv.setText(formattedTime);
                    tv.setVisibility(View.VISIBLE);
                } else {
                    tv.setText(""); // 清空文字，避免缓存
                    tv.setVisibility(View.GONE); // 彻底隐藏
                }
                return true;
            }
            return false;
        }
    };
    /**
     * onCreate is called when Android starts this Activity from scratch.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 新增：初始化偏好设置+应用背景色
        sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int bgColor = sp.getInt(KEY_BG_COLOR, DEFAULT_BG_COLOR);
        getListView().setBackgroundColor(bgColor);
        getWindow().getDecorView().setBackgroundColor(bgColor);


        // The user does not need to hold down the key to use menu shortcuts.
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        /* If no data is given in the Intent that started this Activity, then this Activity
         * was started when the intent filter matched a MAIN action. We should use the default
         * provider URI.
         */
        // Gets the intent that started this Activity.
        Intent intent = getIntent();

        // If there is no data associated with the Intent, sets the data to the default URI, which
        // accesses a list of notes.
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }

        // 强制菜单显示在笔记下方
        ListView listView = getListView();
        listView.setClipToPadding(false); // 兼容边界显示
        listView.setPadding(0, 10, 0, 10); // 轻微留白，优化显示
        // 绑定长按事件，替代原有ContextMenu
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showPopupMenu(view, id);
                return true;
            }
        });

        // 新增：初始化笔记列表
        refreshNotesList();
    }

    // 强制菜单在笔记下方
    private void showPopupMenu(View anchorView, long noteId) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.list_context_menu, popupMenu.getMenu());
        popupMenu.setGravity(Gravity.BOTTOM | Gravity.START);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), noteId);
                int id = item.getItemId();
                if (id == R.id.context_open) {
                    // 打开笔记
                    startActivity(new Intent(Intent.ACTION_EDIT, noteUri));
                    return true;
                } else if (id == R.id.context_copy) {
                    // 复制笔记
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(ClipData.newUri(
                            getContentResolver(),
                            "Note",
                            noteUri
                    ));
                    return true;
                } else if (id == R.id.context_delete) {
                    getContentResolver().delete(noteUri, null, null);
                    refreshNotesList();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    // 新增：刷新笔记列表（支持搜索过滤和时间戳显示）
    private void refreshNotesList() {
        String selection = null;
        String[] selectionArgs = null;
        if (!TextUtils.isEmpty(mSearchQuery)) {
            selection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " +
                    NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ?";
            selectionArgs = new String[]{
                    "%" + mSearchQuery + "%",
                    "%" + mSearchQuery + "%"
            };
        }
        // 查询数据
        Cursor cursor = managedQuery(
                getIntent().getData(),
                PROJECTION_WITH_TIME,
                selection,
                selectionArgs,
                NotePad.Notes.DEFAULT_SORT_ORDER
        );
        // 初始化
        String[] dataColumns = {
                NotePad.Notes.COLUMN_NAME_TITLE,
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE
        };
        int[] viewIDs = {
                android.R.id.text1,
                R.id.note_time
        };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.noteslist_item,
                cursor,
                dataColumns,
                viewIDs
        );
        // 给所有适配器设置时间戳格式化Binder
        adapter.setViewBinder(mTimeStampBinder);
        setListAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_options_menu, menu);
        // 新增：添加搜索菜单项
        MenuItem searchItem = menu.add(Menu.NONE, 1001, Menu.NONE, R.string.menu_search);
        searchItem.setIcon(android.R.drawable.ic_menu_search);
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        //新增：添加设置菜单项
        MenuItem settingsItem = menu.add(Menu.NONE, 1002, Menu.NONE, R.string.menu_settings);
        settingsItem.setIcon(android.R.drawable.ic_menu_preferences);
        settingsItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        // 绑定SearchView
        SearchView searchView = new SearchView(this);
        searchView.setQueryHint(getString(R.string.menu_search));
        searchItem.setActionView(searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchQuery = query.trim();
                refreshNotesList();
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                mSearchQuery = newText.trim();
                refreshNotesList();
                return true;
            }
        });
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        MenuItem mPasteItem = menu.findItem(R.id.menu_paste);
        if (clipboard.hasPrimaryClip()) {
            mPasteItem.setEnabled(true);
        } else {
            // If the clipboard is empty, disables the menu's Paste option.
            mPasteItem.setEnabled(false);
        }

        // Gets the number of notes currently being displayed.
        final boolean haveItems = getListAdapter().getCount() > 0;

        if (haveItems) {

            // This is the selected item.
            Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());

            // Creates an array of Intents with one element. This will be used to send an Intent
            // based on the selected menu item.
            Intent[] specifics = new Intent[1];

            // Sets the Intent in the array to be an EDIT action on the URI of the selected note.
            specifics[0] = new Intent(Intent.ACTION_EDIT, uri);

            // Creates an array of menu items with one element. This will contain the EDIT option.
            MenuItem[] items = new MenuItem[1];

            // Creates an Intent with no specific action, using the URI of the selected note.
            Intent intent = new Intent(null, uri);

            /* Adds the category ALTERNATIVE to the Intent, with the note ID URI as its
             * data. This prepares the Intent as a place to group alternative options in the
             * menu.
             */
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);

            /*
             * Add alternatives to the menu
             */
            menu.addIntentOptions(
                    Menu.CATEGORY_ALTERNATIVE,  // Add the Intents as options in the alternatives group.
                    Menu.NONE,                  // A unique item ID is not required.
                    Menu.NONE,                  // The alternatives don't need to be in order.
                    null,                       // The caller's name is not excluded from the group.
                    specifics,                  // These specific options must appear first.
                    intent,                     // These Intent objects map to the options in specifics.
                    Menu.NONE,                  // No flags are required.
                    items                       // The menu items generated from the specifics-to-
                    // Intents mapping
            );
            // If the Edit menu item exists, adds shortcuts for it.
            if (items[0] != null) {

                // Sets the Edit menu item shortcut to numeric "1", letter "e"
                items[0].setShortcut('1', 'e');
            }
        } else {
            // If the list is empty, removes any existing alternative actions from the menu
            menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
        }

        // Displays the menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 启动设置页并监听返回结果
        if (item.getItemId() == 1002) {
            startActivityForResult(new Intent(this, SettingsActivity.class), 100);
            return true;
        }
        else if (item.getItemId() == R.id.menu_add) {
            startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
            return true;
        } else if (item.getItemId() == R.id.menu_paste) {
            startActivity(new Intent(Intent.ACTION_PASTE, getIntent().getData()));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //新增：接收设置页返回结果，强制刷新
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            refreshNotesList();
            getListView().invalidateViews(); // 强制刷新列表视图，避免缓存
        }
    }
    // 回到列表页强制刷新设置
    @Override
    protected void onResume() {
        super.onResume();
        int bgColor = sp.getInt(KEY_BG_COLOR, DEFAULT_BG_COLOR);
        getListView().setBackgroundColor(bgColor);
        getWindow().getDecorView().setBackgroundColor(bgColor);
        refreshNotesList();
        getListView().invalidateViews(); // 新增：强制刷新视图，确保时间戳状态生效
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // Constructs a new URI from the incoming URI and the row ID
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

        // Gets the action from the incoming Intent
        String action = getIntent().getAction();

        // Handles requests for note data
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            // Sets the result to return to the component that called this Activity. The
            // result contains the new URI
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {
            // Sends out an Intent to start an Activity that can handle ACTION_EDIT. The
            // Intent's data is the note ID URI. The effect is to call NoteEdit.
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }
}