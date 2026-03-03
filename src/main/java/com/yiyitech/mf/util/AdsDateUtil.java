package com.yiyitech.mf.util;


import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName DateUtil.java
 * @Description
 * @createTime 2024年01月10日 14:36:00
 */
public class AdsDateUtil {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_HHMM_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TIME_PARSE_FORMATTER = new DateTimeFormatterBuilder()
                    .parseStrict()
                    .appendPattern("HH:mm")
                    .optionalStart()
                    .appendPattern(":ss")
                    .optionalEnd()
                    .toFormatter();

    public static String xAmzDate() {
        return DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
    }

    public List<String> getDatesBetween(String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        List<String> datesInRange = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (!startDate.isAfter(endDate)) {
            datesInRange.add(startDate.format(formatter));
            startDate = startDate.plusDays(1);
        }
        return datesInRange;
    }

    public static Map<String, String> getLastDays(int num) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(num - 1);

        Map<String, String> dateRange = new HashMap<>();
        dateRange.put("startDateStr", startDate.format(formatter));
        dateRange.put("endDateStr", endDate.format(formatter));
        return dateRange;
    }

    /**
     * 根据传入的 yyyy-MM-dd 字符串，生成一天的起止时间（00:00:00 到 23:59:59）
     * @param startDateStr 开始日期字符串（yyyy-MM-dd）
     * @param endDateStr   结束日期字符串（yyyy-MM-dd）
     * @return Map 包含 formattedStartTime 和 formattedEndTime
     */
    public static Map<String, String> buildFullDayTimeRange(String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
        LocalDate endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);

        //00:00:00
        LocalDateTime startDateTime = startDate.atStartOfDay();
        //23:59:59
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        Map<String, String> result = new HashMap<>();
        result.put("startTime", startDateTime.format(DATETIME_FORMATTER));
        result.put("endTime", endDateTime.format(DATETIME_FORMATTER));
        return result;
    }

    /**
     * 将 java.sql.Timestamp 格式化为 yyyy-MM-dd HH:mm:ss 字符串
     * @param timestamp 时间戳
     * @return 格式化字符串
     */
    public static String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return null;
        return timestamp.toLocalDateTime().format(DATETIME_FORMATTER);
    }

    /**
     * 将 java.util.Date 格式化为 yyyy-MM-dd HH:mm:ss 字符串
     * @param date 日期对象
     * @return 格式化字符串
     */
    public static String formatDate(Date date) {
        if (date == null) return "00-00-00 00:00:00";
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DATETIME_FORMATTER);
    }

    /**
     * 将 yyyy-MM-dd 格式的字符串转为 java.util.Date
     * @param dateStr 日期字符串
     * @return java.util.Date 对象
     */
    public static Date parseDateYmd(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        LocalDate localDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 从 "yyyy-MM-dd HH:mm:ss" 格式的字符串中提取时分部分（HH:mm）
     * @param dateTimeStr 日期时间字符串，例如 "2025-08-26 09:12:00"
     * @return 时分字符串，例如 "09:12"
     */
    public static String extractHHmm(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("dateTimeStr 不能为空");
        }
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        return dateTime.toLocalTime().format(TIME_HHMM_FORMATTER);
    }


    /**
     * 将 LocalTime 转为 HH:mm 字符串（例如 09:12）
     * @param time LocalTime 对象
     * @return 格式化后的字符串
     */
    public static String formatTimeToHHmm(LocalTime time) {
        if (time == null) return null;
        return time.format(TIME_HHMM_FORMATTER);
    }

    /**
     * 将 "HH:mm" 或 "HH:mm:ss" 字符串解析为 LocalTime
     * @param timeStr 如 "09:12" 或 "09:12:34"
     * @return LocalTime 对象
     * @throws IllegalArgumentException 当格式不合法时抛出
     */
    public static LocalTime parseTimeHHmm(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr.trim(), TIME_PARSE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("时间格式应为 HH:mm 或 HH:mm:ss，例如 09:12", e);
        }
    }

    /**
     * 获取美西整周日期
     * @param weeksAgo
     * weeksAgo = -1 → 本周日-下周六
     * weeksAgo = 0 → 上周日-本周六
     * weeksAgo = 1 → 上上周日-上周六
     * ……
     * @return
     */
    public static Map<String, String> weekUtc(int weeksAgo) {
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime thisWeekSunday = nowUtc
                .with(java.time.temporal.ChronoField.DAY_OF_WEEK, 7)//本周周日 00:00:00Z
                .truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        ZonedDateTime start = thisWeekSunday.minusWeeks(weeksAgo + 1);//目标周 周日 00:00:00Z
        ZonedDateTime end   = start.plusDays(6).with(LocalTime.of(23,59,59));//同周 周六 23:59:59Z
        java.time.format.DateTimeFormatter iso = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        Map<String,String> m = new java.util.HashMap<>();
        m.put("start", iso.format(start));
        m.put("end",   iso.format(end));
        return m;
    }

    /**
     * 获取昨天的时间字符串（可自定义格式）
     * @param pattern 日期格式，如 "yyyy-MM-dd" 或 "yyyy-MM-dd HH:mm:ss"
     * @return 格式化后的字符串
     */
    public static String getDateWithOffset(int daysAgo, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime dateTime = LocalDateTime.now().minusDays(daysAgo);
        return dateTime.format(formatter);
    }

    /**
     * 根据给定的结束日期，往前推 daysAgo 天，返回起始日期
     * @param endDateStr 结束日期（yyyy-MM-dd）
     * @param daysAgo    往前推的天数，例如 29
     * @return yyyy-MM-dd 格式的开始日期
     */
    public static String getStartDateFromEnd(String endDateStr, int daysAgo) {
        if (endDateStr == null || endDateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("endDateStr 不能为空");
        }
        LocalDate endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
        LocalDate startDate = endDate.minusDays(daysAgo);
        return startDate.format(DATE_FORMATTER);
    }

    /**
     * 判断指定日期字符串是否是工作日（周一到周五）
     * @param dateStr 日期字符串，支持 "yyyy-MM-dd" 或 "yyyy-MM-dd HH:mm:ss"
     * @return true = 周一到周五, false = 周六或周日
     */
    public static boolean isWeekday(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("dateStr 不能为空");
        }
        LocalDate date;
        // 判断字符串长度，自动适配不同格式
        if (dateStr.length() == 10) { // yyyy-MM-dd
            date = LocalDate.parse(dateStr, DATE_FORMATTER);
        } else if (dateStr.length() == 19) { // yyyy-MM-dd HH:mm:ss
            LocalDateTime dateTime = LocalDateTime.parse(dateStr, DATETIME_FORMATTER);
            date = dateTime.toLocalDate();
        } else {
            throw new IllegalArgumentException("日期格式错误，应为 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss");
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    /**
     * 根据传入的日期时间字符串，生成昨天为截止日的 30 天范围
     * 并返回是否工作日、时分等信息
     *
     * @param dateTimeStr yyyy-MM-dd HH:mm:ss
     * @return Map 包含 isWeekday, startDate, endDate, hhmm
     */
    public static Map<String, Object> buildLast30DaysRange(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("dateTimeStr 不能为空");
        }

        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        LocalDate inputDate = dateTime.toLocalDate();

        // endDate = 传入日期的前一天
        LocalDate endDate = inputDate.minusDays(1);
        // startDate = endDate 往前29天
        LocalDate startDate = endDate.minusDays(29);

        Map<String, Object> result = new HashMap<>();
        result.put("isWeekday", isWeekday(dateTimeStr));  // 传入日期是否工作日
        result.put("startDate", startDate.format(DATE_FORMATTER));
        result.put("endDate", endDate.format(DATE_FORMATTER));
        result.put("hhmm", dateTime.toLocalTime().format(TIME_HHMM_FORMATTER));
        return result;
    }

    /**
     * 将 ISO-8601 字符串统一转换为 yyyy-MM-dd（按 UTC 取“日”）
     * 支持：
     *  - 2025-08-31T00:00:00Z
     *  - 2025-08-31T08:00:00+08:00
     *  - 2025-08-31T00:00:00
     *  - 2025-08-31
     * @param s 输入字符串
     * @return yyyy-MM-dd；无法解析则抛出 IllegalArgumentException
     */
    public static String isoToYmdUTC(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        String str = s.trim();

        // 已是 yyyy-MM-dd
        if (str.length() == 10 && str.charAt(4) == '-' && str.charAt(7) == '-') {
            LocalDate.parse(str, DATE_FORMATTER); // 校验
            return str;
        }

        // Z 结尾：...Z
        try {
            if (str.endsWith("Z")) {
                Instant instant = Instant.parse(str);
                LocalDate ld = LocalDateTime.ofInstant(instant, ZoneOffset.UTC).toLocalDate();
                return ld.format(DATE_FORMATTER);
            }
        } catch (Exception ignore) {}

        // 含偏移量：...+08:00 / ...-05:00
        try {
            OffsetDateTime odt = OffsetDateTime.parse(str);
            LocalDate ld = odt.atZoneSameInstant(ZoneOffset.UTC).toLocalDate();
            return ld.format(DATE_FORMATTER);
        } catch (Exception ignore) {}

        // 本地无偏移：yyyy-MM-dd'T'HH:mm:ss
        try {
            LocalDateTime ldt = LocalDateTime.parse(str);
            return ldt.toLocalDate().format(DATE_FORMATTER);
        } catch (Exception ignore) {}

        // 兜底：截前 10 位再校验
        if (str.length() >= 10) {
            String head = str.substring(0, 10);
            LocalDate.parse(head, DATE_FORMATTER);
            return head;
        }
        throw new IllegalArgumentException("无法解析日期：" + s);
    }

}
