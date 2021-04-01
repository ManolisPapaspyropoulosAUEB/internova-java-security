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
    public Result add(final Http.Request request) throws IOException {
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
                                String user_id = json.findPath("user_id").asText();
                                String description = json.findPath("description").asText();


                                add_result.put("status", "success");
                                add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
                                //   add_result.put("DO_ID", billingsEntity.getId());
                                add_result.put("system", "Λογαριασμοί/Billings");
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
                                            String sqlAvailablesOrders = "select * from orders ord where 1=1 ";
                                            if(availableOrdersSearch!=null && !availableOrdersSearch.equalsIgnoreCase("")){
                                                sqlAvailablesOrders+=" and ord.id like '%"+availableOrdersSearch+"%' " +
                                                        " or ord.customer_id in (select id from customers_suppliers cs where cs.brand_name like '%"+availableOrdersSearch+"%'" +
                                                        " or ord.id in  ( select os.order_id from order_schedules os where os.primary_schedule=1 " +
                                                        " and os.from_city like '%"+availableOrdersSearch+"%' or " +
                                                        "os.from_country  like '%"+availableOrdersSearch+"%' " +
                                                        " or os.to_city like '%"+availableOrdersSearch+"%'" +
                                                        " or os.to_country like '%"+availableOrdersSearch+"%' )     )";
                                            }
                                            System.out.println(sqlAvailablesOrders);
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                            List<OrdersEntity> ordersEntityList
                                                    = (List<OrdersEntity>) entityManager.createNativeQuery(
                                                    sqlAvailablesOrders, OrdersEntity.class).getResultList();
                                            for (OrdersEntity j : ordersEntityList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("orderId", j.getId());
                                                sHmpam.put("customerId", j.getCustomerId());
                                                sHmpam.put("customer", entityManager.find(CustomersSuppliersEntity.class,j.getCustomerId()));
                                                sHmpam.put("status", j.getStatus());
                                                sHmpam.put("type", j.getType());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                String sqlOrdersSchedules =
                                                        "select * from order_schedules ord_s " +
                                                                " where ord_s.order_id=" + j.getId() +
                                                                " and ord_s.primary_schedule=1" +
                                                                " and ord_s.factory_id " +
                                                                " is not null ";
                                                List<OrderSchedulesEntity> orderSchedulesEntityList =
                                                        entityManager.createNativeQuery(sqlOrdersSchedules, OrderSchedulesEntity.class).getResultList();
                                                sHmpam.put("mainSchedule", orderSchedulesEntityList.get(0).getFromCountry() + " " +
                                                        orderSchedulesEntityList.get(0).getFromCity() + "  /  " +
                                                        orderSchedulesEntityList.get(0).getToCountry() + " " +
                                                        orderSchedulesEntityList.get(0).getToCity());
                                                sHmpam.put("factory", entityManager.find(FactoriesEntity.class, orderSchedulesEntityList.get(0).getFactoryId()));
                                                sHmpam.put("factoryId", orderSchedulesEntityList.get(0).getFactoryId());
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


//getOrdersLoadings



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
                                        String billingName = json.findPath("billingName").asText();
                                        String billingDescription = json.findPath("billingDescription").asText();
                                        String creationDate = json.findPath("creationDate").asText();
                                        String start = json.findPath("start").asText();
                                        String limit = json.findPath("limit").asText();
                                        String sqlOrdLoads = "select * from orders_loading ord_load where 1=1 ";
//                                        if (!billingName.equalsIgnoreCase("") && billingName != null) {
//                                            sqlroles += " and b.name like '%" + billingName + "%'";
//                                        }
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