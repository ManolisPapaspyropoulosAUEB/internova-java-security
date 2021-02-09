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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
                                Long offerId = json.findPath("offerId").asLong();
                                JsonNode data = json.findPath("data");
                                ((ObjectNode) json).remove("data");
                                OrdersEntity ordersEntity = new OrdersEntity();
                                ordersEntity.setCreationDate(new Date());
                                ordersEntity.setCustomerId(entityManager.find(OffersEntity.class, offerId).getCustomerId());
                                ordersEntity.setOfferId(offerId);
                                ordersEntity.setSellerId(entityManager.find(OffersEntity.class, offerId).getSellerId());
                                ordersEntity.setAa("1");
                                ordersEntity.setBillingId(entityManager.find(OffersEntity.class, offerId).getBillingId());
                                ordersEntity.setStatus("ΝΕΑ");
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
                                    orderSchedule.setFromPostalCode(offersSchedulesEntity.getFromPostalCode());
                                    orderSchedule.setToAddress(offersSchedulesEntity.getToAddress());
                                    orderSchedule.setToCity(offersSchedulesEntity.getToCity());
                                    orderSchedule.setToCountry(offersSchedulesEntity.getToCountry());
                                    orderSchedule.setToPostalCode(offersSchedulesEntity.getToPostalCode());
                                    orderSchedule.setPrimarySchedule(schedule.findPath("primaryRecord").asInt());
//                                    orderSchedule.setFactoryId((long) 0);
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
                                            orderWaypointsEntity.setPostalCode(offerWayp.getPostalCode());
                                        }
                                        orderWaypointsEntity.setOrderId(ordersEntity.getId());
                                        orderWaypointsEntity.setCreationDate(new Date());
                                        orderWaypointsEntity.setOrderScheduleId(orderSchedule.getId());
                                        orderWaypointsEntity.setOfferScheduleBetweenWaypointId(offerWayp.getId());
                                        entityManager.persist(orderWaypointsEntity);
                                    }
//
                                    //pros8hkh kai tou telikou proorismou apo thn prosfora
                                    OrderWaypointsEntity orderWaypointsEntity = new OrderWaypointsEntity();
                                    orderWaypointsEntity.setCity(offersSchedulesEntity.getToCity());
                                    orderWaypointsEntity.setCountry(offersSchedulesEntity.getToCountry());
                                    orderWaypointsEntity.setPostalCode(offersSchedulesEntity.getToPostalCode());
                                    orderWaypointsEntity.setCreationDate(new Date());
                                    orderWaypointsEntity.setOrderId(ordersEntity.getId());
                                    orderWaypointsEntity.setOrderScheduleId(orderSchedule.getId());
                                    orderWaypointsEntity.setOfferScheduleBetweenWaypointId(null);
                                    orderWaypointsEntity.setNestedScheduleIndicator(0);
                                    entityManager.persist(orderWaypointsEntity);
//                                    //end pros8hkh kai tou telikou proorismou apo thn prosfora

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
                                        System.out.println(sqlPackages);
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


    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }


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
                CompletableFuture<HashMap<String, Object>> getFuture = CompletableFuture.supplyAsync(() -> { //
                            return jpaApi.withTransaction(
                                    entityManager -> {
                                        String orderCol = json.findPath("orderCol").asText();
                                        String descAsc = json.findPath("descAsc").asText();
                                        String id = json.findPath("id").asText();
                                        String offerId = json.findPath("offerId").asText();
                                        String creationDate = json.findPath("creationDate").asText();
                                        String customer = json.findPath("customer").asText();
                                        String status = json.findPath("status").asText();
                                        String from = json.findPath("from").asText();
                                        String to = json.findPath("to").asText();
                                        String seller = json.findPath("seller").asText();
                                        String billing = json.findPath("billing").asText();
                                        String start = json.findPath("start").asText();
                                        String limit = json.findPath("limit").asText();
                                        String sqlCustSupl = "select * from orders ord where 1=1 ";
                                        if (!id.equalsIgnoreCase("") && id != null) {
                                            sqlCustSupl += " and ord.id =" + id + "";
                                        }
                                        if (!offerId.equalsIgnoreCase("") && offerId != null) {
                                            sqlCustSupl += " and ord.offer_id like '%" + offerId + "%'";
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

                                        if (!customer.equalsIgnoreCase("") && customer != null) {
                                            sqlCustSupl += " and ord.customer_id  in " +
                                                    " ( select id from  customers_suppliers cs where cs.brand_name like '%" + customer + "%' )";
                                        }
                                        if (!status.equalsIgnoreCase("") && status != null) {
                                            sqlCustSupl += " and ord.status like '%" + status + "%'";
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
                                        List<OrdersEntity> filalistAll
                                                = (List<OrdersEntity>) entityManager.createNativeQuery(
                                                sqlCustSupl, OrdersEntity.class).getResultList();
                                        if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                            if (orderCol.equalsIgnoreCase("billingName")) {
                                                sqlCustSupl += " order by (select name from billings b where b.id=offer.billing_id)" + descAsc;
                                            } else if (orderCol.equalsIgnoreCase("sellerName")) {
                                                sqlCustSupl += " order by (select name from internova_sellers iseller where iseller.id=offer.seller_id)" + descAsc;
                                            } else if (orderCol.equalsIgnoreCase("brandName")) {
                                                sqlCustSupl += " order by (select brand_name from customers_suppliers cs where cs.id=offer.customer_id)" + descAsc;
                                            } else {
                                                sqlCustSupl += " order by " + orderCol + " " + descAsc;
                                            }
                                        } else {
                                            sqlCustSupl += " order by creation_date desc";
                                        }
                                        if (!start.equalsIgnoreCase("") && start != null) {
                                            sqlCustSupl += " limit " + start + "," + limit;
                                        }
                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();
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
                                            System.out.println(offers);
                                            try {
                                                offers = removeLastChar(offers);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            if (j.getCustomerId() != null) {
                                                CustomersSuppliersEntity customersSuppliersEntity = entityManager.find(CustomersSuppliersEntity.class, j.getCustomerId());
                                                sHmpam.put("customerBrandName", customersSuppliersEntity.getBrandName());
                                                sHmpam.put("customerId", customersSuppliersEntity.getId());
                                                sHmpam.put("customerTelephone", customersSuppliersEntity.getCustomerType());
                                                sHmpam.put("customerEmail", customersSuppliersEntity.getEmail());
                                                sHmpam.put("billingName", entityManager.find(BillingsEntity.class, customersSuppliersEntity.getBillingId()).getName());
                                                sHmpam.put("sellerName", entityManager.find(InternovaSellersEntity.class, customersSuppliersEntity.getInternovaSellerId()).getName());
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
                                                schedmap.put("offerId", os.getOfferId());
                                                schedmap.put("primarySchedule", os.getPrimarySchedule());
                                                schedmap.put("creationDate", os.getCreationDate());
                                                schedmap.put("factoryId", os.getFactoryId());
                                                schedmap.put("fromCity", os.getFromCity());
                                                schedmap.put("orderScheduleId", os.getId());
                                                schedmap.put("fromCountry", os.getFromCountry());
                                                schedmap.put("fromPostalCode", os.getFromPostalCode());

                                                if(os.getFactoryId()!=null){
                                                    schedmap.put("fromAddress", entityManager.find(FactoriesEntity.class,(os.getFactoryId())).getAddress());
                                                    schedmap.put("brandName", entityManager.find(FactoriesEntity.class,(os.getFactoryId())).getBrandName());
                                                }else{
                                                    schedmap.put("fromAddress", "Δεν έχει οριστεί");
                                                    schedmap.put("brandName", "-");
                                                }

                                                schedmap.put("toCity", os.getToCity());
                                                schedmap.put("toCountry", os.getToCountry());
                                                schedmap.put("toPostalCode", os.getToPostalCode());
                                                schedmap.put("orderScheduleId", os.getId());

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
                                                    ordPmap.put("unitPrice", ops.getUnitPrice());
                                                    ordPmap.put("creationDate", ops.getCreationDate());
                                                    ordPmap.put("typeId", ops.getTypeId());
                                                    ordPmap.put("orderScheduleId", ops.getOrderScheduleId());
                                                    ordPmap.put("quantity", ops.getQuantity());

                                                    String sqlDv = "select * from order_distinct_item od where od.order_id="+ops.getOrderId() +" and od.order_schedule_id="+ops.getOrderScheduleId() +" and od.order_package_id="+ops.getId();

                                                    System.out.println(sqlDv);
                                                    List<OrderDistinctItemEntity> orderDistinctItemList = entityManager.createNativeQuery(sqlDv,OrderDistinctItemEntity.class).getResultList();
                                                    List<HashMap<String, Object>> odvList = new ArrayList<HashMap<String, Object>>();
                                                    for(OrderDistinctItemEntity  odv :orderDistinctItemList){
                                                        HashMap<String, Object> distinctItemMap = new HashMap<String, Object>();
                                                        distinctItemMap.put("title", odv.getTitle());
                                                        distinctItemMap.put("from", odv.getFromUnit());
                                                        distinctItemMap.put("to", odv.getToUnit());
                                                        distinctItemMap.put("unitPrice", odv.getUnitPrice());
                                                        odvList.add(distinctItemMap);
                                                    }
                                                    HashMap<String, Object> distinctItem = new HashMap<String, Object>();
                                                    distinctItem.put("title", ops.getTitle());
                                                    distinctItem.put("distanceValues", odvList);
                                                    ordPmap.put("distinctItem", distinctItem);
                                                    orderPackageScheduleList.add(ordPmap);
                                                }
                                                schedmap.put("orderPackageScheduleList", orderPackageScheduleList);
//
                                                String sqlPackages = "" +
                                                        "select distinct " +
                                                        "op.measure_unit_id,mu.title\n" +
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
                                                    item.put("orderId", os.getOrderId());
                                                    item.put("quantity", 0);
                                                    item.put("unitPrice", 0);
                                                    item.put("finalUnitPrice", 0);

                                                    String sqlDistansesValues = "select * FROM order_packages where order_id=" + os.getOrderId() + " and measure_unit_id=" + tu.get(0).asInt();
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
                                                    distinctDistancesPackageList.add(item);
                                                }
                                                schedmap.put("distinctDistancesPackageList", distinctDistancesPackageList);
                                                String waypointsSql = "select * from order_waypoints owp where owp.order_schedule_id=" + os.getId();
                                                List<OrderWaypointsEntity> waypointsList =
                                                        entityManager.createNativeQuery(waypointsSql, OrderWaypointsEntity.class).getResultList();
                                                List<HashMap<String, Object>> waypFinal = new ArrayList<HashMap<String, Object>>();
                                                for (OrderWaypointsEntity waypOb : waypointsList) {
                                                    HashMap<String, Object> waypmap = new HashMap<String, Object>();
                                                    waypmap.put("city", waypOb.getCity());
                                                    waypmap.put("country", waypOb.getCountry());
                                                    waypmap.put("postalCode", waypOb.getPostalCode());


                                                    if(waypOb.getFactoryId()!=null){
                                                        waypmap.put("address", entityManager.find(FactoriesEntity.class,(waypOb.getFactoryId())).getAddress());
                                                        waypmap.put("brandName", entityManager.find(FactoriesEntity.class,(waypOb.getFactoryId())).getBrandName());
                                                    }else{
                                                        waypmap.put("address", "Δεν έχει οριστεί");
                                                        waypmap.put("brandName", "-");
                                                    }


                                                    waypmap.put("nestedScheduleIndicator", waypOb.getNestedScheduleIndicator());
                                                    waypmap.put("factoryId", waypOb.getFactoryId());
                                                    if (waypOb.getOfferScheduleBetweenWaypointId() != null) {
                                                        waypmap.put("finalStation", false);
                                                    } else {
                                                        if(waypOb.getNewWaypoint()!=null && waypOb.getNewWaypoint()==1){
                                                            waypmap.put("finalStation", false);
                                                        }else{
                                                            waypmap.put("finalStation", true);
                                                        }
                                                    }
                                                    waypmap.put("offerScheduleBetweenWaypointId", waypOb.getOfferScheduleBetweenWaypointId());
                                                    waypFinal.add(waypmap);
                                                }
                                                schedmap.put("waypointsList", waypFinal);
                                                schedulesList.add(schedmap);
                                            }
                                            sHmpam.put("schedulesList", schedulesList);
                                            sHmpam.put("offerId", offers);
                                            sHmpam.put("customerId", j.getCustomerId());
                                            sHmpam.put("id", j.getId());
                                            sHmpam.put("comments", j.getComments());
                                            sHmpam.put("factoryId", j.getFactoryId());
                                            sHmpam.put("generalInstructions", j.getGeneralInstructions());
                                            sHmpam.put("truckTemprature", j.getTruckTemprature());
                                            sHmpam.put("creationDate", j.getCreationDate());
                                            sHmpam.put("updateDate", j.getUpdateDate());
                                            sHmpam.put("childaddIndcator", false);
                                            sHmpam.put("status", j.getStatus());
                                            filalist.add(sHmpam);
                                        }
                                        returnList_future.put("data", filalist);
                                        returnList_future.put("total", filalistAll.size());
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


//    @SuppressWarnings({"Duplicates", "unchecked"})
//    public Result getOrders(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
//        ObjectNode result = Json.newObject();
//        JsonNode json = request.body().asJson();
//        if (json == null) {
//            return badRequest("Expecting Json data");
//        } else {
//            if (json == null) {
//                result.put("status", "error");
//                result.put("message", "Δεν εχετε αποστειλει εγκυρα δεδομενα.");
//                return ok(result);
//            } else {
//                ObjectMapper ow = new ObjectMapper();
//                HashMap<String, Object> returnList = new HashMap<String, Object>();
//                String jsonResult = "";
//                CompletableFuture<HashMap<String, Object>> getFuture = CompletableFuture.supplyAsync(() -> { //
//                            return jpaApi.withTransaction(
//                                    entityManager -> {
//                                        String orderCol = json.findPath("orderCol").asText();
//                                        String descAsc = json.findPath("descAsc").asText();
//                                        String id = json.findPath("id").asText();
//                                        String offerId = json.findPath("offerId").asText();
//                                        String creationDate = json.findPath("creationDate").asText();
//                                        String customer = json.findPath("customer").asText();
//                                        String status = json.findPath("status").asText();
//                                        String from = json.findPath("from").asText();
//                                        String to = json.findPath("to").asText();
//                                        String seller = json.findPath("seller").asText();
//                                        String billing = json.findPath("billing").asText();
//                                        String start = json.findPath("start").asText();
//                                        String limit = json.findPath("limit").asText();
//                                        String sqlCustSupl = "select * from orders ord where 1=1 ";
//                                        if (!id.equalsIgnoreCase("") && id != null) {
//                                            sqlCustSupl += " and ord.id =" + id + "";
//                                        }
//                                        if (!offerId.equalsIgnoreCase("") && offerId != null) {
//                                            sqlCustSupl += " and ord.offer_id like '%" + offerId + "%'";
//                                        }
//                                        if (!seller.equalsIgnoreCase("") && seller != null) {
//                                            sqlCustSupl +=
//                                                    " and ord.seller_id " +
//                                                            " in ( select id" +
//                                                            "      from  internova_sellers isell" +
//                                                            "      where isell.name like '%" + seller + "%' ) " +
//                                                            " union " +
//                                                            " select ord.*" +
//                                                            " from orders ord " +
//                                                            " join customers_suppliers cs on (cs.id=ord.customer_id and ord.seller_id is null )" +
//                                                            " where " +
//                                                            " cs.internova_seller_id in " +
//                                                            " (select id from  internova_sellers isell where isell.name like '%" + seller + "%' )";
//                                        }
//
//                                        if (!customer.equalsIgnoreCase("") && customer != null) {
//                                            sqlCustSupl += " and ord.customer_id  in " +
//                                                    " ( select id from  customers_suppliers cs where cs.brand_name like '%" + customer + "%' )";
//                                        }
//                                        if (!status.equalsIgnoreCase("") && status != null) {
//                                            sqlCustSupl += " and ord.status like '%" + status + "%'";
//                                        }
//
//                                        if (!from.equalsIgnoreCase("") && from != null) {
//                                            sqlCustSupl += " and ord.from_address like '%" + from + "%'";
//                                        }
//                                        if (!to.equalsIgnoreCase("") && to != null) {
//                                            sqlCustSupl += " and ord.to_address like '%" + to + "%'";
//                                        }
//                                        if (!billing.equalsIgnoreCase("") && billing != null) {
//                                            sqlCustSupl +=
//                                                    " and ord.billing_id " +
//                                                            " in ( select id" +
//                                                            "      from  billings billing" +
//                                                            "      where billing.name like '%" + billing + "%' ) " +
//                                                            " union " +
//                                                            " select ord.*" +
//                                                            " from orders ord " +
//                                                            " join customers_suppliers cs on (cs.id=ord.customer_id and ord.billing_id is null )" +
//                                                            " where " +
//                                                            " cs.billing_id in " +
//                                                            " (select id from  billings billing where billing.name like '%" + billing + "%' )";
//                                        }
//                                        if (!creationDate.equalsIgnoreCase("") && creationDate != null) {
//                                            sqlCustSupl += " and SUBSTRING( offer.offer_date, 1, 10)  = '" + creationDate + "'";
//                                        }
//                                        List<OrdersEntity> filalistAll
//                                                = (List<OrdersEntity>) entityManager.createNativeQuery(
//                                                sqlCustSupl, OrdersEntity.class).getResultList();
//                                        if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
//                                            if (orderCol.equalsIgnoreCase("billingName")) {
//                                                sqlCustSupl += " order by (select name from billings b where b.id=offer.billing_id)" + descAsc;
//                                            } else if (orderCol.equalsIgnoreCase("sellerName")) {
//                                                sqlCustSupl += " order by (select name from internova_sellers iseller where iseller.id=offer.seller_id)" + descAsc;
//                                            } else if (orderCol.equalsIgnoreCase("brandName")) {
//                                                sqlCustSupl += " order by (select brand_name from customers_suppliers cs where cs.id=offer.customer_id)" + descAsc;
//                                            } else {
//                                                sqlCustSupl += " order by " + orderCol + " " + descAsc;
//                                            }
//                                        } else {
//                                            sqlCustSupl += " order by creation_date desc";
//                                        }
//                                        if (!start.equalsIgnoreCase("") && start != null) {
//                                            sqlCustSupl += " limit " + start + "," + limit;
//                                        }
//                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();
//                                        List<HashMap<String, Object>> filalist = new ArrayList<HashMap<String, Object>>();
//                                        List<OrdersEntity> ordersEntityList
//                                                = (List<OrdersEntity>) entityManager.createNativeQuery(
//                                                sqlCustSupl, OrdersEntity.class).getResultList();
//
//                                        for (OrdersEntity j : ordersEntityList) {
//                                            HashMap<String, Object> sHmpam = new HashMap<String, Object>();
//                                            sHmpam.put("id", j.getId());
//                                            sHmpam.put("offerId", j.getOfferId());
//                                            String sqlRelativeOffers = "select distinct offer_id FROM order_schedules os where os.order_id="+j.getId();
//                                            List<Object> ordSchedList = entityManager.createNativeQuery(sqlRelativeOffers).getResultList();
//                                           String offers="";
//                                            for(int i=0;i<ordSchedList.size();i++){
//                                                offers=offers+ordSchedList.get(i)+",";
//                                            }
//                                            offers = removeLastChar(offers);
//                                            sHmpam.put("offerId", offers);
//                                            sHmpam.put("customerId", j.getCustomerId());
//                                            if (j.getCustomerId() != null) {
//                                                HashMap<String, Object> customerMap = new HashMap<String, Object>();
//                                                HashMap<String, Object> billingsMap = new HashMap<String, Object>();
//                                                HashMap<String, Object> sellerMap = new HashMap<String, Object>();
//                                                CustomersSuppliersEntity customersSuppliersEntity = entityManager.find(CustomersSuppliersEntity.class, j.getCustomerId());
//                                                customerMap.put("customerId", customersSuppliersEntity.getId());
//                                                customerMap.put("email", customersSuppliersEntity.getEmail());
//                                                customerMap.put("telephone", customersSuppliersEntity.getTelephone());
//                                                customerMap.put("brandName", customersSuppliersEntity.getBrandName());
//                                                if (j.getBillingId() != null) {
//                                                    billingsMap.put("billingId", j.getBillingId());
//                                                    billingsMap.put("billingName", entityManager.find(BillingsEntity.class, j.getBillingId()).getName());
//                                                } else {
//                                                    billingsMap.put("billingId", customersSuppliersEntity.getBillingId());
//                                                    billingsMap.put("billingName", entityManager.find(BillingsEntity.class, customersSuppliersEntity.getBillingId()).getName());
//                                                }
//                                                sHmpam.put("billings", billingsMap);
//                                                if (j.getSellerId() != null) {
//                                                    sellerMap.put("sellerId", j.getSellerId());
//                                                    sellerMap.put("sellerName", entityManager.find(InternovaSellersEntity.class, j.getSellerId()).getName());
//                                                } else {
//                                                    sellerMap.put("sellerId", customersSuppliersEntity.getInternovaSellerId());
//                                                    sellerMap.put("sellerName", entityManager.find(InternovaSellersEntity.class, customersSuppliersEntity.getInternovaSellerId()).getName());
//                                                }
//                                                sHmpam.put("seller", sellerMap);
//                                                sHmpam.put("custommer", customerMap);
//                                            }
//                                            sHmpam.put("comments", j.getComments());
//                                            sHmpam.put("factoryId", j.getFactoryId());
//                                            sHmpam.put("generalInstructions", j.getGeneralInstructions());
//                                            DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
//                                            if (j.getArrivalFactoryDay() != null) {
//                                                String datArrival = myDateFormat.format(j.getArrivalFactoryDay());
//                                                sHmpam.put("arrivalFactoryDay", datArrival);
//                                            } else {
//                                                sHmpam.put("arrivalFactoryDay", "");
//                                            }
//                                            sHmpam.put("truckTemprature", j.getTruckTemprature());
//                                            sHmpam.put("creationDate", j.getCreationDate());
//                                            sHmpam.put("updateDate", j.getUpdateDate());
//                                            sHmpam.put("childaddIndcator", false);
//
//
////                                            HashMap<String, Object> fromAddress = new HashMap<String, Object>();
////                                            if (j.getFactoryId() != null) {
////                                                FactoriesEntity fact = entityManager.find(FactoriesEntity.class, j.getFactoryId());
////                                                fromAddress.put("city", fact.getCity());
////                                                fromAddress.put("email", fact.getEmail());
////                                                fromAddress.put("address", fact.getAddress());
////                                                fromAddress.put("telephone", fact.getTelephone());
////                                                fromAddress.put("brandName", fact.getBrandName());
////                                                fromAddress.put("postalCode", fact.getPostalCode());
////                                                fromAddress.put("unloadingLoadingCode", fact.getUnloadingLoadingCode());
////                                                fromAddress.put("country", fact.getCountry());
////                                                fromAddress.put("lattitude", fact.getLattitude());
////                                                fromAddress.put("longtitude", fact.getLongtitude());
////                                                sHmpam.put("from", fromAddress);
////                                            } else {
//////                                                fromAddress.put("city", j.getFromCity());
//////                                                fromAddress.put("address", j.getFromAddress());
//////                                                fromAddress.put("email", "");
//////                                                fromAddress.put("telephone", "");
//////                                                fromAddress.put("unloadingLoadingCode", "");
//////                                                fromAddress.put("brandName", "");
//////                                                fromAddress.put("postalCode", j.getFromPostalCode());
//////                                                fromAddress.put("country", j.getFromCountry());
//////                                                fromAddress.put("lattitude", j.getFromLattitude());
//////                                                fromAddress.put("longtitude", j.getFromLongtitude());
////                                                sHmpam.put("from", fromAddress);
////                                            }
////                                            HashMap<String, Object> toAddress = new HashMap<String, Object>();
////                                            toAddress.put("city", j.getFromCity());
////                                            toAddress.put("address", j.getToAddress());
////                                            toAddress.put("country", j.getToCountry());
////                                            toAddress.put("lattitude", j.getToLattitude());
////                                            toAddress.put("longtitude", j.getToLongtitude());
////                                            toAddress.put("postalCode", j.getToPostalCode());
////                                            sHmpam.put("to", toAddress);
//                                            sHmpam.put("status", j.getStatus());
//
//
////                                            String packages = "select * from order_packages op where op.order_id=" + j.getId();
////                                            List<HashMap<String, Object>> unitFinalList = new ArrayList<HashMap<String, Object>>();
////                                            List<OrderPackagesEntity> orderPackagesEntityList = entityManager.createNativeQuery(packages, OrderPackagesEntity.class).getResultList();
////                                            for (OrderPackagesEntity ordPack : orderPackagesEntityList) {
////                                                HashMap<String, Object> packageOrder = new HashMap<String, Object>();
////                                                packageOrder.put("id", ordPack.getId());
////                                                packageOrder.put("packageId", ordPack.getId());
////                                                packageOrder.put("fromUnit", ordPack.getFromUnit());
////                                                packageOrder.put("toUnit", ordPack.getToUnit());
////                                                packageOrder.put("unitPrice", ordPack.getUnitPrice());
////                                                packageOrder.put("measureUnitId", ordPack.getMeasureUnitId());
////                                                HashMap<String, Object> unit = new HashMap<String, Object>();
////                                                unit.put("title", entityManager.find(MeasurementUnitEntity.class, ordPack.getMeasureUnitId()).getTitle());
////                                                unit.put("id", entityManager.find(MeasurementUnitEntity.class, ordPack.getMeasureUnitId()).getId());
////                                                packageOrder.put("unit", unit);
////                                                unitFinalList.add(packageOrder);
////                                            }
////                                            sHmpam.put("orderPackagesEntityList", unitFinalList);
////                                            String selectedPackagesStartPoint = "select * from order_package_start_point opstp where opstp.order_id=" + j.getId();
////                                            List<HashMap<String, Object>> selectedStratPointPackages = new ArrayList<HashMap<String, Object>>();
////                                            List<OrderPackageStartPointEntity> packagesStartList = entityManager.createNativeQuery(selectedPackagesStartPoint, OrderPackageStartPointEntity.class).getResultList();
//
////                                            for (OrderPackageStartPointEntity opst : packagesStartList) {
////                                                HashMap<String, Object> packagesStart = new HashMap<String, Object>();
////                                                packagesStart.put("measureUnitId", opst.getMeasureUnitId());
////                                                packagesStart.put("title", opst.getTitle());
////                                                packagesStart.put("orderId", opst.getOrderId());
////                                                packagesStart.put("quantity", opst.getQuantity());
////                                                packagesStart.put("unitPrice", opst.getUnitPrice());
////                                                packagesStart.put("finalUnitPrice", opst.getFinalUnitPrice());
////                                                packagesStart.put("oldQuantity", opst.getQuantity());
////                                                packagesStart.put("oldUnitPrice", opst.getUnitPrice());
////                                                packagesStart.put("oldFinalUnitPrice", opst.getFinalUnitPrice());
////                                                String sqlDistansesValues = "select * from internova_db.order_packages where order_id=" + opst.getOrderId() + " and measure_unit_id=" + opst.getMeasureUnitId();
////                                                List<OrderPackagesEntity> distList = entityManager.createNativeQuery(sqlDistansesValues, OrderPackagesEntity.class).getResultList();
////                                                List<HashMap<String, Object>> fdvList = new ArrayList<HashMap<String, Object>>();
////                                                for (OrderPackagesEntity dv : distList) {
////                                                    HashMap<String, Object> distanceValues = new HashMap<>();
////                                                    distanceValues.put("measureUnitId", dv.getMeasureUnitId());
////                                                    distanceValues.put("from", dv.getFromUnit());
////                                                    distanceValues.put("to", dv.getToUnit());
////                                                    distanceValues.put("unitPrice", dv.getUnitPrice());
////                                                    fdvList.add(distanceValues);
////                                                }
////                                                packagesStart.put("distanceValues", fdvList);
////                                            //    selectedStratPointPackages.add(packagesStart);
////                                            }
//                                        //    sHmpam.put("selectedStratPointPackages", selectedStratPointPackages);
//
////                                            HashMap<String, Object> dromologio = new HashMap<>();
////                                            dromologio.put("city", fromAddress.get("city").toString());
////                                            dromologio.put("country", fromAddress.get("country").toString());
//
////                                            String sqlWpnts = "select * from order_waypoints where order_id="+j.getId();
////                                            List<OrderWaypointsEntity> wayplist = entityManager.createNativeQuery(sqlWpnts,OrderWaypointsEntity.class).getResultList();
////                                            List<HashMap<String, Object>> dromologioFinalList = new ArrayList<HashMap<String, Object>>();
////                                            //dromologioFinalList.add(dromologio);
////                                            for(OrderWaypointsEntity owp : wayplist){
////                                                HashMap<String, Object> orderWaypMap = new HashMap<>();
////                                                orderWaypMap.put("city",owp.getCity());
////                                                orderWaypMap.put("country",owp.getCountry());
////                                                dromologioFinalList.add(orderWaypMap);
////                                            }
////                                            sHmpam.put("dromologioList",dromologioFinalList);
//                                            filalist.add(sHmpam);
//                                        }
//                                        returnList_future.put("data", filalist);
//                                        returnList_future.put("total", filalistAll.size());
//                                        returnList_future.put("status", "success");
//                                        returnList_future.put("message", "success");
//                                        return returnList_future;
//                                    });
//                        },
//                        executionContext);
//                returnList = getFuture.get();
//                DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//                ow.setDateFormat(myDateFormat);
//                try {
//                    jsonResult = ow.writeValueAsString(returnList);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    result.put("status", "error");
//                    result.put("message", "Πρόβλημα κατά την ανάγνωση των στοιχείων ");
//                    return ok(result);
//                }
//                return ok(jsonResult);
//            }
//        }
//    }
//
//


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
                                    warehousesEntity.setPostalCode(postalCode);
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
                                //
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
                                    factory.setPostalCode(postalCode);
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
                                String user_id = json.findPath("user_id").asText();
                                Long orderId = json.findPath("orderId").asLong();
                         //       System.out.println(json);

                                //order_distinct_item

                                String sqlDv = "select * from order_distinct_item where order_id="+orderId;
                                List<OrderDistinctItemEntity> orderDistinctItemEntitiesList = entityManager.createNativeQuery(sqlDv,OrderDistinctItemEntity.class).getResultList();

                                for(OrderDistinctItemEntity odv : orderDistinctItemEntitiesList){
                                    entityManager.remove(odv);
                                }

                                String sqlOrderPackages = " select * from  order_package_schedules ops where  ops.order_id="+orderId;
                                List<OrderPackageSchedulesEntity> orderPackageSchedulesEntityList = entityManager.createNativeQuery(sqlOrderPackages,OrderPackageSchedulesEntity.class).getResultList();
                                for(OrderPackageSchedulesEntity ops : orderPackageSchedulesEntityList){
                                    entityManager.remove(ops);
                                }
                                String sqlWp = " select * from order_waypoints owp where owp.order_id="+orderId;
                                List<OrderWaypointsEntity> orderWaypointsEntityList = entityManager.createNativeQuery(sqlWp,OrderWaypointsEntity.class).getResultList();
                                for(OrderWaypointsEntity owp :orderWaypointsEntityList ){
                                    entityManager.remove(owp);
                                }
                                String sqlWpack = "select * from order_waypoints_packages owpack where owpack.order_id="+orderId;
                                List<OrderWaypointsPackagesEntity> waypointsPackagesEntityList = entityManager.createNativeQuery(sqlWpack,OrderWaypointsPackagesEntity.class).getResultList();
                                for(OrderWaypointsPackagesEntity wpack :waypointsPackagesEntityList ){
                                    entityManager.remove(wpack);
                                }
//                                String sqlOrdP = "select * from order_packages op where op.order_id="+orderId;
//                                List <OrderPackagesEntity> orderPackagesEntityList =  entityManager.createNativeQuery(sqlOrdP,OrderPackagesEntity.class).getResultList();
//                                for(OrderPackagesEntity op : orderPackagesEntityList){
//                                 //   entityManager.remove(op);
//                                }
                                OrdersEntity ordersEntity = entityManager.find(OrdersEntity.class,orderId);
                                JsonNode finalTimeline = json.findPath("finalTimeline");
                                Iterator fintIt = finalTimeline.iterator();
                                while (fintIt.hasNext()) {
                                    JsonNode schedule = Json.toJson(fintIt.next());
                                    String factoryId = schedule.findPath("factoryId").asText();
                                    String offerScheduleBetweenWaypointId = schedule.findPath("offerScheduleBetweenWaypointId").asText();
                                    if(schedule.findPath("timelinetype").asText().equalsIgnoreCase("Αφετηρία")){
                                        OrderSchedulesEntity ordS = entityManager.find(OrderSchedulesEntity.class,schedule.findPath("orderScheduleId").asLong());
                                        if(factoryId!=null &&  !factoryId.equalsIgnoreCase("") && !factoryId.equalsIgnoreCase("null")){
                                            ordS.setFactoryId(schedule.findPath("factoryId").asLong());
                                            FactoriesEntity factory = entityManager.find(FactoriesEntity.class,ordS.getFactoryId());
                                            ordS.setFromAddress(factory.getAddress());
                                            entityManager.merge(ordS);
                                        }

                                        JsonNode orderPackageScheduleList = schedule.findPath("orderPackageScheduleList");
                                        Iterator orderPackIt = orderPackageScheduleList.iterator();
                                        while (orderPackIt.hasNext()) {
                                            JsonNode ordPjsonode = Json.toJson(orderPackIt.next());
                                            System.out.println(ordPjsonode);

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

                                            JsonNode distinctItem = ordPjsonode.findPath("distinctItem").findPath("distanceValues");
                                            Iterator distinctItemIter = distinctItem.iterator();
                                            while (distinctItemIter.hasNext()) {
                                                JsonNode distJson = Json.toJson(distinctItemIter.next());
                                                Double unitPriceDv = distJson.findPath("unitPrice").asDouble();
                                                Integer from = distJson.findPath("from").asInt();
                                                Integer to = distJson.findPath("to").asInt();
                                                OrderDistinctItemEntity odv = new OrderDistinctItemEntity();
                                                odv.setFromUnit(from);
                                                odv.setToUnit(to);
                                                odv.setTitle(title);
                                                odv.setUnitPrice(unitPriceDv);
                                                odv.setOrderId(orderId);
                                                odv.setOrderPackageId(orderSched.getId());
                                                odv.setOrderScheduleId(ordS.getId());
                                                entityManager.persist(odv);
                                            }

                                        }


                                    }else{

                                        String newWaypoint = schedule.findPath("newWaypoint").asText();

                                        OrderWaypointsEntity orderWaypointsPackagesEntity = new OrderWaypointsEntity();
                                        orderWaypointsPackagesEntity.setCreationDate(new Date());
                                        if(factoryId!=null &&  !factoryId.equalsIgnoreCase("") && !factoryId.equalsIgnoreCase("null")){
                                            orderWaypointsPackagesEntity.setFactoryId(schedule.findPath("factoryId").asLong());
                                            FactoriesEntity factory = entityManager.find(FactoriesEntity.class,orderWaypointsPackagesEntity.getFactoryId());
                                            orderWaypointsPackagesEntity.setAddress(factory.getAddress());
                                            entityManager.merge(orderWaypointsPackagesEntity);
                                        }
                                        orderWaypointsPackagesEntity.setOrderId(orderId);
                                        orderWaypointsPackagesEntity.setCity(schedule.findPath("city").asText());
                                        orderWaypointsPackagesEntity.setCountry(schedule.findPath("country").asText());
                                        orderWaypointsPackagesEntity.setPostalCode(schedule.findPath("postalCode").asText());
                                        orderWaypointsPackagesEntity.setOrderScheduleId(schedule.findPath("orderScheduleId").asLong());
                                        orderWaypointsPackagesEntity.setNestedScheduleIndicator(schedule.findPath("nestedScheduleIndicator").asInt());
                                        if(offerScheduleBetweenWaypointId!=null &&  !offerScheduleBetweenWaypointId.equalsIgnoreCase("") && !offerScheduleBetweenWaypointId.equalsIgnoreCase("null")){
                                            orderWaypointsPackagesEntity.setOfferScheduleBetweenWaypointId(schedule.findPath("offerScheduleBetweenWaypointId").asLong());
                                        }
                                        if(newWaypoint!=null && !newWaypoint.equalsIgnoreCase("null") && !newWaypoint.equalsIgnoreCase("")){
                                            orderWaypointsPackagesEntity.setNewWaypoint(1);
                                        }

                                        entityManager.persist(orderWaypointsPackagesEntity);
                                    }
                                }

                                add_result.put("status", "success");
                                add_result.put("message", "Η ενημέρωση της παραγγελίας πραγματοποιήθηκε με επιτυχία");
                                add_result.put("system", "ΠΑΡΑΓΓΕΛΙΕΣ");
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

//
//    @SuppressWarnings({"Duplicates", "unchecked"})
//    @BodyParser.Of(BodyParser.Json.class)
//    public Result updateOrderSchedulesAfterAdd(final Http.Request request) throws IOException {
//        JsonNode json = request.body().asJson();
//        if (json == null) {
//            return badRequest("Expecting Json data");
//        } else {
//            try {
//                ObjectNode result = Json.newObject();
//                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
//                            return jpaApi.withTransaction(
//                                    entityManager -> {
//                                        List<HashMap<String, Object>> finalList = new ArrayList<HashMap<String, Object>>();
//                                        ObjectNode add_result = Json.newObject();
//                                        Long orderId = json.findPath("orderId").asLong();
//
//                                        String sqlPackages = "" +
//                                                "select distinct " +
//                                                "op.measure_unit_id,mu.title\n" +
//                                                " from order_packages op " +
//                                                " join measurement_unit mu on (mu.id=op.measure_unit_id) " +
//                                                " where op.order_id=" + orderId;
//                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();
//                                        List tul = entityManager.createNativeQuery(sqlPackages).getResultList();
//                                        Iterator it = tul.iterator();
//
//                                        while (it.hasNext()) {
//                                            JsonNode tu = Json.toJson(it.next());
//                                            HashMap<String, Object> item = new HashMap<>();
//                                            item.put("measureUnitId", tu.get(0).asInt());
//                                            item.put("title", tu.get(1).asText());
//                                            item.put("orderId", orderId);
//                                            item.put("quantity", 0);
//                                            item.put("unitPrice", 0);
//                                            item.put("finalUnitPrice", 0);
//
//                                            String sqlDistansesValues = "select * FROM internova_db.order_packages where order_id=" + orderId + " and measure_unit_id=" + tu.get(0).asInt();
//                                            List<OrderPackagesEntity> distList = entityManager.createNativeQuery(sqlDistansesValues, OrderPackagesEntity.class).getResultList();
//                                            List<HashMap<String, Object>> fdvList = new ArrayList<HashMap<String, Object>>();
//                                            for (OrderPackagesEntity dv : distList) {
//                                                HashMap<String, Object> distanceValues = new HashMap<>();
//                                                distanceValues.put("measureUnitId", dv.getMeasureUnitId());
//                                                distanceValues.put("from", dv.getFromUnit());
//                                                distanceValues.put("to", dv.getToUnit());
//                                                distanceValues.put("unitPrice", dv.getUnitPrice());
//                                                fdvList.add(distanceValues);
//                                            }
//                                            item.put("distanceValues", fdvList);
//                                            finalList.add(item);
//                                        }
//
//
//                                        add_result.put("status", "success");
//                                        add_result.put("message", "Η ενημέρωση της παραγγελίας πραγματοποιήθηκε με επιτυχία");
//                                        return add_result;
//                                    });
//                        },
//                        executionContext);
//                result = (ObjectNode) addFuture.get();
//                return ok(result, request);
//            } catch (Exception e) {
//                ObjectNode result = Json.newObject();
//                e.printStackTrace();
//                result.put("status", "error");
//                result.put("message", "Προβλημα κατα την καταχωρηση");
//                return ok(result);
//            }
//
//        }
//    }
//

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
                                        //roleDescSearchInput
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


//    @SuppressWarnings({"Duplicates", "unchecked"})
//    @BodyParser.Of(BodyParser.Json.class)
//    public Result updateOrder(final Http.Request request) throws IOException {
//        JsonNode json = request.body().asJson();
//        if (json == null) {
//            return badRequest("Expecting Json data");
//        } else {
//            try {
//                ObjectNode result = Json.newObject();
//                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
//                            return jpaApi.withTransaction(entityManager -> {
//                                ObjectNode add_result = Json.newObject();
//                                String user_id = json.findPath("user_id").asText();
//                                String factoryId = json.findPath("factoryId").asText();
//                                Long orderId = json.findPath("orderId").asLong();
//                                String email = json.findPath("email").asText();
//
//                                JsonNode selectedStratPointPackages = json.findPath("selectedStratPointPackages");
//                                ((ObjectNode) json).remove("selectedStratPointPackages");
//                                System.out.println(selectedStratPointPackages);
//
//                                //selectedStratPointPackages
//                                String city = json.findPath("city").asText();
//                                String country = json.findPath("country").asText();
//                                String telephone = json.findPath("telephone").asText();
//                                String brandName = json.findPath("brandName").asText();
//                                String address = json.findPath("address").asText();
//                                String postalCode = json.findPath("postalCode").asText();
//                                String region = json.findPath("region").asText();
//                                String latitude = json.findPath("lattitude").asText();
//                                String longitude = json.findPath("longtitude").asText();
//                                String comments = json.findPath("comments").asText();
//                                String truckTemprature = json.findPath("truckTemprature").asText();
//                                String arrivalFactoryDay = json.findPath("arrivalFactoryDay").asText();
//                                String generalInstructions = json.findPath("generalInstructions").asText();
//                                OrdersEntity order = entityManager.find(OrdersEntity.class, orderId);
//                                //posta
//                                order.setGeneralInstructions(generalInstructions);
//                                if (!truckTemprature.equalsIgnoreCase("") && truckTemprature != null && !truckTemprature.equalsIgnoreCase("null")) {
//                                    order.setTruckTemprature(truckTemprature);
//                                } else {
//                                    order.setTruckTemprature("");
//                                }
//                                if (!comments.equalsIgnoreCase("") && comments != null && !comments.equalsIgnoreCase("null")) {
//                                    order.setComments(comments);
//                                } else {
//                                    order.setComments("");
//                                }
//                                Date offerDateString = null;
//                                try {
//                                    offerDateString = new SimpleDateFormat("yyyy-MM-dd").parse(arrivalFactoryDay);
//                                    order.setArrivalFactoryDay(offerDateString);
//
//                                } catch (ParseException e) {
//                                    e.printStackTrace();
//                                }
//                                if (factoryId != null && !factoryId.equalsIgnoreCase("") && !factoryId.equalsIgnoreCase("null")) {
//                                    order.setFactoryId(Long.valueOf(factoryId));
//                                } else {
//                                    FactoriesEntity factory = new FactoriesEntity();
//                                    factory.setBrandName(brandName);
//                                    factory.setAddress(address);
//                                    factory.setTelephone(telephone);
//                                    factory.setCountry(country);
//                                    factory.setEmail(email);
//                                    factory.setCity(city);
//                                    factory.setPostalCode(postalCode);
//                                    factory.setRegion(region);
//                                    factory.setLongtitude(Double.valueOf(longitude));
//                                    factory.setLattitude(Double.valueOf(latitude));
//                                    factory.setLattitude(Double.valueOf(latitude));
//                                    factory.setAppointmentDays(0);
//                                    factory.setAppointmentRequired((byte) 0);
//                                    entityManager.persist(factory);
//                                    order.setFactoryId(factory.getId());
//                                }
//                                entityManager.merge(order);
//
//
//                                String selectedPackagesStartPoint = "select * from order_package_start_point opstp where opstp.order_id=" + order.getId();
//                                List<OrderPackageStartPointEntity> packagesStartList =
//                                        entityManager.createNativeQuery(selectedPackagesStartPoint, OrderPackageStartPointEntity.class).getResultList();
//                                for (OrderPackageStartPointEntity op : packagesStartList) {
//                                    entityManager.remove(op);
//                                }
//                                Iterator itStartPointPackages = selectedStratPointPackages.iterator();
//                                while (itStartPointPackages.hasNext()) {
//                                    JsonNode startPointNode = (JsonNode) itStartPointPackages.next();
//                                    OrderPackageStartPointEntity op = new OrderPackageStartPointEntity();
//                                    op.setCreationDate(new Date());
//                                    op.setOrderId(order.getId());
//                                    op.setQuantity(startPointNode.findPath("quantity").asInt());
//                                    op.setUnitPrice(startPointNode.findPath("unitPrice").asDouble());
//                                    op.setFinalUnitPrice(startPointNode.findPath("finalUnitPrice").asDouble());
//                                    op.setMeasureUnitId(startPointNode.findPath("measureUnitId").asLong());
//                                    op.setTitle(startPointNode.findPath("title").asText());
//                                    entityManager.persist(op);
//                                }
//
//
//                                add_result.put("status", "success");
//                                add_result.put("message", "Η ενημέρωση της παραγγελίας πραγματοποιήθηκε με επιτυχία");
//                                add_result.put("system", "ΠΑΡΑΓΓΕΛΙΕΣ");
//                                add_result.put("user_id", user_id);
//                                return add_result;
//                            });
//                        },
//                        executionContext);
//                result = (ObjectNode) addFuture.get();
//                return ok(result, request);
//            } catch (Exception e) {
//                ObjectNode result = Json.newObject();
//                e.printStackTrace();
//                result.put("status", "error");
//                result.put("message", "Προβλημα κατα την καταχωρηση");
//                return ok(result);
//            }
//
//        }
//    }


}
