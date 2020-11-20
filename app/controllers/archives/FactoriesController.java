package controllers.archives;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.FactoriesEntity;
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
public class FactoriesController {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;
    @Inject
    public FactoriesController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }

    //getAllFactoriesNoPagination
    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getAllFactoriesNoPagination(final Http.Request request) throws IOException {  // san parametro pernei to org key
        ObjectNode result = Json.newObject();
        System.out.println("getAllFactoriesNoPagination>>");
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
                                        entityManager -> {//appointmentRequired warehouseId

                                            String sqlWarehouses= "select * from factories pos where 1=1 ";
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> filalist = new ArrayList<HashMap<String, Object>>();
                                            List<FactoriesEntity> warehousesEntityList
                                                    = (List<FactoriesEntity>) entityManager.createNativeQuery(
                                                    sqlWarehouses, FactoriesEntity.class).getResultList();
                                            for (FactoriesEntity j : warehousesEntityList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("address", j.getAddress());
                                                sHmpam.put("brandName", j.getBrandName());
                                                sHmpam.put("city", j.getCity());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("appointmentRequired", j.getAppointmentRequired());
                                                sHmpam.put("email", j.getEmail());
                                                sHmpam.put("comments", j.getComments());
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("factoryId", j.getId());
                                                sHmpam.put("manager", j.getManager());
                                                sHmpam.put("postalCode", j.getPostalCode());
                                                sHmpam.put("region", j.getRegion());
                                                sHmpam.put("telephone", j.getTelephone());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                sHmpam.put("longitude", j.getLongtitude());
                                                sHmpam.put("latitude", j.getLattitude());
                                                sHmpam.put("schedule", j.getSchedule());
                                                sHmpam.put("country", j.getCountry());
                                                sHmpam.put("site", j.getSite());
                                                sHmpam.put("coordinates", j.getCoordinates());
                                                sHmpam.put("unloadingLoadingCode", j.getUnloadingLoadingCode());
                                                sHmpam.put("appointmentDays", j.getAppointmentDays());
                                                if(j.getAppointmentRequired()==1){
                                                    sHmpam.put("appointmentRequired", j.getAppointmentRequired());
                                                }else{
                                                    sHmpam.put("appointmentRequired", j.getAppointmentRequired());
                                                }
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
    public Result getFactories(final Http.Request request) throws IOException {  // san parametro pernei to org key
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
                                        entityManager -> {//appointmentRequired warehouseId
                                            String id = json.findPath("id").asText();
                                            String orderCol = json.findPath("orderCol").asText();
                                            String descAsc = json.findPath("descAsc").asText();
                                            String warehouseId = json.findPath("warehouseId").asText();
                                            String factoryId = json.findPath("factoryId").asText();
                                            String address = json.findPath("address").asText();
                                            String brandName = json.findPath("brandName").asText();
                                            String city = json.findPath("city").asText();
                                            String email = json.findPath("email").asText();
                                            String manager = json.findPath("manager").asText();
                                            String postalCode = json.findPath("postalCode").asText();
                                            String region = json.findPath("region").asText();
                                            String telephone = json.findPath("telephone").asText();
                                            String creationDate = json.findPath("creationDate").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlWarehouses= "select * from factories pos where 1=1 ";
                                            if(!id.equalsIgnoreCase("") && id!=null){
                                                sqlWarehouses+=" and pos.id like '%"+id+"%'";
                                            }
                                            if(!warehouseId.equalsIgnoreCase("") && warehouseId!=null){
                                                sqlWarehouses+=" and pos.id like '%"+warehouseId+"%'";
                                            }
                                            if(!factoryId.equalsIgnoreCase("") && factoryId!=null){
                                                sqlWarehouses+=" and pos.id like '%"+factoryId+"%'";
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
                                            List<FactoriesEntity> filalistAll
                                                    = (List<FactoriesEntity>) entityManager.createNativeQuery(
                                                    sqlWarehouses, FactoriesEntity.class).getResultList();
                                            if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                                sqlWarehouses += " order by " + orderCol + " " + descAsc;
                                            } else {
                                                sqlWarehouses += " order by creation_date desc";
                                            }
                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sqlWarehouses += " limit " + start + "," + limit;
                                            }
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> filalist = new ArrayList<HashMap<String, Object>>();
                                            List<FactoriesEntity> warehousesEntityList
                                                    = (List<FactoriesEntity>) entityManager.createNativeQuery(
                                                    sqlWarehouses, FactoriesEntity.class).getResultList();


                                            String sqlMin = "select min(id) from factories cs ";
                                            String sqlMax = "select max(id) from factories cs ";
                                            BigInteger minId = (BigInteger) entityManager.createNativeQuery(sqlMin).getSingleResult();
                                            BigInteger maxId = (BigInteger) entityManager.createNativeQuery(sqlMax).getSingleResult();
                                            for (FactoriesEntity j : warehousesEntityList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("address", j.getAddress());
                                                sHmpam.put("brandName", j.getBrandName());
                                                sHmpam.put("city", j.getCity());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("appointmentRequired", j.getAppointmentRequired());
                                                sHmpam.put("email", j.getEmail());
                                                sHmpam.put("comments", j.getComments());
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("factoryId", j.getId());
                                                sHmpam.put("manager", j.getManager());
                                                sHmpam.put("postalCode", j.getPostalCode());
                                                sHmpam.put("region", j.getRegion());
                                                sHmpam.put("telephone", j.getTelephone());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                sHmpam.put("longitude", j.getLongtitude());
                                                sHmpam.put("latitude", j.getLattitude());
                                                sHmpam.put("schedule", j.getSchedule());
                                                sHmpam.put("country", j.getCountry());
                                                sHmpam.put("site", j.getSite());
                                                sHmpam.put("coordinates", j.getCoordinates());
                                                sHmpam.put("unloadingLoadingCode", j.getUnloadingLoadingCode());
                                                sHmpam.put("appointmentDays", j.getAppointmentDays());
                                                String sqlNextId = "select min(id) from factories cs where cs.creation_date >"+ "'"+ j.getCreationDate()+"'";
                                                String sqlPreviousId = "select max(id) from factories cs where cs.creation_date < "+"'"  + j.getCreationDate()+"'";
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

                                                if(j.getAppointmentRequired()==1){
                                                    sHmpam.put("appointmentRequired", j.getAppointmentRequired());
                                                }else{
                                                    sHmpam.put("appointmentRequired", j.getAppointmentRequired());
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



    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addFactory(final Http.Request request) throws IOException {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode result_add = Json.newObject();
                                    String brandName = json.findPath("brandName").asText();
                                    String address = json.findPath("address").asText();
                                    String telephone = json.findPath("telephone").asText();
                                    String email = json.findPath("email").asText();
                                    String postalCode = json.findPath("postalCode").asText();
                                    String city = json.findPath("city").asText();
                                    String region = json.findPath("region").asText();
                                    String manager = json.findPath("manager").asText();
                                    String country = json.findPath("country").asText();
                                    String comments = json.findPath("comments").asText();
                                    Double longtitude = json.findPath("longitude").asDouble();
                                    Double lattitude = json.findPath("latitude").asDouble();
                                    String schedule = json.findPath("schedule").asText();
                                    String site = json.findPath("site").asText();//appointmentRequired
                                    boolean appointmentRequired = json.findPath("appointmentRequired").asBoolean();//appointmentRequired
                                    String coordinates = json.findPath("coordinates").asText();
                                    String unloadingLoadingCode = json.findPath("unloadingLoadingCode").asText();
                                    Integer appointmentDays = json.findPath("appointmentDays").asInt();
                                    FactoriesEntity factoriesEntity = new FactoriesEntity();
                                    factoriesEntity.setAddress(address);
                                    factoriesEntity.setBrandName(brandName);
                                    factoriesEntity.setCity(city);
                                    factoriesEntity.setEmail(email);
                                    factoriesEntity.setTelephone(telephone);
                                    factoriesEntity.setPostalCode(postalCode);
                                    factoriesEntity.setRegion(region);
                                    factoriesEntity.setManager(manager);
                                    factoriesEntity.setCreationDate(new Date());
                                    factoriesEntity.setSchedule(schedule);
                                    factoriesEntity.setSite(site);
                                    factoriesEntity.setCountry(country);
                                    factoriesEntity.setComments(comments);
                                    factoriesEntity.setCoordinates(coordinates);
                                    factoriesEntity.setUnloadingLoadingCode(unloadingLoadingCode);
                                    factoriesEntity.setAppointmentDays(appointmentDays);
                                    factoriesEntity.setLattitude(lattitude);
                                    factoriesEntity.setLongtitude(longtitude);
                                    if(appointmentRequired){
                                        factoriesEntity.setAppointmentRequired((byte) 1);
                                    }else{
                                        factoriesEntity.setAppointmentRequired((byte) 0);
                                    }
                                    entityManager.persist(factoriesEntity);
                                    result_add.put("status", "success");
                                    result_add.put("factoryId", factoriesEntity.getId());
                                    result_add.put("message", "Η καταχώρηση ολοκληρώθηκε με επιτυχία!");
                                    return result_add;
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
    public Result updateFactory(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    System.out.println(json);
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode result_add = Json.newObject();
                                    String brandName = json.findPath("brandName").asText();
                                    String comments = json.findPath("comments").asText();
                                    String address = json.findPath("address").asText();
                                    String telephone = json.findPath("telephone").asText();
                                    String email = json.findPath("email").asText();
                                    String postalCode = json.findPath("postalCode").asText();
                                    String city = json.findPath("city").asText();
                                    String region = json.findPath("region").asText();
                                    String manager = json.findPath("manager").asText();
                                    Long id = json.findPath("id").asLong();
                                    String schedule = json.findPath("schedule").asText();
                                    String site = json.findPath("site").asText();
                                    String country = json.findPath("country").asText();
                                    String coordinates = json.findPath("coordinates").asText();
                                    String unloadingLoadingCode = json.findPath("unloadingLoadingCode").asText();
                                    Integer appointmentDays = json.findPath("appointmentDays").asInt();
                                    Double longtitude = json.findPath("longitude").asDouble();
                                    Double lattitude = json.findPath("latitude").asDouble();
                                    boolean appointmentRequired = json.findPath("appointmentRequired").asBoolean();//appointmentRequired
                                    FactoriesEntity factoriesEntity = entityManager.find(FactoriesEntity.class,id);
                                    factoriesEntity.setAddress(address);
                                    factoriesEntity.setBrandName(brandName);
                                    factoriesEntity.setCity(city);
                                    factoriesEntity.setEmail(email);
                                    factoriesEntity.setTelephone(telephone);
                                    factoriesEntity.setPostalCode(postalCode);
                                    factoriesEntity.setCountry(country);
                                    factoriesEntity.setRegion(region);
                                    factoriesEntity.setManager(region);
                                    factoriesEntity.setCreationDate(new Date());
                                    factoriesEntity.setManager(manager);
                                    factoriesEntity.setSchedule(schedule);
                                    factoriesEntity.setSite(site);
                                    factoriesEntity.setCoordinates(coordinates);
                                    factoriesEntity.setUnloadingLoadingCode(unloadingLoadingCode);
                                    factoriesEntity.setAppointmentDays(appointmentDays);
                                    factoriesEntity.setComments(comments);
                                    factoriesEntity.setLattitude(lattitude);
                                    factoriesEntity.setLongtitude(longtitude);
                                    if(appointmentRequired){
                                        factoriesEntity.setAppointmentRequired((byte) 1);
                                    }else{
                                        factoriesEntity.setAppointmentRequired((byte) 0);
                                    }
                                    entityManager.merge(factoriesEntity);
                                    result_add.put("status", "success");
                                    result_add.put("message", "Η ενημερωση ολοκληρώθηκε με επιτυχία!");
                                    return result_add;
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
    public Result deleteFactory(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode result_add = Json.newObject();
                                    Long id = json.findPath("id").asLong();
                                    FactoriesEntity factoriesEntity = entityManager.find(FactoriesEntity.class,id);
                                    entityManager.remove(factoriesEntity);
                                    result_add.put("status", "success");
                                    result_add.put("message", "Η διαγραφή ολοκληρώθηκε με επιτυχία!");
                                    return result_add;
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
}
