package com.auratech.dockphonesafe.bean;

import com.litesuits.orm.db.annotation.Table;

@Table("whitelist")
public class WhiteListBean extends BaseListBean {
	
	public WhiteListBean() {
		super();
	}
	
	public WhiteListBean(BaseListBean baseBean) {
		super(baseBean);
	}
}
