package ru.edu.pr02;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SelfCheck {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws IOException {
        System.out.println("=== Unit-проверки ===");
        runUnitTests();
        System.out.println();
        System.out.println("=== Data-driven (sample.csv) ===");
        runDataDriven();
        System.out.println();
        System.out.println("ИТОГО: passed=" + passed + ", failed=" + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------------- helpers ----------------
    private static void pass(String name) {
        passed++;
        System.out.println("PASS " + name);
    }

    private static void fail(String name, String reason) {
        failed++;
        System.out.println("FAIL " + name + " : " + reason);
    }

    private static void expectInt(String name, int actual, int expected) {
        if (actual == expected) pass(name);
        else fail(name, "expected=" + expected + ", actual=" + actual);
    }

    private static void expectDouble(String name, double actual, double expected) {
        if (Math.abs(actual - expected) < 1e-9) pass(name);
        else fail(name, "expected=" + expected + ", actual=" + actual);
    }

    private static void expectArray(String name, int[] actual, int[] expected) {
        if (actual == null || actual.length != expected.length) {
            fail(name, "length mismatch");
            return;
        }
        for (int i = 0; i < expected.length; i++) {
            if (actual[i] != expected[i]) {
                fail(name, "index " + i + " expected=" + expected[i] + ", actual=" + actual[i]);
                return;
            }
        }
        pass(name);
    }

    private static void expectError(String name, String badInput, String expectedFragment) {
        try {
            TimesheetAnalyzer.parseHours(badInput);
            fail(name, "no exception thrown");
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains(expectedFragment)) pass(name);
            else fail(name, "wrong message: " + e.getMessage());
        }
    }

    // ---------------- unit-проверки ----------------
    private static void runUnitTests() {
        // parseHours
        expectArray("parseHours корректная 8;9;10;8;8",
            TimesheetAnalyzer.parseHours("8;9;10;8;8"),
            new int[]{8, 9, 10, 8, 8});
        expectError("parseHours пустая строка", "", "структура");
        expectError("parseHours значение 17", "8;8;17;8;8", "позиция 3");
        expectError("parseHours не-число", "8;abc;8;8;8", "позиция 2");
        expectError("parseHours 4 значения", "8;8;8;8", "структура");
        expectError("parseHours trailing ;", "8;8;8;8;8;", "структура");
        expectError("parseHours ведущий ноль", "08;8;8;8;8", "позиция 1");

        // total
        expectInt("total типовое", TimesheetAnalyzer.total(new int[]{8, 9, 10, 8, 8}), 43);
        expectInt("total нижняя граница", TimesheetAnalyzer.total(new int[]{0, 0, 0, 0, 0}), 0);
        expectInt("total верхняя граница", TimesheetAnalyzer.total(new int[]{16, 16, 16, 16, 16}), 80);

        // average
        expectDouble("average типовое", TimesheetAnalyzer.average(new int[]{8, 9, 10, 8, 8}), 8.60);
        expectDouble("average целое", TimesheetAnalyzer.average(new int[]{8, 8, 8, 8, 8}), 8.00);
        expectDouble("average дробное", TimesheetAnalyzer.average(new int[]{7, 8, 9, 8, 7}), 7.80);

        // overtimeDays (v > 8)
        expectInt("overtimeDays нет переработки", TimesheetAnalyzer.overtimeDays(new int[]{8, 8, 8, 8, 8}), 0);
        expectInt("overtimeDays два дня", TimesheetAnalyzer.overtimeDays(new int[]{9, 8, 7, 10, 8}), 2);
        expectInt("overtimeDays граница 8", TimesheetAnalyzer.overtimeDays(new int[]{8, 9, 8, 8, 8}), 1);

        // weeklyOvertime: вариант 3 — max(0, 40 - sum)
        expectInt("weeklyOvertime сумма 40", TimesheetAnalyzer.weeklyOvertime(new int[]{8, 8, 8, 8, 8}), 0);
        expectInt("weeklyOvertime сумма 50", TimesheetAnalyzer.weeklyOvertime(new int[]{10, 10, 10, 10, 10}), 0);
        expectInt("weeklyOvertime сумма 30", TimesheetAnalyzer.weeklyOvertime(new int[]{6, 6, 6, 6, 6}), 10);

        // --- 4 доп. проверки под вариант 3 ---
        // часть А: weeklyOvertime = max(0, 40 - sum)
        expectInt("weeklyOvertime var3 sum=42", TimesheetAnalyzer.weeklyOvertime(new int[]{9, 8, 7, 10, 8}), 0);
        expectInt("weeklyOvertime var3 sum=35", TimesheetAnalyzer.weeklyOvertime(new int[]{7, 7, 7, 7, 7}), 5);
        // часть Б: addRestHours
        expectInt("addRestHours sum=35", TimesheetAnalyzer.addRestHours(new int[]{7, 7, 7, 7, 7}), 5);
        expectInt("addRestHours sum=50", TimesheetAnalyzer.addRestHours(new int[]{10, 10, 10, 10, 10}), 0);
    }

    // ---------------- data-driven ----------------
    private static void runDataDriven() throws IOException {
        Path path = Path.of("data", "sample.csv");
        if (!Files.exists(path)) {
            System.out.println("sample.csv не найден, пропускаем data-driven");
            return;
        }
        List<String> lines = Files.readAllLines(path);
        int n = 0;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            n++;
            String[] parts = line.split("\\|", -1);
            if (parts.length < 2) {
                fail("data/" + n, "bad format of csv line");
                continue;
            }
            String input = parts[0].equals("EMPTY") ? "" : parts[0];
            String expect = parts[1].trim();
            String name = "data/" + n;

            try {
                int[] hours = TimesheetAnalyzer.parseHours(input);
                if (expect.startsWith("OK:")) {
                    int    sum = TimesheetAnalyzer.total(hours);
                    double avg = TimesheetAnalyzer.average(hours);
                    int    od  = TimesheetAnalyzer.overtimeDays(hours);
                    int    wo  = TimesheetAnalyzer.weeklyOvertime(hours);
                    String exp = expect.replace("OK:", "").trim();
                    boolean ok = exp.contains("sum=" + sum)
                              && exp.contains("od=" + od)
                              && exp.contains("wo=" + wo)
                              && exp.contains("avg=" + String.format(java.util.Locale.ROOT, "%.2f", avg));
                    if (ok) pass(name);
                    else fail(name, "actual: sum=" + sum + ",avg=" + String.format("%.2f", avg)
                                   + ",od=" + od + ",wo=" + wo);
                } else if (expect.startsWith("ERROR:")) {
                    fail(name, "expected error, but no exception");
                }
            } catch (IllegalArgumentException e) {
                if (expect.startsWith("ERROR:")) {
                    String exp = expect.replace("ERROR:", "").trim();
                    if (e.getMessage() != null && e.getMessage().contains(
                            expect.contains("позиция") ? ("позиция " + extractPosition(exp)) : "структура")) {
                        pass(name);
                    } else {
                        fail(name, "wrong message: " + e.getMessage());
                    }
                } else {
                    fail(name, "unexpected exception: " + e.getMessage());
                }
            }
        }
        System.out.println("Data-driven: " + passed + "/" + (passed + failed) + " passed");
    }

    private static String extractPosition(String exp) {
        String[] t = exp.split("\\s+");
        return t.length >= 2 ? t[1] : "";
    }
}