package com.auratech.dockphonesafe.bean;

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
