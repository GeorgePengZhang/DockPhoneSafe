package com.auratech.dockphonesafe.bean;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.auratech.dockphonesafe.utils.ContactsUtils;


/**
 * @ClassName: CallsLogBean
 * @Description: TODO
 * @author: steven zhang
 * @date: Sep 27, 2016 2:31:30 PM
 */
public class CallsLogBean implements Parcelable {

	private long id;
	private String number;
	private long date;
	private long duration;
	private int type;
	private String countryiso;
	private String geocodedlocation;
	private String name;
	private String numbertype;
	private String numberlabel;
	private String lookupuri;
	private String matched_number;
	private String normalizednumber;
	private long photoid;
	private String formattednumber;
	private int isread;
	private String phone;
	
	public CallsLogBean() {
	}
	
	public CallsLogBean(long id, String number, String phone, long date, long duration,
			int type, String countryiso, String geocodedlocation,
			String name, String numbertype, String numberlabel,
			String lookupuri, String matched_number,
			String normalizednumber, long photoid, String formattednumber,
			int isread) {
		this.id = id;
		this.number = number;
		this.phone = phone;
		this.date = date;
		this.duration = duration;
		this.type = type;
		this.countryiso = countryiso;
		this.geocodedlocation = geocodedlocation;
		this.name = name;
		this.numbertype = numbertype;
		this.numberlabel = numberlabel;
		this.lookupuri = lookupuri;
		this.matched_number = matched_number;
		this.normalizednumber = normalizednumber;
		this.photoid = photoid;
		this.formattednumber = formattednumber;
		this.isread = isread;
	}

	/**
	 * @param source
	 */
	public CallsLogBean(Parcel source) {
		id = source.readLong();
		number = source.readString();
		phone = source.readString();
		date = source.readLong();
		duration = source.readLong();
		type = source.readInt();
		countryiso = source.readString();
		geocodedlocation = source.readString();
		name = source.readString();
		numbertype = source.readString();
		numberlabel = source.readString();
		lookupuri = source.readString();
		matched_number = source.readString();
		normalizednumber = source.readString();
		photoid = source.readLong();
		formattednumber = source.readString();
		isread = source.readInt();
	}

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
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
		this.phone = phone;
	}

	public long getDate() {
		return date;
	}
	
	public void setDate(long date) {
		this.date = date;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public String getCountryiso() {
		return countryiso;
	}
	
	public void setCountryiso(String countryiso) {
		this.countryiso = countryiso;
	}
	
	public String getGeocodedlocation() {
		return geocodedlocation;
	}
	
	public void setGeocodedlocation(String geocodedlocation) {
		this.geocodedlocation = geocodedlocation;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getNumbertype() {
		return numbertype;
	}
	
	public void setNumbertype(String numbertype) {
		this.numbertype = numbertype;
	}
	
	public String getNumberlabel() {
		return numberlabel;
	}
	
	public void setNumberlabel(String numberlabel) {
		this.numberlabel = numberlabel;
	}
	
	public String getLookupuri() {
		return lookupuri;
	}
	
	public void setLookupuri(String lookupuri) {
		this.lookupuri = lookupuri;
	}
	
	public String getMatched_number() {
		return matched_number;
	}
	
	public void setMatched_number(String matched_number) {
		this.matched_number = matched_number;
	}
	
	public String getNormalizednumber() {
		return normalizednumber;
	}
	
	public void setNormalizednumber(String normalizednumber) {
		this.normalizednumber = normalizednumber;
	}
	
	public long getPhotoid() {
		return photoid;
	}
	
	public void setPhotoid(long photoid) {
		this.photoid = photoid;
	}
	
	public String getFormattednumber() {
		return formattednumber;
	}
	
	public void setFormattednumber(String formattednumber) {
		this.formattednumber = formattednumber;
	}
	
	public int getIsread() {
		return isread;
	}
	
	public void setIsread(int isread) {
		this.isread = isread;
	}
	
	@Override
	public boolean equals(Object o) {
		CallsLogBean bean = (CallsLogBean)o;
		if (TextUtils.isEmpty(this.name)) {
			if (TextUtils.isEmpty(bean.name)) {
				return this.number.equals(bean.number);
			} else {
				return false;
			}
		} else {
			if (TextUtils.isEmpty(bean.name)) {
				return false;
			} else {
				return this.number.equals(bean.number);
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("id:").append(id).append(",")
		.append("number:").append(number).append(",")
		.append("date:").append(date).append(",")
		.append("duration:").append(duration).append(",")
		.append("type:").append(type).append(",")
		.append("countryiso:").append(countryiso).append(",")
		.append("geocodedlocation:").append(geocodedlocation).append(",")
		.append("name:").append(name).append(",")
		.append("numbertype:").append(numbertype).append(",")
		.append("numberlabel:").append(numberlabel).append(",")
		.append("lookupuri:").append(lookupuri).append(",")
		.append("matched_number:").append(matched_number).append(",")
		.append("normalizednumber:").append(normalizednumber).append(",")
		.append("photoid:").append(photoid).append(",")
		.append("formattednumber:").append(formattednumber).append(",")
		.append("isread:").append(isread);
		
		return builder.toString();
	}
	
	/**
	 * 将数据库中的通话记录数据转化为bean对象
	 * @param cursor
	 * @return
	 */
	public static CallsLogBean getBeanFromCursor(Cursor cursor) {
		CallsLogBean bean = null;
		
		if (cursor != null) {
			long id = cursor.getInt(ContactsUtils.Calls.ID);
			String number = cursor.getString(ContactsUtils.Calls.NUMBER);
			long date = cursor.getLong(ContactsUtils.Calls.DATE);
			long duration = cursor.getLong(ContactsUtils.Calls.DURATION);
			int type = cursor.getInt(ContactsUtils.Calls.CALL_TYPE);
			String countryiso = cursor.getString(ContactsUtils.Calls.COUNTRY_ISO);
			String geocodedlocation = cursor.getString(ContactsUtils.Calls.GEOCODED_LOCATION);
			String name = cursor.getString(ContactsUtils.Calls.CACHED_NAME);
			String numbertype = cursor.getString(ContactsUtils.Calls.CACHED_NUMBER_TYPE);
			String numberlabel = cursor.getString(ContactsUtils.Calls.CACHED_NUMBER_LABEL);
			String lookupuri = cursor.getString(ContactsUtils.Calls.CACHED_LOOKUP_URI);
			String matched_number = cursor.getString(ContactsUtils.Calls.CACHED_MATCHED_NUMBER);
			String normalizednumber = cursor.getString(ContactsUtils.Calls.CACHED_NORMALIZED_NUMBER);
			long photoid = cursor.getInt(ContactsUtils.Calls.CACHED_PHOTO_ID);
			String formattednumber = cursor.getString(ContactsUtils.Calls.CACHED_FORMATTED_NUMBER);
			int isread = cursor.getInt(ContactsUtils.Calls.IS_READ);
			
			String phone = number.replaceAll("\\D", "");
			
			bean = new CallsLogBean(id, number, phone, date, duration, type, countryiso, geocodedlocation, name, 
					numbertype, numberlabel, lookupuri, matched_number, normalizednumber, photoid, formattednumber, isread);
		}
		
		return bean;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(number);
		dest.writeString(phone);
		dest.writeLong(date);
		dest.writeLong(duration);
		dest.writeInt(type);
		dest.writeString(countryiso);
		dest.writeString(geocodedlocation);
		dest.writeString(name);
		dest.writeString(numbertype);
		dest.writeString(numberlabel);
		dest.writeString(lookupuri);
		dest.writeString(matched_number);
		dest.writeString(normalizednumber);
		dest.writeLong(photoid);
		dest.writeString(formattednumber);
		dest.writeInt(isread);
	}
	
	public static final Parcelable.Creator<CallsLogBean> CREATOR = new Parcelable.Creator<CallsLogBean>() {

		@Override
		public CallsLogBean createFromParcel(Parcel source) {
			return new CallsLogBean(source);
		}

		@Override
		public CallsLogBean[] newArray(int size) {
			return new CallsLogBean[size];
		}
	};
}
