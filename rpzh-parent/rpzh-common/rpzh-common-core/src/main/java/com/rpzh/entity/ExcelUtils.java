package com.rpzh.entity;


import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/**
 * Excel工具类
 * @Author liubc
 * @create 2021/11/2 15:15
 */
public class ExcelUtils {

    public static List<Map<Integer, String>> importExcel(MultipartFile file, int startRow, int startCloumn, int endCloumn) throws IOException {
        if (file.getOriginalFilename().contains("xlsx")) {
            return ExcelUtils.xlsxExcelImport(file, startRow, startCloumn, endCloumn);
        } else {
            return ExcelUtils.xlsExcelImport(file, startRow, startCloumn, endCloumn);
        }
    }

    private static List<Map<Integer, String>> xlsxExcelImport(MultipartFile file, int startRow, int startCloumn, int endCloumn) throws IOException {
        List<Map<Integer, String>> rtnLs = new ArrayList<>();
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        int sheets = workbook.getNumberOfSheets();
        for (int i = 0; i < sheets; i++) {
            XSSFSheet sheet = workbook.getSheetAt(i);
            //获取多少行
            int rows = sheet.getPhysicalNumberOfRows();
            Map<Integer, String> rtnMap = null;
            //遍历每一行，注意：第 0 行为标题
            for (int j = startRow; j < rows; j++) {
                XSSFRow row = sheet.getRow(j);
                if (ToolUtil.isEmpty(row)) {
                    continue;
                }
                int y = startCloumn;
                rtnMap = new LinkedHashMap<>();
                while (y <= endCloumn) {
                    rtnMap.put(y, getXSSFCellVal(row.getCell(y)).toString());
                    y += 1;
                }
                rtnLs.add(rtnMap);
            }
        }
        return rtnLs;
    }

    private static List<Map<Integer, String>> xlsExcelImport(MultipartFile file, int startRow, int startCloumn, int endCloumn) throws IOException {
        List<Map<Integer, String>> rtnLs = new ArrayList<>();
        HSSFWorkbook workbook = new HSSFWorkbook(new POIFSFileSystem(file.getInputStream()));
        //有多少个sheet
        int sheets = workbook.getNumberOfSheets();
        for (int i = 0; i < sheets; i++) {
            HSSFSheet sheet = workbook.getSheetAt(i);
            //获取多少行
            int rows = sheet.getPhysicalNumberOfRows();
            Map<Integer, String> rtnMap = null;
            //遍历每一行，注意：第 0 行为标题
            for (int j = startRow; j < rows; j++) {
                HSSFRow row = sheet.getRow(j);
                if (ToolUtil.isEmpty(row)) {
                    continue;
                }
                int y = startCloumn;
                rtnMap = new LinkedHashMap<>();
                while (y <= endCloumn) {
                    rtnMap.put(y, getHSSFCellVal(row.getCell(y)).toString());
                    y += 1;
                }

                rtnLs.add(rtnMap);
            }
        }

        return rtnLs;
    }

    /**
     * 创建excel并填入数据
     */
    public static HSSFWorkbook expExcel(String sheetName, List<String> head, List<String> fileds, JSONArray body) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(sheetName);

        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = null;

        HSSFCellStyle cellStyle = workbook.createCellStyle();
        setBorderStyle(cellStyle, BorderStyle.THIN);
        cellStyle.setFont(setFontStyle(workbook, "黑体", (short) 14));
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        for (int i = 0; i < head.size(); i++) {
            cell = row.createCell(i);
            cell.setCellValue(head.get(i));
            cell.setCellStyle(cellStyle);
        }

        HSSFCellStyle cellStyle2 = workbook.createCellStyle();
        setBorderStyle(cellStyle2, BorderStyle.THIN);
        cellStyle2.setFont(setFontStyle(workbook, "宋体", (short) 12));
        cellStyle2.setAlignment(HorizontalAlignment.CENTER);
        for (int i = 0, isize = body.size(); i < isize; i++) {
            row = sheet.createRow(i + 1);
            JSONObject exportData = body.getJSONObject(i);

            for (int j = 0, jsize = fileds.size(); j < jsize; j++) {
                cell = row.createCell(j);
                String cellVal = "";
                if (fileds.get(j).indexOf(".") > 0) {
                    String[] kes = fileds.get(j).split("\\.");
                    JSONObject childData = new JSONObject();
                    for (int k = 0; k < kes.length; k++) {
                        if (null == childData) {
                            cellVal = "";
                            continue;
                        }
                        if ((k + 1) == kes.length) {
                            if (childData.get(kes[k]) instanceof Date) {
                                cellVal = DateUtil.format(childData.getDate(kes[k]), "yyyy-MM-dd HH:mm:ss");
                            } else {
                                cellVal = childData.getString(kes[k]);
                            }
                        } else {
                            childData = exportData.getJSONObject(kes[k]);
                        }
                    }
                } else {
                    if (exportData.get(fileds.get(j)) instanceof Date) {
                        cellVal = DateUtil.format((Date) exportData.get(fileds.get(j)), "yyyy-MM-dd HH:mm:ss");
                    } else {
                        cellVal = ToolUtil.toStr(exportData.get(fileds.get(j)));
                    }
                }
                cell.setCellValue(cellVal);
                cell.setCellStyle(cellStyle2);
            }
        }
        for (int i = 0, isize = head.size(); i < isize; i++) {
            sheet.autoSizeColumn(i);
        }
        return workbook;
    }

    /**
     * 文件输出
     */
    public static void outFile(HSSFWorkbook workbook, HttpServletResponse response, String filename) {
        OutputStream os = null;
        try {
            response.reset();
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Type", "application/force-download");
            response.setHeader("Content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode(filename, "utf-8") + ".xls");
            os = response.getOutputStream();
            workbook.write(os);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            os.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件输出
     */
    public static void outFile(InputStream inputStream, HttpServletResponse response, String filename, String suffix) {
        OutputStream os = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            response.reset();
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Type", "application/force-download");
            response.setHeader("Content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode(filename, "utf-8") + "." + suffix);
            os = response.getOutputStream();


            bis = new BufferedInputStream(inputStream);
            bos = new BufferedOutputStream(os);


            byte[] buff = new byte[1024];
            int bytesRead;
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
            bos.flush();

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            os.close();
            bos.close();
            bis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置字体样式
     */
    private static HSSFFont setFontStyle(HSSFWorkbook workbook, String name, short height) {
        HSSFFont font = workbook.createFont();
        font.setFontHeightInPoints(height);
        font.setFontName(name);
        return font;
    }

    /**
     * 设置单元格样式
     */
    private static void setBorderStyle(HSSFCellStyle cellStyle, BorderStyle border) {
        cellStyle.setBorderBottom(border); // 下边框
        cellStyle.setBorderLeft(border);// 左边框
        cellStyle.setBorderTop(border);// 上边框
        cellStyle.setBorderRight(border);// 右边框
    }

    private static Object getXSSFCellVal(XSSFCell cell) {
        Object rtnVal = null;
        if (ToolUtil.isEmpty(cell)) {
            return "";
        }
        switch (cell.getCellTypeEnum()) {
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    Date tempValue = cell.getDateCellValue();
                    rtnVal = DateUtil.format(tempValue, "yyyy-MM-dd HH:mm:ss");
                } else {
                    rtnVal = String.valueOf(cell.getNumericCellValue());
                }
                break;
            case STRING:
                rtnVal = cell.getStringCellValue();
                break;

            case FORMULA:
                try {
                    rtnVal = cell.getNumericCellValue();
                } catch (Exception e1) {
                    try {
                        rtnVal = cell.getRichStringCellValue().toString();
                    } catch (Exception e2) {
                        rtnVal = cell.getStringCellValue();
                    }
                }
                break;
            case BOOLEAN:
                rtnVal = cell.getBooleanCellValue();
            default:
                rtnVal = "";
        }
        return rtnVal;
    }

    private static Object getHSSFCellVal(HSSFCell cell) {
        Object rtnVal = null;
        if (ToolUtil.isEmpty(cell)) {
            return "";
        }
        switch (cell.getCellTypeEnum()) {
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    Date tempValue = cell.getDateCellValue();
                    rtnVal = DateUtil.format(tempValue, "yyyy-MM-dd HH:mm:ss");
                } else {
                    rtnVal = String.valueOf(cell.getNumericCellValue());
                }
                break;
            case STRING:
                rtnVal = cell.getStringCellValue();
                break;

            case FORMULA:
                try {
                    rtnVal = cell.getNumericCellValue();
                } catch (Exception e1) {
                    try {
                        rtnVal = cell.getRichStringCellValue().toString();
                    } catch (Exception e2) {
                        rtnVal = cell.getStringCellValue();
                    }
                }
                break;
            case BOOLEAN:
                rtnVal = cell.getBooleanCellValue();
            default:
                rtnVal = "";
        }
        return rtnVal;
    }
}