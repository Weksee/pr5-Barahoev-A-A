package ru.edu.pr02;

public class TimesheetAnalyzer {

    private static final int DAYS_IN_WEEK        = 5;
    private static final int MIN_HOURS           = 0;
    private static final int MAX_HOURS           = 16;
    private static final int DAILY_NORMAL_HOURS  = 8;
    private static final int WEEKLY_NORMAL_HOURS = 40;

    private TimesheetAnalyzer() {} // утилитный класс

    // ---------- R1-R9: единственная точка валидации ----------
    public static int[] parseHours(String line) {
        if (line == null) {
            throw new IllegalArgumentException("Ошибка: структура - строка не может быть null");
        }
        String[] parts = line.split(";", -1); // -1 сохраняет trailing-пустые

        if (parts.length != DAYS_IN_WEEK) {
            throw new IllegalArgumentException(
                "Ошибка: структура - ожидалось " + DAYS_IN_WEEK +
                " значений, получено " + parts.length);
        }

        int[] result = new int[DAYS_IN_WEEK];
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (!part.matches("^(0|[1-9][0-9]*)$")) {
                throw new IllegalArgumentException(
                    "Ошибка: позиция " + (i + 1) +
                    " - значение \"" + part + "\" не является целым числом");
            }
            int v = Integer.parseInt(part);
            if (v < MIN_HOURS || v > MAX_HOURS) {
                throw new IllegalArgumentException(
                    "Ошибка: позиция " + (i + 1) +
                    " - значение " + v + " вне диапазона [" +
                    MIN_HOURS + ", " + MAX_HOURS + "]");
            }
            result[i] = v;
        }
        return result;
    }

    // ---------- защита от программистских ошибок ----------
    private static void checkArray(int[] hours) {
        if (hours == null) {
            throw new IllegalArgumentException("hours is null");
        }
        if (hours.length != DAYS_IN_WEEK) {
            throw new IllegalArgumentException(
                "hours length must be " + DAYS_IN_WEEK + ", got " + hours.length);
        }
    }

    // ---------- total ----------
    public static int total(int[] hours) {
        checkArray(hours);
        int sum = 0;
        for (int h : hours) {
            sum += h;
        }
        return sum;
    }

    // ---------- average (2 знака, через total) ----------
    public static double average(int[] hours) {
        checkArray(hours);
        double avg = (double) total(hours) / DAYS_IN_WEEK;
        return Math.round(avg * 100.0) / 100.0;
    }

    // ---------- overtimeDays (v > 8) ----------
    public static int overtimeDays(int[] hours) {
        checkArray(hours);
        int count = 0;
        for (int h : hours) {
            if (h > DAILY_NORMAL_HOURS) {
                count++;
            }
        }
        return count;
    }

    // ---------- weeklyOvertime: ВАРИАНТ 3 ----------
    // Возвращает НЕДОРАБОТКУ до нормы: max(0, 40 - sum)
    public static int weeklyOvertime(int[] hours) {
        checkArray(hours);
        return Math.max(0, WEEKLY_NORMAL_HOURS - total(hours));
    }

    // ---------- ЧАСТЬ Б (вариант 3): addRestHours ----------
    // Часы отдыха за неделю: 5*8 - total, но не меньше 0.
    public static int addRestHours(int[] hours) {
        checkArray(hours);
        return Math.max(0, WEEKLY_NORMAL_HOURS - total(hours));
    }
}