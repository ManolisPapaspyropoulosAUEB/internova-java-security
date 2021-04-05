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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class OrdersLoadingController extends Application {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public OrdersLoadingController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        super(jpaApi, executionContext);
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;

    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result saveOrderLoading(final Http.Request request) throws IOException {
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

                                System.out.println(json);

                                OrdersLoadingEntity ordersLoadingEntity = new OrdersLoadingEntity();
                                ordersLoadingEntity.setCreationDate(new Date());
                                ordersLoadingEntity.setStatus("ΣΤΑΔΙΟ 1");
                                entityManager.persist(ordersLoadingEntity);

                                Iterator doneListIt = json.findPath("doneList").iterator();
                                int counter=0;
                                while (doneListIt.hasNext()) {
                                    JsonNode orderNode = (JsonNode) doneListIt.next();
                                    if(counter==0){

                                        String sqlOrderMasterSchedule = "select * from order_schedules os where os.order_id="+orderNode.findPath("orderId").asLong() +
                                                " and os.primary_schedule=1";
                                        List<OrderSchedulesEntity> orderSchedulesEntityList = entityManager.createNativeQuery(sqlOrderMasterSchedule,OrderSchedulesEntity.class).getResultList();
                                        ordersLoadingEntity.setFromCity(orderSchedulesEntityList.get(0).getFromCity());
                                        ordersLoadingEntity.setFromCountry(orderSchedulesEntityList.get(0).getFromCountry());
                                        ordersLoadingEntity.setFromPostalCode(orderSchedulesEntityList.get(0).getFromPostalCode());
                                       // ordersLoadingEntity.setFromCity(ordersEntity.get);
                                        entityManager.merge(ordersLoadingEntity);
                                    }else if (counter== json.findPath("doneList").size()-1){
                                        String sqlOrderMasterSchedule = "select * from order_schedules os where os.order_id="+orderNode.findPath("orderId").asLong() +
                                                " and os.primary_schedule=1";
                                        List<OrderSchedulesEntity> orderSchedulesEntityList = entityManager.createNativeQuery(sqlOrderMasterSchedule,OrderSchedulesEntity.class).getResultList();
                                        ordersLoadingEntity.setToCity(orderSchedulesEntityList.get(0).getToCity());
                                        ordersLoadingEntity.setToCountry(orderSchedulesEntityList.get(0).getToCountry());
                                        ordersLoadingEntity.setToPostalCode(orderSchedulesEntityList.get(0).getToPostalCode());
                                        entityManager.merge(ordersLoadingEntity);
                                    }
                                    OrdersLoadingOrdersSelectionsEntity ordersLoadingOrdersSelectionsEntity = new OrdersLoadingOrdersSelectionsEntity();
                                    ordersLoadingOrdersSelectionsEntity.setCreationDate(new Date());
                                    ordersLoadingOrdersSelectionsEntity.setOrderId(orderNode.findPath("orderId").asLong());
                                    ordersLoadingOrdersSelectionsEntity.setOrderLoadingId(ordersLoadingEntity.getId());
                                    entityManager.persist(ordersLoadingOrdersSelectionsEntity);
                                    counter++;
                                }

                                add_result.put("status", "success");
                                add_result.put("ordersLoadingId", ordersLoadingEntity.getId());
                                add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
                                //   add_result.put("DO_ID", billingsEntity.getId());
                                add_result.put("system", "Φόρτωση");
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


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getAvailablesOrders(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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
                                            String availableOrdersSearch = json.findPath("availableOrdersSearch").asText();
                                            String sqlAvailablesOrders = "select * from orders ord where 1=1 \n";
                                            if (availableOrdersSearch != null && !availableOrdersSearch.equalsIgnoreCase("")) {
                                                sqlAvailablesOrders += " and ord.id like '%" + availableOrdersSearch + "%' " +
                                                        " or ord.customer_id in (select id from customers_suppliers cs where cs.brand_name like '%" + availableOrdersSearch + "%'" +
                                                        " or ord.id in  ( select os.order_id from order_schedules os where os.primary_schedule=1 " +
                                                        " and os.from_city like '%" + availableOrdersSearch + "%' or " +
                                                        "os.from_country  like '%" + availableOrdersSearch + "%' " +
                                                        " or os.to_city like '%" + availableOrdersSearch + "%'" +
                                                        " or os.to_country like '%" + availableOrdersSearch + "%' )     )";
                                            }
                                            sqlAvailablesOrders += " order by \n" +
                                                    "(" +
                                                    " select from_country \n" +
                                                    " from order_schedules ords  \n" +
                                                    " where ords.order_id=ord.id and ords.primary_schedule=1 \n" +
                                                    ")" +
                                                    "," +
                                                    "(" +
                                                    " select from_city \n" +
                                                    " from order_schedules ords \n" +
                                                    " where ords.order_id=ord.id and ords.primary_schedule=1 \n" +
                                                    ")" +
                                                    "," +
                                                    "(" +
                                                    " select to_country \n" +
                                                    " from order_schedules ords \n" +
                                                    " where ords.order_id=ord.id and ords.primary_schedule=1 \n" +
                                                    ") \n" +
                                                    "," +
                                                    "(" +
                                                    " select to_city \n" +
                                                    " from order_schedules ords \n" +
                                                    " where ords.order_id=ord.id and ords.primary_schedule=1 \n" +
                                                    ")";
                                            System.out.println(sqlAvailablesOrders);
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                            List<OrdersEntity> ordersEntityList
                                                    = (List<OrdersEntity>) entityManager.createNativeQuery(
                                                    sqlAvailablesOrders, OrdersEntity.class).getResultList();
                                            for (OrdersEntity j : ordersEntityList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                //dromologioParaggelias
                                                List<HashMap<String, Object>> dromologioParaggelias = new ArrayList<HashMap<String, Object>>();
                                                sHmpam.put("orderId", j.getId());
                                                sHmpam.put("customerId", j.getCustomerId());
                                                sHmpam.put("customer", entityManager.find(CustomersSuppliersEntity.class, j.getCustomerId()));
                                                sHmpam.put("status", j.getStatus());
                                                sHmpam.put("type", j.getType());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                sHmpam.put("dromologioParaggelias", dromologioParaggelias);
                                                String sqlOrdersSchedules =
                                                        "select * from order_schedules ord_s " +
                                                                " where ord_s.order_id=" + j.getId() +
                                                                " and ord_s.primary_schedule=1";
                                                List<OrderSchedulesEntity> orderSchedulesEntityList =
                                                        entityManager.createNativeQuery(sqlOrdersSchedules,
                                                                OrderSchedulesEntity.class).getResultList();

                                                if (orderSchedulesEntityList.size() > 0) {
                                                    sHmpam.put("mainSchedule", orderSchedulesEntityList.get(0).getFromCountry() + " " +
                                                            orderSchedulesEntityList.get(0).getFromCity() + "  /  " +
                                                            orderSchedulesEntityList.get(0).getToCountry() + " " +
                                                            orderSchedulesEntityList.get(0).getToCity());
                                                    if (orderSchedulesEntityList.get(0).getFactoryId() != null) {
                                                        sHmpam.put("factory", entityManager.find(FactoriesEntity.class, orderSchedulesEntityList.get(0).getFactoryId()));
                                                        sHmpam.put("factoryId", orderSchedulesEntityList.get(0).getFactoryId());
                                                    } else {
                                                        sHmpam.put("factory", "-");
                                                        sHmpam.put("factoryId", "-");
                                                    }

                                                } else {
                                                    sHmpam.put("factory", "-");
                                                    sHmpam.put("factoryId", "-");
                                                }

                                                serversList.add(sHmpam);
                                            }
                                            returnList_future.put("data", serversList);
                                            returnList_future.put("total", ordersEntityList.size());
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


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getDromologioByOrder(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
        ObjectNode result = Json.newObject();
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                if (json == null || json.findPath("orderId") == null || json.findPath("orderId").asText().equalsIgnoreCase("")) {
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
                                            String orderId = json.findPath("orderId").asText();
                                            String sqlOrdLoads = "select * from order_schedules ords where ords.order_id=" + orderId + " and ords.primary_schedule=1 ";
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> finalList = new ArrayList<HashMap<String, Object>>();
                                            List<OrderSchedulesEntity> orderSchedulesEntityList
                                                    = (List<OrderSchedulesEntity>) entityManager.createNativeQuery(
                                                    sqlOrdLoads, OrderSchedulesEntity.class).getResultList();
                                            HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                            if (orderSchedulesEntityList.get(0).getFactoryId() != null) {
                                                sHmpam.put("longtitude", entityManager.find(FactoriesEntity.class, orderSchedulesEntityList.get(0).getFactoryId()).getLongtitude());
                                                sHmpam.put("lattitude", entityManager.find(FactoriesEntity.class, orderSchedulesEntityList.get(0).getFactoryId()).getLattitude());
                                                sHmpam.put("city", entityManager.find(FactoriesEntity.class, orderSchedulesEntityList.get(0).getFactoryId()).getCity());
                                                sHmpam.put("brandName", entityManager.find(FactoriesEntity.class, orderSchedulesEntityList.get(0).getFactoryId()).getBrandName());
                                                sHmpam.put("country", entityManager.find(FactoriesEntity.class, orderSchedulesEntityList.get(0).getFactoryId()).getCountry());
                                                sHmpam.put("address", entityManager.find(FactoriesEntity.class, orderSchedulesEntityList.get(0).getFactoryId()).getAddress());
                                                sHmpam.put("postalCode", entityManager.find(FactoriesEntity.class, orderSchedulesEntityList.get(0).getFactoryId()).getPostalCode());
                                                sHmpam.put("status", "success");
                                                sHmpam.put("message", "success");
                                            } else {
                                                sHmpam.put("status", "error");
                                                sHmpam.put("message", "Δεν έχει οριστεί εργοστάσιο ή αποθήκη ");
                                                sHmpam.put("city", orderSchedulesEntityList.get(0).getFromCity());
                                                sHmpam.put("country", orderSchedulesEntityList.get(0).getFromCountry());
                                                sHmpam.put("postalCode", orderSchedulesEntityList.get(0).getFromPostalCode());
                                                sHmpam.put("brandName", "Δεν έχει οριστεί");
                                                sHmpam.put("address", "Δεν έχει οριστεί");
                                            }
                                            sHmpam.put("type", "Αφετηρία");
                                            String sqlPackages = "select * from orders_selections_by_point osbp where osbp.order_schedule_id=" + orderSchedulesEntityList.get(0).getId() + " and osbp.order_waypoint_id is null";
                                            List<OrdersSelectionsByPointEntity> ordersSelectionsByPointEntityList = entityManager.createNativeQuery(sqlPackages, OrdersSelectionsByPointEntity.class).getResultList();
                                            List<HashMap<String, Object>> packagesFortwshs = new ArrayList<HashMap<String, Object>>();
                                            List<HashMap<String, Object>> packagesEkfortwshs = new ArrayList<HashMap<String, Object>>();
                                            List<HashMap<String, Object>> allPackages = new ArrayList<HashMap<String, Object>>();
                                            for (OrdersSelectionsByPointEntity osafet : ordersSelectionsByPointEntityList) {
                                                HashMap<String, Object> osafetmap = new HashMap<String, Object>();
                                                osafetmap.put("id", osafet.getId());
                                                osafetmap.put("orderId", osafet.getOrderId());
                                                osafetmap.put("title", osafet.getTitle());
                                                osafetmap.put("orderScheduleId", osafet.getOrderScheduleId());
                                                osafetmap.put("orderWaypointId", osafet.getOrderWaypointId());
                                                osafetmap.put("type", osafet.getType());
                                                osafetmap.put("quantity", osafet.getQuantity());
                                                osafetmap.put("typePackage", osafet.getTypePackage());
                                                osafetmap.put("stackingType", osafet.getStackingType());
                                                osafetmap.put("ldm", osafet.getLdm());
                                                if (osafet.getUnitPrice() != null) {
                                                    osafetmap.put("unitPrice", osafet.getUnitPrice());
                                                } else {
                                                    osafetmap.put("unitPrice", 0);
                                                }
                                                if (osafet.getType().equalsIgnoreCase("Φόρτωση")) {
                                                    packagesFortwshs.add(osafetmap);
                                                } else {
                                                    packagesEkfortwshs.add(osafetmap);
                                                }
                                                allPackages.add(osafetmap);
                                            }
                                            sHmpam.put("packagesFortwshs", packagesFortwshs);
                                            sHmpam.put("packagesEkfortwshs", packagesEkfortwshs);
                                            sHmpam.put("allPackages", allPackages);
                                            finalList.add(sHmpam);
                                            String sqlWaypoints = "select * from order_waypoints ow where ow.order_id=" + orderId + " and ow.order_schedule_id=" + orderSchedulesEntityList.get(0).getId();
                                            List<OrderWaypointsEntity> orderWaypointsEntityList = entityManager.createNativeQuery(sqlWaypoints, OrderWaypointsEntity.class).getResultList();
                                            for (OrderWaypointsEntity owayp : orderWaypointsEntityList) {
                                                HashMap<String, Object> owaypMap = new HashMap<String, Object>();
                                                if (owayp.getFactoryId() != null) {
                                                    owaypMap.put("longtitude", entityManager.find(FactoriesEntity.class, owayp.getFactoryId()).getLongtitude());
                                                    owaypMap.put("lattitude", entityManager.find(FactoriesEntity.class, owayp.getFactoryId()).getLattitude());
                                                    owaypMap.put("brandName", entityManager.find(FactoriesEntity.class, owayp.getFactoryId()).getBrandName());
                                                    owaypMap.put("city", entityManager.find(FactoriesEntity.class, owayp.getFactoryId()).getCity());
                                                    owaypMap.put("country", entityManager.find(FactoriesEntity.class, owayp.getFactoryId()).getCountry());
                                                    owaypMap.put("address", entityManager.find(FactoriesEntity.class, owayp.getFactoryId()).getAddress());
                                                    owaypMap.put("postalCode", entityManager.find(FactoriesEntity.class, owayp.getFactoryId()).getPostalCode());
                                                    owaypMap.put("status", "success");
                                                    owaypMap.put("message", "success");
                                                } else {
                                                    owaypMap.put("status", "error");
                                                    owaypMap.put("message", "Δεν έχει οριστεί εργοστάσιο ή αποθήκη ");
                                                    owaypMap.put("city", owayp.getCity());
                                                    owaypMap.put("country", owayp.getCountry());
                                                    owaypMap.put("postalCode", owayp.getPostalCode());
                                                    owaypMap.put("brandName", "Δεν έχει οριστεί");
                                                    owaypMap.put("address", "Δεν έχει οριστεί");
                                                }
                                                if (owayp.getOfferScheduleBetweenWaypointId() != null) {
                                                    owaypMap.put("type", "Ενδιάμεσο Σημείο");
                                                } else {
                                                    if (owayp.getNewWaypoint() != null && owayp.getNewWaypoint() == 1) {
                                                        owaypMap.put("type", "Ενδιάμεσο Σημείο");
                                                    } else {
                                                        owaypMap.put("type", "Τελικός Προορισμός");
                                                    }
                                                }
                                                sqlPackages = "select * from orders_selections_by_point osbp where osbp.order_schedule_id=" + owayp.getOrderScheduleId() + " and osbp.order_waypoint_id =" + owayp.getId();
                                                ordersSelectionsByPointEntityList = entityManager.createNativeQuery(sqlPackages, OrdersSelectionsByPointEntity.class).getResultList();
                                                packagesFortwshs = new ArrayList<HashMap<String, Object>>();
                                                packagesEkfortwshs = new ArrayList<HashMap<String, Object>>();
                                                allPackages = new ArrayList<HashMap<String, Object>>();
                                                for (OrdersSelectionsByPointEntity osafet : ordersSelectionsByPointEntityList) {
                                                    HashMap<String, Object> osafetmap = new HashMap<String, Object>();
                                                    osafetmap.put("id", osafet.getId());
                                                    osafetmap.put("orderId", osafet.getOrderId());
                                                    osafetmap.put("title", osafet.getTitle());
                                                    osafetmap.put("orderScheduleId", osafet.getOrderScheduleId());
                                                    osafetmap.put("orderWaypointId", osafet.getOrderWaypointId());
                                                    osafetmap.put("type", osafet.getType());
                                                    osafetmap.put("quantity", osafet.getQuantity());
                                                    osafetmap.put("typePackage", osafet.getTypePackage());
                                                    osafetmap.put("stackingType", osafet.getStackingType());
                                                    osafetmap.put("ldm", osafet.getLdm());
                                                    if (osafet.getUnitPrice() != null) {
                                                        osafetmap.put("unitPrice", osafet.getUnitPrice());
                                                    } else {
                                                        osafetmap.put("unitPrice", 0);
                                                    }
                                                    if (osafet.getType().equalsIgnoreCase("Φόρτωση")) {
                                                        packagesFortwshs.add(osafetmap);
                                                    } else {
                                                        packagesEkfortwshs.add(osafetmap);
                                                    }
                                                    allPackages.add(osafetmap);
                                                }
                                                owaypMap.put("packagesFortwshs", packagesFortwshs);
                                                owaypMap.put("packagesEkfortwshs", packagesEkfortwshs);
                                                owaypMap.put("allPackages", allPackages);
                                                finalList.add(owaypMap);
                                            }
                                            String sql = "select * from order_schedules ords where ords.order_id=" + orderId + " and ords.primary_schedule=0 ";
                                            orderSchedulesEntityList = entityManager.createNativeQuery(sql, OrderSchedulesEntity.class).getResultList();
                                            for (OrderSchedulesEntity os : orderSchedulesEntityList) {
                                                HashMap<String, Object> osMap = new HashMap<String, Object>();
                                                if (os.getFactoryId() != null) {
                                                    osMap.put("longtitude", entityManager.find(FactoriesEntity.class, os.getFactoryId()).getLongtitude());
                                                    osMap.put("lattitude", entityManager.find(FactoriesEntity.class, os.getFactoryId()).getLattitude());
                                                    osMap.put("brandName", entityManager.find(FactoriesEntity.class, os.getFactoryId()).getBrandName());
                                                    osMap.put("city", entityManager.find(FactoriesEntity.class, os.getFactoryId()).getCity());
                                                    osMap.put("country", entityManager.find(FactoriesEntity.class, os.getFactoryId()).getCountry());
                                                    osMap.put("address", entityManager.find(FactoriesEntity.class, os.getFactoryId()).getAddress());
                                                    osMap.put("postalCode", entityManager.find(FactoriesEntity.class, os.getFactoryId()).getPostalCode());
                                                    osMap.put("status", "success");
                                                    osMap.put("message", "success");

                                                } else {
                                                    osMap.put("status", "error");
                                                    osMap.put("message", "Δεν έχει οριστεί εργοστάσιο ή αποθήκη ");
                                                    osMap.put("city", os.getFromCity());
                                                    osMap.put("country", os.getFromCountry());
                                                    osMap.put("postalCode", os.getFromPostalCode());
                                                    osMap.put("brandName", "Δεν έχει οριστεί");
                                                    osMap.put("address", "Δεν έχει οριστεί");
                                                }
                                                sqlPackages = "select * from orders_selections_by_point osbp where osbp.order_schedule_id=" + os.getId() + " and osbp.order_waypoint_id is null";
                                                ordersSelectionsByPointEntityList = entityManager.createNativeQuery(sqlPackages, OrdersSelectionsByPointEntity.class).getResultList();
                                                packagesFortwshs = new ArrayList<HashMap<String, Object>>();
                                                packagesEkfortwshs = new ArrayList<HashMap<String, Object>>();
                                                allPackages = new ArrayList<HashMap<String, Object>>();
                                                for (OrdersSelectionsByPointEntity osafet : ordersSelectionsByPointEntityList) {
                                                    HashMap<String, Object> osafetmap = new HashMap<String, Object>();
                                                    osafetmap.put("id", osafet.getId());
                                                    osafetmap.put("orderId", osafet.getOrderId());
                                                    osafetmap.put("title", osafet.getTitle());
                                                    osafetmap.put("orderScheduleId", osafet.getOrderScheduleId());
                                                    osafetmap.put("orderWaypointId", osafet.getOrderWaypointId());
                                                    osafetmap.put("type", osafet.getType());
                                                    osafetmap.put("quantity", osafet.getQuantity());
                                                    osafetmap.put("typePackage", osafet.getTypePackage());
                                                    osafetmap.put("stackingType", osafet.getStackingType());
                                                    osafetmap.put("ldm", osafet.getLdm());
                                                    if (osafet.getUnitPrice() != null) {
                                                        osafetmap.put("unitPrice", osafet.getUnitPrice());
                                                    } else {
                                                        osafetmap.put("unitPrice", 0);
                                                    }
                                                    if (osafet.getType().equalsIgnoreCase("Φόρτωση")) {
                                                        packagesFortwshs.add(osafetmap);
                                                    } else {
                                                        packagesEkfortwshs.add(osafetmap);
                                                    }
                                                    allPackages.add(osafetmap);
                                                }
                                                osMap.put("packagesFortwshs", packagesFortwshs);
                                                osMap.put("packagesEkfortwshs", packagesEkfortwshs);
                                                osMap.put("allPackages", allPackages);
                                                osMap.put("type", "Αφετηρία");
                                                finalList.add(osMap);
                                                String sqlWp1 = "select * from order_waypoints ow where ow.order_id=" + orderId + " and ow.order_schedule_id=" + os.getId();
                                                List<OrderWaypointsEntity> orderWaypointsEntityList1 = entityManager.createNativeQuery(sqlWp1, OrderWaypointsEntity.class).getResultList();
                                                for (OrderWaypointsEntity owpe : orderWaypointsEntityList1) {
                                                    HashMap<String, Object> owpeMap = new HashMap<String, Object>();
                                                    if (owpe.getFactoryId() != null) {
                                                        owpeMap.put("longtitude", entityManager.find(FactoriesEntity.class, owpe.getFactoryId()).getLongtitude());
                                                        owpeMap.put("lattitude", entityManager.find(FactoriesEntity.class, owpe.getFactoryId()).getLattitude());
                                                        owpeMap.put("brandName", entityManager.find(FactoriesEntity.class, owpe.getFactoryId()).getBrandName());
                                                        owpeMap.put("city", entityManager.find(FactoriesEntity.class, owpe.getFactoryId()).getCity());
                                                        owpeMap.put("country", entityManager.find(FactoriesEntity.class, owpe.getFactoryId()).getCountry());
                                                        owpeMap.put("address", entityManager.find(FactoriesEntity.class, owpe.getFactoryId()).getAddress());
                                                        owpeMap.put("postalCode", entityManager.find(FactoriesEntity.class, owpe.getFactoryId()).getPostalCode());
                                                        owpeMap.put("status", "success");
                                                        owpeMap.put("message", "success");
                                                    } else {
                                                        owpeMap.put("status", "error");
                                                        owpeMap.put("message", "Δεν έχει οριστεί εργοστάσιο ή αποθήκη ");
                                                        owpeMap.put("city", owpe.getCity());
                                                        owpeMap.put("country", owpe.getCountry());
                                                        owpeMap.put("postalCode", owpe.getPostalCode());
                                                        owpeMap.put("brandName", "Δεν έχει οριστεί");
                                                        owpeMap.put("address", "Δεν έχει οριστεί");
                                                    }
                                                    if (owpe.getOfferScheduleBetweenWaypointId() != null) {
                                                        owpeMap.put("type", "Ενδιάμεσο Σημείο");
                                                    } else {
                                                        if (owpe.getNewWaypoint() != null && owpe.getNewWaypoint() == 1) {
                                                            owpeMap.put("type", "Ενδιάμεσο Σημείο");
                                                        } else {
                                                            owpeMap.put("type", "Τελικός Προορισμός");
                                                        }
                                                    }
                                                    sqlPackages = "select * from orders_selections_by_point osbp where osbp.order_schedule_id=" + owpe.getOrderScheduleId() + " and osbp.order_waypoint_id =" + owpe.getId();
                                                    ordersSelectionsByPointEntityList = entityManager.createNativeQuery(sqlPackages, OrdersSelectionsByPointEntity.class).getResultList();
                                                    packagesFortwshs = new ArrayList<HashMap<String, Object>>();
                                                    packagesEkfortwshs = new ArrayList<HashMap<String, Object>>();
                                                    allPackages = new ArrayList<HashMap<String, Object>>();
                                                    for (OrdersSelectionsByPointEntity osafet : ordersSelectionsByPointEntityList) {
                                                        HashMap<String, Object> osafetmap = new HashMap<String, Object>();
                                                        osafetmap.put("id", osafet.getId());
                                                        osafetmap.put("orderId", osafet.getOrderId());
                                                        osafetmap.put("title", osafet.getTitle());
                                                        osafetmap.put("orderScheduleId", osafet.getOrderScheduleId());
                                                        osafetmap.put("orderWaypointId", osafet.getOrderWaypointId());
                                                        osafetmap.put("type", osafet.getType());
                                                        osafetmap.put("quantity", osafet.getQuantity());
                                                        osafetmap.put("typePackage", osafet.getTypePackage());
                                                        osafetmap.put("stackingType", osafet.getStackingType());
                                                        osafetmap.put("ldm", osafet.getLdm());
                                                        if (osafet.getUnitPrice() != null) {
                                                            osafetmap.put("unitPrice", osafet.getUnitPrice());
                                                        } else {
                                                            osafetmap.put("unitPrice", 0);
                                                        }
                                                        if (osafet.getType().equalsIgnoreCase("Φόρτωση")) {
                                                            packagesFortwshs.add(osafetmap);
                                                        } else {
                                                            packagesEkfortwshs.add(osafetmap);
                                                        }
                                                        allPackages.add(osafetmap);
                                                    }
                                                    owpeMap.put("packagesFortwshs", packagesFortwshs);
                                                    owpeMap.put("packagesEkfortwshs", packagesEkfortwshs);
                                                    owpeMap.put("allPackages", allPackages);
                                                    finalList.add(owpeMap);
                                                }
                                            }
                                            returnList_future.put("data", finalList);
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


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getPackagesByDromologio(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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

                                        String sqlOrdLoads = "select * from orders_loading ord_load where 1=1 ";

                                        System.out.println(sqlOrdLoads);
                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                        List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                        List<OrdersLoadingEntity> ordersLoadingList
                                                = (List<OrdersLoadingEntity>) entityManager.createNativeQuery(
                                                sqlOrdLoads, OrdersLoadingEntity.class).getResultList();
                                        for (OrdersLoadingEntity j : ordersLoadingList) {
                                            HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                            sHmpam.put("id", j.getId());
                                            sHmpam.put("fromCountry", j.getFromCountry());
                                            sHmpam.put("fromCity", j.getFromCity());
                                            sHmpam.put("fromAddress", j.getFromAddress());
                                            sHmpam.put("fromPostalCode", j.getFromPostalCode());
                                            sHmpam.put("toCountry", j.getFromCountry());
                                            sHmpam.put("toCity", j.getFromCity());
                                            sHmpam.put("toAddress", j.getFromAddress());
                                            sHmpam.put("toPostalCode", j.getFromPostalCode());
                                            sHmpam.put("status", j.getStatus());
                                            sHmpam.put("supplierId", j.getSupplierId());
                                            sHmpam.put("supplierTruckId", j.getSupplierTruckId());
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
        }

    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getOrdersLoadings(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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
                                        String id = json.findPath("id").asText();
                                        String billingDescription = json.findPath("billingDescription").asText();
                                        String creationDate = json.findPath("creationDate").asText();
                                        String start = json.findPath("start").asText();
                                        String limit = json.findPath("limit").asText();
                                        String sqlOrdLoads = "select * from orders_loading ord_load where 1=1 ";
                                        if (!id.equalsIgnoreCase("") && id != null) {
                                            sqlOrdLoads += " and ord_load.id =" + id;
                                        }
                                        System.out.println(sqlOrdLoads);
//                                        if (!billingDescription.equalsIgnoreCase("") && billingDescription != null) {
//                                            sqlroles += " and b.description like '%" + billingDescription + "%'";
//                                        }
//                                        if (!creationDate.equalsIgnoreCase("") && creationDate != null) {
//                                            sqlroles += " and SUBSTRING( b.creation_date, 1, 10)  = '" + creationDate + "'";
//                                        }
                                        List<OrdersLoadingEntity> ordersLoadingAllList
                                                = (List<OrdersLoadingEntity>) entityManager.createNativeQuery(
                                                sqlOrdLoads, OrdersLoadingEntity.class).getResultList();
//
                                        if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                            sqlOrdLoads += " order by " + orderCol + " " + descAsc;
                                        } else {
                                            sqlOrdLoads += " order by creation_date desc";
                                        }


                                        if (!start.equalsIgnoreCase("") && start != null) {
                                            sqlOrdLoads += " limit " + start + "," + limit;
                                        }
                                        System.out.println(sqlOrdLoads);
                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                        List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                        List<OrdersLoadingEntity> ordersLoadingList
                                                = (List<OrdersLoadingEntity>) entityManager.createNativeQuery(
                                                sqlOrdLoads, OrdersLoadingEntity.class).getResultList();
                                        for (OrdersLoadingEntity j : ordersLoadingList) {
                                            HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                            sHmpam.put("id", j.getId());
                                            sHmpam.put("ordersLoadingId", j.getId());
                                            sHmpam.put("fromCountry", j.getFromCountry());
                                            sHmpam.put("fromCity", j.getFromCity());
                                            sHmpam.put("fromAddress", j.getFromAddress());
                                            sHmpam.put("fromPostalCode", j.getFromPostalCode());
                                            sHmpam.put("toCountry", j.getFromCountry());
                                            sHmpam.put("toCity", j.getFromCity());
                                            sHmpam.put("toAddress", j.getFromAddress());
                                            sHmpam.put("toPostalCode", j.getFromPostalCode());
                                            sHmpam.put("status", j.getStatus());
                                            sHmpam.put("supplierId", j.getSupplierId());
                                            sHmpam.put("supplierTruckId", j.getSupplierTruckId());
                                            sHmpam.put("creationDate", j.getCreationDate());
                                            sHmpam.put("updateDate", j.getUpdateDate());

                                            String sqlSelections = "select * from orders_loading_orders_selections olds where olds.order_loading_id="+j.getId();
                                            List<OrdersLoadingOrdersSelectionsEntity> ordersLoadingOrdersSelectionsList = entityManager.createNativeQuery(sqlSelections,OrdersLoadingOrdersSelectionsEntity.class).getResultList();
                                            sHmpam.put("ordersLoadingOrdersSelectionsList", ordersLoadingOrdersSelectionsList);


                                            serversList.add(sHmpam);
                                        }
                                        returnList_future.put("data", serversList);
                                        returnList_future.put("total", ordersLoadingAllList.size());
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


    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }


}