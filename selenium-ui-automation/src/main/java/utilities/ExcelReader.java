package utilities;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ExcelReader {
    private ExcelReader() {
    }

    public static Object[][] readTestData(String workbookResource, String sheetName, String testCaseName) {
        Objects.requireNonNull(workbookResource, "workbookResource must not be null");
        Objects.requireNonNull(sheetName, "sheetName must not be null");
        Objects.requireNonNull(testCaseName, "testCaseName must not be null");

        InputStream workbookStream = ExcelReader.class.getClassLoader()
                .getResourceAsStream(workbookResource);
        if (workbookStream == null) {
            throw new IllegalStateException(
                    "Excel workbook not found on the classpath: " + workbookResource);
        }

        try (InputStream input = workbookStream; Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalStateException("Excel sheet not found: " + sheetName);
            }

            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            List<Object[]> testData = new ArrayList<>();

            for (int rowIndex = sheet.getFirstRowNum() + 1;
                 rowIndex <= sheet.getLastRowNum();
                 rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || !testCaseName.equals(cellValue(row.getCell(0), formatter, evaluator))) {
                    continue;
                }

                int lastDataColumn = findLastDataColumn(row, formatter, evaluator);
                if (lastDataColumn < 1) {
                    throw new IllegalStateException(
                            "No test data found for '" + testCaseName + "' in sheet '" + sheetName + "'");
                }

                Object[] arguments = new Object[lastDataColumn];
                for (int columnIndex = 1; columnIndex <= lastDataColumn; columnIndex++) {
                    arguments[columnIndex - 1] = cellValue(row.getCell(columnIndex), formatter, evaluator);
                }
                testData.add(arguments);
            }

            if (testData.isEmpty()) {
                throw new IllegalStateException(
                        "No rows found for test case '" + testCaseName + "' in sheet '" + sheetName + "'");
            }
            return testData.toArray(new Object[0][]);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read Excel workbook: " + workbookResource, e);
        }
    }

    private static int findLastDataColumn(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        for (int columnIndex = row.getLastCellNum() - 1; columnIndex >= 1; columnIndex--) {
            if (!cellValue(row.getCell(columnIndex), formatter, evaluator).isEmpty()) {
                return columnIndex;
            }
        }
        return -1;
    }

    private static String cellValue(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        return cell == null ? "" : formatter.formatCellValue(cell, evaluator).trim();
    }
}
