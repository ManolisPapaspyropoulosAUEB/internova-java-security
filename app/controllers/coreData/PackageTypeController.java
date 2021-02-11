package controllers.coreData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.system.Application;
import models.BillingsEntity;
import models.OrdersSelectionsByPointEntity;
import models.PackageTypeEntity;
import play.db.jpa.JPAApi;
import controllers.execution_context.DatabaseExecutionContext;
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

public class PackageTypeController extends Application {

    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public PackageTypeController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        super(jpaApi, executionContext);
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }




    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addPackageTypeEntry(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                String type = json.findPath("type").asText();
                                String description = json.findPath("description").asText();
                                String user_id = json.findPath("user_id").asText();

                                PackageTypeEntity packageTypeEntity = new PackageTypeEntity();
                                packageTypeEntity.setDescription(description);
                                packageTypeEntity.setType(type);
                                packageTypeEntity.setCreationDate(new Date());
                                entityManager.persist(packageTypeEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
                                add_result.put("DO_ID", packageTypeEntity.getId());
                                add_result.put("system", "Package/type");
                                add_result.put("user_id", user_id);
                                return add_result;
                            });
                        },
                        executionContext);
                result = (ObjectNode) addFuture.get();
                return ok(result,request);
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
    public Result updatePackageTypeEntry(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                String type = json.findPath("type").asText();
                                String description = json.findPath("description").asText();
                                String user_id = json.findPath("user_id").asText();
                                Integer id = json.findPath("id").asInt();

                                PackageTypeEntity packageTypeEntity = entityManager.find(PackageTypeEntity.class,id);
                                packageTypeEntity.setDescription(description);
                                packageTypeEntity.setType(type);
                                entityManager.merge(packageTypeEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η ενημέρωση πραγματοποίηθηκε με επιτυχία");
                                add_result.put("DO_ID", packageTypeEntity.getId());
                                add_result.put("system", "Package/type");
                                add_result.put("user_id", user_id);
                                return add_result;
                            });
                        },
                        executionContext);
                result = (ObjectNode) addFuture.get();
                return ok(result,request);
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
    public Result deletePackageTypeEntry(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                String user_id = json.findPath("user_id").asText();
                                Integer id = json.findPath("id").asInt();

                                PackageTypeEntity packageTypeEntity = entityManager.find(PackageTypeEntity.class,id);
                                String sql ="select * from orders_selections_by_point where package_type_id="+id;
                                List<OrdersSelectionsByPointEntity> ordersSelectionsByPointEntityList = entityManager.createNativeQuery(sql,OrdersSelectionsByPointEntity.class).getResultList();

                                if(ordersSelectionsByPointEntityList.size()>0){
                                    add_result.put("status", "error");
                                    add_result.put("message", "Διαγραφή δεν μπόρεσε να ολοκληρωθεί καθώς βρέθηκε συνδεδεμένη εγγραφή με παραγγελίες");
                                    return add_result;
                                }
                                entityManager.remove(packageTypeEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η ενημέρωση πραγματοποίηθηκε με επιτυχία");
                                add_result.put("DO_ID", packageTypeEntity.getId());
                                add_result.put("system", "Package/type");
                                add_result.put("user_id", user_id);
                                return add_result;
                            });
                        },
                        executionContext);
                result = (ObjectNode) addFuture.get();
                return ok(result,request);
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
    public Result getPackagesTypesEntries(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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

                                            //roleDescSearchInput
                                            String orderCol = json.findPath("orderCol").asText();
                                            String descAsc = json.findPath("descAsc").asText();
                                            String type = json.findPath("type").asText();
                                            String description = json.findPath("description").asText();
                                            String creationDate = json.findPath("creationDate").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlroles = "select * from package_type b where 1=1 ";
                                            if (!type.equalsIgnoreCase("") && type != null) {
                                                sqlroles += " and b.type like '%" + type + "%'";
                                            }
                                            if (!description.equalsIgnoreCase("") && description != null) {
                                                sqlroles += " and b.description like '%" + description + "%'";
                                            }
                                            if (!creationDate.equalsIgnoreCase("") && creationDate != null) {
                                                sqlroles += " and SUBSTRING( b.creation_date, 1, 10)  = '" + creationDate + "'";
                                            }
                                            List<PackageTypeEntity> rolesListAll
                                                    = (List<PackageTypeEntity>) entityManager.createNativeQuery(
                                                    sqlroles, PackageTypeEntity.class).getResultList();

                                            if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                                sqlroles += " order by " + orderCol + " " + descAsc;
                                            } else {
                                                sqlroles += " order by creation_date desc";
                                            }

                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sqlroles += " limit " + start + "," + limit;
                                            }
                                            System.out.println(sqlroles);
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                            List<PackageTypeEntity> orgsList
                                                    = (List<PackageTypeEntity>) entityManager.createNativeQuery(
                                                    sqlroles, PackageTypeEntity.class).getResultList();
                                            for (PackageTypeEntity j : orgsList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("type", j.getType());
                                                sHmpam.put("description", j.getDescription());
                                                sHmpam.put("creationDate", j.getCreationDate());
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

    }










}