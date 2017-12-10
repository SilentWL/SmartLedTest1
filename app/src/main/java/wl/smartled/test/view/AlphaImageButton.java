package wl.smartled.test.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by Administrator on 2017/11/7 0007.
 */

public class AlphaImageButton extends ImageButton{
    public AlphaImageButton(Context context) {
        super(context);
    }

    public AlphaImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlphaImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            setAlpha(0.5f);
        } else if (event.getAction() == MotionEvent.ACTION_UP){
            setAlpha(1.0f);
        }
        return super.dispatchTouchEvent(event);
    }
}
