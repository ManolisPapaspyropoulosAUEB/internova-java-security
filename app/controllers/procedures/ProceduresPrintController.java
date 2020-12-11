package controllers.procedures;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import controllers.execution_context.DatabaseExecutionContext;
import controllers.system.Application;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

public class ProceduresPrintController extends Application {

    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;


    @Inject
    public ProceduresPrintController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        super(jpaApi,  executionContext);
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }












    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result exportOffersAsXls(final Http.Request request) throws IOException {
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
                                            String filename =  ConfigFactory.load().getString("uploads_reports")+"offers" + random_id + ".xls";
                                            HSSFWorkbook workbook = new HSSFWorkbook();
                                            HSSFSheet sheet = workbook.createSheet("FirstSheet");
                                            HSSFRow rowhead = sheet.createRow((short) 0);
                                            rowhead.createCell((short) 0).setCellValue("ID");
                                            rowhead.createCell((short) 1).setCellValue("A/A ΠΡΟΣΦΟΡΑΣ");
                                            rowhead.createCell((short) 2).setCellValue("ΠΕΛΑΤΗΣ");
                                            rowhead.createCell((short) 3).setCellValue("ΚΑΤΑΣΤΑΣΗ");
                                            rowhead.createCell((short) 4).setCellValue("ΑΠΟ(Αφετηρία)");
                                            rowhead.createCell((short) 5).setCellValue("ΠΡΟΣ(Προορισμός)");
                                            rowhead.createCell((short) 6).setCellValue("ΠΩΛΗΤΗΣ");
                                            rowhead.createCell((short) 7).setCellValue("BILLING");
                                            rowhead.createCell((short) 8).setCellValue("ΗΜΕΡΟΜΗΝΙΑ ΠΡΟΣΦΟΡΑΣ");
                                            String sql = "select * from offers ";
                                            List<OffersEntity> offersEntityList = (List<OffersEntity>)
                                                    entityManager.createNativeQuery(sql, OffersEntity.class).getResultList();
                                            for (int i = 0; i < offersEntityList.size(); i++) {
                                                CustomersSuppliersEntity customersSuppliersEntity = entityManager.find(CustomersSuppliersEntity.class, offersEntityList.get(i).getCustomerId());
                                                HSSFRow row = sheet.createRow((short) i + 1);
                                                row.createCell((short) 0).setCellValue(offersEntityList.get(i).getId());
                                                row.createCell((short) 1).setCellValue(offersEntityList.get(i).getAa());
                                                row.createCell((short) 2).setCellValue( entityManager.find(CustomersSuppliersEntity.class,offersEntityList.get(i).getCustomerId()).getBrandName());
                                                row.createCell((short) 3).setCellValue(offersEntityList.get(i).getStatus());
                                                row.createCell((short) 4).setCellValue(offersEntityList.get(i).getFromAddress());
                                                row.createCell((short) 5).setCellValue(offersEntityList.get(i).getToAddress());

                                                if(offersEntityList.get(i).getSellerId()!=null){
                                                    row.createCell((short) 6).setCellValue(entityManager.find(InternovaSellersEntity.class,offersEntityList.get(i).getSellerId()).getName());

                                                }else{
                                                    row.createCell((short) 6).setCellValue(entityManager.find(InternovaSellersEntity.class, customersSuppliersEntity.getInternovaSellerId()).getName());

                                                }
                                                if(offersEntityList.get(i).getBillingId()!=null){
                                                    row.createCell((short) 7).setCellValue(entityManager.find(BillingsEntity.class,offersEntityList.get(i).getBillingId()).getName());

                                                }else{
                                                    row.createCell((short) 7).setCellValue(entityManager.find(BillingsEntity.class, customersSuppliersEntity.getBillingId()).getName());
                                                }
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                                String creationDateString = myDateFormat.format(offersEntityList.get(i).getOfferDate());
                                                row.createCell((short) 8).setCellValue(creationDateString);
                                            }
                                            for (int col = 0; col < 8; col++) {
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
    public Result exportScheduleAsXlsx(final Http.Request request) throws IOException {
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
                                            String filename =  ConfigFactory.load().getString("uploads_reports")+"schedules" + random_id + ".xls";
                                            HSSFWorkbook workbook = new HSSFWorkbook();
                                            HSSFSheet sheet = workbook.createSheet("FirstSheet");
                                            HSSFRow rowhead = sheet.createRow((short) 0);
                                            rowhead.createCell((short) 0).setCellValue("ΠΟΛΗ ΑΦΕΤΗΡΙΑΣ");
                                            rowhead.createCell((short) 1).setCellValue("ΧΩΡΑ ΑΦΕΤΗΡΙΑΣ");
                                            rowhead.createCell((short) 2).setCellValue("ΠΟΛΗ ΠΡΟΟΡΙΣΜΟΥ");
                                            rowhead.createCell((short) 3).setCellValue("ΧΩΡΑ ΠΡΟΟΡΙΣΜΟΥ");
                                            rowhead.createCell((short) 4).setCellValue("ΣΥΣΚΕΥΑΣΙΑ");
                                            rowhead.createCell((short) 5).setCellValue("ΜΗΚΟΣ (m)");
                                            rowhead.createCell((short) 6).setCellValue("ΥΨΟΣ (m)");
                                            rowhead.createCell((short) 7).setCellValue("ΠΛΑΤΟΣ (m)");
                                            rowhead.createCell((short) 8).setCellValue("ΟΓΚΟΣ (m^3)");
                                            rowhead.createCell((short) 9).setCellValue("ΣΧΟΛΙΑ (m)");
                                            rowhead.createCell((short) 10).setCellValue("ΑΠΟ");
                                            rowhead.createCell((short) 11).setCellValue("ΕΩΣ");
                                            rowhead.createCell((short) 12).setCellValue("ΤΙΜΗ ΜΟΝΑΔΑΣ(€)");
                                            String sql = "select " +
                                                    " s.from_city," +
                                                    " s.from_country," +
                                                    " s.to_city," +
                                                    " s.to_country," +
                                                    " mu.title," +
                                                    " mu.x_index," +
                                                    " mu.y_index," +
                                                    " mu.z_index," +
                                                    " mu.volume," +
                                                    " mu.comments," +
                                                    " sp.from_unit," +
                                                    " sp.to_unit," +
                                                    " sp.unit_price"+
                                                    " from schedule s " +
                                                    " left join  schedule_packages sp on (sp.schedule_id = s.id) " +
                                                    " left join measurement_unit mu on (mu.id=sp.measurement_unit_id) " +
                                                    " order by s.creation_date desc ";
                                            List scheduleList =
                                                    entityManager.createNativeQuery(sql).getResultList();
                                            Iterator it = scheduleList.iterator();
                                            ArrayList<HashMap<String, Object>> scheduleFinalList = new ArrayList<>();
                                            int k = -1;
                                            while (it.hasNext()) {
                                                k++;
                                                HSSFRow row = sheet.createRow((short) k + 2);
                                                JsonNode tu = Json.toJson(it.next());
                                                row.createCell((short) 0).setCellValue(tu.get(0).asText());
                                                row.createCell((short) 1).setCellValue(tu.get(1).asText());
                                                row.createCell((short) 2).setCellValue(tu.get(2).asText());
                                                row.createCell((short) 3).setCellValue(tu.get(3).asText());
                                                row.createCell((short) 4).setCellValue(tu.get(4).asText());
                                                row.createCell((short) 5).setCellValue(tu.get(5).asText());
                                                row.createCell((short) 6).setCellValue(tu.get(6).asText());
                                                row.createCell((short) 7).setCellValue(tu.get(7).asText());
                                                row.createCell((short) 8).setCellValue(tu.get(8).asText());
                                                row.createCell((short) 9).setCellValue(tu.get(9).asText());
                                                row.createCell((short) 10).setCellValue(tu.get(10).asText());
                                                row.createCell((short) 11).setCellValue(tu.get(11).asText());
                                                row.createCell((short) 12).setCellValue(tu.get(12).asText());
                                            }

                                            for (int col = 0; col < 12; col++) {
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
