package com.github.walterfan.kanban.domain;

public class Criteria {
	public String criteriaCode;
	public int sn;
	public int year;
	
	public String toString() {
		return criteriaCode + " " + sn + "-" + year;
	}
}


class GB extends Criteria {
	public GB() {
		criteriaCode = "GB";
	}
	
	public String toString() {
		return super.toString();
	}
}

class ZB extends GB {
	public ZB() {
		criteriaCode = "ZB";
	}
	public String professionCode;
	public String classificationCode;
	
	public String toString() {
		return criteriaCode + " " + professionCode+ " " + classificationCode 
				+ " " + sn + "-" + year;
	}
}

class DB extends GB {
	public DB() {
		criteriaCode = "DB";
	}
	public String regionCode;
	public String areaCode;
	public String professionCode;
	public String kindCode;
	
	public String toString() {
		return criteriaCode + " " + regionCode + "" + areaCode + " " + professionCode+ " "
				+ kindCode 	+ " " + sn + "-" + year;
	}
}

class QB extends GB {
	public QB() {
		criteriaCode = "Q";
	}
	
	public String toString() {
		return super.toString();
	}
}