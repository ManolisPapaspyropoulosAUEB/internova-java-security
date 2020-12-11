package controllers.coreData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import controllers.system.Application;
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

public class ManagersController extends Application {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public ManagersController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        super(jpaApi, executionContext);
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addManager(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                System.out.println(json);
                                ObjectNode result_add = Json.newObject();
                                String firstname = json.findPath("firstname").asText();
                                String lastname = json.findPath("lastname").asText();
                                Long user_id = json.findPath("user_id").asLong();
                                String position = json.findPath("position").asText();
                                String telephone = json.findPath("telephone").asText();
                                String gender = json.findPath("gender").asText();
                                String email = json.findPath("email").asText();
                                /**
                                 * metablhtes susthmatos (ergostasia,apothikes,prosfores,pelates kai promhtheytes)
                                 */
                                Long selectedManagerId = json.findPath("selectedManagerId").asLong();
                                String system = json.findPath("system").asText();
                                Long system_id = json.findPath("system_id").asLong();
                                ManagersEntity manager = new ManagersEntity();
                                manager.setCreationDate(new Date());
                                manager.setFirstName(firstname);
                                manager.setLastName(lastname);
                                manager.setPosition(position);
                                manager.setTelephone(telephone);
                                manager.setEmail(email);
                                manager.setGender(gender);
                                if (!system.isEmpty()) { //erxete apo kapoio apo ta ufistamena systhmata
                                    ManagersSystemEntity ms = new ManagersSystemEntity();
                                    if (selectedManagerId != 0) { //exei dialexei apo thn lista
                                        ms.setCreationDate(new Date());
                                        ms.setManagerId(selectedManagerId);
                                        ms.setSystem(system);
                                        ms.setSystemId(system_id);
                                        entityManager.persist(ms);
                                    } else { //erxete apo systhma kai einai kainourios ara prosthiki kai meta desimo me ayto to systhma
                                        entityManager.persist(manager);
                                        ms.setCreationDate(new Date());
                                        ms.setManagerId(manager.getId());
                                        ms.setSystem(system);
                                        ms.setSystemId(system_id);
                                        entityManager.persist(ms);
                                    }
                                } else { //erxete apo ta parametrika/ypeuthynoi
                                    entityManager.persist(manager);
                                }
                                result_add.put("status", "success");
                                result_add.put("message", "Η καταχώρηση ολοκληρώθηκε με επιτυχία!");
                                result_add.put("DO_ID", manager.getId());
                                result_add.put("system", "Υπεύθυνοι");
                                result_add.put("user_id", user_id);
                                return result_add;
                            });
                        },
                        executionContext);
                result = (ObjectNode) addFuture.get();
                return ok(result, request);

            } catch (Exception e) {
                ObjectNode result = Json.newObject();
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Προβλημα κατα την καταχωρηση");
                return ok(result);
            }
        }
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateManager(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode result_add = Json.newObject();
                                String firstname = json.findPath("firstname").asText();
                                String lastname = json.findPath("lastname").asText();
                                String position = json.findPath("position").asText();
                                String telephone = json.findPath("telephone").asText();
                                String email = json.findPath("email").asText();
                                String user_id = json.findPath("user_id").asText();
                                String system = json.findPath("system").asText();
                                Long systemId = json.findPath("systemId").asLong();
                                String gender = json.findPath("gender").asText();
                                Long id = json.findPath("id").asLong();
                                ManagersEntity manager = entityManager.find(ManagersEntity.class, id);
                                manager.setUpdateDate(new Date());
                                manager.setFirstName(firstname);
                                manager.setLastName(lastname);
                                manager.setPosition(position);
                                manager.setTelephone(telephone);
                                manager.setEmail(email);
                                manager.setGender(gender);
                                manager.setSystem(system);
                                manager.setSystemId(systemId);
                                entityManager.merge(manager);
                                result_add.put("status", "success");
                                result_add.put("message", "Η ενημέρωση ολοκληρώθηκε με επιτυχία!");
                                result_add.put("DO_ID", manager.getId());
                                result_add.put("system", "Υπεύθυνοι");
                                result_add.put("user_id", user_id);
                                return result_add;
                            });
                        },
                        executionContext);
                result = (ObjectNode) addFuture.get();
                return ok(result, request);

            } catch (Exception e) {
                ObjectNode result = Json.newObject();
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Προβλημα κατα την καταχωρηση");
                return ok(result);
            }
        }
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteManager(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> deleteFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode result_delete = Json.newObject();
                                System.out.println(json);
                                String system = json.findPath("system").asText();
                                String user_id = json.findPath("user_id").asText();
                                Long system_id = json.findPath("system_id").asLong();
                                ((ObjectNode) json).remove("systems");
                                Long id = json.findPath("id").asLong();

                                ManagersEntity manager = entityManager.find(ManagersEntity.class, id);
                                if (system_id == 0) {
                                    String sqlChild = "select * from managers_system ms " +
                                            " where ms.manager_id=" + id;
                                    System.out.println(id);
                                    List<ManagersSystemEntity> managersSystemEntityList = (List<ManagersSystemEntity>)
                                            entityManager.createNativeQuery(sqlChild, ManagersSystemEntity.class).getResultList();
                                    if (managersSystemEntityList.size() > 0) {
                                        result_delete.put("status", "error");
                                        result_delete.put("message", "Βρέθηκαν συνδεδεμένες εγγραφές");
                                        return result_delete;
                                    } else {
                                        entityManager.remove(manager);
                                    }
                                } else {
                                    String sqldeleteManagerFromSystem = "select *  from managers_system ms  " +
                                            "where ms.system=" + "'" + system + "'" + " and ms.system_id=" + system_id;
                                    List<ManagersSystemEntity> managersSystemEntityList
                                            = entityManager.createNativeQuery(sqldeleteManagerFromSystem, ManagersSystemEntity.class).getResultList();
                                    entityManager.remove(managersSystemEntityList.get(0));
                                }
                                result_delete.put("status", "success");
                                result_delete.put("message", "Η Διαγραφή ολοκληρώθηκε με επιτυχία!");
                                result_delete.put("DO_ID", manager.getId());
                                result_delete.put("system", "Υπεύθυνοι");
                                result_delete.put("user_id", user_id);
                                return result_delete;
                            });
                        },
                        executionContext);
                result = (ObjectNode) deleteFuture.get();
                return ok(result, request);
            }
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την καταχωρηση");
            return ok(result);
        }
    }


    //getManagersExpect


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getManagersExpect(final Http.Request request) throws IOException {
        ObjectNode result = Json.newObject();
        try {
            JsonNode json = request.body().asJson();
            if (json == null || json.isEmpty()) {
                result.put("status", "error");
                result.put("message", "Δεν εχετε αποστειλει εγκυρα δεδομενα.");
                return ok(result);
            } else {
                System.out.println(json);
                System.out.println(json.isEmpty());
                ObjectMapper ow = new ObjectMapper();
                HashMap<String, Object> returnList = new HashMap<String, Object>();
                String jsonResult = "";
                CompletableFuture<HashMap<String, Object>> getFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(
                                    entityManager -> {
                                        String system = json.findPath("system").asText();
                                        String system_id = json.findPath("system_id").asText();
                                        String sqlManagers = "select * " +
                                                "from managers m " +
                                                "where m.id not in " +
                                                "(select manager_id " +
                                                "from managers_system ms " +
                                                "where ms.system_id= " + system_id + " " +
                                                "and ms.system='" + system + "')";
                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                        List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                        List<ManagersEntity> posList
                                                = (List<ManagersEntity>) entityManager.createNativeQuery(
                                                sqlManagers, ManagersEntity.class).getResultList();
                                        for (ManagersEntity j : posList) {
                                            HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                            sHmpam.put("id", j.getId());
                                            sHmpam.put("firstname", j.getFirstName());
                                            sHmpam.put("lastname", j.getLastName());
                                            sHmpam.put("fullName", j.getFirstName() + " " + j.getLastName());
                                            sHmpam.put("email", j.getEmail());
                                            sHmpam.put("position", j.getPosition());
                                            sHmpam.put("systemId", j.getSystemId());
                                            sHmpam.put("system_id", j.getSystemId());
                                            sHmpam.put("telephone", j.getTelephone());
                                            sHmpam.put("gender", j.getGender());
                                            sHmpam.put("creationDate", j.getCreationDate());
                                            sHmpam.put("updateDate", j.getUpdateDate());
                                            serversList.add(sHmpam);
                                        }
                                        returnList_future.put("data", serversList);
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
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Πρόβλημα κατά την ανάγνωση των στοιχείων");
            return ok(result);
        }
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getManagers(final Http.Request request) throws IOException {
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
                                    String system = json.findPath("system").asText();
                                    String systemSearch = json.findPath("systemSearch").asText();
                                    Long system_id = json.findPath("system_id").asLong();
                                    String id = json.findPath("id").asText();
                                    String firstName = json.findPath("firstName").asText();
                                    String gender = json.findPath("gender").asText();
                                    String email = json.findPath("email").asText();
                                    String lastName = json.findPath("lastName").asText();
                                    String position = json.findPath("position").asText();
                                    String telephone = json.findPath("telephone").asText();
                                    String start = json.findPath("start").asText();
                                    String limit = json.findPath("limit").asText();
                                    String sqlManagers = "select * from managers manager where 1=1 ";
                                    if (!firstName.equalsIgnoreCase("") && firstName != null) {
                                        sqlManagers += " and manager.first_name like '%" + firstName + "%'";
                                    }
                                    if (!email.equalsIgnoreCase("") && email != null) {
                                        sqlManagers += " and manager.email like '%" + email + "%'";
                                    }
                                    if (!lastName.equalsIgnoreCase("") && lastName != null) {
                                        sqlManagers += " and manager.last_name like '%" + lastName + "%'";
                                    }
                                    if (!position.equalsIgnoreCase("") && position != null) {
                                        sqlManagers += " and manager.position like '%" + position + "%'";
                                    }
                                    if (!telephone.equalsIgnoreCase("") && telephone != null) {
                                        sqlManagers += " and manager.telephone like '%" + telephone + "%'";
                                    }
                                    if (!gender.equalsIgnoreCase("") && gender != null) {
                                        sqlManagers += " and manager.gender like '%" + gender + "%'";
                                    }
                                    if (!systemSearch.equalsIgnoreCase("") && systemSearch != null) {
                                        sqlManagers += " and manager.system like '%" + systemSearch + "%'";
                                    }
                                    if (!system.equalsIgnoreCase("") && system != null) {
                                        sqlManagers += " and manager.id in " +
                                                " (select manager_id from managers_system ms where ms.system ='" + system + "')";
                                    }
                                    if (system_id != null && system_id != 0) {
                                        sqlManagers += " and manager.id in " +
                                                " (select manager_id from managers_system ms where ms.system_id =" + system_id + ")";
                                    }
                                    if (!id.equalsIgnoreCase("") && id != null) {
                                        sqlManagers += " and manager.id=" + id;
                                    }
                                    List<ManagersEntity> posListAll
                                            = (List<ManagersEntity>) entityManager.createNativeQuery(
                                            sqlManagers, ManagersEntity.class).getResultList();
                                    if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                        sqlManagers += " order by " + orderCol + " " + descAsc;
                                    } else {
                                        sqlManagers += " order by creation_date desc ";
                                    }

                                    if (!start.equalsIgnoreCase("") && start != null) {
                                        sqlManagers += " limit " + start + "," + limit;
                                    }
                                    HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                    List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                    List<ManagersEntity> posList
                                            = (List<ManagersEntity>) entityManager.createNativeQuery(
                                            sqlManagers, ManagersEntity.class).getResultList();
                                    System.out.println(sqlManagers);

                                    for (ManagersEntity j : posList) {
                                        HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                        sHmpam.put("id", j.getId());
                                        sHmpam.put("firstname", j.getFirstName());
                                        sHmpam.put("lastname", j.getLastName());
                                        sHmpam.put("fullName", j.getFirstName() + " " + j.getLastName());
                                        sHmpam.put("email", j.getEmail());
                                        sHmpam.put("position", j.getPosition());
                                        sHmpam.put("telephone", j.getTelephone());
                                        sHmpam.put("gender", j.getGender());
                                        sHmpam.put("creationDate", j.getCreationDate());
                                        sHmpam.put("updateDate", j.getUpdateDate());
                                        String sqlmanagersSystems =
                                                " select * from managers_system ms where ms.manager_id=" + j.getId();
                                        List<ManagersSystemEntity> managersSystemEntityList =
                                                entityManager.createNativeQuery(sqlmanagersSystems, ManagersSystemEntity.class).getResultList();
                                        List<HashMap<String, Object>> systemList = new ArrayList<HashMap<String, Object>>();
                                        for (ManagersSystemEntity ms : managersSystemEntityList) {
                                            HashMap<String, Object> systemMap = new HashMap<String, Object>();
                                            systemMap.put("id", ms.getId());
                                            systemMap.put("managerId", ms.getManagerId());
                                            systemMap.put("system", ms.getSystem());
                                            systemMap.put("systemId", ms.getSystemId());
                                            systemMap.put("creationDate", ms.getCreationDate());
                                            systemMap.put("brandName", ms.getCreationDate());
                                            systemMap.put("updateDate", ms.getUpdateDate());
                                            if (ms.getSystem().equalsIgnoreCase("Αποθήκη")) {
                                                systemMap.put("brandName", entityManager.find(WarehousesEntity.class,
                                                        ms.getSystemId()).getBrandName());
                                            } else if (ms.getSystem().equalsIgnoreCase("Εργοστάσιο")) {
                                                systemMap.put("brandName", entityManager.find(FactoriesEntity.class,
                                                        ms.getSystemId()).getBrandName());
                                            } else if (ms.getSystem().equalsIgnoreCase("Πελάτες-Προμηθευτές")) {
                                                systemMap.put("brandName", entityManager.find(CustomersSuppliersEntity.class,
                                                        ms.getSystemId()).getBrandName());
                                            } else if (ms.getSystem().equalsIgnoreCase("Προσφορά")) {
                                                CustomersSuppliersEntity customersSuppliersEntity =
                                                        entityManager.find(CustomersSuppliersEntity.class,
                                                                entityManager.find(OffersEntity.class,
                                                                        ms.getSystemId()).getCustomerId());
                                                systemMap.put("brandName", customersSuppliersEntity.getBrandName());
                                            }
                                            systemList.add(systemMap);
                                        }
                                        sHmpam.put("systems", systemList);
                                        sHmpam.put("systemsTotals", systemList.size());
                                        sHmpam.put("systemsTotalsLabel", "Ο συγκεκριμένος υπεύθυνος είναι συνδεδεμένος με " + systemList.size() + " συστήματα ");
                                        serversList.add(sHmpam);
                                    }
                                    returnList_future.put("data", serversList);
                                    returnList_future.put("total", posListAll.size());
                                    returnList_future.put("status", "success");
                                    returnList_future.put("message", "success");
                                    return returnList_future;
                                });
                    }, executionContext);
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
