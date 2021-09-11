package net.osmand.plus.quickaction.actions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.helpers.enums.DayNightMode;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.quickaction.QuickAction;
import net.osmand.plus.quickaction.QuickActionType;

public class HUDAction extends QuickAction {

	public static final QuickActionType TYPE = new QuickActionType(35,
			"HUD.switch", HUDAction.class).
			nameRes(R.string.head_up_display_switch)
			.iconRes(R.drawable.ic_action_head_up_display).nonEditable().
			category(QuickActionType.CONFIGURE_MAP);

	public HUDAction() {super(TYPE);}

	public HUDAction(QuickAction quickAction) {super(quickAction);}

	@Override
	public void execute(MapActivity activity) {
		boolean hud = activity.getMyApplication().getSettings().HEAD_UP_DISPLAY.get();
		activity.getMyApplication().getSettings().HEAD_UP_DISPLAY.set(!hud);
		activity.setHUD();
		activity.recreate();
	}

	@Override
	public void drawUI(ViewGroup parent, MapActivity activity) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.quick_action_with_text, parent, false);
		((TextView) view.findViewById(R.id.text))
				.setText(R.string.quick_action_head_up_display_switch_descr);
		parent.addView(view);
	}

	@Override
	public int getIconRes(Context context) {
//		if (context instanceof MapActivity
//			&& ((MapActivity) context).getMyApplication().getDaynightHelper().isNightMode()) {
//			return R.drawable.ic_action_head_up_display;
//		}
		return R.drawable.ic_action_head_up_display;
	}

	@Override
	public String getActionText(OsmandApplication application) {
		if (application.getSettings().HEAD_UP_DISPLAY.get()) {
			return application.getString(R.string.head_up_display_action_disable);
		} else {
			return application.getString(R.string.head_up_display_action_enable);
		}
	}
}
