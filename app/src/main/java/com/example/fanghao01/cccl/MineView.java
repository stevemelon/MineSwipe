package com.example.fanghao01.cccl;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Created by fanghao01 on 17/3/1.
 */

public class MineView extends View {
    
    private Context mContext;

    private int mapRow = 10;
    private int mapCow = 10;
    private int tileWidth = 100;

    private int adjoin[][] = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {-1, -1}, {-1, 1}, {1, 1}, {1, -1}};//周围8个方块
    private Mine[][] mines = new Mine[mapRow][mapCow];

    private List<Point> points = new ArrayList<>();//是雷的点
    private List<Point> pointsRemain = new ArrayList<>();

    private Signal signal;

    private int minesNum = mapCow * mapRow / 5;

    private Paint bmpPaint;
    private Paint minePaint;
    private Paint mathPaint;
    private Paint blankPaint;

    private Boolean isStarted = false;
    private Boolean isFinished = false;

    private Boolean flagSwitch = false;

    public MineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
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

        for (int i = 0; i < mapRow; i++) {
            for (int j = 0; j < mapCow; j++) {
                Mine mine = new Mine(0, false, false);
                mines[i][j] = mine;

                Point point = new Point(i, j);
                points.add(point);
            }
        }
        generateTile();

    }

    public Boolean getStarted() {
        return isStarted;
    }

    public void setSignal(Signal signal) {
        this.signal = signal;
    }

    public void setFlagSwitch(Boolean flagSwitch) {
        this.flagSwitch = flagSwitch;
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
                mines[i][j].setFlag(false);
                mines[i][j].value = 0;
            }
        }
        generateTile();
        invalidate();
        if (signal != null) {
            signal.onFinish();
            signal.onStart();
        }
    }

    private void generateTile() {
        generateMine();
        generateNum();
    }

    //生成雷
    private void generateMine() {
        if (pointsRemain != null) {
            points.addAll(pointsRemain);
            pointsRemain.clear();
        }
        for (int i = 0; i < minesNum; i++) {
            Random random = new Random();
            Point point = points.get(random.nextInt(points.size()));
            mines[point.getX()][point.getY()].setValue(-1);

            pointsRemain.add(point);
            points.remove(point);
        }
    }

    /***
     * 生成数字
     */
    private void generateNum() {
        for (int i = 0; i < mapRow; i++) {
            for (int j = 0; j < mapCow; j++) {
                if (mines[i][j].value == -1) {
                    continue;
                }
                int sum = 0;
                for (int k = 0; k < adjoin.length; k++) {
                    if (checkPoint(i + adjoin[k][0], j + adjoin[k][1])) {//越界访问
                        if (mines[i + adjoin[k][0]][j + adjoin[k][1]].value == -1) {
                            sum++;
                        }
                    }
                }
                mines[i][j].value = sum;
            }
        }
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
                float x;
                float y;
                x = event.getX();
                y = event.getY();
                NumX = (int) (y / tileWidth);//行号
                NumY = (int) (x / tileWidth);//列号
                if (checkPoint(NumX, NumY)) {

                    if (mines[NumX][NumY].value == 0) {//在已经出现的数字或者空白上点击
                        boolean isAllshow = true;
                        for (int k = 0; k < adjoin.length; k++) {
                            if (checkPoint(NumX + adjoin[k][0], NumY + adjoin[k][1])) {
                                int value = mines[NumX + adjoin[k][0]][NumY + adjoin[k][1]].value;
                                if (value == -1) {
                                    isAllshow = false;
                                }
                            }
                        }
                        if (isAllshow) {
                            for (int k = 0; k < adjoin.length; k++) {
                                if (checkPoint(NumX + adjoin[k][0], NumY + adjoin[k][1])) {
                                    mines[NumX + adjoin[k][0]][NumY + adjoin[k][1]].setOpen(true);
                                }
                            }
                            invalidate();
                        }
                    }
                    if (!flagSwitch) {//扫雷模式
                        if (mines[NumX][NumY].value != -1) {
                            open(new Point(NumX, NumY), false);
                        } else {//碰到雷 gg 了
                            isFinished = true;
                            isStarted = false;
                            if (signal != null) {
                                signal.onFinish();
                            }
                            showFailure();
                        }
                    } else {//插旗模式
                        if (!mines[NumX][NumY].isOpen()) {
                            mines[NumX][NumY].setFlag(true);
                        }
                    }
                    invalidate();

                }
                break;

        }
        return true;
    }

    private boolean checkPoint(int numX, int numY) {
        return numX < mapRow && numX >= 0 && numY < mapCow && numY >= 0;
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
                        if (mines[i][j].isFlag()) {
                            drawFlag(canvas, i, j);
                        } else {
                            drawBlank(canvas, i, j, blankPaint);
                        }
                    } else {
                        sum++;
                        drawShow(canvas, i, j);
                    }
                }
            }
        }
        if (sum + minesNum == mapRow * mapCow) {
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

    private void drawBlank(Canvas canvas, int i, int j, Paint blankPaint) {
        canvas.drawRect(j * tileWidth, i * tileWidth,
                (j + 1) * tileWidth,
                (i + 1) * tileWidth, blankPaint);
    }

    private void drawFlag(Canvas canvas, int i, int j) {
        RectF rectF = new RectF(j * tileWidth, i * tileWidth,
                (j + 1) * tileWidth,
                (i + 1) * tileWidth);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.flag), null, rectF, null);
    }

    /***
     * @param canvas
     * @param i      行号
     * @param j      列号
     */
    private void drawShow(Canvas canvas, int i, int j) {
        if (mines[i][j].value == -1) {//画雷
            canvas.drawRect(j * tileWidth, i * tileWidth,
                    (j + 1) * tileWidth,
                    (i + 1) * tileWidth, minePaint);
        } else {//画数字
            int value = mines[i][j].value;
            if (value == 0) {
                canvas.drawText("", (float) ((j + 0.5) * tileWidth), (float) ((i + 0.75) * tileWidth), mathPaint);
            } else {
                canvas.drawText(value + "", (float) ((j + 0.5) * tileWidth), (float) ((i + 0.75) * tileWidth), mathPaint);
            }

        }
    }

    /***
     * @param point   位置
     * @param isFirst 是否未第一次点开，防止用户第一次就踩了雷
     */
    public void open(Point point, boolean isFirst) {
        if (isFirst) {

        }
        mines[point.getX()][point.getY()].setOpen(true);
        Queue<Point> pointQueue = new LinkedList<>();
        pointQueue.add(point);
        while (pointQueue.size() != 0) {
            Point po = pointQueue.poll();
            for (int i = 0; i < adjoin.length; i++) {
                int x = po.getX() + adjoin[i][0];
                int y = po.getY() + adjoin[i][1];
                if (checkPoint(x, y) && mines[x][y].value == 0 && !mines[x][y].isOpen()) {
                    mines[x][y].setOpen(true);
                    pointQueue.add(new Point(x, y));
                }
            }
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
    interface Signal {

        void onStart();

        void onFinish();

        void onFlag(int num);
    }
}
