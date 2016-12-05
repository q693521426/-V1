package com.example.administrator.v1;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static com.example.administrator.v1.MainActivity.ShelfBook;
import static com.example.administrator.v1.MainActivity.updateShelf;
import static com.example.administrator.v1.book_content.loadData;
import static com.example.administrator.v1.book_content.saveData;


public class book_information extends AppCompatActivity {
    private Integer linecount,curlinecount;
    static public ArrayList<String> book_info, info_list,info_chapter,info_link;
    private ImageButton expand_button ;
    public static String NovelHtml = "http://www.biquge.com";
    private TextView IntruTextView ;
    private ImageView image_inf ;
    private ArrayList<TextView> BookInfoView;
    private View BookInfoMainView;
    public static View BookInfoProcessView;
    public static Button startButton,addShelfButton;
    public static String[] data;
    private ListView BookInfoChapterView;
    public static Bitmap bitmap;
    public static String NovelUrl,NovelTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_information);

        Intent intent=getIntent();
        ArrayList<String> Novel = intent.getStringArrayListExtra("Novel");
        NovelUrl=Novel.get(0);
        NovelTitle=Novel.get(1);

        setupActionBar();

        BookInfoProcessView=(View)findViewById(R.id.BookInfoProcessView);
        BookInfoMainView=(View)findViewById(R.id.BookInfoMainView);
        BookInfoProcessView.setVisibility(View.VISIBLE);
        BookInfoMainView.setVisibility(View.GONE);

        startButton=(Button)findViewById(R.id.start);
        addShelfButton=(Button)findViewById(R.id.addShelf);


        File Shelf=new File(Environment.getExternalStorageDirectory(),NovelTitle+"Shelf.txt");
        if(Shelf.exists()) {
            addShelfButton.setText("移出书架");
        }else{
            addShelfButton.setText("加入书架");
        }


        NovelTask mAuthTask = new NovelTask();
        mAuthTask.execute(NovelUrl);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(NovelTitle);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // TODO Auto-generated method stub
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void expandIntruTextView(){

        linecount = IntruTextView.getLineCount();

        if(linecount>3) {
            curlinecount = 3;
            IntruTextView.setLines(3);
            IntruTextView.setEllipsize(TextUtils.TruncateAt.END);
            expand_button.setBackgroundResource(R.mipmap.btn_instruction_down);
            expand_button.setVisibility(View.VISIBLE);
        }else {
            curlinecount=linecount;
            expand_button.setVisibility(View.GONE);
        }
        expand_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(curlinecount>3){
                    IntruTextView.setLines(3);
                    curlinecount=3;
                    expand_button.setBackgroundResource(R.mipmap.btn_instruction_down);
                }else{
                    IntruTextView.setLines(linecount);
                    curlinecount=linecount;
                    expand_button.setBackgroundResource(R.mipmap.btn_instruction_up);
                }
            }
        });
    }
    static public Bitmap getBitmapFromUrl(String url) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
      //      Log.d(TAG, url);
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setConnectTimeout(0);
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private class NovelTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        public void start() {
            execute();
        }

        @Override
        protected void onPreExecute(){
            expand_button = (ImageButton) findViewById(R.id.expand_button);
            IntruTextView = (TextView) findViewById(R.id.IntruTextView);
            image_inf = (ImageView) findViewById(R.id.image_inf);
            BookInfoChapterView=(ListView)findViewById(R.id.ChapterListView);


            book_info=new ArrayList<String>();
            info_list=new ArrayList<String>();
            info_chapter=new ArrayList<String>();
            info_link=new ArrayList<String>();

            BookInfoView = new ArrayList<TextView>() {{
                add((TextView) findViewById(R.id.category_inf));
                add((TextView) findViewById(R.id.title_inf));
                add((TextView) findViewById(R.id.author_inf));
                add((TextView) findViewById(R.id.lastest_chap));
                add((TextView) findViewById(R.id.lastest_time));
                add(IntruTextView);
            }};

        }

        @Override
        protected Boolean doInBackground(String... params) {

            String thisNovelHtml=params[0];

            String s=GetHtml.read(thisNovelHtml);
            ParseHtml.book_info=new ArrayList<String>();
            ParseHtml.getParseResults(s,ParseHtml.TYPE_NOVEL_INFO);
            book_info=ParseHtml.book_info;
            book_info.remove(3);
            while(book_info.size()<7)
            {
                book_info.add(5,"");
            }
            while(book_info.size()>7){
                book_info.remove(6);
            }
            bitmap = getBitmapFromUrl(NovelHtml+book_info.get(6));

            //章节列表,章节名&&链接
            info_list=ParseHtml.getParseResults(s,ParseHtml.TYPE_NOVEL_LIST);

            info_link=ParseHtml.getParseResults(info_list.get(0),ParseHtml.TYPE_NOVEL_INDEX);

            info_chapter=ParseHtml.getParseResults(info_list.get(0),ParseHtml.TYPE_NOVEL_CHAPTER);


            //    ArrayList<String> list=ParseHtml.getParseResults(s,ParseHtml.TYPE_NOVEL_LIST);
            //    ArrayList<String> chapter=ParseHtml.getParseResults(list.get(0),ParseHtml.TYPE_NOVEL_CHAPTER);

            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values){

        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if(success){
                updateBookInfo();
                updateStartButton();
                updateShelfButton();
                new openTask().execute();
            }
        }

    }
    private void updateBookInfo(){
        BookInfoProcessView.setVisibility(View.GONE);
        BookInfoMainView.setVisibility(View.VISIBLE);
        for(int i=0;i<6;++i){
            BookInfoView.get(i).setText(book_info.get(i));
        }

        image_inf.setImageBitmap(bitmap);
        image_inf.invalidate();

        BookInfoChapterView.setAdapter(new ArrayAdapter<String>(BookInfoChapterView.getContext(), R.layout.book_chapter_adapter,info_chapter));

        BookInfoChapterView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(book_information.this,book_content.class);
                String[] data={Integer.toString(position)};
                intent.putExtra("position",data);

                startActivity(intent);
            }
        });
    }
    static void updateStartButton(){
        if(NovelTitle==null)return;
        File Book=new File(Environment.getExternalStorageDirectory(),NovelTitle+".txt");
        if(!Book.exists()) {
            try {
                saveData(NovelTitle + ".txt", book_information.info_link.get(0), 0, 0,book_information.info_chapter.get(0), book_information.bitmap);
            }catch (IOException e){
                System.out.println("---------");
                e.printStackTrace();
            }
        }

        String str=null;
        String[] s={"0"};
        data=s;
        try {
            str = loadData(NovelTitle+".txt").toString();
        }catch(IOException e){
            e.printStackTrace();
        }
        data=str.split(";");
        if(data[0].equals("") || data[1].equals("0")&&data[2].equals("0")){
            startButton.setText("开始阅读");
        } else {
            startButton.setText("继续阅读");
        }
    }
    public void startButtonListen(View v){
        Intent intent = new Intent(book_information.this, book_content.class);
        intent.putExtra("position", data);
        startActivity(intent);
    }
    public void updateShelfButton(){
        addShelfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File Shelf=new File(Environment.getExternalStorageDirectory(),NovelTitle+"Shelf.txt");
                if(Shelf.exists()) {
                    Shelf.delete();
                    updateShelf();
                    Toast.makeText(book_information.this, "成功移出书架",
                            Toast.LENGTH_SHORT).show();
                    addShelfButton.setText("加入书架");
                }else{
                    try {
                        Shelf.createNewFile();
                        updateShelf();
                        addShelfButton.setText("移出书架");
                        Toast.makeText(book_information.this, "成功加入书架",
                                Toast.LENGTH_SHORT).show();
                    }catch(IOException e){
                        System.out.println("----");
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    private class openTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            return true;
        }
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                expandIntruTextView();
            }
        }
    }
}
