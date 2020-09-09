package oom.android.system.Managers.CallL;

import android.provider.CallLog;

public class CallLogType {
    public static Integer Type(String type){
        Integer i = Integer.valueOf(type);
        switch (i){
            case 1:
                return CallLog.Calls.INCOMING_TYPE;
            case 2:
                return CallLog.Calls.OUTGOING_TYPE;
            case 3:
                return CallLog.Calls.MISSED_TYPE;
        }
        return 0;
    }
}
