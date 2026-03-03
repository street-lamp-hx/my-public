package com.yiyitech.mf.util;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName EnumUtil.java
 * @Description
 * @createTime 2023年12月25日 15:44:00
 */
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnumUtil {

    public List<String> getEnumValue(String enumClassName, String enumMethodName) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> enumClass = Class.forName("com.yiyitech.ads.enums.".concat(enumClassName));
        Object[] colums = enumClass.getEnumConstants();
        Object colum = colums[0];
        Method method = enumClass.getMethod(enumMethodName);
        List<String> enumColumsLst = (ArrayList<String>)method.invoke(colum);
        if (enumClass.isEnum()) {
            return enumColumsLst;
        } else {
            throw new IllegalArgumentException(enumClassName + " 不是枚举类 ");
        }
    }

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        try {
            EnumUtil aa = new EnumUtil();
            List<String> a = aa.getEnumValue("AdsTargetingColumnsEnum", "getColumnsLst");
            String b = "";
            for (String c : a){
                b+=c+",";
            }
            List<String> aaa = Arrays.asList(b);
            List<String> charList = Arrays.stream(b.split(",")).collect(Collectors.toList());
            aaa.forEach(d ->{
                System.out.println(d);
            });
            charList.forEach(d ->{
                System.out.println(d);
            });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
