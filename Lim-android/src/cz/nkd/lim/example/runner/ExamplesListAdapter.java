/**
 * 
 */
package cz.nkd.lim.example.runner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import cz.nkd.lim.R;

/**
 * @author NkD
 *
 */
public class ExamplesListAdapter extends BaseExpandableListAdapter{

    private final Context mContext;

    public ExamplesListAdapter(final Context pContext) {
        this.mContext = pContext;
    }
    
    @Override
    public int getGroupCount() {
        return ExampleGroup.values().length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return ExampleGroup.values()[groupPosition].getExamples().length;
    }

    @Override
    public ExampleGroup getGroup(int groupPosition) {
        return ExampleGroup.values()[groupPosition];
    }

    @Override
    public Example getChild(int groupPosition, int childPosition) {
        return ExampleGroup.values()[groupPosition].getExamples()[childPosition];
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getChildView(final int pGroupPosition, final int pChildPosition, final boolean pIsLastChild, final View pConvertView, final ViewGroup pParent) {
        final View childView;
        if (pConvertView != null){
            childView = pConvertView;
        }else{
            childView = LayoutInflater.from(this.mContext).inflate(R.layout.examples_list_item, null);
        }

        TextView tv = ((TextView)childView.findViewById(R.id.examplesListItemTextView));
        tv.setPadding(60, 10, 2, 10);
        tv.setText(this.getChild(pGroupPosition, pChildPosition).name());
        return childView;
    }

    @Override
    public View getGroupView(final int pGroupPosition, final boolean pIsExpanded, final View pConvertView, final ViewGroup pParent) {
        final View groupView;
        if (pConvertView != null){
            groupView = pConvertView;
        }else{
            groupView = LayoutInflater.from(this.mContext).inflate(R.layout.examples_list_item, null);
        }
        TextView tv = ((TextView)groupView.findViewById(R.id.examplesListItemTextView));
        tv.setPadding(50, 15, 2, 15);
        tv.setText(this.getGroup(pGroupPosition).name());
        return groupView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
