package controllers.coreData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.MailerService;
import controllers.execution_context.DatabaseExecutionContext;
import controllers.system.Application;
import models.ExtraCostsEntity;
import models.ExtraCostsOffersEntity;
import models.OrderSchedulesEntity;
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

public class ExtraCostsOffersController extends Application {
    private JPAApi jpaApi;
    private MailerService ms;
    private DatabaseExecutionContext executionContext;


    @Inject
    public ExtraCostsOffersController(MailerService ms, JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        super(jpaApi, executionContext);
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
        this.ms = ms;
    }




    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addExtraCostOfferSchedule(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                Long extraCostId = json.findPath("extraCostId").asLong();
                                Long offerId = json.findPath("offerId").asLong();
                                Double cost = json.findPath("cost").asDouble();
                                Long offerScheduleId = json.findPath("offerScheduleId").asLong();
                                String sqlDuplicates = "select * from extra_costs_offers where " +
                                        " offer_id="+offerId+" and offer_schedule_id="+offerScheduleId+" and extra_cost_id="+extraCostId;
                                List<ExtraCostsOffersEntity> extraCostsOffersEntityList =
                                        entityManager.createNativeQuery(sqlDuplicates,ExtraCostsOffersEntity.class).getResultList();
                                if(extraCostsOffersEntityList.size()>0){
                                    add_result.put("status", "error");
                                    add_result.put("message", "Αδύναμια καταχώρησης,βρέθηκαν διπλότυπα");
                                    return add_result;
                                }
                                ExtraCostsOffersEntity  extraCostsOffersEntity =  new ExtraCostsOffersEntity();
                                extraCostsOffersEntity.setCreationDate(new Date());
                                extraCostsOffersEntity.setExtraCostId(extraCostId);
                                extraCostsOffersEntity.setOfferId(offerId);
                                extraCostsOffersEntity.setOfferScheduleId(offerScheduleId);
                                extraCostsOffersEntity.setCost(cost);
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
    public Result getExtraCostsOffersSchedule
            (final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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
                                    String offerScheduleId = json.findPath("offerScheduleId").asText();
                                    String sqlExtraCosts = "select * from extra_costs_offers" +
                                            " b where offer_schedule_id="+offerScheduleId;
                                    HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                    List<HashMap<String, Object>> extraCostsOffersList = new ArrayList<HashMap<String, Object>>();
                                    List<ExtraCostsOffersEntity> orgsList
                                            = (List<ExtraCostsOffersEntity>) entityManager.createNativeQuery(
                                            sqlExtraCosts, ExtraCostsOffersEntity.class).getResultList();

                                    String sqlOrd = "select * from order_schedules ords where ords.offer_schedule_id= '" + offerScheduleId + "'";
                                    List<OrderSchedulesEntity> orderSchedulesEntityList =
                                            entityManager.createNativeQuery(sqlOrd, OrderSchedulesEntity.class).getResultList();

                                    for (ExtraCostsOffersEntity j : orgsList) {
                                        HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                        sHmpam.put("id", j.getId());
                                        sHmpam.put("cost", j.getCost());
                                        sHmpam.put("creationDate", j.getCreationDate());
                                        sHmpam.put("extraCostId", j.getExtraCostId());
                                        sHmpam.put("offerId", j.getOfferId());
                                        sHmpam.put("offerScheduleId", j.getOfferScheduleId());
                                        ExtraCostsEntity extraCostsEntity =
                                                entityManager.find(ExtraCostsEntity.class,j.getExtraCostId());
                                        sHmpam.put("title", extraCostsEntity.getTitle());
                                        sHmpam.put("description", extraCostsEntity.getDescription());
                                        sHmpam.put("extraCost", extraCostsEntity.getCost());
                                        sHmpam.put("editableIndicator",false);
                                        if(orderSchedulesEntityList.size()>0){
                                            sHmpam.put("disableUpdate",true);
                                        }else{
                                            sHmpam.put("disableUpdate",false);
                                        }
                                        sHmpam.put("countOrders",orderSchedulesEntityList.size());
                                        extraCostsOffersList.add(sHmpam);
                                    }
                                    returnList_future.put("data", extraCostsOffersList);
                                    returnList_future.put("total", orgsList.size());
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
