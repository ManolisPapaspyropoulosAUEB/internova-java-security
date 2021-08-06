package controllers.coreData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.MailerService;
import controllers.execution_context.DatabaseExecutionContext;
import controllers.system.Application;
import models.ExtraCostsEntity;
import models.ExtraCostsOffersEntity;
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

public class ExtraCostsController extends Application {
    private JPAApi jpaApi;
    private MailerService ms;
    private DatabaseExecutionContext executionContext;

    @Inject
    public ExtraCostsController(MailerService ms, JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        super(jpaApi, executionContext);
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
        this.ms = ms;
    }

    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addExtraCost(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                String title = json.findPath("title").asText();
                                String description = json.findPath("description").asText();
                                Double cost = json.findPath("cost").asDouble();
                                String sqlUnique = "select * from extra_costs b where b.title=" + "'" + title + "'";
                                List<ExtraCostsEntity> extraCostsEntityList = entityManager.createNativeQuery(sqlUnique, ExtraCostsEntity.class).getResultList();
                                if (extraCostsEntityList.size() > 0) {
                                    add_result.put("status", "error");
                                    add_result.put("message", "Βρέθηκε εγγραφή με το ίδιο λεκτικό,προσπαθήστε ξανά");
                                    return add_result;
                                }
                                ExtraCostsEntity extraCostsEntity = new ExtraCostsEntity();
                                extraCostsEntity.setCreationDate(new Date());
                                extraCostsEntity.setTitle(title);
                                extraCostsEntity.setDescription(description);
                                extraCostsEntity.setCost(cost);
                                entityManager.persist(extraCostsEntity);
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
    public Result updateExtraCost(final Http.Request request) throws IOException {
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
                                String title = json.findPath("title").asText();
                                String description = json.findPath("description").asText();
                                Double cost = json.findPath("cost").asDouble();
                                String sqlUnique = "select * from extra_costs b where b.title=" + "'" + title + "'  and b.id!=" + id;
                                List<ExtraCostsEntity> extraCostsEntityList = entityManager.createNativeQuery(sqlUnique, ExtraCostsEntity.class).getResultList();
                                if (extraCostsEntityList.size() > 0) {
                                    add_result.put("status", "error");
                                    add_result.put("message", "Βρέθηκε εγγραφή με το ίδιο λεκτικό,προσπαθήστε ξανά");
                                    return add_result;
                                }
                                ExtraCostsEntity extraCostsEntity = entityManager.find(ExtraCostsEntity.class, id);
                                extraCostsEntity.setCreationDate(new Date());
                                extraCostsEntity.setTitle(title);
                                extraCostsEntity.setDescription(description);
                                extraCostsEntity.setCost(cost);
                                entityManager.merge(extraCostsEntity);
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
    public Result deleteExtraCost(final Http.Request request) throws IOException {
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
                                String sqlExist = "select * from extra_costs_offers excoff where excoff.extra_cost_id="+id;
                                List<ExtraCostsOffersEntity> extraCostsOffersEntityList =
                                        entityManager.createNativeQuery(sqlExist,ExtraCostsEntity.class).getResultList();
                                if(extraCostsOffersEntityList.size()>0){
                                    add_result.put("status", "error");
                                    add_result.put("message", "Αδυναμία διαγραφής,βρέθηκαν συνδεδεμένες εγγραφές");
                                    return add_result;
                                }
                                ExtraCostsEntity extraCostsEntity = entityManager.find(ExtraCostsEntity.class, id);
                                entityManager.remove(extraCostsEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η Διαγραφή πραγματοποίηθηκε με επιτυχία");
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
                result.put("message", "Προβλημα κατα την Διαγραφή");
                return ok(result);
            }
        }
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getExtraCosts(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
        try {
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
                                return jpaApi.withTransaction(entityManager -> {
                                    String orderCol = json.findPath("orderCol").asText();
                                    String descAsc = json.findPath("descAsc").asText();
                                    String title = json.findPath("title").asText();
                                    String description = json.findPath("description").asText();
                                    String cost = json.findPath("cost").asText();
                                    String creationDate = json.findPath("creationDate").asText();
                                    String start = json.findPath("start").asText();
                                    String limit = json.findPath("limit").asText();
                                    String sqlExtraCosts = "select * from extra_costs b where 1=1 ";
                                    if (!title.equalsIgnoreCase("") && title != null) {
                                        sqlExtraCosts += " and b.title like '%" + title + "%'";
                                    }
                                    if (!cost.equalsIgnoreCase("") && cost != null) {
                                        sqlExtraCosts += " and b.cost like '%" + cost + "%'";
                                    }
                                    if (!description.equalsIgnoreCase("") && description != null) {
                                        sqlExtraCosts += " and b.description like '%" + description + "%'";
                                    }
                                    if (!creationDate.equalsIgnoreCase("") && creationDate != null) {
                                        sqlExtraCosts += " and SUBSTRING( b.creation_date, 1, 10)  = '" + creationDate + "'";
                                    }
                                    List<ExtraCostsEntity> rolesListAll
                                            = (List<ExtraCostsEntity>) entityManager.createNativeQuery(
                                            sqlExtraCosts, ExtraCostsEntity.class).getResultList();

                                    if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                        sqlExtraCosts += " order by " + orderCol + " " + descAsc;
                                    } else {
                                        sqlExtraCosts += " order by creation_date desc";
                                    }
                                    if (!start.equalsIgnoreCase("") && start != null) {
                                        sqlExtraCosts += " limit " + start + "," + limit;
                                    }
                                    HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                    List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                    List<ExtraCostsEntity> orgsList
                                            = (List<ExtraCostsEntity>) entityManager.createNativeQuery(
                                            sqlExtraCosts, ExtraCostsEntity.class).getResultList();
                                    for (ExtraCostsEntity j : orgsList) {
                                        HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                        sHmpam.put("id", j.getId());
                                        sHmpam.put("title", j.getTitle());
                                        sHmpam.put("cost", j.getCost());
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
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την καταχωρηση");
            return ok(result);
        }
    }

}
