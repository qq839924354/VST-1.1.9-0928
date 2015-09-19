package com.vst.itv52.v1.model;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown=true)
public class LiveTypeInfo implements Serializable {

	private static final long serialVersionUID = 588748309475447352L;
	@JsonProperty("id")
	public String tid; // tid
	@JsonProperty("name")
	public String tname; // tname
	public LiveTypeInfo(String tid, String tname) {
		super();
		this.tid = tid;
		this.tname = tname;
	}
	public LiveTypeInfo() {
		super();
	}
	
}
