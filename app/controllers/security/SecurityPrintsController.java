package controllers.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import controllers.execution_context.DatabaseExecutionContext;
import models.DepartmentsEntity;
import models.OrganizationsEntity;
import models.RolesEntity;
import models.UsersEntity;
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

public class SecurityPrintsController {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public SecurityPrintsController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }

    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result exportUsersAsXLS(final Http.Request request) throws IOException {
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
                                            String filename =  ConfigFactory.load().getString("uploads_reports")+"users" + random_id + ".xls";
                                            HSSFWorkbook workbook = new HSSFWorkbook();
                                            HSSFSheet sheet = workbook.createSheet("FirstSheet");
                                            HSSFRow rowhead = sheet.createRow((short) 0);
                                            rowhead.createCell((short) 0).setCellValue("USER_ID");
                                            rowhead.createCell((short) 1).setCellValue("USERNAME");
                                            rowhead.createCell((short) 2).setCellValue("EMAIL");
                                            rowhead.createCell((short) 3).setCellValue("ΟΝΟΜΑ");
                                            rowhead.createCell((short) 4).setCellValue("ΕΠΙΘΕΤΟ");
                                            rowhead.createCell((short) 5).setCellValue("ΤΗΛΕΦΩΝΟ");
                                            rowhead.createCell((short) 6).setCellValue("ΚΙΝΗΤΟ");
                                            rowhead.createCell((short) 7).setCellValue("ΚΑΤΑΣΤΑΣΗ");
                                            rowhead.createCell((short) 8).setCellValue("TOKEN");
                                            rowhead.createCell((short) 9).setCellValue("ΡΟΛΟΣ");
                                            rowhead.createCell((short) 10).setCellValue("ΟΡΓΑΝΙΣΜΟΣ");
                                            rowhead.createCell((short) 11).setCellValue("ΤΜΗΜΑ");
                                            rowhead.createCell((short) 12).setCellValue("ΗΜΕΡΟΜΗΝΙΑ ΔΗΜΙΟΥΡΓΙΑΣ");
                                            String sql = "select * from users";
                                            List<UsersEntity> usersEntityList = (List<UsersEntity>)
                                                    entityManager.createNativeQuery(sql, UsersEntity.class).getResultList();
                                            for (int i = 0; i < usersEntityList.size(); i++) {
                                                HSSFRow row = sheet.createRow((short) i + 1);
                                                row.createCell((short) 0).setCellValue(usersEntityList.get(i).getUserId());
                                                row.createCell((short) 1).setCellValue(usersEntityList.get(i).getUsername());
                                                row.createCell((short) 2).setCellValue(usersEntityList.get(i).getEmail());
                                                row.createCell((short) 3).setCellValue(usersEntityList.get(i).getFirstname());
                                                row.createCell((short) 4).setCellValue(usersEntityList.get(i).getLastname());
                                                row.createCell((short) 5).setCellValue(usersEntityList.get(i).getPhone());
                                                row.createCell((short) 6).setCellValue(usersEntityList.get(i).getMobilePhone());
                                                row.createCell((short) 7).setCellValue(usersEntityList.get(i).getStatus());
                                                row.createCell((short) 8).setCellValue(usersEntityList.get(i).getToken());
                                                if(usersEntityList.get(i).getRoleId()!=0){
                                                    row.createCell((short) 9).setCellValue(entityManager.find(RolesEntity.class, usersEntityList.get(i).getRoleId()).getName());
                                                }else{
                                                    row.createCell((short) 9).setCellValue("-");
                                                }
                                                if( usersEntityList.get(i).getOrgId()!=0){
                                                    row.createCell((short) 10).setCellValue(entityManager.find(OrganizationsEntity.class, usersEntityList.get(i).getOrgId()).getName());
                                                }else{
                                                    row.createCell((short) 10).setCellValue("-");
                                                }
                                                if(usersEntityList.get(i).getDepId()!=0){
                                                    row.createCell((short) 11).setCellValue(entityManager.find(DepartmentsEntity.class, usersEntityList.get(i).getDepId()).getDepartment());
                                                }else{
                                                    row.createCell((short) 11).setCellValue("-");

                                                }
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                                String creationDateString = myDateFormat.format(usersEntityList.get(i).getCreationDate());
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
    public Result exportDepartmentsAsXLS(final Http.Request request) throws IOException {
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
                                            String filename =  ConfigFactory.load().getString("uploads_reports")+"departments" + random_id + ".xls";
                                            HSSFWorkbook workbook = new HSSFWorkbook();
                                            HSSFSheet sheet = workbook.createSheet("FirstSheet");
                                            HSSFRow rowhead = sheet.createRow((short) 0);
                                            rowhead.createCell((short) 0).setCellValue("DEPARTMENT_ID");
                                            rowhead.createCell((short) 1).setCellValue("DEPARTMENT");
                                            rowhead.createCell((short) 2).setCellValue("ΗΜΕΡΟΜΗΝΙΑ ΔΗΜΙΟΥΡΓΙΑΣ");
                                            String sql = "select * from departments";
                                            List<DepartmentsEntity> departmentsEntitiesList = (List<DepartmentsEntity>)
                                                    entityManager.createNativeQuery(sql, DepartmentsEntity.class).getResultList();
                                            for (int i = 0; i < departmentsEntitiesList.size(); i++) {
                                                HSSFRow row = sheet.createRow((short) i + 1);
                                                row.createCell((short) 0).setCellValue(departmentsEntitiesList.get(i).getId());
                                                row.createCell((short) 1).setCellValue(departmentsEntitiesList.get(i).getDepartment());
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                                String creationDateString = myDateFormat.format(departmentsEntitiesList.get(i).getCreationDate());
                                                row.createCell((short) 2).setCellValue(creationDateString);
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
    public Result exportRolesAsXLS(final Http.Request request) throws IOException {
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
                                            String filename =  ConfigFactory.load().getString("uploads_reports")+"roles" + random_id + ".xls";
                                            HSSFWorkbook workbook = new HSSFWorkbook();
                                            HSSFSheet sheet = workbook.createSheet("FirstSheet");
                                            HSSFRow rowhead = sheet.createRow((short) 0);
                                            rowhead.createCell((short) 0).setCellValue("ROLE_ID");
                                            rowhead.createCell((short) 1).setCellValue("ΡΟΛΟΣ");
                                            rowhead.createCell((short) 2).setCellValue("ΠΕΡΙΓΡΑΦΗ");
                                            rowhead.createCell((short) 3).setCellValue("ΗΜΕΡΟΜΗΝΙΑ ΔΗΜΙΟΥΡΓΙΑΣ");
                                            String sql = "select * from roles";
                                            List<RolesEntity> rolesEntityList = (List<RolesEntity>)
                                                    entityManager.createNativeQuery(sql, RolesEntity.class).getResultList();
                                            for (int i = 0; i < rolesEntityList.size(); i++) {
                                                HSSFRow row = sheet.createRow((short) i + 1);
                                                row.createCell((short) 0).setCellValue(rolesEntityList.get(i).getRoleId());
                                                row.createCell((short) 1).setCellValue(rolesEntityList.get(i).getName());
                                                row.createCell((short) 2).setCellValue(rolesEntityList.get(i).getDescription());
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                                String creationDateString = myDateFormat.format(rolesEntityList.get(i).getCreationDate());
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
    public Result exportOrganizationsAsXLS(final Http.Request request) throws IOException {
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
                                            String filename =  ConfigFactory.load().getString("uploads_reports")+"organizations" + random_id + ".xls";
                                            HSSFWorkbook workbook = new HSSFWorkbook();
                                            HSSFSheet sheet = workbook.createSheet("FirstSheet");
                                            HSSFRow rowhead = sheet.createRow((short) 0);
                                            rowhead.createCell((short) 0).setCellValue("ORGANIZATION ID");
                                            rowhead.createCell((short) 1).setCellValue("ΟΡΓΑΝΙΣΜΟΣ");
                                            rowhead.createCell((short) 2).setCellValue("ΗΜΕΡΟΜΗΝΙΑ ΔΗΜΙΟΥΡΓΙΑΣ");
                                            String sql = "select * from organizations";
                                            List<OrganizationsEntity> organizationsEntityList = (List<OrganizationsEntity>)
                                                    entityManager.createNativeQuery(sql, OrganizationsEntity.class).getResultList();
                                            for (int i = 0; i < organizationsEntityList.size(); i++) {
                                                HSSFRow row = sheet.createRow((short) i + 1);
                                                row.createCell((short) 0).setCellValue(organizationsEntityList.get(i).getOrganizationId());
                                                row.createCell((short) 1).setCellValue(organizationsEntityList.get(i).getName());
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                                String creationDateString = myDateFormat.format(organizationsEntityList.get(i).getCreationDate());
                                                row.createCell((short) 2).setCellValue(creationDateString);
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
    public Result deleteXLSFromFolder(final Http.Request request) throws IOException {
        ObjectNode result = Json.newObject();
        try {
            JsonNode json = request.body().asJson();
            ObjectMapper ow = new ObjectMapper();
            CompletableFuture<JsonNode> getFuture = CompletableFuture.supplyAsync(() -> {
                        return jpaApi.withTransaction(
                                entityManager -> {
                                    String system_random_id = json.findPath("system_random_id").asText();
                                    ObjectNode res = Json.newObject();
                                    File dest = new File( ConfigFactory.load().getString("uploads_reports") + system_random_id + ".xls");
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
