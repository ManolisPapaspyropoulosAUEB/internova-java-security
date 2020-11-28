package controllers.coreData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.BillingsEntity;
import models.CustomersSuppliersEntity;
import models.OffersEntity;
import org.hibernate.exception.ConstraintViolationException;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
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
                                String sqlUnique = "select * from billings b where b.name=" + "'" + name + "'";
                                List<BillingsEntity> billingsEntityList = entityManager.createNativeQuery(sqlUnique, BillingsEntity.class).getResultList();
                                if (billingsEntityList.size() > 0) {
                                    add_result.put("status", "error");
                                    add_result.put("message", "Βρέθηκε εγγραφή με το ίδιο λεκτικό,προσπαθήστε ξανά");
                                    return add_result;
                                }
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
                                String sqlUnique = "select * from billings b where b.name=" + "'" + name + "'  and b.id!=" + id;
                                List<BillingsEntity> billingsEntityList = entityManager.createNativeQuery(sqlUnique, BillingsEntity.class).getResultList();
                                if (billingsEntityList.size() > 0) {
                                    add_result.put("status", "error");
                                    add_result.put("message", "Βρέθηκε εγγραφή με το ίδιο λεκτικό,προσπαθήστε ξανά");
                                    return add_result;
                                }
                                BillingsEntity billingsEntity = entityManager.find(BillingsEntity.class, id);
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
                                BillingsEntity billingsEntity = entityManager.find(BillingsEntity.class, id);

                                String sqlExistCs = "select * from customers_suppliers cs where cs.billing_id=" + id;
                                List<CustomersSuppliersEntity> suppliersEntityList = (List<CustomersSuppliersEntity>) entityManager.createNativeQuery(sqlExistCs, CustomersSuppliersEntity.class).getResultList();

                                String sqlExistOffers = "select * from offers o where o.billing_id=" + id;
                                List<OffersEntity> offersEntityList = (List<OffersEntity>) entityManager.createNativeQuery(sqlExistOffers, OffersEntity.class).getResultList();

                                if(suppliersEntityList.size()>0){
                                    add_result.put("status", "error");
                                    add_result.put("message", "Αποτυχία Διαγραφής.Ο συγκεκριμένος λογαριασμός είναι συνδεδεμένος με πελάτη/προμηθευτή");
                                }else if(offersEntityList.size()>0){
                                    add_result.put("status", "error");
                                    add_result.put("message", "Αποτυχία Διαγραφής.Ο συγκεκριμένος λογαριασμός είναι συνδεδεμένος με Προσφορά");
                                }else{
                                    entityManager.remove(billingsEntity);
                                    add_result.put("status", "success");
                                    add_result.put("message", "Η Διαγραφή πραγματοποίηθηκε με επιτυχία");
                                }

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
    public Result getBillings(final Http.Request request) throws IOException {
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
                                            String orderCol = json.findPath("orderCol").asText();
                                            String descAsc = json.findPath("descAsc").asText();
                                            String billingName = json.findPath("billingName").asText();
                                            String billingDescription = json.findPath("billingDescription").asText();
                                            String creationDate = json.findPath("creationDate").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlroles = "select * from billings b where 1=1 ";
                                            if (!billingName.equalsIgnoreCase("") && billingName != null) {
                                                sqlroles += " and b.name like '%" + billingName + "%'";
                                            }
                                            if (!billingDescription.equalsIgnoreCase("") && billingDescription != null) {
                                                sqlroles += " and b.description like '%" + billingDescription + "%'";
                                            }
                                            if (!creationDate.equalsIgnoreCase("") && creationDate != null) {
                                                sqlroles += " and SUBSTRING( b.creation_date, 1, 10)  = '" + creationDate + "'";
                                            }
                                            List<BillingsEntity> rolesListAll
                                                    = (List<BillingsEntity>) entityManager.createNativeQuery(
                                                    sqlroles, BillingsEntity.class).getResultList();

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
                                            List<BillingsEntity> orgsList
                                                    = (List<BillingsEntity>) entityManager.createNativeQuery(
                                                    sqlroles, BillingsEntity.class).getResultList();
                                            for (BillingsEntity j : orgsList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("billingId", j.getId());
                                                sHmpam.put("billingName", j.getName());
                                                sHmpam.put("name", j.getName());
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
