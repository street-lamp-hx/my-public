package com.yiyitech.mf.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName SpCorporateEntityEnum.java
 * @Description
 * @createTime 2025年11月07日 17:01:00
 */
@Getter
public enum SpCorporateEntityEnum {
    WM(1, "网萌科技"),
    MG(2, "萌购科技");

    private final int code;
    private final String desc;

    SpCorporateEntityEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SpCorporateEntityEnum of(int code) {
        return Arrays.stream(values())
                .filter(e -> e.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("无效的公司主体"));
    }
}
