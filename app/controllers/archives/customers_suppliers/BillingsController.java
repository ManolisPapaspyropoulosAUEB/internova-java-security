package controllers.archives.customers_suppliers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.BillingsEntity;
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
import java.util.concurrent.ExecutionException;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;
public class BillingsController {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;
    @Inject
    public BillingsController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }

    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addBillingEntry(final Http.Request request) throws IOException {
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
                                    BillingsEntity billingsEntity = new BillingsEntity();
                                    billingsEntity.setCreationDate(new Date());
                                    billingsEntity.setName(name);
                                    billingsEntity.setDescription(description);
                                    entityManager.persist(billingsEntity);
                                    add_result.put("status", "success");
                                    add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
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
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateBillingEntry(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                Long id = json.findPath("id").asLong();
                                String name = json.findPath("name").asText();
                                String description = json.findPath("description").asText();
                                BillingsEntity billingsEntity = entityManager.find(BillingsEntity.class,id);
                                billingsEntity.setUpdateDate(new Date());
                                billingsEntity.setName(name);
                                billingsEntity.setDescription(description);
                                entityManager.persist(billingsEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η ενημέρωση πραγματοποίηθηκε με επιτυχία");
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
    }



    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteBilling(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                Long id = json.findPath("id").asLong();
                                BillingsEntity billingsEntity = entityManager.find(BillingsEntity.class,id);
                                entityManager.remove(billingsEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η ενημέρωση πραγματοποίηθηκε με επιτυχία");
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
    }




    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getBillings(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
        ObjectNode result = Json.newObject();
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
                                            String bSql = "select * from billings where 1=1 ";
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> filalist = new ArrayList<HashMap<String, Object>>();
                                            List<BillingsEntity> billingsEntityList
                                                    = (List<BillingsEntity>) entityManager.createNativeQuery(
                                                    bSql, BillingsEntity.class).getResultList();
                                            for (BillingsEntity j : billingsEntityList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("name", j.getName());
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("description", j.getDescription());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                filalist.add(sHmpam);
                                            }
                                            returnList_future.put("data", filalist);
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

    }





}
