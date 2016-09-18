package com.auratech.dockphonesafe.utils;

import com.litesuits.orm.db.annotation.Table;

@Table("blacklist")
public class BlackListBean extends BaseListBean {
	
	public BlackListBean() {
		super();
	}
	
	public BlackListBean(BaseListBean baseBean) {
		super(baseBean);
	}
}
