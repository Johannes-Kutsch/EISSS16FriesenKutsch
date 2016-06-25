package de.dtsharing.dtsharing;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ListView;

//http://stackoverflow.com/a/24629341
/* Die Custom ListView wird für eine Darstellung in einer ScrollView benötigt. Sie sorgt dafür,
 * dass die ListView ihre komplette Höhe erhält und somit nicht mehr Scrollbar ist. Das Scrollen
 * erfolgt über die ScrollView
 * Diese wird nur in der Suchmaske benötigt */
public class NonScrollListView extends ListView {

    public NonScrollListView(Context context) {
        super(context);
    }
    public NonScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public NonScrollListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasureSpec_custom = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}
