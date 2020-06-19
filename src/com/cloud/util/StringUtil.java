package com.cloud.util;

import groovy.json.JsonException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;

/**
 * @author ethan
 * 上午10:03:06 2-5-16
 */
public class StringUtil {
	private static DecimalFormat df = new DecimalFormat("#.0");

	/**
	 * 判断指定的字符串是否为数字
	 * @param str 字符串
	 * @return 返回boolean
	 */

	public static boolean isNum(String str) {
		String regex = "9";
		if (str == null)
			return false;
		if (str.length() == 0)
			return false;
		for (int i = 0; i < str.length(); i++) {
			if (regex.indexOf(str.charAt(i)) == -1)
				return false;
		}
		return true;
	}

	/**
	 * 把字符串中的带‘与"转成\'与\"
	 * @param orgStr
	 * @return
	 */
	public static String convertQuot(String orgStr) {
		return orgStr.replace("'", "\\'").replace("\"", "\\\"");
	}

	/**
	 * 把字符串中的带.与;转成\
	 * @param orgStr
	 * @return
	 */
	public static String convertPoint(String orgStr) {
		return orgStr.replace(".", "\\").replace(";", "\\");
	}

	/**
	 * 把字符串中的带_与"转成空字符串
	 * @param orgStr
	 * @return
	 */
	public static String convertUnderLine(String orgStr) {
		String objectName="";
		for(String org : orgStr.split("_")){
			if(objectName.equals("")){
				objectName += org;
			}else{
				objectName += initialStrToUpper(org);
			}
		}
		return objectName;
	}

	/**
	 * xx_xx、 xx_Xx、  Xx_xx、 xxXx  统一转换为：xx_xx
	 * @param str
	 * @return
	 */
	public static String string2underline(String str){
		str = str.replaceAll("[A-Z]","_$0").replaceAll("_+","_");;
		str = str.toLowerCase();
		if(str.startsWith("_")) str = str.replaceFirst("_", "");
		return str;
	}

	/**
	 * xx_xx、 xx_Xx、  Xx_xx、 xxXx  统一转换为：xx.xx
	 * @param str
	 * @return
	 */
	public static String string2points(String str){
		str = StringUtil.string2underline(str);
		str = str.replaceAll("_", ".");
		return str;
	}

	/**
	 * xx_xx、 xx_Xx、  Xx_xx、 xxXx  统一转换为：xxXx
	 * @param str
	 * @return
	 */
	public static String string2hump(String str){
		str = StringUtil.string2underline(str);
		str = StringUtil.convertUnderLine(str);
		return str;
	}


	/**
	 *
	 * HTML实体编码转成普通的编码
	 *
	 * @param dataStr
	 *
	 * @return
	 */
	public static String htmlEntityToString(final String dataStr) {
		int start = 0;
		int end = 0;
		final StringBuffer buffer = new StringBuffer();
		while (start > -1) {
			int system = 10;// 进制
			if (start == 0) {
				int t = dataStr.indexOf("&#");
				if (start != t)
					start = t;
			}
			end = dataStr.indexOf(";", start + 2);
			String charStr = "";
			if (end != -1) {
				charStr = dataStr.substring(start + 2, end);
				// 判断进制
				char s = charStr.charAt(0);
				if (s == 'x' || s == 'X') {
					system = 16;
					charStr = charStr.substring(1);
				}
			}
			// 转换
			try {
				char letter = (char) Integer.parseInt(charStr, system);
				buffer.append(new Character(letter).toString());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			// 处理当前unicode字符到下一个unicode字符之间的非unicode字符
			start = dataStr.indexOf("&#", end);
			if (start - end > 1) {
				buffer.append(dataStr.substring(end + 1, start));
			}
			// 处理最后面的非unicode字符
			if (start == -1) {
				int length = dataStr.length();
				if (end + 1 != length) {
					buffer.append(dataStr.substring(end + 1, length));
				}
			}
		}
		return buffer.toString();
	}

	/**
	 *
	 * 把String转成html实体字符
	 *
	 * @param str
	 *
	 * @return
	 */
	public static String stringToHtmlEntity(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			switch (c) {
			case 0x0A:
				sb.append(c);
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '\'':
				sb.append("&apos;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			default:
				if ((c < ' ') || (c > 0x7E)) {
					sb.append("&#x");
					sb.append(Integer.toString(c, 16));
					sb.append(';');
				} else {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	// 转unicode
	public static String stringToUnicode(String s) {
		String unicode = "";
		char[] charAry = new char[s.length()];
		for (int i = 0; i < charAry.length; i++) {
			charAry[i] = (char) s.charAt(i);
			unicode += "\\u" + Integer.toString(charAry[i], 16);
		}
		return unicode;
	}

	public static String unicodeToString(String unicodeStr) {
		StringBuffer sb = new StringBuffer();
		String str[] = unicodeStr.toUpperCase().split("\\\\U");
		for (int i = 0; i < str.length; i++) {
			if (str[i].equals(""))
				continue;
			char c = (char) Integer.parseInt(str[i].trim(), 16);
			sb.append(c);
		}
		return sb.toString();
	}

	public static String html2Text(String inputString) {
		String htmlStr = inputString; // 含html标签的字符串
		String textStr = "";
		java.util.regex.Pattern p_script;
		java.util.regex.Matcher m_script;
		java.util.regex.Pattern p_style;
		java.util.regex.Matcher m_style;
		java.util.regex.Pattern p_html;
		java.util.regex.Matcher m_html;
		try {
			String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; // 定义script的正则表达式{或<script>]*?>[\s\S]*?<\/script>
			// }
			String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; // 定义style的正则表达式{或<style>]*?>[\s\S]*?<\/style>
			// }
			String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
			p_script = java.util.regex.Pattern.compile(regEx_script,
			java.util.regex.Pattern.CASE_INSENSITIVE);
			m_script = p_script.matcher(htmlStr);
			htmlStr = m_script.replaceAll(""); // 过滤script标签
			p_style = java.util.regex.Pattern.compile(regEx_style,
			java.util.regex.Pattern.CASE_INSENSITIVE);
			m_style = p_style.matcher(htmlStr);
			htmlStr = m_style.replaceAll(""); // 过滤style标签
			p_html = java.util.regex.Pattern.compile(regEx_html,
			java.util.regex.Pattern.CASE_INSENSITIVE);
			m_html = p_html.matcher(htmlStr);
			htmlStr = m_html.replaceAll(""); // 过滤html标签
			textStr = htmlStr;
		} catch (Exception e) {
			System.err.println("Html2Text: " + e.getMessage());
		}
		return textStr;// 返回文本字符串
	}

	/**
	 *
	 * escape编码
	 *
	 * @param src
	 *
	 * @return
	 */
	public static String escape(String src) {
		int i;
		char j;
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length() * 6);
		for (i = 0; i < src.length(); i++) {
			j = src.charAt(i);
			if (Character.isDigit(j) || Character.isLowerCase(j)
					|| Character.isUpperCase(j))
				tmp.append(j);
			else if (j < 256) {
				tmp.append("%");
				if (j < 16)
					tmp.append("0");
				tmp.append(Integer.toString(j, 16));
			} else {
				tmp.append("%u");
				tmp.append(Integer.toString(j, 16));
			}
		}
		return tmp.toString();
	}

	public static String unescape(String src) {
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length());
		int lastPos = 0, pos = 0;
		char ch;
		while (lastPos < src.length()) {
			pos = src.indexOf("%", lastPos);
			if (pos == lastPos) {
				if (src.charAt(pos + 1) == 'u') {
					ch = (char) Integer.parseInt(
					src.substring(pos + 2, pos + 6), 16);
					tmp.append(ch);
					lastPos = pos + 6;
				} else {
					ch = (char) Integer.parseInt(
					src.substring(pos + 1, pos + 3), 16);
					tmp.append(ch);
					lastPos = pos + 3;
				}
			} else {
				if (pos == -1) {
					tmp.append(src.substring(lastPos));
					lastPos = src.length();
				} else {
					tmp.append(src.substring(lastPos, pos));
					lastPos = pos;
				}
			}
		}
		return tmp.toString();
	}

	public static String formatDou2Str(Double d) {
		if (d == null)
			return "0";
		return df.format(d);
	}

	public static String decodeStr(String encodeparam) {
		try {
			if (encodeparam == null || "".equalsIgnoreCase(encodeparam)) {
				return null;
			}
			return java.net.URLDecoder.decode(encodeparam, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 首字母大写
	 * @param str
	 * @return
	 */
	public static String initialStrToUpper(String str){
		return str.substring(0,1).toUpperCase()+str.substring(1);
//		return str.replace(str.charAt(0), (char)(str.charAt(0)-32));
	}

	/**
	 * 首字母小写
	 * @param str
	 * @return
	 */
	public static String initialStrToLower(String str){
		return str.substring(0,1).toLowerCase()+str.substring(1);
//		return str.replace(str.charAt(0), (char)(str.charAt(0)-32));
	}

	/**
	 * 替换斜杠
	 * @param str
	 * @return
	 */
	public static String replaceSlash(String str){
		String result = str.replaceAll("//", ".");
		result = result.replaceAll("/", ".");
		result = result.replaceAll("\\\\", ".");
		return result;
	}

	/**
	 * 是否加“.”
	 * @param str
	 * @param addStr
	 * @return
	 */
	public static String addPoint(String str,String addStr){
		String result = str ;
		if(str.endsWith(".")){
			result += addStr;
		}else{
			result += "."+addStr;
		}
		return result;
	}

	/**
	 * 格式化
	 *
	 * @param jsonStr
	 * @return
	 * @author lizhgb
	 * @Date 2015-10-14 下午1:17:35
	 * @Modified 2017-04-28 下午8:55:35
	 */
	public static String formatJson(String jsonStr) {
		if (null == jsonStr || "".equals(jsonStr))
			return "";
		StringBuilder sb = new StringBuilder();
		char last = '\0';
		char current = '\0';
		int indent = 0;
		boolean isInQuotationMarks = false;
		for (int i = 0; i < jsonStr.length(); i++) {
			last = current;
			current = jsonStr.charAt(i);
			switch (current) {
				case '"':
					if (last != '\\'){
						isInQuotationMarks = !isInQuotationMarks;
					}
					sb.append(current);
					break;
				case '{':
				case '[':
					sb.append(current);
					if (!isInQuotationMarks) {
						sb.append('\n');
						indent++;
						addIndentBlank(sb, indent);
					}
					break;
				case '}':
				case ']':
					if (!isInQuotationMarks) {
						sb.append('\n');
						indent--;
						addIndentBlank(sb, indent);
					}
					sb.append(current);
					break;
				case ',':
					sb.append(current);
					if (last != '\\' && !isInQuotationMarks) {
						sb.append('\n');
						addIndentBlank(sb, indent);
					}
					break;
				default:
					sb.append(current);
			}
		}

		return sb.toString();
	}

	/**
	 * 添加space
	 *
	 * @param sb
	 * @param indent
	 * @author lizhgb
	 * @Date 2015-10-14 上午10:38:04
	 */
	private static void addIndentBlank(StringBuilder sb, int indent) {
		for (int i = 0; i < indent; i++) {
			sb.append('\t');
		}
	}

	public static void main(String[] args) {
//		System.out.println(addPoint("com.afbd.","entity"));
		String replace = "C:/Users/Xps13/.IntelliJIdea2018.3/system/plugins-sandbox/plugins/codeMaker/classes/".replaceFirst("/codeMaker/.+$", "/codeMaker/lib/annotations.jar");
		System.out.println(replace);

	}
}
