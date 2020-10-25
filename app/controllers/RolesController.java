package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.RolesEntity;
import models.UsersEntity;
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

public class RolesController {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public RolesController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addRole(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode add_result = Json.newObject();
                                    String name = json.findPath("name").asText();
                                    String description = json.findPath("description").asText();
                                    Integer status = json.findPath("status").asInt();
                                    RolesEntity role = new RolesEntity();
                                    role.setName(name);
                                    role.setDescription(description);
                                    role.setStatus(status);
                                    role.setCreationDate(new Date());
                                    entityManager.persist(role);
                                    add_result.put("status", "success");
                                    add_result.put("message", "Η ενημέρωση ολοκληρώθηκε με επιτυχία!");
                                    return add_result;
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
    public Result updateRole(final Http.Request request) throws IOException {
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
                                    Long id = json.findPath("id").asLong();
                                    String name = json.findPath("role_name").asText();
                                    String description = json.findPath("descriptionRole").asText();
                                    Integer status = json.findPath("status").asInt();
                                    RolesEntity role = entityManager.find(RolesEntity.class, id);
                                    role.setName(name);
                                    role.setDescription(description);
                                    role.setStatus(status);
                                    role.setUpdateTime(new Date());
                                    entityManager.merge(role);
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
    public Result deleteRole(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> deleteFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode result_delete = Json.newObject();
                                    Long id = json.findPath("id").asLong();

                                    String checkIfUsedSql = "select * from users where users.role_id=" + id;
                                    List<UsersEntity> usersList = (List<UsersEntity>) entityManager.createNativeQuery(checkIfUsedSql, UsersEntity.class).getResultList();
                                    if (usersList.size()>0) {
                                        result_delete.put("status", "error");
                                        result_delete.put("message", "Βρεθηκαν συνδεδεμένες εγγραφές,η διαγραφή δεν μπορεσε να ολοκληρωθεί.");
                                        return result_delete;
                                    }
                                    RolesEntity o = entityManager.find(RolesEntity.class, id);
                                    if (o != null) {
                                        entityManager.remove(o);
                                        result_delete.put("status", "success");
                                        result_delete.put("message", "Η διαγραφή ολοκληρώθηκε με επιτυχία!");
                                    } else {
                                        result_delete.put("status", "error");
                                        result_delete.put("message", "Δεν βρέθηκε σχετικός ρόλος");
                                    }

                                    return result_delete;
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
    public Result getRoles(final Http.Request request) throws IOException {  // san parametro pernei to org key
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

                                            //roleDescSearchInput
                                            String roleName = json.findPath("roleName").asText();
                                            String roleDescription = json.findPath("roleDescription").asText();
                                            String creationDate = json.findPath("creationDate").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlroles = "select * from roles role where 1=1 ";
                                            if(!roleName.equalsIgnoreCase("") && roleName!=null){
                                                sqlroles+=" and role.name like '%"+roleName+"%'";
                                            }
                                            if(!roleDescription.equalsIgnoreCase("") && roleDescription!=null){
                                                sqlroles+=" and role.description like '%"+roleDescription+"%'";
                                            }
                                            if(!creationDate.equalsIgnoreCase("") && creationDate!=null){
                                                sqlroles += " and SUBSTRING( role.creation_date, 1, 10)  = '" + creationDate + "'";
                                            }
                                            List<RolesEntity> rolesListAll
                                                    = (List<RolesEntity>) entityManager.createNativeQuery(
                                                    sqlroles, RolesEntity.class).getResultList();
                                            sqlroles+=" order by creation_date desc";
                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sqlroles += " limit " + start + "," + limit;
                                            }
                                            System.out.println(sqlroles);
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                            List<RolesEntity> orgsList
                                                    = (List<RolesEntity>) entityManager.createNativeQuery(
                                                    sqlroles, RolesEntity.class).getResultList();
                                            for (RolesEntity j : orgsList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("name", j.getName());
                                                sHmpam.put("description", j.getDescription());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("updateDate", j.getUpdateTime());
                                                sHmpam.put("id", j.getRoleId());
                                                serversList.add(sHmpam);
                                            }
                                            returnList_future.put("data", serversList);
                                            returnList_future.put("total", rolesListAll.size());
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
