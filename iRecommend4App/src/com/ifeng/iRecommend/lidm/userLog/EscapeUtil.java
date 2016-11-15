package com.ifeng.iRecommend.lidm.userLog;

public class EscapeUtil {
	/**
	 * 使用d替代非法字符.
	 * 
	 * @param text
	 * @param d
	 * @return
	 */
	public static String replaceInvaldateCharacter(String text, char d) {
		if (text != null) {
			char[] data = text.toCharArray();
			for (int i = 0; i < data.length; i++) {
				if (!isXMLCharacter(data[i])){
					data[i] = d;
				}
			}
			return new String(data);
		}
		return "";
	}

	/**
	 * 使用空格替代非法字符.
	 * 
	 * @param text
	 * @return
	 */
	public static String replaceInvaldateCharacter(String text) {
		//return replaceInvaldateCharacter(text, (char) 0x20).replaceAll("&\\w{1,6};", " ").replaceAll("&", "&amp;");
		return replaceInvaldateCharacter(text, (char) 0x20);
	}

	/**
	 * 检查字符是否为合法的xml字符
	 * XML规范中规定了允许的字符范围(http://www.w3.org/TR/REC-xml#dt-character): Char ::= #x9
	 * | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
	 * 
	 * @param c
	 * @return
	 */
	private static boolean isXMLCharacter(int c) {
		if (c <= 0xD7FF) {
			if (c >= 0x20)
				return true;
			else
				return c == '\n' || c == '\r' || c == '\t';
		}
		return (c >= 0xE000 && c <= 0xFFFD) || (c >= 0x10000 && c <= 0x10FFFF);
	}
}
