package com.vst.itv52.v1.biz;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.VideoDetailInfo;
import com.vst.itv52.v1.model.VideoInfo;
import com.vst.itv52.v1.model.VideoSet;
import com.vst.itv52.v1.model.VideoSource;

public class VideoDetailBiz {

	/**
	 * http://so.52itv.cn/?data=info&id=@id
	 * 
	 * @param id
	 * @return
	 */
	public static VideoDetailInfo parseDetailInfo(String url, int id,
			int verCode) {
		url = url + id + ".json";
		String json = HttpUtils.getContent(url, null, null);
		if (json == null) {
			return null;
		}
//		System.out.println(json);
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(json);
			JsonNode playlist = rootNode.path("playlist");
			for (JsonNode playlistNode : playlist) {
				for (JsonNode listNode : playlistNode.path("list")) {
					mapper.treeToValue(listNode, VideoSet.class);
				}
				mapper.treeToValue(playlistNode, VideoSource.class);
			}
			JsonNode new_top = rootNode.path("new_top");
			for (JsonNode jsonNode : new_top) {
				mapper.treeToValue(jsonNode, VideoInfo.class);
			}
			VideoDetailInfo detail = mapper.treeToValue(rootNode,
					VideoDetailInfo.class);
			return detail;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}/**
	
	{"id":"58064","type":"1","title":"\u706b\u7ebf\u53cd\u653b","banben":" \u8bc4\u5206\uff1a5.5","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2213799032.jpg","year":"2012","area":"\u7f8e\u56fd","update":"0","dur":"95","language":"\u82f1\u8bed","cate":"\u5267\u60c5\/\u72af\u7f6a\/\u52a8\u4f5c","director":"David","actor":"\u4e54\u4ec0\u00b7\u675c\u54c8\u660e\/\u5e03\u9c81\u65af\u00b7\u5a01\u5229\u65af\/\u7f57\u838e\u91cc\u5965\u00b7\u9053\u68ee\/\u6587\u68ee\u7279\u00b7\u591a\u8bfa\u8d39\u5965\/50\u5206\/\u6731\u5229\u5b89\u00b7\u9ea6\u514b\u9a6c\u6d2a\/\u7ef4\u5c3c\u00b7\u743c\u65af\/\u963f\u91cc\u00b7\u7ef4\u6587\/\u827e\u91cc\u514b\u00b7\u6e29\u7279\/\u90a6\u59ae\u00b7\/\u7406\u67e5\u5fb7\u00b7\u5e0c\u592b\/\u8a79\u59c6\u65af\u00b7\u52d2\u65af\u5e93\/\u594e\u987f\u00b7\u6770\u514b\u900a\/\u51ef\u6587\u00b7\u675c\u6069","info":"\u52c7\u731b\u679c\u6562\u7684\u6770\u91cc\u7c73\u00b7\u79d1\u66fc\uff08\u4e54\u4ec0\u00b7\u675c\u54c8\u660eJoshDuhamel\u9970\uff09\u662f\u4e00\u540d\u5907\u53d7\u6b22\u8fce\u7684\u6d88\u9632\u5458\uff0c\u67d0\u665a\u5728\u6267\u884c\u5b8c\u4efb\u52a1\u540e\uff0c\u4ed6\u8fdb\u5165\u4fbf\u5229\u5e97\u8d2d\u7269\uff0c\u5374\u906d\u9047\u7387\u9886\u624b\u4e0b\u62a2\u593a\u5730\u76d8\u7684\u9ed1\u5e2e\u5934\u5b50\u6234\u7ef4\u00b7\u9ed1\u6839\uff08\u6587\u68ee\u7279\u00b7\u8bfa\u8d39\u5965VincentDOnofrio\u9970\uff09\uff0c\u5e97\u957f\u53ca\u5176\u5bb6\u4eba\u88ab\u6740\uff0c\u6770\u91cc\u7c73\u4fa5\u5e78\u9003\u8131\u3002\u4e4b\u540e\u4ed6\u4f5c\u4e3a\u8bc1\u4eba\u6307\u8ba4\u51f6\u624b\uff0c\u4f46\u5bf9\u6234\u7ef4\u4e3a\u4eba\u6781\u5176\u4e86\u89e3\u7684\u8b66\u5b98\u8fc8\u514b\u00b7\u585e\u62c9\uff08\u5e03\u9c81\u65af\u00b7\u5a01\u5229\u65afBruceWillis\u9970\uff09\u529d\u8bf4\u6770\u91cc\u7c73\u63a5\u53d7\u8bc1\u4eba\u4fdd\u62a4\uff0c\u5e76\u9690\u59d3\u57cb\u540d\uff0c\u4ee5\u9632\u6b62\u906d\u5230\u6234\u7ef4\u7684\u65e0\u60c5\u62a5\u590d\u3002\u4f46\u662f\u4ed6\u7684\u884c\u8e2a\u5f88\u5feb\u88ab\u6234\u7ef4\u4e00\u4f19\u53d1\u73b0\uff0c\u4ed6\u4e0e\u5973\u53cb\u5854\u8389\u4e9a\uff08\u7f57\u838e\u91cc\u5965\u00b7\u9053\u68eeRosarioDawson\u9970\uff09\u906d\u5230\u4f0f\u51fb\uff0c\u5973\u53cb\u66f4\u8eab\u53d7\u91cd\u4f24\u3002\u7a77\u51f6\u6781\u6076\u7684\u6234\u7ef4\uff0c\u52bf\u8981\u5c06\u6770\u91cc\u7c73\u903c\u5411\u6b7b\u8def\uff0c\u56f0\u517d\u72b9\u6597\uff0c\u6770\u91cc\u7c73\u5c55\u5f00\u7edd\u6b7b\u53cd\u6297\u2026\u2026","doubanid":"6875610","mark":"5.5","setnumber":"1","playcount":444,"playlist":[{"site":"imgo","list":[{"name":"\u7b2c1\u96c6","url":"http:\/\/www.hunantv.com\/v\/3\/104466\/f\/1067322.html"}]}],"new_top":[{"id":"21166","cation":10,"idesc":"","title":"\u5ff5\u5ff5","qxd":"\u84dd\u5149","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2239530046.jpg","mark":"7.3","banben":"\u8bc4\u5206\uff1a7.3"},{"id":"67242","cation":10,"idesc":"","title":"\u9053\u58eb\u51fa\u5c712:\u4f0f\u9b54\u519b\u56e2","qxd":"\u8d85\u6e05","img":"http:\/\/i3.letvimg.com\/lc03_isvrs\/201506\/09\/16\/33\/9ca352bd-8ad8-46cd-b519-248a341a2586.jpg","mark":"7.1","banben":"\u8bc4\u5206\uff1a7.1"},{"id":"57955","cation":10,"idesc":"","title":"\u54c6\u5566A\u68a6:\u4f34\u6211\u540c\u884c","qxd":"\u84dd\u5149","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2209761319.jpg","mark":"8.2","banben":"\u8bc4\u5206\uff1a8.2"},{"id":"58378","cation":10,"idesc":"","title":"\u6211\u7684\u4e2a\u795e\u554a","qxd":"\u84dd\u5149","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2243803873.jpg","mark":"8.3","banben":"\u8bc4\u5206\uff1a8.3"},{"id":"61207","cation":10,"idesc":"","title":"\u51b2\u950b\u8f66 \u56fd\u8bed","qxd":"\u84dd\u5149","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2233196307.jpg","mark":"6.9","banben":"\u8bc4\u5206\uff1a6.9"},{"id":"62223","cation":10,"idesc":"","title":"\u8d85\u80fd\u67e5\u6d3e","qxd":"\u84dd\u5149","img":"http:\/\/img4.douban.com\/view\/photo\/photo\/public\/p2240110789.jpg","mark":"7.2","banben":"\u8bc4\u5206\uff1a7.2"},{"id":"66889","cation":10,"idesc":"","title":"\u957f\u5bff\u5546\u4f1a","qxd":"\u84dd\u5149","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2231115344.jpg","mark":"7.9","banben":"\u8bc4\u5206\uff1a7.9"}]}
	
	
	*/
	
	/**
	 * {"id":"58178","type":"2","title":"\u53ea\u56e0\u5355\u8eab\u5728\u4e00\u8d77","banben":" 24\u96c6\u5168","img":"http:\/\/www.sinaimg.cn\/dy\/slidenews\/4_img\/2015_01\/704_1521141_548775.jpg","year":"2015","area":"\u5927\u9646","update":"0","dur":"45","language":"\u56fd\u8bed","cate":"\u60c5\u611f\/\u5076\u50cf\/\u8a00\u60c5","director":"\u5c39\u5c1a\u6d69","actor":"\u90d1\u5143\u7545\/\u6c6a\u4e1c\u57ce\/\u5f90\u7490\/\u5f20\u99a8\u4e88\/\u9648\u7d2b\u51fd\/\u97e9\u627f\u7fbd\/\u6234\u9633\u5929\/\u5b59\u749e","info":"\u3010\u4f60\u597d\uff0c\u672c\u8282\u76ee\u5982\u9047\u65e0\u6cd5\u64ad\u653e\uff0c\u8bf7\u5728\u8bbe\u7f6e-\u64ad\u653e\u8bbe\u7f6e-\u89e3\u7801\u5668\u9009\u62e9-\u8f6f\u89e3\u3002\u30118\u4f4d\u5355\u8eab\u7537\u5973\uff0c\u4ee5\u5355\u8eab\u4e3a\u5165\u4f4f\u6761\u4ef6\u4f4f\u8fdb\u7edf\u4e00\u5316\u7ba1\u7406\u7684\u5355\u8eab\u516c\u5bd3\uff0c\u5728\u8fd9\u5ea7\u516c\u5bd3\u4e2d\uff0c\u604b\u7231\u88ab\u7981\u6b62\uff0c\u5355\u8eab\u88ab\u63d0\u5021\uff0c\u4e00\u65e6\u8c01\u7834\u574f\u89c4\u5219\uff0c\u5219\u4f1a\u88ab\u5254\u9664\u51fa\u5355\u8eab\u516c\u5bd3\u3002\u6545\u4e8b\u8d70\u5411\u6574\u4f53\u8bbe\u5b9a\u878d\u5165\u5927\u91cf\u771f\u4eba\u79c0\u5143\u7d20\uff0c\u5927\u6709\u771f\u5b9e\u7248\u201c\u9965\u997f\u6e38\u620f\u201d\u7684\u8d8b\u52bf\u3002\u540c\u65f6\uff0c\u5267\u4e2d\u867d\u7136\u662f\u770b\u4f3c\u666e\u901a\u7684\u82e5\u5e72\u4f4d\u201c\u5355\u8eab\u8d35\u65cf\u201d\uff0c\u4f46\u4ed6\u4eec\u6bcf\u4e2a\u4eba\u80cc\u540e\u5374\u6709\u7740\u4e0d\u4e3a\u4eba\u77e5\u7684\u79d8\u5bc6\uff0c\u9690\u85cf\u8eab\u4efd\u7684\u5bcc\u4e8c\u4ee3\u6f2b\u753b\u5bb6\u3001\u611f\u60c5\u53d7\u632b\u7684\u5973\u63d2\u753b\u5e08\u3001\u5367\u5e95\u5355\u8eab\u516c\u5bd3\u7684\u8b66\u5bdf\u4e00\u7cfb\u5217\u6027\u683c\u8fe5\u5f02\u7684\u4eba\u7269\uff0c\u4e0a\u6f14\u7cbe\u5f69\u7eb7\u5448\u7684\u6545\u4e8b\u3002","doubanid":"26206968","mark":"5.7","setnumber":"24","playcount":640,"playlist":[{"site":"imgo","list":[{"name":"\u7b2c1\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1071994.html"},{"name":"\u7b2c2\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1072000.html"},{"name":"\u7b2c3\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1073272.html"},{"name":"\u7b2c4\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1073292.html"},{"name":"\u7b2c5\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1077954.html"},{"name":"\u7b2c6\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1077952.html"},{"name":"\u7b2c7\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1080063.html"},{"name":"\u7b2c8\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1080058.html"},{"name":"\u7b2c9\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1086219.html"},{"name":"\u7b2c10\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1086213.html"},{"name":"\u7b2c11\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1087037.html"},{"name":"\u7b2c12\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1087052.html"},{"name":"\u7b2c13\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1090557.html"},{"name":"\u7b2c14\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1090539.html"},{"name":"\u7b2c15\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1091240.html"},{"name":"\u7b2c16\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1091223.html"},{"name":"\u7b2c17\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1094314.html"},{"name":"\u7b2c18\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1094311.html"},{"name":"\u7b2c19\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1094942.html"},{"name":"\u7b2c20\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1094939.html"},{"name":"\u7b2c21\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1099283.html"},{"name":"\u7b2c22\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1099277.html"},{"name":"\u7b2c23\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1100320.html"},{"name":"\u7b2c24\u96c6","url":"http:\/\/www.hunantv.com\/v\/2\/101595\/f\/1100316.html"}]}],"new_top":[{"id":"67168","cation":10,"idesc":"","title":"\u82b1\u5343\u9aa8","qxd":"\u8d85\u6e05","img":"https:\/\/s.doubanio.com\/view\/photo\/raw\/public\/p2248252738.jpg","mark":"7.2","banben":"\u66f4\u65b0\u81f34\u96c6"},{"id":"66863","cation":10,"idesc":"","title":"\u6293\u4f4f\u5f69\u8679\u7684\u7537\u4eba","qxd":"\u8d85\u6e05","img":"http:\/\/img31.mtime.cn\/pi\/2015\/05\/26\/093747.20682393_1000X1000.jpg","mark":"4.8","banben":"34\u96c6\u5168"},{"id":"66853","cation":10,"idesc":"","title":"\u5211\u8b66\u961f\u957f","qxd":"\u8d85\u6e05","img":"http:\/\/i.gtimg.cn\/qqlive\/img\/jpgcache\/files\/qqvideo\/l\/lv3wkqmqkakcido.jpg","mark":"8.4","banben":"36\u96c6\u5168"},{"id":"66891","cation":10,"idesc":"","title":"\u98ce\u4e91\u5929\u5730","qxd":"\u8d85\u6e05","img":"http:\/\/imgsrc.baidu.com\/forum\/pic\/item\/3c0bfd19ebc4b745e7ba57c1cefc1e178b82159d.jpg","mark":"7.0","banben":"\u66f4\u65b0\u81f336\u96c6"},{"id":"66879","cation":10,"idesc":"","title":"\u524d\u592b\u6c42\u7231\u8bb0","qxd":"\u8d85\u6e05","img":"http:\/\/img2.cache.netease.com\/photo\/0003\/2015-05-22\/AQ7E7DI400B70003.jpg","mark":"5.8","banben":"\u66f4\u65b0\u81f330\u96c6"},{"id":"66774","cation":10,"idesc":"","title":"\u5c4c\u4e1d\u7537\u58eb \u7b2c\u56db\u5b63","qxd":"\u8d85\u6e05","img":"http:\/\/photocdn.sohu.com\/20150426\/Img411921751.jpg","mark":"8.0","banben":"\u66f4\u65b0\u81f34\u96c6"},{"id":"61646","cation":10,"idesc":"","title":"\u540d\u4fa6\u63a2\u72c4\u4ec1\u6770","qxd":"\u8d85\u6e05","img":"http:\/\/img3.douban.com\/view\/photo\/photo\/public\/p2224402040.jpg","mark":"7.3","banben":"12\u96c6\u5168"}]}
	 */
	
}
