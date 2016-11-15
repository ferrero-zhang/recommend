package com.ifeng.iRecommend.kedm.Spider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

public class SinaUserPageProcessor implements PageProcessor {
	public Set<String> sina_uids = new HashSet<String>();
	public int page_num = 1;
	public String uid;
	public String[] un = {"kelaughing@163.com","258613612@qq.com"};
	public boolean listener = true;
	public LoginUserInfo sinauser = new LoginUserInfo();
	private Site site = Site.me().setUserAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 Safari/537.36")
			.setRetryTimes(3).setSleepTime(5000)
			/*.addCookie("ALF", "1503296505")
			.addCookie("SCF", "Aq0_2O1-pyh_qToyc9a8e20zPMc2Z4AVEVxENiVBJmzV76o5UY-6ehdzF-ib54FAIo-GOIVWqzBzgxExnfOFvDc.")
			.addCookie("Apache", "7099287896417.081.1471503848480")
			.addCookie("SINAGLOBAL", "5736819556914.27.1433985297248")
			.addCookie("SSOLoginState", "1471509326")
			.addCookie("SUB", "_2A256vTlDDeTxGedG41MU8i_FzD2IHXVZyy2LrDV8PUJbmtAKLWbakW9svQEdjD5x0tJAjxk_9jSQ0LtnoQ..")
			.addCookie("SUBP", "0033WrSXqPxfM725Ws9jqgMF55529P9D9W5OLLgz20K7xzFwGP8iLkhB5JpX5o2p5NHD95Qp1hnpSKzp1KMpWs4DqcjTds8f9cyDdJUkIc-t")
			.addCookie("SUHB", "08JqrSz9g-MHoM")
			.addCookie("TC-Page-G0", "07e0932d682fda4e14f38fbcb20fac81")
			.addCookie("TC-Ugrow-G0", "968b70b7bcdc28ac97c8130dd353b55e")
			.addCookie("TC-V5-G0", "8518b479055542524f4cf5907e498469")
			.addCookie("ULV", "1471503848485:44:1:1:7099287896417.081.1471503848480:1463391815469")
			.addCookie("UOR", "developer.51cto.com,widget.weibo.com,www.baidu.com")
			.addCookie("WBStore", "84c2973cf767effc|undefined")
			.addCookie("WBtopGlobal_register_version", "c80f26b033c3241f")
			.addCookie("_s_tentry", "www.baidu.com")
			.addCookie("login_sid_t", "0ca860afebf9048e35a2b99353a51003")
			.addCookie("un", "806483074@qq.com")
			.addCookie("wvr", "6");*/
	.addCookie("ALF", "1503387052")
	.addCookie("SCF", "AsbS0kOnCZrwEis89X973yGnbxZTIidwZlnBNBvAJ4Wyzrz4Gw4JTKjxdXjyrKhZyMcssrt2WD52gS72nXTnNco.")
	.addCookie("Apache", "6765791641756.087.1471850937856")
	.addCookie("SINAGLOBAL", "7889156042298.655.1470639299627")
	.addCookie("SSOLoginState", "1471851052")
	.addCookie("SUB", "_2A256vtp8DeTxGeVL7loU8C_MzT-IHXVZyky0rDV8PUNbmtAKLUPCkW8aRTJ3ZLRDsv1b3v6iWRD2ibLWqw..")
	.addCookie("SUBP", "0033WrSXqPxfM725Ws9jqgMF55529P9D9WW34jFEaDxZeln.KLqyD8fF5JpX5K2hUgL.FoefSKnfeh27Soe2dJLoI7_.9P9Ldsv79Pz7entt")
	.addCookie("SUHB", "0TPFxNazIrQyKs")
	.addCookie("TC-Page-G0", "07e0932d682fda4e14f38fbcb20fac81")
	.addCookie("TC-Ugrow-G0", "968b70b7bcdc28ac97c8130dd353b55e")
	.addCookie("TC-V5-G0", "8518b479055542524f4cf5907e498469")
	.addCookie("ULV", "1471850937921:1:1:1:6765791641756.087.1471850937856:")
	.addCookie("UOR", "developer.51cto.com,widget.weibo.com,www.baidu.com")
	.addCookie("WBStore", "84c2973cf767effc|undefined")
	.addCookie("WBtopGlobal_register_version", "c80f26b033c3241f")
	.addCookie("_s_tentry", "-")
	.addCookie("login_sid_t", "bb06b4d11e5559bba0a5e13b3c4f2a61")
	.addCookie("un", "kelaughing@163.com")
	.addCookie("wvr", "6");
			

	public SinaUserPageProcessor(String uid){
		this.uid = uid;
		sinauser.setUid(uid);
	}
	@Override
	public Site getSite() {
		// TODO Auto-generated method stub
		return site;
	}

	@Override
	public void process(Page page) {
		// TODO Auto-generated method stub
		//System.out.println(page.getHtml().toString());
		if(page.getUrl().get().contains("follow")){
			List<Selectable> ls = page.getHtml().xpath("//td[@valign=\"top\"]").nodes();
			for(Selectable s : ls){
				if(s.xpath("//td[@valign=\"top\"]/img[@alt=\"V\"]").match()){
					//System.out.println(s.xpath("//td[@valign=\"top\"]/a/text()"));
					if(s.xpath("//td[@valign=\"top\"]/a/text()") != null){
						sinauser.getV().add(s.xpath("//td[@valign=\"top\"]/a/text()").get());
					}
					
				}
			}
			if(page.getUrl().get().endsWith("follow")){
				String num = page.getHtml().xpath("//input[@name=\"mp\"]").get();
				System.out.println(num.substring(num.indexOf("value=\"")+7, num.lastIndexOf("\"")));
				page_num = Integer.parseInt(num.substring(num.indexOf("value=\"")+7, num.lastIndexOf("\"")));
				
				for(int i= 2;i<= page_num;i++){
					page.addTargetRequest(page.getUrl().get() + "?page=" + i);
				}
				page.addTargetRequest(page.getUrl().get().replace("follow", "info"));
			}
			
		}else if(page.getUrl().get().contains("info")){
			List<String> ss = page.getHtml().xpath("//div[@class=\"c\"]").all();
			for(String s : ss){
				if(!s.contains("昵称"))
					continue;
				String temp = s.replaceAll("<[.[^<]]*>", "");
				System.out.println("user info "+temp);
				if(temp.contains("标签:")){
					String temptags = temp.substring(temp.indexOf("标签:")+3, temp.lastIndexOf("更多"));
					temp = temp.substring(0, temp.indexOf("标签:"));
					String[] tags = temptags.split("\n");
					StringBuffer sb = new StringBuffer();
					for(String t : tags){
						if(t.trim().equals(""))
							continue;
						sb.append(t.replace("&nbsp", "").replace(";", "")).append("#");
						//System.out.println(t.replace("&nbsp", "").replace(";", ""));
					}
					sinauser.setTags(sb.toString());
				}
				
				String[] sss = temp.trim().split("\n");
				for(String info : sss){
					if(info.trim().contains("昵称")){
						String[] kv = info.trim().split(":");
						if(kv.length == 2){
							sinauser.setName(kv[1]);
						}
					}
					if(info.trim().contains("认证")){
						String[] kv = info.trim().split(":");
						if(kv.length == 2){
							sinauser.setVerified(kv[1]);
						}
					}
					if(info.trim().contains("性别")){
						String[] kv = info.trim().split(":");
						if(kv.length == 2){
							sinauser.setGender(kv[1]);
						}
					}
					if(info.trim().contains("简介")){
						String[] kv = info.trim().split(":");
						if(kv.length == 2){
							sinauser.setDescription(kv[1]);
						}
					}
					if(info.trim().contains("生日")){
						String[] kv = info.trim().split(":");
						if(kv.length == 2){
							sinauser.setBirthday(kv[1]);
						}
					}
					if(info.trim().contains("学校")){
						String[] kv = info.trim().split(":");
						if(kv.length == 2){
							sinauser.setSchool(kv[1]);
						}
					}
				}
			}
			Gson gson = new Gson();
			System.out.println(uid + " sina info is "+gson.toJson(sinauser,LoginUserInfo.class));
		}else{
			List<String> links = page.getHtml().xpath("//a").all();
			for(String a : links){
				if(a.contains("资料</a>")){
					String infourl = a.substring(a.indexOf("href=\"")+6,a.indexOf("\">"));
					page.addTargetRequest(infourl.replace("info", "follow"));
				}
			}
		}
		int temp = (int)(10000 + Math.random()*(30000-10000+1));
		System.out.println("sleep " + temp);
		if(listener){
			site.addCookie("un", "kelaughing@163.com");
			listener = false;
		}else{
			site.addCookie("un", "258613612@qq.com");
			listener = true;
		}
		try {
			Thread.sleep(temp);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
