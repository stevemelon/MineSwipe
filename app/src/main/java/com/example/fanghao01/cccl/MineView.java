package com.example.fanghao01.cccl;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by fanghao01 on 17/3/1.
 */

public class MineView extends View {
    Context mContext;
    int mapRow = 5;
    int mapCow = 10;
    int tileWidth = 100;
    int adjoin[][] = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {-1, -1}, {-1, 1}, {1, 1}, {1, -1}
    };
    Mine[][] mines = new Mine[mapRow][mapCow];
    Signal signal;

    int minesNum = 1;

    private Paint bmpPaint;
    private Paint minePaint;
    private Paint mathPaint;
    private Paint blankPaint;

    private Boolean isStarted = false;
    private Boolean isFinished = false;

    public MineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public Boolean getStarted() {
        return isStarted;
    }

    public void setSignal(Signal signal) {
        this.signal = signal;
    }

    public void refresh() {
        if (!isStarted && !isFinished) {
            return;
        }
        isStarted = false;
        isFinished = false;
        for (int i = 0; i < mapRow; i++) {
            for (int j = 0; j < mapCow; j++) {
                mines[i][j].setOpen(false);
            }
        }
        invalidate();
        if (signal != null) {
            signal.onFinish();
            signal.onStart();
        }
    }

    private void init() {
        for (int i = 0; i < mapRow; i++) {
            for (int j = 0; j < mapCow; j++) {
                Mine mine = new Mine(0, false, false);
                mines[i][j] = mine;
            }
        }
        Mine mine = new Mine(-1, false, false);
        mines[1][2] = mine;

        bmpPaint = new Paint();
        bmpPaint.setAntiAlias(true);
        bmpPaint.setColor(Color.WHITE);
        bmpPaint.setStrokeWidth(3);

        //画雷
        minePaint = new Paint();
        minePaint.setColor(Color.BLACK);
        minePaint.setStyle(Paint.Style.FILL);

        //数字 paint
        mathPaint = new Paint(Paint.DEV_KERN_TEXT_FLAG);
        mathPaint.setColor(Color.BLUE);
        mathPaint.setAntiAlias(true);
        mathPaint.setTextSize(60f);
        mathPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mathPaint.setTextAlign(Paint.Align.CENTER);

        //空白
        blankPaint = new Paint();
        blankPaint.setColor(Color.GRAY);
        blankPaint.setStyle(Paint.Style.FILL);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {


            case MotionEvent.ACTION_DOWN:
                if (signal != null && !isStarted) {
                    isStarted = true;
                    signal.onStart();
                }
                int NumX;
                int NumY;
                float x ;
                float y ;
                x = event.getX();
                y = event.getY();
                NumX = (int) (x / tileWidth);//列号
                NumY = (int) (y / tileWidth);//行号
                if (checkPoint(NumX, NumY)) {
                    if (mines[NumY][NumX].value != -1) {
                        mines[NumY][NumX].setOpen(true);

                        Boolean flag = false;
                        for (int k = 0; k < adjoin.length; k++) {
                            if (checkPoint(NumX +adjoin[k][0], NumY + adjoin[k][1])) {//越界访问
                                if (mines[NumY + adjoin[k][1]][ NumX + adjoin[k][0]].value == -1){
                                    flag = true;
                                }
                            }
                        }

                        if (!flag) {
                            for (int k = 0; k < adjoin.length; k++) {
                                if (checkPoint(NumX +adjoin[k][0], NumY + adjoin[k][1])) {//越界访问
                                    mines[NumY + adjoin[k][1]][NumX + adjoin[k][0]].setOpen(true);

                                }
                            }
                        }
                    } else {
                        isFinished = true;
                        isStarted = false;
                        if (signal != null) {
                            signal.onFinish();
                        }
                        showFailure();
                    }
                    invalidate();

                }
                break;

        }
        return true;
    }

    private boolean checkPoint(int numX, int numY) {
        return numX < mapCow && numX >= 0 && numY < mapRow && numY >= 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = mapCow * tileWidth;
        int height = mapRow * tileWidth;
        int sum = 0;
        //画地图
        for (int i = 0; i < mapRow; i++) {
            for (int j = 0; j < mapCow; j++) {
                if (isFinished) {
                    isStarted = false;
                    if (signal != null) {
                        signal.onFinish();
                    }
                    drawShow(canvas, i, j);
                } else {
                    if (!mines[i][j].isOpen()) {//如果没有打开
                        canvas.drawRect(j * tileWidth, i * tileWidth,
                                (j + 1) * tileWidth,
                                (i + 1) * tileWidth, blankPaint);
                    } else {
                        sum++;
                        drawShow(canvas, i, j);
                    }
                }

            }

        }
        if(sum + minesNum == mapRow * mapCow){
            isFinished = true;
            isStarted = false;
            if (signal != null) {
                signal.onFinish();
            }
            showSuccess();
        }
        //划线
        for (int x = 0; x <= mapCow; x++) {
            canvas.drawLine(x * tileWidth, 0, x * tileWidth, height, bmpPaint);
        }
        for (int i = 0; i <= mapRow; i++) {
            canvas.drawLine(0, i * tileWidth, width, i * tileWidth, bmpPaint);
        }


    }
    private void showFailure() {
        new AlertDialog.Builder(mContext)
                .setMessage("恭喜你，爆炸了")
                .setCancelable(false)
                .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }
    private void showSuccess() {
        new AlertDialog.Builder(mContext)
                .setMessage("恭喜你，你找出了所有雷")
                .setCancelable(false)
                .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }

    /***
     *
     * @param canvas
     * @param i 行号
     * @param j 列号
     */
    private void drawShow(Canvas canvas, int i, int j) {
        if (mines[i][j].value == -1) {//画雷
            canvas.drawRect(j * tileWidth, i * tileWidth,
                    (j + 1) * tileWidth,
                    (i + 1) * tileWidth, minePaint);
        } else {//画数字
            int sum = 0;
            for (int k = 0; k < adjoin.length; k++) {
                if (i + adjoin[k][0] >= 0 && i + adjoin[k][0] < mapRow &&
                        j + adjoin[k][1] >= 0 && j + adjoin[k][1] < mapCow) {//越界访问
                    if (mines[i + adjoin[k][0]][j + adjoin[k][1]].value == -1) {
                        sum++;
                    }
                }
            }
            mines[i][j].value = sum;
            canvas.drawText(sum + "", (float) ((j + 0.5) * tileWidth), (float) ((i + 0.75) * tileWidth), mathPaint);
        }
    }

    interface Signal {
        void onStart();

        void onFinish();
    }
}
