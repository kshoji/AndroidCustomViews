package jp.kshoji.android.listener;

import jp.kshoji.android.view.BiaxialSeekBar;

public interface OnBiaxialSeekBarChangeListener {

	void onStartTrackingTouch(BiaxialSeekBar biaxialSeekBar);

	void onStopTrackingTouch(BiaxialSeekBar biaxialSeekBar);

	void onProgressChanged(BiaxialSeekBar biaxialSeekBar, int progressX, int progressY, boolean b);
	
}
