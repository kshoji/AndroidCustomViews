package jp.kshoji.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class ContinuousOvalSeekBar extends SeekBar {
	private Paint wheelPaint;
	private Paint textPaint;
	private float wheelRadius;
	private RectF wheelRectangle = new RectF();
	private boolean onPointer = false;
	
	// view configuration
	private int wheelSize;
	private int wheelColor;
	private boolean isWheelDirectionClockwise;
	private int textSize;
	private int textColor;
	private int progress;
	private int min;
	private int max;
	
	private OnSeekBarChangeListener onSeekBarChangeListener;
	
	public ContinuousOvalSeekBar(Context context) {
		super(context);
		init(null, 0);
	}
	
	public ContinuousOvalSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}
	
	public ContinuousOvalSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}
	
	private void init(AttributeSet attrs, int defStyle) {
		final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.OvalSeekBar, defStyle, 0);
		
		wheelSize = typedArray.getDimensionPixelSize(R.styleable.ContinuousOvalSeekBar_wheelSize, 16);
		wheelColor = typedArray.getColor(R.styleable.ContinuousOvalSeekBar_wheelColor, Color.BLACK);
		
		isWheelDirectionClockwise = typedArray.getInt(R.styleable.ContinuousOvalSeekBar_wheelDirection, 0) == 0;
		
		min = typedArray.getInt(R.styleable.ContinuousOvalSeekBar_min, 0);
		typedArray.recycle();
		
		TypedArray androidTypedArray = getContext().obtainStyledAttributes(attrs, new int[] { android.R.attr.textSize, android.R.attr.textColor, android.R.attr.max, android.R.attr.progress });
		textSize = androidTypedArray.getDimensionPixelSize(0, 12);
		textColor = androidTypedArray.getColor(1, Color.BLUE);
		max = androidTypedArray.getInt(2, 100);
		progress = androidTypedArray.getInt(3, 0);
		androidTypedArray.recycle();
		
		// reset progress value
		setProgress(progress);
		
		wheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		wheelPaint.setStyle(Paint.Style.STROKE);
		wheelPaint.setColor(wheelColor);
		wheelPaint.setStrokeWidth(wheelSize);
		
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextSize(textSize);
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setColor(textColor);
		
		debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		debugPaint.setStyle(Paint.Style.STROKE);
		debugPaint.setColor(Color.GREEN);
		debugPaint.setStrokeWidth(0);
	}
	
	private void drawTextCenter(Canvas canvas, int x, int y, String textToDraw) {
		Rect bounds = new Rect();
		textPaint.getTextBounds(textToDraw, 0, textToDraw.length(), bounds);
		canvas.drawText(textToDraw, x / 2, y / 2 + (bounds.height() * 0.3f), textPaint);
	}
	
	Paint debugPaint;
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected synchronized void onDraw(Canvas canvas) {
		wheelRadius = getWidth() * 0.5f;
		wheelRectangle.set(-wheelRadius + wheelSize / 2, -wheelRadius + wheelSize / 2, wheelRadius - wheelSize / 2, wheelRadius - wheelSize / 2);
		
		canvas.translate(wheelRadius, wheelRadius);
		canvas.drawOval(wheelRectangle, wheelPaint);
		
		drawTextCenter(canvas, 0, 0, "" + getProgress());
	}
	
	int previousProgress;
	
	@Override
	public synchronized void setProgress(int progress) {
		previousProgress = progress; // NOTE not this.progress
		this.progress = progress;
		
		invalidate();
	}
	
	private synchronized void updateProgress(double x, double y) {
		previousProgress = progress;
		progress = getClickedProgress(x, y);
	}
	
	private int getClickedProgress(double x, double y) {
		// unit: 0.0 to 1.0
		double unit;
		
		if (isWheelDirectionClockwise) {
			double angle = Math.atan2(y, x);
			unit = angle / (2.0 * Math.PI);
		} else {
			double angle = Math.atan2(y, x);
			unit = -angle / (2.0 * Math.PI);
		}
		
		return (int) ((1.0 + max - min) * unit);
	}
	
	@Override
	public synchronized int getProgress() {
		int result;
		if (progress * previousProgress < (-(1 + max - min) * (1 + max - min) / 16.0)) {
			// Math.abs(progress) < ((1 + max - min) / 4.0)
			
			if (progress > previousProgress) {
				// progress: 63
				// previousProgress: -63
				result = (progress - (1 + max - min + previousProgress));
			} else {
				// progress: -63
				// previousProgress: +63
				result = ((1 + max - min + progress) - previousProgress);
			}
		} else {
			result = progress - previousProgress;
		}
		
		return result + (max + min) / 2;
	}
	
	@Override
	public synchronized int getMax() {
		return max;
	}
	
	public synchronized int getMin() {
		return min;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				onPointer = true;
				updateProgress(x - wheelRadius, y - wheelRadius);
				updateProgress(x - wheelRadius, y - wheelRadius);
				
				invalidate();
				
				if (onSeekBarChangeListener != null) {
					onSeekBarChangeListener.onStartTrackingTouch(this);
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (onPointer) {
					updateProgress(x - wheelRadius, y - wheelRadius);
					
					invalidate();
					
					if (onSeekBarChangeListener != null) {
						onSeekBarChangeListener.onProgressChanged(this, getProgress(), true);
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				onPointer = false;
				
				updateProgress(x - wheelRadius, y - wheelRadius);
				updateProgress(x - wheelRadius, y - wheelRadius);
				
				if (onSeekBarChangeListener != null) {
					onSeekBarChangeListener.onProgressChanged(this, getProgress(), true);
					onSeekBarChangeListener.onStopTrackingTouch(this);
				}
				break;
		}
		return true;
	}
	
	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		
		SavedState savedState = new SavedState(superState);
		savedState.isWheelDirectionClockwise = isWheelDirectionClockwise;
		savedState.max = max;
		savedState.min = min;
		savedState.progress = progress;
		savedState.textColor = textColor;
		savedState.textSize = textSize;
		savedState.wheelColor = wheelColor;
		savedState.wheelSize = wheelSize;
		
		return savedState;
	}
	
	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state != null && state instanceof SavedState) {
			SavedState savedState = (SavedState) state;
			super.onRestoreInstanceState(savedState.getSuperState());
			isWheelDirectionClockwise = savedState.isWheelDirectionClockwise;
			
			max = savedState.max;
			min = savedState.min;
			progress = savedState.progress;
			textColor = savedState.textColor;
			textSize = savedState.textSize;
			wheelColor = savedState.wheelColor;
			wheelSize = savedState.wheelSize;
		}
	}
	
	/**
	 * @param onSeekBarChangeListener the onSeekBarChangeListener to set
	 */
	@Override
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
		this.onSeekBarChangeListener = onSeekBarChangeListener;
	}
	
	protected static class SavedState extends BaseSavedState {
		boolean isWheelDirectionClockwise;
		int max;
		int min;
		int progress;
		int textColor;
		int textSize;
		int wheelColor;
		int wheelSize;
		
		SavedState(Parcelable superState) {
			super(superState);
		}
		
		SavedState(Parcel in) {
			super(in);
			isWheelDirectionClockwise = in.readInt() == 1;
			max = in.readInt();
			min = in.readInt();
			progress = in.readInt();
			textColor = in.readInt();
			textSize = in.readInt();
			wheelColor = in.readInt();
			wheelSize = in.readInt();
		}
		
		@Override
		public void writeToParcel(Parcel destination, int flags) {
			super.writeToParcel(destination, flags);
			destination.writeInt(isWheelDirectionClockwise ? 1 : 0);
			destination.writeInt(max);
			destination.writeInt(min);
			destination.writeInt(progress);
			destination.writeInt(textColor);
			destination.writeInt(textSize);
			destination.writeInt(wheelColor);
			destination.writeInt(wheelSize);
		}
		
		public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
			
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}
			
			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}
