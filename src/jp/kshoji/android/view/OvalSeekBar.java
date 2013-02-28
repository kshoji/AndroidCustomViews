package jp.kshoji.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class OvalSeekBar extends SeekBar {
	private Paint wheelPaint;
	private Paint pointerPaint;
	private Paint textPaint;
	private float wheelRadius;
	private RectF wheelRectangle = new RectF();
	private boolean onPointer = false;
	
	// view configuration
	private int wheelSize;
	private int wheelColor;
	private int wheelValueSeparatorSizeAngle = 5;
	private int pointerSizeAngle;
	private int pointerColor;
	private int pointerType;
	private boolean isClickableWheel;
	private boolean isWheelDirectionClockwise;
	private int textSize;
	private int textColor;
	private int progress;
	private int min;
	private int max;
	private int wheelZeroAngle;
	private int wheelWholeAngle;
	private Drawable progressDrawable;
	private int progressDrawableSize;

	
	private OnSeekBarChangeListener onSeekBarChangeListener;
	
	public OvalSeekBar(Context context) {
		super(context);
		init(null, 0);
	}
	
	public OvalSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}
	
	public OvalSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}
	
	private void init(AttributeSet attrs, int defStyle) {
		final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.OvalSeekBar, defStyle, 0);
		
		wheelSize = typedArray.getDimensionPixelSize(R.styleable.OvalSeekBar_wheelSize, 16);
		wheelColor = typedArray.getColor(R.styleable.OvalSeekBar_wheelColor, Color.BLACK);
		wheelValueSeparatorSizeAngle = typedArray.getInt(R.styleable.OvalSeekBar_wheelValueSeparatorSizeAngle, 0);
		wheelZeroAngle = typedArray.getInt(R.styleable.OvalSeekBar_wheelZeroAngle, 0);
		wheelWholeAngle = typedArray.getInt(R.styleable.OvalSeekBar_wheelWholeAngle, 270);
		
		pointerSizeAngle = typedArray.getInt(R.styleable.OvalSeekBar_pointerSizeAngle, 48);
		pointerColor = typedArray.getColor(R.styleable.OvalSeekBar_pointerColor, Color.CYAN);
		pointerType = typedArray.getInt(R.styleable.OvalSeekBar_pointerType, 0);
		isWheelDirectionClockwise = typedArray.getInt(R.styleable.OvalSeekBar_wheelDirection, 0) == 0;
		isClickableWheel = typedArray.getBoolean(R.styleable.OvalSeekBar_clickableWheel, false);
		progressDrawableSize = typedArray.getInt(R.styleable.OvalSeekBar_progressDrawableSize, 100);
		
		min = typedArray.getInt(R.styleable.OvalSeekBar_min, 0);

		typedArray.recycle();
		
		TypedArray androidTypedArray = getContext().obtainStyledAttributes(attrs, new int[] { android.R.attr.textSize, android.R.attr.textColor, android.R.attr.max, android.R.attr.progress, android.R.attr.progressDrawable});
		textSize = androidTypedArray.getDimensionPixelSize(0, 12);
		textColor = androidTypedArray.getColor(1, Color.BLUE);
		max = androidTypedArray.getInt(2, 100);
		progress = androidTypedArray.getInt(3, 0);
		progressDrawable = androidTypedArray.getDrawable(4);

		androidTypedArray.recycle();
		
		// reset progress value
		setProgress(progress);
		
		wheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		wheelPaint.setStyle(Paint.Style.STROKE);
		wheelPaint.setColor(wheelColor);
		wheelPaint.setStrokeWidth(wheelSize);
		
		pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		pointerPaint.setStyle(Paint.Style.STROKE);
		pointerPaint.setColor(pointerColor);
		pointerPaint.setStrokeWidth(wheelSize);
		
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextSize(textSize);
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setColor(textColor);
	}
	
	private void drawTextCenter(Canvas canvas, int x, int y, String textToDraw) {
		Rect bounds = new Rect();
		textPaint.getTextBounds(textToDraw, 0, textToDraw.length(), bounds);
		canvas.drawText(textToDraw, x / 2, y / 2 + (bounds.height() * 0.3f), textPaint);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected synchronized void onDraw(Canvas canvas) {
		wheelRadius = getWidth() * 0.5f;
		wheelRectangle.set(-wheelRadius + wheelSize / 2, -wheelRadius + wheelSize / 2, wheelRadius - wheelSize / 2, wheelRadius - wheelSize / 2);
		
		canvas.translate(wheelRadius, wheelRadius);
		
		for (int i = min; i <= max; i++) {
			if (isWheelDirectionClockwise) {
				float minAngle = wheelZeroAngle;
				double pointerAngle = wheelWholeAngle / (1.0 + max - min);
				
				if (pointerType == 0) {
					// pointer
					if (i == getProgress()) {
						canvas.drawArc(wheelRectangle, (float) (minAngle + pointerAngle * (i - min) + pointerAngle / 2 - pointerSizeAngle / 2), pointerSizeAngle, false, pointerPaint);
					} else {
						canvas.drawArc(wheelRectangle, (float) (minAngle + pointerAngle * (i - min)), (float) pointerAngle - wheelValueSeparatorSizeAngle, false, wheelPaint);
					}
				} else {
					// value
					if (i <= getProgress()) {
						canvas.drawArc(wheelRectangle, (float) (minAngle + pointerAngle * (i - min)), (float) pointerAngle - wheelValueSeparatorSizeAngle, false, pointerPaint);
					} else {
						canvas.drawArc(wheelRectangle, (float) (minAngle + pointerAngle * (i - min)), (float) pointerAngle - wheelValueSeparatorSizeAngle, false, wheelPaint);
					}
				}
			} else {
				float minAngle = wheelZeroAngle + wheelWholeAngle;
				double pointerAngle = wheelWholeAngle / (1.0 + max - min);
				
				if (pointerType == 0) {
					// pointer
					if (i == getProgress()) {
						canvas.drawArc(wheelRectangle, (float) (minAngle + pointerAngle * (max - i)), (float) pointerAngle - wheelValueSeparatorSizeAngle, false, pointerPaint);
					} else {
						canvas.drawArc(wheelRectangle, (float) (minAngle + pointerAngle * (max - i)), (float) pointerAngle - wheelValueSeparatorSizeAngle, false, wheelPaint);
					}
				} else {
					// value
					if (i <= getProgress()) {
						canvas.drawArc(wheelRectangle, (float) (minAngle + pointerAngle * (max - i)), (float) pointerAngle - wheelValueSeparatorSizeAngle, false, pointerPaint);
					} else {
						canvas.drawArc(wheelRectangle, (float) (minAngle + pointerAngle * (max - i)), (float) pointerAngle - wheelValueSeparatorSizeAngle, false, wheelPaint);
					}
				}
			}
		}
	
		if (progressDrawable != null) {
			double pointerAngle = -(getProgress() - min + 0.5) * wheelWholeAngle / (1.0 + max - min);
			int x = (int)(-Math.cos(pointerAngle * Math.PI / 180.0) * wheelRectangle.width() / 2);
			int y;
			if (isWheelDirectionClockwise) {
				y = (int)(Math.sin(pointerAngle * Math.PI / 180.0) * wheelRectangle.height() / 2);
			} else {
				y = (int)(-Math.sin(pointerAngle * Math.PI / 180.0) * wheelRectangle.height() / 2);
			}
			progressDrawable.setBounds(
					(int)(x - wheelRectangle.width() * progressDrawableSize / 1000),
					(int)(y - wheelRectangle.height() * progressDrawableSize / 1000),
					(int)(x + wheelRectangle.width() * progressDrawableSize / 1000),
					(int)(y + wheelRectangle.height() * progressDrawableSize / 1000));
			progressDrawable.draw(canvas);
		}
		
		drawTextCenter(canvas, 0, 0, Integer.toString(getProgress()));
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
		
		// angle is radian
		double angle;
		
		if (isWheelDirectionClockwise) {
			angle = Math.atan2(y, x) - Math.PI * wheelZeroAngle / 180d + Math.PI * 5.0 + Math.PI * (360.0 - wheelWholeAngle) / 360.0;
			while (angle > Math.PI) {
				angle -= Math.PI * 2.0;
			}
			unit = angle * 360.0 / wheelWholeAngle / (2.0 * Math.PI) + 0.5;
		} else {
			angle = Math.atan2(y, x) - Math.PI * wheelZeroAngle / 180d + Math.PI * 5.0 - Math.PI * (360.0 - wheelWholeAngle) / 360.0;
			while (angle > Math.PI) {
				angle -= Math.PI * 2.0;
			}
			unit = 0.5 - angle * 360.0 / wheelWholeAngle / (2.0 * Math.PI);
		}
		
		int result = (int) Math.floor((1.0 + max - min) * unit + min);
		if (result > max) {
			result = max;
		}
		if (result < min) {
			result = min;
		}
		
		return result;
	}
	
	@Override
	public synchronized int getProgress() {
		return progress;
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
				int clickedProgress = getClickedProgress(x - wheelRadius, y - wheelRadius);
				
				if (isClickableWheel || pointerType == 1 || clickedProgress == getProgress()) {
					onPointer = true;
					updateProgress(x - wheelRadius, y - wheelRadius);
					
					invalidate();
					
					if (onSeekBarChangeListener != null) {
						onSeekBarChangeListener.onStartTrackingTouch(this);
					}
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
		savedState.isClickableWheel = isClickableWheel;
		savedState.isWheelDirectionClockwise = isWheelDirectionClockwise;
		savedState.max = max;
		savedState.min = min;
		savedState.pointerColor = pointerColor;
		savedState.pointerSizeAngle = pointerSizeAngle;
		savedState.pointerType = pointerType;
		savedState.progress = progress;
		savedState.textColor = textColor;
		savedState.textSize = textSize;
		savedState.wheelColor = wheelColor;
		savedState.wheelSize = wheelSize;
		savedState.wheelWholeAngle = wheelWholeAngle;
		savedState.wheelZeroAngle = wheelZeroAngle;
		return savedState;
	}
	
	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state != null && state instanceof SavedState) {
			SavedState savedState = (SavedState) state;
			super.onRestoreInstanceState(savedState.getSuperState());
			
			isClickableWheel = savedState.isClickableWheel;
			isWheelDirectionClockwise = savedState.isWheelDirectionClockwise;
			max = savedState.max;
			min = savedState.min;
			pointerColor = savedState.pointerColor;
			pointerSizeAngle = savedState.pointerSizeAngle;
			pointerType = savedState.pointerType;
			progress = savedState.progress;
			textColor = savedState.textColor;
			textSize = savedState.textSize;
			wheelColor = savedState.wheelColor;
			wheelSize = savedState.wheelSize;
			wheelWholeAngle = savedState.wheelWholeAngle;
			wheelZeroAngle = savedState.wheelZeroAngle;
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
		int wheelSize;
		int wheelColor;
		int pointerSizeAngle;
		int pointerColor;
		int pointerType;
		boolean isClickableWheel;
		boolean isWheelDirectionClockwise;
		int textSize;
		int textColor;
		int progress;
		int min;
		int max;
		int wheelZeroAngle;
		int wheelWholeAngle;
		
		SavedState(Parcelable superState) {
			super(superState);
		}
		
		SavedState(Parcel in) {
			super(in);
			wheelSize = in.readInt();
			wheelColor = in.readInt();
			pointerSizeAngle = in.readInt();
			pointerColor = in.readInt();
			pointerType = in.readInt();
			isClickableWheel = in.readInt() == 1;
			isWheelDirectionClockwise = in.readInt() == 1;
			textSize = in.readInt();
			textColor = in.readInt();
			progress = in.readInt();
			min = in.readInt();
			max = in.readInt();
			wheelZeroAngle = in.readInt();
			wheelWholeAngle = in.readInt();
		}
		
		@Override
		public void writeToParcel(Parcel destination, int flags) {
			super.writeToParcel(destination, flags);
			destination.writeInt(wheelSize);
			destination.writeInt(wheelColor);
			destination.writeInt(pointerSizeAngle);
			destination.writeInt(pointerColor);
			destination.writeInt(pointerType);
			destination.writeInt(isClickableWheel ? 1 : 0);
			destination.writeInt(isWheelDirectionClockwise ? 1 : 0);
			destination.writeInt(textSize);
			destination.writeInt(textColor);
			destination.writeInt(progress);
			destination.writeInt(min);
			destination.writeInt(max);
			destination.writeInt(wheelZeroAngle);
			destination.writeInt(wheelWholeAngle);
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
