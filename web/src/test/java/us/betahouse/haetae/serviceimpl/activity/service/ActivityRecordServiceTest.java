package us.betahouse.haetae.serviceimpl.activity.service;

import com.alibaba.fastjson.JSON;
import com.csvreader.CsvWriter;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.betahouse.haetae.activity.dal.model.ActivityDO;
import us.betahouse.haetae.activity.dal.model.ActivityRecordDO;
import us.betahouse.haetae.activity.dal.model.PastActivityDO;
import us.betahouse.haetae.activity.dal.repo.ActivityDORepo;
import us.betahouse.haetae.activity.dal.repo.ActivityRecordDORepo;
import us.betahouse.haetae.activity.dal.repo.PastActivityDORepo;
import us.betahouse.haetae.activity.dal.service.ActivityRepoService;
import us.betahouse.haetae.activity.enums.ActivityRecordStateEnum;
import us.betahouse.haetae.activity.enums.ActivityTypeEnum;
import us.betahouse.haetae.activity.idfactory.BizIdFactory;
import us.betahouse.haetae.activity.manager.ActivityRecordManager;
import us.betahouse.haetae.activity.model.basic.ActivityRecordBO;
import us.betahouse.haetae.activity.model.basic.PastActivityBO;
import us.betahouse.haetae.activity.model.basic.YouthLearningBO;
import us.betahouse.haetae.certificate.dal.model.QualificationsDO;
import us.betahouse.haetae.certificate.dal.repo.QualificationsDORepo;
import us.betahouse.haetae.serviceimpl.activity.constant.GradesConstant;
import us.betahouse.haetae.serviceimpl.activity.manager.StampManager;
import us.betahouse.haetae.serviceimpl.activity.model.ActivityRecordStatistics;
import us.betahouse.haetae.serviceimpl.activity.request.ActivityStampRequest;
import us.betahouse.haetae.serviceimpl.activity.request.YouthLearningRequest;
import us.betahouse.haetae.serviceimpl.activity.service.impl.YouthLearningServiceImpl;
import us.betahouse.haetae.serviceimpl.common.utils.TermUtil;
import us.betahouse.haetae.user.dal.service.UserInfoRepoService;
import us.betahouse.haetae.user.model.basic.UserInfoBO;
import us.betahouse.util.utils.CsvUtil;
import us.betahouse.util.utils.DateUtil;
import us.betahouse.util.utils.HttpDownloadUtil;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivityRecordServiceTest {

    @Autowired
    private ActivityRecordService activityRecordService;
    @Autowired
    private UserInfoRepoService userInfoRepoService;
    @Autowired
    private ActivityRecordDORepo activityRecordDORepo;
    @Autowired
    private ActivityDORepo activityDORepo;
    @Autowired
    private BizIdFactory activityBizFactory;
    @Autowired
    private StampManager stampManager;
    @Autowired
    private ActivityRecordManager activityRecordManager;
    @Autowired
    private ActivityRepoService activityRepoService;
    @Autowired
    private PastActivityDORepo pastActivityDORepo;
    @Autowired
    private QualificationsDORepo qualificationsDORepo;
    
    @Test
    public void delete() {
        Map<String, String[]> realNameAndStuIdMap = getRealNameAndStuIdMapFromExcel("C:\\Users\\86181\\Desktop\\????????????????????????.xlsx");
        for (String key : realNameAndStuIdMap.keySet()) {
            System.out.println(realNameAndStuIdMap.get(key)[1]);
            System.out.println(userInfoRepoService.queryUserInfoByStuId(realNameAndStuIdMap.get(key)[1]).getUserId());
            // activityRecordDORepo.deleteAllByActivityIdAndUserId("202106171240408008385310012021", );
        }
    }
    
    @Test
    public void tmpExport() {
        CsvWriter csvWriter = new CsvWriter("C:\\Users\\86181\\Desktop\\????????????????????????.csv", ',', StandardCharsets.UTF_8);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<ActivityRecordDO> activityRecordDos = activityRecordDORepo.findAllByActivityId("202106171240408008385310012021");
        try {
            csvWriter.writeRecord(new String[]{"????????????", "??????????????????", "?????????", "??????"});
            for (ActivityRecordDO activityRecordDO : activityRecordDos) {
                String[] res = new String[4];
                UserInfoBO userInfoBO = userInfoRepoService.queryUserInfoByUserId(activityRecordDO.getUserId());
                res[0] = userInfoBO.getRealName();
                res[1] = userInfoBO.getStuId();
                res[2] = userInfoRepoService.queryUserInfoByUserId(activityRecordDO.getScannerUserId()).getRealName();
                res[3] = simpleDateFormat.format(activityRecordDO.getGmtCreate());
                csvWriter.writeRecord(res);
            }
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void superExport() {
        CsvWriter csvWriter = new CsvWriter("C:\\Users\\86181\\Desktop\\export.csv", ',', StandardCharsets.UTF_8);
        Map<String, String[]> realNameAndStuIdMap = getRealNameAndStuIdMapFromExcel("C:\\Users\\86181\\Desktop\\6.10.xlsx");
        try {
            csvWriter.writeRecord(new String[]{"??????", "??????", "????????????", "??????????????????", "??????????????????", "??????????????????"});
            for (String key : realNameAndStuIdMap.keySet()) {
                String[] res = new String[6];
                String stuId = realNameAndStuIdMap.get(key)[0];
                UserInfoBO userInfoBO = userInfoRepoService.queryUserInfoByStuId(stuId);
                PastActivityDO pastActivityDO = pastActivityDORepo.findByUserId(userInfoBO.getUserId());
                res[0] = stuId;
                res[1] = userInfoBO.getRealName();
                res[2] = (activityRecordDORepo.countAllByUserIdAndType(userInfoBO.getUserId(), "lectureActivity") + pastActivityDO.getPastLectureActivity()) + "";
                res[3] = (activityRecordDORepo.countAllByUserIdAndType(userInfoBO.getUserId(), "schoolActivity") + pastActivityDO.getPastSchoolActivity()) + "";
                res[4] = (activityRecordDORepo.countAllByUserIdAndType(userInfoBO.getUserId(), "practiceActivity") + pastActivityDO.getPastPracticeActivity()) + "";
                res[5] = qualificationsDORepo.countAllByUserId(userInfoBO.getUserId()) + "";
                csvWriter.writeRecord(res);
            }
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void importStamp() {
        String url = "C:\\Users\\86181\\Desktop\\7.1\\????????????????????????.csv";
        List<String> ls = activityRecordService.importStamp(url);
        for (String str : ls) {
            System.out.println(str);
        }
        System.out.println();
        System.out.println(ls.size());
    }
    
    /**
     * ???????????????????????? excel ?????????????????????????????????????????? csv ??????
     */
    @Test
    public void superChecker() {
        String folderPath = "/Users/lyl/Desktop/" + getTodayString();
        String resultPath = folderPath + "/result.txt";
        if (getFileSize(new File(folderPath)) == 0) {
            System.out.println("???????????????");
            return;
        }
        Map<String, String[]> realNameAndStuIdMap = getRealNameAndStuIdMapFromExcels(folderPath);
        List<String> result = new ArrayList<>();
        int index = 1;
        // ?????? map ?????????????????????????????????
        for (String key : realNameAndStuIdMap.keySet()) {
            String[] realNameAndStuId = realNameAndStuIdMap.get(key);
            UserInfoBO userInfoBO = userInfoRepoService.queryUserInfoByStuId(realNameAndStuId[1]);
            if (userInfoBO == null) {
                result.add(index + ". " + key + "??????" + realNameAndStuId[0] + "?????????????????????");
                index++;
            } else if (!userInfoBO.getRealName().equals(realNameAndStuId[0])) {
                result.add(index + ". " + key + "???????????? " + realNameAndStuId[1] + " ??????????????????" + realNameAndStuId[0] + "????????????????????????????????????" + userInfoBO.getRealName() + "???");
                index++;
            }
        }
        // ??????????????????????????? sheet ??? csv ??????????????? excel ??????
        if (result.size() == 0) {
            write2Csv(folderPath);
            deleteOriginExcels(folderPath);
            System.out.println("??????");
        } else {
            result.add("????????????" + result.size() + "??????????????????");
            writeResult(resultPath, result);
            System.out.println("??????");
        }
    }

    @Test
    public void updateScannerName(){
        List<ActivityRecordBO> activityRecordBOList=activityRecordManager.findAll();
        Map<String,String> map=new HashMap<>();
        for (ActivityRecordBO record : activityRecordBOList) {
            if (StringUtils.isBlank(record.getScannerName())) {
                String scannerName;
                if(map.containsKey(record.getScannerUserId())){
                    scannerName=map.get(record.getScannerUserId());
                }else {
                    scannerName = userInfoRepoService.queryUserInfoByUserId(record.getScannerUserId()).getRealName();
                    map.put(record.getScannerUserId(), scannerName);
                }
                activityRecordManager.updateScannerName(record.getActivityRecordId(), scannerName);
                record.setScannerName(scannerName);
            }
        }
    }
    // ????????????
    @Test
    public void importVolunteerWork(){
        String url = "C:\\Users\\j10k\\Desktop\\2019????????????????????????????????????(2).csv";
        String[][] csv = CsvUtil.getWithHeader(url);
        for (int i = 1; i < csv.length; i++) {
            ActivityStampRequest request=new ActivityStampRequest();
            request.setActivityId("201812032305164919790210012018");
            request.setScannerUserId("201812010040554783180001201835");
            request.setVolunteerWorkName(csv[i][2]);
            request.setTime(Double.valueOf(csv[i][3]));
            request.setStatus("ENABLE");
            request.setTerm(TermUtil.getNowTerm());
            List<String> stuIdList=new ArrayList<>();
            stuIdList.add(userInfoRepoService.queryUserInfoByStuId(csv[i][1]).getUserId());
            request.setStuIds(stuIdList);
            stampManager.batchStamp(request, stuIdList);
        }
    }
    @Test
    public void importShenQi(){
        String url = "C:\\Users\\j10k\\Desktop\\2019????????????????????????.csv";
        String[][] csv = CsvUtil.getWithHeader(url);
        for (int i = 1; i < csv.length; i++) {
            ActivityStampRequest request=new ActivityStampRequest();
            request.setActivityId("201904151547343710112210012019");
            request.setScannerUserId("201812010040554783180001201835");
            request.setStatus("ENABLE");
            request.setTerm(TermUtil.getNowTerm());
            List<String> stuIdList=new ArrayList<>();
            stuIdList.add(userInfoRepoService.queryUserInfoByStuId(csv[i][1]).getUserId());
            request.setStuIds(stuIdList);
            stampManager.batchStamp(request, stuIdList);
        }
    }
    // ?????????????????????????????????
    @Test
    public void importCXJC(){
        String url = "/Users/lyl/Desktop/7.23/???????????????????????????????????? - Sheet1.csv";
        String[][] csv = CsvUtil.getWithHeader(url);
        for (int i = 1; i < csv.length; i++) {
            ActivityStampRequest request=new ActivityStampRequest();
            // ???????????? ID
//            request.setActivityId("201904151546362400821310012019");
            // ???????????? ID
            request.setActivityId("201904151545269174388510012019");
            request.setScannerUserId("201812010040554783180001201835");
            request.setStatus("ENABLE");
            request.setTerm(TermUtil.getNowTerm());
            List<String> stuIdList=new ArrayList<>();
            stuIdList.add(userInfoRepoService.queryUserInfoByStuId(csv[i][1]).getUserId());
            request.setStuIds(stuIdList);
            stampManager.batchStamp(request, stuIdList);
        }
    }
    // ?????????
    @Test
    public void importLastPartyRecord(){
        String url =  "C:\\Users\\86181\\Desktop\\????????????2???\\???????????????????????????????????????????????????????????? - Sheet1.csv";
        String[][] csv = CsvUtil.getWithHeader(url);
        for (int i = 1; i < csv.length; i++) {
            ActivityRecordDO activityRecordDO = new ActivityRecordDO();
            activityRecordDO.setActivityRecordId(activityBizFactory.getActivityRecordId());
            activityRecordDO.setActivityId(activityDORepo.findByActivityName(csv[i][2]).getActivityId());
            activityRecordDO.setUserId(userInfoRepoService.queryUserInfoByStuId(csv[i][1]).getUserId());
            activityRecordDO.setScannerUserId("201812010040554783180001201835");
            activityRecordDO.setType(ActivityTypeEnum.PARTY_ACTIVITY.getCode());
            activityRecordDO.setTime(0);
            activityRecordDO.setStatus("ENABLE");
            activityRecordDO.setTerm(csv[i][3]);
            activityRecordDORepo.save(activityRecordDO);
            System.out.println(i+" "+activityRecordDO);
        }
    }
    @Test
    public void importOneHour(){
        String url =  "C:\\Users\\86181\\Desktop\\5.10\\???????????????\\??????????????????????????????????????????????????????????????? - ???????????????????????????????????????????????????????????????.csv";
        String[][] csv = CsvUtil.getWithHeader(url);
        for (int i = 1; i < csv.length; i++) {
            ActivityRecordDO activityRecordDO = new ActivityRecordDO();
            activityRecordDO.setActivityRecordId(activityBizFactory.getActivityRecordId());
            activityRecordDO.setActivityId("201904151539461439186510012019");
            activityRecordDO.setUserId(userInfoRepoService.queryUserInfoByStuId(csv[i][1]).getUserId());
            activityRecordDO.setScannerUserId("201812010040554783180001201835");
            activityRecordDO.setTime((int)(Double.valueOf(csv[i][2])*10));
            activityRecordDO.setType("partyTimeActivity");
            activityRecordDO.setStatus("ENABLE");
            activityRecordDO.setTerm(TermUtil.getNowTerm());
            activityRecordDORepo.save(activityRecordDO);
            System.out.println(i+" "+activityRecordDO);
        }
    }

    // ????????????
    @Test
    public void importVolunteerActivity(){
        String url = "C:\\Users\\86181\\Desktop\\7.8\\??????????????????2020-2021????????????????????????????????? - Sheet2.csv";
        String[][] csv = CsvUtil.getWithHeader(url);
        for (int i = 1; i < csv.length; i++) {
            ActivityRecordDO activityRecordDO = new ActivityRecordDO();
            activityRecordDO.setActivityRecordId(activityBizFactory.getActivityRecordId());
            System.out.println(csv[i][3]);
            activityRecordDO.setActivityId(activityDORepo.findByActivityName(csv[i][3]).getActivityId());
            activityRecordDO.setUserId(userInfoRepoService.queryUserInfoByStuId(csv[i][1]).getUserId());
            activityRecordDO.setScannerUserId("201812010040554783180001201835");
            activityRecordDO.setTime((int)(Double.valueOf(csv[i][2])*10));
            activityRecordDO.setType("volunteerActivity");
            activityRecordDO.setStatus("ENABLE");
            activityRecordDO.setTerm(TermUtil.getNowTerm());
            activityRecordDORepo.save(activityRecordDO);
            System.out.println(i+" "+activityRecordDO);
        }
    }

    // ????????????
    @Test
    public void importPracticeActivity2() {
        String url = "/Users/kagantuya/Desktop/??????????????????????????????.csv";
        String[][] csv = CsvUtil.getWithHeader(url);

        for (int i = 1; i < csv.length; i++) {
            ActivityRecordDO activityRecordDO = new ActivityRecordDO();
            activityRecordDO.setActivityRecordId(activityBizFactory.getActivityRecordId());
            activityRecordDO.setActivityId(activityDORepo.findByActivityName(csv[i][2]).getActivityId());
            activityRecordDO.setUserId(userInfoRepoService.queryUserInfoByStuId(csv[i][1]).getUserId());
            activityRecordDO.setScannerUserId("201812010040554783180001201835");
            activityRecordDO.setTime(0);
            activityRecordDO.setType("practiceActivity");
            activityRecordDO.setStatus("ENABLE");
            activityRecordDO.setTerm(TermUtil.getNowTerm());
            switch (csv[i][5]) {
                case "??????":
                    activityRecordDO.setGrades(GradesConstant.EXCELLENT);
                    break;
                case "?????????":
                    activityRecordDO.setGrades(GradesConstant.FAIL);
                    break;
                case "??????":
                    activityRecordDO.setGrades(GradesConstant.PASS);
                    break;
                default:
                    System.out.println(i);
                    assert false;
            }
            activityRecordDORepo.save(activityRecordDO);
        }
    }
    @Test
    public void check() {
        String url = "C:\\Users\\86181\\Desktop\\a\\1.csv";
        String[][] csv = CsvUtil.getWithHeader(url);
        List<String> notStampStuIds = new ArrayList<>();
        for (int i = 1; i < csv.length; i++) {
            UserInfoBO userInfoBO = userInfoRepoService.queryUserInfoByStuId(csv[i][1]);
            if (userInfoBO == null) {
                System.out.println(i + " " + csv[i][1] + " " + null);
                notStampStuIds.add(csv[i][1]);
            } else if (!userInfoBO.getRealName().equals(csv[i][0])) {
                System.out.println(i + " " + csv[i][1] + " name " + userInfoBO.getRealName() + " " + csv[i][0]);
                notStampStuIds.add(csv[i][1]);
            }
        }
        System.out.println(notStampStuIds.size());
    }
    
    /**
     * ??????????????????
     *
     * @return ?????????6.9
     */
    private String getTodayString() {
        String today = new SimpleDateFormat("M.dd").format(new Date());
        String[] split = today.split("\\.");
        if (split[1].charAt(0) == '0') {
            return split[0] + "." + split[1].substring(1);
        }
        return today;
    }
    
    /**
     * ????????????????????????????????? excel ??????
     *
     * @param folderPath ???????????????
     * @param excelPaths excel ??????????????????
     */
    private void getExcelPaths(String folderPath, List<String> excelPaths) {
        File folder = new File(folderPath);
        if (folder.exists()) {
            File[] lists = folder.listFiles();
            if (null != lists && lists.length != 0) {
                for (File file : lists) {
                    String absolutePath = file.getAbsolutePath();
                    if (file.isDirectory()) {
                        getExcelPaths(absolutePath, excelPaths);
                    } else if (absolutePath.endsWith("xlsx") || absolutePath.endsWith("xls")) {
                        excelPaths.add(absolutePath);
                    }
                }
            }
        }
    }
    
    /**
     * ???????????? excel ?????????????????? sheet ????????????????????? map
     *
     * @param excelPath excel ????????????
     * @return ?????? sheet ????????????????????? map
     */
    private Map<String, String[]> getRealNameAndStuIdMapFromExcel(String excelPath) {
        DataFormatter dataForMatter = new DataFormatter();
        // ??????????????? - sheet ??? - sheet ??????????????????????????? sheet ????????????????????????????????????sheet ???????????????????????????????????????????????????
        Map<String, String[]> realNameAndStuIdMap = new HashMap<>();
        File excel = new File(excelPath);
        Workbook workbook = null;
        try {
            if (excelPath.endsWith("xls")) {
                workbook = new HSSFWorkbook(new FileInputStream(excel));
            } else {
                workbook = new XSSFWorkbook(new FileInputStream(excel));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (workbook == null) {
            return realNameAndStuIdMap;
        }
        // ?????? sheet
        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            int rows = sheet.getPhysicalNumberOfRows();
            // ????????????????????????
            for (int rowIndex = 1; rowIndex < rows; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                // ????????????
                if (row == null || row.getCell(0) == null || row.getCell(0).getStringCellValue().length() == 0) {
                    continue;
                }
                String[] realNameAndStuId = new String[2];
                realNameAndStuId[0] = row.getCell(0).getStringCellValue();
                realNameAndStuId[1] = dataForMatter.formatCellValue(row.getCell(1));
                realNameAndStuIdMap.put(excel.getName() + " - " + sheet.getSheetName() + " - ?????????" + (rowIndex + 1) + "???", realNameAndStuId);
            }
        }
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return realNameAndStuIdMap;
    }
    
    /**
     * ???????????????????????? excel ???????????? sheet ????????????????????? map
     *
     * @param folderPath ???????????????
     * @return ?????????????????? excel ???????????? sheet ????????????????????? map
     */
    private Map<String, String[]> getRealNameAndStuIdMapFromExcels(String folderPath) {
        Map<String, String[]> realNameAndStuIdMap = new HashMap<>();
        List<String> excelPaths = new ArrayList<>();
        getExcelPaths(folderPath, excelPaths);
        for (String excelPath : excelPaths) {
            realNameAndStuIdMap.putAll(getRealNameAndStuIdMapFromExcel(excelPath));
        }
        return realNameAndStuIdMap;
    }
    
    /**
     * ??????????????????
     *
     * @param filePath ????????????
     * @param from ?????????
     * @param to ????????????
     * @return ???????????????
     */
    private String replaceLast(String filePath, String from, String to) {
        return filePath.replaceFirst( "(?s)" + from + "(?!.*?" + from + ")", to);
    }
    
    /**
     * ?????????????????? excel ????????? sheet ??????????????? csv ???
     *
     * @param folderPath ???????????????
     */
    private void write2Csv(String folderPath) {
        DataFormatter dataForMatter = new DataFormatter();
        CsvWriter csvWriter;
        List<String> excelPaths = new ArrayList<>();
        getExcelPaths(folderPath, excelPaths);
        try {
            for (String excelPath : excelPaths) {
                File excel = new File(excelPath);
                Workbook workbook = null;
                try {
                    if (excelPath.endsWith("xls")) {
                        workbook = new HSSFWorkbook(new FileInputStream(excel));
                    } else {
                        workbook = new XSSFWorkbook(new FileInputStream(excel));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (workbook == null) {
                    System.out.println("?????? " + excelPath + " ????????????");
                    return;
                }
                if (excelPath.endsWith("xls")) {
                    excelPath = replaceLast(excelPath, ".xls", "");
                } else {
                    excelPath = replaceLast(excelPath, ".xlsx", "");
                }
                // ?????? sheet
                for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                    Sheet sheet = workbook.getSheetAt(sheetIndex);
                    // ??????????????????????????? csv ??????
                    csvWriter = new CsvWriter(excelPath + " - " + sheet.getSheetName() + ".csv", ',', StandardCharsets.UTF_8);
                    int rows = sheet.getPhysicalNumberOfRows();
                    // ????????? sheet
                    if (rows == 0) {
                        continue;
                    }
                    int cols = sheet.getRow(0).getPhysicalNumberOfCells();
                    // ????????????????????????
                    for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
                        Row row = sheet.getRow(rowIndex);
                        // ????????????
                        if (row == null || row.getCell(0) == null || row.getCell(0).getStringCellValue().length() == 0) {
                            continue;
                        }
                        String[] record = new String[cols];
                        for (int colIndex = 0; colIndex < cols; colIndex++) {
                            record[colIndex] = dataForMatter.formatCellValue(row.getCell(colIndex));
                        }
                        csvWriter.writeRecord(record);
                    }
                    csvWriter.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * ?????????????????????
     *
     * @param path ??????
     * @param list ????????????
     */
    private void writeResult(String path, List<String> list) {
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(path);
            for (String s : list) {
                fileWriter.write(s + "\r\n");
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * ???????????????????????? excel ??????
     *
     * @param folderPath ???????????????
     */
    private void deleteOriginExcels(String folderPath) {
        List<String> excelPaths = new ArrayList<>();
        getExcelPaths(folderPath, excelPaths);
        for (String excelPath : excelPaths) {
            File excel = new File(excelPath);
            excel.delete();
        }
    }
    
    /**
     * ??????????????????????????????
     *
     * @param dir ????????????
     * @return ????????????????????????
     */
    private static long getFileSize(File dir) {
        long size = 0;
        File[] fileList = dir.listFiles();
        for (File file : fileList) {
            if (file.isDirectory()) {
                size = size + getFileSize(file);
            } else {
                size = size + file.length();
            }
        }
        return size;
    }
    
    @Test
    public void fetchUserRecordStatistics() {
        ActivityRecordStatistics activityRecordStatistics = activityRecordService.fetchUserRecordStatistics("201811302142241446120001201817");
        System.out.println(activityRecordStatistics);
    }

    @Test
    public void fetchUserRecordStatistics1() throws IOException {
        CsvWriter csvWriter = new CsvWriter("C:\\Users\\j10k\\Desktop\\??????12.csv", ',', Charset.forName("GBK"));
        String[] headers = {"??????", "??????", "??????????????????", "??????????????????", "??????????????????", "??????????????????", "??????????????????", "??????????????????", "??????????????????","?????????????????????","??????","??????","??????"};
        csvWriter.writeRecord(headers);
        List<UserInfoBO> userInfoBOList = userInfoRepoService.queryAllUser();
        for (UserInfoBO userInfoBO : userInfoBOList) {
            ActivityRecordStatistics activityRecordStatistics = activityRecordService.fetchUserRecordStatistics(userInfoBO.getUserId());
            PastActivityBO pastActivityBO=activityRepoService.getPastByUserId(userInfoBO.getUserId());
            Map<String, Integer> map = activityRecordStatistics.getStatistics();
            String[] content = new String[13];
            content[0] = activityRecordStatistics.getStuId();
            content[1] = activityRecordStatistics.getRealName();
            content[2] = String.valueOf((Long.valueOf(map.get(ActivityTypeEnum.SCHOOL_ACTIVITY.getCode()))+pastActivityBO.getPastSchoolActivity()));
            content[3] = String.valueOf(Long.valueOf( map.get(ActivityTypeEnum.LECTURE_ACTIVITY.getCode()))+pastActivityBO.getPastLectureActivity());
            content[4] =String.valueOf(Long.valueOf( map.get(ActivityTypeEnum.PRACTICE_ACTIVITY.getCode()))+pastActivityBO.getPastPracticeActivity());
            content[5] = map.get(ActivityTypeEnum.VOLUNTEER_ACTIVITY.getCode()).toString();
            content[6] = String.format("%.1f", (double) (map.get("volunteerActivityTime") + pastActivityBO.getPastVolunteerActivityTime()) / 10.0);
            content[7] = map.get(ActivityTypeEnum.VOLUNTEER_WORK.getCode()).toString();
            content[8] = String.format("%.1f", Double.valueOf(map.get("volunteerWorkTime")) / 10.0);
            content[9] = pastActivityBO.getUndistributedStamp().toString();
            content[10]=userInfoBO.getGrade();
            content[11]=userInfoBO.getMajor();
            content[12]=userInfoBO.getClassId();
            System.out.println(activityRecordStatistics);
            csvWriter.writeRecord(content);
        }
        csvWriter.close();
    }

    @Test
    public void fetchUserRecordStatistics2() throws IOException {
        String temp1="=IF(J{0}>=1,G{0}+4,G{0})";
        String temp2="=I{0}+J{0}*4";
        String temp3="=F{0}+I{0}+J{0}*4+K{0}";
        String temp4="=IF(IF(OR(AND((F{0}>7),(H{0}>7)),(K{0}>16),AND((F{0}>7),(H{0}+K{0})>7),AND((H{0}>7),((F{0}+K{0})>7)),AND((F{0}<8),(H{0}<8),(F{0}+H{0}+K{0})>15)),0,1)=0,0,IF(OR(AND((H{0}>7),(F{0}+K{0})<8),AND((F{0}<8),(H{0}<8),(F{0}+K{0})<8,(H{0}+K{0})>7),AND((H{0}+K{0})<8,(F{0}+K{0})<8)),8-F{0}-K{0},0))";
        String temp5="=IF(IF(OR(AND((F{0}>7),(H{0}>7)),(K{0}>16),AND((F{0}>7),(H{0}+K{0})>7),AND((H{0}>7),((F{0}+K{0})>7)),AND((F{0}<8),(H{0}<8),(F{0}+H{0}+K{0})>15)),0,1)=0,0,IF(OR(AND((F{0}>7),(H{0}+K{0})<8),AND((H{0}<8),(F{0}<8),(F{0}+K{0})<8,(F{0}+K{0})>7),AND((F{0}+K{0})<8,(H{0}+K{0})<8)),8-H{0}-K{0},0))";
        String temp6="=IF((M{0}+N{0})<(16-L{0}),(16-L{0}),(M{0}+N{0}))";
        CsvWriter csvWriter = new CsvWriter("C:\\Users\\j10k\\Desktop\\??????"+DateUtil.getYearMonthDay(new Date())+".csv", ',', Charset.forName("GBK"));
        String[] headers ={"??????", "??????","??????","??????","??????","??????(??????)","??????????????????","????????????(??????)","??????????????????","??????????????????","?????????????????????","?????????","???????????????","???????????????","???????????????"};


        csvWriter.writeRecord(headers);
        List<UserInfoBO> userInfoBOList = userInfoRepoService.queryAllUser();
        int i=1;
        for (UserInfoBO userInfoBO : userInfoBOList) {
            i++;
            ActivityRecordStatistics activityRecordStatistics = activityRecordService.fetchUserRecordStatistics(userInfoBO.getUserId());
            PastActivityBO pastActivityBO=activityRepoService.getPastByUserId(userInfoBO.getUserId());
            Map<String, Integer> map = activityRecordStatistics.getStatistics();
            String[] content = new String[15];
            content[0] = activityRecordStatistics.getStuId();
            content[1] = activityRecordStatistics.getRealName();
            content[2] = userInfoBO.getMajor();
            content[3] = userInfoBO.getGrade();
            content[4] = userInfoBO.getClassId();
            content[5] = MessageFormat.format(temp1, String.valueOf(i));
            content[6] = String.valueOf(Long.valueOf( map.get(ActivityTypeEnum.LECTURE_ACTIVITY.getCode()))+pastActivityBO.getPastLectureActivity());
            content[7] = MessageFormat.format(temp2, String.valueOf(i));
            content[8] = String.valueOf((Long.valueOf(map.get(ActivityTypeEnum.SCHOOL_ACTIVITY.getCode()))+pastActivityBO.getPastSchoolActivity()));
            content[9] = String.valueOf(Long.valueOf( map.get(ActivityTypeEnum.PRACTICE_ACTIVITY.getCode()))+pastActivityBO.getPastPracticeActivity());
            content[10] = pastActivityBO.getUndistributedStamp().toString();
            content[11] = MessageFormat.format(temp3, String.valueOf(i));
            content[12] = MessageFormat.format(temp4, String.valueOf(i));
            content[13] = MessageFormat.format(temp5, String.valueOf(i));
            content[14] = MessageFormat.format(temp6, String.valueOf(i));
            System.out.println(activityRecordStatistics);
            csvWriter.writeRecord(content);
        }
        csvWriter.close();
    }
    @Test
    public void fetchUserRecordStatistics3() throws IOException {
        CsvWriter csvWriter = new CsvWriter("C:\\Users\\86181\\Desktop\\??????12.csv", ',', Charset.forName("GBK"));
        String[] headers = {"??????", "??????", "??????????????????", "??????????????????", "??????????????????", "??????????????????", "??????????????????", "??????????????????", "??????????????????","??????","??????","??????"};
        csvWriter.writeRecord(headers);
        List<UserInfoBO> userInfoBOList = userInfoRepoService.queryAllUser();
        for (UserInfoBO userInfoBO : userInfoBOList) {
            ActivityRecordStatistics activityRecordStatistics = activityRecordService.fetchUserRecordStatistics(userInfoBO.getUserId(),"2018B");
            PastActivityBO pastActivityBO=activityRepoService.getPastByUserId(userInfoBO.getUserId());
            Map<String, Integer> map = activityRecordStatistics.getStatistics();
            String[] content = new String[12];
            content[0] = activityRecordStatistics.getStuId();
            content[1] = activityRecordStatistics.getRealName();
            content[2] = String.valueOf((Long.valueOf(map.get(ActivityTypeEnum.SCHOOL_ACTIVITY.getCode()))));
            content[3] = String.valueOf(Long.valueOf( map.get(ActivityTypeEnum.LECTURE_ACTIVITY.getCode())));
            content[4] =String.valueOf(Long.valueOf( map.get(ActivityTypeEnum.PRACTICE_ACTIVITY.getCode())));
            content[5] = map.get(ActivityTypeEnum.VOLUNTEER_ACTIVITY.getCode()).toString();
            content[6] = String.format("%.1f", (double) (map.get("volunteerActivityTime")) / 10.0);
            content[7] = map.get(ActivityTypeEnum.VOLUNTEER_WORK.getCode()).toString();
            content[8] = String.format("%.1f", Double.valueOf(map.get("volunteerWorkTime")) / 10.0);
            content[9]=userInfoBO.getGrade();
            content[10]=userInfoBO.getMajor();
            content[11]=userInfoBO.getClassId();
            System.out.println(activityRecordStatistics);
            csvWriter.writeRecord(content);
        }
        csvWriter.close();
    }

    @Test
    public void t11()throws IOException{
        Map<Object,Object[]> t1=new HashMap<>();//LECTURE_ACTIVITY
        Map<Object,Object[]> t2=new HashMap<>();//SCHOOL_ACTIVITY
        Map<Object,Object[]> t3=new HashMap<>();//PRACTICE_ACTIVITY
        Map<Object,Object[]> t5=new HashMap<>();
            for (Object[] objects : activityRecordDORepo.findGroupByActivityTypeAndUserId()) {
                if(objects[3].equals(ActivityTypeEnum.LECTURE_ACTIVITY.getCode())){
                    t1.put(objects[2],objects );
                }
                if(objects[3].equals(ActivityTypeEnum.SCHOOL_ACTIVITY.getCode())){
                    t2.put(objects[2],objects );
                }
                if(objects[3].equals(ActivityTypeEnum.PRACTICE_ACTIVITY.getCode())){
                    t3.put(objects[2],objects );
                }
                if(objects[3].equals(ActivityTypeEnum.VOLUNTEER_ACTIVITY.getCode())){
                    t5.put(objects[2],objects );
                }
//                System.out.println(JSON.toJSONString(objects));
            }
        CsvWriter csvWriter = new CsvWriter("C:\\Users\\86181\\Desktop\\"+DateUtil.getYearMonthDay(new Date())+".csv", ',', Charset.forName("GBK"));
        String[] headers ={"??????", "??????","??????","??????","??????","??????(??????)","??????????????????","????????????(??????)","??????????????????","??????????????????","?????????????????????","?????????","???????????????","???????????????","???????????????","????????????"};
        csvWriter.writeRecord(headers);
        List<UserInfoBO> userInfoBOList = userInfoRepoService.queryAllUser();
        List<PastActivityDO> pastActivityDOList=pastActivityDORepo.findAll();
        Map<Object,PastActivityDO> t4=new HashMap<>();
        for (PastActivityDO pastActivityDO : pastActivityDOList) {
            t4.put(pastActivityDO.getUserId(), pastActivityDO);
        }
        String temp1="=IF(J{0}>=1,G{0}+4,G{0})";
        String temp2="=I{0}+J{0}*4";
        String temp3="=F{0}+I{0}+J{0}*4+K{0}";
        String temp4="=IF(IF(OR(AND((F{0}>7),(H{0}>7)),(K{0}>16),AND((F{0}>7),(H{0}+K{0})>7),AND((H{0}>7),((F{0}+K{0})>7)),AND((F{0}<8),(H{0}<8),(F{0}+H{0}+K{0})>15)),0,1)=0,0,IF(OR(AND((H{0}>7),(F{0}+K{0})<8),AND((F{0}<8),(H{0}<8),(F{0}+K{0})<8,(H{0}+K{0})>7),AND((H{0}+K{0})<8,(F{0}+K{0})<8)),8-F{0}-K{0},0))";
        String temp5="=IF(IF(OR(AND((F{0}>7),(H{0}>7)),(K{0}>16),AND((F{0}>7),(H{0}+K{0})>7),AND((H{0}>7),((F{0}+K{0})>7)),AND((F{0}<8),(H{0}<8),(F{0}+H{0}+K{0})>15)),0,1)=0,0,IF(OR(AND((F{0}>7),(H{0}+K{0})<8),AND((H{0}<8),(F{0}<8),(F{0}+K{0})<8,(F{0}+K{0})>7),AND((F{0}+K{0})<8,(H{0}+K{0})<8)),8-H{0}-K{0},0))";
        String temp6="=IF((M{0}+N{0})<(16-L{0}),(16-L{0}),(M{0}+N{0}))";




        int i=1;
        for (UserInfoBO userInfoBO : userInfoBOList) {
            i++;
//            ActivityRecordStatistics activityRecordStatistics = activityRecordService.fetchUserRecordStatistics(userInfoBO.getUserId());
//            PastActivityBO pastActivityBO=activityRepoService.getPastByUserId(userInfoBO.getUserId());
//            Map<String, Integer> map = activityRecordStatistics.getStatistics();
            PastActivityDO pastActivityDO=t4.get(userInfoBO.getUserId());
            String[] content = new String[16];
            content[0] = userInfoBO.getStuId();
            content[1] = userInfoBO.getRealName();
            content[2] = userInfoBO.getMajor();
            content[3] = userInfoBO.getGrade();
            content[4] = userInfoBO.getClassId();
            content[5] = MessageFormat.format(temp1, String.valueOf(i));
            content[6] = String.valueOf(getLongValue(t1, userInfoBO.getUserId(), 0)+pastActivityDO.getPastLectureActivity());
            //content[6] = String.valueOf(Long.valueOf( map.get(ActivityTypeEnum.LECTURE_ACTIVITY.getCode()))+pastActivityBO.getPastLectureActivity());
            content[7] = MessageFormat.format(temp2, String.valueOf(i));
            content[8] = String.valueOf((getLongValue(t2, userInfoBO.getUserId(), 0)+pastActivityDO.getPastSchoolActivity()));
            content[9] = String.valueOf(getLongValue(t3, userInfoBO.getUserId(), 0)+pastActivityDO.getPastPracticeActivity());
            //content[8] = String.valueOf((Long.valueOf(map.get(ActivityTypeEnum.SCHOOL_ACTIVITY.getCode()))+pastActivityBO.getPastSchoolActivity()));
            //content[9] = String.valueOf(Long.valueOf( map.get(ActivityTypeEnum.PRACTICE_ACTIVITY.getCode()))+pastActivityBO.getPastPracticeActivity());
            content[10] = pastActivityDO.getUndistributedStamp().toString();
            content[11] = MessageFormat.format(temp3, String.valueOf(i));
            content[12] = MessageFormat.format(temp4, String.valueOf(i));
            content[13] = MessageFormat.format(temp5, String.valueOf(i));
            content[14] = MessageFormat.format(temp6, String.valueOf(i));
            content[15] = String.valueOf(getLongValue(t5, userInfoBO.getUserId(), 1)+pastActivityDO.getPastVolunteerActivityTime());
            //            System.out.println(activityRecordStatistics);
//            System.out.println(JSON.toJSONString(content));
//            System.out.println(userInfoBO.getUserId());
            csvWriter.writeRecord(content);
        }
        csvWriter.close();
    }
    
    @Test
    public void exportStraightUp()throws IOException{
        CsvWriter csvWriter = new CsvWriter("C:\\Users\\86181\\Desktop\\"+DateUtil.getYearMonthDay(new Date())+".csv", ',', Charset.forName("GBK"));
        String[] headers ={"??????", "??????","??????","????????????","????????????","????????????"};
        csvWriter.writeRecord(headers);
        String[] stuIds = {"189090228"};
        for (String stuId : stuIds) {
            UserInfoBO userInfoBO = userInfoRepoService.queryUserInfoByStuId(stuId);
            
            List<ActivityRecordDO> activityRecordDos = activityRecordDORepo.findByUserId(userInfoBO.getUserId());
            StringBuilder l = new StringBuilder();
            StringBuilder p = new StringBuilder();
            StringBuilder s = new StringBuilder();
            for (ActivityRecordDO activityRecordDo : activityRecordDos) {
                String activityName = activityDORepo.findByActivityId(activityRecordDo.getActivityId()).getActivityName();
                switch (activityRecordDo.getType()) {
                    case "lectureActivity":
                        l.append(activityName).append(",");
                        break;
                    case "practiceActivity":
                        p.append(activityName).append(",");
                        break;
                    case "schoolActivity":
                        s.append(activityName).append(",");
                        break;
                    default:
                        break;
                }
            }
    
            String[] content = new String[6];
            content[0] = userInfoBO.getStuId();
            content[1] = userInfoBO.getRealName();
            content[2] = userInfoBO.getSex();
            content[3] = l.toString();
            content[4] = p.toString();
            content[5] = s.toString();
            csvWriter.writeRecord(content);
        }
        csvWriter.close();
    }

    private Long getLongValue(Map<Object, Object[]> map, Object key, int No){
        if(map.get(key)==null)
            return 0L;
        else{
//            System.out.println(map.get(key)[No]);
            BigDecimal ans=(BigDecimal)map.get(key)[No];
            return ans.longValue();
        }
    }

    @Test
    private void YouthLearningRecord(){
//        try {
//            String[][] values = CsvUtil.getWithHeader(file.getInputStream());
//            int stuClass=-1,stuId=-1,finishTime=-1,activityName=-1;
//            for (int i = 0; i < values[0].length; i++) {
//                switch (values[0][i]){
//                    case "??????/??????/???????????????":
//                        stuClass=i;
//                        break;
//                    case "??????/??????/??????":
//                        stuId=i;
//                        break;
//                    case "????????????":
//                        finishTime=i;
//                        break;
//                    case "??????":
//                        activityName=i;
//                        break;
//                }
//            }
//            if(stuId==-1||finishTime==-1||activityName==-1||stuClass==-1){
//                HttpDownloadUtil.downloadByValue("result.txt","?????????????????????????????????,?????????????????????????????????/??????/??????,??????/??????/???????????????,????????????,??????",response);
//                return null;
//            }
//            List<YouthLearningBO> youthLearningBOS=new ArrayList<>();
//            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy/MM/dd HH:mm");
//            for (int i = 1; i < values.length; i++) {
//                String stuid="";
//                for (int j = 0; j < values[i][stuId].length(); j++) {
//                    char c = values[i][stuId].charAt(j);
//                    if(c>='0'&&c<='9') stuid+=String.valueOf(c);
//                }
//                YouthLearningBO youthLearningBO=new YouthLearningBO();
//                youthLearningBO.setScannerUserId(request.getUserId());
//                youthLearningBO.setType(ActivityTypeEnum.YOUTH_LEARNING_ACTIVITY.getCode());
//                youthLearningBO.setTerm(TermUtil.getNowTerm());
//                youthLearningBO.setStatus(ActivityRecordStateEnum.ENABLE.getCode());
//                youthLearningBO.setActivityName(values[i][activityName]);
//                youthLearningBO.setUserId(userService.findByStuid(stuid).getUserId());
//                youthLearningBO.setFinishTime(simpleDateFormat.parse(values[i][finishTime]));
//                youthLearningBO.setClassId(values[i][stuClass]);
//                youthLearningBOS.add(youthLearningBO);
//            }
//            List<YouthLearningBO> fails=null;
//            YouthLearningServiceImpl.Info info=null;
//            if(youthLearningBOS.size()>0){
//                YouthLearningRequest youthLearningRequest=new YouthLearningRequest();
//                youthLearningRequest.setYouthLearningBOList(youthLearningBOS);
//                info = youthLearningService.batchSaveRecord(youthLearningRequest);
//            }else {
//                HttpDownloadUtil.downloadByValue("result.txt","????????????????????????????????????",response);
//                return null;
//            }
//            if(info.getRepeat().size()==0){
//                HttpDownloadUtil.downloadByValue("result.txt","???????????? :) \n"+info.getInfo(),response);
//                return null;
//            }
//            ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
//            CsvWriter csvWriter=new CsvWriter(outputStream,',', StandardCharsets.UTF_8);
//            csvWriter.writeRecord(new String[]{"??????/??????/??????","??????/??????/???????????????", "??????","????????????"});
//            for (int i = 0; i < info.getRepeat().size(); i++) {
//                YouthLearningBO youthLearningBO = info.getRepeat().get(i);
//                String sId=youthLearningBO.getStuId()==null?"":youthLearningBO.getStuId();
//                String name=youthLearningBO.getRealName()==null?"":youthLearningBO.getRealName();
//                String clsid=youthLearningBO.getClassId()==null?"":youthLearningBO.getClassId();
//                csvWriter.writeRecord(new String[]{sId+" "+name,clsid,youthLearningBO.getActivityName(),simpleDateFormat.format(youthLearningBO.getFinishTime())});
//            }
//            ByteArrayInputStream infoStream=new ByteArrayInputStream(info.getInfo().toString().getBytes());
//            ByteArrayInputStream resultStream=new ByteArrayInputStream(outputStream.toByteArray());
//            HttpDownloadUtil.downloadInputStreamZIP("??????.zip",response,"result.txt",resultStream,"info.txt",infoStream);
//            return null;
//        } catch (IOException | ParseException e) {
//            e.printStackTrace();
//        }
//        return null;
    }
}
