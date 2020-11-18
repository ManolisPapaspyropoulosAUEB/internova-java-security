package controllers.archives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

public class ArchivesPrintController {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public ArchivesPrintController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }

    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result exportWarehousesAsXLS(final Http.Request request) throws IOException {
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
                                            String filename = "D:/developm/internova(Pr)/internova_JAVA_security/app/reports/users" + random_id + ".xls";
                                            HSSFWorkbook workbook = new HSSFWorkbook();
                                            HSSFSheet sheet = workbook.createSheet("FirstSheet");
                                            HSSFRow rowhead = sheet.createRow((short) 0);
                                            rowhead.createCell((short) 0).setCellValue("WAREHOUSE_ID");
                                            rowhead.createCell((short) 1).setCellValue("ΕΠΩΝΥΜΙΑ");
                                            rowhead.createCell((short) 2).setCellValue("ΥΠΕΥΘΥΝΟΣ");
                                            rowhead.createCell((short) 3).setCellValue("ΔΙΕΥΘΥΝΣΗ");
                                            rowhead.createCell((short) 4).setCellValue("ΤΗΛΕΦΩΝΟ");
                                            rowhead.createCell((short) 5).setCellValue("ΕΜΑΙΛ");
                                            rowhead.createCell((short) 6).setCellValue("Τ.Κ");
                                            rowhead.createCell((short) 7).setCellValue("ΠΟΛΗ");
                                            rowhead.createCell((short) 8).setCellValue("ΠΕΡΙΟΧΗ");
                                            rowhead.createCell((short) 9).setCellValue("ΣΧΟΛΙΑ");
                                            rowhead.createCell((short) 10).setCellValue("ΗΜΕΡΟΜΗΝΙΑ ΔΗΜΙΟΥΡΓΙΑΣ");
                                            String sql = "select * from warehouses ";
                                            List<WarehousesEntity> warehousesEntityList = (List<WarehousesEntity>)
                                                    entityManager.createNativeQuery(sql, WarehousesEntity.class).getResultList();
                                            for (int i = 0; i < warehousesEntityList.size(); i++) {
                                                HSSFRow row = sheet.createRow((short) i + 1);
                                                row.createCell((short) 0).setCellValue(warehousesEntityList.get(i).getId());
                                                row.createCell((short) 1).setCellValue(warehousesEntityList.get(i).getBrandName());
                                                row.createCell((short) 2).setCellValue(warehousesEntityList.get(i).getEmail());
                                                row.createCell((short) 3).setCellValue(warehousesEntityList.get(i).getManager());
                                                row.createCell((short) 4).setCellValue(warehousesEntityList.get(i).getAddress());
                                                row.createCell((short) 5).setCellValue(warehousesEntityList.get(i).getEmail());
                                                row.createCell((short) 6).setCellValue(warehousesEntityList.get(i).getPostalCode());
                                                row.createCell((short) 7).setCellValue(warehousesEntityList.get(i).getCity());
                                                row.createCell((short) 8).setCellValue(warehousesEntityList.get(i).getRegion());
                                                row.createCell((short) 9).setCellValue(warehousesEntityList.get(i).getComments());
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                                String creationDateString = myDateFormat.format(warehousesEntityList.get(i).getCreationDate());
                                                row.createCell((short) 10).setCellValue(creationDateString);
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
    public Result exportFactoriesAsXLS(final Http.Request request) throws IOException {  // san parametro pernei to org key
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
                                            String filename = "D:/developm/internova(Pr)/internova_JAVA_security/app/reports/users" + random_id + ".xls";
                                            HSSFWorkbook workbook = new HSSFWorkbook();
                                            HSSFSheet sheet = workbook.createSheet("FirstSheet");
                                            HSSFRow rowhead = sheet.createRow((short) 0);
                                            rowhead.createCell((short) 0).setCellValue("FACTORY_ID");
                                            rowhead.createCell((short) 1).setCellValue("ΕΠΩΝΥΜΙΑ");
                                            rowhead.createCell((short) 2).setCellValue("ΥΠΕΥΘΥΝΟΣ");
                                            rowhead.createCell((short) 3).setCellValue("ΔΙΕΥΘΥΝΣΗ");
                                            rowhead.createCell((short) 4).setCellValue("ΤΗΛΕΦΩΝΟ");
                                            rowhead.createCell((short) 5).setCellValue("ΕΜΑΙΛ");
                                            rowhead.createCell((short) 6).setCellValue("Τ.Κ");
                                            rowhead.createCell((short) 7).setCellValue("ΠΟΛΗ");
                                            rowhead.createCell((short) 8).setCellValue("ΠΕΡΙΟΧΗ");
                                            rowhead.createCell((short) 9).setCellValue("ΣΧΟΛΙΑ");
                                            rowhead.createCell((short) 10).setCellValue("ΗΜΕΡΕΣ ΠΟΥ ΧΡΕΙΑΖΟΝΤΑΙ ΓΙΑ ΤΟ ΡΑΝΤΕΒΟΥ");
                                            rowhead.createCell((short) 11).setCellValue("ΧΩΡΑ");
                                            rowhead.createCell((short) 12).setCellValue("ΗΜΕΡΟΜΗΝΙΑ ΔΗΜΙΟΥΡΓΙΑΣ");
                                            String sql = "select * from factories f ";
                                            List<FactoriesEntity> factoriesEntityList = (List<FactoriesEntity>)
                                                    entityManager.createNativeQuery(sql, FactoriesEntity.class).getResultList();
                                            for (int i = 0; i < factoriesEntityList.size(); i++) {
                                                HSSFRow row = sheet.createRow((short) i + 1);
                                                row.createCell((short) 0).setCellValue(factoriesEntityList.get(i).getId());
                                                row.createCell((short) 1).setCellValue(factoriesEntityList.get(i).getBrandName());
                                                row.createCell((short) 2).setCellValue(factoriesEntityList.get(i).getEmail());
                                                row.createCell((short) 3).setCellValue(factoriesEntityList.get(i).getManager());
                                                row.createCell((short) 4).setCellValue(factoriesEntityList.get(i).getAddress());
                                                row.createCell((short) 5).setCellValue(factoriesEntityList.get(i).getEmail());
                                                row.createCell((short) 6).setCellValue(factoriesEntityList.get(i).getPostalCode());
                                                row.createCell((short) 7).setCellValue(factoriesEntityList.get(i).getCity());
                                                row.createCell((short) 8).setCellValue(factoriesEntityList.get(i).getRegion());
                                                row.createCell((short) 9).setCellValue(factoriesEntityList.get(i).getComments());
                                                row.createCell((short) 10).setCellValue(factoriesEntityList.get(i).getAppointmentDays());
                                                row.createCell((short) 11).setCellValue(factoriesEntityList.get(i).getCountry());
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                                String creationDateString = myDateFormat.format(factoriesEntityList.get(i).getCreationDate());
                                                row.createCell((short) 12).setCellValue(creationDateString);
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
    public Result exportCustomersSuppliersAsXls(final Http.Request request) throws IOException {  // san parametro pernei to org key
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
                                            String filename = "D:/developm/internova(Pr)/internova_JAVA_security/app/reports/users" + random_id + ".xls";
                                            HSSFWorkbook workbook = new HSSFWorkbook();
                                            HSSFSheet sheet = workbook.createSheet("FirstSheet");
                                            HSSFRow rowhead = sheet.createRow((short) 0);
                                            rowhead.createCell((short) 0).setCellValue("ID");
                                            rowhead.createCell((short) 1).setCellValue("ΕΠΩΝΥΜΙΑ");
                                            rowhead.createCell((short) 2).setCellValue("ΤΥΠΟΣ");
                                            rowhead.createCell((short) 3).setCellValue("ΑΦΜ");
                                            rowhead.createCell((short) 4).setCellValue("ΔΟΥ");
                                            rowhead.createCell((short) 5).setCellValue("ΕΠΑΓΓΕΛΜΑ");
                                            rowhead.createCell((short) 6).setCellValue("ΔΙΕΥΘΥΝΣΗ");
                                            rowhead.createCell((short) 7).setCellValue("ΠΕΡΙΟΧΗ");
                                            rowhead.createCell((short) 8).setCellValue("ΧΩΡΑ");
                                            rowhead.createCell((short) 9).setCellValue("ΠΟΛΗ");
                                            rowhead.createCell((short) 10).setCellValue("ΤΚ");
                                            rowhead.createCell((short) 11).setCellValue("ΤΗΛΕΦΩΝΟ");
                                            rowhead.createCell((short) 12).setCellValue("EMAIL");
                                            rowhead.createCell((short) 13).setCellValue("WEBSITE");
                                            rowhead.createCell((short) 14).setCellValue("ΣΧΟΛΙΑ");
                                            rowhead.createCell((short) 15).setCellValue("ΗΜΕΡΟΜΗΝΙΑ ΔΗΜΙΟΥΡΓΙΑΣ");
                                            String sql = "select * from customers_suppliers ";
                                            List<CustomersSuppliersEntity> suppliersEntityList = (List<CustomersSuppliersEntity>)
                                                    entityManager.createNativeQuery(sql, CustomersSuppliersEntity.class).getResultList();
                                            for (int i = 0; i < suppliersEntityList.size(); i++) {
                                                HSSFRow row = sheet.createRow((short) i + 1);
                                                row.createCell((short) 0).setCellValue(suppliersEntityList.get(i).getId());
                                                row.createCell((short) 1).setCellValue(suppliersEntityList.get(i).getBrandName());
                                                row.createCell((short) 2).setCellValue(suppliersEntityList.get(i).getCustomerType());
                                                row.createCell((short) 3).setCellValue(suppliersEntityList.get(i).getAfm());
                                                row.createCell((short) 4).setCellValue(suppliersEntityList.get(i).getDoy());
                                                row.createCell((short) 5).setCellValue(suppliersEntityList.get(i).getJob());
                                                row.createCell((short) 6).setCellValue(suppliersEntityList.get(i).getAddress());
                                                row.createCell((short) 7).setCellValue(suppliersEntityList.get(i).getRegion());
                                                row.createCell((short) 8).setCellValue(suppliersEntityList.get(i).getCountry());
                                                row.createCell((short) 9).setCellValue(suppliersEntityList.get(i).getCity());
                                                row.createCell((short) 10).setCellValue(suppliersEntityList.get(i).getPostalCode());
                                                row.createCell((short) 11).setCellValue(suppliersEntityList.get(i).getTelephone());
                                                row.createCell((short) 12).setCellValue(suppliersEntityList.get(i).getEmail());
                                                row.createCell((short) 13).setCellValue(suppliersEntityList.get(i).getWebsite());
                                                row.createCell((short) 14).setCellValue(suppliersEntityList.get(i).getComments());
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                                String creationDateString = myDateFormat.format(suppliersEntityList.get(i).getCreationDate());
                                                row.createCell((short) 15).setCellValue(creationDateString);
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










    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result deleteXLSFromFolder(final Http.Request request) throws IOException {  // san parametro pernei to org key
        ObjectNode result = Json.newObject();
        try {
            JsonNode json = request.body().asJson();
            ObjectMapper ow = new ObjectMapper();
            CompletableFuture<JsonNode> getFuture = CompletableFuture.supplyAsync(() -> {
                        return jpaApi.withTransaction(
                                entityManager -> {
                                    String system_random_id = json.findPath("system_random_id").asText();
                                    ObjectNode res = Json.newObject();
                                    File dest = new File("D:/developm/internova(Pr)/internova_JAVA_security/app/reports/" + system_random_id + ".xls");
                                    dest.delete();
                                    res.put("status", "success");
                                    res.put("message", "Το εγγραφο διαγράφτηκε με επιτυχία");
                                    return res;
                                });
                    },
                    executionContext);
            result = (ObjectNode) getFuture.get();
            return ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Πρόβλημα κατά την διαγραφή");
            return ok(result);
        }
    }






















}
