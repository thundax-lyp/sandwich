package com.github.thundax.common.helper;

import com.github.thundax.common.utils.Reflections;
import com.github.thundax.common.utils.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.lang.NonNull;

/**
 * @param <T> Row对象
 * @author thundax
 */
public class ImportSimpleExcelHelper<T> {

    /** excel 工作薄对象 */
    private final Workbook workbook;

    private Integer sheetIndex = 0;
    private String sheetName;

    /** cell表达式计算器 */
    private final FormulaEvaluator evaluator;

    /** cell格式化工具 */
    private final DataFormatter formatter;

    private final String[] properties;

    /** 构造函数 */
    public ImportSimpleExcelHelper(InputStream inputStream, String[] properties)
            throws EncryptedDocumentException, IOException {
        this.workbook = WorkbookFactory.create(inputStream);
        this.evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        this.formatter = new DataFormatter();
        this.properties = properties;
    }

    public ImportSimpleExcelHelper(InputStream inputStream, int sheetIndex, String[] properties)
            throws EncryptedDocumentException, IOException {
        this(inputStream, properties);

        this.sheetIndex = sheetIndex;
    }

    public ImportSimpleExcelHelper(InputStream inputStream, String sheetName, String[] properties)
            throws EncryptedDocumentException, IOException {
        this(inputStream, properties);

        this.sheetName = sheetName;
    }

    /** 获取sheet数目 */
    public int getSheetCount() {
        return this.workbook.getNumberOfSheets();
    }

    public int persist(String sheetName, @NonNull PersistListener<T> listener) {
        this.sheetName = sheetName;
        return this.persist(listener);
    }

    public int persist(@NonNull PersistListener<T> listener) {
        Sheet sheet = this.workbook.getSheetAt(findSheetIndexByName(sheetName, sheetIndex));
        if (sheet == null) {
            return 0;
        }

        // 定位sheet中的表格位置
        CellRangeAddress tableRange = this.getTableRegion(sheet);
        if (tableRange == null) {
            return 0;
        }

        int flushCount = 0, rowIndex = 0;
        List<T> objectList = new ArrayList<>();

        int startRows = tableRange.getFirstRow() + listener.getFirstRowNum();
        int stopRows = tableRange.getLastRow();
        int totalRows = stopRows - startRows + 1;

        listener.progress(false, totalRows, 0);

        // 读取excel中的cell文本，填充到html table中
        for (int rowNum = startRows; rowNum <= stopRows; rowNum++) {
            listener.progress(false, totalRows, rowNum - startRows);

            Row row = sheet.getRow(rowNum);
            if (row == null) {
                continue;
            }
            int firstCellNum = row.getFirstCellNum();
            int lastCellNum = row.getLastCellNum();

            T object = listener.createObject();
            for (int cellNum = firstCellNum; cellNum <= lastCellNum; cellNum++) {
                if (cellNum < 0) {
                    continue;
                }
                Cell cell = row.getCell(cellNum);
                if (cell == null) {
                    continue;
                }

                CellValue cellValue = this.evaluator.evaluate(cell);
                if (cellValue == null) {
                    continue;
                }

                int propertyIdx = cellNum - firstCellNum;
                String val;

                if (cellValue.getCellType() == CellType.NUMERIC) {
                    val = cellValue.formatAsString();
                } else {
                    val = this.formatter.formatCellValue(cell);
                }

                if (propertyIdx < properties.length
                        && StringUtils.isNotEmpty(properties[propertyIdx])) {
                    Reflections.invokeSetter(object, properties[propertyIdx], val);
                }
            }

            if (listener.validate(object, rowIndex)) {
                objectList.add(object);
                rowIndex++;
            }

            if (objectList.size() > 100) {
                flushCount += listener.flush(objectList);
                objectList.clear();
            }
        }

        if (objectList.size() > 0) {
            flushCount += listener.flush(objectList);
            objectList.clear();
        }

        listener.progress(true, totalRows, totalRows);

        return flushCount;
    }

    protected int findSheetIndexByName(String sheetName, int defaultValue) {
        if (StringUtils.isNotEmpty(sheetName)) {
            for (int idx = 0; idx < getSheetCount(); idx++) {
                Sheet sheet = this.workbook.getSheetAt(idx);
                if (StringUtils.equalsIgnoreCase(sheet.getSheetName(), sheetName)) {
                    return idx;
                }
            }
        }
        return defaultValue;
    }

    /**
     * 获取表格在sheet中的区域位置 获取每行的最左列和最右列位置，最小的“最左列位置”作为表格的“最左列位置”，最大的“最右列位置”作为表格的“最右列位置”
     *
     * @param sheet excel sheet
     */
    protected CellRangeAddress getTableRegion(Sheet sheet) {
        // 定位sheet中的表格位置
        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();
        int firstCellNum = Integer.MAX_VALUE;
        int lastCellNum = Integer.MIN_VALUE;
        // 获取每行的最左列和最右列位置
        // 最小的“最左列位置”作为表格的“最左列位置”，最大的“最右列位置”作为表格的“最右列位置”
        for (int rowNum = firstRowNum; rowNum <= lastRowNum; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row != null) {
                int rowFirstCellNum = row.getFirstCellNum();
                int rowLastCellNum = row.getLastCellNum();
                firstCellNum = Math.min(firstCellNum, rowFirstCellNum);
                lastCellNum = Math.max(lastCellNum, rowLastCellNum);
            }
        }
        // 没有表格，返回空
        if (firstCellNum == Integer.MAX_VALUE) {
            return null;
        }

        return new CellRangeAddress(firstRowNum, lastRowNum, firstCellNum, lastCellNum);
    }

    public interface PersistListener<T> {
        /**
         * 第一行数据位置
         *
         * @return 第一行数据位置
         */
        default int getFirstRowNum() {
            return 1;
        }

        /**
         * 创建 T 对象
         *
         * @return 对象
         */
        T createObject();

        /**
         * 校验对象
         *
         * @param object 对象
         * @param rowIndex 所在行
         * @return 正确:true
         */
        default boolean validate(T object, int rowIndex) {
            return true;
        }

        /**
         * 写入列表
         *
         * @param list 列表
         * @return 写入记录数
         */
        int flush(List<T> list);

        /**
         * 进度
         *
         * @param result 完成:true
         * @param total 最大数据行
         * @param current 当前处理行
         */
        default void progress(boolean result, int total, int current) {}
    }
}
