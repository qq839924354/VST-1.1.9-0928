package com.vst.itv52.v1.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown=true)
public class ServerInfo {

	/**
	 * ?data=info ?data=so ?data=list ?data=tj ?data=item ?data=host
	 */
	public String hosturl;
	/**
	 * /play.php?+link
	 */
	public String playurl;

	/**
	 * /live.php?uptime=0
	 */
	public String liveurl;

	/**
	 * /tvback.php
	 */

	public String tvback_url;

	/**
	 * /tvepg.php?id=
	 */
	public String tvepg_url;

	/**
	 * /news.php
	 */
	public String news_url;

	/**
	 * /mv.php
	 */
	public String mv_url;
}
