package net.osmand.plus.headupdisplay;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import net.osmand.AndroidUtils;
import net.osmand.GPXUtilities;
import net.osmand.PlatformUtil;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.UiUtilities;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.base.BottomSheetBehaviourDialogFragment;
import net.osmand.plus.base.bottomsheetmenu.BaseBottomSheetItem;
import net.osmand.plus.base.bottomsheetmenu.simpleitems.DividerItem;
import net.osmand.plus.base.bottomsheetmenu.simpleitems.TitleItem;
import net.osmand.plus.helpers.AndroidUiHelper;
import net.osmand.plus.helpers.GpxUiHelper.GPXInfo;
import net.osmand.plus.importfiles.ImportHelper;
import net.osmand.plus.importfiles.ImportHelper.OnGpxImportCompleteListener;
import net.osmand.plus.settings.backend.ApplicationMode;
import net.osmand.plus.widgets.TextViewEx;

import org.apache.commons.logging.Log;

import java.util.List;

import static net.osmand.plus.settings.bottomsheets.SingleSelectPreferenceBottomSheet.SELECTED_ENTRY_INDEX_KEY;
//import static net.osmand.plus.measurementtool.SelectFileBottomSheet.Mode.OPEN_TRACK;

public class HeadUpDisplaySettingsBottomSheet extends BottomSheetBehaviourDialogFragment //BasePreferenceBottomSheet
		implements SeekBar.OnSeekBarChangeListener {

	public static final String TAG = HeadUpDisplaySettingsBottomSheet.class.getSimpleName();
	private static final Log LOG = PlatformUtil.getLog(HeadUpDisplaySettingsBottomSheet.class);
	public static final int BOTTOM_SHEET_HEIGHT_DP = 427;
	private static final int OPEN_GPX_DOCUMENT_REQUEST = 1001;

	protected View mainView;
	//protected GpxTrackAdapter adapter;
	private ImportHelper importHelper;
	private MapActivity mapActivity;
	private SwitchCompat hudEnableSwitch;

	public HeadUpDisplaySettingsBottomSheet(MapActivity mapActivity) {

		this.mapActivity = mapActivity;
	}


	@Override
	public void createMenuItems(Bundle savedInstanceState) {
		app = requiredMyApplication();//getMyApplication()
		importHelper = new ImportHelper((AppCompatActivity) getActivity(), getMyApplication(), null);
		final int themeRes = nightMode ? R.style.OsmandDarkTheme : R.style.OsmandLightTheme;
		mainView = UiUtilities.getInflater(getContext(), nightMode)
				.inflate(R.layout.bottom_sheet_head_up_display_scale, null);
		items.add(new TitleItem(getString(R.string.head_up_display_title)));
		verticalScale = app.getSettings().HEAD_UP_DISPLAY_SCALE.get();
		tvSeekBarValueLabel = mainView.findViewById(R.id.tv_seek_bar_value_label);
		seekBarVerticalScale = mainView.findViewById(R.id.seek_bar_vertical_scale);
		hudEnableSwitch = (SwitchCompat)mainView.findViewById(R.id.hud_enable_switch);
		hudEnableSwitch.setChecked(app.getSettings().HEAD_UP_DISPLAY.get());
		hudEnableSwitch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean checked = hudEnableSwitch.isChecked();
				hudEnableSwitch.setChecked(checked);
				mapActivity.setHUD();
				//updateBottomButtons();
			}
		});
		setProfileColorToSeekBar();
		seekBarVerticalScale.setOnSeekBarChangeListener(this);
		updateViews();
		//items.add(createBottomSheetItem());

		//OsmandApplication app = getMyApplication();
		if (app == null) {
			return;
		}

		items.add(new BaseBottomSheetItem.Builder().setCustomView(mainView).create());
	}

	@Override
	protected int getPeekHeight() {
		return AndroidUtils.dpToPx(requiredMyApplication(), BOTTOM_SHEET_HEIGHT_DP);
	}

	public static void showInstance( MapActivity map) {
		FragmentManager fragmentManager = map.getSupportFragmentManager();
		if (!fragmentManager.isStateSaved()) {
			HeadUpDisplaySettingsBottomSheet fragment = new HeadUpDisplaySettingsBottomSheet(map);
			fragment.setRetainInstance(true);
			fragment.show(fragmentManager, TAG);
		}
	}


	@Override
	protected int getDismissButtonTextId() {
		return R.string.shared_string_cancel;
	}

	private OsmandApplication app;
	private TextViewEx tvSeekBarValueLabel;
	private SeekBar seekBarVerticalScale;
	private int verticalScale = -1;
	private boolean collapsed = true;
	private BaseBottomSheetItem createBottomSheetItem() {
		tvSeekBarValueLabel = mainView.findViewById(R.id.tv_seek_bar_value_label);
		seekBarVerticalScale = mainView.findViewById(R.id.seek_bar_vertical_scale);
		setProfileColorToSeekBar();
		seekBarVerticalScale.setOnSeekBarChangeListener(this);
		updateViews();

		return new BaseBottomSheetItem.Builder()
				.setCustomView(mainView)
				.create();
	}
	private void setProfileColorToSeekBar() {
		int color = ContextCompat.getColor(app, getAppMode().getIconColorInfo().getColor(nightMode));

		LayerDrawable seekBarProgressLayer =
				(LayerDrawable) ContextCompat.getDrawable(app, R.drawable.seekbar_progress_announcement_time);

		GradientDrawable background = (GradientDrawable) seekBarProgressLayer.findDrawableByLayerId(R.id.background);
		background.setColor(color);
		background.setAlpha(70);

		GradientDrawable progress = (GradientDrawable) seekBarProgressLayer.findDrawableByLayerId(R.id.progress);
		progress.setColor(color);
		Drawable clippedProgress = new ClipDrawable(progress, Gravity.CENTER_VERTICAL | Gravity.START, 1);

		seekBarVerticalScale.setProgressDrawable(new LayerDrawable(new Drawable[] {
				background, clippedProgress
		}));

		LayerDrawable seekBarThumpLayer =
				(LayerDrawable) ContextCompat.getDrawable(app, R.drawable.seekbar_thumb_announcement_time);
		GradientDrawable thump = (GradientDrawable) seekBarThumpLayer.findDrawableByLayerId(R.id.thump);
		thump.setColor(color);
		seekBarVerticalScale.setThumb(thump);
	}
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (progress != verticalScale) {
			verticalScale = progress;
			updateViews();
			app.getSettings().HEAD_UP_DISPLAY_SCALE.set(verticalScale);
			mapActivity.setHUD();
			mapActivity.updateApplicationModeSettings();
		}
	}
	private void updateViews() {
		hudEnableSwitch.setChecked(app.getSettings().HEAD_UP_DISPLAY.get());
		seekBarVerticalScale.setProgress(verticalScale);
		tvSeekBarValueLabel.setText(String.format("%d%%", verticalScale));
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
	@Override
	protected void onRightBottomButtonClick() {
		app.getSettings().HEAD_UP_DISPLAY_SCALE.set(verticalScale);
		mapActivity.setHUD();
		//mapActivity.updateApplicationModeSettings();
		mapActivity.recreate();
		dismiss();
	}
	@Override
	protected void onDismissButtonClickAction() {
		app.getSettings().HEAD_UP_DISPLAY.set(false);
		mapActivity.setHUD();
		mapActivity.updateApplicationModeSettings();
		dismiss();
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SELECTED_ENTRY_INDEX_KEY, verticalScale);
	}

	@Override
	protected int getRightBottomButtonTextId() {
		return R.string.shared_string_apply;
	}


	private ApplicationMode appMode;
	public ApplicationMode getAppMode() {
		return appMode != null ? appMode : requiredMyApplication().getSettings().getApplicationMode();
	}
}
