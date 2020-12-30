package controllers.procedures;

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
import java.util.concurrent.ExecutionException;

public class OrdersController extends Application {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public OrdersController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        super(jpaApi, executionContext);
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result createOrder(final Http.Request request) throws IOException {
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
                                Long offerScheduleId = json.findPath("offerScheduleId").asLong();
                                Long offerScheduleToken = json.findPath("offerScheduleToken").asLong();
                                Long offerId = json.findPath("offerId").asLong();

                                //

                                OrdersEntity ordersEntity = new OrdersEntity();
                                OffersSchedulesEntity offersSchedulesEntity = entityManager.find(OffersSchedulesEntity.class, offerScheduleId);
                                ordersEntity.setCreationDate(new Date());
                                ordersEntity.setCustomerId(entityManager.find(OffersEntity.class, offerId).getCustomerId());
                                ordersEntity.setFromCity(offersSchedulesEntity.getFromCity());
                                ordersEntity.setFromCountry(offersSchedulesEntity.getFromCountry());
                                if (offersSchedulesEntity.getFromPostalCode() != null) {
                                    ordersEntity.setFromPostalCode(offersSchedulesEntity.getFromPostalCode());
                                }
                                ordersEntity.setOfferId(offerId);
                                ordersEntity.setOfferScheduleToken(offersSchedulesEntity.getToken());
                                ordersEntity.setOfferScheduleId(offerScheduleId);
                                ordersEntity.setStatus("ΝΕΑ");
                                ordersEntity.setToCity(offersSchedulesEntity.getToCity());
                                ordersEntity.setToCountry(offersSchedulesEntity.getToCountry());

                                if (offersSchedulesEntity.getToPostalCode() != null) {
                                    ordersEntity.setToPostalCode(offersSchedulesEntity.getToPostalCode());
                                }
                                ordersEntity.setType(offersSchedulesEntity.getType());
                                entityManager.persist(ordersEntity);
                                entityManager.merge(offersSchedulesEntity);
                                String offerScheduleWayPoints = " select * from offer_schedule_between_waypoints wayp where wayp.offer_id=" + offerId + " " +
                                        " and wayp.offer_schedule_id=" + offerScheduleId;
                                List<OfferScheduleBetweenWaypointsEntity> offerWaypointsEntityList =
                                        entityManager.createNativeQuery(offerScheduleWayPoints, OfferScheduleBetweenWaypointsEntity.class).getResultList();
                                for (OfferScheduleBetweenWaypointsEntity offerWayp : offerWaypointsEntityList) {
                                    OrderWaypointsEntity orderWaypointsEntity = new OrderWaypointsEntity();
                                    orderWaypointsEntity.setAddress(offerWayp.getAddress());
                                    orderWaypointsEntity.setCity(offerWayp.getCity());
                                    orderWaypointsEntity.setCountry(offerWayp.getCountry());
                                    if (offerWayp.getPostalCode() != null) {
                                        orderWaypointsEntity.setPostalCode(offerWayp.getPostalCode());
                                    }
                                    orderWaypointsEntity.setOrderId(ordersEntity.getId());
                                    orderWaypointsEntity.setCreationDate(new Date());
                                    entityManager.persist(orderWaypointsEntity);
                                }

                                //pros8hkh kai tou telikou proorismou apo thn prosfora
                                OrderWaypointsEntity orderWaypointsEntity = new OrderWaypointsEntity();
                                orderWaypointsEntity.setCity(offersSchedulesEntity.getToCity());
                                orderWaypointsEntity.setCountry(offersSchedulesEntity.getToCountry());
                                orderWaypointsEntity.setPostalCode(offersSchedulesEntity.getToPostalCode());
                                orderWaypointsEntity.setCreationDate(new Date());
                                orderWaypointsEntity.setOrderId(ordersEntity.getId());
                                entityManager.persist(orderWaypointsEntity);
                                //end pros8hkh kai tou telikou proorismou apo thn prosfora

                                String offerSchedulePackage = "select * from schedule_package_offer spo where spo.offer_id=" + offerId + " and spo.offer_schedule_id=" + offerScheduleId;
                                List<SchedulePackageOfferEntity> schedulePackageOfferEntityList =
                                        entityManager.createNativeQuery(offerSchedulePackage, SchedulePackageOfferEntity.class).getResultList();
                                for (SchedulePackageOfferEntity spoe : schedulePackageOfferEntityList) {
                                    OrderPackagesEntity orderPackagesEntity = new OrderPackagesEntity();
                                    orderPackagesEntity.setFromUnit(spoe.getFromUnit());
                                    orderPackagesEntity.setMeasureUnitId(spoe.getMeasureUnitId());
                                    orderPackagesEntity.setOrderId(ordersEntity.getId());
                                    orderPackagesEntity.setToUnit(spoe.getToUnit());
                                    orderPackagesEntity.setUnitPrice(spoe.getUnitPrice());
                                    orderPackagesEntity.setCreationDate(new Date());
                                    entityManager.persist(orderPackagesEntity);
                                }
                                add_result.put("status", "success");
                                add_result.put("id", ordersEntity.getId());
                                add_result.put("message", "Η δημιουργία νέας παραγγελίας καταχωρήθηκε με επιτυχία πραγματοποίηθηκε με επιτυχία");
                                add_result.put("DO_ID", ordersEntity.getId());
                                add_result.put("system", "ΠΑΡΑΓΓΕΛΙΕΣ");
                                add_result.put("user_id", user_id);
                                return add_result;
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

    //offersSchedulesEntity
    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getOrders(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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
                                            String orderCol = json.findPath("orderCol").asText();
                                            String descAsc = json.findPath("descAsc").asText();
                                            String creationDate = json.findPath("creationDate").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlordrs = "select * from orders ord where 1=1 ";
                                            List<OrdersEntity> ordersEntityListAll = entityManager.createNativeQuery(
                                                    sqlordrs, OrdersEntity.class).getResultList();

                                            if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                                sqlordrs += " order by " + orderCol + " " + descAsc;
                                            } else {
                                                sqlordrs += " order by creation_date desc";
                                            }
                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sqlordrs += " limit " + start + "," + limit;
                                            }
                                            if (!creationDate.equalsIgnoreCase("") && creationDate != null) {
                                                sqlordrs += " and SUBSTRING( role.creation_date, 1, 10)  = '" + creationDate + "'";
                                            }
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                            List<OrdersEntity> ordersEntityListfinal
                                                    = (List<OrdersEntity>) entityManager.createNativeQuery(
                                                    sqlordrs, OrdersEntity.class).getResultList();
                                            for (OrdersEntity j : ordersEntityListfinal) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("customerId", j.getCustomerId());
                                                sHmpam.put("customer", entityManager.find(CustomersSuppliersEntity.class, j.getCustomerId()));
                                                sHmpam.put("fromAddress", j.getFromAddress());
                                                sHmpam.put("fromCity", j.getFromCity());
                                                sHmpam.put("fromCountry", j.getFromCountry());
                                                sHmpam.put("fromLattitude", j.getFromLattitude());
                                                sHmpam.put("fromLongtitude", j.getFromLongtitude());
                                                sHmpam.put("fromPostalCode", j.getFromPostalCode());
                                                sHmpam.put("offerId", j.getOfferId());
                                                sHmpam.put("offerScheduleId", j.getOfferScheduleId());
                                                sHmpam.put("status", j.getStatus());
                                                sHmpam.put("toAddress", j.getToAddress());
                                                sHmpam.put("toCity", j.getToCity());
                                                sHmpam.put("toCountry", j.getToCountry());
                                                sHmpam.put("toLattitude", j.getToLattitude());
                                                sHmpam.put("toLongtitude", j.getToLongtitude());
                                                sHmpam.put("postalCode", j.getToPostalCode());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("orderId", j.getId());
                                                serversList.add(sHmpam);
                                            }
                                            returnList_future.put("data", serversList);
                                            returnList_future.put("total", ordersEntityListAll.size());
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
