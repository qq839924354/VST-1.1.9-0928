package com.vst.itv52.v1.biz;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;

import com.vst.itv52.v1.https.HttpClientHelper;
import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.VideoInfo;
import com.vst.itv52.v1.model.VideoList;
import com.vst.itv52.v1.model.VideoTypeInfo;

public class VideoCateBiz {

	public static ArrayList<VideoTypeInfo> parseTopCate(Context context,
			String url, boolean fromCache) {
		url = url + "item";
//		System.out.println("分类页" + url);
		ArrayList<VideoTypeInfo> list = null;
		/**
		 * 从文件获取
		 */
		File cateFile = new File(context.getCacheDir(), "cateFile");
		if (fromCache) {
			if (cateFile.exists()) {
				try {
					list = new ArrayList<VideoTypeInfo>();
					ObjectMapper mapper = new ObjectMapper();
					JsonNode rootNode = mapper.readTree(cateFile);
					JsonNode type = rootNode.path("type");
					for (JsonNode jsonNode : type) {
						VideoTypeInfo typeInfo = mapper.treeToValue(jsonNode,
								VideoTypeInfo.class);
						System.out.println(typeInfo);
						list.add(typeInfo);
					}
					// 部分型号电影、电视剧排序颠倒，原因不明，此处换回来
					if (!list.isEmpty() && list.get(1).name.equals("电影")) {
						VideoTypeInfo infoTemp = list.remove(1);
						list.add(2, infoTemp);
					}
					return list;
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		String json = HttpUtils.getContent(url, null, null);
		if (json != null) {
//			System.out.println("分类页数据：" + json);
			try {
				// 保存文件
				FileOutputStream fos = new FileOutputStream(cateFile);
				ByteArrayInputStream bis = new ByteArrayInputStream(
						json.getBytes());
				int count = -1;
				byte[] buffer = new byte[2048];
				while ((count = bis.read(buffer)) != -1) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				bis.close();

				list = new ArrayList<VideoTypeInfo>();
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(json);
				JsonNode type = rootNode.path("type");
				for (JsonNode jsonNode : type) {
					VideoTypeInfo typeInfo = mapper.treeToValue(jsonNode,
							VideoTypeInfo.class);
					System.out.println(typeInfo);
					list.add(typeInfo);
				}
				// 部分型号电影、电视剧排序颠倒，原因不明，此处换回来
				if (!list.isEmpty() && list.get(1).name.equals("电影")) {
					VideoTypeInfo infoTemp = list.remove(1);
					list.add(2, infoTemp);
				}
				return list;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static VideoList parseVideoList(String url,
			Map<String, String> parames) {
		if (parames.get("item") != null
				&& parames.get("item").equalsIgnoreCase("全部"))
			parames.remove("item");
		if (parames.get("area") != null
				&& parames.get("area").equalsIgnoreCase("不限"))
			parames.remove("area");
		NameValuePair[] pairs = HttpClientHelper.mapToPairs(parames);
		// System.out.println("pairs parames =" + pairs);
		String json = HttpUtils.getContent(url, null, pairs);
		if (json == null) {
			return null;
		}
		// System.out.println(json);
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(json);
			JsonNode videoNode = rootNode.path("video");
			for (JsonNode jsonNode : videoNode) {
				mapper.treeToValue(jsonNode, VideoInfo.class);
			}
			return mapper.treeToValue(rootNode, VideoList.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, ArrayList<String>> parseCateList(String url,
			String tid) {
		String json = HttpUtils.getContent(url, null, new NameValuePair[] {
				new BasicNameValuePair("tid", tid),
				new BasicNameValuePair("data", "item") });
		try {
			Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(json);
			JsonNode itemNode = rootNode.path("item");
			ArrayList<String> itemList = new ArrayList<String>();
			for (JsonNode jsonNode : itemNode) {
				itemList.add(jsonNode.path("name").asText());
			}

			JsonNode areaNode = rootNode.path("area");
			ArrayList<String> areaList = new ArrayList<String>();
			for (JsonNode jsonNode : areaNode) {
				areaList.add(jsonNode.path("name").asText());
			}

			JsonNode yearNode = rootNode.path("year");
			ArrayList<String> yearList = new ArrayList<String>();
			for (JsonNode jsonNode : yearNode) {
				yearList.add(jsonNode.path("name").asText());
			}

			map.put("item", itemList);
			map.put("area", areaList);
			map.put("year", yearList);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
		
		
	}
	
	
	/**
	 * 电视  1.
	 * {"video_count":"9183","zurpage":"30","maxpage":"20","punpage":"1","video":[{"id":"67168","cation":10,"idesc":"","title":"\u82b1\u5343\u9aa8","qxd":"\u8d85\u6e05","img":"https:\/\/s.doubanio.com\/view\/photo\/raw\/public\/p2248252738.jpg","mark":"7.2","banben":"\u66f4\u65b0\u81f34\u96c6"},{"id":"66863","cation":10,"idesc":"","title":"\u6293\u4f4f\u5f69\u8679\u7684\u7537\u4eba","qxd":"\u8d85\u6e05","img":"http:\/\/img31.mtime.cn\/pi\/2015\/05\/26\/093747.20682393_1000X1000.jpg","mark":"4.8","banben":"34\u96c6\u5168"},{"id":"66853","cation":10,"idesc":"","title":"\u5211\u8b66\u961f\u957f","qxd":"\u8d85\u6e05","img":"http:\/\/i.gtimg.cn\/qqlive\/img\/jpgcache\/files\/qqvideo\/l\/lv3wkqmqkakcido.jpg","mark":"8.4","banben":"36\u96c6\u5168"},{"id":"66891","cation":10,"idesc":"","title":"\u98ce\u4e91\u5929\u5730","qxd":"\u8d85\u6e05","img":"http:\/\/imgsrc.baidu.com\/forum\/pic\/item\/3c0bfd19ebc4b745e7ba57c1cefc1e178b82159d.jpg","mark":"7.0","banben":"\u66f4\u65b0\u81f336\u96c6"},{"id":"66879","cation":10,"idesc":"","title":"\u524d\u592b\u6c42\u7231\u8bb0","qxd":"\u8d85\u6e05","img":"http:\/\/img2.cache.netease.com\/photo\/0003\/2015-05-22\/AQ7E7DI400B70003.jpg","mark":"5.8","banben":"\u66f4\u65b0\u81f330\u96c6"},{"id":"66774","cation":10,"idesc":"","title":"\u5c4c\u4e1d\u7537\u58eb \u7b2c\u56db\u5b63","qxd":"\u8d85\u6e05","img":"http:\/\/photocdn.sohu.com\/20150426\/Img411921751.jpg","mark":"8.0","banben":"\u66f4\u65b0\u81f34\u96c6"},{"id":"66937","cation":10,"idesc":"","title":"\u8840\u6218\u5230\u5e95\u4e4b\u58ee\u4e01\u4e5f\u662f\u5175","qxd":"\u8d85\u6e05","img":"http:\/\/r4.ykimg.com\/050E00005564026867BC3C78A20CCC23","mark":"6.8","banben":"\u66f4\u65b0\u81f326\u96c6"},{"id":"61646","cation":10,"idesc":"","title":"\u540d\u4fa6\u63a2\u72c4\u4ec1\u6770","qxd":"\u8d85\u6e05","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2224402040.jpg","mark":"7.3","banben":"12\u96c6\u5168"},{"id":"67222","cation":10,"idesc":"","title":"\u6d77\u4e0a\u5b5f\u5e9c","qxd":"\u8d85\u6e05","img":"http:\/\/r3.ykimg.com\/050E00005577E98C67BC3C2B810C0745","mark":"8.2","banben":"\u66f4\u65b0\u81f36\u96c6"},{"id":"67167","cation":10,"idesc":"","title":"\u52ab\u4e2d\u52ab","qxd":"\u8d85\u6e05","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2193399502.jpg","mark":"5.4","banben":"\u66f4\u65b0\u81f320\u96c6"},{"id":"67207","cation":10,"idesc":"","title":"\u5267\u573a","qxd":"\u8d85\u6e05","img":"http:\/\/img4.douban.com\/view\/photo\/photo\/public\/p2178338157.jpg","mark":"7.9","banben":"\u66f4\u65b0\u81f36\u96c6"},{"id":"66868","cation":10,"idesc":"","title":"\u5f85\u5ac1\u8001\u7238","qxd":"\u8d85\u6e05","img":"http:\/\/r2.ykimg.com\/050E000053F6FC7D67379F14E40B8919","mark":"0.0","banben":"\u66f4\u65b0\u81f336\u96c6"},{"id":"66818","cation":10,"idesc":"","title":"\u95ea\u4eae\u8317\u5929","qxd":"\u8d85\u6e05","img":"http:\/\/img3.cache.netease.com\/photo\/0003\/2015-05-20\/AQ292B0900B70003.jpg","mark":"4.6","banben":"\u66f4\u65b0\u81f344\u96c6"},{"id":"66858","cation":10,"idesc":"","title":"\u4e8c\u5a76","qxd":"\u8d85\u6e05","img":"http:\/\/r3.ykimg.com\/050E00005549739967BC3C2575083D4D","mark":"4.2","banben":"\u66f4\u65b0\u81f337\u96c6"},{"id":"66840","cation":10,"idesc":"","title":"\u7231\u7684\u5987\u4ea7\u79d12","qxd":"\u8d85\u6e05","img":"http:\/\/img4.douban.com\/view\/photo\/photo\/public\/p2245815697.jpg","mark":"6.9","banben":"\u66f4\u65b0\u81f312\u96c6"},{"id":"66851","cation":10,"idesc":"","title":"\u7a7a\u5de2\u59e5\u7237","qxd":"\u8d85\u6e05","img":"http:\/\/img4.douban.com\/view\/photo\/photo\/public\/p2204202599.jpg","mark":"0.0","banben":"\u66f4\u65b0\u81f331\u96c6"},{"id":"66924","cation":10,"idesc":"","title":"\u79bb\u5a5a\u534f\u8bae","qxd":"\u8d85\u6e05","img":"http:\/\/r3.ykimg.com\/050E00005565503C67BC3C7D280A1C09","mark":"6.7","banben":"\u66f4\u65b0\u81f328\u96c6"},{"id":"66817","cation":10,"idesc":"","title":"\u5a5a\u59fb\u65f6\u5dee","qxd":"\u8d85\u6e05","img":"http:\/\/i-7.vcimg.com\/crop\/d08c794c2827259312124d71c0c19f06145896(600x)\/thumb.jpg","mark":"4.7","banben":"38\u96c6\u5168"},{"id":"66923","cation":10,"idesc":"","title":"\u7edd\u5730\u5200\u950b","qxd":"\u8d85\u6e05","img":"http:\/\/r3.ykimg.com\/050E0000543B2F4167379F1A7C095EE6","mark":"4.2","banben":"36\u96c6\u5168"},{"id":"66382","cation":10,"idesc":"","title":"\u864e\u5988\u732b\u7238","qxd":"\u8d85\u6e05","img":"http:\/\/i.gtimg.cn\/qqlive\/img\/jpgcache\/files\/qqvideo\/m\/miq3dzivmn4q97e.jpg","mark":"7.0","banben":"45\u96c6\u5168"},{"id":"62147","cation":10,"idesc":"","title":"\u5c11\u5e74\u56db\u5927\u540d\u6355","qxd":"\u8d85\u6e05","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2115314532.jpg","mark":"3.8","banben":"48\u96c6\u5168"},{"id":"66318","cation":10,"idesc":"","title":"\u7279\u8b66\u529b\u91cf","qxd":"\u8d85\u6e05","img":"http:\/\/r2.ykimg.com\/050E0000553DF3CE67BC3C0F080AF5C0","mark":"4.5","banben":"51\u96c6\u5168"},{"id":"66802","cation":10,"idesc":"","title":"\u4faf\u5929\u660e\u7684\u68a6","qxd":"\u8d85\u6e05","img":"http:\/\/r3.ykimg.com\/050E00005541EB9367BC3C1D0E005C4C","mark":"3.0","banben":"40\u96c6\u5168"},{"id":"66429","cation":10,"idesc":"","title":"\u804a\u658b\u65b0\u7f16","qxd":"\u8d85\u6e05","img":"http:\/\/i.gtimg.cn\/qqlive\/img\/jpgcache\/files\/qqvideo\/u\/ubmmtipzv8x7xsq.jpg","mark":"5.7","banben":"39\u96c6\u5168"},{"id":"66329","cation":10,"idesc":"","title":"\u4e8e\u65e0\u58f0\u5904","qxd":"\u8d85\u6e05","img":"http:\/\/www.sinaimg.cn\/dy\/slidenews\/4_img\/2015_16\/704_1600369_893160.jpg","mark":"7.7","banben":"34\u96c6\u5168"},{"id":"66621","cation":10,"idesc":"","title":"\u51ac\u6696\u82b1\u4f1a\u5f00","qxd":"\u8d85\u6e05","img":"http:\/\/i.gtimg.cn\/qqlive\/img\/jpgcache\/files\/qqvideo\/i\/ib78bx3k7livox6.jpg","mark":"3.2","banben":"32\u96c6\u5168"},{"id":"66568","cation":10,"idesc":"","title":"\u9526\u7ee3\u7f18\u534e\u4e3d\u5192\u9669 \u6c5f\u897f\u536b\u89c6TV\u7248","qxd":"\u8d85\u6e05","img":"http:\/\/img31.mtime.cn\/pi\/2014\/09\/01\/153152.77664490_1000X1000.jpg","mark":"4.8","banben":"40\u96c6\u5168"},{"id":"66571","cation":10,"idesc":"","title":"\u70fd\u706b\u82f1\u96c4\u4f20","qxd":"\u8d85\u6e05","img":"http:\/\/e.hiphotos.baidu.com\/baike\/c0%3Dbaike80%2C5%2C5%2C80%2C26\/sign=0b185d7b1a30e924dba994632d610563\/b8389b504fc2d5628925909ee21190ef76c66c97.jpg","mark":"0.0","banben":"36\u96c6\u5168"},{"id":"66410","cation":10,"idesc":"","title":"\u5b59\u8001\u5014\u7684\u5e78\u798f","qxd":"\u8d85\u6e05","img":"http:\/\/imgbdb2.bendibao.com\/bjbdb\/20151\/8\/2015181810122.jpg","mark":"5.8","banben":"40\u96c6\u5168"},{"id":"66433","cation":10,"idesc":"","title":"\u575088\u8def\u8f66\u56de\u5bb6","qxd":"\u8d85\u6e05","img":"http:\/\/img5.douban.com\/view\/photo\/photo\/public\/p2236888256.jpg","mark":"6.7","banben":"40\u96c6\u5168"}]}
	 * 
	 * 2.
	 * 
	 * {"video_count":"9183","zurpage":"30","maxpage":"20","punpage":"4","video":[{"id":"57830","cation":10,"idesc":"","title":"\u6b66\u5a9a\u5a18\u4f20\u5947 \u6e56\u5357\u536b\u89c6\u7248","qxd":"\u8d85\u6e05","img":"http:\/\/r2.ykimg.com\/050E00005493E5F567379F413B002D5C","mark":"6.2","banben":"96\u96c6\u5168"},{"id":"61502","cation":10,"idesc":"","title":"\u8f6c\u8eab\u8bf4\u7231\u4f60","qxd":"\u8d85\u6e05","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2205967909.jpg","mark":"7.8","banben":"32\u96c6\u5168"},{"id":"61303","cation":10,"idesc":"","title":"\u533b\u9986\u7b11\u4f20","qxd":"\u8d85\u6e05","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2205447479.jpg","mark":"5.9","banben":"37\u96c6\u5168"},{"id":"61304","cation":10,"idesc":"","title":"\u957f\u5927","qxd":"\u8d85\u6e05","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2186812012.jpg","mark":"6.7","banben":"38\u96c6\u5168"},{"id":"58310","cation":10,"idesc":"","title":"\u4f55\u4ee5\u7b19\u7bab\u9ed8","qxd":"\u8d85\u6e05","img":"http:\/\/a.hiphotos.baidu.com\/baike\/c0%3Dbaike180%2C5%2C5%2C180%2C60\/sign=71a30999223fb80e18dc698557b8444b\/48540923dd54564ee212e191b0de9c82d1584f26.jpg","mark":"6.9","banben":"36\u96c6\u5168"},{"id":"61151","cation":10,"idesc":"","title":"\u5927\u5200\u8bb0","qxd":"\u8d85\u6e05","img":"http:\/\/r4.ykimg.com\/050E000054B7719467379F6B8F018B3A","mark":"7.5","banben":"57\u96c6\u5168"},{"id":"61366","cation":10,"idesc":"","title":"\u6218\u9b42","qxd":"\u8d85\u6e05","img":"http:\/\/img3.cache.netease.com\/photo\/0003\/2015-01-30\/AH71BKM600B70003.jpg","mark":"8.2","banben":"31\u96c6\u5168"},{"id":"61268","cation":10,"idesc":"","title":"\u6740\u5bc7\u51b3","qxd":"\u8d85\u6e05","img":"http:\/\/r3.ykimg.com\/050E00005435EEC767379F674D0E6905","mark":"9.0","banben":"47\u96c6\u5168"},{"id":"61346","cation":10,"idesc":"","title":"\u6211\u4eec\u5bb6\u7684\u5fae\u5e78\u798f\u751f\u6d3b","qxd":"\u8d85\u6e05","img":"http:\/\/r1.ykimg.com\/050E000054CB38FA67379F6CA70C7D09","mark":"8.5","banben":"36\u96c6\u5168"},{"id":"61314","cation":10,"idesc":"","title":"\u5149\u5f71","qxd":"\u8d85\u6e05","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2215884256.jpg","mark":"3.8","banben":"43\u96c6\u5168"},{"id":"58178","cation":10,"idesc":"","title":"\u53ea\u56e0\u5355\u8eab\u5728\u4e00\u8d77","qxd":"\u8d85\u6e05","img":"http:\/\/www.sinaimg.cn\/dy\/slidenews\/4_img\/2015_01\/704_1521141_548775.jpg","mark":"5.7","banben":"24\u96c6\u5168"},{"id":"51350","cation":10,"idesc":"","title":"\u65b0\u4eac\u534e\u70df\u4e91","qxd":"\u6807\u6e05","img":"http:\/\/img.91vst.com\/vst\/photo\/2.0\/vod\/pic\/90ec5d20bd985ffdc9250576596fabd6.jpg","mark":"7.8","banben":"43\u96c6\u5168"},{"id":"61598","cation":10,"idesc":"","title":"\u8349\u5e3d\u8b66\u5bdf","qxd":"\u8d85\u6e05","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2173563134.jpg","mark":"9.3","banben":"28\u96c6\u5168"},{"id":"20981","cation":10,"idesc":"","title":"\u730e\u9b54","qxd":"\u8d85\u6e05","img":"http:\/\/t3.baidu.com\/it\/u=2333906901,3243854291&fm=20","mark":"8.0","banben":"33\u96c6\u5168"},{"id":"61150","cation":10,"idesc":"","title":"\u522b\u53eb\u6211\u5144\u5f1f","qxd":"\u8d85\u6e05","img":"http:\/\/r4.ykimg.com\/050E00005424DE1067379F654D0918F1","mark":"6.4","banben":"32\u96c6\u5168"},{"id":"20840","cation":10,"idesc":"","title":"\u90bb\u5c45\u4e5f\u75af\u72c2","qxd":"\u9ad8\u6e05","img":"http:\/\/r4.ykimg.com\/050E00005297F45F67583953F608C19E","mark":"7.4","banben":"30\u96c6\u5168"},{"id":"58309","cation":10,"idesc":"","title":"\u9646\u5c0f\u51e4\u4e0e\u82b1\u6ee1\u697c","qxd":"\u8d85\u6e05","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2035400183.jpg","mark":"4.1","banben":"43\u96c6\u5168"},{"id":"58018","cation":10,"idesc":"","title":"\u950b\u5203","qxd":"\u8d85\u6e05","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2203961266.jpg","mark":"8.3","banben":"42\u96c6\u5168"},{"id":"19661","cation":10,"idesc":"","title":"\u9519\u4f0f","qxd":"\u9ad8\u6e05","img":"http:\/\/f.hiphotos.baidu.com\/baike\/c0%3Dbaike92%2C5%2C5%2C92%2C30\/sign=f2eb1150d833c895b2739029b07a1895\/d8f9d72a6059252d29e57bc3359b033b5ab5b903.jpg","mark":"5.1","banben":"30\u96c6\u5168"},{"id":"57956","cation":10,"idesc":"","title":"\u4e8c\u70ae\u624b","qxd":"\u8d85\u6e05","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2212215673.jpg","mark":"9.0","banben":"36\u96c6\u5168"},{"id":"57868","cation":10,"idesc":"","title":"\u8001\u519c\u6c11","qxd":"\u8d85\u6e05","img":"http:\/\/r4.ykimg.com\/050E00005497BD7267379F6D860A317A","mark":"7.3","banben":"60\u96c6\u5168"},{"id":"57809","cation":10,"idesc":"","title":"\u9e7f\u9f0e\u8bb0","qxd":"\u8d85\u6e05","img":"http:\/\/img2.cache.netease.com\/photo\/0003\/2014-12-12\/AD90F5Q800B70003.jpg","mark":"3.7","banben":"50\u96c6\u5168"},{"id":"56893","cation":10,"idesc":"","title":"\u5339\u8bfa\u66f9","qxd":"\u8d85\u6e05","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2209805848.jpg","mark":"8.4","banben":"20\u96c6\u5168"},{"id":"58306","cation":10,"idesc":"","title":"\u9752\u6625\u6b63\u80fd\u91cf\u4e4b\u6211\u662f\u5973\u795e","qxd":"\u8d85\u6e05","img":"http:\/\/img.91vst.com\/vst\/photo\/2.0\/vod\/pic\/d99106bda8d5e7e0b9b6f4fe816c97cd.jpg","mark":"0.0","banben":"35\u96c6\u5168"},{"id":"52374","cation":10,"idesc":"","title":"\u7231\u4f60\u4e0d\u653e\u624b","qxd":"\u8d85\u6e05","img":"http:\/\/img5.douban.com\/view\/photo\/photo\/public\/p2221119378.jpg","mark":"0.0","banben":"40\u96c6\u5168"},{"id":"58308","cation":10,"idesc":"","title":"\u6025\u8bca\u5ba4\u6545\u4e8b","qxd":"\u8d85\u6e05","img":"http:\/\/img5.cache.netease.com\/photo\/0003\/2015-01-10\/AFJLTAND00B70003.jpg","mark":"8.0","banben":"38\u96c6\u5168"},{"id":"58307","cation":10,"idesc":"","title":"\u5f53\u4eba\u5fc3\u9047\u4e0a\u4ec1\u5fc3","qxd":"\u8d85\u6e05","img":"http:\/\/c.hiphotos.baidu.com\/baike\/c0%3Dbaike116%2C5%2C5%2C116%2C38\/sign=02a26facacc3793169658e7b8aaddc20\/b21c8701a18b87d65d2d06b5040828381f30fd29.jpg","mark":"0.0","banben":"34\u96c6\u5168"},{"id":"58381","cation":10,"idesc":"","title":"\u8840\u5203","qxd":"\u8d85\u6e05","img":"http:\/\/img3.douban.com\/lpic\/s9041140.jpg","mark":"0.0","banben":"27\u96c6\u5168"},{"id":"58339","cation":10,"idesc":"","title":"\u5929\u751f\u8981\u5b8c\u7f8e","qxd":"\u8d85\u6e05","img":"http:\/\/h.hiphotos.baidu.com\/baike\/c0%3Dbaike180%2C5%2C5%2C180%2C60\/sign=a8305d7eb78f8c54f7decd7d5b404690\/960a304e251f95cad6a90926ca177f3e660952c5.jpg","mark":"0.0","banben":"30\u96c6\u5168"},{"id":"58093","cation":10,"idesc":"","title":"\u5927\u6e05\u76d0\u5546","qxd":"\u8d85\u6e05","img":"http:\/\/img3.douban.com\/lpic\/s24525964.jpg","mark":"0.0","banben":"34\u96c6\u5168"}]}
	 * 
	 * 
	 */
}
