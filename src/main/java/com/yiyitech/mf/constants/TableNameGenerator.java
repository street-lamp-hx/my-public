package com.yiyitech.mf.constants;

import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class TableNameGenerator {
    
    public static String generateMonthlyTableName(String prefix, Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
        return prefix + "_" + format.format(date);
    }
    
    public static String generateMonthlyTableName(String prefix, String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = inputFormat.parse(dateString);
            return generateMonthlyTableName(prefix, date);
        } catch (Exception e) {
            // 默认使用当前日期
            return generateMonthlyTableName(prefix, new Date());
        }
    }
}