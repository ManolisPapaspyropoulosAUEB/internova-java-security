package controllers.archives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import controllers.system.Application;
import models.CustomersSuppliersEntity;
import models.SuppliersTrucksEntity;
import models.TruckTypeEntity;
import models.TrucksEntity;
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

public class TrucksController extends Application {

    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public TrucksController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        super(jpaApi, executionContext);
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addTruck(final Http.Request request) throws IOException {
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
                                    String brandName = json.findPath("brandName").asText();
                                    String plateNumber = json.findPath("plateNumber").asText();
                                    String description = json.findPath("description").asText();
                                    String trailerTrackor = json.findPath("trailerTrackor").asText();
                                    Long typeTruckId = json.findPath("typeTruckId").asLong();
                                    Long user_id = json.findPath("user_id").asLong();
                                    Long suplierId = json.findPath("suplierId").asLong();

                                    TrucksEntity truck = new TrucksEntity();
                                    truck.setBrandName(brandName);
                                    truck.setPlateNumber(plateNumber);
                                    truck.setDescription(description);
                                    truck.setTypeTruckId(typeTruckId);
                                    truck.setCreationDate(new Date());
                                    truck.setTrailerTrackor(trailerTrackor);
                                    entityManager.persist(truck);
                                    if(suplierId!=null && suplierId!=0){
                                        SuppliersTrucksEntity supTrEnt = new SuppliersTrucksEntity();
                                        supTrEnt.setCustomersSuppliersId(suplierId);
                                        supTrEnt.setTruckId(truck.getId());
                                        entityManager.persist(supTrEnt);
                                    }

                                    add_result.put("status", "success");
                                    add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
                                    add_result.put("DO_ID", truck.getId());
                                    add_result.put("system", "Φορτηγά");
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
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την καταχωρηση");
            return ok(result);
        }
    }



    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateTruck(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode update_result = Json.newObject();
                                    String brandName = json.findPath("brandName").asText();
                                    String plateNumber = json.findPath("plateNumber").asText();
                                    String description = json.findPath("description").asText();
                                    Long typeTruckId = json.findPath("typeTruckId").asLong();
                                    Long user_id = json.findPath("user_id").asLong();
                                    Long id = json.findPath("id").asLong();
                                    Long suplierId = json.findPath("suplierId").asLong();
                                    String trailerTrackor = json.findPath("trailerTrackor").asText();
                                    TrucksEntity truck = entityManager.find(TrucksEntity.class, id);
                                    truck.setBrandName(brandName);
                                    truck.setPlateNumber(plateNumber);
                                    truck.setDescription(description);
                                 //   truck.setTypeTruckId(typeTruckId);
                                    truck.setUdpateDate(new Date());
                                    truck.setTrailerTrackor(trailerTrackor);
                                    if(trailerTrackor.equalsIgnoreCase("trailer")){
                                        truck.setTypeTruckId(typeTruckId);
                                    }else{
                                        truck.setTypeTruckId((long) 0);
                                    }
                                    entityManager.merge(truck);
                                    if(suplierId!=null && suplierId!=0){
                                        SuppliersTrucksEntity supTrEnt = new SuppliersTrucksEntity();
                                        supTrEnt.setCustomersSuppliersId(suplierId);
                                        supTrEnt.setTruckId(truck.getId());
                                        entityManager.persist(supTrEnt);
                                    }
                                    update_result.put("status", "success");
                                    update_result.put("message", "Η ενημέρωση πραγματοποίηθηκε με επιτυχία");
                                    update_result.put("DO_ID", truck.getId());
                                    update_result.put("system", "Φορτηγά");
                                    update_result.put("user_id", user_id);
                                    return update_result;
                                });
                            },
                            executionContext);
                    result = (ObjectNode) updateFuture.get();
                    return ok(result, request);
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
    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteTruck(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode update_result = Json.newObject();
                                    Long user_id = json.findPath("user_id").asLong();
                                    Long id = json.findPath("id").asLong();
                                    TrucksEntity truck = entityManager.find(TrucksEntity.class, id);

                                    String sql = "select * from suppliers_trucks st where st.truck_id="+id;
                                    List<SuppliersTrucksEntity> stlist = entityManager.createNativeQuery(sql,SuppliersTrucksEntity.class).getResultList();

                                    if(stlist.size()>0){
                                        update_result.put("status", "error");
                                        update_result.put("message", "Βρέθηκε συνδεδεμένη εγγραφή");
                                        return update_result;
                                    }
                                    entityManager.remove(truck);
                                    update_result.put("status", "success");
                                    update_result.put("message", "Η διαγραφή πραγματοποίηθηκε με επιτυχία");
                                    update_result.put("DO_ID", truck.getId());
                                    update_result.put("system", "Φορτηγά");
                                    update_result.put("user_id", user_id);
                                    return update_result;
                                });
                            },
                            executionContext);
                    result = (ObjectNode) updateFuture.get();
                    return ok(result, request);
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
    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteTruckSupplier(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode update_result = Json.newObject();
                                    Long user_id = json.findPath("user_id").asLong();
                                    Long id = json.findPath("id").asLong();
                                    Long suplierId = json.findPath("suplierId").asLong();
                                    String query = "select * from suppliers_trucks st where st.customers_suppliers_id="+suplierId+" and st.truck_id="+id;
                                    List<SuppliersTrucksEntity> suppList = entityManager.createNativeQuery(query,SuppliersTrucksEntity.class).getResultList();
                                    for(SuppliersTrucksEntity st : suppList){
                                        entityManager.remove(st);
                                    }
                                    update_result.put("status", "success");
                                    update_result.put("message", "Η διαγραφή πραγματοποίηθηκε με επιτυχία");
                                    update_result.put("user_id", user_id);
                                    return update_result;
                                });
                            },
                            executionContext);
                    result = (ObjectNode) updateFuture.get();
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
    public Result getTrucks(final Http.Request request) throws IOException, ExecutionException, InterruptedException {//plateNumberSearch
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
                                        String brandName = json.findPath("brandName").asText();
                                        String suplierName = json.findPath("suplierName").asText();
                                        String description = json.findPath("description").asText();
                                        String typeTruck = json.findPath("typeTruck").asText();
                                        String customerSupplierId = json.findPath("customerSupplierId").asText();
                                        Long suplierId = json.findPath("suplierId").asLong();
                                        String id = json.findPath("id").asText();
                                        String plateNumber = json.findPath("plateNumber").asText();
                                        String trailerTrackor = json.findPath("trailerTrackor").asText();
                                        String creationDate = json.findPath("creationDate").asText();
                                        String start = json.findPath("start").asText();
                                        String limit = json.findPath("limit").asText();
                                        String sqlTrucks = "select * from trucks truck where 1=1 ";
                                        if (!brandName.equalsIgnoreCase("") && brandName != null) {
                                            sqlTrucks += " and truck.brand_name like '%" + brandName + "%'";
                                        }
                                        if (!description.equalsIgnoreCase("") && description != null) {
                                            sqlTrucks += " and truck.description like '%" + description + "%'";
                                        }
                                        if (  trailerTrackor != null && !trailerTrackor.equalsIgnoreCase("")  && !trailerTrackor.equalsIgnoreCase("null") && !trailerTrackor.equalsIgnoreCase("all") ) {
                                            sqlTrucks += " and truck.trailer_trackor  ='" + trailerTrackor + "'";
                                        }
                                        if (suplierId != null && suplierId != 0) {
                                            sqlTrucks += " and truck.id in " +
                                                    " (select truck_id from suppliers_trucks ms where ms.customers_suppliers_id =" + suplierId + ")";
                                        }
                                        if(suplierName!=null && !suplierName.equalsIgnoreCase("") && !suplierName.equalsIgnoreCase("null")){
                                            sqlTrucks +=
                                                    " and " +
                                                    " truck.id in " +
                                                    " (select truck_id " +
                                                    " from suppliers_trucks st " +
                                                    " where st.customers_suppliers_id " +
                                                    " in (select id" +
                                                    " from customers_suppliers cs" +
                                                    " where cs.brand_name like '%"
                                                            +suplierName+"%'))";
                                        }
                                        if (!typeTruck.equalsIgnoreCase("") && typeTruck != null) {
                                            sqlTrucks += " and truck.type_truck_id in (select id  from truck_type tp where type like '%"+typeTruck+"%')";
                                        }
                                        if (!id.equalsIgnoreCase("") && id != null) {
                                            sqlTrucks += " and truck.id like '%" + id + "%'";
                                        }
                                        if (!plateNumber.equalsIgnoreCase("") && plateNumber != null) {
                                            sqlTrucks += " and truck.plate_number like '%" + plateNumber + "%'";
                                        }
                                        if (!creationDate.equalsIgnoreCase("") && creationDate != null) {
                                            sqlTrucks += " and SUBSTRING( b.creation_date, 1, 10)  = '" + creationDate + "'";
                                        }
                                        List<TrucksEntity> rolesListAll
                                                = (List<TrucksEntity>) entityManager.createNativeQuery(
                                                sqlTrucks, TrucksEntity.class).getResultList();
                                        if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                            if(orderCol.equalsIgnoreCase("suplier_name")){
                                                sqlTrucks +=  " order by (\n" +
                                                        " select brand_name \n" +
                                                        " from customers_suppliers cs \n" +
                                                        " where cs.id in \n" +
                                                        " (select customers_suppliers_id \n" +
                                                        " from suppliers_trucks strucks \n" +
                                                        " where strucks.truck_id=truck.id \n" +
                                                        " ) \n" +
                                                        " ) \n"+ descAsc;
                                            }else if (orderCol.equalsIgnoreCase("type_truck")) {
                                                sqlTrucks+=" order by (\n" +
                                                        " select tp.type\n" +
                                                        " from truck_type tp\n" +
                                                        " where tp.id=truck.type_truck_id\n" +
                                                        " )"+ descAsc;
                                            }else{
                                                sqlTrucks += " order by " + orderCol + " " + descAsc;
                                            }
                                        } else {
                                            sqlTrucks += " order by creation_date desc";
                                        }
                                        if (!start.equalsIgnoreCase("") && start != null) {
                                            sqlTrucks += " limit " + start + "," + limit;
                                        }
                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                        List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                        List<TrucksEntity> orgsList
                                                = (List<TrucksEntity>) entityManager.createNativeQuery(
                                                sqlTrucks, TrucksEntity.class).getResultList();
                                        for (TrucksEntity j : orgsList) {
                                            HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                            sHmpam.put("id", j.getId());
                                            sHmpam.put("truckId", j.getId());
                                            sHmpam.put("brandName", j.getBrandName());
                                            sHmpam.put("description", j.getDescription());
                                            sHmpam.put("plateNumber", j.getPlateNumber());
                                            sHmpam.put("trailerTrackor", j.getTrailerTrackor());
                                            sHmpam.put("creationDate", j.getCreationDate());
                                            sHmpam.put("udpateDate", j.getUdpateDate());
                                            if(j.getTypeTruckId()!=0){
                                                sHmpam.put("typeTruckId", j.getTypeTruckId());
                                                sHmpam.put("typeTruck", entityManager.find(TruckTypeEntity.class,j.getTypeTruckId()).getType());
                                            }else{
                                                sHmpam.put("typeTruck","");
                                            }
                                            String sqlSuppl="select * from suppliers_trucks st where st.truck_id="+j.getId();
                                            List<SuppliersTrucksEntity> suppliersTrucksEntityList =  entityManager.createNativeQuery(sqlSuppl,SuppliersTrucksEntity.class).getResultList();
                                            if(suppliersTrucksEntityList.size()>0){
                                                CustomersSuppliersEntity suppl =
                                                        entityManager.find(CustomersSuppliersEntity.class,suppliersTrucksEntityList.get(0).getCustomersSuppliersId());
                                                sHmpam.put("suplier",suppl);
                                                sHmpam.put("suplierName",suppl.getBrandName());
                                            }else{
                                                sHmpam.put("suplierName","");
                                            }
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

    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getTrucksExpect(final Http.Request request) throws IOException {
        ObjectNode result = Json.newObject();
        try {
            JsonNode json = request.body().asJson();
            if (json == null || json.isEmpty()) {
                result.put("status", "error");
                result.put("message", "Δεν εχετε αποστειλει εγκυρα δεδομενα.");
                return ok(result);
            } else {
                ObjectMapper ow = new ObjectMapper();
                HashMap<String, Object> returnList = new HashMap<String, Object>();
                String jsonResult = "";
                CompletableFuture<HashMap<String, Object>> getFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(//suplierId
                                    entityManager -> {
                                        String suplierId = json.findPath("suplierId").asText();
                                        String sqlManagers = "select * " +
                                                "from trucks m " +
                                                "where m.id not in " +
                                                "(select truck_id " +
                                                "from suppliers_trucks ms )";
                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                        List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                        List<TrucksEntity> orgsList
                                                = (List<TrucksEntity>) entityManager.createNativeQuery(
                                                sqlManagers, TrucksEntity.class).getResultList();
                                        for (TrucksEntity j : orgsList) {
                                            HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                            sHmpam.put("id", j.getId());
                                            sHmpam.put("brandName", j.getBrandName());
                                            sHmpam.put("description", j.getDescription());
                                            sHmpam.put("trailerTrackor", j.getTrailerTrackor());
                                            sHmpam.put("plateNumber", j.getPlateNumber());
                                            if(j.getTypeTruckId()!=0){
                                                sHmpam.put("typeTruckId", j.getTypeTruckId());
                                                sHmpam.put("typeTruck", entityManager.find(TruckTypeEntity.class,j.getTypeTruckId()).getType());
                                            }else{
                                                sHmpam.put("typeTruck","");
                                            }
                                            sHmpam.put("creationDate", j.getCreationDate());
                                            sHmpam.put("udpateDate", j.getUdpateDate());
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
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Πρόβλημα κατά την ανάγνωση των στοιχείων");
            return ok(result);
        }
    }



    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addTruckType(final Http.Request request) throws IOException {
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
                                    String type = json.findPath("type").asText();
                                    String description = json.findPath("description").asText();
                                    Long user_id = json.findPath("user_id").asLong();

                                    TruckTypeEntity truck = new TruckTypeEntity();
                                    truck.setType(type);
                                    truck.setDescription(description);
                                    truck.setCreationDate(new Date());
                                    entityManager.persist(truck);
                                    add_result.put("status", "success");
                                    add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
                                    add_result.put("DO_ID", truck.getId());
                                    add_result.put("system", "τύποι φορτηγών");
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
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την καταχωρηση");
            return ok(result);
        }
    }



    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateTruckType(final Http.Request request) throws IOException {
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
                                    String type = json.findPath("type").asText();
                                    String description = json.findPath("description").asText();
                                    Long id = json.findPath("id").asLong();
                                    Long user_id = json.findPath("user_id").asLong();
                                    TruckTypeEntity truck = entityManager.find(TruckTypeEntity.class,id);
                                    truck.setType(type);
                                    truck.setDescription(description);
                                    truck.setUpdateDate(new Date());
                                    entityManager.merge(truck);
                                    add_result.put("status", "success");
                                    add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
                                    add_result.put("DO_ID", truck.getId());
                                    add_result.put("system", "τύποι φορτηγών");
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
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την καταχωρηση");
            return ok(result);
        }
    }




    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteTruckTypes(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode update_result = Json.newObject();
                                    Long user_id = json.findPath("user_id").asLong();
                                    Long id = json.findPath("id").asLong();
                                    TruckTypeEntity truckType = entityManager.find(TruckTypeEntity.class, id);

                                    String sql = "select * from trucks st where st.type_truck_id="+id;
                                    List<TrucksEntity> tlist = entityManager.createNativeQuery(sql,TrucksEntity.class).getResultList();
                                    if(tlist.size()>0){
                                        update_result.put("status", "error");
                                        update_result.put("message", "Βρέθηκε συνδεδεμένη εγγραφή");
                                        return update_result;
                                    }
                                    entityManager.remove(truckType);
                                    //todo:check if this truck type used
                                    update_result.put("status", "success");
                                    update_result.put("message", "Η διαγραφή πραγματοποίηθηκε με επιτυχία");
                                    update_result.put("DO_ID", truckType.getId());
                                    update_result.put("system", "τύποι φορτηγών");
                                    update_result.put("user_id", user_id);
                                    return update_result;
                                });
                            },
                            executionContext);
                    result = (ObjectNode) updateFuture.get();
                    return ok(result, request);
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
    public Result getTruckTypes(final Http.Request request) throws IOException, ExecutionException, InterruptedException {//plateNumberSearch
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
                                        String type = json.findPath("type").asText();
                                        String description = json.findPath("description").asText();
                                        String creationDate = json.findPath("creationDate").asText();
                                        String start = json.findPath("start").asText();
                                        String limit = json.findPath("limit").asText();
                                        String sqlTrucks = "select * from truck_type truck where 1=1 ";
                                        if (!type.equalsIgnoreCase("") && type != null) {
                                            sqlTrucks += " and truck.type like '%" + type + "%'";
                                        }
                                        if (!description.equalsIgnoreCase("") && description != null) {
                                            sqlTrucks += " and truck.description like '%" + description + "%'";
                                        }
                                        if (!creationDate.equalsIgnoreCase("") && creationDate != null) {
                                            sqlTrucks += " and SUBSTRING( b.creation_date, 1, 10)  = '" + creationDate + "'";
                                        }
                                        List<TruckTypeEntity> rolesListAll
                                                = (List<TruckTypeEntity>) entityManager.createNativeQuery(
                                                sqlTrucks, TruckTypeEntity.class).getResultList();

                                        if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                            sqlTrucks += " order by " + orderCol + " " + descAsc;
                                        } else {
                                            sqlTrucks += " order by creation_date desc";
                                        }


                                        if (!start.equalsIgnoreCase("") && start != null) {
                                            sqlTrucks += " limit " + start + "," + limit;
                                        }
                                        System.out.println(sqlTrucks);
                                        HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                        List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                        List<TruckTypeEntity> orgsList
                                                = (List<TruckTypeEntity>) entityManager.createNativeQuery(
                                                sqlTrucks, TruckTypeEntity.class).getResultList();
                                        for (TruckTypeEntity j : orgsList) {
                                            HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                            sHmpam.put("id", j.getId());
                                            sHmpam.put("type", j.getType());
                                            sHmpam.put("description", j.getDescription());
                                            sHmpam.put("creationDate", j.getCreationDate());
                                            sHmpam.put("updateDate", j.getUpdateDate());
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

    }








}
