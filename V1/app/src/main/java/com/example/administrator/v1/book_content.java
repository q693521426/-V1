package com.example.administrator.v1;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;



import static android.R.id.tabs;
import static com.example.administrator.v1.MainActivity.updateShelf;
import static com.example.administrator.v1.book_information.bitmap;
import static com.example.administrator.v1.book_information.book_info;
import static com.example.administrator.v1.book_information.getBitmapFromUrl;
import static com.example.administrator.v1.book_information.updateStartButton;

public class book_content extends AppCompatActivity {
    private static String NovelTitle;
    private List<TextView> NovelText;
    private GridView FuncGridView;
    private AppBarLayout appBarLayout;
    private Boolean hide=true;
    private contView ContView;
    private Context context;
    static HashMap<Integer,String> NovelBuffer;
    private ArrayList<View> FuncView;
    private Integer TextSize=20;
    private Integer position,pageIndex;
    private String BackGround="#CCFFCC";
    private Bitmap bitmap;
    boolean order=true;
    public static String NovelUrl,ChapterUrl,ChapterName;
    public static View ContProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_content);
        context=this;


        Intent intent = getIntent();
        String[] data= intent.getStringArrayExtra("position");
        if(data.length==1){
            position=Integer.valueOf(data[0]);
            pageIndex=0;
            ChapterUrl=book_information.NovelHtml + book_information.info_link.get(position);
            ChapterName=book_information.info_chapter.get(position);
            NovelTask mNovelTask = new NovelTask();
            mNovelTask.execute(position,0);
        } else{
            position=Integer.valueOf(data[1]);
            pageIndex=Integer.valueOf(data[2]);
            ChapterUrl=book_information.NovelHtml+data[0];
            ChapterName=data[3];
            NovelTask mNovelTask = new NovelTask();
            mNovelTask.execute(position,1);
        }

    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public void onBackPressed(){
        save();
    }
    private void save(){
        Integer ChapterIndex=ContView.getIndex();
        Integer PageIndex=ContView.getPageIndex();
            try {
            saveData(NovelTitle+".txt",book_information.info_link.get(ChapterIndex),ChapterIndex,PageIndex,book_information.info_chapter.get(ChapterIndex),bitmap);
    /*        Toast.makeText(book_content.this, "保存成功",
                    Toast.LENGTH_SHORT).show();*/
        } catch (IOException e) {
            System.out.println("---------");
            e.printStackTrace();
        }
        updateStartButton();
        updateShelf();
        finish();
    }



    private void modMenuBar(){
        if(hide.booleanValue()==true){
            //全屏模式
            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            hide=false;
        } else{
            getSupportActionBar().show();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            hide=true;
        }
    }

    private class NovelTask extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        public void start() {
            execute();
        }

        @Override
        protected void onPreExecute(){
            ContProgressBar=(View)findViewById(R.id.ContProgressBar);
            FuncGridView=(GridView)findViewById(R.id.FuncGridView);
            FuncGridView.setVisibility(View.GONE);
            appBarLayout=(AppBarLayout)findViewById(R.id.AppBarLayout);
            appBarLayout.setVisibility(View.GONE);
            NovelText=new ArrayList<TextView>();
            NovelBuffer=new HashMap<Integer,String>();
            FuncView=new ArrayList<View>();

            modMenuBar();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            if(params[1]==1 && NovelUrl!=null && book_information.NovelUrl!=NovelUrl) {
                String s = GetHtml.read(NovelUrl);
                ParseHtml.book_info = new ArrayList<String>();
                ParseHtml.getParseResults(s, ParseHtml.TYPE_NOVEL_INFO);
                book_info = ParseHtml.book_info;
                book_info.remove(3);
                while (book_info.size() < 7) {
                    book_info.add(5, "");
                }
                while (book_info.size() > 7) {
                    book_info.remove(6);
                }
                NovelTitle=book_info.get(1);
                //章节列表,章节名&&链接
                book_information.info_list = ParseHtml.getParseResults(s, ParseHtml.TYPE_NOVEL_LIST);

                book_information.info_link = ParseHtml.getParseResults(book_information.info_list.get(0), ParseHtml.TYPE_NOVEL_INDEX);

                book_information.info_chapter = ParseHtml.getParseResults(book_information.info_list.get(0), ParseHtml.TYPE_NOVEL_CHAPTER);
                book_information.bitmap = getBitmapFromUrl(NovelUrl + book_info.get(6));
            }
            else{
                NovelTitle=book_information.NovelTitle;
            }
            updateNovelBuffer(params[0]);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if(success){
                updateFunc();
            }
        }

    }
    private void updateFunc() {
        updateFuncIndex();

        ContView=(contView) findViewById(R.id.contView);
        ContView.updateView(position,pageIndex,NovelBuffer.get(position),book_information.info_chapter.get(position),false,false);
        ContView.setListenBar(appBarLayout);
        ContView.setListenView(FuncGridView);
        ContView.setListenView(FuncView);
    }
    private void updateFuncIndex(){
        updateAppBarLayout();
        updateFuncIndexTab();
        updateChapterList(order);
        updateOption();
        updateAnimationOption();

        LinearLayout chapterView=(LinearLayout)findViewById(R.id.func_index_list);
        LinearLayout optionView=(LinearLayout)findViewById(R.id.func_option);
        LinearLayout optionAnimationView=(LinearLayout)findViewById(R.id.func_option_animation);

        FuncView.add(chapterView);
        FuncView.add(optionView);
        FuncView.add(optionAnimationView);

        ArrayList<HashMap<String,Object>> FuncList=new ArrayList<HashMap<String, Object>>();
        String[] Func_str={"目录","阅读选项","翻页动画","亮度调节","离线","缓冲"};
        for(int i=0;i<Func_str.length;++i){
            HashMap<String,Object> map=new HashMap<String,Object>();
            map.put("FuncText",Func_str[i]);
            FuncList.add(map);
        }
        SimpleAdapter FuncAdapter=new SimpleAdapter(context,FuncList,R.layout.func_adapter,new String[]{"FuncText"},new int[]{R.id.FuncText});
        FuncGridView.setAdapter(FuncAdapter);
        FuncGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FuncGridView.setVisibility(View.GONE);
                FuncView.get(position).setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateAppBarLayout(){
        ImageButton returnButton=(ImageButton)findViewById(R.id.returnButton);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        TextView titleText=(TextView)findViewById(R.id.titleText);
        titleText.setText(NovelTitle);

    }

    private void updateFuncIndexTab(){
        TabHost th= (TabHost)findViewById(R.id.tabHost);
        th.setup();
        th.addTab(th.newTabSpec("tab1").setIndicator("目录",null).setContent(R.id.tab1));
        th.addTab(th.newTabSpec("tab2").setIndicator("书签",null).setContent(R.id.tab2));

        TabWidget tabWidget=(TabWidget)findViewById(tabs);
        for (int i = 0; i < tabWidget.getChildCount(); i++) {
            TextView tv=(TextView)tabWidget.getChildAt(i).findViewById(android.R.id.title);
            tv.setTextSize(20);//设置字体的大小；
            tv.setTextColor(Color.parseColor("#000000"));//设置字体的颜色；
        }

        TextView ChapterNum=(TextView)findViewById(R.id.ChapterNum);
        ChapterNum.setText("一共"+book_information.info_chapter.size()+"章");
        final Button ReverseButton=(Button)findViewById(R.id.ReverseButton);
        ReverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order=order?false:true;
                updateChapterList(order);
                ReverseButton.setText(order?"倒序":"顺序");
            }
        });

        ImageButton exitButton=(ImageButton)findViewById(R.id.imageButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FuncView.get(0).setVisibility(View.GONE);
                appBarLayout.setVisibility(View.GONE);
                //           ContView.setOnTouch(true);
                ContView.setShowFunc(false);
            }
        });
    }
    private void updateChapterList(boolean order){
        ListView listView=(ListView)findViewById(R.id.func_chapter_list);
        if(order)
            listView.setAdapter(new ArrayAdapter<String>(listView.getContext(), R.layout.book_chapter_adapter2,book_information.info_chapter));
        else{
            ArrayList<String> reverse_info_chapter=new ArrayList<>(book_information.info_chapter);
            Collections.reverse(reverse_info_chapter);
            listView.setAdapter(new ArrayAdapter<String>(listView.getContext(), R.layout.book_chapter_adapter2, reverse_info_chapter));
        }

        final boolean tem_order=order;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FuncView.get(0).setVisibility(View.GONE);
                appBarLayout.setVisibility(View.GONE);
                ContView.setShowFunc(false);
                final Integer tem_position=tem_order?position:book_information.info_chapter.size()-1-position;
                if(!NovelBuffer.containsKey(tem_position)){
                    Thread thread=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            updateNovelBuffer(tem_position);
                        }
                    });
                    thread.start();
                    try {
                        while (!NovelBuffer.containsKey(tem_position))
                            Thread.sleep(100);
                        ContView.updateView(position,0,NovelBuffer.get(tem_position), book_information.info_chapter.get(position),false,false);
                    }catch(InterruptedException e){

                    }
                }
                else
                    ContView.updateView(position,0,NovelBuffer.get(position), book_information.info_chapter.get(position),false,false);
            }
        });
    }

    private void updateOption(){
        Button bigTextSize=(Button)findViewById(R.id.bigTextSize);
        Button smallTextSize=(Button)findViewById(R.id.smallTextSize);
        bigTextSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextSize<=30){
                    TextSize+=2;
                    ContView.updateView(TextSize);
                }
            }
        });
        smallTextSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextSize>=18){
                    TextSize-=2;
                    ContView.updateView(TextSize);
                }
            }
        });
        ArrayList<ImageButton> BackGroundButton=new ArrayList<ImageButton>();
        BackGroundButton.add((ImageButton) findViewById(R.id.whiteBackground));
        BackGroundButton.add((ImageButton) findViewById(R.id.greenBackground));
        BackGroundButton.add((ImageButton) findViewById(R.id.brownBackground));
        final String[] BackGroundColor={"#FFFFFF","#CCFFCC","#EDDCC4"};
        for(int i=0;i<BackGroundButton.size();++i){
            final String color=BackGroundColor[i];
            BackGroundButton.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BackGround=color;
                    ContView.updateView(color);
                }
            });
        }
        Switch nightSwitch=(Switch)findViewById(R.id.switch1);
        nightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ContView.updateView("#000000","#FFFFFF");
                } else {
                    ContView.updateView(BackGround,"#000000");
                }
            }
        });

    }

    private void updateAnimationOption(){
        RadioGroup animationOption=(RadioGroup)findViewById(R.id.RadioGroup);
        animationOption.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton Translation=(RadioButton)findViewById(R.id.Translation);
                RadioButton Bezier=(RadioButton)findViewById(R.id.Bezier);
                if(checkedId==Translation.getId())
                    ContView.setAnimator(0);
                else if(checkedId==Bezier.getId())
                    ContView.setAnimator(1);
            }
        });
    }
    static void updateNovelBuffer(Integer position){
        if(NovelBuffer.containsKey(position))
            return;

        String content_str = GetHtml.read(ChapterUrl);
        ArrayList<String> content_arr = ParseHtml.getParseResults(content_str, ParseHtml.TYPE_NOVEL_CONTENT);

        String content=book_information.info_chapter.get(position) + "\r\n"+content_arr.get(0);

        book_content.NovelBuffer.put(position, content);
    }

    static void saveData(String FILENAME,String NovelLink,Integer ChapterIndex,Integer PageIndex,String ChapterName,Bitmap bitmap)throws IOException {
        File sdCard = Environment.getExternalStorageDirectory();
        Log.i("tag", sdCard.getAbsolutePath());
        sdCard = new File(sdCard, FILENAME);
        FileOutputStream out = new FileOutputStream(sdCard);
        Writer writer = new OutputStreamWriter(out);
        try {
            String str = NovelLink+";"+ChapterIndex.toString()+";"+PageIndex.toString()+";"+ChapterName;
            savePic(bitmap,NovelTitle+".jpg");
            writer.write(str);
        } catch (IOException e) {
        } finally{
            writer.close();
        }
    }
    static void savePic(Bitmap bm, String fileName) throws IOException {
        File myCaptureFile = new File(Environment.getExternalStorageDirectory(),fileName);
        if(myCaptureFile.exists())return;
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        bos.flush();
        bos.close();
    }
    static StringBuilder loadData(String FILENAME)throws FileNotFoundException, IOException {
        BufferedReader reader = null;
        StringBuilder data = new StringBuilder();
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            sdCard = new File(sdCard, FILENAME);
            FileInputStream in = new FileInputStream(sdCard);
            reader = new BufferedReader(new InputStreamReader(in));
            String line = new String();
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            reader.close();
            return data;
        }
    }

}
