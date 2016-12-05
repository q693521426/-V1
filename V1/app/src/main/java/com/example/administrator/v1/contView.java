package com.example.administrator.v1;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.example.administrator.v1.book_content.NovelBuffer;
import static com.example.administrator.v1.book_content.updateNovelBuffer;
import static com.example.administrator.v1.book_content.ContProgressBar;
import static com.example.administrator.v1.book_information.info_chapter;
import static java.lang.Thread.sleep;

public class contView extends View {
    private Paint mPaint;
    private String cont,title;
    private Integer index;
    private Boolean EndChapterFlag,NextSignal=false,PreSignal=false,NextReceive=false,PreReceive=false;
    private Integer TextSize = 20;
    private String BackgroundColor = "#CCFFCC";
    private String TextColor = "#000000",TitleColor="#948F90";
    private Integer pageLine;
    private Integer pageNum;
    private Integer pageIndex=0;
    private Bitmap upBitmap,downBitmap,lastBitmap,curBitmap,nextBitmap;
    private List<String> conText;
    public Integer ClickCount=0;
    Integer BufferNum=5;
    NovelBufferTask mNovelBufferTask;
    public View ListenView,ListenBar;
    public ArrayList<View> ArrayListenView=null;
    private boolean isForestPage, isLastPage,showFunc=false,onTouch=false;
    private float mClipX;// 裁剪右端点坐标
    private float mAutoAreaLeft, mAutoAreaRight;// 控件左侧和右侧自动吸附的区域
    private float mCurPointX;// 指尖触碰屏幕时点X的坐标值
    private int mViewWidth, mViewHeight,height;// 控件宽高
    private float mMoveValid;//移动有效距离

    private static final int offset = 400;

    private Paint paint;
    private Path path;
    private Path pathNext;
    private float ax,ay,bx,by,cx,cy,dx,dy,ex,ey,fx,fy,gx,gy,hx,hy,ix,iy,jx,jy,kx,ky;
    private float cornerX, cornerY;
    private float outX,outY;
    private ValueAnimator valueAnimator;

    public Integer AnimationOption=0;
    final int AnimationTranslation=0,AnimationBezier=1;

    public contView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public contView(Context context)
    {
        super(context);
        init();
    }


    public contView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        cont=null;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(1);
        path = new Path();
        pathNext = new Path();
        valueAnimator = ValueAnimator.ofFloat(0,50f);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentValue = (Float)animation.getAnimatedValue();
                float fraction = currentValue / 50f;
                ax += fraction * (outX - ax);
                ay += fraction * (outY - ay);
                postInvalidate();
            }
        });
    }

    public Integer getIndex(){
        return index;
    }

    public Integer getPageIndex(){
        return pageIndex;
    }
    public void setShowFunc(Boolean showFunc){
        this.showFunc=showFunc;
        invalidate();
    }

    public void setListenView(View v){
        ListenView=v;
    }
    public void setListenBar(View v){
        ListenBar=v;
    }
    public void setListenView(ArrayList<View> v){
        ArrayListenView=v;
    }

    public void setAnimator(Integer AnimationOption){
        this.AnimationOption=AnimationOption;
        mClipX=mViewWidth;
        ax = cornerX = mViewWidth;
        ay = cornerY = mViewHeight;
        invalidate();
    }

    public void updateView(Integer TextSize){
        this.TextSize=TextSize;
        curBitmap=null;
        ax = cornerX = mViewWidth;
        ay = cornerY = mViewHeight;
        mClipX=mViewWidth;
        invalidate();
    }

    public void updateView(String BackgroundColor,String TextColor){
        this.BackgroundColor=BackgroundColor;
        this.TextColor=TextColor;
        curBitmap=null;
        ax = cornerX = mViewWidth;
        ay = cornerY = mViewHeight;
        mClipX=mViewWidth;
        invalidate();
    }

    public void updateView(String BackgroundColor){
        this.BackgroundColor=BackgroundColor;
        curBitmap=null;
        ax = cornerX = mViewWidth;
        ay = cornerY = mViewHeight;
        mClipX=mViewWidth;
        invalidate();
    }


    public void updateView(Integer index,Integer pageIndex,String cont,String title,Boolean EndChapterFlag,Boolean adj){
        this.cont=cont;
        this.index=index;
        this.title=title;
        this.pageIndex=pageIndex;
        this.EndChapterFlag=EndChapterFlag;
        if(adj==false) {
            upBitmap = null;
            downBitmap=null;
            lastBitmap=null;
            nextBitmap=null;
        }
        curBitmap=null;
        mClipX=mViewWidth;
        ax = cornerX = mViewWidth;
        ay = cornerY = mViewHeight;
        mNovelBufferTask=new NovelBufferTask();
        mNovelBufferTask.execute(index);
        invalidate();
    }
    public Bitmap getBitmap(Integer Index) {
        Bitmap mBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);
        if(DoPaint(canvas,Index)) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
            return mBitmap;
        }else
            return null;
    }
    private boolean DoPaint(Canvas canvas,Integer pageIndex){
        List<String> conText;
        String title;

        Integer pageNum=this.pageNum;

        if(pageIndex<0 && index>0){
            while(!NovelBuffer.containsKey(index-1));
            conText = splitLine(NovelBuffer.get(index-1));
            title=info_chapter.get(index-1);
            pageNum=(int)Math.ceil((double) conText.size() / pageLine);
            pageIndex=pageNum-1;
        }else if(pageIndex>=this.pageNum && index < info_chapter.size() - 1){
            while(!NovelBuffer.containsKey(index+1));
            conText = splitLine(NovelBuffer.get(index+1));
            title=info_chapter.get(index+1);
            pageNum=(int)Math.ceil((double) conText.size() / pageLine);
            pageIndex=0;
        } else if((pageIndex<0 && index==0) || (pageIndex>=pageNum && index == info_chapter.size() - 1)){
            return false;
        }else{
            pageNum=this.pageNum;
            title=this.title;
            conText=this.conText;
        }


        mPaint.setColor(Color.parseColor(BackgroundColor));
        canvas.drawRect(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingEnd(), mPaint);
        canvas.drawColor(Color.parseColor(BackgroundColor));

        mPaint.setColor(Color.parseColor(TitleColor));

        mPaint.setTextSize(dip2px(getContext(), TextSize*3/4));
        canvas.drawText(title,getPaddingLeft(), getPaddingTop()+dip2px(getContext(), TextSize*3/4), mPaint);

        mPaint.setColor(Color.parseColor(TextColor));
        mPaint.setTextSize(dip2px(getContext(), TextSize));

        for (int j = 0; j < ((pageIndex==pageNum-1)?(conText.size()%pageLine):pageLine); ++j)
            canvas.drawText(conText.get(pageIndex * pageLine + j), getPaddingLeft(), getPaddingTop() + (j + 2) * dip2px(getContext(), TextSize), mPaint);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(cont==null)return;
        height = px2dip(getContext(), getMeasuredHeight() - getPaddingTop() - getPaddingEnd());

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mPaint.setTextSize(dip2px(getContext(), TextSize));
        conText = splitLine(cont);

        pageLine = height / TextSize -1;
        pageNum = (int) Math.ceil((double) conText.size() / pageLine);
        if(EndChapterFlag)
        {
            pageIndex=pageNum-1;
            EndChapterFlag=false;
        }

        if(curBitmap==null) {
            lastBitmap = getBitmap(pageIndex-1);
            curBitmap = getBitmap(pageIndex);
            nextBitmap = getBitmap(pageIndex+1);
        }

        upBitmap=curBitmap;
        if (showFunc||NextSignal){
            downBitmap=nextBitmap;
        }else if(showFunc||PreSignal){
            downBitmap=lastBitmap;
        }else
            downBitmap=nextBitmap;


        switch (AnimationOption){
            case AnimationTranslation:{
                canvas.drawBitmap(downBitmap, 0, 0, null);
                if(NextSignal)canvas.clipRect(0, 0, mClipX, mViewHeight);
                else if(PreSignal)canvas.clipRect(mClipX, 0, mViewWidth, mViewHeight);
                else
                    canvas.clipRect(0, 0, mClipX, mViewHeight);
                canvas.drawBitmap(upBitmap, 0, 0, null);
            }break;
            case AnimationBezier:{
                calculatePoints();
                canvas.save();
                path.reset();
                path.moveTo(jx, jy);
                path.quadTo(hx, hy, kx, ky);
                path.lineTo(ax, ay);
                path.lineTo(bx, by);
                path.quadTo(ex, ey, cx, cy);
                path.lineTo(fx, fy);
                path.close();
                canvas.drawBitmap(downBitmap, 0, 0, null);
                canvas.restore();

                canvas.save();
                canvas.clipPath(path, Region.Op.XOR);
                canvas.drawBitmap(upBitmap, 0, 0, null);
                canvas.restore();

                canvas.save();
                canvas.clipPath(path);
                pathNext.reset();
                pathNext.moveTo(cx, cy);
                pathNext.quadTo(dx, dy, dx, dy);
                pathNext.lineTo(ix, iy);
                pathNext.quadTo(jx, jy, jx, jy);
                pathNext.lineTo(fx, fy);
                pathNext.close();
                canvas.clipPath(pathNext, Region.Op.DIFFERENCE);
                canvas.drawColor(Color.parseColor(BackgroundColor));
                canvas.restore();
            }break;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        mViewWidth = w;
        mViewHeight = h;

        ax = cornerX = mViewWidth;
        ay = cornerY = mViewHeight;

        mClipX = mViewWidth;

        mAutoAreaLeft = mViewWidth * 1 / 5F;
        mAutoAreaRight = mViewWidth * 4 / 5F;

        mMoveValid = mViewWidth  / 100F;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(AnimationOption){
            case AnimationTranslation:{
                return onTouchTranslation(event);
            }
            case AnimationBezier:{
                return onTouchBezier(event);
            }
        }
        return true;
    }

    private boolean onTouchTranslation(MotionEvent event){
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {//触摸
                mCurPointX = event.getX();
                if (!showFunc) {
                    if (mCurPointX < mAutoAreaLeft) {
                        if(index==0){
                            showToast("the foremost page");
                            PreSignal=false;
                            NextSignal=false;
                        }else{
                            mClipX = mCurPointX;
                            PreSignal=true;
                            NextSignal=false;
                            invalidate();
                        }
                    } else if(mCurPointX > mAutoAreaRight){
                        if(index==info_chapter.size()-1){
                            showToast("the latest page");
                            PreSignal=false;
                            NextSignal=false;
                        }else {
                            mClipX = mCurPointX;
                            PreSignal = false;
                            NextSignal = true;
                            invalidate();
                        }
                    }
                }
            }
            break;
            case MotionEvent.ACTION_MOVE:// 滑动时
                float SlideDis = mCurPointX - event.getX();
                if (!showFunc) {
                    if ((Math.abs(SlideDis) > mMoveValid)) {
                        mClipX = event.getX();
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:// 触点抬起时
                updateFunc(event.getX());
                if (!showFunc&&(Math.abs(mCurPointX-event.getX())>mMoveValid)) {
                    judgeSlideAuto(event.getX());
                }
                break;
        }
        return true;
    }

    private boolean onTouchBezier(MotionEvent event){
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mCurPointX = event.getX();
                if(!showFunc){
                    float x = event.getX();
                    float y = event.getY();
                    if (x <= mViewWidth / 2 && y <= mViewHeight / 2) {
                        //second
                        cornerX = 0;
                        cornerY = 0;
                        outX = mViewWidth + 3*offset;
                        outY = mViewHeight + 3*offset;
                        PreSignal=true;
                        NextSignal=false;
                    }else if(x <= mViewWidth / 2 && y > mViewHeight / 2){
                        //third
                        cornerX = 0;
                        cornerY = mViewHeight;
                        outX = mViewWidth + 3*offset;
                        outY = -3*offset;
                        PreSignal=true;
                        NextSignal=false;
                    }else if(x > mViewWidth / 2 && y <= mViewHeight / 2){
                        //first
                        cornerX = mViewWidth;
                        cornerY = 0;
                        outX = -3*offset;
                        outY = mViewHeight + 3*offset;
                        PreSignal = false;
                        NextSignal = true;
                    }else if(x > mViewWidth / 2 && y > mViewHeight / 2){
                        //fourth
                        cornerX = mViewWidth;
                        cornerY = mViewHeight;
                        outX = -3*offset;
                        outY = -3*offset;
                        PreSignal = false;
                        NextSignal = true;
                    }
                    ax = x;
                    ay = y;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!showFunc) {
                    ax = event.getX();
                    ay = event.getY();
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                updateFunc(event.getX());
                if (!showFunc&&(Math.abs(mCurPointX-event.getX())>mMoveValid)) {
                    if (((Math.abs(outX - ax)-3*offset < mViewWidth / 2)) || ((Math.abs(outY - ay)-3*offset < mViewHeight / 2))) {
                        valueAnimator.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (NextSignal) {
                                    if (index < info_chapter.size() - 1) {
                                        pageIndex++;

                                        lastBitmap = curBitmap;
                                        curBitmap = nextBitmap;
                                        nextBitmap = getBitmap(pageIndex + 1);

                                        ax=cornerX;
                                        ay=cornerY;
                                        NextSignal=false;

                                        if(pageIndex>=pageNum) {
                                            pageIndex=0;
                                            updateView(index + 1, pageIndex,NovelBuffer.get(index + 1), info_chapter.get(index + 1), false,true);
                                        }

                                        invalidate();
                                    } else {
                                        isLastPage = true;
                                        showToast("the lastest page");
                                    }
                                } else if (PreSignal) {
                                    if (index > 0) {
                                        pageIndex--;
                                        nextBitmap=curBitmap;
                                        curBitmap=lastBitmap;
                                        lastBitmap=getBitmap(pageIndex-1);

                                        ax=cornerX;
                                        ay=cornerY;
                                        PreSignal=false;
                                        invalidate();

                                        if(pageIndex<0) {
                                            updateView(index - 1, 0,NovelBuffer.get(index - 1), info_chapter.get(index - 1), true,true);
                                        }
                                    } else {
                                        PreReceive = false;
                                        isForestPage = true;
                                        showToast("the forest page");
                                    }
                                    PreSignal=false;
                                    invalidate();
                                }
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }
                        });
                        valueAnimator.setDuration(500).start();

                    } else {
                        ax=mViewWidth;
                        ay=mViewHeight;
                    }
                }
                break;
        }
        return true;
    }

    private void updateFunc(float eventX){
        if((Math.abs(mCurPointX-eventX)<mMoveValid)){
            if(showFunc){
                ListenBar.setVisibility(GONE);
                ListenView.setVisibility(GONE);
                for(View v:ArrayListenView)
                    v.setVisibility(GONE);
                showFunc=false;
            }
            else{
                if((mCurPointX>mAutoAreaLeft)&&(mCurPointX<mAutoAreaRight)) {
                    ListenBar.setVisibility(VISIBLE);
                    ListenView.setVisibility(VISIBLE);
                    showFunc = true;
                }
            }
        }
    }

    private void judgeSlideAuto(float eventX) {
        mClipX=eventX;
        if (mClipX < mAutoAreaLeft) {
            while (mClipX-- > 0)
                invalidate();
            if(NextSignal) {
                if (index < info_chapter.size() - 1) {
                    pageIndex++;

                    lastBitmap = curBitmap;
                    curBitmap = nextBitmap;
                    nextBitmap = getBitmap(pageIndex + 1);

                    NextSignal = false;
                    mClipX = mViewWidth;
                    invalidate();
                    if (pageIndex >= pageNum) {
                        updateView(index + 1, 0,NovelBuffer.get(index + 1), info_chapter.get(index + 1), false, false);
                    }
                }else {
                    isLastPage = true;
                    showToast("the lastest page");
                    mClipX = mViewWidth;
                    invalidate();
                }
            }
        } else if (mClipX > mAutoAreaRight) {
            while (mClipX++ < mViewWidth)
                invalidate();
            if(PreSignal){
                if (index > 0) {
                    pageIndex--;
                    nextBitmap = curBitmap;
                    curBitmap = lastBitmap;
                    lastBitmap = getBitmap(pageIndex - 1);

                    PreSignal = false;
                    mClipX = mViewWidth;
                    invalidate();
                    if (pageIndex < 0) {
                        updateView(index - 1, 0,NovelBuffer.get(index - 1), info_chapter.get(index - 1), true, false);
                    }
                }
                else {
                    PreReceive=false;
                    isForestPage = true;
                    showToast("the forest page");
                    mClipX = mViewWidth;
                    invalidate();
                }
                PreReceive=false;
            }
        }

        mClipX=mViewWidth;
        invalidate();
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public List<String> splitParagraph(String cont) {
        List<String> result = new ArrayList<String>();
        if (cont != null) {
            String[] sp = cont.split("\r\n");
            for (int i = 0; i < sp.length; ++i) {
                if (sp[i] != "")
                    result.add(sp[i]);
            }
        } else {
            return null;
        }
        return result;
    }

    public List<String> splitLine(String cont) {
        List<String> line_result = new ArrayList<String>();
        if (cont != null) {
            List<String> para_result = splitParagraph(cont);
            for (String para : para_result) {
                while (true) {
                    int str_num = mPaint.breakText(para, true, getMeasuredWidth(), null);
                    if (str_num < para.length()) {//多行
                        String str_line = para.substring(0, str_num);
                        line_result.add(str_line);
                        para = para.substring(str_num, para.length());
                    } else {
                        line_result.add(para);
                        break;
                    }
                }
            }
        }
        return line_result;
    }

    private void showToast(Object msg) {
        Toast.makeText(getContext(), msg.toString(), Toast.LENGTH_SHORT).show();
    }

    private void calculatePoints(){
        fx = cornerX;
        fy = cornerY;

        gx = (ax + fx) / 2;
        gy = (ay + fy) / 2;

        float gm = fy - gy;
        float mf = fx - gx;
        float em = gm * gm / mf;

        ex = gx - em;
        ey = fy;

        float hm = mf * mf / gm;

        hx = fx;
        hy = gy - hm;


        cx = ex - (fx - ex)/2;
        cy = fy;

        jx = fx;
        jy = hy - (fy - hy)/2;

        bx = (ax + ex) / 2;
        by = (ay + ey) / 2;

        kx = (ax + hx) / 2;
        ky = (ay + hy) / 2;

        //p middle point of the bc;
        float px = (bx + cx) / 2;
        float py = (by + cy) / 2;

        dx = (px + ex) / 2;
        dy = (py + ey) / 2;


        px = (kx + jx) / 2;
        py = (ky + jy) / 2;

        ix = (px + hx) / 2;
        iy = (py + hy) / 2;
    }

    private class NovelBufferTask extends AsyncTask<Integer, Integer, Boolean> {

        public void start() {
            execute();
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            Integer position = params[0];

            for(int i=-1;i<BufferNum-1;i++){
                Integer tem_position=position+i;
                if(i==0)continue;
                if(tem_position<0)continue;
                if(tem_position>=book_information.info_link.size())break;

                updateNovelBuffer(tem_position);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if(success){

            }
        }
    }
}
