package controllers.archives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import controllers.system.Application;
import models.*;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSRequestFilter;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

public class CustomersSuppliersController extends Application {

    private final WSClient ws;
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public CustomersSuppliersController(JPAApi jpaApi, DatabaseExecutionContext executionContext, WSClient ws) {
        super(jpaApi, executionContext);
        this.jpaApi = jpaApi;
        this.ws = ws;
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
                                    Long user_id = json.findPath("user_id").asLong();
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
                                    /**
                                     * String sqlUniqEmail=" select * from customers_suppliers cs where cs.email='"+email+"'";
                                     * List<CustomersSuppliersEntity> suppliersEntityList =
                                     *         entityManager.createNativeQuery(sqlUniqEmail,CustomersSuppliersEntity.class).getResultList();
                                     * if(suppliersEntityList.size()>0){
                                     *     add_result.put("status", "error");
                                     *     add_result.put("message", "Το email που δώσατε χρησιμοποιείτε από κάποιον άλλον χρήστη");
                                     *     return add_result;
                                     * }
                                     * **/
                                    String sqlUniqBrand = " select * from customers_suppliers cs where cs.brand_name='"+brandName+"'";
                                    List<CustomersSuppliersEntity>  suppliersEntityList =
                                            entityManager.createNativeQuery(sqlUniqBrand,CustomersSuppliersEntity.class).getResultList();
                                    if(suppliersEntityList.size()>0){
                                        add_result.put("status", "error");
                                        add_result.put("message", "Η επωνυμία που δώσατε χρησιμοποιείτε από κάποιον άλλον χρήστη");
                                        return add_result;
                                    }
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
                                    System.out.println(billingId);
                                    if (billingId != null && billingId != 0) {
                                        warehousesEntity.setBillingId(billingId);
                                    }
                                    warehousesEntity.setCountry(country);
                                    warehousesEntity.setDoy(doy);
                                    warehousesEntity.setCustomerType(customerType);
                                    if (internovaSellerId != null && internovaSellerId != 0) {
                                        warehousesEntity.setInternovaSellerId(internovaSellerId);
                                    }
                                    warehousesEntity.setJob(job);
                                    entityManager.persist(warehousesEntity);
                                    add_result.put("customerSupplierId", warehousesEntity.getId());

                                    if (internovaSellerId != null && internovaSellerId != 0) {
                                        add_result.put("internovaSellerName", entityManager.find(InternovaSellersEntity.class, warehousesEntity.getInternovaSellerId()).getName());
                                    }
                                    if (billingId != null && billingId != 0) {
                                        add_result.put("billingName", entityManager.find(BillingsEntity.class, warehousesEntity.getBillingId()).getName());
                                    }
                                    add_result.put("status", "success");
                                    add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
                                    add_result.put("DO_ID", warehousesEntity.getId());
                                    add_result.put("customerSupplierId", warehousesEntity.getId());
                                    add_result.put("system", "πελατες προμηθευτες");
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
                                    String user_id = json.findPath("user_id").asText();
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
                                    /**
                                    String sqlUniqEmail=" select * from customers_suppliers cs where cs.email='"+email+"'" +" and cs.id!="+id;
                                    List<CustomersSuppliersEntity> suppliersEntityList =
                                            entityManager.createNativeQuery(sqlUniqEmail,CustomersSuppliersEntity.class).getResultList();


                                     * if(suppliersEntityList.size()>0){
                                     *     add_result.put("status", "success");
                                     *     add_result.put("message", "Το email που δώσατε χρησιμοποιείτε από κάποιον άλλον χρήστη");
                                     *     return add_result;
                                     * }
                                     */


                                    String sqlUniqBrand = " select * from customers_suppliers cs where cs.brand_name='"+brandName+"'"+" and cs.id!="+id;;
                                    List<CustomersSuppliersEntity>   suppliersEntityList =
                                            entityManager.createNativeQuery(sqlUniqBrand,CustomersSuppliersEntity.class).getResultList();
                                    if(suppliersEntityList.size()>0){
                                        add_result.put("status", "success");
                                        add_result.put("message", "Η επωνυμία που δώσατε χρησιμοποιείτε από κάποιον άλλον χρήστη");
                                        return add_result;
                                    }
                                    CustomersSuppliersEntity warehousesEntity = entityManager.find(CustomersSuppliersEntity.class, id);
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
                                    if (billingId != null && billingId != 0) {
                                        warehousesEntity.setBillingId(billingId);
                                    }
                                    warehousesEntity.setCountry(country);
                                    warehousesEntity.setDoy(doy);
                                    warehousesEntity.setCustomerType(customerType);
                                    if (internovaSellerId != null && internovaSellerId != 0) {
                                        warehousesEntity.setInternovaSellerId(internovaSellerId);
                                    }
                                    warehousesEntity.setJob(job);
                                    entityManager.merge(warehousesEntity);
                                    add_result.put("status", "success");
                                    add_result.put("message", "Η ενημέρωση πραγματοποίηθηκε με επιτυχία");
                                    add_result.put("DO_ID", warehousesEntity.getId());
                                    add_result.put("system", "πελατες προμηθευτες");
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
    public Result deleteCustomerSupplier(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            ((ObjectNode) json).remove("internovaSeller");
            ((ObjectNode) json).remove("billing");
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> deleteFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode delete_result = Json.newObject();
                                    ((ObjectNode) json).remove("billing");
                                    ((ObjectNode) json).remove("internovaSeller");
                                    Long id = json.findPath("id").asLong();
                                    Long user_id = json.findPath("user_id").asLong();
                                    String sqlOffers = "select * from offers of where of.customer_id=" + id;
                                    List<OffersEntity> suppliersEntityList = (List<OffersEntity>) entityManager.createNativeQuery(sqlOffers, OffersEntity.class).getResultList();
                                    if (suppliersEntityList.size() > 0) {
                                        delete_result.put("status", "error");
                                        delete_result.put("message", "Βρέθηκαν συνδεδεμένες εγγραφές με προσφορές");
                                        return delete_result;
                                    }
                                    String sqlOrders = "select * from orders ord where ord.customer_id="+id;
                                    List<OrdersEntity> ordersEntityList =entityManager.createNativeQuery(sqlOrders,OrdersEntity.class).getResultList();
                                    if (ordersEntityList.size() > 0) {
                                        delete_result.put("status", "error");
                                        delete_result.put("message", "Βρέθηκαν συνδεδεμένες εγγραφές με παραγγελίες");
                                        return delete_result;
                                    }
                                    String sqlManagers = " select * from managers_system ms where ms.system='Πελάτες-Προμηθευτές' and ms.system_id="+id;
                                    List<ManagersSystemEntity> managersSystemEntityList = entityManager.createNativeQuery(sqlManagers,ManagersSystemEntity.class).getResultList();
                                    if (managersSystemEntityList.size() > 0) {
                                        delete_result.put("status", "error");
                                        delete_result.put("message", "Βρέθηκαν συνδεδεμένες εγγραφές με Υπέυθυνους");
                                        return delete_result;
                                    }
                                    String sqlSupExtras = "select * from order_loading_extra_suppliers ols where ols.supplier_id="+id;
                                    List<OrderLoadingExtraSuppliersEntity> orderLoadingExtraSuppliersEntityList = entityManager.createNativeQuery(sqlSupExtras,OrderLoadingExtraSuppliersEntity.class).getResultList();
                                    if (orderLoadingExtraSuppliersEntityList.size() > 0) {
                                        delete_result.put("status", "error");
                                        delete_result.put("message", "Βρέθηκαν συνδεδεμένες εγγραφές με Μερίδες");
                                        return delete_result;
                                    }
                                    String sqlTrucksSups = "select * from suppliers_trucks st where st.customers_suppliers_id="+id;
                                    List<SuppliersTrucksEntity>  suppliersTrucksEntityList = entityManager.createNativeQuery(sqlTrucksSups,SuppliersTrucksEntity.class).getResultList();
                                    if (suppliersTrucksEntityList.size() > 0) {
                                        delete_result.put("status", "error");
                                        delete_result.put("message", "Βρέθηκαν συνδεδεμένες εγγραφές με Φορτηγά");
                                        return delete_result;
                                    }
                                    String sqlOrdLoad = "select * from orders_loading ol where ol.supplier_id="+id;
                                    List<OrdersLoadingEntity> ordersLoadingEntityList = entityManager.createNativeQuery(sqlOrdLoad,OrdersLoadingEntity.class).getResultList();
                                    if (ordersLoadingEntityList.size() > 0) {
                                        delete_result.put("status", "error");
                                        delete_result.put("message", "Βρέθηκαν συνδεδεμένες εγγραφές με Μερίδες");
                                        return delete_result;
                                    }
                                    CustomersSuppliersEntity customersSuppliersEntity = entityManager.find(CustomersSuppliersEntity.class, id);
                                    entityManager.remove(customersSuppliersEntity);
                                    delete_result.put("status", "success");
                                    delete_result.put("message", "Η διαγραφή ολοκληρώθηκε με επιτυχία!");
                                    delete_result.put("DO_ID", customersSuppliersEntity.getId());
                                    delete_result.put("system", "πελατες προμηθευτες");
                                    delete_result.put("user_id", user_id);
                                    return delete_result;
                                });
                            },
                            executionContext);
                    result = (ObjectNode) deleteFuture.get();
                    return ok(result, request);

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
    public Result getRoadCostsBySuplier(final Http.Request request) throws IOException {
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
                                            String id = json.findPath("id").asText();
                                            String sqlNaulo = " select * from " +
                                                    " suppliers_roads_costs src" +
                                                    " where src.customers_suppliers_id=" + id;
                                            System.out.println(sqlNaulo);
                                            List<SuppliersRoadsCostsEntity> nauloList = entityManager.
                                                    createNativeQuery(sqlNaulo, SuppliersRoadsCostsEntity.class).getResultList();
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> schedList = new ArrayList<HashMap<String, Object>>();
                                            for (SuppliersRoadsCostsEntity naulo : nauloList) {
                                                HashMap<String, Object> spmap = new HashMap<String, Object>();
                                                spmap.put("creationDate", naulo.getCreationDate());
                                                spmap.put("cost", naulo.getCost());
                                                spmap.put("fromCountry", naulo.getFromCountry());
                                                spmap.put("fromCity", naulo.getFromCity());
                                                spmap.put("fromCountry", naulo.getFromCountry());
                                                spmap.put("toCity", naulo.getToCity());
                                                spmap.put("toCountry", naulo.getToCountry());
                                                spmap.put("id", naulo.getId());
                                                spmap.put("nauloId", naulo.getId());
                                                schedList.add(spmap);
                                            }
                                            //searchMeasureSearch
                                            returnList_future.put("data", schedList);
                                            returnList_future.put("total", schedList.size());
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
    public Result getCustomersSuppliers(final Http.Request request) throws IOException {
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
                                            String orderCol = json.findPath("orderCol").asText();
                                            String descAsc = json.findPath("descAsc").asText();
                                            String id = json.findPath("id").asText();
                                            String customerSupplierId = json.findPath("customerSupplierId").asText();
                                            String address = json.findPath("address").asText();
                                            String placesSearchInput = json.findPath("placesSearchInput").asText();
                                            String countryCitySearch = json.findPath("countryCitySearch").asText();
                                            String job = json.findPath("job").asText();
                                            String customersSupliersTypes = json.findPath("customersSupliersTypes").asText();
                                            boolean supplier = json.findPath("supplier").asBoolean();
                                            boolean customer = json.findPath("customer").asBoolean();
                                            String brandName = json.findPath("brandName").asText();
                                            String country = json.findPath("country").asText();
                                            String city = json.findPath("city").asText();
                                            String afm = json.findPath("afm").asText();
                                            String email = json.findPath("email").asText();
                                            String postalCode = json.findPath("postalCode").asText();
                                            String region = json.findPath("region").asText();
                                            String telephone = json.findPath("telephone").asText();
                                            String creationDate = json.findPath("creationDate").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlCustSupl = "select * from customers_suppliers pos where 1=1 ";
                                            if (!id.equalsIgnoreCase("") && id != null) {
                                                sqlCustSupl += " and pos.id like " + id;
                                            }
                                            if (!countryCitySearch.equalsIgnoreCase("") && countryCitySearch != null) {
                                                sqlCustSupl += " and ( pos.city like '%" + countryCitySearch + "%' or  pos.country like   '%" + countryCitySearch + "%'  )";
                                            }
                                            if (!customerSupplierId.equalsIgnoreCase("") && customerSupplierId != null) {
                                                sqlCustSupl += " and pos.id like '%" + customerSupplierId + "%'";
                                            }
                                            if (!address.equalsIgnoreCase("") && address != null) {
                                                sqlCustSupl += " and pos.address like '%" + address + "%'";
                                            }
                                            if (!brandName.equalsIgnoreCase("") && brandName != null) {
                                                sqlCustSupl += " and pos.brand_name like '%" + brandName + "%'";
                                            }
                                            if (!job.equalsIgnoreCase("") && job != null) {
                                                sqlCustSupl += " and pos.job like '%" + job + "%'";
                                            }
                                            if (!customersSupliersTypes.equalsIgnoreCase("") && customersSupliersTypes != null && !customersSupliersTypes.equalsIgnoreCase("null")) {
                                                sqlCustSupl += " and pos.customer_type = '" + customersSupliersTypes + "'";
                                            }
                                            if (supplier == true) {
                                                sqlCustSupl += " and pos.customer_type in ('Προμηθευτής','Πελάτης & Προμηθευτής')";
                                            }else if(customer==true){
                                                sqlCustSupl += " and pos.customer_type in ('Πελάτης','Πελάτης & Προμηθευτής')";
                                            }
                                            if (!brandName.equalsIgnoreCase("") && brandName != null) {
                                                sqlCustSupl += " and pos.brand_name like '%" + brandName + "%'";
                                            }
                                            if (!city.equalsIgnoreCase("") && city != null) {
                                                sqlCustSupl += " and pos.city like '%" + city + "%'";
                                            }
                                            if (!afm.equalsIgnoreCase("") && city != null) {
                                                sqlCustSupl += " and pos.afm like '%" + afm + "%'";
                                            }
                                            if (!country.equalsIgnoreCase("") && country != null) {
                                                sqlCustSupl += " and pos.country like '%" + country + "%'";
                                            }
                                            if (placesSearchInput != null && !placesSearchInput.equalsIgnoreCase("")) {
                                                sqlCustSupl += " and pos.id in " +
                                                        "(" +
                                                        "select customers_suppliers_id " +
                                                        "from suppliers_roads_costs src " +
                                                        "where src.from_city like'%" + placesSearchInput + "%' or " +
                                                        " src.from_country like '%" + placesSearchInput + "%' or " +
                                                        " src.to_country like '%" + placesSearchInput + "%' or" +
                                                        " src.to_city like '%" + placesSearchInput + "%' or " +
                                                        " CONCAT(from_country, \" \", from_city,\" \",to_country,\" \",to_city) like '%" + placesSearchInput + "%' )";
                                            }
                                            if (!email.equalsIgnoreCase("") && email != null) {
                                                sqlCustSupl += " and pos.email like '%" + email + "%'";
                                            }
                                            if (!postalCode.equalsIgnoreCase("") && postalCode != null) {
                                                sqlCustSupl += " and pos.postal_code like '%" + postalCode + "%'";
                                            }
                                            if (!region.equalsIgnoreCase("") && region != null) {
                                                sqlCustSupl += " and pos.region like '%" + region + "%'";
                                            }
                                            if (!telephone.equalsIgnoreCase("") && telephone != null) {
                                                sqlCustSupl += " and pos.telephone like '%" + telephone + "%'";
                                            }
                                            if (!creationDate.equalsIgnoreCase("") && creationDate != null) {
                                                sqlCustSupl += " and SUBSTRING( role.creation_date, 1, 10)  = '" + creationDate + "'";
                                            }
                                            List<CustomersSuppliersEntity> filalistAll
                                                    = (List<CustomersSuppliersEntity>) entityManager.createNativeQuery(
                                                    sqlCustSupl, CustomersSuppliersEntity.class).getResultList();
                                            if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                                sqlCustSupl += " order by " + orderCol + " " + descAsc;
                                            } else if(supplier) {
                                                sqlCustSupl += " order by pos.brand_name ";
                                            }else{
                                                sqlCustSupl += " order by creation_date desc";
                                            }

                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sqlCustSupl += " limit " + start + "," + limit;
                                            }
                                            System.out.println(sqlCustSupl);
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> filalist = new ArrayList<HashMap<String, Object>>();
                                            List<CustomersSuppliersEntity> warehousesEntityList
                                                    = (List<CustomersSuppliersEntity>) entityManager.createNativeQuery(
                                                    sqlCustSupl, CustomersSuppliersEntity.class).getResultList();
                                            Integer index = 0;
                                            for (CustomersSuppliersEntity j : warehousesEntityList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                if (j.getInternovaSellerId() != null) {
                                                    sHmpam.put("internovaSeller", entityManager.find(InternovaSellersEntity.class, j.getInternovaSellerId()));
                                                    sHmpam.put("internovaSellerName", entityManager.find(InternovaSellersEntity.class, j.getInternovaSellerId()).getName());
                                                }
                                                if (j.getBillingId() != null) {
                                                    sHmpam.put("billing", entityManager.find(BillingsEntity.class, j.getBillingId()));
                                                    sHmpam.put("billingName", entityManager.find(BillingsEntity.class, j.getBillingId()).getName());
                                                }
                                                sHmpam.put("internovaSellerId", j.getInternovaSellerId());
                                                sHmpam.put("address", j.getAddress());
                                                sHmpam.put("brandName", j.getBrandName());
                                                sHmpam.put("city", j.getCity());
                                                sHmpam.put("email", j.getEmail());
                                                sHmpam.put("job", j.getJob());
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("warehouseId", j.getId());
                                                sHmpam.put("customerSupplierId", j.getId());
                                                sHmpam.put("postalCode", j.getPostalCode());
                                                sHmpam.put("country", j.getCountry());
                                                sHmpam.put("region", j.getRegion());
                                                sHmpam.put("afm", j.getAfm());
                                                sHmpam.put("billingId", j.getBillingId());
                                                sHmpam.put("doy", j.getDoy());
                                                sHmpam.put("website", j.getWebsite());
                                                sHmpam.put("comments", j.getComments());
                                                sHmpam.put("customerType", j.getCustomerType());
                                                if (j.getCustomerType().equalsIgnoreCase("Πελάτης & Προμηθευτής") || j.getCustomerType().equalsIgnoreCase("Προμηθευτής")) {
                                                    sHmpam.put("rowHeight", 580);
                                                } else {
                                                    sHmpam.put("rowHeight", 580);
                                                }
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("telephone", j.getTelephone());
                                                sHmpam.put("updateDate", j.getUpdateDate());
//                                                if(supplier){
//                                                    String sqlFirstOrder = "select * from orders o where o.customer_id = "+j.getId();
//                                                    List<OrdersEntity> ordersEntityList = entityManager.createNativeQuery(sqlFirstOrder,OrdersEntity.class).getResultList();
//                                                    sHmpam.put("orderId", ordersEntityList.get(0));
//                                                }
                                                filalist.add(sHmpam);
                                                index++;
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
    public Result getAllCustomersSuppliersNoPagination(final Http.Request request) throws IOException {
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
                                            //roleDescSearchInput afmSearch customersSupliersTypesSearch


                                            String sqlCustSupl = "select * from customers_suppliers pos where 1=1 order by creation_date desc ";

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
                                                sHmpam.put("customerSupplierId", j.getId());
                                                sHmpam.put("postalCode", j.getPostalCode());
                                                sHmpam.put("country", j.getCountry());
                                                sHmpam.put("region", j.getRegion());
                                                sHmpam.put("afm", j.getAfm());
                                                sHmpam.put("billingId", j.getBillingId());
                                                sHmpam.put("doy", j.getDoy());
                                                sHmpam.put("website", j.getWebsite());
                                                sHmpam.put("internovaSellerId", j.getInternovaSellerId());
                                                if (j.getInternovaSellerId() != null) {
                                                    sHmpam.put("internovaSeller", entityManager.find(InternovaSellersEntity.class, j.getInternovaSellerId()));
                                                    sHmpam.put("internovaSellerName", entityManager.find(InternovaSellersEntity.class, j.getInternovaSellerId()).getName());
                                                }
                                                if (j.getBillingId() != null) {
                                                    sHmpam.put("billing", entityManager.find(BillingsEntity.class, j.getBillingId()));
                                                    sHmpam.put("billingName", entityManager.find(BillingsEntity.class, j.getBillingId()).getName());
                                                }
                                                sHmpam.put("comments", j.getComments());
                                                sHmpam.put("customerType", j.getCustomerType());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("telephone", j.getTelephone());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                filalist.add(sHmpam);
                                            }
                                            returnList_future.put("data", filalist);
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
    public Result addSupplierRoadCost(final Http.Request request) throws IOException {
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
                                    Long customersSuppliersId = json.findPath("customersSuppliersId").asLong();
                                    String fromCountry = json.findPath("fromCountry").asText();
                                    String fromCity = json.findPath("fromCity").asText();
                                    String toCountry = json.findPath("toCountry").asText();
                                    String toCity = json.findPath("toCity").asText();
                                    Double cost = json.findPath("cost").asDouble();

                                    SuppliersRoadsCostsEntity suproad = new SuppliersRoadsCostsEntity();
                                    suproad.setCustomersSuppliersId(customersSuppliersId);
                                    suproad.setFromCity(fromCity);
                                    suproad.setFromCountry(fromCountry);
                                    suproad.setToCountry(toCountry);
                                    suproad.setToCity(toCity);
                                    suproad.setCost(cost);
                                    suproad.setCreationDate(new Date());
                                    entityManager.persist(suproad);

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
    public Result updateSupplierRoadCost(final Http.Request request) throws IOException {
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
                                    Long customersSuppliersId = json.findPath("customersSuppliersId").asLong();
                                    String fromCountry = json.findPath("fromCountry").asText();
                                    String fromCity = json.findPath("fromCity").asText();
                                    String toCountry = json.findPath("toCountry").asText();
                                    String toCity = json.findPath("toCity").asText();
                                    Double cost = json.findPath("cost").asDouble();
                                    Long id = json.findPath("id").asLong();

                                    SuppliersRoadsCostsEntity suproad = entityManager.find(SuppliersRoadsCostsEntity.class, id);
                                    suproad.setCustomersSuppliersId(customersSuppliersId);
                                    suproad.setFromCity(fromCity);
                                    suproad.setFromCountry(fromCountry);
                                    suproad.setToCountry(toCountry);
                                    suproad.setToCity(toCity);
                                    suproad.setCost(cost);
                                    entityManager.merge(suproad);

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
    public Result deleteRoadCost(final Http.Request request) throws IOException {
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
                                    Long id = json.findPath("id").asLong();
                                    SuppliersRoadsCostsEntity suproad = entityManager.find(SuppliersRoadsCostsEntity.class, id);
                                    entityManager.remove(suproad);
                                    add_result.put("status", "success");
                                    add_result.put("message", "Η διαγραφή πραγματοποίηθηκε με επιτυχία");
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
    @BodyParser.Of(BodyParser.Json.class)
    public Result viesvalidator(final Http.Request request) throws IOException {
        JsonNode jsonNode = request.body().asJson();
        if (jsonNode == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                String VAT = jsonNode.findPath("VAT").asText();
                String CountryCode = jsonNode.findPath("CountryCode").asText();
                if(VAT!=null && !VAT.equalsIgnoreCase("") && CountryCode!=null && !CountryCode.equalsIgnoreCase("")){
                    ObjectNode finalResult = Json.newObject();
                    ObjectNode reqBody = Json.newObject();
                    reqBody.put("VAT", VAT);
                    reqBody.put("CountryCode", CountryCode);
                    CompletableFuture<WSResponse> wsFuture = (CompletableFuture)
                            ws.url("https://webservices.synergic.systems/viesvalidator/")
                            .setBody(reqBody)
                            .get().thenApplyAsync(webServiceResponse -> {
                                return webServiceResponse;
                            });
                    finalResult = (ObjectNode) wsFuture.get().asJson();
                    return ok(finalResult);
                }else{
                    ObjectNode result = Json.newObject();
                    result.put("status", "error");
                    result.put("message", "VAT και CountryCode είναι υποχρεωτικά");
                    return ok(result);
                }
            } catch (Exception e) {
                ObjectNode result = Json.newObject();
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Προβλημα κατα την διαγραφή");
                return ok(result);
            }
        }

    }



    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getRoadCostsHistoryBySuppCountryCity(final Http.Request request) throws IOException {
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
                                            String customersSuppliersId = json.findPath("customersSuppliersId").asText();
                                            String fromCountry = json.findPath("fromCountry").asText();
                                            String fromCity = json.findPath("fromCity").asText();
                                            String toCountry = json.findPath("toCountry").asText();
                                            String toCity = json.findPath("toCity").asText();
                                            String cost = json.findPath("cost").asText();
                                            String sqlCustSupl =" select * from suppliers_roads_costs sr \n" +
                                                    " where \n" +
                                                    " sr.from_country= '"+fromCountry+"' \n" +
                                                    " and sr.from_city='"+fromCity+"' \n" +
                                                    " and  sr.to_city='"+toCity+"' \n" +
                                                    " and sr.to_country='"+toCountry+"' and customers_suppliers_id="+customersSuppliersId+" \n" +
                                                    " group by cost ";

                                            System.out.println(sqlCustSupl);

                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> filalist = new ArrayList<HashMap<String, Object>>();
                                            List<SuppliersRoadsCostsEntity> scostsList
                                                    = (List<SuppliersRoadsCostsEntity>) entityManager.createNativeQuery(
                                                    sqlCustSupl, SuppliersRoadsCostsEntity.class).getResultList();
                                            for (SuppliersRoadsCostsEntity j : scostsList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("customersSuppliersId", j.getCustomersSuppliersId());
                                                sHmpam.put("fromCountry", j.getFromCountry());
                                                sHmpam.put("fromCity", j.getFromCity());
                                                sHmpam.put("toCountry", j.getToCountry());
                                                sHmpam.put("toCity", j.getToCity());
                                                sHmpam.put("cost", j.getCost());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                filalist.add(sHmpam);
                                            }
                                            returnList_future.put("data", filalist);
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
    public Result getRoadCosts(final Http.Request request) throws IOException {
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
                                            String customersSuppliersId = json.findPath("customersSuppliersId").asText();
                                            String fromCountry = json.findPath("fromCountry").asText();
                                            String fromCity = json.findPath("fromCity").asText();
                                            String toCountry = json.findPath("toCountry").asText();
                                            String toCity = json.findPath("toCity").asText();
                                            String cost = json.findPath("cost").asText();
                                            String sqlCustSupl = " \n" +
                                                    " select * \n" +
                                                    " from suppliers_roads_costs srcosts\n" +
                                                    " where 1=1\n" +
                                                    " and srcosts.cost=\n" +
                                                    " (select max(s.cost)\n" +
                                                    " from suppliers_roads_costs s\n" +
                                                    " where s.customers_suppliers_id=srcosts.customers_suppliers_id \n" +
                                                    " and s.from_city=srcosts.from_city\n" +
                                                    " and s.from_country=srcosts.from_country\n" +
                                                    " and s.to_city=srcosts.to_city\n" +
                                                    " and s.to_country=srcosts.to_country\n" +
                                                    " \n" +
                                                    " ) ";
                                            if (customersSuppliersId != null && !customersSuppliersId.equalsIgnoreCase("") && !customersSuppliersId.equalsIgnoreCase("null")) {
                                                sqlCustSupl += " and srcosts.customers_suppliers_id=" + customersSuppliersId;
                                            }
                                            if (fromCountry != null && !fromCountry.equalsIgnoreCase("") && !fromCountry.equalsIgnoreCase("null")) {
                                                sqlCustSupl += " and srcosts.from_country like '%" + fromCountry + "%'";
                                            }
                                            if (fromCity != null && !fromCity.equalsIgnoreCase("") && !fromCity.equalsIgnoreCase("null")) {
                                                sqlCustSupl += " and srcosts.from_city like '%" + fromCity + "%'";
                                            }
                                            if (toCountry != null && !toCountry.equalsIgnoreCase("") && !toCountry.equalsIgnoreCase("null")) {
                                                sqlCustSupl += " and srcosts.to_country like '%" + toCountry + "%'";
                                            }
                                            if (toCity != null && !toCity.equalsIgnoreCase("") && !toCity.equalsIgnoreCase("null")) {
                                                sqlCustSupl += " and srcosts.to_city like '%" + toCity + "%'";
                                            }
                                            if (cost != null && !cost.equalsIgnoreCase("") && !cost.equalsIgnoreCase("null")) {
                                                sqlCustSupl += " and srcosts.cost like '%" + cost + "%'";
                                            }
                                            sqlCustSupl += "  group by srcosts.cost ";
                                            sqlCustSupl += " order by creation_date desc";

                                            System.out.println(sqlCustSupl);
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> filalist = new ArrayList<HashMap<String, Object>>();
                                            List<SuppliersRoadsCostsEntity> scostsList
                                                    = (List<SuppliersRoadsCostsEntity>) entityManager.createNativeQuery(
                                                    sqlCustSupl, SuppliersRoadsCostsEntity.class).getResultList();
                                            for (SuppliersRoadsCostsEntity j : scostsList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("customersSuppliersId", j.getCustomersSuppliersId());
                                                sHmpam.put("fromCountry", j.getFromCountry());
                                                sHmpam.put("fromCity", j.getFromCity());
                                                sHmpam.put("toCountry", j.getToCountry());
                                                sHmpam.put("toCity", j.getToCity());
                                                sHmpam.put("cost", j.getCost());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                filalist.add(sHmpam);
                                            }
                                            returnList_future.put("data", filalist);
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
