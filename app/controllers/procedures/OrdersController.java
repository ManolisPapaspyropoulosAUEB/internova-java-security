package controllers.procedures;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import controllers.execution_context.DatabaseExecutionContext;
import controllers.system.Application;
import models.*;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class OrdersController extends Application {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;
    private final WSClient ws;


    @Inject
    public OrdersController(JPAApi jpaApi,
                            DatabaseExecutionContext executionContext,
                            WSClient ws) {
        super(jpaApi, executionContext);
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
        this.ws = ws;


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
                                Long offerId = json.findPath("offerId").asLong();
                                JsonNode data = json.findPath("data");
                                ((ObjectNode) json).remove("data");
                                OrdersEntity ordersEntity = new OrdersEntity();
                                ordersEntity.setCreationDate(new Date());
                                ordersEntity.setCustomerId(entityManager.find(OffersEntity.class, offerId).getCustomerId());
                                ordersEntity.setOfferId(offerId);
                                ordersEntity.setSellerId(entityManager.find(OffersEntity.class, offerId).getSellerId());
                                ordersEntity.setAa("1");
                                ordersEntity.setNetWeight(0.0);
                                ordersEntity.setGrossWeight(0.0);
                                ordersEntity.setBillingId(entityManager.find(OffersEntity.class, offerId).getBillingId());
                                ordersEntity.setStatus("ΣΕ ΖΗΤΗΣΗ");
                                ordersEntity.setCrmIndicator((byte) 0);
                                entityManager.persist(ordersEntity);
                                Iterator dataIterator = data.iterator();
                                while (dataIterator.hasNext()) {
                                    JsonNode schedule = (JsonNode) dataIterator.next();
                                    Long offerScheduleId = schedule.findPath("offerScheduleId").asLong();
                                    OffersSchedulesEntity offersSchedulesEntity = entityManager.find(OffersSchedulesEntity.class, offerScheduleId);
                                    OrderSchedulesEntity orderSchedule = new OrderSchedulesEntity();
                                    orderSchedule.setCreationDate(new Date());
                                    orderSchedule.setFromAddress(offersSchedulesEntity.getFromAddress());
                                    orderSchedule.setFromCity(offersSchedulesEntity.getFromCity());
                                    orderSchedule.setFromCountry(offersSchedulesEntity.getFromCountry());
                                    orderSchedule.setFromPostalCode(offersSchedulesEntity.getFromPostalCode().replaceAll(" ", ""));
                                    orderSchedule.setToAddress(offersSchedulesEntity.getToAddress());
                                    orderSchedule.setToCity(offersSchedulesEntity.getToCity());
                                    orderSchedule.setToCountry(offersSchedulesEntity.getToCountry());
                                    orderSchedule.setToPostalCode(offersSchedulesEntity.getToPostalCode().replaceAll(" ", ""));
                                    orderSchedule.setPrimarySchedule(schedule.findPath("primaryRecord").asInt());
                                    orderSchedule.setOrderId(ordersEntity.getId());
                                    orderSchedule.setOfferId(offersSchedulesEntity.getOfferId());
                                    orderSchedule.setOfferScheduleId(offersSchedulesEntity.getId());
                                    entityManager.persist(orderSchedule);
                                    String offerScheduleWayPoints = " select * from offer_schedule_between_waypoints wayp where wayp.offer_id=" + offerId + " " +
                                            " and wayp.offer_schedule_id=" + offerScheduleId;
                                    List<OfferScheduleBetweenWaypointsEntity> offerWaypointsEntityList =
                                            entityManager.createNativeQuery(offerScheduleWayPoints, OfferScheduleBetweenWaypointsEntity.class).getResultList();
                                    for (OfferScheduleBetweenWaypointsEntity offerWayp : offerWaypointsEntityList) {
                                        OrderWaypointsEntity orderWaypointsEntity = new OrderWaypointsEntity();
                                        orderWaypointsEntity.setAddress(offerWayp.getAddress());
                                        orderWaypointsEntity.setCity(offerWayp.getCity());
                                        orderWaypointsEntity.setNestedScheduleIndicator(offerWayp.getNestedScheduleIndicator());
                                        orderWaypointsEntity.setCountry(offerWayp.getCountry());
                                        if (offerWayp.getPostalCode() != null) {
                                            orderWaypointsEntity.setPostalCode(offerWayp.getPostalCode().replaceAll(" ", ""));
                                        }
                                        orderWaypointsEntity.setOrderId(ordersEntity.getId());
                                        orderWaypointsEntity.setCreationDate(new Date());
                                        orderWaypointsEntity.setOrderScheduleId(orderSchedule.getId());
                                        orderWaypointsEntity.setOfferScheduleBetweenWaypointId(offerWayp.getId());
                                        entityManager.persist(orderWaypointsEntity);
                                    }
                                    OrderWaypointsEntity orderWaypointsEntity = new OrderWaypointsEntity();
                                    orderWaypointsEntity.setCity(offersSchedulesEntity.getToCity());
                                    orderWaypointsEntity.setCountry(offersSchedulesEntity.getToCountry());
                                    orderWaypointsEntity.setPostalCode(offersSchedulesEntity.getToPostalCode().replaceAll(" ", ""));
                                    orderWaypointsEntity.setCreationDate(new Date());
                                    orderWaypointsEntity.setOrderId(ordersEntity.getId());
                                    orderWaypointsEntity.setOrderScheduleId(orderSchedule.getId());
                                    orderWaypointsEntity.setOfferScheduleBetweenWaypointId(null);
                                    orderWaypointsEntity.setNestedScheduleIndicator(0);
                                    entityManager.persist(orderWaypointsEntity);
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
                                        orderPackagesEntity.setTypePackage(spoe.getTypePackageMeasure());
                                        orderPackagesEntity.setOrderScheduleId(orderSchedule.getId());
                                        orderPackagesEntity.setOfferId(orderSchedule.getOfferId());
                                        orderPackagesEntity.setOfferId(orderSchedule.getId());
                                        entityManager.persist(orderPackagesEntity);
                                    }
                                }
                                add_result.put("status", "success");
                                add_result.put("id", ordersEntity.getId());
                                add_result.put("message", "Η δημιουργία νέας παραγγελίας καταχωρήθηκε με επιτυχία");
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


    //getOrderWaypoint getDistinctPckagesStartPoint


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getDistinctPckagesStartPoint(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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
                                        List<HashMap<String, Object>> finalList = new ArrayList<HashMap<String, Object>>();
                                        String orderId = json.findPath("orderId").asText();
                                        String title = json.findPath("title").asText();
                                        String sqlPackages = "" +
                                                "select distinct " +
                                                "op.measure_unit_id,mu.title\n" +
                                                " from order_packages op " +
                                                " join measurement_unit mu on (mu.id=op.measure_unit_id) " +
                                                " where op.order_id=" + orderId;
                                        if (!title.equalsIgnoreCase("") && title != null) {
                                            sqlPackages += " and mu.title like '%" + title + "%'";
                                        }
                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();

                                        List tul = entityManager.createNativeQuery(sqlPackages).getResultList();
                                        Iterator it = tul.iterator();

                                        while (it.hasNext()) {
                                            JsonNode tu = Json.toJson(it.next());
                                            HashMap<String, Object> item = new HashMap<>();
                                            item.put("measureUnitId", tu.get(0).asInt());
                                            item.put("title", tu.get(1).asText());
                                            item.put("orderId", orderId);
                                            item.put("quantity", 0);
                                            item.put("unitPrice", 0);
                                            item.put("finalUnitPrice", 0);

                                            String sqlDistansesValues = "select * FROM internova_db.order_packages where order_id=" + orderId + " and measure_unit_id=" + tu.get(0).asInt();
                                            List<OrderPackagesEntity> distList = entityManager.createNativeQuery(sqlDistansesValues, OrderPackagesEntity.class).getResultList();
                                            List<HashMap<String, Object>> fdvList = new ArrayList<HashMap<String, Object>>();
                                            for (OrderPackagesEntity dv : distList) {
                                                HashMap<String, Object> distanceValues = new HashMap<>();
                                                distanceValues.put("measureUnitId", dv.getMeasureUnitId());
                                                distanceValues.put("from", dv.getFromUnit());
                                                distanceValues.put("to", dv.getToUnit());
                                                distanceValues.put("unitPrice", dv.getUnitPrice());
                                                fdvList.add(distanceValues);
                                            }
                                            item.put("distanceValues", fdvList);
                                            finalList.add(item);
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

    }



    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateOrderStatus(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                Long orderId = json.findPath("orderId").asLong();
                                String status = json.findPath("status").asText();
                                OrdersEntity ordersEntity = entityManager.find(OrdersEntity.class,orderId);
                                ordersEntity.setStatus(status);
                                entityManager.merge(ordersEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η Ενημέρωση πραγματοποίηθηκε με επιτυχία");
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
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateOrderBilling(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                Long orderId = json.findPath("orderId").asLong();
                                Long billingId = json.findPath("billingId").asLong();
                                OrdersEntity ordersEntity = entityManager.find(OrdersEntity.class,orderId);
                                ordersEntity.setBillingId(billingId);
                                BillingsEntity billName = entityManager.find(BillingsEntity.class,billingId);
                                entityManager.merge(ordersEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η Ενημέρωση πραγματοποίηθηκε με επιτυχία");
                                add_result.put("billName", billName.getName());
                                add_result.put("billingId", billName.getId());
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
    public Result getOrdersListenerSize(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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
                                return jpaApi.withTransaction(
                                        entityManager -> {
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            String sqlOrdLoads = "select count(*) from orders ord   where 1=1 ";
                                            BigInteger allmyListCount = (BigInteger) entityManager.createNativeQuery(sqlOrdLoads).getSingleResult();
                                            returnList_future.put("allmyListCount", allmyListCount);
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
            result.put("message", "Προβλημα κατα την ανάγνωση των στοιχείων");
            return ok(result);
        }
    }



    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getOrders(final Http.Request request) throws IOException,
            ExecutionException, InterruptedException {
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
                CompletableFuture<HashMap<String, Object>> getFuture = CompletableFuture.supplyAsync(() -> { //
                            return jpaApi.withTransaction(
                                    entityManager -> {
                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                        Long user_id = json.findPath("user_id").asLong();
                                        UsersEntity internovaUser = entityManager.find(UsersEntity.class,user_id);
                                        if(internovaUser==null){
                                            returnList_future.put("status", "error");
                                            returnList_future.put("message", "Δεν έχετε δώσει user_id  αποστειλει εγκυρα δεδομενα.");
                                            return returnList_future;
                                        }
                                        String orderCol = json.findPath("orderCol").asText();
                                        String descAsc = json.findPath("descAsc").asText();
                                        String id = json.findPath("id").asText();
                                        String orderid = json.findPath("orderid").asText();
                                        String offerId = json.findPath("offerId").asText();
                                        String creationDate = json.findPath("creationDate").asText();
                                        String appointmentDayStartPoint = json.findPath("appointmentDayStartPoint").asText();
                                        String customer = json.findPath("customer").asText();
                                        String status = json.findPath("status").asText();
                                        String from = json.findPath("from").asText();
                                        String to = json.findPath("to").asText();
                                        String mainSchedule = json.findPath("mainSchedule").asText();
                                        String seller = json.findPath("seller").asText();
                                        String billing = json.findPath("billing").asText();
                                        String start = json.findPath("start").asText();
                                        String limit = json.findPath("limit").asText();
                                        String sqlCustSupl = "select * from orders ord where 1=1 ";
                                        if(internovaUser.getRoleId()==60){
                                            InternovaSellersEntity internovaSellersEntity =
                                                    entityManager.find(InternovaSellersEntity.
                                                            class,internovaUser.getInternovaSellerId());
                                            sqlCustSupl += " and ord.customer_id in " +
                                                    "( select cs.id from customers_suppliers cs where cs.internova_seller_id="
                                                    +internovaSellersEntity.getId()+")";
                                        }
                                        if (!orderid.equalsIgnoreCase("") && orderid != null) {
                                            sqlCustSupl += " and ord.id like '%" + orderid + "%'";
                                        }
                                        if (!id.equalsIgnoreCase("") && id != null) {
                                            sqlCustSupl += " and ord.id = '" + id + "'";
                                        }
                                        //orderid
                                        if (!offerId.equalsIgnoreCase("") && offerId != null) {
                                            sqlCustSupl += " and ord.offer_id like '%" + offerId + "%'";
                                        }
                                        if (mainSchedule != null && !mainSchedule.equalsIgnoreCase("")) {
                                            sqlCustSupl +=
                                                    " and ord.id in (select osc.order_id from order_schedules osc where osc.from_country " +
                                                            " like '%" + mainSchedule + "%' or osc.from_city like '%" + mainSchedule + "%' " +
                                                            " or osc.to_city like" +
                                                            " '%" + mainSchedule + "%' or osc.to_country like" +
                                                            " '%" + mainSchedule + "%' and osc.primary_schedule=1)";
                                        }
                                        if (!seller.equalsIgnoreCase("") && seller != null) {
                                            sqlCustSupl +=
                                                    " and ord.seller_id " +
                                                            " in ( select id" +
                                                            "      from  internova_sellers isell" +
                                                            "      where isell.name like '%" + seller + "%' ) " +
                                                            " union " +
                                                            " select ord.*" +
                                                            " from orders ord " +
                                                            " join customers_suppliers cs on (cs.id=ord.customer_id and ord.seller_id is null )" +
                                                            " where " +
                                                            " cs.internova_seller_id in " +
                                                            " (select id from  internova_sellers isell where isell.name like '%" + seller + "%' )";
                                        }

                                        if (!customer.equalsIgnoreCase("") && customer != null && !customer.equalsIgnoreCase("null")) {
                                            sqlCustSupl += " and ord.customer_id  in " +
                                                    " ( select id from  customers_suppliers cs where cs.brand_name = '" + customer + "' )";
                                        }
                                        if (!status.equalsIgnoreCase("") && status != null && !status.equalsIgnoreCase("null")) {
                                            sqlCustSupl += " and ord.status = '" + status + "'";
                                        }

                                        if (!from.equalsIgnoreCase("") && from != null) {
                                            sqlCustSupl += " and ord.from_address like '%" + from + "%'";
                                        }
                                        if (!to.equalsIgnoreCase("") && to != null) {
                                            sqlCustSupl += " and ord.to_address like '%" + to + "%'";
                                        }
                                        if (!billing.equalsIgnoreCase("") && billing != null) {
                                            sqlCustSupl +=
                                                    " and ord.billing_id " +
                                                            " in ( select id" +
                                                            "      from  billings billing" +
                                                            "      where billing.name like '%" + billing + "%' ) " +
                                                            " union " +
                                                            " select ord.*" +
                                                            " from orders ord " +
                                                            " join customers_suppliers cs on (cs.id=ord.customer_id and ord.billing_id is null )" +
                                                            " where " +
                                                            " cs.billing_id in " +
                                                            " (select id from  billings billing where billing.name like '%" + billing + "%' )";
                                        }
                                        if (!creationDate.equalsIgnoreCase("") && creationDate != null) {
                                            sqlCustSupl += " and SUBSTRING( offer.offer_date, 1, 10)  = '" + creationDate + "'";
                                        }
                                        if (!appointmentDayStartPoint.equalsIgnoreCase("") && appointmentDayStartPoint != null) {
                                            sqlCustSupl += "  and ord.id in \n" +
                                                    " (select \n" +
                                                    " osc.order_id\n" +
                                                    " from order_schedules osc\n" +
                                                    " where SUBSTRING( osc.appointment_day, 1, 10) = '" + appointmentDayStartPoint +"'"+
                                                    " ) ";
                                        }
                                        List<OrdersEntity> filalistAll
                                                = (List<OrdersEntity>) entityManager.createNativeQuery(
                                                sqlCustSupl, OrdersEntity.class).getResultList();
                                        if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                            if (orderCol.equalsIgnoreCase("billingName")) {
                                                sqlCustSupl += " order by (select name from billings b where b.id=offer.billing_id)" + descAsc;
                                            }else if(orderCol.equalsIgnoreCase("customerBrandName")) {
                                                sqlCustSupl += " order by (select brand_name from customers_suppliers cs where cs.id=ord.customer_id)" + descAsc;
                                            }else if (orderCol.equalsIgnoreCase("appointmentDayStartPoint")){
                                                sqlCustSupl+="   order by (select \n" +
                                                        "osc.appointment_day \n" +
                                                        " from order_schedules osc\n" +
                                                        " where osc.appointment_day \n" +
                                                        " and osc.order_id=ord.id\n" +
                                                        " limit 1\n" +
                                                        " )"  + descAsc;
                                            } else if (orderCol.equalsIgnoreCase("mainSchedule")) {
                                                sqlCustSupl+=" order by (select concat( osc.from_country,' ', osc.from_city)\n" +
                                                        "from order_schedules osc \n" +
                                                        "where \n" +
                                                        "osc.primary_schedule=1 \n" +
                                                        "and osc.order_id=ord.id\n" +
                                                        "\n" +
                                                        ")"  + descAsc;
                                            } else {
                                                sqlCustSupl += " order by " + orderCol + " " + descAsc;
                                            }
                                        } else {
                                            sqlCustSupl += " order by creation_date desc";
                                        }
                                        if (!start.equalsIgnoreCase("") && start != null) {
                                            sqlCustSupl += " limit " + start + "," + limit;
                                        }
                                        List<HashMap<String, Object>> filalist = new ArrayList<HashMap<String, Object>>();
                                        List<OrdersEntity> ordersEntityList
                                                = (List<OrdersEntity>) entityManager.createNativeQuery(
                                                sqlCustSupl, OrdersEntity.class).getResultList();
                                        for (OrdersEntity j : ordersEntityList) {
                                            HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                            String sqlRelativeOffers = "select distinct offer_id FROM order_schedules os where os.order_id=" + j.getId();
                                            List<Object> ordSchedList = entityManager.createNativeQuery(sqlRelativeOffers).getResultList();
                                            String offers = "";
                                            for (int i = 0; i < ordSchedList.size(); i++) {
                                                offers = offers + ordSchedList.get(i) + ",";
                                            }
                                            try {
                                                offers = removeLastChar(offers);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            if (j.getCustomerId() != null) {//
                                                CustomersSuppliersEntity customersSuppliersEntity = entityManager.find(CustomersSuppliersEntity.class, j.getCustomerId());
                                                sHmpam.put("customerBrandName", customersSuppliersEntity.getBrandName());
                                                sHmpam.put("customerId", customersSuppliersEntity.getId());
                                                sHmpam.put("customerTelephone", customersSuppliersEntity.getTelephone());
                                                sHmpam.put("customerEmail", customersSuppliersEntity.getEmail());
                                                if (customersSuppliersEntity.getBillingId() != null) {
                                                    sHmpam.put("billingName", entityManager.find(BillingsEntity.class, customersSuppliersEntity.getBillingId()).getName());
                                                } else {
                                                    sHmpam.put("billingName", "-");
                                                }
                                                if (customersSuppliersEntity.getInternovaSellerId() != null) {
                                                    sHmpam.put("sellerName", entityManager.find(InternovaSellersEntity.class, customersSuppliersEntity.getInternovaSellerId()).getName());
                                                } else {
                                                    sHmpam.put("sellerName", "-");
                                                }
                                            }
                                            DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                            if (j.getArrivalFactoryDay() != null) {
                                                String datArrival = myDateFormat.format(j.getArrivalFactoryDay());
                                                sHmpam.put("arrivalFactoryDay", datArrival);
                                            } else {
                                                sHmpam.put("arrivalFactoryDay", "");
                                            }
                                            String sqlOrdersSchedules = "select * from order_schedules ord_s where ord_s.order_id=" + j.getId()
                                                    + " order by ord_s.primary_schedule desc ";
                                            List<OrderSchedulesEntity> orderSchedulesEntityList =
                                                    entityManager.createNativeQuery(sqlOrdersSchedules, OrderSchedulesEntity.class).getResultList();
                                            List<HashMap<String, Object>> schedulesList = new ArrayList<HashMap<String, Object>>();
                                            for (OrderSchedulesEntity os : orderSchedulesEntityList) {
                                                HashMap<String, Object> schedmap = new HashMap<String, Object>();
                                                if (os.getPrimarySchedule() == 1) {

                                                    if(os.getFactoryId()!=null && os.getFactoryId()!=0 ){
                                                        FactoriesEntity factoriesEntity = entityManager.find(FactoriesEntity.class,os.getFactoryId());
                                                        sHmpam.put("mainSchedule", os.getFromCountry() + " " + os.getFromCity() +"( "+factoriesEntity.getBrandName()+" )"+ "  /  " + os.getToCountry() + " " + os.getToCity());
                                                    }else{
                                                        sHmpam.put("mainSchedule", os.getFromCountry() + " " + os.getFromCity() +"( - )"+"  /  " + os.getToCountry() + " " + os.getToCity());
                                                    }
                                                    if( os.getAppointmentDay()!=null){
                                                        sHmpam.put("appointmentDayStartPoint", os.getAppointmentDay());
                                                    }else{
                                                        sHmpam.put("appointmentDayStartPoint", "-");
                                                    }
                                                }
                                                schedmap.put("offerId", os.getOfferId());
                                                schedmap.put("primarySchedule", os.getPrimarySchedule());
                                                schedmap.put("creationDate", os.getCreationDate());
                                                schedmap.put("factoryId", os.getFactoryId());
                                                schedmap.put("fromCity", os.getFromCity());
                                                schedmap.put("orderScheduleId", os.getId());
                                                schedmap.put("fromCountry", os.getFromCountry());
                                                schedmap.put("fromPostalCode", os.getFromPostalCode());
                                                schedmap.put("truckLoadingCode", os.getTruckLoadingCode());
                                                if (os.getTimeToArrive() != null && !os.getTimeToArrive().equalsIgnoreCase("null")) {
                                                    schedmap.put("timeToArrive", os.getTimeToArrive());
                                                } else {
                                                    schedmap.put("timeToArrive", "");
                                                }
                                                schedmap.put("appointmentDay", os.getAppointmentDay());
                                                if (os.getFactoryId() != null) {
                                                    FactoriesEntity factory = entityManager.find(FactoriesEntity.class, os.getFactoryId());
                                                    schedmap.put("factory", entityManager.find(FactoriesEntity.class, os.getFactoryId()));
                                                    schedmap.put("appointment", entityManager.find(FactoriesEntity.class, os.getFactoryId()).getAppointmentRequired());

                                                    if (os.getAppointmentDay() != null) {
                                                        Date date2 = new Date();
                                                        int currentDate = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(date2));
                                                        int appointmentDayMinusReq =
                                                                Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(os.getAppointmentDay()))
                                                                        - factory.getAppointmentDays();
                                                        if (currentDate > appointmentDayMinusReq) {
                                                            schedmap.put("colorAppointmentDay", true);
                                                        } else {
                                                            schedmap.put("colorAppointmentDay", false);
                                                        }
                                                    } else {
                                                        schedmap.put("colorAppointmentDay", false);
                                                    }
                                                } else {
                                                    schedmap.put("colorAppointmentDay", false);
                                                }
                                                if (os.getFactoryId() != null) {
                                                    schedmap.put("fromAddress", entityManager.find(FactoriesEntity.class, (os.getFactoryId())).getAddress());
                                                    schedmap.put("brandName", entityManager.find(FactoriesEntity.class, (os.getFactoryId())).getBrandName());
                                                    schedmap.put("latitude", entityManager.find(FactoriesEntity.class, (os.getFactoryId())).getLattitude());
                                                    schedmap.put("longitude", entityManager.find(FactoriesEntity.class, (os.getFactoryId())).getLongtitude());
                                                } else {
                                                    schedmap.put("fromAddress", "Δεν έχει οριστεί σημείο φόρτωσης η εκφόρτωσης");
                                                    schedmap.put("brandName", "-");
                                                    schedmap.put("lattitude", "not available");
                                                    schedmap.put("longtitude", "not available");
                                                }
                                                schedmap.put("toCity", os.getToCity());
                                                schedmap.put("toCountry", os.getToCountry());
                                                schedmap.put("toPostalCode", os.getToPostalCode());
                                                schedmap.put("orderScheduleId", os.getId());

                                                String summSql = "select sum(final_unit_price) " +
                                                        "from order_package_schedules t " +
                                                        "where t.order_id=" + os.getOrderId() + " and t.order_schedule_id=" + os.getId();

                                                Double summ = (Double) entityManager.
                                                        createNativeQuery(summSql).getSingleResult();
                                                if (summ != null) {
                                                    schedmap.put("scheduleFinalUnitPrice", summ);

                                                } else {
                                                    schedmap.put("scheduleFinalUnitPrice", "0.0");
                                                }
                                                String itemsAfethriasFortwshs = "select * from orders_selections_by_point where order_id=" + os.getOrderId() +
                                                        " and  order_schedule_id=" + os.getId() + " and order_waypoint_id is null";
                                                List<OrdersSelectionsByPointEntity> itemsPackagesAfethrias = entityManager.createNativeQuery(itemsAfethriasFortwshs, OrdersSelectionsByPointEntity.class).getResultList();
                                                List<HashMap<String, Object>> itemsPackagesAfethriasFinal = new ArrayList<HashMap<String, Object>>();
                                                for (OrdersSelectionsByPointEntity osbp : itemsPackagesAfethrias) {
                                                    HashMap<String, Object> osbpMap = new HashMap<String, Object>();
                                                    osbpMap.put("title", osbp.getTitle());
                                                    osbpMap.put("quantity", osbp.getQuantity());
                                                    osbpMap.put("unitPrice", osbp.getUnitPrice());
                                                    osbpMap.put("stackingType", osbp.getStackingType());
                                                    osbpMap.put("ldm", osbp.getLdm());
                                                    osbpMap.put("oldQuantity", osbp.getQuantity());
                                                    osbpMap.put("packageTypeId", osbp.getPackageTypeId());
                                                    osbpMap.put("typePackage", osbp.getTypePackage());
                                                    if (osbp.getPackageTypeId() != null) {
                                                        osbpMap.put("packageType", entityManager.find(PackageTypeEntity.class, osbp.getPackageTypeId()).getType());
                                                    } else {
                                                        osbpMap.put("packageType", "");
                                                    }
                                                    HashMap<String, Object> osbpMapS = new HashMap<String, Object>();
                                                    osbpMapS.put("selectedPackage", osbpMap);
                                                    osbpMapS.put("typePackage", osbp.getTypePackage());
                                                    osbpMapS.put("stackingType", osbp.getStackingType());
                                                    osbpMapS.put("ldm", osbp.getLdm());
                                                    osbpMapS.put("quantity", osbp.getQuantity());
                                                    osbpMapS.put("unitPrice", osbp.getUnitPrice());
                                                    osbpMapS.put("oldQuantity", osbp.getQuantity());
                                                    osbpMapS.put("packageTypeId", osbp.getPackageTypeId());
                                                    if (osbp.getPackageTypeId() != null) {
                                                        osbpMapS.put("packageType", entityManager.find(PackageTypeEntity.class, osbp.getPackageTypeId()).getType());
                                                    } else {
                                                        osbpMapS.put("packageType", "");
                                                    }
                                                    itemsPackagesAfethriasFinal.add(osbpMapS);
                                                }
                                                schedmap.put("itemsPackagesAfethrias", itemsPackagesAfethriasFinal);
                                                String sqlOrdPackagesSchedules = "select * from order_package_schedules ops where  ops.order_id=" + os.getOrderId() +
                                                        " and ops.order_schedule_id=" + os.getId();
                                                List<OrderPackageSchedulesEntity> orderPackList = entityManager.createNativeQuery(sqlOrdPackagesSchedules, OrderPackageSchedulesEntity.class).getResultList();
                                                List<HashMap<String, Object>> orderPackageScheduleList = new ArrayList<HashMap<String, Object>>();
                                                for (OrderPackageSchedulesEntity ops : orderPackList) {
                                                    HashMap<String, Object> ordPmap = new HashMap<String, Object>();
                                                    ordPmap.put("id", ops.getId());
                                                    ordPmap.put("title", ops.getTitle());
                                                    ordPmap.put("measureUnitId", ops.getMeasureUnitId());
                                                    ordPmap.put("type", ops.getType());
                                                    ordPmap.put("orderId", ops.getOrderId());
                                                    ordPmap.put("orderPackageId", ops.getOrderPackageId());
                                                    ordPmap.put("finalUnitPrice", ops.getFinalUnitPrice());
                                                    ordPmap.put("oldFinalUnitPrice", ops.getFinalUnitPrice());
                                                    ordPmap.put("unitPrice", ops.getUnitPrice());
                                                    ordPmap.put("oldUnitPrice", ops.getUnitPrice());
                                                    ordPmap.put("creationDate", ops.getCreationDate());
                                                    ordPmap.put("typeId", ops.getTypeId());
                                                    ordPmap.put("orderScheduleId", ops.getOrderScheduleId());
                                                    ordPmap.put("quantity", ops.getQuantity());
                                                    ordPmap.put("oldQuantity", ops.getQuantity());
                                                    String sqlDv = "select * from order_distinct_item od where od.order_id=" +
                                                            ops.getOrderId() + " and od.order_schedule_id=" + ops.getOrderScheduleId() +
                                                            " and od.order_package_id=" + ops.getId();
                                                    List<OrderDistinctItemEntity> orderDistinctItemList = entityManager.createNativeQuery(sqlDv, OrderDistinctItemEntity.class).getResultList();
                                                    List<HashMap<String, Object>> odvList = new ArrayList<HashMap<String, Object>>();
                                                    for (OrderDistinctItemEntity odv : orderDistinctItemList) {
                                                        HashMap<String, Object> distinctItemMap = new HashMap<String, Object>();
                                                        distinctItemMap.put("title", odv.getTitle());
                                                        distinctItemMap.put("from", odv.getFromUnit());
                                                        distinctItemMap.put("to", odv.getToUnit());
                                                        distinctItemMap.put("unitPrice", odv.getUnitPrice());
                                                        distinctItemMap.put("typePackage", odv.getTypePackage());
                                                        odvList.add(distinctItemMap);
                                                    }
                                                    HashMap<String, Object> distinctItem = new HashMap<String, Object>();
                                                    distinctItem.put("title", ops.getTitle());
                                                    distinctItem.put("distanceValues", odvList);
                                                    if (odvList.size() > 0) {
                                                        distinctItem.put("typePackage", odvList.get(0).get("typePackage").toString());
                                                    } else {
                                                        distinctItem.put("typePackage", "");
                                                    }

                                                    ordPmap.put("distinctItem", distinctItem);
                                                    orderPackageScheduleList.add(ordPmap);
                                                }
                                                schedmap.put("orderPackageScheduleList", orderPackageScheduleList);
                                                String sqlPackages = "" +
                                                        "select distinct " +
                                                        "op.measure_unit_id,mu.title,op.type_package \n" +
                                                        " from order_packages op " +
                                                        " join measurement_unit mu on (mu.id=op.measure_unit_id) " +
                                                        " where op.order_id=" + os.getOrderId() + " and op.order_schedule_id =" + os.getId();
                                                List tul = entityManager.createNativeQuery(sqlPackages).getResultList();
                                                Iterator it = tul.iterator();
                                                List<HashMap<String, Object>> distinctDistancesPackageList = new ArrayList<HashMap<String, Object>>();
                                                while (it.hasNext()) {
                                                    JsonNode tu = Json.toJson(it.next());
                                                    HashMap<String, Object> item = new HashMap<>();
                                                    item.put("measureUnitId", tu.get(0).asInt());
                                                    item.put("title", tu.get(1).asText());
                                                    item.put("typePackage", tu.get(2).asText());
                                                    item.put("orderId", os.getOrderId());
                                                    item.put("quantity", 0);
                                                    item.put("unitPrice", 0);
                                                    item.put("finalUnitPrice", 0);
                                                    String sqlDistansesValues =
                                                            "select * from order_packages where order_id=" + os.getOrderId() + " and measure_unit_id=" + tu.get(0).asInt();
                                                    List<OrderPackagesEntity> distList = entityManager.createNativeQuery(sqlDistansesValues, OrderPackagesEntity.class).getResultList();
                                                    List<HashMap<String, Object>> fdvList = new ArrayList<HashMap<String, Object>>();
                                                    for (OrderPackagesEntity dv : distList) {
                                                        HashMap<String, Object> distanceValues = new HashMap<>();
                                                        distanceValues.put("measureUnitId", dv.getMeasureUnitId());
                                                        distanceValues.put("from", dv.getFromUnit());
                                                        distanceValues.put("to", dv.getToUnit());
                                                        distanceValues.put("unitPrice", dv.getUnitPrice());
                                                        distanceValues.put("typePackage", dv.getTypePackage());
                                                        fdvList.add(distanceValues);
                                                    }
                                                    item.put("distanceValues", fdvList);
                                                    distinctDistancesPackageList.add(item);
                                                }
                                                schedmap.put("distinctDistancesPackageList", distinctDistancesPackageList);
                                                String waypointsSql = "select * from order_waypoints owp where owp.order_schedule_id=" + os.getId();
                                                List<OrderWaypointsEntity> waypointsList =
                                                        entityManager.createNativeQuery(waypointsSql, OrderWaypointsEntity.class).getResultList();
                                                List<HashMap<String, Object>> waypFinal = new ArrayList<HashMap<String, Object>>();
                                                for (OrderWaypointsEntity waypOb : waypointsList) {
                                                    HashMap<String, Object> waypmap = new HashMap<String, Object>();
                                                    waypmap.put("itemsPackagesFortwshsEndiamesouShmeiou", new ArrayList<HashMap<String, Object>>());
                                                    waypmap.put("itemsPackagesEkfortwshsEndiamesouShmeiou", new ArrayList<HashMap<String, Object>>());
                                                    waypmap.put("itemsPackagesEkfortwshsProorismou", new ArrayList<HashMap<String, Object>>());
                                                    waypmap.put("truckLoadingCode", waypOb.getTruckLoadingCode());
                                                    if (waypOb.getFactoryId() != null) {
                                                        waypmap.put("factory", entityManager.find(FactoriesEntity.class, waypOb.getFactoryId()));
                                                    }
                                                    if (waypOb.getFactoryId() != null) {
                                                        FactoriesEntity factory = entityManager.find(FactoriesEntity.class, waypOb.getFactoryId());
                                                        waypmap.put("factory", entityManager.find(FactoriesEntity.class, waypOb.getFactoryId()));
                                                        waypmap.put("appointment", entityManager.find(FactoriesEntity.class, waypOb.getFactoryId()).getAppointmentRequired());

                                                        if (waypOb.getAppointmentDay() != null) {
                                                            Date date2 = new Date();
                                                            int currentDate = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(date2));
                                                            int appointmentDayMinusReq =
                                                                    Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(waypOb.getAppointmentDay()))
                                                                            - factory.getAppointmentDays();
                                                            if (currentDate > appointmentDayMinusReq) {
                                                                waypmap.put("colorAppointmentDay", true);
                                                            } else {
                                                                waypmap.put("colorAppointmentDay", false);
                                                            }
                                                        } else {
                                                            waypmap.put("colorAppointmentDay", false);
                                                        }
                                                    } else {
                                                        waypmap.put("colorAppointmentDay", false);
                                                    }
                                                    waypmap.put("city", waypOb.getCity());
                                                    waypmap.put("country", waypOb.getCountry());
                                                    waypmap.put("postalCode", waypOb.getPostalCode());
                                                    if (waypOb.getTimeToArrive() != null && !waypOb.getTimeToArrive().equalsIgnoreCase("null")) {
                                                        waypmap.put("timeToArrive", waypOb.getTimeToArrive());
                                                    } else {
                                                        waypmap.put("timeToArrive", "");
                                                    }
//                                                    waypmap.put("appointment", waypOb.getAppointment());
                                                    waypmap.put("appointmentDay", waypOb.getAppointmentDay());
                                                    if (waypOb.getFactoryId() != null) {
                                                        waypmap.put("address", entityManager.find(FactoriesEntity.class, (waypOb.getFactoryId())).getAddress());
                                                        waypmap.put("brandName", entityManager.find(FactoriesEntity.class, (waypOb.getFactoryId())).getBrandName());
                                                        waypmap.put("latitude", entityManager.find(FactoriesEntity.class, (waypOb.getFactoryId())).getLattitude());
                                                        waypmap.put("longitude", entityManager.find(FactoriesEntity.class, (waypOb.getFactoryId())).getLongtitude());
                                                        waypmap.put("appointment", entityManager.find(FactoriesEntity.class, waypOb.getFactoryId()).getAppointmentRequired());

                                                    } else {
                                                        waypmap.put("address", "Δεν έχει οριστεί σημείο φόρτωσης η εκφόρτωσης");
                                                        waypmap.put("brandName", "-");
                                                        waypmap.put("lattitude", "not available");
                                                        waypmap.put("longtitude", "not available");
                                                    }
                                                    waypmap.put("nestedScheduleIndicator", waypOb.getNestedScheduleIndicator());
                                                    waypmap.put("factoryId", waypOb.getFactoryId());
                                                    if (waypOb.getOfferScheduleBetweenWaypointId() != null) {
                                                        waypmap.put("finalStation", false);
                                                    } else {
                                                        if (waypOb.getNewWaypoint() != null && waypOb.getNewWaypoint() == 1) {
                                                            waypmap.put("finalStation", false);
                                                        } else {
                                                            waypmap.put("finalStation", true);
                                                        }
                                                    }
                                                    waypmap.put("offerScheduleBetweenWaypointId", waypOb.getOfferScheduleBetweenWaypointId());
                                                    if (waypOb.getNewWaypoint() != null && waypOb.getNewWaypoint() == 1) {
                                                        waypmap.put("newWaypoint", true);
                                                    }
                                                    String itemsEndFortSql =
                                                            "select * from orders_selections_by_point where order_id=" + os.getOrderId() +
                                                                    " and  order_schedule_id=" + waypOb.getOrderScheduleId() + " and order_waypoint_id =" + waypOb.getId() + " and type='Φόρτωση'";
                                                    List<OrdersSelectionsByPointEntity> itemsPackagesFortwshsEndiamesouShmeiou =
                                                            entityManager.createNativeQuery(itemsEndFortSql, OrdersSelectionsByPointEntity.class).getResultList();
                                                    List<HashMap<String, Object>> itemsPackagesFortwshsEndiamesouShmeiouFinal = new ArrayList<HashMap<String, Object>>();
                                                    for (OrdersSelectionsByPointEntity endshmFort : itemsPackagesFortwshsEndiamesouShmeiou) {
                                                        HashMap<String, Object> osbpMap = new HashMap<String, Object>();
                                                        osbpMap.put("title", endshmFort.getTitle());
                                                        osbpMap.put("quantity", endshmFort.getQuantity());
                                                        osbpMap.put("oldQuantity", endshmFort.getQuantity());
                                                        osbpMap.put("stackingType", endshmFort.getStackingType());
                                                        osbpMap.put("ldm", endshmFort.getLdm());
                                                        osbpMap.put("packageTypeId", endshmFort.getPackageTypeId());
                                                        osbpMap.put("typePackage", endshmFort.getTypePackage());
                                                        if (endshmFort.getPackageTypeId() != null) {
                                                            osbpMap.put("packageType", entityManager.find(PackageTypeEntity.class, endshmFort.getPackageTypeId()).getType());
                                                        } else {
                                                            osbpMap.put("packageType", "");
                                                        }
                                                        HashMap<String, Object> osbpMapS = new HashMap<String, Object>();
                                                        osbpMapS.put("selectedPackage", osbpMap);
                                                        osbpMapS.put("typePackage", endshmFort.getTypePackage());
                                                        osbpMapS.put("quantity", endshmFort.getQuantity());
                                                        osbpMapS.put("stackingType", endshmFort.getStackingType());
                                                        osbpMapS.put("oldQuantity", endshmFort.getQuantity());
                                                        osbpMapS.put("ldm", endshmFort.getLdm());
                                                        osbpMapS.put("packageTypeId", endshmFort.getPackageTypeId());
                                                        if (endshmFort.getPackageTypeId() != null) {
                                                            osbpMapS.put("packageType", entityManager.find(PackageTypeEntity.class, endshmFort.getPackageTypeId()).getType());
                                                        } else {
                                                            osbpMapS.put("packageType", "");
                                                        }
                                                        itemsPackagesFortwshsEndiamesouShmeiouFinal.add(osbpMapS);
                                                    }
                                                    waypmap.put("itemsPackagesFortwshsEndiamesouShmeiou", itemsPackagesFortwshsEndiamesouShmeiouFinal);
                                                    String itemsEndEkfortSql =
                                                            "select * from orders_selections_by_point where order_id=" + os.getOrderId() +
                                                                    " and  order_schedule_id=" + waypOb.getOrderScheduleId() + " and order_waypoint_id =" + waypOb.getId() + " and type='Εκφόρτωση'";
                                                    List<OrdersSelectionsByPointEntity> itemsPackagesEkfortwshsEndiamesouShmeiou =
                                                            entityManager.createNativeQuery(itemsEndEkfortSql, OrdersSelectionsByPointEntity.class).getResultList();
                                                    List<HashMap<String, Object>> itemsPackagesEkfortwshsEndiamesouShmeiouFinal = new ArrayList<HashMap<String, Object>>();
                                                    for (OrdersSelectionsByPointEntity endshmFort : itemsPackagesEkfortwshsEndiamesouShmeiou) {
                                                        HashMap<String, Object> osbpMap = new HashMap<String, Object>();
                                                        osbpMap.put("title", endshmFort.getTitle());
                                                        osbpMap.put("quantity", endshmFort.getQuantity());
                                                        osbpMap.put("stackingType", endshmFort.getStackingType());
                                                        osbpMap.put("ldm", endshmFort.getLdm());
                                                        osbpMap.put("oldQuantity", endshmFort.getQuantity());
                                                        osbpMap.put("packageTypeId", endshmFort.getPackageTypeId());
                                                        osbpMap.put("typePackage", endshmFort.getTypePackage());
                                                        if (endshmFort.getPackageTypeId() != null) {
                                                            osbpMap.put("packageType", entityManager.find(PackageTypeEntity.class, endshmFort.getPackageTypeId()).getType());
                                                        } else {
                                                            osbpMap.put("packageType", "");
                                                        }
                                                        HashMap<String, Object> osbpMapS = new HashMap<String, Object>();
                                                        osbpMapS.put("selectedPackage", osbpMap);
                                                        osbpMapS.put("quantity", endshmFort.getQuantity());
                                                        osbpMapS.put("oldQuantity", endshmFort.getQuantity());
                                                        osbpMapS.put("stackingType", endshmFort.getStackingType());
                                                        osbpMapS.put("ldm", endshmFort.getLdm());
                                                        osbpMapS.put("packageTypeId", endshmFort.getPackageTypeId());
                                                        osbpMapS.put("typePackage", endshmFort.getTypePackage());

                                                        if (endshmFort.getPackageTypeId() != null) {
                                                            osbpMapS.put("packageType", entityManager.find(PackageTypeEntity.class, endshmFort.getPackageTypeId()).getType());
                                                        } else {
                                                            osbpMapS.put("packageType", "");
                                                        }
                                                        itemsPackagesEkfortwshsEndiamesouShmeiouFinal.add(osbpMapS);
                                                    }
                                                    waypmap.put("itemsPackagesEkfortwshsEndiamesouShmeiou", itemsPackagesEkfortwshsEndiamesouShmeiouFinal);
                                                    String itemsEkfProorismou =
                                                            "select * from orders_selections_by_point where order_id=" + os.getOrderId() +
                                                                    " and  order_schedule_id=" + waypOb.getOrderScheduleId() + " and order_waypoint_id =" + waypOb.getId() + " and type='Εκφόρτωση Προορισμού'";
                                                    List<OrdersSelectionsByPointEntity> itemsEkfProorismouList =
                                                            entityManager.createNativeQuery(itemsEkfProorismou, OrdersSelectionsByPointEntity.class).getResultList();
                                                    List<HashMap<String, Object>> itemsEkfProorismouFinal = new ArrayList<HashMap<String, Object>>();

                                                    for (OrdersSelectionsByPointEntity endshmFort : itemsEkfProorismouList) {
                                                        HashMap<String, Object> osbpMap = new HashMap<String, Object>();
                                                        osbpMap.put("title", endshmFort.getTitle());
                                                        osbpMap.put("quantity", endshmFort.getQuantity());
                                                        osbpMap.put("oldQuantity", endshmFort.getQuantity());
                                                        osbpMap.put("stackingType", endshmFort.getStackingType());
                                                        osbpMap.put("ldm", endshmFort.getLdm());
                                                        osbpMap.put("packageTypeId", endshmFort.getPackageTypeId());
                                                        osbpMap.put("typePackage", endshmFort.getTypePackage());
                                                        if (endshmFort.getPackageTypeId() != null) {
                                                            osbpMap.put("packageType", entityManager.find(PackageTypeEntity.class, endshmFort.getPackageTypeId()).getType());
                                                        } else {
                                                            osbpMap.put("packageType", "");
                                                        }
                                                        HashMap<String, Object> osbpMapS = new HashMap<String, Object>();
                                                        osbpMapS.put("selectedPackage", osbpMap);
                                                        osbpMapS.put("quantity", endshmFort.getQuantity());
                                                        osbpMapS.put("oldQuantity", endshmFort.getQuantity());
                                                        osbpMapS.put("stackingType", endshmFort.getStackingType());
                                                        osbpMapS.put("ldm", endshmFort.getLdm());
                                                        osbpMapS.put("packageTypeId", endshmFort.getPackageTypeId());
                                                        osbpMapS.put("typePackage", endshmFort.getTypePackage());
                                                        if (endshmFort.getPackageTypeId() != null) {
                                                            osbpMapS.put("packageType", entityManager.find(PackageTypeEntity.class, endshmFort.getPackageTypeId()).getType());
                                                        } else {
                                                            osbpMapS.put("packageType", "");
                                                        }
                                                        itemsEkfProorismouFinal.add(osbpMapS);
                                                    }
                                                    waypmap.put("itemsPackagesEkfortwshsProorismou", itemsEkfProorismouFinal);
                                                    waypFinal.add(waypmap);
                                                }
                                                schedmap.put("waypointsList", waypFinal);
                                                schedulesList.add(schedmap);
                                            }
                                            sHmpam.put("schedulesList", schedulesList);
                                            String summSql = "select sum(final_unit_price) " +
                                                    "from order_package_schedules t " +
                                                    "where t.order_id=" + j.getId();

                                            Double sumSchedules = (Double) entityManager.
                                                    createNativeQuery(summSql).getSingleResult();
                                            if (sumSchedules != null) {
                                                sHmpam.put("sumSchedules", sumSchedules);
                                            } else {
                                                sHmpam.put("sumSchedules", "0.0");
                                            }
                                            sHmpam.put("offerId", offers);
                                            sHmpam.put("sender", j.getSender());
                                            sHmpam.put("customerId", j.getCustomerId());
                                            sHmpam.put("timologioCode", j.getTimologioCode());
                                            sHmpam.put("id", j.getId());
                                            sHmpam.put("orderId", j.getId());
                                            sHmpam.put("prepareForPdf", false);
                                            sHmpam.put("comments", j.getComments());
                                            sHmpam.put("factoryId", j.getFactoryId());
                                            sHmpam.put("generalInstructions", j.getGeneralInstructions());
                                            sHmpam.put("grossWeight", j.getGrossWeight());
                                            sHmpam.put("netWeight", j.getNetWeight());
                                            sHmpam.put("truckTemprature", j.getTruckTemprature());
                                            sHmpam.put("creationDate", j.getCreationDate());
                                            sHmpam.put("updateDate", j.getUpdateDate());
                                            sHmpam.put("childaddIndcator", false);
                                            sHmpam.put("status", j.getStatus());
                                            filalist.add(sHmpam);
                                        }
                                        String sqlOrdLoads = "select count(*) from orders ord   where 1=1 ";
                                        BigInteger allmyListCount = (BigInteger) entityManager.createNativeQuery(sqlOrdLoads).getSingleResult();
                                        returnList_future.put("data", filalist);
                                        returnList_future.put("total", filalistAll.size());
                                        returnList_future.put("allmyListCount", allmyListCount);
                                        returnList_future.put("status", "success");
                                        returnList_future.put("message", "success");
                                        return returnList_future;
                                    });
                        },
                        executionContext);
                returnList = getFuture.get();
                DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
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
    public Result getAllOrdersNoPagination(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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
                                        String id = json.findPath("id").asText();
                                        String customer = json.findPath("customer").asText();
                                        String sqlroles = "select * from orders b where 1=1 ";
                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                        List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                        if (!customer.equalsIgnoreCase("") && customer != null) {
                                            sqlroles += " and b.customer_id  in " +
                                                    " ( select id from  customers_suppliers cs where cs.brand_name like '%" + customer + "%' )";
                                        }
                                        if (id != null && !id.equalsIgnoreCase("")) {
                                            sqlroles += " and b.id like '%" + id + "%'";
                                        }
                                        List<OrdersEntity> orgsList
                                                = (List<OrdersEntity>) entityManager.createNativeQuery(
                                                sqlroles, OrdersEntity.class).getResultList();
                                        for (OrdersEntity j : orgsList) {
                                            HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                            sHmpam.put("id", j.getId());
                                            sHmpam.put("orderId", j.getId());
                                            String sqlOrdersSchedules = "select * from order_schedules ord_s where ord_s.order_id=" + j.getId()
                                                    + " and ord_s.primary_schedule=1 ";


                                            System.out.println(sqlOrdersSchedules);

                                            List<OrderSchedulesEntity> osList =
                                                    entityManager.createNativeQuery(sqlOrdersSchedules, OrderSchedulesEntity.class).getResultList();
                                            sHmpam.put("mainSchedule", osList.get(0).getFromCountry() + " " + osList.get(0).getFromCity() + "  /  "
                                                    + osList.get(0).getToCountry() + " " + osList.get(0).getToCity());
                                            CustomersSuppliersEntity cust = entityManager.find(CustomersSuppliersEntity.class, j.getCustomerId());
                                            sHmpam.put("brandName", cust.getBrandName());
                                            sHmpam.put("creationDate", j.getCreationDate());
                                            sHmpam.put("updateDate", j.getUpdateDate());
                                            sHmpam.put("id", j.getId());
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
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateOrderWaypoint(final Http.Request request) throws IOException {
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
                                Long waypointId = json.findPath("waypointId").asLong();
                                String warehouseId = json.findPath("warehouseId").asText();
                                String email = json.findPath("email").asText();
                                String country = json.findPath("country").asText();
                                String telephone = json.findPath("telephone").asText();
                                String brandName = json.findPath("brandName").asText();
                                String address = json.findPath("address").asText();
                                String city = json.findPath("city").asText();
                                String region = json.findPath("region").asText();
                                String postalCode = json.findPath("postalCode").asText();
                                String lattitude = json.findPath("lattitude").asText();
                                String longtitude = json.findPath("longtitude").asText();
                                OrderWaypointsEntity orderWaypointsEntity = entityManager.find(OrderWaypointsEntity.class, waypointId);

                                if (warehouseId != null && !warehouseId.equalsIgnoreCase("") && !warehouseId.equalsIgnoreCase("null")) {
                                    orderWaypointsEntity.setFactoryId(Long.valueOf(warehouseId));
                                } else {
                                    WarehousesEntity warehousesEntity = new WarehousesEntity();
                                    warehousesEntity.setBrandName(brandName);
                                    warehousesEntity.setAddress(address);
                                    warehousesEntity.setTelephone(telephone);
                                    warehousesEntity.setCountry(country);
                                    warehousesEntity.setEmail(email);
                                    warehousesEntity.setCity(city);
                                    warehousesEntity.setPostalCode(postalCode.replaceAll(" ", ""));
                                    warehousesEntity.setRegion(region);
                                    warehousesEntity.setLongitude(Double.valueOf(longtitude));
                                    warehousesEntity.setLatitude(Double.valueOf(lattitude));
                                    warehousesEntity.setCreationDate(new Date());
                                    entityManager.persist(warehousesEntity);
                                    orderWaypointsEntity.setFactoryId(warehousesEntity.getId());
                                }
                                entityManager.merge(orderWaypointsEntity);


                                add_result.put("status", "success");
                                add_result.put("message", "Η ενημέρωση του σημείου εκφόρτωσης πραγματοποιήθηκε με επιτυχία");
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


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateOrderFactory(final Http.Request request) throws IOException {
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
                                String factoryId = json.findPath("factoryId").asText();
                                Long orderId = json.findPath("orderId").asLong();
                                String city = json.findPath("city").asText();
                                String unloadingLoadingCode = json.findPath("unloadingLoadingCode").asText();
                                String country = json.findPath("country").asText();
                                String telephone = json.findPath("telephone").asText();
                                String email = json.findPath("email").asText();
                                String brandName = json.findPath("brandName").asText();
                                String address = json.findPath("address").asText();
                                String postalCode = json.findPath("postalCode").asText();
                                String region = json.findPath("region").asText();
                                String latitude = json.findPath("lattitude").asText();
                                String longitude = json.findPath("longtitude").asText();
                                OrdersEntity order = entityManager.find(OrdersEntity.class, orderId);
                                if (factoryId != null && !factoryId.equalsIgnoreCase("") && !factoryId.equalsIgnoreCase("null")) {
                                    order.setFactoryId(Long.valueOf(factoryId));
                                } else {
                                    FactoriesEntity factory = new FactoriesEntity();
                                    factory.setBrandName(brandName);
                                    factory.setUnloadingLoadingCode(unloadingLoadingCode);
                                    factory.setAddress(address);
                                    factory.setTelephone(telephone);
                                    factory.setCountry(country);
                                    factory.setEmail(email);
                                    factory.setCity(city);
                                    factory.setPostalCode(postalCode.replaceAll(" ", ""));
                                    factory.setRegion(region);
                                    factory.setLongtitude(Double.valueOf(longitude));
                                    factory.setLattitude(Double.valueOf(latitude));
                                    factory.setLattitude(Double.valueOf(latitude));
                                    factory.setAppointmentDays(0);
                                    factory.setAppointmentRequired((byte) 0);
                                    entityManager.persist(factory);
                                    order.setFactoryId(factory.getId());
                                }
                                entityManager.merge(order);
                                add_result.put("status", "success");
                                add_result.put("message", "Η ενημέρωση του σημείου φόρτωσης πραγματοποιήθηκε με επιτυχία");
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


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteOrder(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                Long orderId = json.findPath("orderId").asLong();
                                OrdersEntity order = entityManager.find(OrdersEntity.class, orderId);

                                String orderPack = "select * from order_packages op where op.order_id=" + orderId;
                                List<OrderPackagesEntity> orderPackagesEntityList = entityManager.createNativeQuery(orderPack, OrderPackagesEntity.class).getResultList();
                                for (OrderPackagesEntity op : orderPackagesEntityList) {
                                    entityManager.remove(op);
                                }

                                String ordersScheduleSql = "select * from order_schedules os where os.order_id=" + orderId;
                                List<OrderSchedulesEntity> orderSchedulesEntityList = entityManager.createNativeQuery(ordersScheduleSql, OrderSchedulesEntity.class).getResultList();
                                for (OrderSchedulesEntity ose : orderSchedulesEntityList) {
                                    entityManager.remove(ose);
                                }
                                String sqlDv =
                                        "select * " +
                                                "from order_distinct_item " +
                                                "where order_id=" + orderId;
                                List<OrderDistinctItemEntity> orderDistinctItemEntitiesList =
                                        entityManager.createNativeQuery(sqlDv,
                                                OrderDistinctItemEntity.class).getResultList();
                                for (OrderDistinctItemEntity odv : orderDistinctItemEntitiesList) {
                                    entityManager.remove(odv);
                                }
                                String sqlPackagesByPoints =
                                        "select * " +
                                                "from orders_selections_by_point " +
                                                "where order_id=" + orderId;
                                List<OrdersSelectionsByPointEntity> ordersSelectionsByPointEntityList =
                                        entityManager.createNativeQuery(sqlPackagesByPoints,
                                                OrdersSelectionsByPointEntity.class).getResultList();
                                for (OrdersSelectionsByPointEntity osp : ordersSelectionsByPointEntityList) {
                                    entityManager.remove(osp);
                                }
                                String sqlOrderPackages =
                                        "select * " +
                                                "from  order_package_schedules ops " +
                                                "where  ops.order_id=" + orderId;
                                List<OrderPackageSchedulesEntity> orderPackageSchedulesEntityList =
                                        entityManager.createNativeQuery(sqlOrderPackages,
                                                OrderPackageSchedulesEntity.class).getResultList();
                                for (OrderPackageSchedulesEntity ops : orderPackageSchedulesEntityList) {
                                    entityManager.remove(ops);
                                }
                                String sqlWp =
                                        "select * " +
                                                "from order_waypoints owp " +
                                                "where owp.order_id=" + orderId;
                                List<OrderWaypointsEntity> orderWaypointsEntityList =
                                        entityManager.createNativeQuery(sqlWp,
                                                OrderWaypointsEntity.class).getResultList();
                                for (OrderWaypointsEntity owp : orderWaypointsEntityList) {
                                    entityManager.remove(owp);
                                }
                                String sqlWpack = "" +
                                        "select * " +
                                        "from order_waypoints_packages owpack " +
                                        "where owpack.order_id=" + orderId;
                                List<OrderWaypointsPackagesEntity> waypointsPackagesEntityList =
                                        entityManager.createNativeQuery(sqlWpack,
                                                OrderWaypointsPackagesEntity.class).getResultList();
                                for (OrderWaypointsPackagesEntity wpack : waypointsPackagesEntityList) {
                                    entityManager.remove(wpack);
                                }

                                entityManager.remove(order);
                                add_result.put("status", "success");
                                add_result.put("message", "Η διαγραφή της παραγγελίας πραγματοποιήθηκε με επιτυχία");
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
    public Result updateOrder(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();

                                Long cveid = Long.valueOf(1);
                                CoreVariablesEntity cve = entityManager.find(CoreVariablesEntity.class, cveid);

                                String user_id = json.findPath("user_id").asText();
                                Long orderId = json.findPath("orderId").asLong();
                                String generalInstructions = json.findPath("generalInstructions").asText();
                                Double netWeight = json.findPath("netWeight").asDouble();
                                Double grossWeight = json.findPath("grossWeight").asDouble();
                                String truckTemprature = json.findPath("truckTemprature").asText();
                                String sender = json.findPath("sender").asText();
                                String timologioCode = json.findPath("timologioCode").asText();
                                String status = json.findPath("status").asText();
                                String comments = json.findPath("orders_comments").asText();//      orders_comments: this.comments,
                                String sqlPackagesByPoints =
                                        "select * " +
                                                "from orders_selections_by_point " +
                                                "where order_id=" + orderId;
                                List<OrdersSelectionsByPointEntity> ordersSelectionsByPointEntityList =
                                        entityManager.createNativeQuery(sqlPackagesByPoints,
                                                OrdersSelectionsByPointEntity.class).getResultList();
                                for (OrdersSelectionsByPointEntity osp : ordersSelectionsByPointEntityList) {
                                    entityManager.remove(osp);
                                }
                                String sqlDv =
                                        "select * " +
                                                "from order_distinct_item " +
                                                "where order_id=" + orderId;
                                List<OrderDistinctItemEntity> orderDistinctItemEntitiesList =
                                        entityManager.createNativeQuery(sqlDv,
                                                OrderDistinctItemEntity.class).getResultList();
                                for (OrderDistinctItemEntity odv : orderDistinctItemEntitiesList) {
                                    entityManager.remove(odv);
                                }
                                String sqlOrderPackages =
                                        "select * " +
                                                "from  order_package_schedules ops " +
                                                "where  ops.order_id=" + orderId;
                                List<OrderPackageSchedulesEntity> orderPackageSchedulesEntityList =
                                        entityManager.createNativeQuery(sqlOrderPackages,
                                                OrderPackageSchedulesEntity.class).getResultList();
                                for (OrderPackageSchedulesEntity ops : orderPackageSchedulesEntityList) {
                                    entityManager.remove(ops);
                                }
                                String sqlWp =
                                        "select * " +
                                                "from order_waypoints owp " +
                                                "where owp.order_id=" + orderId;
                                List<OrderWaypointsEntity> orderWaypointsEntityList =
                                        entityManager.createNativeQuery(sqlWp,
                                                OrderWaypointsEntity.class).getResultList();
                                for (OrderWaypointsEntity owp : orderWaypointsEntityList) {
                                    entityManager.remove(owp);
                                }
                                String sqlWpack = "" +
                                        "select * " +
                                        "from order_waypoints_packages owpack " +
                                        "where owpack.order_id=" + orderId;
                                List<OrderWaypointsPackagesEntity> waypointsPackagesEntityList =
                                        entityManager.createNativeQuery(sqlWpack,
                                                OrderWaypointsPackagesEntity.class).getResultList();
                                for (OrderWaypointsPackagesEntity wpack : waypointsPackagesEntityList) {
                                    entityManager.remove(wpack);
                                }
                                OrdersEntity ordersEntity = entityManager.find(OrdersEntity.class, orderId);
                                if (generalInstructions != null && !generalInstructions.equalsIgnoreCase("null")) {
                                    ordersEntity.setGeneralInstructions(generalInstructions);
                                }

                                if (netWeight != null) {
                                    ordersEntity.setNetWeight(netWeight);
                                }

                                if (grossWeight != null) {
                                    ordersEntity.setGrossWeight(grossWeight);
                                }

                                if (truckTemprature != null && !truckTemprature.equalsIgnoreCase("null")) {
                                    ordersEntity.setTruckTemprature(truckTemprature);
                                }
                                if (sender != null && !sender.equalsIgnoreCase("null")) {
                                    ordersEntity.setSender(sender);
                                }
                                if (comments != null && !comments.equalsIgnoreCase("null")) {
                                    ordersEntity.setComments(comments);
                                }

                                if (timologioCode != null && !timologioCode.equalsIgnoreCase("null")) {
                                    ordersEntity.setTimologioCode(timologioCode);
                                }
                                ordersEntity.setStatus(status);
                                entityManager.merge(ordersEntity);
                                JsonNode finalTimeline = json.findPath("finalTimeline");
                                Iterator fintIt = finalTimeline.iterator();
                                while (fintIt.hasNext()) {
                                    JsonNode schedule = Json.toJson(fintIt.next());
                                    ((ObjectNode) schedule).remove("factory");
                                    String factoryId = schedule.findPath("factoryId").asText();//
                                    String timeToArrive = schedule.findPath("timeToArrive").asText();
                                    Integer appointment = schedule.findPath("appointment").asInt();
                                    String appointmentDay = schedule.findPath("appointmentDay").asText();
                                    String truckLoadingCode = schedule.findPath("truckLoadingCode").asText();
                                    String offerScheduleBetweenWaypointId = schedule.findPath("offerScheduleBetweenWaypointId").asText();
                                    if (schedule.findPath("timelinetype").asText().equalsIgnoreCase("Αφετηρία")) {
                                        OrderSchedulesEntity ordS = entityManager.find(OrderSchedulesEntity.class, schedule.findPath("orderScheduleId").asLong());
                                        if (factoryId != null && !factoryId.equalsIgnoreCase("") && !factoryId.equalsIgnoreCase("null")) {
                                            ordS.setFactoryId(schedule.findPath("factoryId").asLong());
                                            FactoriesEntity factory = entityManager.find(FactoriesEntity.class, ordS.getFactoryId());
                                            ordS.setFromAddress(factory.getAddress());
                                            entityManager.merge(ordS);
                                        }
                                        ordS.setAppointment(appointment);
                                        ordS.setTimeToArrive(timeToArrive);
                                        DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                        if (appointmentDay != null && !appointmentDay.equalsIgnoreCase("")) {
                                            try {
                                                Date appointmentDayDate = myDateFormat.parse(appointmentDay);
                                                ordS.setAppointmentDay(appointmentDayDate);
                                                ordS.setAppointmentDayLoad(appointmentDayDate);
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        if (truckLoadingCode != null && !truckLoadingCode.equalsIgnoreCase("") && !truckLoadingCode.equalsIgnoreCase("null")) {
                                            ordS.setTruckLoadingCode(truckLoadingCode);
                                        }
                                        JsonNode orderPackageScheduleList = schedule.findPath("orderPackageScheduleList");
                                        Iterator orderPackIt = orderPackageScheduleList.iterator();
                                        while (orderPackIt.hasNext()) {
                                            JsonNode ordPjsonode = Json.toJson(orderPackIt.next());
                                            ((ObjectNode) ordPjsonode).remove("distinctDistancesPackageList");
                                            JsonNode distanceValues = ordPjsonode.findPath("distinctItem").findPath("distanceValues");
                                            ((ObjectNode) ordPjsonode).remove("distinctItem");
                                            Double finalUnitPrice = ordPjsonode.findPath("finalUnitPrice").asDouble();
                                            Long orderScheduleId = ordPjsonode.findPath("orderScheduleId").asLong();
                                            Integer quantity = ordPjsonode.findPath("quantity").asInt();
                                            String title = ordPjsonode.findPath("title").asText();
                                            Double unitPrice = ordPjsonode.findPath("unitPrice").asDouble();
                                            OrderPackageSchedulesEntity orderSched = new OrderPackageSchedulesEntity();
                                            orderSched.setCreationDate(new Date());
                                            orderSched.setFinalUnitPrice(finalUnitPrice);
                                            orderSched.setOrderId(orderId);
                                            orderSched.setOrderScheduleId(orderScheduleId);
                                            orderSched.setQuantity(quantity);
                                            orderSched.setTitle(title);
                                            orderSched.setUnitPrice(unitPrice);
                                            entityManager.persist(orderSched);
                                            Iterator distinctItemIter = distanceValues.iterator();
                                            while (distinctItemIter.hasNext()) {
                                                JsonNode distJson = Json.toJson(distinctItemIter.next());
                                                Double unitPriceDv = distJson.findPath("unitPrice").asDouble();
                                                Integer from = distJson.findPath("from").asInt();
                                                Integer to = distJson.findPath("to").asInt();
                                                String typePackage = distJson.findPath("typePackage").asText();
                                                OrderDistinctItemEntity odv = new OrderDistinctItemEntity();
                                                odv.setFromUnit(from);
                                                odv.setTypePackage(typePackage);
                                                odv.setToUnit(to);
                                                odv.setTitle(title);
                                                odv.setUnitPrice(unitPriceDv);
                                                odv.setOrderId(orderId);
                                                odv.setOrderPackageId(orderSched.getId());
                                                odv.setOrderScheduleId(ordS.getId());
                                                entityManager.persist(odv);
                                            }
                                        }
                                        JsonNode itemsPackagesAfethrias = schedule.findPath("itemsPackagesAfethrias");
                                        Iterator itemsAfethrias = itemsPackagesAfethrias.iterator();
                                        while (itemsAfethrias.hasNext()) {
                                            JsonNode itemsAfethriasNode = Json.toJson(itemsAfethrias.next());
                                            String title = itemsAfethriasNode.findPath("selectedPackage").findPath("title").asText();
                                            String typePackage = itemsAfethriasNode.findPath("selectedPackage").findPath("typePackage").asText();
                                            ((ObjectNode) itemsAfethriasNode).remove("selectedPackage");
                                            String stackingType = itemsAfethriasNode.findPath("stackingType").asText();
                                            Integer quantity = itemsAfethriasNode.findPath("quantity").asInt();
                                            Double unitPrice = itemsAfethriasNode.findPath("unitPrice").asDouble();
                                            Integer packageTypeId = itemsAfethriasNode.findPath("packageTypeId").asInt();
                                            OrdersSelectionsByPointEntity selections = new OrdersSelectionsByPointEntity();
                                            selections.setOrderId(orderId);
                                            selections.setTitle(title);
                                            selections.setTypePackage(typePackage);
                                            if (packageTypeId != 0) {
                                                selections.setPackageTypeId(packageTypeId);
                                            }
                                            selections.setOrderScheduleId(ordS.getId());
                                            selections.setType("Φόρτωση");
                                            selections.setStackingType(stackingType);
                                            selections.setQuantity(quantity);
                                            selections.setUnitPrice(unitPrice);
                                            if (title.equalsIgnoreCase("")) {
                                                add_result.put("status", "warning");
                                                add_result.put("message", "Δεν έχετε επιλέξει συσκευασία " +
                                                        "για φόρτωση στην αφετηρία " + ordS.getToCountry() + " " + ordS.getFromCity() + "," + ordS.getFromPostalCode());
                                                return add_result;
                                            }
                                            if (packageTypeId == 0) {
                                                add_result.put("status", "warning");
                                                add_result.put("message", "Δεν έχετε επιλέξει Τύπος πρ/ντος " +
                                                        "για φόρτωση στην αφετηρία " + ordS.getToCountry() + " " + ordS.getFromCity() + "," + ordS.getFromPostalCode());
                                                return add_result;
                                            }
                                            if (stackingType.equalsIgnoreCase("")) {
                                                add_result.put("status", "warning");
                                                add_result.put("message", "Δεν έχετε επιλέξει είδος Φόρτωσης " +
                                                        "για φόρτωση στην αφετηρία " + ordS.getToCountry() + " " + ordS.getFromCity() + "," + ordS.getFromPostalCode());
                                                return add_result;
                                            }
                                            String sqlMu = "select * from measurement_unit mu where mu.title='" + title + "'";
                                            List<MeasurementUnitEntity> muE = entityManager.createNativeQuery(sqlMu, MeasurementUnitEntity.class).getResultList();
                                            MeasurementUnitEntity mue = entityManager.find(MeasurementUnitEntity.class, muE.get(0).getId());
                                            Double ldm = 0.0;
                                            ldm = calculateLdm(stackingType, mue, quantity, mue.getStackingFactor());
                                            selections.setLdm(ldm);
                                            selections.setMeasureUnitId(muE.get(0).getId());
                                            entityManager.persist(selections);
                                        }
                                    } else {
                                        String newWaypoint = schedule.findPath("newWaypoint").asText();
                                        OrderWaypointsEntity orderWaypointsPackagesEntity = new OrderWaypointsEntity();
                                        orderWaypointsPackagesEntity.setCreationDate(new Date());
                                        if (factoryId != null && !factoryId.equalsIgnoreCase("") && !factoryId.equalsIgnoreCase("null")) {
                                            orderWaypointsPackagesEntity.setFactoryId(schedule.findPath("factoryId").asLong());
                                            FactoriesEntity factory = entityManager.find(FactoriesEntity.class, orderWaypointsPackagesEntity.getFactoryId());
                                            orderWaypointsPackagesEntity.setAddress(factory.getAddress());
                                        }
                                        orderWaypointsPackagesEntity.setAppointment(appointment);
                                        orderWaypointsPackagesEntity.setTimeToArrive(timeToArrive);
                                        DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                        if (appointmentDay != null && !appointmentDay.equalsIgnoreCase("")) {
                                            try {
                                                Date appointmentDayDate = myDateFormat.parse(appointmentDay);
                                                orderWaypointsPackagesEntity.setAppointmentDay(appointmentDayDate);
                                                orderWaypointsPackagesEntity.setAppointmentDayLoad(appointmentDayDate);
                                                //setAppointmentDayLoad
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        if (truckLoadingCode != null && !truckLoadingCode.equalsIgnoreCase("") && !truckLoadingCode.equalsIgnoreCase("null")) {
                                            orderWaypointsPackagesEntity.setTruckLoadingCode(truckLoadingCode);
                                        }
                                        orderWaypointsPackagesEntity.setOrderId(orderId);
                                        orderWaypointsPackagesEntity.setCity(schedule.findPath("city").asText());
                                        orderWaypointsPackagesEntity.setCountry(schedule.findPath("country").asText());
                                        orderWaypointsPackagesEntity.setPostalCode(schedule.findPath("postalCode").asText().replaceAll(" ", ""));
                                        orderWaypointsPackagesEntity.setOrderScheduleId(schedule.findPath("orderScheduleId").asLong());
                                        orderWaypointsPackagesEntity.setNestedScheduleIndicator(schedule.findPath("nestedScheduleIndicator").asInt());
                                        if (offerScheduleBetweenWaypointId != null && !offerScheduleBetweenWaypointId.equalsIgnoreCase("")
                                                && !offerScheduleBetweenWaypointId.equalsIgnoreCase("null")) {
                                            orderWaypointsPackagesEntity.setOfferScheduleBetweenWaypointId(schedule.findPath("offerScheduleBetweenWaypointId").asLong());
                                        }
                                        if (newWaypoint != null && !newWaypoint.equalsIgnoreCase("null") && !newWaypoint.equalsIgnoreCase("")) {
                                            orderWaypointsPackagesEntity.setNewWaypoint(1);
                                        }
                                        entityManager.persist(orderWaypointsPackagesEntity);
                                        JsonNode itemsPackagesEndiamesou = schedule.findPath("itemsPackagesFortwshsEndiamesouShmeiou");
                                        Iterator itemsPackagesEndiamesouIter = itemsPackagesEndiamesou.iterator();
                                        while (itemsPackagesEndiamesouIter.hasNext()) {
                                            JsonNode itemsPackagesEndiamesouNode = Json.toJson(itemsPackagesEndiamesouIter.next());
                                            String title = itemsPackagesEndiamesouNode.findPath("selectedPackage").findPath("title").asText();
                                            String typePackage = itemsPackagesEndiamesouNode.findPath("selectedPackage").findPath("typePackage").asText();
                                            ((ObjectNode) itemsPackagesEndiamesouNode).remove("selectedPackage");
                                            String stackingType = itemsPackagesEndiamesouNode.findPath("stackingType").asText();
                                            Integer quantity = itemsPackagesEndiamesouNode.findPath("quantity").asInt();
                                            Integer packageTypeId = itemsPackagesEndiamesouNode.findPath("packageTypeId").asInt();
                                            OrdersSelectionsByPointEntity selections = new OrdersSelectionsByPointEntity();
                                            selections.setOrderId(orderId);
                                            selections.setTitle(title);
                                            selections.setTypePackage(typePackage);
                                            selections.setOrderScheduleId(orderWaypointsPackagesEntity.getOrderScheduleId());
                                            selections.setOrderWaypointId(orderWaypointsPackagesEntity.getId());
                                            selections.setType("Φόρτωση");
                                            selections.setQuantity(quantity);
                                            selections.setStackingType(stackingType);
                                            if (packageTypeId != 0) {
                                                selections.setPackageTypeId(packageTypeId);
                                            }
                                            if (title.equalsIgnoreCase("")) {
                                                add_result.put("status", "warning");
                                                add_result.put("message", "Δεν έχετε επιλέξει συσκευασία για φόρτωση στην αφετηρία "
                                                        + orderWaypointsPackagesEntity.getCountry() + " "
                                                        + orderWaypointsPackagesEntity.getCity() + "," + orderWaypointsPackagesEntity.getPostalCode());
                                                return add_result;
                                            }
                                            if (packageTypeId == 0) {
                                                add_result.put("status", "warning");
                                                add_result.put("message", "Δεν έχετε επιλέξει Τύπος πρ/ντος " +
                                                        "για φόρτωση στην αφετηρία " + orderWaypointsPackagesEntity.getCountry() + " " + orderWaypointsPackagesEntity.getCity()
                                                        + "," + orderWaypointsPackagesEntity.getPostalCode());
                                                return add_result;
                                            }
                                            if (stackingType.equalsIgnoreCase("")) {
                                                add_result.put("status", "warning");
                                                add_result.put("message", "Δεν έχετε επιλέξει είδος Φόρτωσης " +
                                                        "για φόρτωση στην αφετηρία " + orderWaypointsPackagesEntity.getCountry() + " " + orderWaypointsPackagesEntity.getCity()
                                                        + "," + orderWaypointsPackagesEntity.getPostalCode());
                                                return add_result;
                                            }

                                            String sqlMu = "select * from measurement_unit mu where mu.title='" + title + "'";
                                            List<MeasurementUnitEntity> muE = entityManager.createNativeQuery(sqlMu, MeasurementUnitEntity.class).getResultList();
                                            MeasurementUnitEntity mue = entityManager.find(MeasurementUnitEntity.class, muE.get(0).getId());
                                            Double ldm = 0.0;
                                            ldm = calculateLdm(stackingType, mue, quantity, mue.getStackingFactor());
                                            selections.setLdm(ldm);
                                            selections.setMeasureUnitId(muE.get(0).getId());
                                            entityManager.persist(selections);
                                        }
                                        JsonNode itemsPackagesEndiamesouEkfortwsh = schedule.findPath("itemsPackagesEkfortwshsEndiamesouShmeiou");
                                        Iterator itemsPackagesEkfortwshsEndiamesouShmeiouIter = itemsPackagesEndiamesouEkfortwsh.iterator();
                                        while (itemsPackagesEkfortwshsEndiamesouShmeiouIter.hasNext()) {
                                            JsonNode itemEkfortwshs = Json.toJson(itemsPackagesEkfortwshsEndiamesouShmeiouIter.next());
                                            String title = itemEkfortwshs.findPath("selectedPackage").findPath("title").asText();
                                            String typePackage = itemEkfortwshs.findPath("selectedPackage").findPath("typePackage").asText();
                                            ((ObjectNode) itemEkfortwshs).remove("selectedPackage");
                                            String stackingType = itemEkfortwshs.findPath("stackingType").asText();
                                            Integer quantity = itemEkfortwshs.findPath("quantity").asInt();
                                            Integer packageTypeId = itemEkfortwshs.findPath("packageTypeId").asInt();
                                            OrdersSelectionsByPointEntity selections = new OrdersSelectionsByPointEntity();
                                            selections.setOrderId(orderId);
                                            selections.setTitle(title);
                                            selections.setTypePackage(typePackage);
                                            selections.setOrderScheduleId(orderWaypointsPackagesEntity.getOrderScheduleId());
                                            selections.setOrderWaypointId(orderWaypointsPackagesEntity.getId());
                                            selections.setType("Εκφόρτωση");
                                            selections.setQuantity(quantity);
                                            selections.setStackingType(stackingType);
                                            if (packageTypeId != 0) {
                                                selections.setPackageTypeId(packageTypeId);
                                            }
                                            if (title.equalsIgnoreCase("")) {
                                                add_result.put("status", "warning");
                                                add_result.put("message", "Δεν έχετε επιλέξει συσκευασία για φόρτωση στην αφετηρία "
                                                        + orderWaypointsPackagesEntity.getCountry() + " "
                                                        + orderWaypointsPackagesEntity.getCity() + "," + orderWaypointsPackagesEntity.getPostalCode());
                                                return add_result;
                                            }
                                            if (packageTypeId == 0) {
                                                add_result.put("status", "warning");
                                                add_result.put("message", "Δεν έχετε επιλέξει Τύπος πρ/ντος " +
                                                        "για φόρτωση στην αφετηρία " + orderWaypointsPackagesEntity.getCountry() + " " + orderWaypointsPackagesEntity.getCity()
                                                        + "," + orderWaypointsPackagesEntity.getPostalCode());
                                                return add_result;
                                            }
                                            if (stackingType.equalsIgnoreCase("")) {
                                                add_result.put("status", "warning");
                                                add_result.put("message", "Δεν έχετε επιλέξει είδος Φόρτωσης " +
                                                        "για φόρτωση στην αφετηρία " + orderWaypointsPackagesEntity.getCountry() + " " + orderWaypointsPackagesEntity.getCity()
                                                        + "," + orderWaypointsPackagesEntity.getPostalCode());
                                                return add_result;
                                            }
                                            String sqlMu = "select * from measurement_unit mu where mu.title='" + title + "'";
                                            List<MeasurementUnitEntity> muE = entityManager.createNativeQuery(sqlMu, MeasurementUnitEntity.class).getResultList();
                                            MeasurementUnitEntity mue = entityManager.find(MeasurementUnitEntity.class, muE.get(0).getId());
                                            Double ldm = 0.0;
                                            ldm = calculateLdm(stackingType, mue, quantity, mue.getStackingFactor());
                                            selections.setLdm(ldm);
                                            selections.setMeasureUnitId(muE.get(0).getId());
                                            entityManager.persist(selections);
                                        }
                                        JsonNode itemsPackagesEkfortwshsProorismou = schedule.findPath("itemsPackagesEkfortwshsProorismou");
                                        Iterator itemsPackagesEkfortwshsProorismouIter = itemsPackagesEkfortwshsProorismou.iterator();
                                        while (itemsPackagesEkfortwshsProorismouIter.hasNext()) {
                                            JsonNode itemEkfortwshsPr = Json.toJson(itemsPackagesEkfortwshsProorismouIter.next());
                                            String title = itemEkfortwshsPr.findPath("selectedPackage").findPath("title").asText();
                                            String typePackage = itemEkfortwshsPr.findPath("selectedPackage").findPath("typePackage").asText();
                                            ((ObjectNode) itemEkfortwshsPr).remove("selectedPackage");
                                            String stackingType = itemEkfortwshsPr.findPath("stackingType").asText();
                                            Integer quantity = itemEkfortwshsPr.findPath("quantity").asInt();
                                            Integer packageTypeId = itemEkfortwshsPr.findPath("packageTypeId").asInt();
                                            OrdersSelectionsByPointEntity selections = new OrdersSelectionsByPointEntity();
                                            selections.setOrderId(orderId);
                                            selections.setTypePackage(typePackage);
                                            selections.setTitle(title);
                                            if (packageTypeId != 0) {
                                                selections.setPackageTypeId(packageTypeId);
                                            }
                                            selections.setOrderScheduleId(orderWaypointsPackagesEntity.getOrderScheduleId());
                                            selections.setOrderWaypointId(orderWaypointsPackagesEntity.getId());
                                            selections.setType("Εκφόρτωση Προορισμού");
                                            selections.setStackingType(stackingType);
                                            selections.setQuantity(quantity);
                                            if (title.equalsIgnoreCase("")) {
                                                add_result.put("status", "warning");
                                                add_result.put("message", "Δεν έχετε επιλέξει συσκευασία για φόρτωση στην αφετηρία "
                                                        + orderWaypointsPackagesEntity.getCountry() + " "
                                                        + orderWaypointsPackagesEntity.getCity() + "," + orderWaypointsPackagesEntity.getPostalCode());
                                                return add_result;
                                            }
                                            if (packageTypeId == 0) {
                                                add_result.put("status", "warning");
                                                add_result.put("message", "Δεν έχετε επιλέξει Τύπος πρ/ντος " +
                                                        "για φόρτωση στην αφετηρία " + orderWaypointsPackagesEntity.getCountry() + " " + orderWaypointsPackagesEntity.getCity() + "," + orderWaypointsPackagesEntity.getPostalCode());
                                                return add_result;
                                            }
                                            if (stackingType.equalsIgnoreCase("")) {
                                                add_result.put("status", "warning");
                                                add_result.put("message", "Δεν έχετε επιλέξει είδος Φόρτωσης " +
                                                        "για φόρτωση στην αφετηρία " + orderWaypointsPackagesEntity.getCountry() + " " + orderWaypointsPackagesEntity.getCity() + "," + orderWaypointsPackagesEntity.getPostalCode());
                                                return add_result;
                                            }

                                            String sqlMu = "select * from measurement_unit mu where mu.title='" + title + "'";
                                            List<MeasurementUnitEntity> muE = entityManager.createNativeQuery(sqlMu, MeasurementUnitEntity.class).getResultList();
                                            MeasurementUnitEntity mue = entityManager.find(MeasurementUnitEntity.class, muE.get(0).getId());
                                            Double ldm = 0.0;
                                            ldm = calculateLdm(stackingType, mue, quantity, mue.getStackingFactor());
                                            selections.setLdm(ldm);
                                            selections.setMeasureUnitId(muE.get(0).getId());
                                            entityManager.persist(selections);
                                        }
                                    }
                                }
                                add_result.put("status", "success");
                                add_result.put("user_id", user_id);
                                add_result.put("DO_ID", ordersEntity.getId());
                                add_result.put("message", "Η ενημέρωση της " + "παραγγελίας πραγματοποιήθηκε με επιτυχία");
                                add_result.put("system", "ΠΑΡΑΓΓΕΛΙΕΣ");
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


    private double calculateLdm(String stackingType, MeasurementUnitEntity mue, Integer quantity, Double stackingFactor) {
        Double ldm = 0.0;
        DecimalFormat df = new DecimalFormat("###.#");

        if (stackingType.equalsIgnoreCase("Μη Στοιβάσιμη")) {
            ldm = ((mue.getxIndex() * mue.getzIndex()) / (2.4)) * quantity;
        } else {
            if (quantity % 2 == 1) {//tote monos
                Double unique = ((mue.getxIndex() * mue.getzIndex()) / (2.4));
                ldm = ((((mue.getxIndex() * mue.getzIndex()) / 2.4)) / stackingFactor) * (quantity - 1) + unique;
            } else {
                ldm = ((((mue.getxIndex() * mue.getzIndex()) / 2.4)) / stackingFactor) * quantity;
            }
        }
        ldm = Double.valueOf(df.format(ldm));
        return ldm;
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getOrderWaypoints(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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
                                        String orderId = json.findPath("orderId").asText();
                                        String waypointId = json.findPath("waypointId").asText();
                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                        String sqlWaypoints = "select * from order_waypoints op where op.order_id=" + orderId;
                                        if (waypointId != null && !waypointId.equalsIgnoreCase("")) {
                                            sqlWaypoints += " and op.id=" + waypointId;
                                        }
                                        List<OrderWaypointsEntity> orderWaypointsEntityList = entityManager.createNativeQuery(sqlWaypoints, OrderWaypointsEntity.class).getResultList();
                                        List<HashMap<String, Object>> orderWaypointsfinalList = new ArrayList<HashMap<String, Object>>();
                                        for (OrderWaypointsEntity op : orderWaypointsEntityList) {
                                            HashMap<String, Object> waypoint = new HashMap<String, Object>();
                                            HashMap<String, Object> addressMap = new HashMap<String, Object>();
                                            if (op.getFactoryId() != null) {
                                                addressMap.put("address", entityManager.find(WarehousesEntity.class, op.getFactoryId()).getAddress());
                                                addressMap.put("brandName", entityManager.find(WarehousesEntity.class, op.getFactoryId()).getBrandName());
                                                addressMap.put("telephone", entityManager.find(WarehousesEntity.class, op.getFactoryId()).getTelephone());
                                                addressMap.put("city", entityManager.find(WarehousesEntity.class, op.getFactoryId()).getCity());
                                                addressMap.put("country", entityManager.find(WarehousesEntity.class, op.getFactoryId()).getCountry());
                                                addressMap.put("email", entityManager.find(WarehousesEntity.class, op.getFactoryId()).getEmail());
                                                addressMap.put("postalCode", entityManager.find(WarehousesEntity.class, op.getFactoryId()).getPostalCode());
                                                addressMap.put("lattitude", entityManager.find(WarehousesEntity.class, op.getFactoryId()).getLatitude());
                                                addressMap.put("longtitude", entityManager.find(WarehousesEntity.class, op.getFactoryId()).getLongitude());
                                            } else {
                                                addressMap.put("address", "");
                                                addressMap.put("brandName", "");
                                                addressMap.put("telephone", "");
                                                addressMap.put("city", op.getCity());
                                                addressMap.put("country", op.getCountry());
                                                addressMap.put("email", "");
                                                addressMap.put("postalCode", op.getPostalCode());
                                                addressMap.put("lattitude", "");
                                                addressMap.put("longtitude", "");
                                            }
                                            waypoint.put("addressMap", addressMap);
                                            waypoint.put("factoryId", op.getFactoryId());
                                            waypoint.put("creationDate", op.getCreationDate());
                                            waypoint.put("id", op.getId());
                                            waypoint.put("waypointId", op.getId());
                                            waypoint.put("factoryId", op.getFactoryId());
                                            String sqlWpPackg = "select * from order_waypoints_packages owp where owp.order_waypoint_id=" + op.getId();
                                            List<OrderWaypointsPackagesEntity> orderWaypointsPackages = entityManager.createNativeQuery(sqlWpPackg, OrderWaypointsPackagesEntity.class).getResultList();
                                            List<HashMap<String, Object>> selectedStratPointPackages = new ArrayList<HashMap<String, Object>>();
                                            for (OrderWaypointsPackagesEntity opack : orderWaypointsPackages) {
                                                HashMap<String, Object> packageWp = new HashMap<String, Object>();
                                                packageWp.put("measureUnitId", opack.getMeasureUnitId());
                                                packageWp.put("title", opack.getTitle());
                                                packageWp.put("orderId", opack.getOrderId());
                                                packageWp.put("quantity", opack.getQuantity());
                                                packageWp.put("unitPrice", opack.getUnitPrice());
                                                packageWp.put("finalUnitPrice", opack.getFinalUnitPrice());
                                                packageWp.put("oldQuantity", opack.getQuantity());
                                                packageWp.put("oldUnitPrice", opack.getUnitPrice());
                                                packageWp.put("oldFinalUnitPrice", opack.getFinalUnitPrice());
                                                selectedStratPointPackages.add(packageWp);
                                            }
                                            waypoint.put("selectedWayPointPackages", selectedStratPointPackages);
                                            orderWaypointsfinalList.add(waypoint);
                                        }
                                        returnList_future.put("data", orderWaypointsfinalList);
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