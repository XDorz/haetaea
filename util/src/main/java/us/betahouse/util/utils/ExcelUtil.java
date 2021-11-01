package us.betahouse.util.utils;





import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import us.betahouse.util.enums.CommonResultCode;
import us.betahouse.util.exceptions.BetahouseException;
import us.betahouse.util.template.ExcelTemplate;


import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.*;

import static org.apache.poi.ss.usermodel.CellType.*;

/**
 * @author zjb
 * @version : ExcelUtil.java 2019/8/1 14:50 zjb
 */
public class ExcelUtil {


    /**
     * 临时文件路径
     */
    private static final String TEMP_FILE_PATH = "./file/temp/";


    public static <T> HSSFWorkbook createExcel(List<T> list) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("sheet1");
        sheet.setDefaultColumnWidth(20);
        for (int i = 0; i < list.size(); i++) {
            //利用反射机制获取所有成员字段
            Class<?> itemClass = list.get(i).getClass();
            Field[] fields = itemClass.getDeclaredFields();
            HSSFCell cell = null;
            HSSFRow row;

            //表头
            if (i == 0) {
                row = wb.getSheetAt(0).createRow((int) 0);
                for (int j = 0; j < fields.length; j++) {
                    boolean flag = fields[j].isAccessible();
                    fields[j].setAccessible(true);    //设置该成员字段可访问
                    cell = row.createCell((short) j);
                    cell.setCellValue(fields[j].getName());
                    fields[j].setAccessible(flag);
                }
            }

            //数据
            row = wb.getSheetAt(0).createRow((int) i + 1);
            for (int j = 0; j < fields.length; j++) {
                try {
                    boolean flag = fields[j].isAccessible();
                    fields[j].setAccessible(true);
                    cell = row.createCell((short) j);
                    cell.setCellValue(fields[j].get(list.get(i)) == null ? "" : fields[j].get(list.get(i)).toString());
                    fields[j].setAccessible(flag);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return wb;
    }


    /**
     * 将List对象转存为Excel临时文件
     *
     * @param list
     * @param filePath
     * @param <T>
     * @return
     */
    public static <T> String list2ExcelFile(List<T> list, String filePath, String fileName) {

        HSSFWorkbook wb = createExcel(list);
        String nowTime = DateUtil.format(new Date(), "yyyyMMddHHmmssSSS");


        String path = "";
        if (!fileName.endsWith(".xls")) fileName += ".xls";
        if (filePath == null || filePath == "") {
            path = TEMP_FILE_PATH + nowTime + fileName;
        } else {
            if (!filePath.endsWith("/")) filePath += "/";
            path = filePath + nowTime + fileName;
        }


        try {
            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream(path);
            } catch (FileNotFoundException e) {

                File newFile = new File(path);
                if (!newFile.getParentFile().exists()) {
                    try {
                        newFile.getParentFile().mkdirs();
                        fout = new FileOutputStream(path);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

            }
            wb.write(fout);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return path;

    }

    public static List<ExcelTemplate> parseExcel(Workbook workbook) {
        List<ExcelTemplate> total=new ArrayList<>();
        for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
            ExcelTemplate excelTemplate=new ExcelTemplate();
            Sheet sheet = workbook.getSheetAt(sheetNum);
            // 校验sheet是否合法
            if (sheet == null||sheet.getRow(sheet.getFirstRowNum())==null) {
                continue;
            }
            //获取第一个实际行的行号
            int firstRowNum = sheet.getFirstRowNum();
            Row firstRow = sheet.getRow(firstRowNum);
            //实际测试lastRowNum总会多出一行？？？？？
            int lastRowNum=sheet.getLastRowNum();
            int firstColumn=sheet.getRow(firstRowNum+1).getFirstCellNum();
            int lastColumn=sheet.getRow(firstRowNum+1).getLastCellNum();
            int rowNum=lastRowNum-firstRowNum+1;
            String title;
            //有合并单元格，默认为标题,在第一行
            if(sheet.getNumMergedRegions()==1){
                CellRangeAddress mergedRegion = sheet.getMergedRegion(0);
                int fc=mergedRegion.getFirstColumn();
                title=convertCellValueToString(firstRow.getCell(fc));
                excelTemplate.setTitle(title);
                Map<String,List<String>> data=new HashMap<>();
                List<String> catalog=new ArrayList<>();
                //初始化data
                for (int i = firstColumn; i < lastColumn; i++) {
                    data.put(convertCellValueToString(sheet.getRow(firstRowNum+1).getCell(i)),new ArrayList<>());
                    catalog.add(convertCellValueToString(sheet.getRow(firstRowNum+1).getCell(i)));
                }
                for (int i = firstRowNum+2; i < lastRowNum+1; i++) {
                    if(sheet.getRow(i)==null){
                        rowNum--;
                        continue;
                    }
                    for (int j = firstColumn; j < lastColumn; j++) {
                        data.get(convertCellValueToString(sheet.getRow(firstRowNum+1).getCell(j))).add(convertCellValueToString(sheet.getRow(i).getCell(j)));
                    }
                }
                excelTemplate.setDate(data);
                excelTemplate.setColumnNum(lastColumn-firstColumn);
                excelTemplate.setRowNum(rowNum-2);
                excelTemplate.setCatalog(catalog);
                total.add(excelTemplate);
            }else if(sheet.getNumMergedRegions()==0){
                //没有标题的情况
                Map<String,List<String>> data=new HashMap<>();
                List<String> catalog=new ArrayList<>();
                //初始化data
                for (int i = firstColumn; i < lastColumn; i++) {
                    data.put(convertCellValueToString(sheet.getRow(firstRowNum).getCell(i)),new ArrayList<>());
                    catalog.add(convertCellValueToString(sheet.getRow(firstRowNum).getCell(i)));
                }
                for (int i = firstRowNum+1; i < lastRowNum+1; i++) {
                    if(sheet.getRow(i)==null){
                        rowNum--;
                        continue;
                    }
                    for (int j = firstColumn; j < lastColumn; j++) {
                        data.get(convertCellValueToString(sheet.getRow(firstRowNum).getCell(j))).add(convertCellValueToString(sheet.getRow(i).getCell(j)));
                    }
                }
                excelTemplate.setDate(data);
                excelTemplate.setColumnNum(lastColumn-firstColumn);
                excelTemplate.setRowNum(rowNum-1);
                excelTemplate.setCatalog(catalog);
                total.add(excelTemplate);
            }else {
                throw new BetahouseException(CommonResultCode.SYSTEM_ERROR,"表单中有多个合并行，无法解析");
            }
        }
        return total;
    }

        /**
         * 将单元格内容转换为字符串
         * @param cell
         * @return
         */
        private static String convertCellValueToString (Cell cell) {
            if (cell == null) {
                return null;
            }
            String returnValue = null;
            switch (cell.getCellTypeEnum()) {
                case NUMERIC:   //数字
                    Double doubleValue = cell.getNumericCellValue();
                    // 格式化科学计数法，取一位整数
                    DecimalFormat df = new DecimalFormat("0");
                    returnValue = df.format(doubleValue);
                    break;
                case STRING:    //字符串
                    returnValue = cell.getStringCellValue();
                    break;
                case BOOLEAN:   //布尔
                    Boolean booleanValue = cell.getBooleanCellValue();
                    returnValue = booleanValue.toString();
                    break;
                case BLANK:     // 空值
                    break;
                case FORMULA:   // 公式
                    returnValue = cell.getCellFormula();
                    break;
                case ERROR:     // 故障
                    break;
                default:
                    break;
            }
            return returnValue;
        }
    /**
     * 创建excel文档
     *
     * @param list
     * @param keys        列名称集合
     * @param columnNames 列名
     * @return
     */
    public static HSSFWorkbook createWorkBook(List<Map<String, Object>> list, String[] keys, String columnNames[]) {
        // 1. 创建excel工作簿
        HSSFWorkbook wb = new HSSFWorkbook();
        // 2. 创建第一个sheet页，并命名
        HSSFSheet sheet = wb.createSheet(list.get(0).get("sheetName").toString());
        // 3. 设置每列的宽
        for (int i = 0; i < keys.length; i++) {
            sheet.setColumnWidth((short) i, (short) (50 * 60));
        }
        // 4. 创建第一行，设置其单元格格式，并将数据放入
        HSSFRow row = sheet.createRow((short) 0);
        row.setHeight((short) 500);
        // 4.1 设置单元格格式
        HSSFCellStyle cs = wb.createCellStyle();
        HSSFFont f = wb.createFont();
        f.setFontName("宋体");
        f.setFontHeightInPoints((short) 10);
        f.setBold(true);
        cs.setFont(f);
        cs.setAlignment(HorizontalAlignment.CENTER);// 水平居中
        cs.setVerticalAlignment(VerticalAlignment.CENTER);// 垂直居中
        cs.setLocked(true);
        cs.setWrapText(true);//自动换行
        // 4.2 设置列名（取出列名集合进行创建）
        for (int i = 0; i < columnNames.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(columnNames[i]);
            cell.setCellStyle(cs);
        }

        // 5. 设置首行外,每行每列的值(Row和Cell都从0开始)
        for (short i = 1; i < list.size(); i++) {
            HSSFRow row1 = sheet.createRow((short) i);
            String flag = "";
            // 5.1 在Row行创建单元格
            for (short j = 0; j < keys.length; j++) {
                HSSFCell cell = row1.createCell(j);
                cell.setCellValue(list.get(i).get(keys[j]) == null ? " " : list.get(i).get(keys[j]).toString());
            }
            // 5.2 设置该行样式
            HSSFFont f2 = wb.createFont();
            f2.setFontName("宋体");
            f2.setFontHeightInPoints((short) 10);
            // 5.3 设置单元格样式
            HSSFCellStyle cs2 = wb.createCellStyle();
            cs2.setFont(f2);
            cs2.setAlignment(HorizontalAlignment.CENTER);// 左右居中
            cs2.setVerticalAlignment(VerticalAlignment.CENTER);// 上下居中
            cs2.setLocked(true);
            cs2.setWrapText(true);//自动换行
            for (int m = 0; m < keys.length; m++) {
                HSSFCell hssfCell = row1.getCell(m);
                hssfCell.setCellStyle(cs2);
            }

        }
        return wb;
    }

    /**
     * 生成并下载Excel
     *
     * @param list
     * @param keys
     * @param columnNames
     * @param fileName
     * @param response
     * @throws IOException
     */
    public static void downloadWorkBook(List<Map<String, Object>> list,
                                        String keys[],
                                        String columnNames[],
                                        String fileName,
                                        HttpServletResponse response) throws IOException {
        // 1. 声明字节输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            // 2. 生成excel文件并写入输出流
            ExcelUtil.createWorkBook(list, keys, columnNames).write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 3. 将输出流转换成byte[] 数组
        byte[] content = os.toByteArray();
        // 4. 将数组放入输入流中
        InputStream is = new ByteArrayInputStream(content);
        // 5. 设置response参数
        response.reset(); // 重置response的设置
//        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setContentType("application/octet-stream");
//        response.setHeader("Content-Disposition", "attachment;filename=" + new String((fileName + ".xls").getBytes(), "iso-8859-1"));
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName,"UTF-8") + ".xls");
        // 6. 创建Servlet 输出流对象
        ServletOutputStream out = response.getOutputStream();
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            // 6.1装载缓冲输出流
            bis = new BufferedInputStream(is);
            bos = new BufferedOutputStream(out);
            byte[] buff = new byte[2048];
            int bytesRead;
            // 6.2 输出内容
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
        } catch (final IOException e) {
            throw e;
        } finally {
            if (bis != null)
                bis.close();
            if (bos != null)
                bos.close();
        }
    }
    }

