package com.auratech.dockphonesafe.utils;

import android.content.Context;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBaseConfig;

public class DbUtils {
	private static final String DB_NAME = "disturbtime"; 

	public static DbUtils instance;
	
	private LiteOrm liteOrm;
	private Context mContext;
	
	public static DbUtils getInstance(Context context) {
		if (instance == null) {
			synchronized (DbUtils.class) {
				if (instance == null) {
					instance = new DbUtils(context.getApplicationContext());
				}
			}
		}
		return instance;
	}
	
	private DbUtils(Context context) {
		mContext = context;
		init(context);
	}

	public void init(Context context) {
		if (liteOrm == null) {
            // 使用级联操作
            DataBaseConfig config = new DataBaseConfig(context, DB_NAME);
            config.debugged = true; // open the log
            config.dbVersion = 1; // set database version
            config.onUpdateListener = null; // set database update listener
            liteOrm = LiteOrm.newCascadeInstance(config);// cascade
        }
	}
	
	public LiteOrm getDbInstance() {
		init(mContext);
		return liteOrm;
	}
	
	public void close() {
		if (liteOrm != null) {
			liteOrm.close();
			liteOrm = null;
		}
	}
}
