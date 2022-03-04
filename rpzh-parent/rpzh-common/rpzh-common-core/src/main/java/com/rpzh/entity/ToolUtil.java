package com.rpzh.entity;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @Author liubc
 * @create 2021/5/18 17:03
 * 高频方法工具类
 */
public class ToolUtil {

    /**
     * 获取随机位数的字符串
     *
     * @author fengshuonan
     * @Date 2017/8/24 14:09
     */
    public static String getRandomString(int length) {
        String base = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }


    /**
     * 获取异常的具体信息
     *
     * @author fengshuonan
     * @Date 2017/3/30 9:21
     * @version 2.0
     */
    public static String getExceptionMsg(Exception e) {
        StringWriter sw = new StringWriter();
        try {
            e.printStackTrace(new PrintWriter(sw));
        } finally {
            try {
                sw.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return sw.getBuffer().toString().replaceAll("\\$", "T");
    }

    /**
     * 比较两个对象是否相等。<br>
     * 相同的条件有两个，满足其一即可：<br>
     * 1. obj1 == null && obj2 == null; 2. obj1.equals(obj2)
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @return 是否相等
     */
    public static boolean equals(Object obj1, Object obj2) {
        return (obj1 != null) ? (obj1.equals(obj2)) : (obj2 == null);
    }

    /**
     * 计算对象长度，如果是字符串调用其length函数，集合类调用其size函数，数组调用其length属性，其他可遍历对象遍历计算长度
     *
     * @param obj 被计算长度的对象
     * @return 长度
     */
    public static int length(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length();
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).size();
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).size();
        }

        int count;
        if (obj instanceof Iterator) {
            Iterator<?> iter = (Iterator<?>) obj;
            count = 0;
            while (iter.hasNext()) {
                count++;
                iter.next();
            }
            return count;
        }
        if (obj instanceof Enumeration) {
            Enumeration<?> enumeration = (Enumeration<?>) obj;
            count = 0;
            while (enumeration.hasMoreElements()) {
                count++;
                enumeration.nextElement();
            }
            return count;
        }
        if (obj.getClass().isArray() == true) {
            return Array.getLength(obj);
        }
        return -1;
    }

    /**
     * 对象中是否包含元素
     *
     * @param obj     对象
     * @param element 元素
     * @return 是否包含
     */
    public static boolean contains(Object obj, Object element) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof String) {
            if (element == null) {
                return false;
            }
            return ((String) obj).contains(element.toString());
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).contains(element);
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).values().contains(element);
        }

        if (obj instanceof Iterator) {
            Iterator<?> iter = (Iterator<?>) obj;
            while (iter.hasNext()) {
                Object o = iter.next();
                if (equals(o, element)) {
                    return true;
                }
            }
            return false;
        }
        if (obj instanceof Enumeration) {
            Enumeration<?> enumeration = (Enumeration<?>) obj;
            while (enumeration.hasMoreElements()) {
                Object o = enumeration.nextElement();
                if (equals(o, element)) {
                    return true;
                }
            }
            return false;
        }
        if (obj.getClass().isArray() == true) {
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                Object o = Array.get(obj, i);
                if (equals(o, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 对象是否不为空(新增)
     *
     * @param
     * @return
     */
    public static boolean isNotEmpty(Object o) {
        return !isEmpty(o);
    }

    /**
     * 对象是否为空
     *
     * @param
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        }
        if (o instanceof String) {
            if (o.toString().trim().equals("")) {
                return true;
            }
        } else if (o instanceof List) {
            if (((List) o).size() == 0) {
                return true;
            }
        } else if (o instanceof Map) {
            if (((Map) o).size() == 0) {
                return true;
            }
        } else if (o instanceof Set) {
            if (((Set) o).size() == 0) {
                return true;
            }
        } else if (o instanceof Object[]) {
            if (((Object[]) o).length == 0) {
                return true;
            }
        } else if (o instanceof int[]) {
            if (((int[]) o).length == 0) {
                return true;
            }
        } else if (o instanceof long[]) {
            if (((long[]) o).length == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 对象组中是否存在 Empty Object
     *
     * @param os 对象组
     * @return
     */
    public static boolean isOneEmpty(Object... os) {
        for (Object o : os) {
            if (isEmpty(o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 对象组中是否全是 Empty Object
     *
     * @param os
     * @return
     */
    public static boolean isAllEmpty(Object... os) {
        for (Object o : os) {
            if (!isEmpty(o)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否为数字
     *
     * @param obj
     * @return
     */
    public static boolean isNum(Object obj) {
        try {
            Integer.parseInt(obj.toString());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 如果为空, 则调用默认值
     *
     * @param str
     * @return
     */
    public static Object getValue(Object str, Object defaultValue) {
        if (isEmpty(str)) {
            return defaultValue;
        }
        return str;
    }


    /**
     * 强转->string,并去掉多余空格
     *
     * @param str
     * @return
     */
    public static String toStr(Object str) {
        return toStr(str, "");
    }

    /**
     * 强转->string,并去掉多余空格
     *
     * @param str
     * @param defaultValue
     * @return
     */
    public static String toStr(Object str, String defaultValue) {
        if (null == str) {
            return defaultValue;
        }
        return str.toString().trim();
    }


    /**
     * 强转->int
     *
     * @param obj
     * @return
     */
//	public static int toInt(Object value) {
//		return toInt(value, -1);
//	}

    /**
     * 强转->int
     *
     * @param obj
     * @param defaultValue
     * @return
     */
//	public static int toInt(Object value, int defaultValue) {
//		return Convert.toInt(value, defaultValue);
//	}

    /**
     * 强转->long
     *
     * @param obj
     * @return
     */
//	public static long toLong(Object value) {
//		return toLong(value, -1);
//	}

    /**
     * 强转->long
     *
     * @param obj
     * @param defaultValue
     * @return
     */
//	public static long toLong(Object value, long defaultValue) {
//		return Convert.toLong(value, defaultValue);
//	}
//
//	public static String encodeUrl(String url) {
//		return URLKit.encode(url, CharsetKit.UTF_8);
//	}
//
//	public static String decodeUrl(String url) {
//		return URLKit.decode(url, CharsetKit.UTF_8);
//	}

    /**
     * map的key转为小写
     *
     * @param map
     * @return Map<String, Object>
     */
    public static Map<String, Object> caseInsensitiveMap(Map<String, Object> map) {
        Map<String, Object> tempMap = new HashMap<>();
        for (String key : map.keySet()) {
            tempMap.put(key.toLowerCase(), map.get(key));
        }
        return tempMap;
    }

    /**
     * 获取map中第一个数据值
     *
     * @param <K> Key的类型
     * @param <V> Value的类型
     * @param map 数据源
     * @return 返回的值
     */
    public static <K, V> V getFirstOrNull(Map<K, V> map) {
        V obj = null;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            obj = entry.getValue();
            if (obj != null) {
                break;
            }
        }
        return obj;
    }

    /**
     * 创建StringBuilder对象
     *
     * @return StringBuilder对象
     */
    public static StringBuilder builder(String... strs) {
        final StringBuilder sb = new StringBuilder();
        for (String str : strs) {
            sb.append(str);
        }
        return sb;
    }

    /**
     * 创建StringBuilder对象
     *
     * @return StringBuilder对象
     */
    public static void builder(StringBuilder sb, String... strs) {
        for (String str : strs) {
            sb.append(str);
        }
    }

    /**
     * 去掉指定后缀
     *
     * @param str    字符串
     * @param suffix 后缀
     * @return 切掉后的字符串，若后缀不是 suffix， 返回原字符串
     */
    public static String removeSuffix(String str, String suffix) {
        if (isEmpty(str) || isEmpty(suffix)) {
            return str;
        }

        if (str.endsWith(suffix)) {
            return str.substring(0, str.length() - suffix.length());
        }
        return str;
    }

    /**
     * 判断是否是windows操作系统
     *
     * @author stylefeng
     * @Date 2017/5/24 22:34
     */
    public static Boolean isWinOs() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取临时目录
     *
     * @author stylefeng
     * @Date 2017/5/24 22:35
     */
    public static String getTempPath() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * 把一个数转化为int
     *
     * @author fengshuonan
     * @Date 2017/11/15 下午11:10
     */
    public static Integer toInt(Object val) {
        if (val instanceof Double) {
            BigDecimal bigDecimal = new BigDecimal((Double) val);
            return bigDecimal.intValue();
        } else {
            return Integer.valueOf(val.toString());
        }

    }

    /**
     * 获取项目路径
     */
    public static String getWebRootPath(String filePath) {
        try {
            String path = ToolUtil.class.getClassLoader().getResource("").toURI().getPath();
            path = path.replace("/WEB-INF/classes/", "");
            path = path.replace("/target/classes/", "");
            path = path.replace("file:/", "");
            if (ToolUtil.isEmpty(filePath)) {
                return path;
            } else {
                return path + "/" + filePath;
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取文件后缀名 不包含点
     */
    public static String getFileSuffix(String fileWholeName) {
        if (ToolUtil.isEmpty(fileWholeName)) {
            return "none";
        }
        int lastIndexOf = fileWholeName.lastIndexOf(".");
        return fileWholeName.substring(lastIndexOf + 1);
    }

    /**
     * 将Byte[] 16进制转ASCII
     *
     * @param hex
     * @return
     */
    public static String hexConvertAcsii(byte[] hex) {
        byte[] hexBuff = new byte[hex.length];
        for (int i = 0; i < hex.length; i++) {
            hexBuff[i] = (byte) Integer.parseInt(String.valueOf(hex[i]), 16);
        }
        String ascii = null;
        try {
            ascii = new String(hexBuff, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ascii;
    }

    /**
     * 字符串补充前缀
     *
     * @param prefix
     * @param dataLength
     * @param data
     * @return
     */
    public static String addingPrefix(String prefix, int dataLength, String data) {
        if (data.length() < dataLength) {
            return prefix + data;
        } else {
            return data;
        }
    }

    /**
     * //最小长度为8位，应至少包括数字、大写字母、小写字母、特殊字符中的三种字符的正则表达式
     * @param comparePwd 需要比较的密码
     */
    /**
     * 2020-1-8 zhenglb
     **/
    public static boolean getComparePassWord(String comparePwd) {
        String reg = "^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\\W_]+$)(?![a-z0-9]+$)(?![a-z\\W_]+$)(?![0-9\\W_]+$)[a-zA-Z0-9\\W_]{8,}$";
        if (!(comparePwd.matches(reg))) {
            return true;
            //throw new UserException(UserException.UserType.PWD_FORMAT_NOT_MATCH, "新密码至少8位包括数字、大写字母、小写字母、特殊字符中的三种字符");
        }
        return false;
    }

    public static <T> T sqlBeanTransition(Object dtoOrVo, T pojo) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<?> constructor = pojo.getClass().getConstructor(dtoOrVo.getClass());
        pojo = (T) constructor.newInstance(dtoOrVo);
        return pojo;
    }


    /**
     * 是否已经过超过几分钟
     *
     * @param date 需要判断的时间
     * @return
     */
    public static boolean isLongerthanMinute(Date date, int numTime) {

        /*Calendar cal = Calendar.getInstance();

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        cal1.add(Calendar.MINUTE, +numTime);

        return cal1.compareTo(cal) == 1?false:true;*/
        Calendar dateOne = Calendar.getInstance();
        Calendar dateTwo = Calendar.getInstance();
        dateOne.setTime(new Date());    //设置为当前系统时间
        dateTwo.setTime(date);    //获取数据库中的时间
        long timeOne = dateOne.getTimeInMillis();
        long timeTwo = dateTwo.getTimeInMillis();
        long minute = (timeOne - timeTwo) / (1000 * 60);//转化minute
        //判断账户锁定时间是否大于30分钟
        if (minute < numTime) {
            //小于30分钟，锁定用户
            return false;
        }
        return true;
    }
}