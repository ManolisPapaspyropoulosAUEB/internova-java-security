package controllers.security.CoreData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import controllers.execution_context.DatabaseExecutionContext;
import models.*;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

public class CoreDataPrintsController {


    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public CoreDataPrintsController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }



    //exportManagersAsXLS


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result exportManagersAsXLS(final Http.Request request) throws IOException {  // san parametro pernei to org key
        ObjectNode result = Json.newObject();
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                if (json == null) {
                    result.put("status", "error");
                    result.put("message", "Δεν εχετε αποστειλει εγκυρα δεδομενα.");
                    return ok(result);
                } else {
                    ObjectMapper ow = new ObjectMapper();
                    String jsonResult = "";
                    CompletableFuture<String> createXLSResult = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(
                                        entityManager -> {
                                            ObjectNode resultNode = Json.newObject();
                                            String random_id = json.findPath("random_id").asText();
                                            Random rand = new Random();
                                            String filename =  ConfigFactory.load().getString("uploads_reports")+"managers" + random_id + ".xls";
                                            HSSFWorkbook workbook = new HSSFWorkbook();
                                            HSSFSheet sheet = workbook.createSheet("FirstSheet");
                                            HSSFRow rowhead = sheet.createRow((short) 0);
                                            rowhead.createCell((short) 0).setCellValue("ID");
                                            rowhead.createCell((short) 1).setCellValue("ΕΠΩΝΥΜΟ");
                                            rowhead.createCell((short) 2).setCellValue("ΟΝΟΜΑ");
                                            rowhead.createCell((short) 3).setCellValue("ΘΕΣΗ");
                                            rowhead.createCell((short) 4).setCellValue("ΤΗΛΕΦΩΝΟ");
                                            rowhead.createCell((short) 5).setCellValue("EMAIL");
                                            rowhead.createCell((short) 6).setCellValue("ΣΥΣΤΗΜΑ");
                                            rowhead.createCell((short) 7).setCellValue("ΕΠΩΝΥΜΙΑ");
                                            rowhead.createCell((short) 8).setCellValue("ΗΜΕΡΟΜΗΝΙΑ ΔΗΜΙΟΥΡΓΙΑΣ");
                                            String sql = "select * from managers ";
                                            List<ManagersEntity> managersEntityList = (List<ManagersEntity>)
                                                    entityManager.createNativeQuery(sql, ManagersEntity.class).getResultList();
                                            for (int i = 0; i < managersEntityList.size(); i++) {
                                                HSSFRow row = sheet.createRow((short) i + 1);
                                                row.createCell((short) 0).setCellValue(managersEntityList.get(i).getId());
                                                row.createCell((short) 1).setCellValue(managersEntityList.get(i).getLastName());
                                                row.createCell((short) 2).setCellValue(managersEntityList.get(i).getFirstName());
                                                row.createCell((short) 3).setCellValue(managersEntityList.get(i).getPosition());
                                                row.createCell((short) 4).setCellValue(managersEntityList.get(i).getTelephone());
                                                row.createCell((short) 5).setCellValue(managersEntityList.get(i).getEmail());
                                                if (managersEntityList.get(i).getSystemId() != null) {
                                                    row.createCell((short) 6).setCellValue(managersEntityList.get(i).getSystem());
                                                    switch (managersEntityList.get(i).getSystem()) {
                                                        case "Εργοστάσιο":
                                                            row.createCell((short) 7).setCellValue(entityManager.find(FactoriesEntity.class, managersEntityList.get(i).getSystemId()).getBrandName());
                                                            break;
                                                        case "Αποθήκη":
                                                            row.createCell((short) 7).setCellValue(entityManager.find(WarehousesEntity.class, managersEntityList.get(i).getSystemId()).getBrandName());
                                                            break;
                                                        default:
                                                    }
                                                }else{
                                                    row.createCell((short) 7).setCellValue("-");
                                                    row.createCell((short) 6).setCellValue("-");
                                                }
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                                String creationDateString = myDateFormat.format(managersEntityList.get(i).getCreationDate());
                                                row.createCell((short) 8).setCellValue(creationDateString);
                                            }
                                            for (int col = 0; col < 16; col++) {
                                                sheet.autoSizeColumn(col);
                                            }
                                            FileOutputStream fileOut = null;
                                            try {
                                                fileOut = new FileOutputStream(filename);
                                                workbook.write(fileOut);
                                                fileOut.close();
                                                return filename;
                                            } catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            resultNode.put("message", "success");
                                            return filename;
                                        });
                            },
                            executionContext);
                    String ret_path = createXLSResult.get();
                    File previewFile = new File(ret_path);
                    return ok(previewFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Πρόβλημα κατά την ανάγνωση των στοιχείων");
            return ok(result);
        }
    }



//






    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result exportInternovaAsXls(final Http.Request request) throws IOException {
        ObjectNode result = Json.newObject();
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                if (json == null) {
                    result.put("status", "error");
                    result.put("message", "Δεν εχετε αποστειλει εγκυρα δεδομενα.");
                    return ok(result);
                } else {
                    ObjectMapper ow = new ObjectMapper();
                    String jsonResult = "";
                    CompletableFuture<String> createXLSResult = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(
                                        entityManager -> {
                                            ObjectNode resultNode = Json.newObject();
                                            String random_id = json.findPath("random_id").asText();
                                            Random rand = new Random();
                                            String filename =  ConfigFactory.load().getString("uploads_reports")+"internova_sellers" + random_id + ".xls";
                                            HSSFWorkbook workbook = new HSSFWorkbook();
                                            HSSFSheet sheet = workbook.createSheet("FirstSheet");
                                            HSSFRow rowhead = sheet.createRow((short) 0);
                                            rowhead.createCell((short) 0).setCellValue("BILLING ID");
                                            rowhead.createCell((short) 1).setCellValue("ΛΟΓΑΡΙΑΣΜΟΣ");
                                            rowhead.createCell((short) 2).setCellValue("ΠΕΡΙΓΡΑΦΗ");
                                            rowhead.createCell((short) 3).setCellValue("ΗΜΕΡΟΜΗΝΙΑ ΔΗΜΙΟΥΡΓΙΑΣ");
                                            String sql = "select * from internova_sellers b ";
                                            List<BillingsEntity> billingsEntityList = (List<BillingsEntity>)
                                                    entityManager.createNativeQuery(sql, BillingsEntity.class).getResultList();
                                            for (int i = 0; i < billingsEntityList.size(); i++) {
                                                HSSFRow row = sheet.createRow((short) i + 1);
                                                row.createCell((short) 0).setCellValue(billingsEntityList.get(i).getId());
                                                row.createCell((short) 1).setCellValue(billingsEntityList.get(i).getName());
                                                row.createCell((short) 2).setCellValue(billingsEntityList.get(i).getDescription());
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                                String creationDateString = myDateFormat.format(billingsEntityList.get(i).getCreationDate());
                                                row.createCell((short) 3).setCellValue(creationDateString);
                                            }
                                            for (int col = 0; col < 13; col++) {
                                                sheet.autoSizeColumn(col);
                                            }
                                            FileOutputStream fileOut = null;
                                            try {
                                                fileOut = new FileOutputStream(filename);
                                                workbook.write(fileOut);
                                                fileOut.close();
                                                return filename;
                                            } catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            resultNode.put("message", "success");
                                            return filename;
                                        });
                            },
                            executionContext);
                    String ret_path = createXLSResult.get();
                    File previewFile = new File(ret_path);
                    return ok(previewFile);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Πρόβλημα κατά την ανάγνωση των στοιχείων");
            return ok(result);
        }
    }




















    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result exportBillingsAsXls(final Http.Request request) throws IOException {
        ObjectNode result = Json.newObject();
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                if (json == null) {
                    result.put("status", "error");
                    result.put("message", "Δεν εχετε αποστειλει εγκυρα δεδομενα.");
                    return ok(result);
                } else {
                    ObjectMapper ow = new ObjectMapper();
                    String jsonResult = "";
                    CompletableFuture<String> createXLSResult = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(
                                        entityManager -> {
                                            ObjectNode resultNode = Json.newObject();
                                            String random_id = json.findPath("random_id").asText();
                                            Random rand = new Random();
                                            String filename =  ConfigFactory.load().getString("uploads_reports")+"billings" + random_id + ".xls";
                                            HSSFWorkbook workbook = new HSSFWorkbook();
                                            HSSFSheet sheet = workbook.createSheet("FirstSheet");
                                            HSSFRow rowhead = sheet.createRow((short) 0);
                                            rowhead.createCell((short) 0).setCellValue("BILLING ID");
                                            rowhead.createCell((short) 1).setCellValue("ΛΟΓΑΡΙΑΣΜΟΣ");
                                            rowhead.createCell((short) 2).setCellValue("ΠΕΡΙΓΡΑΦΗ");
                                            rowhead.createCell((short) 3).setCellValue("ΗΜΕΡΟΜΗΝΙΑ ΔΗΜΙΟΥΡΓΙΑΣ");
                                            String sql = "select * from billings b ";
                                            List<BillingsEntity> billingsEntityList = (List<BillingsEntity>)
                                                    entityManager.createNativeQuery(sql, BillingsEntity.class).getResultList();
                                            for (int i = 0; i < billingsEntityList.size(); i++) {
                                                HSSFRow row = sheet.createRow((short) i + 1);
                                                row.createCell((short) 0).setCellValue(billingsEntityList.get(i).getId());
                                                row.createCell((short) 1).setCellValue(billingsEntityList.get(i).getName());
                                                row.createCell((short) 2).setCellValue(billingsEntityList.get(i).getDescription());
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                                String creationDateString = myDateFormat.format(billingsEntityList.get(i).getCreationDate());
                                                row.createCell((short) 3).setCellValue(creationDateString);
                                            }
                                            for (int col = 0; col < 13; col++) {
                                                sheet.autoSizeColumn(col);
                                            }
                                            FileOutputStream fileOut = null;
                                            try {
                                                fileOut = new FileOutputStream(filename);
                                                workbook.write(fileOut);
                                                fileOut.close();
                                                return filename;
                                            } catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            resultNode.put("message", "success");
                                            return filename;
                                        });
                            },
                            executionContext);
                    String ret_path = createXLSResult.get();
                    File previewFile = new File(ret_path);
                    return ok(previewFile);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Πρόβλημα κατά την ανάγνωση των στοιχείων");
            return ok(result);
        }
    }












}