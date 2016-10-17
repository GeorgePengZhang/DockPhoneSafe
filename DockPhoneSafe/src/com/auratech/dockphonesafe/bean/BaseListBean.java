package com.auratech.dockphonesafe.bean;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Conflict;
import com.litesuits.orm.db.annotation.NotNull;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Unique;
import com.litesuits.orm.db.enums.AssignType;
import com.litesuits.orm.db.enums.Strategy;

public class BaseListBean implements Comparable<BaseListBean> {
	public static final int MAX = 999;
	public static final String PHONE = "phone";
	public static final String ENABLE = "enable";
	
	@PrimaryKey(AssignType.AUTO_INCREMENT)
	@Column("_id")
	private int id;
	private String name;
	private String number;
	@Unique
	@NotNull
	@Conflict(Strategy.FAIL)
	private String phone;
	private String firstLetter;
	private String spellname;
	private long photoid;
	private String photoUri;
	private boolean enable;
	
	public BaseListBean() {
	}
	
	public BaseListBean(BaseListBean bean) {
		this.id = bean.getId();
		this.name = bean.getName();
		this.number = bean.getNumber();
		this.phone = bean.getPhone();
		this.enable = bean.isEnable();
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getNumber() {
		return number;
	}
	
	public void setNumber(String number) {
		this.number = number;
	}
	
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone.replaceAll("\\D", "");
	}

	public String getFirstLetter() {
		return firstLetter;
	}

	public void setFirstLetter(String firstLetter) {
		this.firstLetter = firstLetter;
	}

	public String getSpellname() {
		return spellname;
	}

	public void setSpellname(String spellname) {
		this.spellname = spellname;
	}
	
	public long getPhotoid() {
		return photoid;
	}

	public void setPhotoid(long photoid) {
		this.photoid = photoid;
	}

	public String getPhotoUri() {
		return photoUri;
	}

	public void setPhotoUri(String photoUri) {
		this.photoUri = photoUri;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	@Override
	public boolean equals(Object o) {
		BaseListBean bean = (BaseListBean) o;
		return this.phone.equals(bean.phone);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("id:").append(id).append(",")
		.append("name:").append(name).append(",")
		.append("number:").append(number).append(",")
		.append("phone:").append(phone).append(",")
		.append("firstLetter:").append(firstLetter).append(",")
		.append("spellname:").append(spellname).append(",")
		.append("photoid:").append(photoid).append(",")
		.append("photoUri:").append(photoUri).append(",")
		.append("enable:").append(enable);
		return builder.toString();
	}

	@Override
	public int compareTo(BaseListBean another) {
		if (!"#".equals(this.getFirstLetter()) && "#".equals(another.getFirstLetter())) {
			return -1;
		} else if ("#".equals(this.getFirstLetter()) && !"#".equals(another.getFirstLetter())) {
			return 1; 
		} else {
			return this.getSpellname().compareTo(another.getSpellname());
		}
	}
}
