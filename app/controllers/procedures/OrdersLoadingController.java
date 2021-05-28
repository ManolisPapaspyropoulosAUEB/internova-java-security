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

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class OrdersLoadingController extends Application {
    private JPAApi jpaApi;
    private final WSClient ws;
    private DatabaseExecutionContext executionContext;

    @Inject
    public OrdersLoadingController(JPAApi jpaApi, DatabaseExecutionContext executionContext, WSClient ws) {
        super(jpaApi, executionContext);
        this.jpaApi = jpaApi;
        this.ws = ws;
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
                                JsonNode doneDromologia = json.findPath("doneDromologia");
                                ((ObjectNode) json).remove("doneDromologia");

                                ObjectMapper ow = new ObjectMapper();
                                ObjectNode reqBody = Json.newObject();
                                String user_id = json.findPath("user_id").asText();
                                Long customerSupplierId = json.findPath("customerSupplierId").asLong();
                                Double naulo = json.findPath("naulo").asDouble();
                                Long truckTrailerId = json.findPath("truckTrailerId").asLong(); // truckTractorId
                                Long truckTractorId = json.findPath("truckTractorId").asLong(); // truckTractorId
                                String commentsMaster = json.findPath("commentsMaster").asText();
                                String status = json.findPath("statusOrderLoading").asText();
                                boolean timologioIndicator = json.findPath("timologioIndicator").asBoolean(); // truckTractorId
                                String arithmosTimologiou = json.findPath("arithmosTimologiou").asText();



                                reqBody.put("customerSupplierId", customerSupplierId);
                                reqBody.put("naulo", naulo);
                                OrdersLoadingEntity ordersLoadingEntity = new OrdersLoadingEntity();
                                ordersLoadingEntity.setCreationDate(new Date());
                                ordersLoadingEntity.setStatus(status);
                                ordersLoadingEntity.setSupplierId(customerSupplierId);
                                ordersLoadingEntity.setNaulo(naulo);
                                ordersLoadingEntity.setArithmosTimologiou(arithmosTimologiou);

                                if (timologioIndicator) {
                                    ordersLoadingEntity.setTimologioIndicator((byte) 1);
                                } else {
                                    ordersLoadingEntity.setTimologioIndicator((byte) 0);
                                }
                                ordersLoadingEntity.setSupplierTruckTrailerId(truckTrailerId);
                                ordersLoadingEntity.setSupplierTruckTractorId(truckTractorId);
                                ordersLoadingEntity.setComments(commentsMaster);
                                String maxAasql = "select max(aa) " +
                                        "from orders_loading t ";
                                Integer maxAa = (Integer) entityManager.
                                        createNativeQuery(maxAasql).getSingleResult();
                                if (maxAa == null) {
                                    ordersLoadingEntity.setAa(0);
                                } else {
                                    ordersLoadingEntity.setAa(maxAa + 1);
                                }
                                entityManager.persist(ordersLoadingEntity);
                                Iterator doneListIt = json.findPath("doneList").iterator();
                                List<JsonNode> finalDromologioParaggelias = new ArrayList<JsonNode>();

                                while (doneListIt.hasNext()) {
                                    JsonNode orderNode = (JsonNode) doneListIt.next();
                                    Iterator dromologioParIter = orderNode.findPath("dromologioParaggelias").iterator();
                                    ((ObjectNode) orderNode).remove("dromologioParaggelias");
                                    String statusOrder = orderNode.findPath("status").asText();
                                    String crmNumber = orderNode.findPath("crmNumber").asText();
                                    String crmIndicator = orderNode.findPath("crmIndicator").asText();

                                    OrdersEntity order = entityManager.find(OrdersEntity.class, orderNode.findPath("orderId").asLong());
                                    order.setStatus(statusOrder);
                                    order.setStatus(statusOrder);
                                    order.setCrmNumber(crmNumber);
                                    if (crmIndicator.equalsIgnoreCase("true")) {
                                        order.setCrmIndicator((byte) 1);
                                    } else {
                                        order.setCrmIndicator((byte) 0);
                                    }

                                    OrdersLoadingOrdersSelectionsEntity ordersLoadingOrdersSelectionsEntity = new OrdersLoadingOrdersSelectionsEntity();
                                    ordersLoadingOrdersSelectionsEntity.setCreationDate(new Date());
                                    ordersLoadingOrdersSelectionsEntity.setOrderId(orderNode.findPath("orderId").asLong());
                                    ordersLoadingOrdersSelectionsEntity.setOrderLoadingId(ordersLoadingEntity.getId());
                                    while (dromologioParIter.hasNext()) {
                                        JsonNode dromologioParNode = (JsonNode) dromologioParIter.next();
                                        finalDromologioParaggelias.add(dromologioParNode);
                                        if (dromologioParNode.findPath("type").asText().equalsIgnoreCase("Αφετηρία")) {
                                            OrderSchedulesEntity orderSchedulesEntity = entityManager.find(OrderSchedulesEntity.class,
                                                    dromologioParNode.findPath("orderScheduleId").asLong());
                                            if (!dromologioParNode.findPath("appointmentDay").asText().equalsIgnoreCase("") &&
                                                    !dromologioParNode.findPath("appointmentDay").asText().equalsIgnoreCase("Invalid date")) {
                                                String appointmentDay = dromologioParNode.findPath("appointmentDay").asText();
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                                if (appointmentDay != null && !appointmentDay.equalsIgnoreCase("")) {
                                                    try {
                                                        Date appointmentDayDate = myDateFormat.parse(appointmentDay);
                                                        orderSchedulesEntity.setAppointmentDay(appointmentDayDate);
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        } else {
                                            if (!dromologioParNode.findPath("appointmentDay").asText().equalsIgnoreCase("") &&
                                                    !dromologioParNode.findPath("appointmentDay").asText().equalsIgnoreCase("Invalid date")) {
                                                OrderWaypointsEntity orderWaypointsEntity = entityManager.find(OrderWaypointsEntity.class,
                                                        dromologioParNode.findPath("waypointId").asLong());
                                                String appointmentDay = dromologioParNode.findPath("appointmentDay").asText();
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                                if (appointmentDay != null && !appointmentDay.equalsIgnoreCase("")) {
                                                    try {
                                                        Date appointmentDayDate = myDateFormat.parse(appointmentDay);
                                                        orderWaypointsEntity.setAppointmentDay(appointmentDayDate);
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    entityManager.persist(ordersLoadingOrdersSelectionsEntity);
                                }

                                while (doneListIt.hasNext()) {
                                    JsonNode orderNode = (JsonNode) doneListIt.next();
                                    OrdersEntity order = entityManager.find(OrdersEntity.class, orderNode.findPath("orderId").asLong());

                                    OrdersLoadingOrdersSelectionsEntity ordersLoadingOrdersSelectionsEntity = new OrdersLoadingOrdersSelectionsEntity();
                                    ordersLoadingOrdersSelectionsEntity.setCreationDate(new Date());
                                    ordersLoadingOrdersSelectionsEntity.setOrderId(orderNode.findPath("orderId").asLong());
                                    ordersLoadingOrdersSelectionsEntity.setOrderLoadingId(ordersLoadingEntity.getId());
                                    Iterator dromologioParIter = orderNode.findPath("dromologioParaggelias").iterator();
                                    while (dromologioParIter.hasNext()) {
                                        JsonNode dromologioParNode = (JsonNode) dromologioParIter.next();
                                        finalDromologioParaggelias.add(dromologioParNode);
                                        if (dromologioParNode.findPath("type").asText().equalsIgnoreCase("Αφετηρία")) {
                                            if (!dromologioParNode.findPath("appointmentDay").asText().equalsIgnoreCase("") &&
                                                    !dromologioParNode.findPath("appointmentDay").asText().equalsIgnoreCase("Invalid date")) {
                                                OrderSchedulesEntity orderSchedulesEntity = entityManager.find(OrderSchedulesEntity.class,
                                                        dromologioParNode.findPath("orderScheduleId").asLong());
                                                String appointmentDay = dromologioParNode.findPath("appointmentDay").asText();
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                                if (appointmentDay != null && !appointmentDay.equalsIgnoreCase("")) {
                                                    try {
                                                        Date appointmentDayDate = myDateFormat.parse(appointmentDay);
                                                        orderSchedulesEntity.setAppointmentDay(appointmentDayDate);
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        } else {
                                            if (!dromologioParNode.findPath("appointmentDay").asText().equalsIgnoreCase("") &&
                                                    !dromologioParNode.findPath("appointmentDay").asText().equalsIgnoreCase("Invalid date")) {
                                                OrderWaypointsEntity orderWaypointsEntity = entityManager.find(OrderWaypointsEntity.class,
                                                        dromologioParNode.findPath("waypointId").asLong());
                                                String appointmentDay = dromologioParNode.findPath("appointmentDay").asText();
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                                if (appointmentDay != null && !appointmentDay.equalsIgnoreCase("")) {
                                                    try {
                                                        Date appointmentDayDate = myDateFormat.parse(appointmentDay);
                                                        orderWaypointsEntity.setAppointmentDay(appointmentDayDate);
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    entityManager.persist(ordersLoadingOrdersSelectionsEntity);
                                }
                                Iterator extrasups = json.findPath("extrasups").iterator();
                                while (extrasups.hasNext()) {
                                    JsonNode extraSupNode = (JsonNode) extrasups.next();
                                    if (extraSupNode.findPath("supplierId") != null &&
                                            !extraSupNode.findPath("supplierId").asText().equalsIgnoreCase("null") &&
                                            !extraSupNode.findPath("supplierId").asText().equalsIgnoreCase("")) {
                                        OrderLoadingExtraSuppliersEntity orderEtraSup = new OrderLoadingExtraSuppliersEntity();
                                        orderEtraSup.setNaulo(extraSupNode.findPath("naulo").asDouble());
                                        if (extraSupNode.findPath("extraSupTimologioIndicator").asBoolean()) {
                                            orderEtraSup.setExtraSupTimologioIndicator((byte) 1);
                                        } else {
                                            orderEtraSup.setExtraSupTimologioIndicator((byte) 0);
                                        }
                                        orderEtraSup.setSupplierId(extraSupNode.findPath("supplierId").asLong());
                                        orderEtraSup.setOrderLoadingId(ordersLoadingEntity.getId());
                                        entityManager.persist(orderEtraSup);
                                    }
                                }

                                JsonNode dromNodeStart = finalDromologioParaggelias.get(0);
                                JsonNode dromNodeEnd = finalDromologioParaggelias.get(finalDromologioParaggelias.size() - 1);
                                String fromCountry = dromNodeStart.findPath("country").asText();
                                String toCountry = dromNodeEnd.findPath("country").asText();


                                if (fromCountry.trim().equalsIgnoreCase("Ελλάδα") && toCountry.equalsIgnoreCase("Ελλάδα")) {
                                    ordersLoadingEntity.setType("PR");
                                }
                                if (fromCountry.trim().equalsIgnoreCase("Ελλάδα") && !toCountry.trim().equalsIgnoreCase("Ελλάδα")) {
                                    ordersLoadingEntity.setType("EX");
                                }
                                if (!fromCountry.trim().equalsIgnoreCase("Ελλάδα") && toCountry.trim().equalsIgnoreCase("Ελλάδα")) {
                                    ordersLoadingEntity.setType("IM");
                                }
                                entityManager.merge(ordersLoadingEntity);


                                ObjectNode wsResult = Json.newObject();
                                reqBody.set("finalDromologioParaggelias", ow.valueToTree(finalDromologioParaggelias));
                                CompletableFuture<WSResponse> wsFuture = (CompletableFuture)
                                        ws.url(ConfigFactory.load().getString("ws_url") + "updateOrAddSuplierRoadCost")
                                                .post(reqBody).thenApplyAsync(webServiceResponse -> {
                                            return webServiceResponse;
                                        });
                                try {
                                    wsResult = (ObjectNode) wsFuture.get().asJson();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }



                                Date appointmentDayDate = null;
                                for (int i = 0; i < doneDromologia.size(); i++) {
                                    JsonNode stationNode = doneDromologia.get(i);
                                    if (!stationNode.findPath("appointmentDay2").asText().equalsIgnoreCase("")
                                            && !stationNode.findPath("appointmentDay2").asText().equalsIgnoreCase("Invalid date")) {
                                        String appointmentDay = stationNode.findPath("appointmentDay2").asText();
                                        DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                        if (appointmentDay != null && !appointmentDay.equalsIgnoreCase("")) {
                                            try {
                                                if (i == 0) {
                                                    appointmentDayDate = myDateFormat.parse(appointmentDay);
                                                } else {
                                                    if (stationNode.findPath("type").asText().equalsIgnoreCase("Αφετηρία")) {
                                                        OrderSchedulesEntity orderSchedulesEntity =
                                                                entityManager.find(OrderSchedulesEntity.class, stationNode.findPath("orderScheduleId").asLong());
                                                        if (orderSchedulesEntity.getAppointmentDay().compareTo(appointmentDayDate) == -1) {
                                                            orderSchedulesEntity.setAppointmentDay(appointmentDayDate);
                                                            entityManager.merge(orderSchedulesEntity);
                                                        }
                                                        appointmentDayDate = myDateFormat.parse(appointmentDay);
                                                    } else {
                                                        OrderWaypointsEntity orderWaypointsEntity = entityManager.find(OrderWaypointsEntity.class, stationNode.findPath("waypointId").asLong());
                                                        if (orderWaypointsEntity.getAppointmentDay().compareTo(appointmentDayDate) == -1) {
                                                            orderWaypointsEntity.setAppointmentDay(appointmentDayDate);
                                                            entityManager.merge(orderWaypointsEntity);
                                                        }
                                                        appointmentDayDate = myDateFormat.parse(appointmentDay);
                                                    }
                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }


                                add_result.put("status", "success");
                                add_result.put("ordersLoadingId", ordersLoadingEntity.getId());
                                add_result.put("aa", ordersLoadingEntity.getAa());
                                add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
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
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateOrderLoading(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                ObjectMapper ow = new ObjectMapper();
                                ObjectNode reqBody = Json.newObject();
                                String user_id = json.findPath("user_id").asText();
                                Long customerSupplierId = json.findPath("customerSupplierId").asLong();
                                Double naulo = json.findPath("naulo").asDouble();
                                Long truckTrailerId = json.findPath("truckTrailerId").asLong();
                                String commentsMaster = json.findPath("commentsMaster").asText();
                                Long ordersLoadingId = json.findPath("ordersLoadingId").asLong();
                                Long truckTractorId = json.findPath("truckTractorId").asLong(); // truckTractorId
                                boolean timologioIndicator = json.findPath("timologioIndicator").asBoolean(); // truckTractorId
                                String arithmosTimologiou = json.findPath("arithmosTimologiou").asText();
                                String status = json.findPath("statusOrderLoading").asText();

                                JsonNode doneDromologia = json.findPath("doneDromologia");
                                ((ObjectNode) json).remove("doneDromologia");


                                reqBody.put("customerSupplierId", customerSupplierId);
                                reqBody.put("naulo", naulo);
                                OrdersLoadingEntity ordersLoadingEntity = entityManager.find(OrdersLoadingEntity.class, ordersLoadingId);
                                ordersLoadingEntity.setUpdateDate(new Date());
                                ordersLoadingEntity.setStatus(status);
                                ordersLoadingEntity.setArithmosTimologiou(arithmosTimologiou);
                                if (timologioIndicator) {
                                    ordersLoadingEntity.setTimologioIndicator((byte) 1);
                                } else {
                                    ordersLoadingEntity.setTimologioIndicator((byte) 0);
                                }
                                ordersLoadingEntity.setStatus(status);
                                ordersLoadingEntity.setSupplierId(customerSupplierId);
                                ordersLoadingEntity.setNaulo(naulo);
                                ordersLoadingEntity.setSupplierTruckTrailerId(truckTrailerId);
                                ordersLoadingEntity.setSupplierTruckTractorId(truckTractorId);
                                ordersLoadingEntity.setComments(commentsMaster);
                                entityManager.merge(ordersLoadingEntity);
                                String sql = "select * from orders_loading_orders_selections olds where  olds.order_loading_id=" + ordersLoadingId;
                                List<OrdersLoadingOrdersSelectionsEntity> ordersLoadingEntityList =
                                        entityManager.createNativeQuery(sql, OrdersLoadingOrdersSelectionsEntity.class).getResultList();
                                for (OrdersLoadingOrdersSelectionsEntity ols : ordersLoadingEntityList) {
                                    OrdersEntity order = entityManager.find(OrdersEntity.class, ols.getOrderId());
                                    order.setStatus("ΣΕ ΖΗΤΗΣΗ");
                                    entityManager.merge(order);
                                    entityManager.remove(ols);
                                }

                                String sqlExtraSups = "select * from order_loading_extra_suppliers els where els.order_loading_id=" + ordersLoadingId;
                                List<OrderLoadingExtraSuppliersEntity> orderLoadingExtraSuppliersEntityList =
                                        entityManager.createNativeQuery(sqlExtraSups, OrderLoadingExtraSuppliersEntity.class).getResultList();
                                for (OrderLoadingExtraSuppliersEntity plexs : orderLoadingExtraSuppliersEntityList) {
                                    entityManager.remove(plexs);
                                }

                                Iterator doneListIt = json.findPath("doneList").iterator();
                                List<JsonNode> finalDromologioParaggelias = new ArrayList<JsonNode>();
                                while (doneListIt.hasNext()) {
                                    JsonNode orderNode = (JsonNode) doneListIt.next();
                                    Iterator dromologioParIter = orderNode.findPath("dromologioParaggelias").iterator();
                                    ((ObjectNode) orderNode).remove("dromologioParaggelias");
                                    String statusOrder = orderNode.findPath("status").asText();
                                    String crmNumber = orderNode.findPath("crmNumber").asText();
                                    String crmIndicator = orderNode.findPath("crmIndicator").asText();
                                    Long customerId = orderNode.findPath("customerId").asLong();
                                    Long billingId = orderNode.findPath("customer").findPath("billingId").asLong();
                                    CustomersSuppliersEntity customersSuppliersEntity = entityManager.find(CustomersSuppliersEntity.class, customerId);
                                    if (billingId != null && billingId != 0) {
                                        customersSuppliersEntity.setBillingId(billingId);
                                        customersSuppliersEntity.setId(customerId);
                                        entityManager.merge(customersSuppliersEntity);
                                    }

                                    OrdersEntity order = entityManager.find(OrdersEntity.class, orderNode.findPath("orderId").asLong());
                                    order.setStatus(statusOrder);
                                    order.setCrmNumber(crmNumber);
                                    if (crmIndicator.equalsIgnoreCase("true")) {
                                        order.setCrmIndicator((byte) 1);
                                    } else {
                                        order.setCrmIndicator((byte) 0);
                                    }
                                    entityManager.merge(order);
                                    OrdersLoadingOrdersSelectionsEntity ordersLoadingOrdersSelectionsEntity = new OrdersLoadingOrdersSelectionsEntity();
                                    ordersLoadingOrdersSelectionsEntity.setCreationDate(new Date());
                                    ordersLoadingOrdersSelectionsEntity.setOrderId(orderNode.findPath("orderId").asLong());
                                    ordersLoadingOrdersSelectionsEntity.setOrderLoadingId(ordersLoadingEntity.getId());


                                    while (dromologioParIter.hasNext()) {
                                        JsonNode dromologioParNode = (JsonNode) dromologioParIter.next();
                                        finalDromologioParaggelias.add(dromologioParNode);
                                        if (dromologioParNode.findPath("type").asText().equalsIgnoreCase("Αφετηρία")) {
                                            OrderSchedulesEntity orderSchedulesEntity = entityManager.find(OrderSchedulesEntity.class, dromologioParNode.findPath("orderScheduleId").asLong());
                                            Integer position = dromologioParNode.findPath("position").asInt();
                                            orderSchedulesEntity.setPosition(position);
                                            if (!dromologioParNode.findPath("appointmentDay").asText().equalsIgnoreCase("") && !dromologioParNode.findPath("appointmentDay").asText().equalsIgnoreCase("Invalid date")) {
                                                String appointmentDay = dromologioParNode.findPath("appointmentDay").asText();
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                                if (appointmentDay != null && !appointmentDay.equalsIgnoreCase("")) {
                                                    try {
                                                        Date appointmentDayDate = myDateFormat.parse(appointmentDay);
                                                        orderSchedulesEntity.setAppointmentDay(appointmentDayDate);
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }else{
                                                orderSchedulesEntity.setAppointmentDay(new Date());
                                            }
                                            entityManager.merge(orderSchedulesEntity);
                                        } else {
                                            OrderWaypointsEntity orderWaypointsEntity = entityManager.find(OrderWaypointsEntity.class, dromologioParNode.findPath("waypointId").asLong());
                                            Integer position = dromologioParNode.findPath("position").asInt();
                                            orderWaypointsEntity.setPosition(position);
                                            if (!dromologioParNode.findPath("appointmentDay").asText().equalsIgnoreCase("") && !dromologioParNode.findPath("appointmentDay").asText().equalsIgnoreCase("Invalid date")) {
                                                String appointmentDay = dromologioParNode.findPath("appointmentDay").asText();
                                                DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                                if (appointmentDay != null && !appointmentDay.equalsIgnoreCase("")) {
                                                    try {
                                                        Date appointmentDayDate = myDateFormat.parse(appointmentDay);

                                                        orderWaypointsEntity.setAppointmentDay(appointmentDayDate);
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }else{
                                                orderWaypointsEntity.setAppointmentDay(new Date());
                                            }
                                            entityManager.merge(orderWaypointsEntity);
                                        }
                                    }

                                    entityManager.persist(ordersLoadingOrdersSelectionsEntity);
                                }
                                Iterator extrasups = json.findPath("extrasups").iterator();
                                while (extrasups.hasNext()) {
                                    JsonNode extraSupNode = (JsonNode) extrasups.next();
                                    if (extraSupNode.findPath("supplierId") != null &&
                                            !extraSupNode.findPath("supplierId").asText().equalsIgnoreCase("null") &&
                                            !extraSupNode.findPath("supplierId").asText().equalsIgnoreCase("")) {
                                        OrderLoadingExtraSuppliersEntity orderEtraSup = new OrderLoadingExtraSuppliersEntity();

                                        if (extraSupNode.findPath("extraSupTimologioIndicator").asBoolean()) {
                                            orderEtraSup.setExtraSupTimologioIndicator((byte) 1);
                                        } else {
                                            orderEtraSup.setExtraSupTimologioIndicator((byte) 0);
                                        }

                                        orderEtraSup.setNaulo(extraSupNode.findPath("naulo").asDouble());
                                        orderEtraSup.setSupplierId(extraSupNode.findPath("supplierId").asLong());
                                        orderEtraSup.setOrderLoadingId(ordersLoadingId);
                                        entityManager.persist(orderEtraSup);
                                    }
                                }


                                JsonNode dromNodeStart = finalDromologioParaggelias.get(0);
                                JsonNode dromNodeEnd = finalDromologioParaggelias.get(finalDromologioParaggelias.size() - 1);
                                String fromCountry = dromNodeStart.findPath("country").asText();
                                String toCountry = dromNodeEnd.findPath("country").asText();


                                if (fromCountry.trim().equalsIgnoreCase("Ελλάδα") && toCountry.equalsIgnoreCase("Ελλάδα")) {
                                    ordersLoadingEntity.setType("PR");
                                }
                                if (fromCountry.trim().equalsIgnoreCase("Ελλάδα") && !toCountry.trim().equalsIgnoreCase("Ελλάδα")) {
                                    ordersLoadingEntity.setType("EX");
                                }
                                if (!fromCountry.trim().equalsIgnoreCase("Ελλάδα") && toCountry.trim().equalsIgnoreCase("Ελλάδα")) {
                                    ordersLoadingEntity.setType("IM");
                                }
                                entityManager.merge(ordersLoadingEntity);

                                ObjectNode wsResult = Json.newObject();
                                reqBody.set("finalDromologioParaggelias", ow.valueToTree(finalDromologioParaggelias));
                                reqBody.put("idOrderLoading", ordersLoadingEntity.getId());
                                CompletableFuture<WSResponse> wsFuture = (CompletableFuture)
                                        ws.url(ConfigFactory.load().getString("ws_url") + "updateOrAddSuplierRoadCost")
                                                .post(reqBody).thenApplyAsync(webServiceResponse -> {
                                            return webServiceResponse;
                                        });
                                try {
                                    wsResult = (ObjectNode) wsFuture.get().asJson();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }

                                Date appointmentDayDate = null;
                                for (int i = 0; i < doneDromologia.size(); i++) {
                                    JsonNode stationNode = doneDromologia.get(i);
                                    if (!stationNode.findPath("appointmentDay2").asText().equalsIgnoreCase("")
                                            && !stationNode.findPath("appointmentDay2").asText().equalsIgnoreCase("Invalid date")) {
                                        String appointmentDay = stationNode.findPath("appointmentDay2").asText();
                                        DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                        if (appointmentDay != null && !appointmentDay.equalsIgnoreCase("")) {
                                            try {
                                                if (i == 0) {
                                                    appointmentDayDate = myDateFormat.parse(appointmentDay);
                                                } else {
                                                    if (stationNode.findPath("type").asText().equalsIgnoreCase("Αφετηρία")) {
                                                        OrderSchedulesEntity orderSchedulesEntity =
                                                                entityManager.find(OrderSchedulesEntity.class, stationNode.findPath("orderScheduleId").asLong());
                                                        if (orderSchedulesEntity.getAppointmentDay().compareTo(appointmentDayDate) == -1) {
                                                            orderSchedulesEntity.setAppointmentDay(appointmentDayDate);
                                                            entityManager.merge(orderSchedulesEntity);
                                                        }
                                                        appointmentDayDate = myDateFormat.parse(appointmentDay);
                                                    } else {
                                                        OrderWaypointsEntity orderWaypointsEntity = entityManager.find(OrderWaypointsEntity.class, stationNode.findPath("waypointId").asLong());
                                                        if (orderWaypointsEntity.getAppointmentDay().compareTo(appointmentDayDate) == -1) {
                                                            orderWaypointsEntity.setAppointmentDay(appointmentDayDate);
                                                            entityManager.merge(orderWaypointsEntity);
                                                        }
                                                        appointmentDayDate = myDateFormat.parse(appointmentDay);
                                                    }
                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                                add_result.put("status", "success");
                                add_result.put("ordersLoadingId", ordersLoadingEntity.getId());
                                add_result.put("message", "Η ενημέρωση πραγματοποίηθηκε με επιτυχία");
                                add_result.put("DO_ID", ordersLoadingId);
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
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateOrAddSuplierRoadCost(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode add_result = Json.newObject();


                                    if (json.findPath("finalDromologioParaggelias").size() == 0) {
                                        add_result.put("status", "error");
                                        add_result.put("message", "Δεν βρέθηκε τελικό δρομολόγιο");
                                        return add_result;
                                    }


                                    Long customerSupplierId = json.findPath("customerSupplierId").asLong();
                                    Double naulo = json.findPath("naulo").asDouble();


                                    String fromCity = "";
                                    String fromCountry = "";
                                    String toCity = "";
                                    String toCountry = "";
                                    JsonNode dromNodeStart = json.findPath("finalDromologioParaggelias").get(0);
                                    JsonNode dromNodeEnd = json.findPath("finalDromologioParaggelias").get(json.findPath("finalDromologioParaggelias").size() - 1);
                                    fromCity = dromNodeStart.findPath("city").asText();
                                    fromCountry = dromNodeStart.findPath("country").asText();
                                    toCity = dromNodeEnd.findPath("city").asText();
                                    toCountry = dromNodeEnd.findPath("country").asText();


                                    SuppliersRoadsCostsEntity suppliersRoadsCostsEntity = new SuppliersRoadsCostsEntity();
                                    suppliersRoadsCostsEntity.setFromCity(fromCity);
                                    suppliersRoadsCostsEntity.setFromCountry(fromCountry);
                                    suppliersRoadsCostsEntity.setToCity(toCity);
                                    suppliersRoadsCostsEntity.setCreationDate(new Date());
                                    suppliersRoadsCostsEntity.setToCountry(toCountry);
                                    suppliersRoadsCostsEntity.setCustomersSuppliersId(customerSupplierId);
                                    suppliersRoadsCostsEntity.setCost(naulo);

                                    entityManager.persist(suppliersRoadsCostsEntity);


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
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την καταχωρηση");
            return ok(result);
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
                                            DecimalFormat df = new DecimalFormat("###.#");
                                            String availableOrdersSearch = json.findPath("availableOrdersSearch").asText();
                                            String sqlAvailablesOrders = "select * from orders ord where 1=1  ";
                                            String orderId = json.findPath("orderId").asText();
                                            String ordersLoadingId = json.findPath("ordersLoadingId").asText();
                                            if (orderId != null && !orderId.equalsIgnoreCase("")) {
                                                sqlAvailablesOrders += " and  ord.id=" + orderId;
                                            }
                                            if (ordersLoadingId != null && !ordersLoadingId.equalsIgnoreCase("")) {
                                                sqlAvailablesOrders += " and  ord.id not in (select order_id from orders_loading_orders_selections ) ";
                                            }
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
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                            List<OrdersEntity> ordersEntityList
                                                    = (List<OrdersEntity>) entityManager.createNativeQuery(
                                                    sqlAvailablesOrders, OrdersEntity.class).getResultList();
                                            for (OrdersEntity j : ordersEntityList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                List<HashMap<String, Object>> dromologioParaggelias = new ArrayList<HashMap<String, Object>>();

                                                String sqlPack = "select * from orders_selections_by_point osp where osp.order_id= " + j.getId();
                                                List<OrdersSelectionsByPointEntity> ordersPackages = entityManager.createNativeQuery(sqlPack, OrdersSelectionsByPointEntity.class).getResultList();
                                                List<String> typePack = new ArrayList<String>();
                                                for (OrdersSelectionsByPointEntity op : ordersPackages) {
                                                    typePack.add(entityManager.find(PackageTypeEntity.class, op.getPackageTypeId()).getType());
                                                }
                                                sHmpam.put("typePack", typePack);
                                                if (typePack.size() > 0) {
                                                    sHmpam.put("firstTypePack", typePack.get(0));
                                                } else {
                                                    sHmpam.put("firstTypePack", "-");
                                                }
                                                sHmpam.put("orderId", j.getId());
                                                if (j.getCrmNumber() != null && !j.getCrmNumber().equalsIgnoreCase("null")) {
                                                    sHmpam.put("crmNumber", j.getCrmNumber());

                                                } else {
                                                    sHmpam.put("crmNumber", "");

                                                }
                                                if (j.getCrmIndicator() == 1) {
                                                    sHmpam.put("crmIndicator", true);
                                                } else {
                                                    sHmpam.put("crmIndicator", false);
                                                }
                                                sHmpam.put("customerId", j.getCustomerId());
                                                sHmpam.put("customer", entityManager.find(CustomersSuppliersEntity.class, j.getCustomerId()));
                                                if (entityManager.find(CustomersSuppliersEntity.class, j.getCustomerId()).getBillingId() != null) {
                                                    sHmpam.put("customerBilling", entityManager.find(BillingsEntity.class, entityManager.find(CustomersSuppliersEntity.class, j.getCustomerId()).getBillingId()).getName());
                                                } else {
                                                    sHmpam.put("customerBilling", "-");
                                                }
                                                sHmpam.put("status", j.getStatus());
                                                sHmpam.put("showDromologioIndicator", false);
                                                sHmpam.put("type", j.getType());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                sHmpam.put("truckTemprature", j.getTruckTemprature());
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
                                                            orderSchedulesEntityList.get(0).getFromCity() + "  ->  " +
                                                            orderSchedulesEntityList.get(0).getToCountry() + " " +
                                                            orderSchedulesEntityList.get(0).getToCity());
                                                    sHmpam.put("fromAddressLabel", orderSchedulesEntityList.get(0).getFromCountry() + ","
                                                            + orderSchedulesEntityList.get(0).getFromCity() + " " + orderSchedulesEntityList.get(0).getFromPostalCode());
                                                    sHmpam.put("fromAppointmentDay", orderSchedulesEntityList.get(0).getAppointmentDay());
                                                    sHmpam.put("fromAppointment", orderSchedulesEntityList.get(0).getAppointment());
                                                    sHmpam.put("fromCountry", orderSchedulesEntityList.get(0).getFromCountry());
                                                    sHmpam.put("fromCity", orderSchedulesEntityList.get(0).getFromCity());
                                                    if (orderSchedulesEntityList.get(0).getFactoryId() != null) {
                                                        FactoriesEntity factory = entityManager.find(FactoriesEntity.class, orderSchedulesEntityList.get(0).getFactoryId());
                                                        sHmpam.put("appointmentRequired", factory.getAppointmentRequired());
                                                        sHmpam.put("appointmentDays", factory.getAppointmentDays());
                                                    } else {
                                                        sHmpam.put("appointmentRequired", 0);
                                                        sHmpam.put("appointmentDays", "-");
                                                    }
                                                } else {
                                                    sHmpam.put("mainSchedule", "-");
                                                    sHmpam.put("fromAddressLabel", "-");
                                                    sHmpam.put("fromAppointmentDay", "-");
                                                    sHmpam.put("fromAppointment", "-");
                                                    sHmpam.put("appointmentRequired", 0);
                                                    sHmpam.put("appointmentDays", "-");
                                                    sHmpam.put("fromCountry", "-");
                                                    sHmpam.put("fromCity", "-");
                                                }
                                                //   String sqlMasterSchedule = "select  osp.unit_price from orders_selections_by_point osp where osp.order_id="+ j.getId()+" limit 1" ;
                                                String sqlMasterSchedule = "select  * from orders_selections_by_point osp where osp.order_id=" + j.getId() + " and  osp.order_waypoint_id is null limit 1";
                                                Double summMasterSchedule = 0.0;
                                                List<OrdersSelectionsByPointEntity> ordersSelectionsByPointEntityList = entityManager.createNativeQuery(sqlMasterSchedule, OrdersSelectionsByPointEntity.class).getResultList();
                                                if (ordersSelectionsByPointEntityList.size() > 0) {
                                                    sHmpam.put("summMasterSchedule", ordersSelectionsByPointEntityList.get(0).getUnitPrice());
                                                    String sqlSumPrice = "select  sum(osp.unit_price) from orders_selections_by_point osp where osp.order_id=" + j.getId();
                                                    Double summPrice = (Double) entityManager.createNativeQuery(sqlSumPrice).getSingleResult();
                                                    if (summPrice != null) {
                                                        sHmpam.put("summPriceNested", summPrice - ordersSelectionsByPointEntityList.get(0).getUnitPrice());
                                                    } else {
                                                        sHmpam.put("summPriceNested", "0.0");
                                                    }
                                                } else {
                                                    sHmpam.put("summMasterSchedule", "0.0");
                                                    sHmpam.put("summPriceNested", "0.0");
                                                }
                                                String sqlSumLdm = "select  sum(osp.ldm) from orders_selections_by_point osp where osp.order_id=" + j.getId();
                                                Double summLdm = (Double) entityManager.createNativeQuery(sqlSumLdm).getSingleResult();
                                                if (summLdm != null) {
                                                    sHmpam.put("summLdm", df.format(summLdm));
                                                } else {
                                                    sHmpam.put("summLdm", "0.0");
                                                }
                                                String sqlSumQuantity = "select  sum(osp.quantity) from orders_selections_by_point osp where osp.order_id=" + j.getId() + " and osp.type_package!='Βάσει βάρους επί καθαρού (kg)' and osp.type_package!='Βάσει βάρους επί μικτού (kg)'";


                                                BigDecimal summQuantity = (BigDecimal) entityManager.createNativeQuery(sqlSumQuantity).getSingleResult();
                                                if (summQuantity != null) {
                                                    sHmpam.put("summQuantity", summQuantity);
                                                } else {
                                                    sHmpam.put("summQuantity", "0");
                                                }
                                                String sqlSumQuantityKg = "select  sum(osp.quantity) from orders_selections_by_point osp where osp.order_id=" + j.getId() + " and ( osp.type_package='Βάσει βάρους επί καθαρού (kg)' ) ";
                                                BigDecimal summQuantityKg = (BigDecimal) entityManager.createNativeQuery(sqlSumQuantityKg).getSingleResult();
                                                if (summQuantityKg != null) {
                                                    sHmpam.put("summQuantityKg", summQuantityKg);
                                                } else {
                                                    sHmpam.put("summQuantityKg", "0");
                                                }

                                                String sqlsummQuantityKgNet = "select  sum(osp.quantity) from orders_selections_by_point osp where osp.order_id=" + j.getId() + " and ( osp.type_package='Βάσει βάρους επί μικτού (kg)' ) ";
                                                BigDecimal summQuantityKgNet = (BigDecimal) entityManager.createNativeQuery(sqlsummQuantityKgNet).getSingleResult();
                                                if (summQuantityKgNet != null) {
                                                    sHmpam.put("summQuantityKgNet", summQuantityKgNet);
                                                } else {
                                                    sHmpam.put("summQuantityKgNet", "0");
                                                }


                                                String attatchmentsCountSql = "select  count(*) from documents docs where docs.system='orders' and  docs.sub_folder_id=" + j.getId();
                                                BigInteger attatchmentsCount = (BigInteger) entityManager.createNativeQuery(attatchmentsCountSql).getSingleResult();
                                                if (attatchmentsCount != null) {
                                                    sHmpam.put("attatchmentsCount", attatchmentsCount);
                                                } else {
                                                    sHmpam.put("attatchmentsCount", "0");
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
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> finalList = new ArrayList<HashMap<String, Object>>();
                                            List<OrderSchedulesEntity> orderSchedulesEntityList;
                                            Double finalSummPrice = 0.0;
                                            Double finalSummLdm = 0.0;
                                            Integer finalSummQuantity = 0;
                                            Integer finalSummQuantityKg = 0;
                                            Integer finalSummQuantityKgNet = 0;
                                            DecimalFormat df;
                                            String sqlSumPrice = "";
                                            Double summPrice;
                                            Double summMasterSchedule;
                                            String sqlSumLdm;
                                            Double summLdm;
                                            String sqlSumQuantity;
                                            String sqlSumQuantityKg;
                                            String sqlsummQuantityKgNet;
                                            BigDecimal summQuantity;
                                            BigDecimal summQuantityKg;
                                            BigDecimal summQuantityKgNet;
                                            String sqlPackages;
                                            List<OrdersSelectionsByPointEntity> ordersSelectionsByPointEntityList;
                                            List<HashMap<String, Object>> packagesFortwshs;
                                            List<HashMap<String, Object>> packagesEkfortwshs;
                                            List<HashMap<String, Object>> allPackages;
                                            String sql = "select * from order_schedules ords where ords.order_id="
                                                    + orderId;
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
                                                    osMap.put("orderId", os.getOrderId());
                                                    osMap.put("orderScheduleId", os.getId());
                                                    osMap.put("appointmentDay", os.getAppointmentDay());
                                                    osMap.put("position", os.getPosition());
                                                    osMap.put("appointment", os.getAppointment());
                                                    osMap.put("postalCode", entityManager.find(FactoriesEntity.class, os.getFactoryId()).getPostalCode());
                                                    OrdersEntity ordersEntity = entityManager.find(OrdersEntity.class, json.findPath("orderId").asLong());
                                                    CustomersSuppliersEntity customersSuppliersEntity = entityManager.find(CustomersSuppliersEntity.class, ordersEntity.getCustomerId());
                                                    osMap.put("orderCustomerName", customersSuppliersEntity.getBrandName());
                                                    osMap.put("status", "success");
                                                    osMap.put("message", "success");

                                                } else {
                                                    osMap.put("status", "error");
                                                    osMap.put("message", "Δεν έχει οριστεί εργοστάσιο ή αποθήκη ");
                                                    osMap.put("city", os.getFromCity());
                                                    osMap.put("country", os.getFromCountry());
                                                    osMap.put("appointmentDay", os.getAppointmentDay());
                                                    osMap.put("position", os.getPosition());
                                                    osMap.put("appointment", os.getAppointment());
                                                    osMap.put("orderId", os.getOrderId());
                                                    osMap.put("orderScheduleId", os.getId());
                                                    osMap.put("postalCode", os.getFromPostalCode());
                                                    OrdersEntity ordersEntity = entityManager.find(OrdersEntity.class, json.findPath("orderId").asLong());
                                                    CustomersSuppliersEntity customersSuppliersEntity = entityManager.find(CustomersSuppliersEntity.class, ordersEntity.getCustomerId());
                                                    osMap.put("orderCustomerName", customersSuppliersEntity.getBrandName());
                                                    osMap.put("brandName", "Δεν έχει οριστεί");
                                                    osMap.put("address", "Δεν έχει οριστεί");
                                                }

                                                finalSummPrice = 0.0;
                                                finalSummLdm = 0.0;
                                                finalSummQuantity = 0;
                                                finalSummQuantityKg = 0;
                                                finalSummQuantityKgNet = 0;

                                                df = new DecimalFormat("###.#");
                                                sqlSumPrice = "select  sum(osbp.unit_price) from orders_selections_by_point osbp where osbp.order_schedule_id=" + os.getId() + " and osbp.order_waypoint_id is null";
                                                summPrice = (Double) entityManager.createNativeQuery(sqlSumPrice).getSingleResult();
                                                if (summPrice != null) {
                                                    finalSummPrice = finalSummPrice + summPrice;
                                                    osMap.put("finalSummPrice", df.format(finalSummPrice));
                                                } else {
                                                    osMap.put("finalSummPrice", df.format(finalSummPrice));
                                                }
                                                sqlSumLdm = "select  sum(osbp.ldm) from orders_selections_by_point osbp where osbp.order_schedule_id=" + os.getId() + " and osbp.order_waypoint_id is null";
                                                summLdm = (Double) entityManager.createNativeQuery(sqlSumLdm).getSingleResult();
                                                if (summLdm != null) {
                                                    finalSummLdm = finalSummLdm + summLdm;
                                                    osMap.put("finalSummLdm", df.format(finalSummLdm));
                                                } else {
                                                    osMap.put("finalSummLdm", df.format(finalSummLdm));
                                                }

                                                sqlSumQuantity = "select  sum(osbp.quantity) from orders_selections_by_point osbp where osbp.order_schedule_id=" + os.getId() + " and osbp.type_package!='Βάσει βάρους επί καθαρού (kg)' and osbp.type_package!='Βάσει βάρους επί μικτού (kg)'  and osbp.order_waypoint_id is null";
                                                summQuantity = (BigDecimal) entityManager.createNativeQuery(sqlSumQuantity).getSingleResult();
                                                if (summQuantity != null) {
                                                    finalSummQuantity = finalSummQuantity + summQuantity.intValue();
                                                    osMap.put("finalSummQuantity", finalSummQuantity);
                                                } else {
                                                    osMap.put("finalSummQuantity", finalSummQuantity);
                                                }
                                                sqlSumQuantityKg = "select  sum(osbp.quantity) from orders_selections_by_point osbp where osbp.order_schedule_id=" + os.getId() + " and ( osbp.type_package='Βάσει βάρους επί καθαρού (kg)' )  and osbp.order_waypoint_id is null";


                                                summQuantityKg = (BigDecimal) entityManager.createNativeQuery(sqlSumQuantityKg).getSingleResult();
                                                if (summQuantityKg != null) {
                                                    finalSummQuantityKg = finalSummQuantityKg + summQuantityKg.intValue();
                                                    osMap.put("finalSummQuantityKg", finalSummQuantityKg);
                                                } else {
                                                    osMap.put("finalSummQuantityKg", finalSummQuantityKg);
                                                }


                                                sqlsummQuantityKgNet = "select  sum(osbp.quantity) from orders_selections_by_point osbp where osbp.order_schedule_id=" + os.getId() + " and ( osbp.type_package='Βάσει βάρους επί μικτού (kg)' )  and osbp.order_waypoint_id is null";
                                                summQuantityKgNet = (BigDecimal) entityManager.createNativeQuery(sqlsummQuantityKgNet).getSingleResult();
                                                if (summQuantityKgNet != null) {
                                                    finalSummQuantityKgNet = finalSummQuantityKgNet + summQuantityKgNet.intValue();
                                                    osMap.put("finalSummQuantityKgNet", finalSummQuantityKgNet);
                                                } else {
                                                    osMap.put("finalSummQuantityKgNet", finalSummQuantityKgNet);
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

                                                    osafetmap.put("packageType", entityManager.find(PackageTypeEntity.class, osafet.getPackageTypeId()).getType());
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
                                                osMap.put("timelinetype", "Αφετηρία");
                                                osMap.put("nestedScheduleIndicator", 0);
                                                finalList.add(osMap);
                                                String sqlWp1 = "select * from order_waypoints ow where ow.order_id=" + orderId
                                                        + " and ow.order_schedule_id=" + os.getId();
                                                List<OrderWaypointsEntity> orderWaypointsEntityList1 = entityManager.createNativeQuery(sqlWp1, OrderWaypointsEntity.class).getResultList();
                                                for (OrderWaypointsEntity owpe : orderWaypointsEntityList1) {
                                                    HashMap<String, Object> owpeMap = new HashMap<String, Object>();
                                                    if (owpe.getFactoryId() != null) {
                                                        owpeMap.put("longtitude", entityManager.find(FactoriesEntity.class, owpe.getFactoryId()).getLongtitude());
                                                        owpeMap.put("lattitude", entityManager.find(FactoriesEntity.class, owpe.getFactoryId()).getLattitude());
                                                        owpeMap.put("brandName", entityManager.find(FactoriesEntity.class, owpe.getFactoryId()).getBrandName());
                                                        owpeMap.put("city", entityManager.find(FactoriesEntity.class, owpe.getFactoryId()).getCity());
                                                        owpeMap.put("country", entityManager.find(FactoriesEntity.class, owpe.getFactoryId()).getCountry());
                                                        owpeMap.put("orderId", owpe.getOrderId());
                                                        owpeMap.put("waypointId", owpe.getId());
                                                        owpeMap.put("appointmentDay", owpe.getAppointmentDay());
                                                        owpeMap.put("position", owpe.getPosition());
                                                        owpeMap.put("appointment", owpe.getAppointment());
                                                        owpeMap.put("address", entityManager.find(FactoriesEntity.class, owpe.getFactoryId()).getAddress());
                                                        owpeMap.put("postalCode", entityManager.find(FactoriesEntity.class, owpe.getFactoryId()).getPostalCode());
                                                        OrdersEntity ordersEntity = entityManager.find(OrdersEntity.class, json.findPath("orderId").asLong());
                                                        CustomersSuppliersEntity customersSuppliersEntity = entityManager.find(CustomersSuppliersEntity.class, ordersEntity.getCustomerId());
                                                        owpeMap.put("orderCustomerName", customersSuppliersEntity.getBrandName());
                                                        owpeMap.put("status", "success");
                                                        owpeMap.put("message", "success");
                                                    } else {
                                                        owpeMap.put("status", "error");
                                                        owpeMap.put("message", "Δεν έχει οριστεί εργοστάσιο ή αποθήκη ");
                                                        owpeMap.put("city", owpe.getCity());
                                                        owpeMap.put("orderId", owpe.getOrderId());
                                                        owpeMap.put("waypointId", owpe.getId());
                                                        owpeMap.put("country", owpe.getCountry());
                                                        owpeMap.put("appointmentDay", owpe.getAppointmentDay());
                                                        owpeMap.put("position", owpe.getPosition());
                                                        owpeMap.put("appointment", owpe.getAppointment());
                                                        owpeMap.put("postalCode", owpe.getPostalCode());
                                                        OrdersEntity ordersEntity = entityManager.find(OrdersEntity.class, json.findPath("orderId").asLong());
                                                        CustomersSuppliersEntity customersSuppliersEntity = entityManager.find(CustomersSuppliersEntity.class, ordersEntity.getCustomerId());
                                                        owpeMap.put("orderCustomerName", customersSuppliersEntity.getBrandName());
                                                        owpeMap.put("brandName", "Δεν έχει οριστεί");
                                                        owpeMap.put("address", "Δεν έχει οριστεί");
                                                    }
                                                    if (owpe.getOfferScheduleBetweenWaypointId() != null) {
                                                        owpeMap.put("type", "Ενδιάμεσο Σημείο");
                                                        owpeMap.put("timelinetype", "Ενδ/σο σημείο");
                                                        owpeMap.put("nestedScheduleIndicator", owpe.getNestedScheduleIndicator());
                                                    } else {
                                                        if (owpe.getNewWaypoint() != null && owpe.getNewWaypoint() == 1) {
                                                            owpeMap.put("type", "Ενδιάμεσο Σημείο");
                                                            owpeMap.put("timelinetype", "Ενδ/σο σημείο");
                                                            owpeMap.put("nestedScheduleIndicator", owpe.getNestedScheduleIndicator());
                                                        } else {
                                                            owpeMap.put("type", "Τελικός Προορισμός");
                                                            owpeMap.put("timelinetype", "Προορισμός");
                                                            owpeMap.put("nestedScheduleIndicator", owpe.getNestedScheduleIndicator());
                                                        }
                                                    }
                                                    finalSummPrice = 0.0;
                                                    finalSummLdm = 0.0;
                                                    finalSummQuantity = 0;
                                                    finalSummQuantityKg = 0;
                                                    df = new DecimalFormat("###.#");
                                                    sqlSumPrice = "select  sum(osbp.unit_price) from orders_selections_by_point osbp where osbp.order_schedule_id=" + owpe.getOrderScheduleId() + " and osbp.order_waypoint_id =" + owpe.getId();
                                                    summPrice = (Double) entityManager.createNativeQuery(sqlSumPrice).getSingleResult();
                                                    if (summPrice != null) {
                                                        finalSummPrice = finalSummPrice + summPrice;
                                                        owpeMap.put("finalSummPrice", df.format(finalSummPrice));
                                                    } else {
                                                        owpeMap.put("finalSummPrice", df.format(finalSummPrice));
                                                    }
                                                    sqlSumLdm = "select  sum(osbp.ldm) from orders_selections_by_point osbp where osbp.order_schedule_id=" + owpe.getOrderScheduleId() + " and osbp.order_waypoint_id =" + owpe.getId();
                                                    summLdm = (Double) entityManager.createNativeQuery(sqlSumLdm).getSingleResult();
                                                    if (summLdm != null) {
                                                        finalSummLdm = finalSummLdm + summLdm;
                                                        owpeMap.put("finalSummLdm", df.format(finalSummLdm));
                                                    } else {
                                                        owpeMap.put("finalSummLdm", df.format(finalSummLdm));
                                                    }
                                                    sqlSumQuantity = "select  sum(osbp.quantity) from orders_selections_by_point osbp where osbp.order_schedule_id=" + owpe.getOrderScheduleId() + " and osbp.type_package!='Βάσει βάρους επί καθαρού (kg)' and  osbp.type_package!='Βάσει βάρους επί μικτού (kg)' " + " and osbp.order_waypoint_id =" + owpe.getId();
                                                    summQuantity = (BigDecimal) entityManager.createNativeQuery(sqlSumQuantity).getSingleResult();
                                                    if (summQuantity != null) {
                                                        finalSummQuantity = finalSummQuantity + summQuantity.intValue();
                                                        owpeMap.put("finalSummQuantity", finalSummQuantity);
                                                    } else {
                                                        owpeMap.put("finalSummQuantity", finalSummQuantity);
                                                    }


                                                    sqlSumQuantityKg = "select  sum(osbp.quantity) from orders_selections_by_point osbp where osbp.order_schedule_id=" + owpe.getOrderScheduleId() + "  and ( osbp.type_package='Βάσει βάρους επί καθαρού (kg)' )  " + " and osbp.order_waypoint_id =" + owpe.getId();
                                                    summQuantityKg = (BigDecimal) entityManager.createNativeQuery(sqlSumQuantityKg).getSingleResult();
                                                    if (summQuantityKg != null) {
                                                        finalSummQuantityKg = finalSummQuantityKg + summQuantityKg.intValue();
                                                        owpeMap.put("finalSummQuantityKg", finalSummQuantityKg);
                                                    } else {
                                                        owpeMap.put("finalSummQuantityKg", finalSummQuantityKg);
                                                    }


                                                    sqlsummQuantityKgNet = "select  sum(osbp.quantity) from orders_selections_by_point osbp where osbp.order_schedule_id=" + owpe.getOrderScheduleId() + "  and (  osbp.type_package='Βάσει βάρους επί μικτού (kg)' )  " + " and osbp.order_waypoint_id =" + owpe.getId();
                                                    summQuantityKgNet = (BigDecimal) entityManager.createNativeQuery(sqlsummQuantityKgNet).getSingleResult();
                                                    if (summQuantityKgNet != null) {
                                                        finalSummQuantityKgNet = finalSummQuantityKgNet + summQuantityKgNet.intValue();
                                                        owpeMap.put("finalSummQuantityKgNet", finalSummQuantityKgNet);
                                                    } else {
                                                        owpeMap.put("finalSummQuantityKgNet", finalSummQuantityKgNet);
                                                    }


                                                    sqlPackages = "select * from orders_selections_by_point osbp " +
                                                            "where osbp.order_schedule_id=" + owpe.getOrderScheduleId()
                                                            + " and osbp.order_waypoint_id =" + owpe.getId();
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
                                                        osafetmap.put("packageType", entityManager.find(PackageTypeEntity.class, osafet.getPackageTypeId()).getType());
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
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Πρόβλημα κατά την ανάγνωση των στοιχείων");
            return ok(result);
        }

    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getNauloBySuplierAndSchedule(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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

                                        String fromCity = json.findPath("fromCity").asText();
                                        String fromCountry = json.findPath("fromCountry").asText();
                                        String toCity = json.findPath("toCity").asText();
                                        String toCountry = json.findPath("toCountry").asText();
                                        String suplierId = json.findPath("suplierId").asText();
                                        if (suplierId != null && !suplierId.equalsIgnoreCase("")) {
                                            String sqlOrdLoads = "select * " +
                                                    " from suppliers_roads_costs src " +
                                                    " where src.customers_suppliers_id=" + suplierId +
                                                    " and src.from_city='" + fromCity.trim() + "'" +
                                                    " and src.from_country='" + fromCountry.trim() + "' " +
                                                    " and src.to_city='" + toCity.trim() + "'" +
                                                    " and src.to_country='" + toCountry.trim() + "' order by id desc";

                                            List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                            List<SuppliersRoadsCostsEntity> suppliersRoadsCostsEntityList
                                                    = (List<SuppliersRoadsCostsEntity>) entityManager.createNativeQuery(
                                                    sqlOrdLoads, SuppliersRoadsCostsEntity.class).getResultList();

                                            if (suppliersRoadsCostsEntityList.size() > 0) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("id", suppliersRoadsCostsEntityList.get(0).getId());
                                                sHmpam.put("cost", suppliersRoadsCostsEntityList.get(0).getCost());
                                                serversList.add(sHmpam);
                                            }

                                            returnList_future.put("data", serversList);
                                            returnList_future.put("status", "success");
                                            returnList_future.put("message", "success");
                                            return returnList_future;
                                        } else {
                                            returnList_future.put("status", "error");
                                            returnList_future.put("message", "Δεν έχετε αποστείλει id προμηθευτή");
                                            return returnList_future;
                                        }
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
                                        String orderCol = json.findPath("orderCol").asText();
                                        String descAsc = json.findPath("descAsc").asText();
                                        String id = json.findPath("id").asText();
                                        String idOrderSearch = json.findPath("idOrderSearch").asText();
                                        String supplierNameSearch = json.findPath("supplierNameSearch").asText();
                                        String truckTrailerNameSearch = json.findPath("truckTrailerNameSearch").asText();
                                        String truckTractorNameSearch = json.findPath("truckTractorNameSearch").asText();
                                        String aa = json.findPath("aa").asText();
                                        String start = json.findPath("start").asText();
                                        String limit = json.findPath("limit").asText();
                                        String sqlOrdLoads = "select * from orders_loading ord_load   where 1=1 ";
                                        if (!id.equalsIgnoreCase("") && id != null) {
                                            sqlOrdLoads += " and ord_load.id =" + id;
                                        }
                                        if (aa != null && !aa.equalsIgnoreCase("")) {
                                            sqlOrdLoads += " and  ord_load.aa like '%" + aa + "%' ";
                                        }
                                        if (idOrderSearch != null && !idOrderSearch.equalsIgnoreCase("")) {
                                            sqlOrdLoads += " and  ord_load.id in " +
                                                    " (select olds.order_loading_id from orders_loading_orders_selections olds " +
                                                    "  where olds.order_id like '%" + idOrderSearch + "%' ) " +
                                                    " or " +
                                                    " ord_load.id " +
                                                    " in  " +
                                                    " ( " +
                                                    " select " +
                                                    " olds.order_loading_id " +
                                                    " from orders_loading_orders_selections olds " +
                                                    " where olds.order_id " +
                                                    " in ( " +
                                                    " select id from orders where customer_id in " +
                                                    " ( " +
                                                    " select id from customers_suppliers where brand_name like '%" + idOrderSearch + "%'" +
                                                    " ) " +
                                                    " )" +
                                                    " )";
                                        }

                                        if (truckTractorNameSearch != null && !truckTractorNameSearch.equalsIgnoreCase("")) {
                                            sqlOrdLoads += " and  ord_load.supplier_truck_tractor_id " + " in ( select t.id from trucks t  where t.brand_name   like '%" + truckTractorNameSearch + "%'   )";
                                        }
                                        if (truckTrailerNameSearch != null && !truckTrailerNameSearch.equalsIgnoreCase("")) {
                                            sqlOrdLoads += " and  ord_load.supplier_truck_tractor_id  in ( select t.id from trucks t  where t.brand_name   like '%" + truckTrailerNameSearch + "%'   )";
                                        }
                                        if (supplierNameSearch != null && !supplierNameSearch.equalsIgnoreCase("")) {
                                            sqlOrdLoads += " and ord_load.supplier_id in (  select cs.id   from customers_suppliers cs where cs.brand_name like '%" + supplierNameSearch + "%' ) ";
                                        }
                                        List<OrdersLoadingEntity> ordersLoadingAllList
                                                = (List<OrdersLoadingEntity>) entityManager.createNativeQuery(
                                                sqlOrdLoads, OrdersLoadingEntity.class).getResultList();
                                        if (!orderCol.equalsIgnoreCase("") && orderCol != null) { //ordersSums
                                            if (orderCol.equalsIgnoreCase("supplier")) {
                                                sqlOrdLoads += " order by (select cs.brand_name from customers_suppliers cs where cs.id=ord_load.supplier_id) " + descAsc;
                                            } else if (orderCol.equalsIgnoreCase("truckTrailer")) {
                                                sqlOrdLoads += " order by (select tr.brand_name from trucks tr where tr.id=ord_load.supplier_truck_trailer_id) " + descAsc;
                                            } else if (orderCol.equalsIgnoreCase("truckTractor")) {
                                                sqlOrdLoads += " order by (select tr.brand_name from trucks tr where tr.id=ord_load.supplier_truck_tractor_id) " + descAsc;
                                            } else if (orderCol.equalsIgnoreCase("ordersSums")) {
                                                sqlOrdLoads += " order by (select sum(ldm) from " +
                                                        " orders_selections_by_point osbp " +
                                                        " where osbp.order_id in" +
                                                        " (select olos.order_id " +
                                                        " from orders_loading_orders_selections olos " +
                                                        " where olos.order_loading_id=ord_load.id " +
                                                        " )) " + descAsc;
                                            } else {
                                                sqlOrdLoads += " order by creation_date " + descAsc;
                                            }
                                        } else {
                                            sqlOrdLoads += " order by creation_date desc";
                                        }
                                        if (!start.equalsIgnoreCase("") && start != null) {
                                            sqlOrdLoads += " limit " + start + "," + limit;
                                        }
                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                        List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                        List<OrdersLoadingEntity> ordersLoadingList
                                                = (List<OrdersLoadingEntity>) entityManager.createNativeQuery(
                                                sqlOrdLoads, OrdersLoadingEntity.class).getResultList();
                                        for (OrdersLoadingEntity j : ordersLoadingList) {
                                            HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                            sHmpam.put("id", j.getId());
                                            sHmpam.put("ordersLoadingId", j.getId());
                                            sHmpam.put("type", j.getType());
                                            sHmpam.put("fromCountry", j.getFromCountry());
                                            sHmpam.put("fromCity", j.getFromCity());
                                            sHmpam.put("aa", formatAaOrderLoad(j.getCreationDate().toString(), j.getAa().toString(), j.getType()));
                                            sHmpam.put("fromAddress", j.getFromAddress());
                                            sHmpam.put("fromPostalCode", j.getFromPostalCode());
                                            sHmpam.put("toCountry", j.getFromCountry());
                                            sHmpam.put("comments", j.getComments());
                                            sHmpam.put("toCity", j.getFromCity());
                                            sHmpam.put("toAddress", j.getFromAddress());
                                            sHmpam.put("toPostalCode", j.getFromPostalCode());
                                            sHmpam.put("statusOrderLoading", j.getStatus());
                                            sHmpam.put("supplierId", j.getSupplierId());
                                            if (j.getArithmosTimologiou() != null) {

                                                sHmpam.put("arithmosTimologiou", j.getArithmosTimologiou());
                                            } else {
                                                sHmpam.put("arithmosTimologiou", "-");
                                            }

                                            if (j.getTimologioIndicator() == 1) {
                                                sHmpam.put("timologioIndicator", true);
                                            } else {
                                                sHmpam.put("timologioIndicator", false);
                                            }

                                            String sqlExtraSups = "select * from order_loading_extra_suppliers els where els.order_loading_id=" + j.getId();
                                            List<OrderLoadingExtraSuppliersEntity> orderLoadingExtraSuppliersEntityList =
                                                    entityManager.createNativeQuery(sqlExtraSups, OrderLoadingExtraSuppliersEntity.class).getResultList();
                                            List<HashMap<String, Object>> extrasups = new ArrayList<HashMap<String, Object>>();

                                            Double naulaPromhtheytwn = 0.0;
                                            for (OrderLoadingExtraSuppliersEntity plexs : orderLoadingExtraSuppliersEntityList) {
                                                HashMap<String, Object> plexsmap = new HashMap<String, Object>();
                                                naulaPromhtheytwn=naulaPromhtheytwn+plexs.getNaulo();
                                                plexsmap.put("naulo", plexs.getNaulo());
                                                plexsmap.put("supplierId", plexs.getSupplierId());
                                                CustomersSuppliersEntity customersSuppliersEntity = entityManager.find(CustomersSuppliersEntity.class, plexs.getSupplierId());
                                                plexsmap.put("brandName", customersSuppliersEntity.getBrandName());
                                                plexsmap.put("random_id", plexs.getId());
                                                if (plexs.getExtraSupTimologioIndicator() == 1) {
                                                    plexsmap.put("extraSupTimologioIndicator", true);
                                                } else {
                                                    plexsmap.put("extraSupTimologioIndicator", false);
                                                }


                                                String attatchmentsCountSql = "select  count(*) from documents docs where docs.system='customersSuppliers' and  docs.sub_folder_id=" + plexs.getSupplierId();
                                                BigInteger attatchmentsCount = (BigInteger) entityManager.createNativeQuery(attatchmentsCountSql).getSingleResult();
                                                if (attatchmentsCount != null) {
                                                    plexsmap.put("attatchmentsCount", attatchmentsCount);
                                                } else {
                                                    plexsmap.put("attatchmentsCount", "0");
                                                }

                                                extrasups.add(plexsmap);
                                            }
                                            sHmpam.put("extrasups", extrasups);

                                            if (j.getSupplierId() != null) {
                                                CustomersSuppliersEntity cust = entityManager.find(CustomersSuppliersEntity.class, j.getSupplierId());
                                                sHmpam.put("supplierName", cust.getBrandName());
                                            } else {
                                                sHmpam.put("supplierName", "-");
                                            }
                                            if (j.getSupplierTruckTrailerId() != null && j.getSupplierTruckTrailerId() != 0) {
                                                TrucksEntity truck = entityManager.find(TrucksEntity.class, j.getSupplierTruckTrailerId());
                                                sHmpam.put("truckTrailerName", truck.getBrandName());
                                                sHmpam.put("truckTrailerPlateNumber", truck.getPlateNumber());
                                                sHmpam.put("truckTrailerLdm", truck.getLdm());
                                            } else {
                                                sHmpam.put("truckTrailerName", "-");
                                                sHmpam.put("truckTrailerPlateNumber", "-");
                                                sHmpam.put("truckTrailerLdm", 0);
                                            }
                                            sHmpam.put("supplierTruckTrailerId", j.getSupplierTruckTrailerId());
                                            sHmpam.put("truckTrailerId", j.getSupplierTruckTrailerId());
                                            if (j.getSupplierTruckTractorId() != null && j.getSupplierTruckTractorId() != 0) {
                                                TrucksEntity truck = entityManager.find(TrucksEntity.class, j.getSupplierTruckTractorId());
                                                sHmpam.put("truckTractorName", truck.getBrandName());
                                                sHmpam.put("truckTractorPlateNumber", truck.getPlateNumber());
                                            } else {
                                                sHmpam.put("truckTractorName", "-");
                                                sHmpam.put("truckTractorPlateNumber", "-");
                                            }
                                            sHmpam.put("supplierTruckTractorId", j.getSupplierTruckTractorId());
                                            sHmpam.put("truckTractorId", j.getSupplierTruckTractorId());
                                            sHmpam.put("creationDate", j.getCreationDate());
                                            sHmpam.put("updateDate", j.getUpdateDate());
                                            sHmpam.put("customerSupplierId", j.getSupplierId());
                                            sHmpam.put("naulo", j.getNaulo());
                                            sHmpam.put("sumNaula", j.getNaulo()+naulaPromhtheytwn);

                                            //naulaPromhtheytwn

                                            sHmpam.put("finalSummPrice", 0);
                                            sHmpam.put("finalSummLdm", 0);
                                            sHmpam.put("finalSummQuantity", 0);
                                            sHmpam.put("finalSummQuantityKg", 0);
                                            sHmpam.put("finalSummQuantityKgNet", 0);
                                            Double finalSummPrice = 0.0;
                                            Double finalSummLdm = 0.0;
                                            Integer finalSummQuantity = 0;
                                            Integer finalSummQuantityKg = 0;
                                            Integer finalSummQuantityKgNet = 0;
                                            sHmpam.put("mainSchedule", j.getFromCountry() + " " +
                                                    j.getFromCity() + "  ->  " +
                                                    j.getToCountry() + " " +
                                                    j.getToCity());
                                            String sqlOrdersDoneList =
                                                    " select * from orders_loading_orders_selections olrs where olrs.order_loading_id=" + j.getId();
                                            List<OrdersLoadingOrdersSelectionsEntity> ordersLoadingOrdersSelectionsEntityList =
                                                    entityManager.createNativeQuery(sqlOrdersDoneList, OrdersLoadingOrdersSelectionsEntity.class).getResultList();
                                            List<HashMap<String, Object>> doneList = new ArrayList<HashMap<String, Object>>();
                                            for (OrdersLoadingOrdersSelectionsEntity os : ordersLoadingOrdersSelectionsEntityList) {
                                                List<HashMap<String, Object>> dromologioParaggelias = new ArrayList<HashMap<String, Object>>();
                                                ObjectNode wsResult = Json.newObject();
                                                ObjectNode reqBody = Json.newObject();
                                                reqBody.put("orderId", os.getOrderId());
                                                CompletableFuture<WSResponse> wsFuture = (CompletableFuture)
                                                        ws.url(ConfigFactory.load().getString("ws_url") + "getAvailablesOrders")
                                                                .post(reqBody).thenApplyAsync(webServiceResponse -> {
                                                            return webServiceResponse;
                                                        });
                                                try {
                                                    wsResult = (ObjectNode) wsFuture.get().asJson();
                                                    Iterator doneIt = wsResult.findPath("data").iterator();
                                                    while (doneIt.hasNext()) {
                                                        JsonNode doneNode = (JsonNode) doneIt.next();
                                                        HashMap<String, Object> doneMap = new HashMap<String, Object>();
                                                        doneMap.put("updateDate", doneNode.findPath("updateDate").asText());
                                                        doneMap.put("mainSchedule", doneNode.findPath("mainSchedule").asText());
                                                        doneMap.put("fromAddressLabel", doneNode.findPath("fromAddressLabel").asText());
                                                        doneMap.put("fromAppointmentDay", doneNode.findPath("fromAppointmentDay").asText());
                                                        doneMap.put("fromAppointment", doneNode.findPath("fromAppointment").asText());
                                                        doneMap.put("appointmentRequired", doneNode.findPath("appointmentRequired").asText());
                                                        doneMap.put("appointmentDays", doneNode.findPath("appointmentDays").asText());
                                                        doneMap.put("fromCountry", doneNode.findPath("fromCountry").asText());
                                                        doneMap.put("fromCity", doneNode.findPath("fromCity").asText());
                                                        doneMap.put("typePack", doneNode.findPath("typePack"));
                                                        doneMap.put("firstTypePack", doneNode.findPath("firstTypePack").asText());
                                                        if (doneNode.findPath("truckTemprature").asText().equalsIgnoreCase("null")) {
                                                            doneMap.put("truckTemprature", "-");
                                                        } else {
                                                            doneMap.put("truckTemprature", doneNode.findPath("truckTemprature").asText());
                                                        }
                                                        doneMap.put("crmIndicator", doneNode.findPath("crmIndicator").asBoolean());
                                                        doneMap.put("crmNumber", doneNode.findPath("crmNumber").asText());
                                                        doneMap.put("orderId", doneNode.findPath("orderId").asText());
                                                        doneMap.put("type", doneNode.findPath("type").asText());
                                                        doneMap.put("attatchmentsCount", doneNode.findPath("attatchmentsCount").asText());
                                                        doneMap.put("creationDate", doneNode.findPath("creationDate").asText());
                                                        doneMap.put("summPriceNested", doneNode.findPath("summPriceNested").asText());
                                                        doneMap.put("summMasterSchedule", doneNode.findPath("summMasterSchedule").asText());
                                                        doneMap.put("summQuantity", doneNode.findPath("summQuantity").asText());
                                                        doneMap.put("summQuantityKg", doneNode.findPath("summQuantityKg").asText());
                                                        doneMap.put("summQuantityKgNet", doneNode.findPath("summQuantityKgNet").asText());
                                                        doneMap.put("summLdm", doneNode.findPath("summLdm").asText());
                                                        doneMap.put("showDromologioIndicator", false);
                                                        doneMap.put("customerId", doneNode.findPath("customerId").asText());
                                                        doneMap.put("customer", doneNode.findPath("customer"));
                                                        doneMap.put("customerBilling", doneNode.findPath("customerBilling"));
                                                        doneMap.put("status", doneNode.findPath("status"));
                                                        DecimalFormat df = new DecimalFormat("###.#");
                                                        String sqlSumPrice = "select  sum(osp.unit_price) from orders_selections_by_point osp where osp.order_id=" + doneNode.findPath("orderId").asText();
                                                        Double summPrice = (Double) entityManager.createNativeQuery(sqlSumPrice).getSingleResult();
                                                        if (summPrice != null) {
                                                            finalSummPrice = finalSummPrice + summPrice;
                                                            sHmpam.put("finalSummPrice", df.format(finalSummPrice));
                                                        } else {
                                                            sHmpam.put("finalSummPrice", df.format(finalSummPrice));
                                                        }
                                                        String sqlSumLdm = "select  sum(osp.ldm) from orders_selections_by_point osp where osp.order_id=" + doneNode.findPath("orderId").asText();
                                                        Double summLdm = (Double) entityManager.createNativeQuery(sqlSumLdm).getSingleResult();
                                                        if (summLdm != null) {
                                                            finalSummLdm = finalSummLdm + summLdm;
                                                            sHmpam.put("finalSummLdm", df.format(finalSummLdm));
                                                        } else {
                                                            sHmpam.put("finalSummLdm", df.format(finalSummLdm));
                                                        }
                                                        String sqlSumQuantity = "select  sum(osp.quantity) from orders_selections_by_point osp where osp.order_id=" + doneNode.findPath("orderId").asText() + " and osp.type_package!='Βάσει βάρους επί καθαρού (kg)' and osp.type_package!='Βάσει βάρους επί μικτού (kg)' ";
                                                        BigDecimal summQuantity = (BigDecimal) entityManager.createNativeQuery(sqlSumQuantity).getSingleResult();
                                                        if (summQuantity != null) {
                                                            finalSummQuantity = finalSummQuantity + summQuantity.intValue();
                                                            sHmpam.put("finalSummQuantity", finalSummQuantity);
                                                        } else {
                                                            sHmpam.put("finalSummQuantity", finalSummQuantity);
                                                        }

                                                        String sqlSumQuantityKg = "select  sum(osbp.quantity) from orders_selections_by_point osbp where osbp.order_id=" + doneNode.findPath("orderId").asText() + "  and ( osbp.type_package='Βάσει βάρους επί καθαρού (kg)' ) ";
                                                        BigDecimal summQuantityKg = (BigDecimal) entityManager.createNativeQuery(sqlSumQuantityKg).getSingleResult();
                                                        if (summQuantityKg != null) {
                                                            finalSummQuantityKg = finalSummQuantityKg + summQuantityKg.intValue();
                                                            sHmpam.put("finalSummQuantityKg", finalSummQuantityKg);
                                                        } else {
                                                            sHmpam.put("finalSummQuantityKg", finalSummQuantityKg);
                                                        }

                                                        String sqlsummQuantityKgNet = "select  sum(osbp.quantity) from orders_selections_by_point osbp where osbp.order_id=" + doneNode.findPath("orderId").asText() + "  and ( osbp.type_package='Βάσει βάρους επί μικτού (kg)' ) ";
                                                        BigDecimal summQuantityKgNet = (BigDecimal) entityManager.createNativeQuery(sqlsummQuantityKgNet).getSingleResult();
                                                        if (summQuantityKgNet != null) {
                                                            finalSummQuantityKgNet = finalSummQuantityKgNet + summQuantityKgNet.intValue();
                                                            sHmpam.put("finalSummQuantityKgNet", finalSummQuantityKgNet);
                                                        } else {
                                                            sHmpam.put("finalSummQuantityKgNet", finalSummQuantityKgNet);
                                                        }


                                                        ObjectNode reqBodyDromWs = Json.newObject();
                                                        ObjectNode dromRes = Json.newObject();
                                                        reqBodyDromWs.put("orderId", os.getOrderId());
                                                        CompletableFuture<WSResponse> wsFutureDrom = (CompletableFuture)
                                                                ws.url(ConfigFactory.load().getString("ws_url") + "getDromologioByOrder")
                                                                        .post(reqBodyDromWs).thenApplyAsync(webServiceResponse -> {
                                                                    return webServiceResponse;
                                                                });
                                                        dromRes = (ObjectNode) wsFutureDrom.get().asJson();
                                                        Iterator dromResIt = dromRes.findPath("data").iterator();
                                                        while (dromResIt.hasNext()) {
                                                            JsonNode dromResNode = (JsonNode) dromResIt.next();
                                                            HashMap<String, Object> dromResNodeMap = new HashMap<String, Object>();
                                                            dromResNodeMap.put("orderId", dromResNode.findPath("orderId").asText());
                                                            dromResNodeMap.put("type", dromResNode.findPath("type").asText());
                                                            dromResNodeMap.put("status", dromResNode.findPath("status").asText());
                                                            dromResNodeMap.put("country", dromResNode.findPath("country").asText());
                                                            dromResNodeMap.put("orderScheduleId", dromResNode.findPath("orderScheduleId").asText());
                                                            dromResNodeMap.put("waypointId", dromResNode.findPath("waypointId").asText());
                                                            dromResNodeMap.put("brandName", dromResNode.findPath("brandName").asText());
                                                            dromResNodeMap.put("orderCustomerName", dromResNode.findPath("orderCustomerName").asText());
                                                            dromResNodeMap.put("timelinetype", dromResNode.findPath("timelinetype").asText());
                                                            dromResNodeMap.put("nestedScheduleIndicator", dromResNode.findPath("nestedScheduleIndicator").asText());
                                                            dromResNodeMap.put("position", dromResNode.findPath("position").asInt());
                                                            dromResNodeMap.put("address", dromResNode.findPath("address").asText());
                                                            dromResNodeMap.put("lattitude", dromResNode.findPath("lattitude").asDouble());
                                                            dromResNodeMap.put("city", dromResNode.findPath("city").asText());
                                                            dromResNodeMap.put("postalCode", dromResNode.findPath("postalCode").asText());
                                                            dromResNodeMap.put("longtitude", dromResNode.findPath("longtitude").asDouble());
                                                            dromResNodeMap.put("appointment", dromResNode.findPath("appointment").asText());
                                                            dromResNodeMap.put("message", dromResNode.findPath("message").asText());
                                                            dromResNodeMap.put("appointmentDay", dromResNode.findPath("appointmentDay").asText());
                                                            dromResNodeMap.put("allPackages", dromResNode.findPath("allPackages"));
                                                            dromResNodeMap.put("packagesFortwshs", dromResNode.findPath("packagesFortwshs"));
                                                            dromResNodeMap.put("packagesEkfortwshs", dromResNode.findPath("packagesEkfortwshs"));
                                                            dromResNodeMap.put("finalSummPrice", dromResNode.findPath("finalSummPrice"));
                                                            dromResNodeMap.put("finalSummLdm", dromResNode.findPath("finalSummLdm"));
                                                            dromResNodeMap.put("finalSummQuantity", dromResNode.findPath("finalSummQuantity"));
                                                            dromResNodeMap.put("finalSummQuantityKg", dromResNode.findPath("finalSummQuantityKg"));
                                                            dromResNodeMap.put("finalSummQuantityKgNet", dromResNode.findPath("finalSummQuantityKgNet"));
                                                            dromResNodeMap.put("showPackagesIndicator", false);
                                                            dromResNodeMap.put("showDromologioIndicator", false);
                                                            dromResNodeMap.put("includedToDromologio", true);
                                                            dromologioParaggelias.add(dromResNodeMap);
                                                        }
                                                        doneMap.put("dromologioParaggelias", dromologioParaggelias);
                                                        doneList.add(doneMap);
                                                    }
                                                    sHmpam.put("doneList", doneList);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                } catch (ExecutionException e) {
                                                    e.printStackTrace();
                                                }
                                            }
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

    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteOrderLoading(final Http.Request request) throws IOException {
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
                                Long user_id = json.findPath("user_id").asLong();
                                OrdersLoadingEntity ordersLoadingEntity = entityManager.find(OrdersLoadingEntity.class, id);
                                String sql = " select * from orders_loading_orders_selections ordl where ordl.order_loading_id=" + id;
                                List<OrdersLoadingOrdersSelectionsEntity> ordersLoadingOrdersSelectionsEntityList =
                                        entityManager.createNativeQuery(sql, OrdersLoadingOrdersSelectionsEntity.class).getResultList();
                                for (OrdersLoadingOrdersSelectionsEntity ordls : ordersLoadingOrdersSelectionsEntityList) {
                                    entityManager.remove(ordls);
                                }
                                entityManager.remove(ordersLoadingEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η Διαγραφή πραγματοποίηθηκε με επιτυχία");
                                add_result.put("system", "ΗΜΕΡΙΔΕΣ");
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

    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }

    private static String formatAaOrderLoad(String date, String aa, String type) {
        String year = date.substring(0, 4);
        String month = date.substring(5, 7);

        if (aa.length() == 1) {
            aa = "000" + aa;
        }
        if (aa.length() == 2) {
            aa = "00" + aa;
        }
        if (aa.length() == 3) {
            aa = "0" + aa;
        }
        if (aa.length() == 4) {
            aa = aa;
        }
        String myDate = type + month + year + aa;
        return myDate;

    }


}