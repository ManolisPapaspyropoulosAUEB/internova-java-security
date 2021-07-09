package controllers.procedures;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import controllers.BaseJasperReport;
import controllers.execution_context.DatabaseExecutionContext;
import controllers.system.Application;
import models.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.util.ResourceUtils;
import play.api.db.Database;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

public class ProceduresPrintController extends Application {

    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;
    private Database db;


    @Inject
    public ProceduresPrintController(Database db, JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        super(jpaApi, executionContext);
        this.jpaApi = jpaApi;
        this.db = db;
        this.executionContext = executionContext;
    }





    public ByteArrayInputStream generateOrdeReport(String selectedOrdersIds) throws FileNotFoundException, JRException {
        ObjectNode reportParams = Json.newObject();
        reportParams.put("selected_orders_ids", " timeline_table.order_id in "+selectedOrdersIds);
        ByteArrayInputStream export=null;
        export = (ByteArrayInputStream) BaseJasperReport.generatePDF("selectedOrders/main", reportParams);
        return export;
    }


    public ByteArrayInputStream generateOfferReport(String offerId,String user,String lng,String company,String label1,String fromCountryCity,String toCountryCity,String ourOffer,String paymentMethod) throws FileNotFoundException, JRException {
        ObjectNode reportParams = Json.newObject();
        reportParams.put("offer_id", offerId);
        reportParams.put("user", user);
        reportParams.put("label1", label1);
        reportParams.put("fromCountryCity", fromCountryCity);
        reportParams.put("toCountryCity", toCountryCity);
        reportParams.put("ourOffer", ourOffer);
        reportParams.put("paymentMethod", paymentMethod);
        ByteArrayInputStream export=null;
        if(lng.equalsIgnoreCase("el") && company.equalsIgnoreCase("internova")){
            export = (ByteArrayInputStream) BaseJasperReport.generatePDF("offers_internova/internova_offer_gr", reportParams);
            return export;
        }else if(lng.equalsIgnoreCase("en") && company.equalsIgnoreCase("internova")){
            export = (ByteArrayInputStream) BaseJasperReport.generatePDF("offers_internova _english/internova_offer_en", reportParams);
            return export;
        }else if(lng.equalsIgnoreCase("el") && company.equalsIgnoreCase("nova")){
            export = (ByteArrayInputStream) BaseJasperReport.generatePDF("offers_nova/nova_gr", reportParams);
            return export;
        }else if (lng.equalsIgnoreCase("en") && company.equalsIgnoreCase("nova")){
            export = (ByteArrayInputStream) BaseJasperReport.generatePDF("offers_nova_english/nova_en", reportParams);
            return export;
        }
        return export;
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result exportSelectedSchedulesOffersJasper(final Http.Request request) throws IOException {
        try {
            Result result;
            String offerId = request.queryString("offerId").get();
            String userId = request.queryString("userId").get();
            String lng = request.queryString("lng").get();
            String company = request.queryString("company").get();
            String label1 = request.queryString("label1").get();
            String fromCountryCity = request.queryString("fromCountryCity").get();
            String toCountryCity = request.queryString("toCountryCity").get();
            String ourOffer = request.queryString("ourOffer").get();
            String paymentMethod = request.queryString("paymentMethod").get();
            CompletableFuture<Result> addFuture = CompletableFuture.supplyAsync(() -> {
                        return jpaApi.withTransaction(entityManager -> {
                            UsersEntity user = entityManager.find(UsersEntity.class,Long.valueOf(userId));
                            ObjectNode add_result = Json.newObject();
                            try {
                                ByteArrayInputStream  export=
                                        generateOfferReport(offerId,user.getFirstname()+" "+user.getLastname(),lng,company,label1,fromCountryCity,toCountryCity,ourOffer,paymentMethod);
                                add_result.put("status", "success");
                                add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
                                return ok(export).as("application/pdf; charset=UTF-8");
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();

                            } catch (JRException e) {
                                e.printStackTrace();
                            }
                            add_result.put("status", "error");
                            add_result.put("message", "Προβλημα κατα την καταχωρηση");
                            return ok(add_result);
                        });
                    },
                    executionContext);
            result = addFuture.get();
            return result;
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την καταχωρηση");
            return ok(result);
        }
    }



    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result exportSelectedOrdersJasper(final Http.Request request) throws IOException {
        try {
            Result result;
            String selectedOrdersIds = request.queryString("selectedOrdersIds").get();
            CompletableFuture<Result> addFuture = CompletableFuture.supplyAsync(() -> {
                        return jpaApi.withTransaction(entityManager -> {
                            ObjectNode add_result = Json.newObject();
                            try {
                                ByteArrayInputStream  export=
                                        generateOrdeReport(selectedOrdersIds);
                                add_result.put("status", "success");
                                add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
                                return ok(export).as("application/pdf; charset=UTF-8");
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();

                            } catch (JRException e) {
                                e.printStackTrace();
                            }
                            add_result.put("status", "error");
                            add_result.put("message", "Προβλημα κατα την καταχωρηση");
                            return ok(add_result);
                        });
                    },
                    executionContext);
            result = addFuture.get();
            return result;
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την καταχωρηση");
            return ok(result);
        }
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
                                            String filename = ConfigFactory.load().getString("uploads_reports") + "offers" + random_id + ".xls";
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
                                                row.createCell((short) 2).setCellValue(entityManager.find(CustomersSuppliersEntity.class, offersEntityList.get(i).getCustomerId()).getBrandName());
                                                row.createCell((short) 3).setCellValue(offersEntityList.get(i).getStatus());
                                                row.createCell((short) 4).setCellValue(offersEntityList.get(i).getFromAddress());
                                                row.createCell((short) 5).setCellValue(offersEntityList.get(i).getToAddress());

                                                if (offersEntityList.get(i).getSellerId() != null) {
                                                    row.createCell((short) 6).setCellValue(entityManager.find(InternovaSellersEntity.class, offersEntityList.get(i).getSellerId()).getName());

                                                } else {
                                                    row.createCell((short) 6).setCellValue(entityManager.find(InternovaSellersEntity.class, customersSuppliersEntity.getInternovaSellerId()).getName());

                                                }
                                                if (offersEntityList.get(i).getBillingId() != null) {
                                                    row.createCell((short) 7).setCellValue(entityManager.find(BillingsEntity.class, offersEntityList.get(i).getBillingId()).getName());

                                                } else {
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


    public String exportReport(String reportFormat) throws FileNotFoundException, JRException {
        String path = "C:\\Users\\basan\\Desktop\\Report";
        List employees = null;
        //load file and compile it
        File file = ResourceUtils.getFile("classpath:employees.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(employees);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("createdBy", "Java Techie");
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        if (reportFormat.equalsIgnoreCase("html")) {
            JasperExportManager.exportReportToHtmlFile(jasperPrint, path + "\\employees.html");
        }
        if (reportFormat.equalsIgnoreCase("pdf")) {
            JasperExportManager.exportReportToPdfFile(jasperPrint, path + "\\employees.pdf");
        }

        return "report generated in path : " + path;
    }



    public ByteArrayInputStream generateOrderLoadingReport (String orderLoadingId,String user,String lng,String company) throws FileNotFoundException, JRException {
        ObjectNode reportParams = Json.newObject();
        reportParams.put("order_loading_id", orderLoadingId);
        reportParams.put("user", user);
        ByteArrayInputStream export=null;
        if(lng.equalsIgnoreCase("el") && company.equalsIgnoreCase("internova")){
             export = (ByteArrayInputStream) BaseJasperReport.generatePDF("anathesh/main", reportParams);
            return export;
        }else if(lng.equalsIgnoreCase("en") && company.equalsIgnoreCase("internova")){
             export = (ByteArrayInputStream) BaseJasperReport.generatePDF("anathesh_eng/main", reportParams);
            return export;
        }else if(lng.equalsIgnoreCase("el") && company.equalsIgnoreCase("nova")){
             export = (ByteArrayInputStream) BaseJasperReport.generatePDF("anathesh_nova/main", reportParams);
            return export;
        }else if (lng.equalsIgnoreCase("en") && company.equalsIgnoreCase("nova")){
             export = (ByteArrayInputStream) BaseJasperReport.generatePDF("anathesh_nova_eng/main", reportParams);
            return export;
        }
        return export;
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result exportOrderLoadingJasper(final Http.Request request) throws IOException {
        try {
            Result result;
            String order_loading_id = request.queryString("order_loading_id").get();
            String userId = request.queryString("userId").get();
            String lng = request.queryString("lng").get();
            String company = request.queryString("company").get();
            CompletableFuture<Result> addFuture = CompletableFuture.supplyAsync(() -> {
                        return jpaApi.withTransaction(entityManager -> {
                            UsersEntity user = entityManager.find(UsersEntity.class,Long.valueOf(userId));
                            ObjectNode add_result = Json.newObject();
                            try {
                                ByteArrayInputStream  export=  generateOrderLoadingReport(order_loading_id,user.getFirstname()+" "+user.getLastname(),lng,company);
                                add_result.put("status", "success");
                                add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
                                return ok(export).as("application/pdf; charset=UTF-8");
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();

                            } catch (JRException e) {
                                e.printStackTrace();
                            }
                            add_result.put("status", "error");
                            add_result.put("message", "Προβλημα κατα την καταχωρηση");
                            return ok(add_result);
                        });
                    },
                    executionContext);
            result = addFuture.get();
            return result;
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την καταχωρηση");
            return ok(result);
        }
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result exportOrdersAsXls(final Http.Request request) throws IOException {
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
                                            String filename = ConfigFactory.load().getString("uploads_reports") + "orders" + random_id + ".xls";
                                            HSSFWorkbook workbook = new HSSFWorkbook();
                                            HSSFSheet sheet = workbook.createSheet("FirstSheet");
                                            HSSFRow rowhead = sheet.createRow((short) 0);
                                            rowhead.createCell((short) 0).setCellValue("ID");
                                            rowhead.createCell((short) 1).setCellValue("OFFER ID");
                                            rowhead.createCell((short) 2).setCellValue("ΠΕΛΑΤΗΣ");
                                            rowhead.createCell((short) 3).setCellValue("ΚΑΤΑΣΤΑΣΗ");
                                            rowhead.createCell((short) 4).setCellValue("ΔΡΟΜΟΛΟΓΙΟ");
                                            rowhead.createCell((short) 5).setCellValue("ΗΜΕΡΟΜΗΝΙΑ ΠΑΡΑΓΓΕΛΙΑΣ");
                                            String sql = "select * from orders ";
                                            List<OrdersEntity> ordersEntityList = (List<OrdersEntity>)
                                                    entityManager.createNativeQuery(sql, OrdersEntity.class).getResultList();
                                            for (int i = 0; i < ordersEntityList.size(); i++) {
                                                CustomersSuppliersEntity customersSuppliersEntity = entityManager.find(CustomersSuppliersEntity.class, ordersEntityList.get(i).getCustomerId());
                                                HSSFRow row = sheet.createRow((short) i + 1);
                                                row.createCell((short) 0).setCellValue(ordersEntityList.get(i).getId());
                                                row.createCell((short) 1).setCellValue(ordersEntityList.get(i).getOfferId());
                                                row.createCell((short) 2).setCellValue(entityManager.find(CustomersSuppliersEntity.class, ordersEntityList.get(i).getCustomerId()).getBrandName());
                                                row.createCell((short) 3).setCellValue(ordersEntityList.get(i).getStatus());
                                                String sqlOrdersSchedules = "select * from order_schedules ord_s where ord_s.order_id=" + ordersEntityList.get(i).getId()
                                                        + " and ord_s.primary_schedule=1 ";

                                                List<OrderSchedulesEntity> osList =
                                                        entityManager.createNativeQuery(sqlOrdersSchedules, OrderSchedulesEntity.class).getResultList();
                                                row.createCell((short) 4).setCellValue(osList.get(0).getFromCountry() + " " + osList.get(0).getFromCity() + "  /  "
                                                        + osList.get(0).getToCountry() + " " + osList.get(0).getToCity());
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                                String creationDateString = myDateFormat.format(ordersEntityList.get(i).getCreationDate());
                                                row.createCell((short) 5).setCellValue(creationDateString);
                                            }
                                            for (int col = 0; col < 6; col++) {
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
                                            String filename = ConfigFactory.load().getString("uploads_reports") + "schedules" + random_id + ".xls";
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
                                                    " sp.unit_price" +
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
