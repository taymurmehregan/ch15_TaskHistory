package com.murach.taskhistory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;


public class TipHistoryActivity extends Activity
    implements AdapterView.OnItemClickListener{
    private static final String DB_NAME = "tipDB";
    private static final int DB_VERSION = 1;

    private static final String TIP_TABLE = "tips_table";

    private static final String TIP_ID = "tip_id";
    private static final int TIP_ID_COL = 0;

    private static final String TIP_PERCENT = "tip_percent";
    private static final int TIP_PERCENT_COL = 1;

    private static final String TIP_BILL_AMOUNT = "bill_amount";
    private static final int TIP_BILL_AMOUNT_COL = 2;

    private static final String TIP_BILL_DATE = "bill_date";
    private static final int TIP_BILL_DATE_COL = 3;

    public static final String AUTHORITY = "com.murach.tiplist.provider";
    public static final Uri TIPS_URI = Uri.parse("content://" + AUTHORITY + "/tips");

    private ListView taskListView;
    private SimpleCursorAdapter adapter;
    private Cursor cursor;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_history);

        context = this;
        taskListView = (ListView) findViewById(R.id.tipListView);
    }

    @Override
    public void onResume() {
        super.onResume();

        // get cursor
        String orderBy = TIP_BILL_DATE + " DESC";
        cursor = getContentResolver()
                .query(TIPS_URI, null, null, null, null);

        // define variables for adapter
        int layout_id = R.layout.listview_task;
        String[] fromColumns = {TIP_ID, TIP_PERCENT, TIP_BILL_AMOUNT, TIP_BILL_DATE};
        int[] toViews = {R.id.idTextView, R.id.percentTextView, R.id.billAmountTextView,
                R.id.dateTextView};

        // create and set adapter
        adapter = new SimpleCursorAdapter(this, layout_id,
                cursor, fromColumns, toViews, 0);
        taskListView.setAdapter(adapter);
        taskListView.setOnItemClickListener(this);

        // convert column data to readable values
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor,
                                        int colIndex) {
                if (colIndex == TIP_BILL_DATE_COL){
                    long dateMillis = cursor.getLong(colIndex);
                    TextView tv = (TextView) view;
                    SimpleDateFormat date =
                            new SimpleDateFormat("EEE, MMM d yyyy HH:mm");
                    tv.setText("Completed on: " +
                            date.format(new Date(dateMillis)));
                    return true;
                }
                else if (colIndex == TIP_BILL_AMOUNT_COL) {
                    String notes = cursor.getString(colIndex);
                    if (notes == null || notes.equals("")) {
                        TextView tv = (TextView) view;
                        tv.setText("No notes");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /*@Override
    protected void onPause() {
        adapter.changeCursor(null);   // close cursor for the adapter
        cursor.close();               // close cursor for the activity
        super.onPause();
    }*/

    @Override
    public void onItemClick(AdapterView<?> adapter, View view,
                            int position, long id) {

        // get data from cursor
        cursor.moveToPosition(position);
        final int taskId = cursor.getInt(TIP_ID_COL);
        final String taskName = cursor.getString(TIP_BILL_DATE_COL);

        // display a dialog to confirm the delete
        new AlertDialog.Builder(this)
                .setMessage("Do you want to permanently delete task: " +
                        taskName + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // delete the specified task
                        String where = TIP_ID + " = ?";
                        String[] whereArgs = { Integer.toString(taskId) };
                        int deleteCount = getContentResolver()
                                .delete(TIPS_URI, where, whereArgs);
                        if (deleteCount <= 0) {
                            Toast.makeText(context, "Delete operation failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(context, "Delete operation successful",
                                    Toast.LENGTH_SHORT).show();
                            onResume();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }
}
