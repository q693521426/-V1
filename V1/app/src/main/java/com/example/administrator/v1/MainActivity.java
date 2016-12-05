package com.example.administrator.v1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.Space;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.administrator.v1.book_content.loadData;
import static com.example.administrator.v1.book_information.bitmap;
import static com.example.administrator.v1.book_information.getBitmapFromUrl;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public final static String EXTRA_MESSAGE = "com.example.administrator.MESSAGE";
    public final static int VISITOR=0;
    public final static int USER=1;
    public final static int LOGIN_REQUEST=1;
    public int status=USER;
    public static final class ViewHolder{
        public ImageView img;
        public TextView title;
        public TextView author;
        public TextView category;
        public TextView latest;
        public TextView latestChapter;
    }

    private static ArrayList<ListView> BookListView;

    public String SearchHtml="http://zhannei.baidu.com/cse/search?s=287293036948159515&q=";
    public String[] BookAttr={"category","title","author","latest","latestChapter"};
    public String[] categoryText={ "都市言情","女生频道","东方玄幻","玄幻奇幻",
            "现代言情","科幻灵异","奇幻修真","穿越时空",
            "武侠仙侠","架空历史"," 古代言情","网游竞技",
            "历史军事","校园言情"," 经典美文","小说同人",
            "玄幻小说","都市小说"};
    public ArrayList<String>  book_info;
    public static List<Map<String, Object>> mData;
    ArrayList<String> NovelUrl,NovelTitle;
    String[] wellChosenNovel={"http://www.biquge.com/0_111/","http://www.biquge.com/0_174/"},
            wellChosenNovelTitle={"将夜","雪中悍刀行"};
    public static HashMap<String,Boolean>ShelfBook=new HashMap<String,Boolean>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        updateDrawer();
        updateBookListView();
        updateTab();
        updateCategory();
        updateSearchView();
    }

    private void updateDrawer(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "你有2本小说更新", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.setVisibility(View.GONE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void updateCategory(){
        GridView cateGrid=(GridView)findViewById(R.id.cateGridView);
        ArrayList<HashMap<String,Object>> cateList=new ArrayList<HashMap<String, Object>>();
        for(int i=0;i<categoryText.length;++i){
            HashMap<String,Object> map=new HashMap<String,Object>();
            map.put("categoryText",categoryText[i]);
            cateList.add(map);
        }
        SimpleAdapter cateAdapter=new SimpleAdapter(this,cateList,R.layout.func_adapter,new String[]{"categoryText"},new int[]{R.id.FuncText});
        cateGrid.setAdapter(cateAdapter);
        cateGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchTask searchTask=new SearchTask();
                searchTask.execute(categoryText[position]);
            }
        });
    }

    private void updateBookListView(){
        BookListView=new ArrayList<ListView>()
        {{
            add((ListView)findViewById(R.id.PersonBookList));
            add((ListView)findViewById(R.id.FilterBookList));
            add((ListView)findViewById(R.id.ClassifyBookList));
            add((ListView)findViewById(R.id.SearchList));
        }};

        for(int i=1;i<3;++i){
            mData=getData(i);
            BookAdapter adapter = new BookAdapter(BookListView.get(i).getContext(),mData,true);
            BookListView.get(i).setAdapter(adapter);
        }

       updateShelf();

        for(int i=0;i<BookListView.size();++i){
            final int index=i;
            BookListView.get(i).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final Integer tem=position;
                    Intent intent;
                    if(index!=0)intent = new Intent(parent.getContext(),book_information.class);
                    else intent=new Intent(parent.getContext(),book_content.class);
                    ArrayList<String> Novel;
                    switch(index) {
                        case 0: {
                            String ChapterIndex=(String)mData.get(position).get("ChapterIndex");
                            String pageIndex=(String)mData.get(position).get("pageIndex");
                            String ChapterName=(String)mData.get(position).get("ChapterName");
                            String ChapterUrl=(String)mData.get(position).get("ChapterUrl");
                            book_content.NovelUrl="http://www.biquge.com"+(String)mData.get(position).get("NovelUrl");
                            String[] data={ChapterUrl,ChapterIndex,pageIndex,ChapterName};
                            intent.putExtra("position",data);
                            startActivity(intent);
                        }
                            break;
                        case 1: {
                            Novel = new ArrayList<String>() {{
                                add(wellChosenNovel[tem]);
                                add(wellChosenNovelTitle[tem]);
                            }};
                            intent.putExtra("Novel", Novel);
                            startActivity(intent);
                        }
                            break;
                        case 3: {
                            Novel = new ArrayList<String>() {{
                                add(NovelUrl.get(tem));
                                add(NovelTitle.get(tem));
                            }};
                            intent.putExtra("Novel", Novel);
                            startActivity(intent);
                        }
                            break;
                        default: {
                            Novel = new ArrayList<String>() {{
                                add(wellChosenNovel[tem]);
                                add(wellChosenNovelTitle[tem]);
                            }};
                            intent.putExtra("Novel", Novel);
                            startActivity(intent);
                        }
                            break;
                    }
                }
            });
        }
    }
    static void updateShelf(){
        ListView ShelfList=BookListView.get(0);
        mData=getData(0);
        BookAdapter adapter = new BookAdapter(ShelfList.getContext(),mData,false);
        ShelfList.setAdapter(adapter);
    }
    private void updateTab(){
        TabHost th= (TabHost)findViewById(R.id.tabHost);
        //设置Tab1
        th.setup();
        th.addTab(th.newTabSpec("tab1").setIndicator("书架",null).setContent(R.id.tab1));
        th.addTab(th.newTabSpec("tab2").setIndicator("精选",null).setContent(R.id.tab2));
        th.addTab(th.newTabSpec("tab3").setIndicator("分类",null).setContent(R.id.tab3));
        th.addTab(th.newTabSpec("tab4").setIndicator("搜索",null).setContent(R.id.tab4));
        th.setCurrentTab(3);
        th.getCurrentTabView().setVisibility(View.GONE);
        updateTabHost(status);
    }

    private void updateSearchView(){
        SearchView searchView=(SearchView)findViewById(R.id.SearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                SearchTask searchTask=new SearchTask();
                searchTask.execute(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login) {
            Intent login_intent = new Intent(this, LoginActivity.class);
            startActivityForResult(login_intent, LOGIN_REQUEST);
        } else if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_logout) {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("确定注销")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(status==USER) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("提示")
                                        .setMessage("注销成功")
                                        .setPositiveButton("确定",null)
                                        .show();
                                TextView userView = (TextView) findViewById(R.id.user);
                                userView.setText(String.valueOf("当前状态：游客"));
                                updateTabHost(status);
                            }else {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("提示")
                                        .setMessage("注销失败")
                                        .setPositiveButton("确定",null)
                                        .show();
                            }
                        }
                    })
                    .setNegativeButton("取消",null)
                    .show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == LOGIN_REQUEST) {
            if (requestCode == LOGIN_REQUEST) {
                String User = data.getExtras().getString("login_success");//接收返回数据
                TextView userView = (TextView) findViewById(R.id.user);
                userView.setText(User);
                status=USER;
                updateTabHost(status);
            }
        }

    }


    private List<Map<String, Object>> addData(List<Map<String, Object>>Data,ArrayList<String> book_info){
        Map<String, Object> map = new HashMap<String, Object>();

        for(int i=0;i<BookAttr.length;++i){
            map.put(BookAttr[i],book_info.get(i));
        }
        map.put("img",R.drawable.book);
        Data.add(map);

        return Data;
    }

    private static List<Map<String, Object>> getData(int x) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        switch (x){
            case 0:
            {
                File sdcard =Environment.getExternalStorageDirectory();
                String [] fileName = sdcard.list();
                for(String novel:fileName){
                    if(novel.contains("Shelf")){
                        String s=novel.replace("Shelf","");
                        String[] data={"0"};
                        String str=null;
                        try {
                            str=loadData(s).toString();;
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                        if(str!=null &&str!=""){
                             data=str.split(";");
                        }else continue;
                        String novelTitle=s.replace(".txt","");
                        Bitmap bitmap=getDiskBitmap(novelTitle+".jpg");
                        map = new HashMap<String, Object>();
                        map.put("title", novelTitle);
                        map.put("latestChapter", "已读:"+data[3]);
                        map.put("ChapterName", data[3]);
                        map.put("img", bitmap);
                        String NovelUrl=data[0].substring(0,data[0].lastIndexOf('/')+1);
                        map.put("NovelUrl",NovelUrl);
                        map.put("ChapterUrl",data[0]);
                        map.put("ChapterIndex",data[1]);
                        map.put("pageIndex",data[2]);

                        list.add(map);
                    }
                }


            }break;
            case 1:
            {
                map.put("title", "将夜");
                map.put("author", "作者：猫腻");
                map.put("category","分类：东方玄幻");
                map.put("latest", "更新：无穷的欢乐——后记");
                map.put("latestChapter", "简介：与天斗，其乐无穷");
                map.put("img", R.mipmap.ic_jiangye);
                list.add(map);

                map = new HashMap<String, Object>();
                map.put("title", "雪中悍刀行");
                map.put("category","分类：奇幻武侠");
                map.put("author", "作者:烽火戏诸侯");
                map.put("latest", "更新：小二上酒");
                map.put("latestChapter", "简介：江湖是一张珠帘");
                map.put("img", R.mipmap.ic_xuezhong);
                list.add(map);
            }break;
            default:break;
        }
        return list;
    }

    public static class BookAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<Map<String, Object>> mData;
        Boolean isSrc;
        public BookAdapter(Context context,List<Map<String, Object>> mData,Boolean isSrc){
            this.mInflater = LayoutInflater.from(context);
            this.mData=mData;
            this.isSrc=isSrc;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mData.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder=new ViewHolder();
                convertView = mInflater.inflate(R.layout.book_list_adapter, null);
                holder.img = (ImageView)convertView.findViewById(R.id.book_img);
                holder.title = (TextView)convertView.findViewById(R.id.book_title);
                holder.author = (TextView)convertView.findViewById(R.id.book_author);
                holder.category=(TextView)convertView.findViewById(R.id.book_category);
          //      holder.info = (TextView)convertView.findViewById(R.id.book_info);
                holder.latest=(TextView)convertView.findViewById(R.id.book_latest);
                holder.latestChapter=(TextView)convertView.findViewById(R.id.book_latestChapter);
       //         holder.viewBtn = (Button)convertView.findViewById(R.id.view_btn);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder)convertView.getTag();
            }

            if(mData.get(position).get("img")!=null) {
                if (isSrc)
                    holder.img.setBackgroundResource((Integer) mData.get(position).get("img"));
                else
                    holder.img.setBackground(new BitmapDrawable((Bitmap) mData.get(position).get("img")));
            }
            String str=null;
            if((str=(String)mData.get(position).get("title"))!=null)
                holder.title.setText(str);
            else
                holder.title.setVisibility(View.GONE);
            if((str=(String)mData.get(position).get("author"))!=null)
                holder.author.setText(str);
            else
                holder.author.setVisibility(View.GONE);
            if((str=(String)mData.get(position).get("latest"))!=null)
                holder.latest.setText(str);
            else
                holder.latest.setVisibility(View.GONE);
            if((str=(String)mData.get(position).get("latestChapter"))!=null)
                holder.latestChapter.setText(str);
            else
                holder.latestChapter.setVisibility(View.GONE);
            if((str=(String)mData.get(position).get("category"))!=null)
                holder.category.setText(str);
            else
                holder.category.setVisibility(View.GONE);

            return convertView;
        }

    }

    private void updateTabHost(int statue){
        TabHost th= (TabHost)findViewById(R.id.tabHost);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        th.setCurrentTab(0);
        switch(statue) {
            case VISITOR:{
                th.getCurrentTabView().setVisibility(View.GONE);
                fab.setVisibility(View.GONE);
                th.setCurrentTab(1);
            }break;
            case USER: {
                th.getCurrentTabView().setVisibility(View.VISIBLE);
             //   fab.setVisibility(View.VISIBLE);
            }break;
        }
        BookListView.get(th.getCurrentTab()).setVisibility(View.VISIBLE);

    }

    static Bitmap getDiskBitmap(String pathString)
    {
        File sdcard =Environment.getExternalStorageDirectory();
        Bitmap bitmap = null;
        try
        {
            File file = new File(sdcard,pathString);
            if(file.exists()) {
                bitmap = BitmapFactory.decodeFile(sdcard.getAbsolutePath()+"/"+pathString);
            }
        } catch (Exception e) {

        }
        return bitmap;
    }
    private class SearchTask extends AsyncTask<String, Integer, Boolean> {
        private List<Map<String,Object>> mData;
        private ProgressBar progressBar;
        private TextView ProgressText;
        @Override
        protected void onPreExecute(){
            ProgressText=(TextView)findViewById(R.id.ProgressText);
            ProgressText.setVisibility(View.VISIBLE);
            BookListView.get(3).setVisibility(View.GONE);
            progressBar=(ProgressBar)findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            TabHost th= (TabHost)findViewById(R.id.tabHost);
            th.setCurrentTab(3);
            th.getCurrentTabView().setVisibility(View.VISIBLE);

            NovelTitle=new ArrayList<String>();


            View mainView = (View) findViewById(R.id.drawer_layout);


            InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mainView.getApplicationWindowToken(), 0);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String list=GetHtml.read(SearchHtml+params[0]);
            NovelUrl=ParseHtml.getParseResults(list,ParseHtml.TYPE_NOVEL_HTML);
            mData=new ArrayList<Map<String, Object>>();
            for(int i=0;i<NovelUrl.size();++i)
            {
                String thisNovelHtml = NovelUrl.get(i);
                String s = GetHtml.read(thisNovelHtml);

                ParseHtml.book_info=new ArrayList<String>();
                ParseHtml.getParseResults(s, ParseHtml.TYPE_NOVEL_INFO_WITHOUT);
                book_info = ParseHtml.book_info;
                book_info.remove(3);
                while(book_info.size()<6)
                {
                    book_info.add(4,"");
                }
                while(book_info.size()>6){
                    book_info.remove(5);
                }
                NovelTitle.add(book_info.get(1));
                mData = addData(mData, book_info);
                try{
                    Thread.sleep(1);
                }catch (InterruptedException e){

                }
                publishProgress((i+1)*100/NovelUrl.size());
            }
            return true;
        }
        @Override
        protected void onProgressUpdate(Integer...Progress){
            ProgressBar ProgressBar=(android.widget.ProgressBar)findViewById(R.id.progressBar);
            ProgressText=(TextView)findViewById(R.id.ProgressText);
            ProgressBar.setProgress(Progress[0]);
            ProgressText.setText(Integer.toString(Progress[0]));
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if(success) {
                ProgressText.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                BookAdapter adapter = new BookAdapter(BookListView.get(3).getContext(), mData,true);
                BookListView.get(3).setVisibility(View.VISIBLE);
                BookListView.get(3).setAdapter(adapter);
                BookListView.get(3).setVisibility(View.VISIBLE);
            }
        }
    }

}
