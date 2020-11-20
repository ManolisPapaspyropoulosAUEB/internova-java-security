package controllers.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.*;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

public class DepartmentController {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public DepartmentController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }

    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addDepartment(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode result_add = Json.newObject();
                                    String department = json.findPath("department").asText();
                                    Integer status = json.findPath("status").asInt();
                                    DepartmentsEntity o = new DepartmentsEntity();
                                    o.setDepartment(department);
                                    o.setStatus(status);
                                    o.setCreationDate(new Date());
                                    entityManager.persist(o);
                                    result_add.put("status", "success");
                                    result_add.put("message", "Η καταχώρηση ολοκληρώθηκε με επιτυχία!");
                                    return result_add;
                                });
                            },
                            executionContext);
                    result = (ObjectNode) addFuture.get();
                    return ok(result);

                } catch (Exception e) {
                    ObjectNode result = Json.newObject();
                    e.printStackTrace();
                    result.put("status", "error");
                    result.put("message", "Προβλημα κατα την καταχωρηση");
                    return ok(result);
                }
            }
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την καταχωρηση");
            return ok(result);
        }
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateDepartment(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode result_update = Json.newObject();
                                    String department = json.findPath("department").asText();
                                    Long id = json.findPath("id").asLong();
                                    Integer status = json.findPath("status").asInt();
                                    DepartmentsEntity o = entityManager.find(DepartmentsEntity.class, id);
                                    o.setDepartment(department);
                                    o.setStatus(status);
                                    o.setUpdateDate(new Date());
                                    entityManager.persist(o);
                                    result_update.put("status", "success");
                                    result_update.put("message", "Η ενημέρωση ολοκληρώθηκε με επιτυχία!");
                                    return result_update;
                                });
                            },
                            executionContext);
                    result = (ObjectNode) updateFuture.get();
                    return ok(result);
                } catch (Exception e) {
                    ObjectNode result = Json.newObject();
                    e.printStackTrace();
                    result.put("status", "error");
                    result.put("message", "Προβλημα κατα την ενημερωση");
                    return ok(result);
                }
            }
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την ενημερωση");
            return ok(result);
        }
    }

    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteDepartment(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> deleteFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode delete_result = Json.newObject();
                                    Long id = json.findPath("id").asLong();
                                    String checkIfUsedSql = "select * from users r where r.dep_id=" + id;
                                    List<UsersEntity> usersEntityList = (List<UsersEntity>) entityManager.createNativeQuery(checkIfUsedSql, UsersEntity.class).getResultList();
                                    if (usersEntityList.size() > 0) {
                                        delete_result.put("status", "error");
                                        delete_result.put("message", "Βρέθηκαν συνδεδεμένες εγγραφές");
                                        return delete_result;
                                    }
                                    DepartmentsEntity o = entityManager.find(DepartmentsEntity.class, id);
                                    if (o != null) {
                                        entityManager.remove(o);
                                        delete_result.put("status", "success");
                                        delete_result.put("message", "Η διαγραφή ολοκληρώθηκε με επιτυχία!");
                                    } else {
                                        delete_result.put("status", "error");
                                        delete_result.put("message", "Δεν βρέθηκε σχετική θέση");
                                    }

                                    return delete_result;
                                });
                            },
                            executionContext);
                    result = (ObjectNode) deleteFuture.get();
                    return ok(result);

                } catch (Exception e) {
                    ObjectNode result = Json.newObject();
                    e.printStackTrace();
                    result.put("status", "error");
                    result.put("message", "Προβλημα κατα την διαγραφή");
                    return ok(result);
                }
            }
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την διαγραφή");
            return ok(result);
        }
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getDepartments(final Http.Request request) throws IOException {
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
                    HashMap<String, Object> returnList = new HashMap<String, Object>();
                    String jsonResult = "";
                    CompletableFuture<HashMap<String, Object>> getFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(
                                        entityManager -> {
                                            String orderCol = json.findPath("orderCol").asText();
                                            String descAsc = json.findPath("descAsc").asText();
                                            String department = json.findPath("department").asText();
                                            String creationDate = json.findPath("creationDate").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlOrgs = "select * from departments depa where 1=1 ";
                                            if (!department.equalsIgnoreCase("") && department != null) {
                                                sqlOrgs += " and depa.department like '%" + department + "%'";
                                            }
                                            if (!creationDate.equalsIgnoreCase("") && creationDate != null) {
                                                sqlOrgs += " and SUBSTRING( orgs.creation_date, 1, 10)  = '" + creationDate + "'";
                                            }
                                            List<DepartmentsEntity> posListAll
                                                    = (List<DepartmentsEntity>) entityManager.createNativeQuery(
                                                    sqlOrgs, DepartmentsEntity.class).getResultList();
                                            if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                                sqlOrgs += " order by " + orderCol + " " + descAsc;
                                            } else {
                                                sqlOrgs += " order by creation_date desc";
                                            }
                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sqlOrgs += " limit " + start + "," + limit;
                                            }
                                            System.out.println(sqlOrgs);
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                            List<DepartmentsEntity> posList
                                                    = (List<DepartmentsEntity>) entityManager.createNativeQuery(
                                                    sqlOrgs, DepartmentsEntity.class).getResultList();
                                            for (DepartmentsEntity j : posList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("department", j.getDepartment());
                                                sHmpam.put("name", j.getDepartment());
                                                sHmpam.put("status", j.getStatus());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                sHmpam.put("id", j.getId());
                                                serversList.add(sHmpam);
                                            }
                                            returnList_future.put("data", serversList);
                                            returnList_future.put("total", posListAll.size());
                                            returnList_future.put("status", "success");
                                            returnList_future.put("message", "success");
                                            return returnList_future;
                                        });
                            },
                            executionContext);
                    returnList = getFuture.get();
                    DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    ow.setDateFormat(myDateFormat);
                    try {
                        jsonResult = ow.writeValueAsString(returnList);
                    } catch (Exception e) {
                        e.printStackTrace();
                        result.put("status", "error");
                        result.put("message", "Πρόβλημα κατά την ανάγνωση των στοιχείων ");
                        return ok(result);
                    }
                    return ok(jsonResult);
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