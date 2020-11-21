package controllers.procedures;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.BillingsEntity;
import models.CustomersSuppliersEntity;
import models.InternovaSellersEntity;
import models.OffersEntity;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.persistence.RollbackException;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

public class OffersController {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public OffersController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }

    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getOffers(final Http.Request request) throws IOException {
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
                    CompletableFuture<HashMap<String, Object>> getFuture = CompletableFuture.supplyAsync(() -> { //
                                return jpaApi.withTransaction(
                                        entityManager -> {
                                            String orderCol = json.findPath("orderCol").asText();
                                            String descAsc = json.findPath("descAsc").asText();
                                            String id = json.findPath("id").asText();
                                            String offerId = json.findPath("offerId").asText();
                                            String offerDate = json.findPath("offerDate").asText();
                                            String aa = json.findPath("aa").asText();
                                            String customer = json.findPath("customer").asText();
                                            String status = json.findPath("status").asText();
                                            String from = json.findPath("from").asText();
                                            String to = json.findPath("to").asText();
                                            String seller = json.findPath("seller").asText();
                                            String billing = json.findPath("billing").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlCustSupl = "select * from offers offer where 1=1 ";
                                            if (!id.equalsIgnoreCase("") && id != null) {
                                                sqlCustSupl += " and offer.id like '%" + id + "%'";
                                            }
                                            if (!offerId.equalsIgnoreCase("") && offerId != null) {
                                                sqlCustSupl += " and offer.id = " + offerId;
                                            }
                                            if (!aa.equalsIgnoreCase("") && aa != null) {
                                                sqlCustSupl += " and offer.aa like '%" + aa + "%'";
                                            }
                                            if (!seller.equalsIgnoreCase("") && seller != null) {
                                                sqlCustSupl +=
                                                        " and offer.seller_id " +
                                                                " in ( select id" +
                                                                "      from  internova_sellers isell" +
                                                                "      where isell.name like '%" + seller + "%' ) " +
                                                                " union " +
                                                                " select offer.*" +
                                                                " from offers offer " +
                                                                " join customers_suppliers cs on (cs.id=offer.customer_id and offer.seller_id is null )" +
                                                                " where " +
                                                                " cs.internova_seller_id in " +
                                                                " (select id from  internova_sellers isell where isell.name like '%" + seller + "%' )";
                                            }

                                            if (!customer.equalsIgnoreCase("") && customer != null) {
                                                sqlCustSupl += " and offer.customer_id  in " +
                                                        " ( select id from  customers_suppliers cs where cs.brand_name like '%" + customer + "%' )";
                                            }

                                            if (!status.equalsIgnoreCase("") && status != null) {
                                                sqlCustSupl += " and offer.status like '%" + status + "%'";
                                            }

                                            if (!from.equalsIgnoreCase("") && from != null) {
                                                sqlCustSupl += " and offer.from_address like '%" + from + "%'";
                                            }

                                            if (!to.equalsIgnoreCase("") && to != null) {
                                                sqlCustSupl += " and offer.to_address like '%" + to + "%'";
                                            }
                                            if (!billing.equalsIgnoreCase("") && billing != null) {
                                                sqlCustSupl +=
                                                        " and offer.billing_id " +
                                                                " in ( select id" +
                                                                "      from  billings billing" +
                                                                "      where billing.name like '%" + billing + "%' ) " +
                                                                " union " +
                                                                " select offer.*" +
                                                                " from offers offer " +
                                                                " join customers_suppliers cs on (cs.id=offer.customer_id and offer.billing_id is null )" +
                                                                " where " +
                                                                " cs.billing_id in " +
                                                                " (select id from  billings billing where billing.name like '%" + billing + "%' )";
                                            }
                                            if (!offerDate.equalsIgnoreCase("") && offerDate != null) {
                                                sqlCustSupl += " and SUBSTRING( offer.offer_date, 1, 10)  = '" + offerDate + "'";
                                            }
                                            List<OffersEntity> filalistAll
                                                    = (List<OffersEntity>) entityManager.createNativeQuery(
                                                    sqlCustSupl, OffersEntity.class).getResultList();
                                            if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                                if(orderCol.equalsIgnoreCase("billingName")){
                                                    sqlCustSupl +=  " order by (select name from billings b where b.id=offer.billing_id)"+ descAsc;
                                                }else if (orderCol.equalsIgnoreCase("sellerName")){
                                                    sqlCustSupl +=  " order by (select name from internova_sellers iseller where iseller.id=offer.seller_id)"+ descAsc;
                                                }else if(orderCol.equalsIgnoreCase("brandName")){
                                                    sqlCustSupl +=  " order by (select brand_name from customers_suppliers cs where cs.id=offer.customer_id)"+ descAsc;
                                                }else{
                                                    sqlCustSupl += " order by " + orderCol + " " + descAsc;
                                                }
                                            } else {
                                                sqlCustSupl += " order by creation_date desc";
                                            }


                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sqlCustSupl += " limit " + start + "," + limit;
                                            }
                                            System.out.println(sqlCustSupl);
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> filalist = new ArrayList<HashMap<String, Object>>();
                                            List<OffersEntity> offersEntityList
                                                    = (List<OffersEntity>) entityManager.createNativeQuery(
                                                    sqlCustSupl, OffersEntity.class).getResultList();
                                            String sqlMin = "select min(id) from offers cs ";
                                            String sqlMax = "select max(id) from offers cs ";
                                            BigInteger minId = (BigInteger) entityManager.createNativeQuery(sqlMin).getSingleResult();
                                            BigInteger maxId = (BigInteger) entityManager.createNativeQuery(sqlMax).getSingleResult();
                                            for (OffersEntity j : offersEntityList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("customerId", j.getCustomerId());
                                                sHmpam.put("aa", j.getAa());
                                                if (j.getCustomerId() != null) {
                                                    HashMap<String, Object> customerMap = new HashMap<String, Object>();
                                                    HashMap<String, Object> billingsMap = new HashMap<String, Object>();
                                                    HashMap<String, Object> sellerMap = new HashMap<String, Object>();
                                                    CustomersSuppliersEntity customersSuppliersEntity = entityManager.find(CustomersSuppliersEntity.class, j.getCustomerId());
                                                    customerMap.put("customerId", customersSuppliersEntity.getId());
                                                    customerMap.put("email", customersSuppliersEntity.getEmail());
                                                    customerMap.put("telephone", customersSuppliersEntity.getTelephone());
                                                    customerMap.put("brandName", customersSuppliersEntity.getBrandName());
                                                    if (j.getBillingId() != null) {
                                                        billingsMap.put("billingId", j.getBillingId());
                                                        billingsMap.put("billingName", entityManager.find(BillingsEntity.class, j.getBillingId()).getName());
                                                    } else {
                                                        billingsMap.put("billingId", customersSuppliersEntity.getBillingId());
                                                        billingsMap.put("billingName", entityManager.find(BillingsEntity.class, customersSuppliersEntity.getBillingId()).getName());
                                                    }
                                                    sHmpam.put("billings", billingsMap);
                                                    if (j.getSellerId() != null) {
                                                        sellerMap.put("sellerId", j.getSellerId());
                                                        sellerMap.put("sellerName", entityManager.find(InternovaSellersEntity.class, j.getSellerId()).getName());
                                                    } else {
                                                        sellerMap.put("sellerId", customersSuppliersEntity.getInternovaSellerId());
                                                        sellerMap.put("sellerName", entityManager.find(InternovaSellersEntity.class, customersSuppliersEntity.getInternovaSellerId()).getName());
                                                    }
                                                    sHmpam.put("seller", sellerMap);
                                                    sHmpam.put("custommer", customerMap);
                                                }
                                                sHmpam.put("comments", j.getComments());
                                                sHmpam.put("offerDate", j.getOfferDate());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                HashMap<String, Object> fromAddress = new HashMap<String, Object>();
                                                fromAddress.put("city", j.getFromCity());
                                                fromAddress.put("address", j.getFromAddress());
                                                fromAddress.put("country", j.getFromCountry());
                                                fromAddress.put("lattitude", j.getFromLattitude());
                                                fromAddress.put("longtitude", j.getFromLongtitude());
                                                fromAddress.put("postalCode", j.getFromPostalCode());
                                                fromAddress.put("region", j.getFromRegion());
                                                sHmpam.put("from", fromAddress);
                                                HashMap<String, Object> toAddress = new HashMap<String, Object>();
                                                toAddress.put("city", j.getFromCity());
                                                toAddress.put("address", j.getToAddress());
                                                toAddress.put("country", j.getToCountry());
                                                toAddress.put("lattitude", j.getToLattitude());
                                                toAddress.put("longtitude", j.getToLongtitude());
                                                toAddress.put("postalCode", j.getToPostalCode());
                                                toAddress.put("region", j.getToRegion());
                                                sHmpam.put("to", toAddress);
                                                sHmpam.put("status", j.getStatus());
                                                String sqlNextId = "select min(id) from offers cs where cs.creation_date >" + "'" + j.getCreationDate() + "'";
                                                String sqlPreviousId = "select max(id) from offers cs where cs.creation_date < " + "'" + j.getCreationDate() + "'";
                                                BigInteger nextId = (BigInteger) entityManager.createNativeQuery(sqlNextId).getSingleResult();
                                                BigInteger previousId = (BigInteger) entityManager.createNativeQuery(sqlPreviousId).getSingleResult();
                                                if (nextId != null) {
                                                    sHmpam.put("previousId", nextId);
                                                } else {
                                                    sHmpam.put("previousId", maxId);
                                                }
                                                if (previousId != null) {
                                                    sHmpam.put("nextId", previousId);
                                                } else {
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


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteOffer(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> deleteFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                Long id = json.findPath("id").asLong();
                                OffersEntity offersEntity = entityManager.find(OffersEntity.class, id);
                                entityManager.remove(offersEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η διαγραφή πραγματοποίηθηκε με επιτυχία");
                                return add_result;
                            });
                        },
                        executionContext);
                result = (ObjectNode) deleteFuture.get();
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

    @SuppressWarnings("Duplicates")
// Generates a random int with n digits
    public static int generateRandomDigits(int n) {
        int m = (int) Math.pow(10, n - 1);
        return m + new Random().nextInt(9 * m);
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addOffer(final Http.Request request) throws IOException, Exception {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                try {
                                    DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    JsonNode custommer = json.findPath("custommer2");
                                    ((ObjectNode) json).remove("custommer2");

                                    JsonNode internovaSeller = json.findPath("internovaSeller");
                                    JsonNode billing = json.findPath("billing");
                                    JsonNode from = json.findPath("from");
                                    JsonNode to = json.findPath("to");
                                    ((ObjectNode) json).remove("internovaSeller");
                                    ((ObjectNode) json).remove("billing");
                                    ((ObjectNode) json).remove("from");
                                    ((ObjectNode) json).remove("to");
                                    ((ObjectNode) custommer).remove("billing");
                                    ((ObjectNode) custommer).remove("internovaSeller");
                                    ObjectNode add_result = Json.newObject();
                                    OffersEntity billingsEntity = new OffersEntity();
                                    billingsEntity.setAa((long) generateRandomDigits(3));
                                    Date offerDateString = new SimpleDateFormat("yyyy-MM-dd").parse(json.findPath("offerDate").asText());
                                    billingsEntity.setOfferDate(offerDateString);
                                    billingsEntity.setSellerId(internovaSeller.findPath("sellerId").asLong());
                                    billingsEntity.setBillingId(billing.findPath("billingId").asLong());
                                    billingsEntity.setComments(json.findPath("offers_comments").asText());
                                    billingsEntity.setStatus(json.findPath("status").asText());
                                    billingsEntity.setCreationDate(new Date());
                                    billingsEntity.setCustomerId(custommer.findPath("id").asLong());
                                    billingsEntity.setFromAddress(from.findPath("address").asText());
                                    billingsEntity.setFromCity(from.findPath("city").asText());
                                    billingsEntity.setFromCountry(from.findPath("country").asText());
                                    billingsEntity.setFromPostalCode(from.findPath("postalCode").asText());
                                    billingsEntity.setFromRegion(from.findPath("region").asText());
                                    billingsEntity.setFromLattitude(from.findPath("lattitude").asDouble());
                                    billingsEntity.setFromLongtitude(from.findPath("longtitude").asDouble());
                                    billingsEntity.setToAddress(to.findPath("address").asText());
                                    billingsEntity.setToCity(from.findPath("city").asText());
                                    billingsEntity.setToCountry(to.findPath("country").asText());
                                    billingsEntity.setToPostalCode(to.findPath("postalCode").asText());
                                    billingsEntity.setToRegion(to.findPath("region").asText());
                                    billingsEntity.setToLattitude(to.findPath("lattitude").asDouble());
                                    billingsEntity.setToLongtitude(to.findPath("longtitude").asDouble());
                                    entityManager.persist(billingsEntity);
                                    add_result.put("status", "success");
                                    add_result.put("offerId", billingsEntity.getId());
                                    add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
                                    return add_result;
                                } catch (ParseException e) {
                                    ObjectNode add_result = Json.newObject();
                                    e.printStackTrace();
                                    add_result.put("status", "error");
                                    add_result.put("message", "Προβλημα κατα την καταχωρηση");
                                    return add_result;
                                }
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
    public Result updateOffer(final Http.Request request) throws IOException, Exception {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                try {
                                    DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    JsonNode internovaSeller = json.findPath("internovaSeller");
                                    JsonNode billing = json.findPath("billing");
                                    JsonNode custommer = json.findPath("custommer2");
                                    JsonNode from = json.findPath("from");
                                    JsonNode to = json.findPath("to");
                                    Long offerId = json.findPath("offerId").asLong();
                                    ((ObjectNode) json).remove("internovaSeller");
                                    ((ObjectNode) json).remove("billing");
                                    ((ObjectNode) json).remove("custommer2");
                                    ((ObjectNode) json).remove("from");
                                    ((ObjectNode) json).remove("to");
                                    ((ObjectNode) custommer).remove("billing");
                                    ((ObjectNode) custommer).remove("internovaSeller");
                                    ObjectNode add_result = Json.newObject();
                                    OffersEntity billingsEntity =entityManager.find(OffersEntity.class,offerId);
                                    billingsEntity.setAa((long) generateRandomDigits(3));
                                    Date offerDateString = new SimpleDateFormat("yyyy-MM-dd").parse(json.findPath("offerDate").asText());
                                    billingsEntity.setOfferDate(offerDateString);
                                    billingsEntity.setSellerId(internovaSeller.findPath("sellerId").asLong());
                                    billingsEntity.setBillingId(billing.findPath("billingId").asLong());
                                    billingsEntity.setComments(json.findPath("offers_comments").asText());
                                    billingsEntity.setStatus(json.findPath("status").asText());
                                    billingsEntity.setCreationDate(new Date());
                                    billingsEntity.setCustomerId(custommer.findPath("customerId").asLong());
                                    billingsEntity.setFromAddress(from.findPath("address").asText());
                                    billingsEntity.setFromCity(from.findPath("city").asText());
                                    billingsEntity.setFromCountry(from.findPath("country").asText());
                                    billingsEntity.setFromPostalCode(from.findPath("postalCode").asText());
                                    billingsEntity.setFromRegion(from.findPath("region").asText());
                                    billingsEntity.setFromLattitude(from.findPath("lattitude").asDouble());
                                    billingsEntity.setFromLongtitude(from.findPath("longtitude").asDouble());
                                    billingsEntity.setToAddress(to.findPath("address").asText());
                                    billingsEntity.setToCity(from.findPath("city").asText());
                                    billingsEntity.setToCountry(to.findPath("country").asText());
                                    billingsEntity.setToPostalCode(to.findPath("postalCode").asText());
                                    billingsEntity.setToRegion(to.findPath("region").asText());
                                    billingsEntity.setToLattitude(to.findPath("lattitude").asDouble());
                                    billingsEntity.setToLongtitude(to.findPath("longtitude").asDouble());
                                    entityManager.merge(billingsEntity);
                                    add_result.put("status", "success");
                                    add_result.put("message", "Η ενημέρωση πραγματοποίηθηκε με επιτυχία");
                                    return add_result;
                                } catch (ParseException e) {
                                    ObjectNode add_result = Json.newObject();
                                    e.printStackTrace();
                                    add_result.put("status", "error");
                                    add_result.put("message", "Προβλημα κατα την καταχωρηση");
                                    return add_result;
                                }
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




}
