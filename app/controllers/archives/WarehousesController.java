package controllers.archives;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.FactoriesEntity;
import models.WarehousesEntity;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import javax.inject.Inject;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;
public class WarehousesController {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;
    @Inject
    public WarehousesController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addWarehouse(final Http.Request request) throws IOException {
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
                                    String address = json.findPath("address").asText();
                                    String brandName = json.findPath("brandName").asText();
                                    String city = json.findPath("city").asText();
                                    String email = json.findPath("email").asText();
                                    String manager = json.findPath("manager").asText();
                                    String postalCode = json.findPath("postalCode").asText();
                                    String region = json.findPath("region").asText();
                                    String telephone = json.findPath("telephone").asText();
                                    Double latitude = json.findPath("latitude").asDouble();
                                    Double longitude = json.findPath("longitude").asDouble();
                                    String comments = json.findPath("comments").asText();
                                    WarehousesEntity warehousesEntity = new WarehousesEntity();
                                    warehousesEntity.setAddress(address);
                                    warehousesEntity.setBrandName(brandName);
                                    warehousesEntity.setCity(city);
                                    warehousesEntity.setCreationDate(new Date());
                                    warehousesEntity.setEmail(email);
                                    warehousesEntity.setManager(manager);
                                    warehousesEntity.setPostalCode(postalCode);
                                    warehousesEntity.setRegion(region);
                                    warehousesEntity.setTelephone(telephone);
                                    warehousesEntity.setComments(comments);
                                    warehousesEntity.setLongitude(longitude);
                                    warehousesEntity.setLatitude(latitude);
                                    warehousesEntity.setComments(comments);
                                    entityManager.persist(warehousesEntity);
                                    add_result.put("status", "success");
                                    add_result.put("warehouseId", warehousesEntity.getId());
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
    public Result updateWarehouse(final Http.Request request) throws IOException {
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
                                    String address = json.findPath("address").asText();
                                    String brandName = json.findPath("brandName").asText();
                                    String city = json.findPath("city").asText();
                                    String email = json.findPath("email").asText();
                                    String manager = json.findPath("manager").asText();
                                    String postalCode = json.findPath("postalCode").asText();
                                    String region = json.findPath("region").asText();
                                    String telephone = json.findPath("telephone").asText();
                                    Long id = json.findPath("warehouseId").asLong();
                                    String comments = json.findPath("comments").asText();
                                    Double latitude = json.findPath("latitude").asDouble();
                                    Double longitude = json.findPath("longitude").asDouble();
                                    WarehousesEntity warehousesEntity = entityManager.find(WarehousesEntity.class, id);
                                    warehousesEntity.setAddress(address);
                                    warehousesEntity.setBrandName(brandName);
                                    warehousesEntity.setCity(city);
                                    warehousesEntity.setCreationDate(new Date());
                                    warehousesEntity.setEmail(email);
                                    warehousesEntity.setManager(manager);
                                    warehousesEntity.setPostalCode(postalCode);
                                    warehousesEntity.setRegion(region);
                                    warehousesEntity.setTelephone(telephone);
                                    warehousesEntity.setComments(comments);
                                    warehousesEntity.setLongitude(longitude);
                                    warehousesEntity.setLatitude(latitude);
                                    entityManager.merge(warehousesEntity);
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
    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteWarehouse(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> deleteFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode delete_result = Json.newObject();
                                    Long id = json.findPath("id").asLong();
                                    WarehousesEntity warehousesEntity = entityManager.find(WarehousesEntity.class, id);
                                    entityManager.remove(warehousesEntity);
                                    delete_result.put("status", "success");
                                    delete_result.put("message", "Η διαγραφή ολοκληρώθηκε με επιτυχία!");
                                    return delete_result;
                                });
                            },
                            executionContext);
                    result = (ObjectNode) deleteFuture.get();
                    return ok(result);

                } catch (Exception e) {
                    ObjectNode result = Json.newObject();
                    e.printStackTrace();
                    result.put("status", "error");
                    result.put("message", "Προβλημα κατα την διαγραφή");
                    return ok(result);
                }
            }
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την διαγραφή");
            return ok(result);
        }
    }



    //getAllWarehousesNoPagination


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getAllWarehousesNoPagination(final Http.Request request) throws IOException {
        ObjectNode result = Json.newObject();
        System.out.println("getAllWarehousesNoPagination>>");
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

                                            String sqlWarehouses= "select * from warehouses pos where 1=1 ";
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> filalist = new ArrayList<HashMap<String, Object>>();
                                            List<WarehousesEntity> warehousesEntityList
                                                    = (List<WarehousesEntity>) entityManager.createNativeQuery(
                                                    sqlWarehouses, WarehousesEntity.class).getResultList();
                                            for (WarehousesEntity j : warehousesEntityList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("address", j.getAddress());
                                                sHmpam.put("brandName", j.getBrandName());
                                                sHmpam.put("city", j.getCity());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("email", j.getEmail());
                                                sHmpam.put("comments", j.getComments());
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("warehouseId", j.getId());
                                                sHmpam.put("latitude", j.getLatitude());
                                                sHmpam.put("longitude", j.getLongitude());
                                                sHmpam.put("manager", j.getManager());
                                                sHmpam.put("postalCode", j.getPostalCode());
                                                sHmpam.put("region", j.getRegion());
                                                sHmpam.put("telephone", j.getTelephone());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                filalist.add(sHmpam);
                                            }
                                            returnList_future.put("data", filalist);
                                            returnList_future.put("total", filalist.size());
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
    public Result getWarehouses(final Http.Request request) throws IOException {  //
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
                                            //roleDescSearchInput warehouseId
                                            String id = json.findPath("id").asText();
                                            String orderCol = json.findPath("orderCol").asText();
                                            String descAsc = json.findPath("descAsc").asText();
                                            String address = json.findPath("address").asText();
                                            String brandName = json.findPath("brandName").asText();
                                            String city = json.findPath("city").asText();
                                            String email = json.findPath("email").asText();
                                            String manager = json.findPath("manager").asText();
                                            String postalCode = json.findPath("postalCode").asText();
                                            String region = json.findPath("region").asText();
                                            String telephone = json.findPath("telephone").asText();
                                            String creationDate = json.findPath("creationDate").asText();
                                            String warehouseId = json.findPath("warehouseId").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlWarehouses= "select * from warehouses pos where 1=1 ";
                                            if(!id.equalsIgnoreCase("") && id!=null){
                                                sqlWarehouses+=" and pos.id like '%"+id+"%'";
                                            }
                                            if(!warehouseId.equalsIgnoreCase("") && warehouseId!=null){
                                                sqlWarehouses+=" and pos.id = "+warehouseId+"";
                                            }
                                            if(!address.equalsIgnoreCase("") && address!=null){
                                                sqlWarehouses+=" and pos.address like '%"+address+"%'";
                                            }
                                            if(!brandName.equalsIgnoreCase("") && brandName!=null){
                                                sqlWarehouses+=" and pos.brand_name like '%"+brandName+"%'";
                                            }
                                            if(!brandName.equalsIgnoreCase("") && brandName!=null){
                                                sqlWarehouses+=" and pos.brand_name like '%"+brandName+"%'";
                                            }
                                            if(!city.equalsIgnoreCase("") && city!=null){
                                                sqlWarehouses+=" and pos.city like '%"+city+"%'";
                                            }
                                            if(!email.equalsIgnoreCase("") && email!=null){
                                                sqlWarehouses+=" and pos.email like '%"+email+"%'";
                                            }
                                            if(!manager.equalsIgnoreCase("") && manager!=null){
                                                sqlWarehouses+=" and pos.manager like '%"+manager+"%'";
                                            }
                                            if(!postalCode.equalsIgnoreCase("") && postalCode!=null){
                                                sqlWarehouses+=" and pos.postal_code like '%"+postalCode+"%'";
                                            }
                                            if(!region.equalsIgnoreCase("") && region!=null){
                                                sqlWarehouses+=" and pos.region like '%"+region+"%'";
                                            }
                                            if(!telephone.equalsIgnoreCase("") && telephone!=null){
                                                sqlWarehouses+=" and pos.telephone like '%"+telephone+"%'";
                                            }
                                            if(!creationDate.equalsIgnoreCase("") && creationDate!=null){
                                                sqlWarehouses += " and SUBSTRING( role.creation_date, 1, 10)  = '" + creationDate + "'";
                                            }
                                            List<WarehousesEntity> filalistAll
                                                    = (List<WarehousesEntity>) entityManager.createNativeQuery(
                                                    sqlWarehouses, WarehousesEntity.class).getResultList();
                                            if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                                sqlWarehouses += " order by " + orderCol + " " + descAsc;
                                            } else {
                                                sqlWarehouses += " order by creation_date desc";
                                            }
                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sqlWarehouses += " limit " + start + "," + limit;
                                            }

                                            String sqlMin = "select min(id) from warehouses w ";
                                            String sqlMax = "select max(id) from warehouses w ";
                                            BigInteger minId = (BigInteger) entityManager.createNativeQuery(sqlMin).getSingleResult();
                                            BigInteger maxId = (BigInteger) entityManager.createNativeQuery(sqlMax).getSingleResult();


                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> filalist = new ArrayList<HashMap<String, Object>>();
                                            List<WarehousesEntity> warehousesEntityList
                                                    = (List<WarehousesEntity>) entityManager.createNativeQuery(
                                                    sqlWarehouses, WarehousesEntity.class).getResultList();
                                            for (WarehousesEntity j : warehousesEntityList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("address", j.getAddress());
                                                sHmpam.put("brandName", j.getBrandName());
                                                sHmpam.put("city", j.getCity());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("email", j.getEmail());
                                                sHmpam.put("comments", j.getComments());
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("warehouseId", j.getId());
                                                sHmpam.put("latitude", j.getLatitude());
                                                sHmpam.put("longitude", j.getLongitude());
                                                sHmpam.put("manager", j.getManager());
                                                sHmpam.put("postalCode", j.getPostalCode());
                                                sHmpam.put("region", j.getRegion());
                                                sHmpam.put("telephone", j.getTelephone());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                String sqlNextId = "select min(id) from warehouses w where w.creation_date >"+ "'"+ j.getCreationDate()+"'";
                                                String sqlPreviousId = "select max(id) from warehouses w where w.creation_date < "+"'"  + j.getCreationDate()+"'";
                                                BigInteger nextId = (BigInteger) entityManager.createNativeQuery(sqlNextId).getSingleResult();
                                                BigInteger previousId = (BigInteger) entityManager.createNativeQuery(sqlPreviousId).getSingleResult();
                                                if(nextId!=null){
                                                    sHmpam.put("previousId",nextId);
                                                }else{
                                                    sHmpam.put("previousId",maxId);
                                                }
                                                if(previousId!=null){
                                                    sHmpam.put("nextId", previousId);
                                                }else{
                                                    sHmpam.put("nextId", minId);
                                                }
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
