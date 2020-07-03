package com.xw.sample.dashboardviewdemo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * 带有超速报警的仪表盘
 * Create by LiTtleBayReal
 * 2020/7/2
 */
public class SpeedBoardView extends View {
	private int mRadius; // 圆形半径
	private int mStartAngle = 150; // 刻度起始角度
	private int mSweepAngle = 240; // 刻度绘制角度
	private int mMin = 0; // 刻度最小值
	private int mMax = 70; // 刻度最大值
	private int mSection = 7; // 值域（mMax-mMin）等分份数
	private int mPortion = 10; // 一个mSection等分份数
	private int mRealTimeValue = mMin; // 实时读数
	private int mStrokeWidth; // 画笔宽度
	private int mLength1; // 长刻度的相对圆弧的长度
	private int mLength2; // 刻度读数顶部的相对圆弧的长度
	private int mPadding;//内边距
	private float mCenterX, mCenterY; // 圆心坐标
	private Paint mPaint;//画笔
	private Path mPath;
	private RectF mRectFArc;//最外层矩形
	private RectF mRectFInnerArc;//内层矩形
	private Rect mRectText;//刻度文字矩形框
	private String[] mTexts;
	public SpeedBoardView(Context context) {
		this(context, null);
	}

	public SpeedBoardView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SpeedBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	private void init(){
		mStrokeWidth = dp2px(1);
		mLength1 = dp2px(8) + mStrokeWidth;
		mLength2 = mLength1 + dp2px(15);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeCap(Paint.Cap.ROUND);

		mPath = new Path();
		mRectFArc = new RectF();
		mRectFInnerArc = new RectF();
		mRectText = new Rect();

		mTexts = new String[mSection + 1]; // 需要显示mSection + 1个刻度读数
		for (int i = 0; i < mTexts.length; i++) {
			int n = (mMax - mMin) / mSection;
			mTexts[i] = String.valueOf(mMin + i * n);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mPadding = Math.max(
			Math.max(getPaddingLeft(), getPaddingTop()),
			Math.max(getPaddingRight(), getPaddingBottom())
		);
		setPadding(mPadding, mPadding, mPadding, mPadding);
		//你希望的宽度和测量的宽度通过resolveSize告诉你实际宽度是多少
		int width = resolveSize(dp2px(200), widthMeasureSpec);
		//通过实际宽度得到圆半径
		mRadius = (width - mPadding * 2 - mStrokeWidth * 2) / 2;
		int height = mRadius * 2 + mStrokeWidth * 2;
        //设置画笔字体大小
		mPaint.setTextSize(sp2px(16));
		setMeasuredDimension(width, height + getPaddingTop() + getPaddingBottom());

		mCenterX = mCenterY = getMeasuredWidth() / 2f;

		mRectFArc.set(
			getPaddingLeft() + mStrokeWidth,
			getPaddingTop() + mStrokeWidth,
			getMeasuredWidth() - getPaddingRight() - mStrokeWidth,
			getMeasuredWidth() - getPaddingBottom() - mStrokeWidth
		);

		mPaint.setTextSize(sp2px(10));
		//测量文字的宽高
		mPaint.getTextBounds("0", 0, "0".length(), mRectText);
		mRectFInnerArc.set(
			getPaddingLeft() + mLength2 + mRectText.height(),
			getPaddingTop() + mLength2 + mRectText.height(),
			getMeasuredWidth() - getPaddingRight() - mLength2 - mRectText.height(),
			getMeasuredWidth() - getPaddingBottom() - mLength2 - mRectText.height()
		);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		/**
		 * 画圆弧
		 */
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(10);
		mPaint.setColor(Color.parseColor("#c0c0c0"));
		canvas.drawOval(mRectFArc,mPaint);
//		canvas.drawArc(mRectFArc, mStartAngle, mSweepAngle, false, mPaint);

		/**
		 * 画长刻度
		 * 画好起始角度的一条刻度后通过canvas绕着原点旋转来画剩下的长刻度
		 */
		double cos = Math.cos(Math.toRadians(mStartAngle - 180));
		double sin = Math.sin(Math.toRadians(mStartAngle - 180));
		float x0 = (float) (mPadding + mStrokeWidth + mRadius * (1 - cos));
		float y0 = (float) (mPadding + mStrokeWidth + mRadius * (1 - sin));
		float x1 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1) * cos);
		float y1 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1) * sin);

		canvas.save();
		canvas.drawLine(x0, y0, x1, y1, mPaint);
		float angle = mSweepAngle * 1f / mSection;
		for (int i = 0; i < mSection; i++) {
			canvas.rotate(angle, mCenterX, mCenterY);
			canvas.drawLine(x0, y0, x1, y1, mPaint);
		}
		canvas.restore();
		/**
		 * 画短刻度
		 * 同样采用canvas的旋转原理
		 */
		canvas.save();
		mPaint.setStrokeWidth(2);
		float x2 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1 / 2f) * cos);
		float y2 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1 / 2f) * sin);
		canvas.drawLine(x0, y0, x2, y2, mPaint);
		angle = mSweepAngle * 1f / (mSection * mPortion);
		for (int i = 1; i < mSection * mPortion; i++) {
			canvas.rotate(angle, mCenterX, mCenterY);
			if (i % mPortion == 0) { // 避免与长刻度画重合
				continue;
			}
			canvas.drawLine(x0, y0, x2, y2, mPaint);
		}
		canvas.restore();

		/**
		 * 画长刻度读数
		 * 添加一个圆弧path，文字沿着path绘制
		 */
//		mPaint.setTextSize(sp2px(20));
//		mPaint.setTextAlign(Paint.Align.LEFT);
//		mPaint.setStyle(Paint.Style.FILL);
//		for (int i = 0; i < mTexts.length; i++) {
//			mPaint.getTextBounds(mTexts[i], 0, mTexts[i].length(), mRectText);
//			// 粗略把文字的宽度视为圆心角2*θ对应的弧长，利用弧长公式得到θ，下面用于修正角度
//			float θ = (float) (180 * mRectText.width() / 2 /
//				(Math.PI * (mRadius - mLength2 - mRectText.height()/2)));
//
//			mPath.reset();
//			mPath.addArc(
//				mRectFInnerArc,
//				mStartAngle + i * (mSweepAngle / mSection) - θ, // 正起始角度减去θ使文字居中对准长刻度
//				mSweepAngle
//			);
//			canvas.drawTextOnPath(mTexts[i], mPath, 0, 0, mPaint);
//		}
		/**
		 * 画长刻度读数
		 */
		mPaint.setTextSize(sp2px(20));
		mPaint.setStyle(Paint.Style.FILL);
		float α;
		float[] p;
		angle = mSweepAngle * 1f / mSection;
		for (int i = 0; i <= mSection; i++) {
			α = mStartAngle + angle * i;
			p = getCoordinatePoint(mRadius - mLength2, α);
			if (α % 360 > 135 && α % 360 < 225) {
				mPaint.setTextAlign(Paint.Align.LEFT);
			} else if ((α % 360 >= 0 && α % 360 < 45) || (α % 360 > 315 && α % 360 <= 360)) {
				mPaint.setTextAlign(Paint.Align.RIGHT);
			} else {
				mPaint.setTextAlign(Paint.Align.CENTER);
			}
//			mPaint.getTextBounds(mHeaderText, 0, mTexts[i].length(), mRectText);
			int txtH = mRectText.height();
			if (i <= 1 || i >= mSection - 1) {
				canvas.drawText(mTexts[i], p[0], p[1] + txtH / 2, mPaint);
			} else if (i == 3) {
				canvas.drawText(mTexts[i], p[0] + txtH / 2, p[1] + txtH, mPaint);
			} else if (i == mSection - 3) {
				canvas.drawText(mTexts[i], p[0] - txtH / 2, p[1] + txtH, mPaint);
			} else {
				canvas.drawText(mTexts[i], p[0], p[1] + txtH, mPaint);
			}
		}
	}

	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
			Resources.getSystem().getDisplayMetrics());
	}

	private int sp2px(int sp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
			Resources.getSystem().getDisplayMetrics());
	}

	public float[] getCoordinatePoint(int radius, float angle) {
		float[] point = new float[2];

		double arcAngle = Math.toRadians(angle); //将角度转换为弧度
		if (angle < 90) {
			point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
			point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
		} else if (angle == 90) {
			point[0] = mCenterX;
			point[1] = mCenterY + radius;
		} else if (angle > 90 && angle < 180) {
			arcAngle = Math.PI * (180 - angle) / 180.0;
			point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
			point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
		} else if (angle == 180) {
			point[0] = mCenterX - radius;
			point[1] = mCenterY;
		} else if (angle > 180 && angle < 270) {
			arcAngle = Math.PI * (angle - 180) / 180.0;
			point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
			point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
		} else if (angle == 270) {
			point[0] = mCenterX;
			point[1] = mCenterY - radius;
		} else {
			arcAngle = Math.PI * (360 - angle) / 180.0;
			point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
			point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
		}

		return point;
	}
}
