package controllers.archives.customers_suppliers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.CustomersSuppliersEntity;
import models.WarehousesEntity;
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

public class CustomersSuppliersPrintController {

    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public CustomersSuppliersPrintController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
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







}
