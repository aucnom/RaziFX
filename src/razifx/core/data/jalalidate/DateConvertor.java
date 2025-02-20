/** Gregorian & Jalali (Hijri_Shamsi,Solar) Date Converter Functions
 * Author: JDF.SCR.IR =>> Download Full Version :  http://jdf.scr.ir/jdf
 * License: GNU/LGPL _ Open Source & Free :: Version: 2.80 : [2020=1399]
 * ---------------------------------------------------------------------
 * 355746=361590-5844 & 361590=(30*33*365)+(30*8) & 5844=(16*365)+(16/4)
 * 355666=355746-79-1 & 355668=355746-79+1 &  1595=605+990 &  605=621-16
 * 990=30*33 & 12053=(365*33)+(32/4) & 36524=(365*100)+(100/4)-(100/100)
 * 1461=(365*4)+(4/4) & 146097=(365*400)+(400/4)-(400/100)+(400/400) */

/*
 * The MIT License
 *
 * Copyright 2025 mahdihoseinzade.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package razifx.core.data.jalalidate;

import java.sql.Date;
import java.time.LocalDate;

/**
 * DateConverter.java: change date From http://jdf.scr.ir/jdf website
 *
 * @author mahdihoseinzade
 * @since 1.0
 */
public class DateConvertor {

    /**
     * months of year in jalali date. for (Map.Entry<String, Integer> entry :
     * monthOfYear.entrySet()) { String name = entry.getKey(); int age =
     * entry.getValue(); } }
     */
    private static String[] monthOfYear;

    static {
        monthOfYear = new String[]{
            "فروردین", "اردیبهشت", "خرداد", "تیر",
            "مرداد", "شهریور", "مهر", "آبان",
            "آذر", "دی", "بهمن", "اسفند"
        };
    }

    /**
     * Don't allow to create instance from this class.
     */
    private DateConvertor() {
    }

    /**
     * Convert Jalali date to Gregorian

     * @param year of Jalali date
     * @param month of Jalali date
     * @param day of Jalali date
     * @return localDate
     */
    public static LocalDate jalaliToGregorian(int year, int month, int day) {
        DateConverter dateConverter = new DateConverter();
        LocalDate localdate1 = dateConverter.jalaliToGregorian(year, month, day);
        return localdate1;
    }

    /**
     * months of year
     *
     * @return solar hijrah months of year
     */
    public static String[] getMonthOfYear() {
        return monthOfYear;
    }

    /**
     * Number of month
     *
     * @param monthName
     * @return number of month
     */
    public static int getMonthOfYear(String monthName) {
        int numberOfMonth = 0;
        switch (monthName) {
            case "فروردین":
                numberOfMonth = 1;
                break;
            case "اردیبهشت":
                numberOfMonth = 2;
                break;
            case "خرداد":
                numberOfMonth = 3;
                break;
            case "تیر":
                numberOfMonth = 4;
                break;
            case "مرداد":
                numberOfMonth = 5;
                break;
            case "شهریور":
                numberOfMonth = 6;
                break;
            case "مهر":
                numberOfMonth = 7;
                break;
            case "آبان":
                numberOfMonth = 8;
                break;
            case "آذر":
                numberOfMonth = 9;
                break;
            case "دی":
                numberOfMonth = 10;
                break;
            case "بهمن":
                numberOfMonth = 11;
                break;
            case "اسفند":
                numberOfMonth = 12;
                break;
        }
        return numberOfMonth;
    }

    /**
     * gregorian_to_jalali
     *
     * @param year gregorian-year
     * @param month gregorian-mount
     * @param day gregorian-day
     * @return gregorian date
     */
    private static int[] gregorian_to_jalali(int year, int mounth, int day) {
        int[] out = {(mounth > 2) ? (year + 1) : year, 0, 0};

        {
            int[] g_d_m = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
            out[2] = 355666 + (365 * year) + ((int) ((out[0] + 3) / 4)) - ((int) ((out[0] + 99) / 100)) + ((int) ((out[0] + 399) / 400)) + day + g_d_m[mounth - 1];
        }

        out[0] = -1595 + (33 * ((int) (out[2] / 12053)));
        out[2] %= 12053;
        out[0] += 4 * ((int) (out[2] / 1461));
        out[2] %= 1461;
        if (out[2] > 365) {
            out[0] += (int) ((out[2] - 1) / 365);
            out[2] = (out[2] - 1) % 365;
        }
        if (out[2] < 186) {
            out[1] = 1 + (int) (out[2] / 31);
            out[2] = 1 + (out[2] % 31);
        } else {
            out[1] = 7 + (int) ((out[2] - 186) / 30);
            out[2] = 1 + ((out[2] - 186) % 30);
        }
        return out;
    }

    /**
     * jalali_to_gregorian: http://jdf.scr.ir/jdf
     *
     * @param year jalali-year
     * @param mounth jalali-mounth
     * @param day jalali-day
     * @return jalali date
     */
    private static int[] jalali_to_gregorian(int year, int mounth, int day) {
        year += 1595;
        int[] out = {
            0,
            0,
            -355668 + (365 * year) + (((int) (year / 33)) * 8) + ((int) (((year % 33) + 3) / 4)) + day + ((mounth < 7) ? (mounth - 1) * 31 : ((mounth - 7) * 30) + 186)
        };
        out[0] = 400 * ((int) (out[2] / 146097));
        out[2] %= 146097;
        if (out[2] > 36524) {
            out[0] += 100 * ((int) (--out[2] / 36524));
            out[2] %= 36524;
            if (out[2] >= 365) {
                out[2]++;
            }
        }
        out[0] += 4 * ((int) (out[2] / 1461));
        out[2] %= 1461;
        if (out[2] > 365) {
            out[0] += (int) ((out[2] - 1) / 365);
            out[2] = (out[2] - 1) % 365;
        }
        int[] sal_a = {0, 31, ((out[0] % 4 == 0 && out[0] % 100 != 0) || (out[0] % 400 == 0)) ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        for (out[2]++; out[1] < 13 && out[2] > sal_a[out[1]]; out[1]++) {
            out[2] -= sal_a[out[1]];
        }
        return out;
    }

    /**
     * toJalali Note: can't store jalali calender in the Date.
     *
     * @param date
     * @return date
     */
    public static String toJalali(Date date) {
        int[] x;
        StringBuilder s = new StringBuilder("");
        LocalDate d = date.toLocalDate();
        x = gregorian_to_jalali(d.getYear(), d.getMonthValue(), d.getDayOfMonth());
        for (int i : x) {
            /**
             * if i is the day then don't append '-' in the variable and break
             * for-each
             */
            if (i == x[2]) {
                s.append(i);
                break;
            }
            s.append(i).append("-");
        }
        return s.toString();
    }

    /**
     * toGregorian
     *
     * @param date
     * @return date
     */
    public static Date toGregorian(Date date) {
        int[] x;
        StringBuilder s = new StringBuilder("");
        LocalDate d = date.toLocalDate();
        x = gregorian_to_jalali(d.getYear(), d.getMonthValue(), d.getDayOfMonth());
        for (int i : x) {
            /**
             * if i is the day then don't append '-' in the variable and break
             * for-each
             */
            if (i == x[2]) {
                s.append(i);
                break;
            }
            s.append(i).append("-");
        }
        return Date.valueOf(s.toString());
    }

}
