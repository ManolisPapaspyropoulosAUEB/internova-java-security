package controllers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.AuditLogsEntity;
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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;
public class AuditLogsController {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;
    @Inject
    public AuditLogsController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }

    @SuppressWarnings({"Duplicates","unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addAuditLog(final Http.Request request) throws IOException {
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
                                    String system = json.findPath("system").asText();
                                    Long userId = json.findPath("userId").asLong();
                                    Long objectId = json.findPath("objectId").asLong();
                                    String message = json.findPath("message").asText();
                                    AuditLogsEntity log = new AuditLogsEntity();
                                    log.setSystem(system);
                                    log.setUserId(userId);
                                    log.setObjectId(objectId);
                                    log.setMessage(message);
                                    entityManager.persist(log);
                                    add_result.put("status", "success");
                                    add_result.put("message", "Επιτυχης καταχωρηση");
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


    @SuppressWarnings({"Duplicates","unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateAuditLog(final Http.Request request) throws IOException {
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
                                    String system = json.findPath("system").asText();
                                    Long id = json.findPath("system").asLong();
                                    Long userId = json.findPath("userId").asLong();
                                    Long objectId = json.findPath("objectId").asLong();
                                    String message = json.findPath("message").asText();
                                    AuditLogsEntity log = entityManager.find(AuditLogsEntity.class,id);
                                    log.setSystem(system);
                                    log.setUserId(userId);
                                    log.setObjectId(objectId);
                                    log.setMessage(message);
                                    entityManager.merge(log);
                                    result_update.put("status", "success");
                                    result_update.put("message", "Επιτυχης καταχωρηση");
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


    @SuppressWarnings({"Duplicates","unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteLog(final Http.Request request) throws IOException {
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
                                    AuditLogsEntity log = entityManager.find(AuditLogsEntity.class,id);
                                    if(log!=null){
                                        entityManager.remove(log);
                                        result_delete.put("status", "success");
                                        result_delete.put("message", "Επιτυχης διαγραφή");
                                    }else{
                                        result_delete.put("status", "error");
                                        result_delete.put("message", "Δεν βρέθηκε σχετικός οργανισμός");
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

    @SuppressWarnings({"Duplicates","unchecked"})
    public Result getAuditLogs(final Http.Request request) throws IOException {  // san parametro pernei to org key
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
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                            List<AuditLogsEntity> orgsList
                                                    = (List<AuditLogsEntity>) entityManager.createNativeQuery(
                                                    "select * from audit_logs logs ", AuditLogsEntity.class).getResultList();
                                            for (AuditLogsEntity j : orgsList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("system", j.getSystem());
                                                sHmpam.put("userId", j.getUserId());
                                                sHmpam.put("objectId", j.getObjectId());
                                                sHmpam.put("creationDate", j.getCreationDate());
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
                    DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
