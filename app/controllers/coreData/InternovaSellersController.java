package controllers.coreData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import controllers.system.Application;
import models.CustomersSuppliersEntity;
import models.InternovaSellersEntity;
import models.OffersEntity;
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

public class InternovaSellersController extends Application {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;
    @Inject
    public InternovaSellersController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        super(jpaApi,  executionContext);
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addInternovaSeller(final Http.Request request) throws IOException {
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
                                String name = json.findPath("name").asText();
                                String description = json.findPath("description").asText();
                                String sqlUnique = "select * from internova_sellers b where b.name=" + "'" + name + "'";
                                List<InternovaSellersEntity> billingsEntityList = entityManager.createNativeQuery(sqlUnique, InternovaSellersEntity.class).getResultList();
                                if (billingsEntityList.size() > 0) {
                                    add_result.put("status", "error");
                                    add_result.put("message", "Βρέθηκε εγγραφή με το ίδιο λεκτικό,προσπαθήστε ξανά");
                                    return add_result;
                                }
                                InternovaSellersEntity internovaSellersEntity = new InternovaSellersEntity();
                                internovaSellersEntity.setCreationDate(new Date());
                                internovaSellersEntity.setName(name);
                                internovaSellersEntity.setDescription(description);
                                entityManager.persist(internovaSellersEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
                                add_result.put("DO_ID", internovaSellersEntity.getId());
                                add_result.put("system", "Πωλητές/Internova");
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
    public Result updateInternovaSeller(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                String name = json.findPath("name").asText();
                                String description = json.findPath("description").asText();
                                Long user_id = json.findPath("user_id").asLong();
                                Long id = json.findPath("id").asLong();

                                String sqlUnique = "select * from internova_sellers b where b.name=" + "'" + name + "' and b.id!="+id;
                                List<InternovaSellersEntity> billingsEntityList = entityManager.createNativeQuery(sqlUnique, InternovaSellersEntity.class).getResultList();
                                if (billingsEntityList.size() > 0) {
                                    add_result.put("status", "error");
                                    add_result.put("message", "Βρέθηκε εγγραφή με το ίδιο λεκτικό,προσπαθήστε ξανά");
                                    return add_result;
                                }
                                System.out.println(id);
                                InternovaSellersEntity internovaSellersEntity = entityManager.find(InternovaSellersEntity.class,id);
                                internovaSellersEntity.setUpdateDate(new Date());
                                internovaSellersEntity.setName(name);
                                internovaSellersEntity.setDescription(description);
                                entityManager.merge(internovaSellersEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η ενημέρωση πραγματοποίηθηκε με επιτυχία");
                                add_result.put("DO_ID", internovaSellersEntity.getId());
                                add_result.put("system", "Πωλητές/Internova");
                                add_result.put("user_id", user_id);
                                return add_result;
                            });
                        },
                        executionContext);
                result = (ObjectNode) updateFuture.get();
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
    public Result deleteInternovaSeller(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> deleteFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                Long id = json.findPath("id").asLong();
                                Long user_id = json.findPath("user_id").asLong();
                                String sqlExistCs = "select * from customers_suppliers cs where cs.internova_seller_id=" + id;
                                List<CustomersSuppliersEntity> suppliersEntityList = (List<CustomersSuppliersEntity>) entityManager.createNativeQuery(sqlExistCs, CustomersSuppliersEntity.class).getResultList();
                                String sqlExistOffers = "select * from offers o where o.seller_id=" + id;
                                List<OffersEntity> offersEntityList = (List<OffersEntity>) entityManager.createNativeQuery(sqlExistOffers, OffersEntity.class).getResultList();
                                if(suppliersEntityList.size()>0){
                                    add_result.put("status", "error");
                                    add_result.put("message", "Αποτυχία Διαγραφής.Ο συγκεκριμένος Πωλητής είναι συνδεδεμένος με πελάτη/προμηθευτή");
                                }else if(offersEntityList.size()>0){
                                    add_result.put("status", "error");
                                    add_result.put("message", "Αποτυχία Διαγραφής.Ο συγκεκριμένος Πωλητής είναι συνδεδεμένος με Προσφορά");
                                }else{
                                    InternovaSellersEntity internovaSellersEntity = entityManager.find(InternovaSellersEntity.class,id);
                                    entityManager.remove(internovaSellersEntity);
                                    add_result.put("status", "success");
                                    add_result.put("message", "Η Διαγραφή πραγματοποίηθηκε με επιτυχία");
                                    add_result.put("DO_ID", internovaSellersEntity.getId());
                                    add_result.put("system", "Πωλητές/Internova");
                                    add_result.put("user_id", user_id);
                                }
                                return add_result;
                            });
                        },
                        executionContext);
                result = (ObjectNode) deleteFuture.get();
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
    public Result getInternovaSellers(final Http.Request request) throws IOException {
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
                                            String sellerName = json.findPath("sellerName").asText();
                                            String sellerDescription = json.findPath("sellerDescription").asText();
                                            String creationDate = json.findPath("creationDate").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlroles = "select * from internova_sellers intsellers where 1=1 ";
                                            if(!sellerName.equalsIgnoreCase("") && sellerName!=null){
                                                sqlroles+=" and intsellers.name like '%"+sellerName+"%'";
                                            }
                                            if(!sellerDescription.equalsIgnoreCase("") && sellerDescription!=null){
                                                sqlroles+=" and intsellers.description like '%"+sellerDescription+"%'";
                                            }
                                            if(!creationDate.equalsIgnoreCase("") && creationDate!=null){
                                                sqlroles += " and SUBSTRING( intsellers.creation_date, 1, 10)  = '" + creationDate + "'";
                                            }
                                            List<InternovaSellersEntity> rolesListAll
                                                    = (List<InternovaSellersEntity>) entityManager.createNativeQuery(
                                                    sqlroles, InternovaSellersEntity.class).getResultList();
                                            if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                                sqlroles += " order by " + orderCol + " " + descAsc;
                                            } else {
                                                sqlroles += " order by creation_date desc";
                                            }

                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sqlroles += " limit " + start + "," + limit;
                                            }
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                            List<InternovaSellersEntity> orgsList
                                                    = (List<InternovaSellersEntity>) entityManager.createNativeQuery(
                                                    sqlroles, InternovaSellersEntity.class).getResultList();
                                            for (InternovaSellersEntity j : orgsList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("name", j.getName());
                                                sHmpam.put("sellerName", j.getName());
                                                sHmpam.put("sellerId", j.getId());
                                                sHmpam.put("description", j.getDescription());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                sHmpam.put("id", j.getId());
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
