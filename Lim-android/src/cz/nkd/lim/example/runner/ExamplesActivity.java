package cz.nkd.lim.example.runner;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;
import cz.nkd.lim.R;

/**
 * @author Michal NkD Nikodim (michal.nikodim@gmail.com)
 */
public class ExamplesActivity extends ExpandableListActivity {

    private ExamplesListAdapter mExamplesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.examples);

        mExamplesListAdapter = new ExamplesListAdapter(this);

        this.setListAdapter(mExamplesListAdapter);

        getExpandableListView().expandGroup(ExampleGroup.NkD.ordinal());
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        final Example example = this.mExamplesListAdapter.getChild(groupPosition, childPosition);
        if (example.getActivityClass() != null) {
            this.startActivity(new Intent(this, example.getActivityClass()));
        } else {
            Toast.makeText(this, "ActivityClass for example " + example.name() + " not found.", Toast.LENGTH_SHORT).show();
        }
        return super.onChildClick(parent, v, groupPosition, childPosition, id);
    }

}
