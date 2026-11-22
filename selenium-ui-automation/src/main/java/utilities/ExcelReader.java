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

                // Collect only the non-blank cells. Blank cells (e.g. a column used by
                // another test but not this one) are skipped, so each test receives
                // exactly the arguments it needs and gaps do not shift the values.
                List<Object> arguments = new ArrayList<>();
                for (int columnIndex = 1; columnIndex < row.getLastCellNum(); columnIndex++) {
                    String value = cellValue(row.getCell(columnIndex), formatter, evaluator);
                    if (!value.isEmpty()) {
                        arguments.add(value);
                    }
                }

                if (arguments.isEmpty()) {
                    throw new IllegalStateException(
                            "No test data found for '" + testCaseName + "' in sheet '" + sheetName + "'");
                }
                testData.add(arguments.toArray());
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

    private static String cellValue(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        return cell == null ? "" : formatter.formatCellValue(cell, evaluator).trim();
    }
}
