package app.varlorg.ambilwarna;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AmbilWarnaDialog {
	public interface OnAmbilWarnaListener {
		void onCancel(AmbilWarnaDialog dialog);

		void onOk(AmbilWarnaDialog dialog, int color);

		void onReset(AmbilWarnaDialog dialog, int color);
	}

	final AlertDialog dialog;
	private final boolean supportsAlpha;
	final OnAmbilWarnaListener listener;
	final View viewHue;
	final AmbilWarnaSquare viewSatVal;
	final ImageView viewCursor;
	final ImageView viewAlphaCursor;
	final View viewOldColor;
	final View viewNewColor;
	final EditText viewNewColorHexa;
	final View viewAlphaOverlay;
	final ImageView viewTarget;
	final ImageView viewAlphaCheckered;
	final ViewGroup viewContainer;
	final float[] currentColorHsv = new float[3];
	int alpha;

	/**
	 * Create an AmbilWarnaDialog.
	 *
	 * @param context activity context
	 * @param color current color
	 * @param listener an OnAmbilWarnaListener, allowing you to get back error or OK
	 */
	public AmbilWarnaDialog(final Context context, int color, OnAmbilWarnaListener listener) {
		this(context, color, false, listener);
	}

	/**
	 * Create an AmbilWarnaDialog.
	 *
	 * @param context activity context
	 * @param color current color
	 * @param supportsAlpha whether alpha/transparency controls are enabled
	 * @param listener an OnAmbilWarnaListener, allowing you to get back error or OK
	 */
	public AmbilWarnaDialog(final Context context, int color, boolean supportsAlpha, OnAmbilWarnaListener listener) {
		this.supportsAlpha = supportsAlpha;
		this.listener = listener;

		if (!supportsAlpha) { // remove alpha if not supported
			color = color | 0xff000000;
		}

		Color.colorToHSV(color, currentColorHsv);
		alpha = Color.alpha(color);

		final View view = LayoutInflater.from(context).inflate(R.layout.ambilwarna_dialog, null);
		viewHue = view.findViewById(R.id.ambilwarna_viewHue);
		viewSatVal = (AmbilWarnaSquare) view.findViewById(R.id.ambilwarna_viewSatBri);
		viewCursor = (ImageView) view.findViewById(R.id.ambilwarna_cursor);
		viewOldColor = view.findViewById(R.id.ambilwarna_oldColor);
		viewNewColor = view.findViewById(R.id.ambilwarna_newColor);
		viewNewColorHexa = view.findViewById(R.id.ambilwarna_newColor_hexa);
		viewTarget = (ImageView) view.findViewById(R.id.ambilwarna_target);
		viewContainer = (ViewGroup) view.findViewById(R.id.ambilwarna_viewContainer);
		viewAlphaOverlay = view.findViewById(R.id.ambilwarna_overlay);
		viewAlphaCursor = (ImageView) view.findViewById(R.id.ambilwarna_alphaCursor);
		viewAlphaCheckered = (ImageView) view.findViewById(R.id.ambilwarna_alphaCheckered);

		{ // hide/show alpha
			viewAlphaOverlay.setVisibility(supportsAlpha? View.VISIBLE: View.GONE);
			viewAlphaCursor.setVisibility(supportsAlpha? View.VISIBLE: View.GONE);
			viewAlphaCheckered.setVisibility(supportsAlpha? View.VISIBLE: View.GONE);
		}

		viewSatVal.setHue(getHue());
		viewOldColor.setBackgroundColor(color);
		viewNewColor.setBackgroundColor(color);
		String hexColor = String.format("#%06X", (0xFFFFFF & color));
		viewNewColorHexa.setText(hexColor);
		viewNewColorHexa.setTextSize(viewNewColorHexa.getTextSize()/3);

		viewNewColorHexa.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,int arg2, int arg3) {}

			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
				if (viewNewColorHexa.hasFocus()) {
					// Regex to check valid hexadecimal color code.
					String regex = "^#([A-Fa-f0-9]{6})$";
					// The following names are also accepted by Color.parseColor:
					// red, blue, green, black, white, gray, cyan, magenta, yellow,
					// lightgray, darkgray, grey, lightgrey, darkgrey, aqua, fuchsia,
					// lime, maroon, navy, olive, purple, silver, and teal
					Pattern p = Pattern.compile(regex);
					String colorHexa = viewNewColorHexa.getText().toString();
					if (colorHexa != null) {
						Matcher m = p.matcher(colorHexa);
						if (m.matches()) {
							int color = Color.parseColor(colorHexa);
							Color.colorToHSV(color, currentColorHsv);
							moveCursor();
							if (AmbilWarnaDialog.this.supportsAlpha) moveAlphaCursor();
							moveTarget();
							if (AmbilWarnaDialog.this.supportsAlpha) updateAlphaView();
							viewSatVal.setHue(getHue());
							viewNewColor.setBackgroundColor(color);

							Log.d("app.varlorg.unote.debug", "colorPicker int  " + color);
							Log.d("app.varlorg.unote.debug", "colorPicker hexa  " + colorHexa);

						}
					}
				}
			}

		});
		viewHue.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE
				|| event.getAction() == MotionEvent.ACTION_DOWN
				|| event.getAction() == MotionEvent.ACTION_UP) {

					float y = event.getY();
					if (y < 0.f) y = 0.f;
					if (y > viewHue.getMeasuredHeight()) {
						y = viewHue.getMeasuredHeight() - 0.001f; // to avoid jumping the cursor from bottom to top.
					}
					float hue = 360.f - 360.f / viewHue.getMeasuredHeight() * y;
					if (hue == 360.f) hue = 0.f;
					setHue(hue);

					// update view
					viewSatVal.setHue(getHue());
					moveCursor();
					viewNewColor.setBackgroundColor(getColor());
					updateAlphaView();
					String hexColor = String.format("#%06X", (0xFFFFFF & getColor()));
					viewNewColorHexa.setText(hexColor);

					return true;
				}
				return false;
			}
		});

		if (supportsAlpha) viewAlphaCheckered.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_MOVE)
				|| (event.getAction() == MotionEvent.ACTION_DOWN)
				|| (event.getAction() == MotionEvent.ACTION_UP)) {

					float y = event.getY();
					if (y < 0.f) {
						y = 0.f;
					}
					if (y > viewAlphaCheckered.getMeasuredHeight()) {
						y = viewAlphaCheckered.getMeasuredHeight() - 0.001f; // to avoid jumping the cursor from bottom to top.
					}
					final int a = Math.round(255.f - ((255.f / viewAlphaCheckered.getMeasuredHeight()) * y));
					AmbilWarnaDialog.this.setAlpha(a);

					// update view
					moveAlphaCursor();
					int col = AmbilWarnaDialog.this.getColor();
					int c = a << 24 | col & 0x00ffffff;
					viewNewColor.setBackgroundColor(c);
					String hexColor = String.format("#%06X", (0xFFFFFF & getColor()));
					viewNewColorHexa.setText(hexColor);
					return true;
				}
				return false;
			}
		});
		viewSatVal.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE
				|| event.getAction() == MotionEvent.ACTION_DOWN
				|| event.getAction() == MotionEvent.ACTION_UP) {

					float x = event.getX(); // touch event are in dp units.
					float y = event.getY();

					if (x < 0.f) x = 0.f;
					if (x > viewSatVal.getMeasuredWidth()) x = viewSatVal.getMeasuredWidth();
					if (y < 0.f) y = 0.f;
					if (y > viewSatVal.getMeasuredHeight()) y = viewSatVal.getMeasuredHeight();

					setSat(1.f / viewSatVal.getMeasuredWidth() * x);
					setVal(1.f - (1.f / viewSatVal.getMeasuredHeight() * y));

					// update view
					moveTarget();
					viewNewColor.setBackgroundColor(getColor());
					String hexColor = String.format("#%06X", (0xFFFFFF & getColor()));
					viewNewColorHexa.setText(hexColor);
					return true;
				}
				return false;
			}
		});

		dialog = new AlertDialog.Builder(context)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (AmbilWarnaDialog.this.listener != null) {
					AmbilWarnaDialog.this.listener.onOk(AmbilWarnaDialog.this, getColor());
				}
			}
		})
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (AmbilWarnaDialog.this.listener != null) {
					AmbilWarnaDialog.this.listener.onCancel(AmbilWarnaDialog.this);
				}
			}
		})
		//.setNeutralButton(android.R.string.copy, new DialogInterface.OnClickListener() {
		.setNeutralButton("Reset", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (AmbilWarnaDialog.this.listener != null) {
					AmbilWarnaDialog.this.listener.onReset(AmbilWarnaDialog.this, getColor());
				}
			}
		})
		.setOnCancelListener(new OnCancelListener() {
			// if back button is used, call back our listener.
			@Override
			public void onCancel(DialogInterface paramDialogInterface) {
				if (AmbilWarnaDialog.this.listener != null) {
					AmbilWarnaDialog.this.listener.onCancel(AmbilWarnaDialog.this);
				}

			}
		})
		.create();
		// kill all padding from the dialog window
		dialog.setView(view, 0, 0, 0, 0);

		// move cursor & target on first draw
		ViewTreeObserver vto = view.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				moveCursor();
				if (AmbilWarnaDialog.this.supportsAlpha) moveAlphaCursor();
				moveTarget();
				if (AmbilWarnaDialog.this.supportsAlpha) updateAlphaView();
				view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
	}

	protected void moveCursor() {
		float y = viewHue.getMeasuredHeight() - (getHue() * viewHue.getMeasuredHeight() / 360.f);
		if (y == viewHue.getMeasuredHeight()) y = 0.f;
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewCursor.getLayoutParams();
		layoutParams.leftMargin = (int) (viewHue.getLeft() - Math.floor(viewCursor.getMeasuredWidth() / 2) - viewContainer.getPaddingLeft());
		layoutParams.topMargin = (int) (viewHue.getTop() + y - Math.floor(viewCursor.getMeasuredHeight() / 2) - viewContainer.getPaddingTop());
		viewCursor.setLayoutParams(layoutParams);
	}

	protected void moveTarget() {
		float x = getSat() * viewSatVal.getMeasuredWidth();
		float y = (1.f - getVal()) * viewSatVal.getMeasuredHeight();
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewTarget.getLayoutParams();
		layoutParams.leftMargin = (int) (viewSatVal.getLeft() + x - Math.floor(viewTarget.getMeasuredWidth() / 2) - viewContainer.getPaddingLeft());
		layoutParams.topMargin = (int) (viewSatVal.getTop() + y - Math.floor(viewTarget.getMeasuredHeight() / 2) - viewContainer.getPaddingTop());
		viewTarget.setLayoutParams(layoutParams);
	}

	protected void moveAlphaCursor() {
		final int measuredHeight = this.viewAlphaCheckered.getMeasuredHeight();
		float y = measuredHeight - ((this.getAlpha() * measuredHeight) / 255.f);
		final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.viewAlphaCursor.getLayoutParams();
		layoutParams.leftMargin = (int) (this.viewAlphaCheckered.getLeft() - Math.floor(this.viewAlphaCursor.getMeasuredWidth() / 2) - this.viewContainer.getPaddingLeft());
		layoutParams.topMargin = (int) ((this.viewAlphaCheckered.getTop() + y) - Math.floor(this.viewAlphaCursor.getMeasuredHeight() / 2) - this.viewContainer.getPaddingTop());

		this.viewAlphaCursor.setLayoutParams(layoutParams);
	}

	private int getColor() {
		final int argb = Color.HSVToColor(currentColorHsv);
		return alpha << 24 | (argb & 0x00ffffff);
	}

	private float getHue() {
		return currentColorHsv[0];
	}

	private float getAlpha() {
		return this.alpha;
	}

	private float getSat() {
		return currentColorHsv[1];
	}

	private float getVal() {
		return currentColorHsv[2];
	}

	private void setHue(float hue) {
		currentColorHsv[0] = hue;
	}

	private void setSat(float sat) {
		currentColorHsv[1] = sat;
	}

	private void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	private void setVal(float val) {
		currentColorHsv[2] = val;
	}

	public void show() {
		dialog.show();
	}

	public AlertDialog getDialog() {
		return dialog;
	}

	private void updateAlphaView() {
		final GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {
		Color.HSVToColor(currentColorHsv), 0x0
		});
		viewAlphaOverlay.setBackgroundDrawable(gd);
	}
}
