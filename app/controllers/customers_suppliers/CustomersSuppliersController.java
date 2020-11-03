package controllers.customers_suppliers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.CustomersSuppliersEntity;
import models.WarehousesEntity;
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

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

public class CustomersSuppliersController {

    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;
    @Inject
    public CustomersSuppliersController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }



    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addCustomerSupplier(final Http.Request request) throws IOException {
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
                                    String postalCode = json.findPath("postalCode").asText();
                                    String region = json.findPath("region").asText();
                                    String telephone = json.findPath("telephone").asText();
                                    String comments = json.findPath("comments").asText();
                                    String website = json.findPath("website").asText();
                                    String afm = json.findPath("afm").asText();
                                    Long billingId = json.findPath("billingId").asLong();
                                    String country = json.findPath("country").asText();
                                    String doy = json.findPath("doy").asText();
                                    String customerType = json.findPath("customerType").asText();
                                    String job = json.findPath("job").asText();
                                    Long internovaSellerId = json.findPath("internovaSellerId").asLong();

                                    CustomersSuppliersEntity warehousesEntity = new CustomersSuppliersEntity();
                                    warehousesEntity.setAddress(address);
                                    warehousesEntity.setBrandName(brandName);
                                    warehousesEntity.setCity(city);
                                    warehousesEntity.setCreationDate(new Date());
                                    warehousesEntity.setEmail(email);
                                    warehousesEntity.setPostalCode(postalCode);
                                    warehousesEntity.setRegion(region);
                                    warehousesEntity.setTelephone(telephone);
                                    warehousesEntity.setComments(comments);
                                    warehousesEntity.setWebsite(website);
                                    warehousesEntity.setAfm(afm);
                                    warehousesEntity.setBillingId(billingId);
                                    warehousesEntity.setCountry(country);
                                    warehousesEntity.setDoy(doy);
                                    warehousesEntity.setCustomerType(customerType);
                                    warehousesEntity.setInternovaSellerId(internovaSellerId);
                                    warehousesEntity.setJob(job);
                                    entityManager.persist(warehousesEntity);
                                    add_result.put("status", "success");
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
    public Result updateCustomerSupplier(final Http.Request request) throws IOException {
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
                                    String postalCode = json.findPath("postalCode").asText();
                                    String region = json.findPath("region").asText();
                                    String telephone = json.findPath("telephone").asText();
                                    String comments = json.findPath("comments").asText();
                                    String website = json.findPath("website").asText();
                                    String afm = json.findPath("afm").asText();
                                    Long billingId = json.findPath("billingId").asLong();
                                    String country = json.findPath("country").asText();
                                    String doy = json.findPath("doy").asText();
                                    String customerType = json.findPath("customerType").asText();
                                    String job = json.findPath("job").asText();
                                    Long internovaSellerId = json.findPath("internovaSellerId").asLong();
                                    Long id = json.findPath("id").asLong();

                                    CustomersSuppliersEntity warehousesEntity = entityManager.find(CustomersSuppliersEntity.class,id);
                                    warehousesEntity.setAddress(address);
                                    warehousesEntity.setBrandName(brandName);
                                    warehousesEntity.setCity(city);
                                    warehousesEntity.setUpdateDate(new Date());
                                    warehousesEntity.setEmail(email);
                                    warehousesEntity.setPostalCode(postalCode);
                                    warehousesEntity.setRegion(region);
                                    warehousesEntity.setTelephone(telephone);
                                    warehousesEntity.setComments(comments);
                                    warehousesEntity.setWebsite(website);
                                    warehousesEntity.setAfm(afm);
                                    warehousesEntity.setBillingId(billingId);
                                    warehousesEntity.setCountry(country);
                                    warehousesEntity.setDoy(doy);
                                    warehousesEntity.setCustomerType(customerType);
                                    warehousesEntity.setInternovaSellerId(internovaSellerId);
                                    warehousesEntity.setJob(job);
                                    entityManager.persist(warehousesEntity);
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
    public Result deleteCustomerSupplier(final Http.Request request) throws IOException {
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
                                    CustomersSuppliersEntity customersSuppliersEntity = entityManager.find(CustomersSuppliersEntity.class, id);
                                    entityManager.remove(customersSuppliersEntity);
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







    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getCustomersSuppliers(final Http.Request request) throws IOException {  // san parametro pernei to org key
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
                                            //roleDescSearchInput
                                            String id = json.findPath("id").asText();
                                            String address = json.findPath("address").asText();
                                            String brandName = json.findPath("brandName").asText();
                                            String city = json.findPath("city").asText();
                                            String email = json.findPath("email").asText();
                                            String postalCode = json.findPath("postalCode").asText();
                                            String region = json.findPath("region").asText();
                                            String telephone = json.findPath("telephone").asText();
                                            String creationDate = json.findPath("creationDate").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlCustSupl= "select * from customers_suppliers pos where 1=1 ";
                                            if(!id.equalsIgnoreCase("") && id!=null){
                                                sqlCustSupl+=" and pos.id like '%"+id+"%'";
                                            }
                                            if(!address.equalsIgnoreCase("") && address!=null){
                                                sqlCustSupl+=" and pos.address like '%"+address+"%'";
                                            }
                                            if(!brandName.equalsIgnoreCase("") && brandName!=null){
                                                sqlCustSupl+=" and pos.brand_name like '%"+brandName+"%'";
                                            }

                                            if(!brandName.equalsIgnoreCase("") && brandName!=null){
                                                sqlCustSupl+=" and pos.brand_name like '%"+brandName+"%'";
                                            }
                                            if(!city.equalsIgnoreCase("") && city!=null){
                                                sqlCustSupl+=" and pos.city like '%"+city+"%'";
                                            }

                                            if(!email.equalsIgnoreCase("") && email!=null){
                                                sqlCustSupl+=" and pos.email like '%"+email+"%'";
                                            }
                                            if(!postalCode.equalsIgnoreCase("") && postalCode!=null){
                                                sqlCustSupl+=" and pos.postal_code like '%"+postalCode+"%'";
                                            }
                                            if(!region.equalsIgnoreCase("") && region!=null){
                                                sqlCustSupl+=" and pos.region like '%"+region+"%'";
                                            }
                                            if(!telephone.equalsIgnoreCase("") && telephone!=null){
                                                sqlCustSupl+=" and pos.telephone like '%"+telephone+"%'";
                                            }
                                            if(!creationDate.equalsIgnoreCase("") && creationDate!=null){
                                                sqlCustSupl += " and SUBSTRING( role.creation_date, 1, 10)  = '" + creationDate + "'";
                                            }
                                            List<CustomersSuppliersEntity> filalistAll
                                                    = (List<CustomersSuppliersEntity>) entityManager.createNativeQuery(
                                                    sqlCustSupl, CustomersSuppliersEntity.class).getResultList();
                                            sqlCustSupl+=" order by creation_date desc";
                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sqlCustSupl += " limit " + start + "," + limit;
                                            }
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> filalist = new ArrayList<HashMap<String, Object>>();
                                            List<CustomersSuppliersEntity> warehousesEntityList
                                                    = (List<CustomersSuppliersEntity>) entityManager.createNativeQuery(
                                                    sqlCustSupl, CustomersSuppliersEntity.class).getResultList();
                                            for (CustomersSuppliersEntity j : warehousesEntityList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("address", j.getAddress());
                                                sHmpam.put("brandName", j.getBrandName());
                                                sHmpam.put("city", j.getCity());
                                                sHmpam.put("email", j.getEmail());
                                                sHmpam.put("job", j.getJob());
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("warehouseId", j.getId());
                                                sHmpam.put("postalCode", j.getPostalCode());
                                                sHmpam.put("region", j.getRegion());
                                                sHmpam.put("afm", j.getAfm());
                                                sHmpam.put("billingId", j.getBillingId());
                                                sHmpam.put("doy", j.getDoy());
                                                sHmpam.put("internovaSellerId", j.getInternovaSellerId());
                                                sHmpam.put("comments", j.getComments());
                                                sHmpam.put("customerType", j.getCustomerType());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("telephone", j.getTelephone());
                                                sHmpam.put("updateDate", j.getUpdateDate());
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
