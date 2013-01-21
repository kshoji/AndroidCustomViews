package jp.kshoji.android.view;

import jp.kshoji.android.listener.OnBiaxialSeekBarChangeListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class BiaxialSeekBar extends View {
	private int x_min;
	private int x_max;
	private int x_progress;
	private int y_min;
	private int y_max;
	private int y_progress;
	private int pointerSize;
	private int pointerColor;
	private Paint pointerPaint;

	private RectF pointerRectangle = new RectF();
	private boolean onPointer;
	private OnBiaxialSeekBarChangeListener onBiaxialSeekBarChangeListener;
	
	public BiaxialSeekBar(Context context) {
		super(context);
		init(null, 0);
	}
	
	public BiaxialSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}
	
	public BiaxialSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.BiaxialSeekBar, defStyle, 0);
		
		x_min = typedArray.getInt(R.styleable.BiaxialSeekBar_x_min, 0);
		x_max = typedArray.getInt(R.styleable.BiaxialSeekBar_x_max, 100);
		x_progress = typedArray.getInt(R.styleable.BiaxialSeekBar_x_progress, 50);
		y_min = typedArray.getInt(R.styleable.BiaxialSeekBar_y_min, 0);
		y_max = typedArray.getInt(R.styleable.BiaxialSeekBar_y_max, 100);
		y_progress = typedArray.getInt(R.styleable.BiaxialSeekBar_y_progress, 50);
		pointerSize = typedArray.getDimensionPixelSize(R.styleable.BiaxialSeekBar_pointerSize, 44);
		pointerColor = typedArray.getColor(R.styleable.BiaxialSeekBar_pointerColor, Color.BLUE);
		
		pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		pointerPaint.setStyle(Paint.Style.FILL);
		pointerPaint.setColor(pointerColor);
		pointerPaint.setStrokeWidth(pointerSize);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected synchronized void onDraw(Canvas canvas) {
		pointerRectangle.left = ((float)getWidth() - pointerSize) * x_progress / (1 + x_max - x_min);
		pointerRectangle.top = ((float)getHeight() - pointerSize) * y_progress / (1 + y_max - y_min);
		pointerRectangle.right = pointerRectangle.left + (float)pointerSize;
		pointerRectangle.bottom = pointerRectangle.top + (float)pointerSize;

		canvas.drawOval(pointerRectangle, pointerPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX() - pointerSize / 2;
		float y = event.getY() - pointerSize / 2;

		x = x * x_max / (getWidth() - pointerSize);
		y = y * y_max / (getHeight() - pointerSize);
		if (x > x_max) {
			x = x_max;
		}
		if (y > y_max) {
			y = y_max;
		}
		if (x < x_min) {
			x = x_min;
		}
		if (y < y_min) {
			y = y_min;
		}
		
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				onPointer = true;
				if (onBiaxialSeekBarChangeListener != null) {
					onBiaxialSeekBarChangeListener.onStartTrackingTouch(this);
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (onPointer) {
					x_progress = (int)x;
					y_progress = (int)y;
					invalidate();
					
					if (onBiaxialSeekBarChangeListener != null) {
						onBiaxialSeekBarChangeListener.onProgressChanged(this, getXProgress(), getYProgress(), true);
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				onPointer = false;
				
				x_progress = (int)x;
				y_progress = (int)y;
				invalidate();
				
				if (onBiaxialSeekBarChangeListener != null) {
					onBiaxialSeekBarChangeListener.onProgressChanged(this, getXProgress(), getYProgress(), true);
					onBiaxialSeekBarChangeListener.onStopTrackingTouch(this);
					
				}
				break;
		}
		return true;
	}

	public int getXProgress() {
		return x_progress;
	}
	
	public int getXMin() {
		return x_min;
	}

	public int getXMax() {
		return x_max;
	}
	
	private int getYProgress() {
		return y_progress;
	}
	
	public int getYMin() {
		return y_min;
	}
	
	public int getYMax() {
		return y_max;
	}
	
	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState savedState = new SavedState(superState);
		savedState.pointerColor = pointerColor;
		savedState.pointerSize = pointerSize;
		savedState.x_max = x_max;
		savedState.x_min = x_min;
		savedState.x_progress = x_progress;
		savedState.y_max = y_max;
		savedState.y_min = y_min;
		savedState.y_progress = y_progress;
		return savedState;
	}
	
	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state != null && state instanceof SavedState) {
			SavedState savedState = (SavedState) state;
			super.onRestoreInstanceState(savedState.getSuperState());
			
			pointerSize = savedState.pointerSize;
			pointerColor = savedState.pointerColor;
			x_progress = savedState.x_progress;
			x_min = savedState.x_min;
			x_max = savedState.x_max;
			y_progress = savedState.y_progress;
			y_min = savedState.y_min;
			y_max = savedState.y_max;
		}
	}
	
	/**
	 * @param onBiaxialSeekBarChangeListener the onBiaxialSeekBarChangeListener to set
	 */
	public void setOnBixialSeekBarChangeListener(OnBiaxialSeekBarChangeListener onBiaxialSeekBarChangeListener) {
		this.onBiaxialSeekBarChangeListener = onBiaxialSeekBarChangeListener;
	}
	
	protected static class SavedState extends BaseSavedState {
		int pointerSize;
		int pointerColor;
		int x_progress;
		int x_min;
		int x_max;
		int y_progress;
		int y_min;
		int y_max;
		
		SavedState(Parcelable superState) {
			super(superState);
		}
		
		SavedState(Parcel in) {
			super(in);
			pointerSize = in.readInt();
			pointerColor = in.readInt();
			x_progress = in.readInt();
			x_min = in.readInt();
			x_max = in.readInt();
			y_progress = in.readInt();
			y_min = in.readInt();
			y_max = in.readInt();
		}
		
		@Override
		public void writeToParcel(Parcel destination, int flags) {
			super.writeToParcel(destination, flags);
			destination.writeInt(pointerSize);
			destination.writeInt(pointerColor);
			destination.writeInt(x_progress);
			destination.writeInt(x_min);
			destination.writeInt(x_max);
			destination.writeInt(y_progress);
			destination.writeInt(y_min);
			destination.writeInt(y_max);
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
