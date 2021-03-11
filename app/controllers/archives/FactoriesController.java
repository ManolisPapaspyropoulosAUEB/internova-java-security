package controllers.archives;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import controllers.system.Application;
import models.CustomersSuppliersEntity;
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
public class FactoriesController extends Application {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;
    @Inject
    public FactoriesController(JPAApi jpaApi, DatabaseExecutionContext executionContext)  {
        super(jpaApi,  executionContext);

        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }

    //getAllFactoriesNoPagination
    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getAllFactoriesNoPagination(final Http.Request request) throws IOException {
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
                                            String sqlWarehouses= "select * from factories pos where 1=1";


                                            String address = json.findPath("address").asText();
                                            String telephone = json.findPath("telephone").asText();
                                            String brandName = json.findPath("brandName").asText();
                                            String city = json.findPath("city").asText();
                                            String country = json.findPath("country").asText();


                                            if (!address.equalsIgnoreCase("") && address != null) {
                                                sqlWarehouses += " and pos.address like '%"+address+"%'" ;
                                            }
                                            if (!telephone.equalsIgnoreCase("") && telephone != null) {
                                                sqlWarehouses += " and pos.telephone like '%"+telephone+"%'" ;
                                            }
                                            if (!brandName.equalsIgnoreCase("") && brandName != null) {
                                                sqlWarehouses += " and pos.brand_name like '%"+brandName+"%'" ;
                                            }
                                            if (!city.equalsIgnoreCase("") && city != null) {
                                                sqlWarehouses += " and pos.city like '%"+city+"%'" ;
                                            }
                                            if (!country.equalsIgnoreCase("") && country != null) {
                                                sqlWarehouses += " and pos.country like '%"+country+"%'" ;
                                            }
                                            sqlWarehouses+=" order by pos.creation_date desc ";


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
                                                sHmpam.put("factoryType", j.getFactoryType());
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
    public Result getSuggestedFactories(final Http.Request request) throws IOException {
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
                                            String factoryId = json.findPath("factoryId").asText();
                                            String address = json.findPath("address").asText();
                                            String brandName = json.findPath("brandName").asText();
                                       //     String city = json.findPath("city").asText();
                                         //   String country = json.findPath("country").asText();
                                            String postalCode = json.findPath("postalCode").asText();
                                            String email = json.findPath("email").asText();
                                            String customerId = json.findPath("customerId").asText();
                                            String manager = json.findPath("manager").asText();
                                            String region = json.findPath("region").asText();
                                            String telephone = json.findPath("telephone").asText();
                                            String creationDate = json.findPath("creationDate").asText();
                                            String typeCategory = json.findPath("typeCategory").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlFactFinal="";

                                            String sqlFact1= "" +
                                                    " select pos.* " +
                                                    " from factories pos  " +
                                                    " where id in ( select factory_id from orders ord where ord.customer_id= "+customerId+")" ;

                                            String sqlFact2= " select * " +
                                                    "from factories pos  " +
                                                    "where id in ( select factory_id from orders ord where ord.customer_id!= "+customerId+")" ;


                                            String sqlFact3= " select * " +
                                                    "from factories pos where 1=1 ";

                                            if(typeCategory.equalsIgnoreCase("1")){
                                                sqlFactFinal=sqlFact1;
                                            }else if (typeCategory.equalsIgnoreCase("2")){
                                                sqlFactFinal=sqlFact2;
                                            }else if (typeCategory.equalsIgnoreCase("3")){
                                                sqlFactFinal=sqlFact3;

                                            }




                                            if(!id.equalsIgnoreCase("") && id!=null){
                                                sqlFactFinal+=" and pos.id = "+id;
                                            }
                                            if(!factoryId.equalsIgnoreCase("") && factoryId!=null){
                                                sqlFactFinal+=" and pos.id like '%"+factoryId+"%'";
                                            }
                                            if(!factoryId.equalsIgnoreCase("") && factoryId!=null && !factoryId.equalsIgnoreCase("null")){
                                                sqlFactFinal+=" and pos.id like '%"+factoryId+"%'";
                                            }

                                            if(!postalCode.equalsIgnoreCase("") && postalCode!=null  && !postalCode.equalsIgnoreCase("null")){
                                                sqlFactFinal+=" and pos.postal_code like '%"+postalCode+"%'";
                                            }

                                            if(!address.equalsIgnoreCase("") && address!=null && !address.equalsIgnoreCase("null")){
                                                sqlFactFinal+=" and pos.address like '%"+address+"%'";
                                            }
                                            if(!brandName.equalsIgnoreCase("") && brandName!=null && !brandName.equalsIgnoreCase("null")){
                                                sqlFactFinal+=" and pos.brand_name like '%"+brandName+"%'";
                                            }

                                            if(!email.equalsIgnoreCase("") && email!=null && !email.equalsIgnoreCase("null")){
                                                sqlFactFinal+=" and pos.email like '%"+email+"%'";
                                            }
                                            if(!manager.equalsIgnoreCase("") && manager!=null && !manager.equalsIgnoreCase("null")){
                                                sqlFactFinal+=" and pos.manager like '%"+manager+"%'";
                                            }
                                            if(!region.equalsIgnoreCase("") && region!=null && !region.equalsIgnoreCase("null")){
                                                sqlFactFinal+=" and pos.region like '%"+region+"%'";
                                            }
                                            if(!telephone.equalsIgnoreCase("") && telephone!=null && !telephone.equalsIgnoreCase("null")){
                                                sqlFactFinal+=" and pos.telephone like '%"+telephone+"%'";
                                            }
                                            if(!creationDate.equalsIgnoreCase("") && creationDate!=null && !creationDate.equalsIgnoreCase("null")){
                                                sqlFactFinal += " and SUBSTRING( role.creation_date, 1, 10)  = '" + creationDate + "'";
                                            }

                                            System.out.println(sqlFactFinal);

                                            List<FactoriesEntity> filalistAll
                                                    = (List<FactoriesEntity>) entityManager.createNativeQuery(
                                                    sqlFactFinal, FactoriesEntity.class).getResultList();
                                            if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                                sqlFactFinal += " order by " + orderCol + " " + descAsc;
                                            } else {
                                                sqlFactFinal += " order by creation_date desc";
                                            }
                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sqlFactFinal += " limit " + start + "," + limit;
                                            }
                                            System.out.println(sqlFactFinal);

                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> filalist = new ArrayList<HashMap<String, Object>>();
                                            List<FactoriesEntity> warehousesEntityList
                                                    = (List<FactoriesEntity>) entityManager.createNativeQuery(
                                                    sqlFactFinal, FactoriesEntity.class).getResultList();

                                            System.out.println(warehousesEntityList.size());
                                            for (FactoriesEntity j : warehousesEntityList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("address", j.getAddress());
                                                sHmpam.put("brandName", j.getBrandName());
                                                sHmpam.put("city", j.getCity());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("appointmentRequired", j.getAppointmentRequired());
                                                sHmpam.put("email", j.getEmail());
                                                sHmpam.put("comments", j.getComments());
                                                sHmpam.put("factoryType", j.getFactoryType());
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
                                                sHmpam.put("unloadingLoadingCodeMasked", j.getUnloadingLoadingCode());
                                                sHmpam.put("appointmentDays", j.getAppointmentDays());
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


    //getFactoryCustomers

    //getAllFactoriesNoPagination
    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getFactoryCustomers(final Http.Request request) throws IOException {
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
                                            String factoryId = json.findPath("factoryId").asText();

                                            String sqlCusts= " select * \n" +
                                                    "from customers_suppliers cs \n" +
                                                    "where cs.id in\n" +
                                                    "(select customer_id \n" +
                                                    "from orders ord\n" +
                                                    "where ord.id in \n" +
                                                    "(select order_id from order_schedules ords where ords.factory_id="+factoryId+") \n" +
                                                    "union\n" +
                                                    "select customer_id \n" +
                                                    "from orders ord\n" +
                                                    "where ord.id in \n" +
                                                    "(select order_id from order_waypoints ordw where ordw.factory_id=  "+factoryId+" ) \n" + ") ";
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> filalist = new ArrayList<HashMap<String, Object>>();
                                            List<CustomersSuppliersEntity> suppliersEntityList
                                                    = (List<CustomersSuppliersEntity>) entityManager.createNativeQuery(
                                                    sqlCusts, CustomersSuppliersEntity.class).getResultList();
                                            for (CustomersSuppliersEntity j : suppliersEntityList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("brandName", j.getBrandName());
                                                sHmpam.put("email", j.getEmail());
                                                sHmpam.put("afm", j.getAfm());
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("customerSupplierId", j.getId());
                                                sHmpam.put("telephone", j.getTelephone());

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
    public Result getFactories(final Http.Request request) throws IOException {
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
                                                sqlWarehouses+=" and pos.id = "+id;
                                            }
                                            if(!factoryId.equalsIgnoreCase("") && factoryId!=null){
                                                sqlWarehouses+=" and pos.id like '%"+factoryId+"%'";
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
                                            System.out.println(sqlWarehouses);

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
                                                sHmpam.put("factoryType", j.getFactoryType());
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
                                                sHmpam.put("unloadingLoadingCodeMasked", j.getUnloadingLoadingCode());
                                                sHmpam.put("appointmentDays", j.getAppointmentDays());
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
                                    String user_id = json.findPath("user_id").asText();
                                    String address = json.findPath("address").asText();
                                    String telephone = json.findPath("telephone").asText();
                                    String email = json.findPath("email").asText();
                                    String postalCode = json.findPath("postalCode").asText();
                                    String city = json.findPath("city").asText();
                                    String region = json.findPath("region").asText();
                                    String manager = json.findPath("manager").asText();
                                    String country = json.findPath("country").asText();
                                    String factoryType = json.findPath("factoryType").asText();

                                    postalCode = postalCode.replaceAll("\\s+","");

                                    //
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
                                    factoriesEntity.setFactoryType(factoryType);
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
                                    result_add.put("address", factoriesEntity.getAddress());
                                    result_add.put("appointmentRequired", factoriesEntity.getAppointmentRequired());
                                    result_add.put("brandName", factoriesEntity.getBrandName());
                                    result_add.put("factoryId", factoriesEntity.getId());
                                    result_add.put("message", "Η καταχώρηση ολοκληρώθηκε με επιτυχία!");
                                    result_add.put("DO_ID", factoriesEntity.getId());
                                    result_add.put("system", "εργοστάσια");
                                    result_add.put("user_id", user_id);
                                    return result_add;
                                });
                            },
                            executionContext);
                    result = (ObjectNode) addFuture.get();
                    return ok(result,request);

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
                                    String user_id = json.findPath("user_id").asText();
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
                                    String factoryType = json.findPath("factoryType").asText();
                                    String coordinates = json.findPath("coordinates").asText();
                                    String unloadingLoadingCode = json.findPath("unloadingLoadingCode").asText();
                                    Integer appointmentDays = json.findPath("appointmentDays").asInt();
                                    Double longtitude = json.findPath("longitude").asDouble();
                                    Double lattitude = json.findPath("latitude").asDouble();
                                    boolean appointmentRequired = json.findPath("appointmentRequired").asBoolean();//appointmentRequired
                                    postalCode = postalCode.replaceAll("\\s+","");
                                    FactoriesEntity factoriesEntity = entityManager.find(FactoriesEntity.class,id);
                                    factoriesEntity.setAddress(address);
                                    factoriesEntity.setBrandName(brandName);
                                    factoriesEntity.setCity(city);
                                    factoriesEntity.setEmail(email);
                                    factoriesEntity.setFactoryType(factoryType);


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
                                    result_add.put("address", factoriesEntity.getAddress());
                                    result_add.put("appointmentRequired", factoriesEntity.getAppointmentRequired());
                                    result_add.put("brandName", factoriesEntity.getBrandName());
                                    result_add.put("factoryId", factoriesEntity.getId());
                                    result_add.put("message", "Η ενημερωση ολοκληρώθηκε με επιτυχία!");
                                    result_add.put("DO_ID", factoriesEntity.getId());
                                    result_add.put("system", "εργοστάσια");
                                    result_add.put("user_id", user_id);
                                    return result_add;
                                });
                            },
                            executionContext);
                    result = (ObjectNode) addFuture.get();
                    return ok(result,request);

                } catch (Exception e) {
                    ObjectNode result = Json.newObject();
                    e.printStackTrace();
                    result.put("status", "error");
                    result.put("message", "Προβλημα κατα την ενημερωση");
                    return ok(result);
                }
            }
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την ενημερωση");
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
                                    Long user_id = json.findPath("user_id").asLong();
                                    FactoriesEntity factoriesEntity = entityManager.find(FactoriesEntity.class,id);
                                    entityManager.remove(factoriesEntity);
                                    result_add.put("status", "success");
                                    result_add.put("message", "Η διαγραφή ολοκληρώθηκε με επιτυχία!");
                                    result_add.put("DO_ID", factoriesEntity.getId());
                                    result_add.put("system", "εργοστάσια");
                                    result_add.put("user_id", user_id);
                                    return result_add;
                                });
                            },
                            executionContext);
                    result = (ObjectNode) addFuture.get();
                    return ok(result,request);

                } catch (Exception e) {
                    ObjectNode result = Json.newObject();
                    e.printStackTrace();
                    result.put("status", "error");
                    result.put("message", "Προβλημα κατα την καταχωρηση");
                    return ok(result,request);
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
