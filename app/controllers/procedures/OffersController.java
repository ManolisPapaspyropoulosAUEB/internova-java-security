package controllers.procedures;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import controllers.execution_context.DatabaseExecutionContext;
import controllers.system.Application;
import models.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

public class OffersController extends Application {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;
    private static final String ALGO = "AES";
    private static final byte[] keyValue =
            new byte[]{'T', 'h', 'e', 'B', 'e', 's', 't',
                    'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y'};

    @Inject
    public OffersController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        super(jpaApi, executionContext);
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;


    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getAllOffersNoPagination(final Http.Request request) throws IOException {
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
                                            String sqlCustSupl = "select * from offers offer where 1=1 ";
                                            String id = json.findPath("id").asText();
                                            String customer = json.findPath("customer").asText();
                                            if (!customer.equalsIgnoreCase("") && customer != null) {
                                                sqlCustSupl += " and offer.customer_id  in " +
                                                        " ( select id from  customers_suppliers cs where cs.brand_name like '%" + customer + "%' )";
                                            }
                                            if(id!=null && !id.equalsIgnoreCase("")){
                                                sqlCustSupl+=" and offer.id like '%"+id+"%'";
                                            }
                                            sqlCustSupl+=" order by offer.creation_date desc";
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> filalist = new ArrayList<HashMap<String, Object>>();
                                            List<OffersEntity> offersEntityList
                                                    = (List<OffersEntity>) entityManager.createNativeQuery(
                                                    sqlCustSupl, OffersEntity.class).getResultList();
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
                                                sHmpam.put("offerId", j.getId());
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
    public Result getOffersSchedulesPackegesByUserId(final Http.Request request) throws IOException {
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
                                            String id = json.findPath("offersScheduleId").asText();
                                            String measureUnitLabel = json.findPath("measureUnitLabel").asText();
                                            String measureuUitPrice = json.findPath("measureuUitPrice").asText();
                                            String measureTo = json.findPath("measureTo").asText();
                                            String measureFrom = json.findPath("measureFrom").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sql = "select * from schedule_package_offer sp where 1=1 ";
                                            if (!id.equalsIgnoreCase("") && id != null) {
                                                sql += " and sp.offer_schedule_id =" + id + "";
                                            }
                                            if (!measureUnitLabel.equalsIgnoreCase("") && measureUnitLabel != null) {
                                                sql += " and sp.measurement_unit_id in " +
                                                        " (select id from measurement_unit mu where mu.title like '%" + measureUnitLabel + "%' ) ";
                                            }
                                            if (!measureTo.equalsIgnoreCase("") && measureTo != null) {
                                                sql += " and sp.to like '%" + measureTo + "%'";
                                            }
                                            if (!measureFrom.equalsIgnoreCase("") && measureFrom != null) {
                                                sql += " and sp.from like '%" + measureFrom + "%'";
                                            }
                                            if (!measureuUitPrice.equalsIgnoreCase("") && measureuUitPrice != null) {
                                                sql += " and sp.unit_price like '%" + measureuUitPrice + "%'";
                                            }
                                            List<SchedulePackageOfferEntity> schedulePackagesEntityListaLL
                                                    = (List<SchedulePackageOfferEntity>) entityManager.createNativeQuery(
                                                    sql, SchedulePackageOfferEntity.class).getResultList();
                                            if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                                if (orderCol.equalsIgnoreCase("measureUnitLabel")) {
                                                    sql += " order by (select title from measurement_unit r where r.id=sp.measurement_unit_id)" + descAsc;
                                                } else {
                                                    sql += " order by " + orderCol + " " + descAsc;
                                                }
                                            } else {
                                                sql += " order by sp.creation_date desc";
                                            }
                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sql += " limit " + start + "," + limit;
                                            }
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> schedList = new ArrayList<HashMap<String, Object>>();
                                            List<SchedulePackageOfferEntity> scheduleEntityList
                                                    = (List<SchedulePackageOfferEntity>) entityManager.createNativeQuery(
                                                    sql, SchedulePackageOfferEntity.class).getResultList();
                                            for (SchedulePackageOfferEntity sp : scheduleEntityList) {
                                                HashMap<String, Object> spmap = new HashMap<String, Object>();
                                                MeasurementUnitEntity measurementUnit = entityManager.find(MeasurementUnitEntity.class, sp.getMeasureUnitId());
                                                spmap.put("measurementUnit_id", measurementUnit.getId());
                                                spmap.put("measurementUnit_title", measurementUnit.getTitle());
                                                spmap.put("measurementUnit", measurementUnit);
                                                spmap.put("measurementUnitId", sp.getMeasureUnitId());
                                                spmap.put("from", sp.getFromUnit().toString());
                                                spmap.put("to", sp.getToUnit().toString());
                                                spmap.put("offersScheduleId", sp.getOfferScheduleId());
                                                spmap.put("shdulesPackageId", sp.getId());
                                                spmap.put("id", sp.getId());
                                                spmap.put("unitPrice", sp.getUnitPrice().toString());
                                                spmap.put("updateDate", sp.getUpdateDate());
                                                spmap.put("creationDate", sp.getCreationDate());
                                                spmap.put("measureUnitLabel", entityManager.find(MeasurementUnitEntity.class, sp.getMeasureUnitId()).getTitle());
                                                schedList.add(spmap);
                                            }
                                            returnList_future.put("data", schedList);
                                            returnList_future.put("total", schedulePackagesEntityListaLL.size());
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
    public Result getOffers(final Http.Request request) throws IOException {
        ObjectNode result = Json.newObject();//
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
                                            String idOrdersSearch = json.findPath("idOrdersSearch").asText();
                                            String customer = json.findPath("customer").asText();
                                            String suplierId = json.findPath("suplierId").asText();
                                            String status = json.findPath("status").asText();
                                            String from = json.findPath("from").asText();
                                            String to = json.findPath("to").asText();
                                            String seller = json.findPath("seller").asText();
                                            String billing = json.findPath("billing").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sqlCustSupl = "select * from offers offer where 1=1 ";
                                            if (!id.equalsIgnoreCase("") && id != null) {
                                                sqlCustSupl += " and offer.id =" + id + "";
                                            }
                                            if(suplierId!=null && !suplierId.equalsIgnoreCase("")){
                                                sqlCustSupl += " and  offer.customer_id="+suplierId;
                                            }
                                            if(idOrdersSearch!=null && !idOrdersSearch.equalsIgnoreCase("")
                                                    && !idOrdersSearch.equalsIgnoreCase("null")){
                                                sqlCustSupl += " and  offer.id in (select ord.offer_id from orders ord where ord.id like '%"+idOrdersSearch+"%' ) ";
                                            }
                                            if (!customer.equalsIgnoreCase("") && customer != null) {
                                                sqlCustSupl += " and offer.customer_id  in " +
                                                        " ( select id from  customers_suppliers cs where cs.brand_name like '%" + customer + "%' )";
                                            }
                                            if (!status.equalsIgnoreCase("") && status != null) {
                                                sqlCustSupl += " and offer.status like '%" + status + "%'";
                                            }
                                            if (!offerId.equalsIgnoreCase("") && offerId != null) {
                                                sqlCustSupl += " and offer.id like '%" + offerId + "%'";
                                            }
                                            if (!aa.equalsIgnoreCase("") && aa != null) {
                                                sqlCustSupl += " and offer.aa like '%" + aa + "%'";
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
                                            if (!seller.equalsIgnoreCase("") && seller != null) {
                                                sqlCustSupl +=
                                                        " and offer.seller_id " +
                                                                " in ( select id" +
                                                                "      from  internova_sellers isell" +
                                                                "      where isell.name like '%" + seller + "%' ) " ;
                                            }
                                            if (!from.equalsIgnoreCase("") && from != null) {
                                                sqlCustSupl += " and offer.from_address like '%" + from + "%'";
                                            }
                                            if (!to.equalsIgnoreCase("") && to != null) {
                                                sqlCustSupl += " and offer.to_address like '%" + to + "%'";
                                            }

                                            if (!offerDate.equalsIgnoreCase("") && offerDate != null) {
                                                sqlCustSupl += " and SUBSTRING( offer.offer_date, 1, 10)  = '" + offerDate + "'";
                                            }
                                            List<OffersEntity> filalistAll
                                                    = (List<OffersEntity>) entityManager.createNativeQuery(
                                                    sqlCustSupl, OffersEntity.class).getResultList();
                                            if (!orderCol.equalsIgnoreCase("") && orderCol != null) {
                                                if (orderCol.equalsIgnoreCase("billingName")) {
                                                    sqlCustSupl += " order by (select name from billings b where b.id=offer.billing_id)" + descAsc;
                                                } else if (orderCol.equalsIgnoreCase("sellerName")) {
                                                    sqlCustSupl += " order by (select name from internova_sellers iseller where iseller.id=offer.seller_id)" + descAsc;
                                                } else if (orderCol.equalsIgnoreCase("brandName")) {
                                                    sqlCustSupl += " order by (select brand_name from customers_suppliers cs where cs.id=offer.customer_id)" + descAsc;
                                                } else if ( orderCol.equalsIgnoreCase("idOrders") ) {
                                                    sqlCustSupl += "order by (select count(ord.id) from orders ord where ord.offer_id=offer.id) " + descAsc;
                                                }else{
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
                                            List<OffersEntity> offersEntityList
                                                    = (List<OffersEntity>) entityManager.createNativeQuery(
                                                    sqlCustSupl, OffersEntity.class).getResultList();

                                            for (OffersEntity j : offersEntityList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                String orderCheck = "select * from orders ord where ord.offer_id=" + j.getId();
                                                List<OrdersEntity> ordersEntityListcheck = entityManager.createNativeQuery(orderCheck, OrdersEntity.class).getResultList();
                                                if (ordersEntityListcheck.size() > 0) {
                                                    sHmpam.put("orderExist", true);

                                                } else {
                                                    sHmpam.put("orderExist", false);
                                                }
                                                String sqlOrders = "select GROUP_CONCAT(concat('#','(',ord.id,')'))  as ordids \n" +
                                                        "from orders ord where ord.offer_id = "+j.getId();
                                                String ordids = (String) entityManager.createNativeQuery(sqlOrders).getSingleResult();

                                                if (ordids != null) {
                                                    sHmpam.put("ordids", ordids);
                                                } else {
                                                    sHmpam.put("ordids", "-");
                                                }
                                                String isLatestVersionSql
                                                        = "select offer.id  from offers offer\n" +
                                                        " where customer_id= "+j.getCustomerId()+"  \n" +
                                                        " and offer.creation_date\n" +
                                                        " =(select max(offer2.creation_date) from offers offer2 where customer_id="+j.getCustomerId()+")\n" +
                                                        " ";
                                                BigInteger latestVersionOfferId = (BigInteger) entityManager.createNativeQuery(isLatestVersionSql).getSingleResult();
                                                if(latestVersionOfferId.longValue()==j.getId()){
                                                    sHmpam.put("isLatestVersion", true);
                                                }else{
                                                    sHmpam.put("isLatestVersion", false);
                                                }
                                                sHmpam.put("latestVersionOfferId", latestVersionOfferId);
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("acceptOfferDate", j.getAcceptOfferDate());
                                                sHmpam.put("sendOfferDate", j.getSendOfferDate());
                                                sHmpam.put("offerDate", j.getOfferDate());
                                                sHmpam.put("customerId", j.getCustomerId());
                                                sHmpam.put("managerCustomerId", j.getManagerCustomerId());
                                                sHmpam.put("aa", j.getAa());
                                                if (j.getCustomerId() != null) {
                                                    HashMap<String, Object> customerMap = new HashMap<String, Object>();
                                                    HashMap<String, Object> billingsMap = new HashMap<String, Object>();
                                                    HashMap<String, Object> sellerMap = new HashMap<String, Object>();
                                                    HashMap<String, Object> managerMap = new HashMap<String, Object>();
                                                    CustomersSuppliersEntity customersSuppliersEntity = entityManager.find(CustomersSuppliersEntity.class, j.getCustomerId());
                                                    customerMap.put("customerId", customersSuppliersEntity.getId());
                                                    customerMap.put("email", customersSuppliersEntity.getEmail());
                                                    customerMap.put("telephone", customersSuppliersEntity.getTelephone());
                                                    customerMap.put("brandName", customersSuppliersEntity.getBrandName());

                                                    if(j.getManagerCustomerId()!=null){
                                                        managerMap.put("managerCustomerId", j.getManagerCustomerId());
                                                        ManagersEntity managersEntity = entityManager.find(ManagersEntity.class,j.getManagerCustomerId());
                                                        managerMap.put("managerName", managersEntity.getFirstName()+" "+managersEntity.getLastName());
                                                    }
                                                    sHmpam.put("managerMap", managerMap);

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
                                                sHmpam.put("declineReasons", j.getDeclineReasons());
                                                sHmpam.put("childaddIndcator", false);
                                                HashMap<String, Object> fromAddress = new HashMap<String, Object>();
                                                fromAddress.put("city", j.getFromCity());
                                                fromAddress.put("address", j.getFromAddress());
                                                fromAddress.put("country", j.getFromCountry());
                                                fromAddress.put("lattitude", j.getFromLattitude());
                                                fromAddress.put("longtitude", j.getFromLongtitude());
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
                                                sHmpam.put("statusTemp", j.getStatus());
                                                String sqlOffersSchedules = "select * " +
                                                        " from offers_schedules os" +
                                                        " where os.offer_id= " + j.getId() + " " +
                                                        " order by os.creation_date asc";

                                                List<OffersSchedulesEntity> offschelist = (List<OffersSchedulesEntity>)
                                                        entityManager.createNativeQuery(sqlOffersSchedules, OffersSchedulesEntity.class).getResultList();
                                                List<HashMap<String, Object>> offschelistFinal = new ArrayList<HashMap<String, Object>>();
                                                for (OffersSchedulesEntity osent : offschelist) {

                                                    HashMap<String, Object> osentMap = new HashMap<String, Object>();
                                                    String sqlOrd = "select * from order_schedules ords where ords.offer_schedule_id= '" + osent.getId() + "'";
                                                    List<OrderSchedulesEntity> orderSchedulesEntityList =
                                                            entityManager.createNativeQuery(sqlOrd, OrderSchedulesEntity.class).getResultList();

                                                    osentMap.put("countOrders",orderSchedulesEntityList.size());
                                                    if (orderSchedulesEntityList.size() > 0) {
                                                        osentMap.put("order_id", orderSchedulesEntityList.get(0).getOrderId());
                                                    }

                                                    List<HashMap<String, Object>> offerOrderList= new ArrayList<HashMap<String, Object>>();
                                                    for(OrderSchedulesEntity ordsd : orderSchedulesEntityList){
                                                        HashMap<String, Object> ordOfferMap = new HashMap<String, Object>();
                                                        OrdersEntity ordersEntity = entityManager.find(OrdersEntity.class,ordsd.getOrderId());
                                                        ordOfferMap.put("id", "#"+ordersEntity.getId());
                                                        ordOfferMap.put("orderId", ordersEntity.getId());
                                                        ordOfferMap.put("status", ordersEntity.getStatus());
                                                        ordOfferMap.put("creationDate", ordersEntity.getCreationDate());
                                                        offerOrderList.add(ordOfferMap);
                                                    }



                                                    osentMap.put("isPopoverOpen", false);
                                                    osentMap.put("offerOrderList", offerOrderList);
                                                    osentMap.put("fromAddress", osent.getFromAddress());
                                                    osentMap.put("token", osent.getToken());
                                                    osentMap.put("fromCity", osent.getFromCity());
                                                    osentMap.put("fromCountry", osent.getFromCountry());
                                                    osentMap.put("fromPostalCode", osent.getFromPostalCode());
                                                    osentMap.put("toAddress", osent.getToAddress());
                                                    osentMap.put("toCity", osent.getToCity());
                                                    osentMap.put("toCountry", osent.getToCountry());
                                                    osentMap.put("toPostalCode", osent.getToPostalCode());
                                                    osentMap.put("offerScheduleId", osent.getId());
                                                    osentMap.put("offerId", osent.getOfferId());
                                                    osentMap.put("id", osent.getId());
                                                    osentMap.put("type", osent.getType());
                                                    String sqlWayPoigetnts = "select * " +
                                                            " from offer_schedule_between_waypoints wp" +
                                                            " where wp.offer_id=" + j.getId() + " and wp.offer_schedule_id= " + osent.getId();
                                                    List<OfferScheduleBetweenWaypointsEntity> waypointsEntityList =
                                                            entityManager.createNativeQuery(sqlWayPoigetnts, OfferScheduleBetweenWaypointsEntity.class).getResultList();
                                                    osentMap.put("waypointsEntityList", waypointsEntityList);
                                                    List<HashMap<String, Object>> spoList = new ArrayList<HashMap<String, Object>>();
                                                    String offersUnit = "select * from schedule_package_offer spo where spo.offer_id=" + osent.getOfferId() +
                                                            " and spo.offer_schedule_id=" + osent.getId() + " ";
                                                    List<SchedulePackageOfferEntity> schedulePackage = (List<SchedulePackageOfferEntity>) entityManager.createNativeQuery(offersUnit, SchedulePackageOfferEntity.class).getResultList();
                                                    for (SchedulePackageOfferEntity spo : schedulePackage) {
                                                        HashMap<String, Object> spoMap = new HashMap<String, Object>();
                                                        spoMap.put("from", spo.getFromUnit());
                                                        spoMap.put("to", spo.getToUnit());
                                                        spoMap.put("unitPrice", spo.getUnitPrice());
                                                        spoMap.put("typePackageMeasure", spo.getTypePackageMeasure());
                                                        spoMap.put("scheduleId", osent.getId());//todo:na ginei object id
                                                        spoMap.put("id", spo.getId());
                                                        spoMap.put("comments", spo.getComments());
                                                        spoMap.put("measureUnitId", spo.getMeasureUnitId());
                                                        spoMap.put("measurementUnitId", spo.getMeasureUnitId());
                                                        spoMap.put("measureUnitLabel", entityManager.find(MeasurementUnitEntity.class, spo.getMeasureUnitId()).getTitle());
                                                        spoMap.put("measurementUnit", entityManager.find(MeasurementUnitEntity.class, spo.getMeasureUnitId()));
                                                        spoList.add(spoMap);
                                                    }
                                                    osentMap.put("schedulePackageList", spoList);
                                                    offschelistFinal.add(osentMap);
                                                }
                                                sHmpam.put("tableDataTimokatalogosProsfores", offschelistFinal);
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
                    DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd");
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
                                ((ObjectNode) json).remove("tableDataTimokatalogosProsfores");
                                Long user_id = json.findPath("user_id").asLong();
                                Long id = json.findPath("id").asLong();
                                OffersEntity offersEntity = entityManager.find(OffersEntity.class, id);
                                entityManager.remove(offersEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η διαγραφή πραγματοποίηθηκε με επιτυχία");
                                add_result.put("DO_ID", offersEntity.getId());
                                add_result.put("system", "Προσφορές");
                                add_result.put("user_id", user_id);
                                return add_result;
                            });
                        },
                        executionContext);
                result = (ObjectNode) deleteFuture.get();
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
    public Result getSuggestedOffersSchedulesByCustomer(final Http.Request request) throws IOException {
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
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> finalList = new ArrayList<HashMap<String, Object>>();
                                            Long customerId = json.findPath("customerId").asLong();
                                            String fromCity = json.findPath("fromCity").asText();
                                            String fromCountry = json.findPath("fromCountry").asText();
                                            String toCity = json.findPath("toCity").asText();
                                            String toCountry = json.findPath("toCountry").asText();
                                            String type = json.findPath("type").asText();
                                            String typeCategory = json.findPath("typeCategory").asText();
                                            String sqlCustSupl = "" +
                                                    "select * from " +
                                                    "( " +
                                                    "select " +
                                                    "sched.id, " +
                                                    "sched.from_city, " +
                                                    "sched.from_country, " +
                                                    "sched.to_city, " +
                                                    "sched.to_country, " +
                                                    "sched.id as object_id, " +
                                                    "'Δρομολόγιο' as type , sched.creation_date , " +
                                                    "null as from_address, " +
                                                    "null as to_address, " +
                                                    "sched.from_postal_code, " +
                                                    "sched.to_postal_code " +
                                                    "from internova_db.schedule sched " +
                                                    "where  sched.id in " +
                                                    "(select schedule_id " +
                                                    "from schedule_packages sp ) " +

                                                    " union " +

                                                    "select " +
                                                    "offesched.id, " +
                                                    "offesched.from_city, " +
                                                    "offesched.from_country, " +
                                                    "offesched.to_city, " +
                                                    "offesched.to_country, " +
                                                    "offesched.id as object_id, " +
                                                    "'Προσφορά' as type , offesched.creation_date , " +
                                                    " offesched.from_address as from_address, " +
                                                    " offesched.to_address as to_address, " +
                                                    "offesched.from_postal_code, " +
                                                    "offesched.to_postal_code " +
                                                    "from offers_schedules offesched " +
                                                    "left join offers offe on (offe.id=offesched.offer_id) " +
                                                    "where offe.customer_id=" + customerId +

                                                    " union " +

                                                    "select " +
                                                    "offesched.id, " +
                                                    "offesched.from_city, " +
                                                    "offesched.from_country, " +
                                                    "offesched.to_city, " +
                                                    "offesched.to_country, " +
                                                    "offesched.id as object_id, " +
                                                    "'Προσφορά απο άλλους' as type , offesched.creation_date , " +
                                                    " offesched.from_address as from_address, " +
                                                    " offesched.to_address as to_address, " +
                                                    "offesched.from_postal_code, " +
                                                    "offesched.to_postal_code " +
                                                    "from offers_schedules offesched " +
                                                    "left join offers offe on (offe.id=offesched.offer_id) " +
                                                    "where offe.customer_id!= " + customerId +

                                                    " ) " +

                                                    "as sugg where 1=1 ";
                                            if (typeCategory.equalsIgnoreCase("1")) {
                                                sqlCustSupl += " and sugg.type ='Δρομολόγιο'";
                                            } else if (typeCategory.equalsIgnoreCase("2")) {
                                                sqlCustSupl += " and sugg.type ='Προσφορά'";
                                            } else if (typeCategory.equalsIgnoreCase("3")) {
                                                sqlCustSupl += " and sugg.type ='Προσφορά απο άλλους'";
                                            }
                                            if (!fromCity.equalsIgnoreCase("") && fromCity != null) {
                                                sqlCustSupl += " and sugg.from_city like '%" + fromCity + "%'";
                                            }
                                            if (!fromCountry.equalsIgnoreCase("") && fromCountry != null) {
                                                sqlCustSupl += " and sugg.from_country like '%" + fromCountry + "%'";
                                            }
                                            if (!toCity.equalsIgnoreCase("") && toCity != null) {
                                                sqlCustSupl += " and sugg.to_city like '%" + toCity + "%'";
                                            }
                                            if (!toCountry.equalsIgnoreCase("") && toCountry != null) {
                                                sqlCustSupl += " and sugg.to_country like '%" + toCountry + "%'";
                                            }
                                            if (!type.equalsIgnoreCase("") && type != null) {
                                                sqlCustSupl += " and sugg.type like '%" + type + "%'";
                                            }
                                            sqlCustSupl += " order by sugg.id desc ";
                                            List tul = entityManager.createNativeQuery(sqlCustSupl).getResultList();
                                            Iterator it = tul.iterator();
                                            ArrayList<HashMap<String, Object>> foreisList = new ArrayList<>();
                                            while (it.hasNext()) {
                                                JsonNode tu = Json.toJson(it.next());
                                                HashMap<String, Object> item = new HashMap<>();
                                                item.put("fromCity", tu.get(1).asText());
                                                item.put("fromCountry", tu.get(2).asText());
                                                item.put("toCity", tu.get(3).asText());
                                                item.put("toCountry", tu.get(4).asText());
                                                item.put("id", tu.get(5).asText());
                                                item.put("type", tu.get(6).asText());
                                                item.put("creationDate", tu.get(7).asText());
                                                item.put("fromAddress", tu.get(8).asText());
                                                item.put("toAddress", tu.get(9).asText());
                                                item.put("fromPostalCode", tu.get(10).asText());
                                                item.put("toPostalCode", tu.get(11).asText());
                                                if(!typeCategory.equalsIgnoreCase("1")){
                                                    String sql=
                                                            " select * " +
                                                            " from offer_schedule_between_waypoints wp" +
                                                            " where wp.offer_schedule_id= "+tu.get(5).asText();
                                                    List<OfferScheduleBetweenWaypointsEntity> waypointsEntityList =
                                                            entityManager.createNativeQuery(sql,OfferScheduleBetweenWaypointsEntity.class).getResultList();
                                                    item.put("waypointsEntityList",waypointsEntityList);

                                                }
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
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Πρόβλημα κατά την ανάγνωση των στοιχείων");
            return ok(result);
        }
    }




    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getAllSuggestedSchedulesByCustomer(final Http.Request request) throws IOException {
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

                                            System.out.println(json);
                                            String offerScheduleId = json.findPath("offerScheduleId").asText();
                                            Long offerId = json.findPath("offerId").asLong();
                                            OffersEntity offersEntity = entityManager.find(OffersEntity.class,offerId);


//                                            String sqlSuggSchedules = " " +
//                                                    "  select * from \n" +
//                                                    "( \n" +
//                                                    "select * \n" +
//                                                    "from offers_schedules offs where offer_id="+offerId+" and id!="+offerScheduleId+" \n" +
//                                                    "union \n" +
//                                                    "select * from \n" +
//                                                    "offers_schedules offs \n" +
//                                                    "where offs.offer_id in (select offer.id from offers offer where offer.customer_id="+offersEntity.getCustomerId()+" and offer.status='ΑΠΟΔΟΧΗ') \n" +
//                                                    "and  offs.offer_id!="+offerId+") as sugg \n" +
//                                                    "where sugg.from_city in \n" +
//                                                    "( \n" +
//                                                    "select offs.from_city as city from offers_schedules offs where offs.id="+offerScheduleId+" \n" +
//                                                    "union \n" +
//                                                    "select offs.to_city as city from offers_schedules offs where offs.id="+offerScheduleId+" \n" +
//                                                    "union \n" +
//                                                    "(select city from offer_schedule_between_waypoints waypoints where  waypoints.offer_schedule_id="+offerScheduleId+") \n" +
//                                                    " ); ";


                                            String sqlSuggSchedules="" +
                                                    "select *\n" +
                                                    "from offers_schedules offs where offer_id="+offerId+" and id!="+offerScheduleId;

                                            System.out.println(sqlSuggSchedules);
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                            List<OffersSchedulesEntity> orgsList
                                                    = (List<OffersSchedulesEntity>) entityManager.createNativeQuery(
                                                    sqlSuggSchedules, OffersSchedulesEntity.class).getResultList();
                                            for (OffersSchedulesEntity j : orgsList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("offerScheduleId", j.getId());
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("offerId", j.getOfferId());
                                                sHmpam.put("fromCountry", j.getFromCountry());
                                                sHmpam.put("fromCity", j.getFromCity());
                                                sHmpam.put("fromPostalCode", j.getFromPostalCode());
                                                sHmpam.put("toCountry", j.getToCountry());
                                                sHmpam.put("toCity", j.getToCity());
                                                sHmpam.put("toPostalCode", j.getToPostalCode());

                                                String packages = "select * " +
                                                        "from schedule_package_offer pack " +
                                                        "where pack.offer_schedule_id="+offerScheduleId;
                                                List<SchedulePackageOfferEntity> packagesList = (List<SchedulePackageOfferEntity>) entityManager.createNativeQuery(
                                                        packages,SchedulePackageOfferEntity.class).getResultList();
                                                List<HashMap<String, Object>> schedList = new ArrayList<HashMap<String, Object>>();
                                                for (SchedulePackageOfferEntity sp : packagesList) {
                                                    HashMap<String, Object> packMap = new HashMap<String, Object>();
                                                    packMap.put("from", sp.getFromUnit().toString());
                                                    packMap.put("to", sp.getToUnit().toString());
                                                    packMap.put("offersScheduleId", sp.getOfferScheduleId());
                                                    packMap.put("shdulesPackageId", sp.getId());
                                                    packMap.put("id", sp.getId());
                                                    packMap.put("measurementUnitId", sp.getMeasureUnitId());
                                                    MeasurementUnitEntity measurementUnit = entityManager.find(MeasurementUnitEntity.class, sp.getMeasureUnitId());
                                                    packMap.put("measurementUnit_id", measurementUnit.getId());
                                                    packMap.put("measurementUnit_title", measurementUnit.getTitle());
                                                    packMap.put("measurementUnit", measurementUnit);
                                                    packMap.put("unitPrice", sp.getUnitPrice().toString());
                                                    packMap.put("updateDate", sp.getUpdateDate());
                                                    packMap.put("creationDate", sp.getCreationDate());
                                                    packMap.put("measureUnitLabel", entityManager.find(MeasurementUnitEntity.class, sp.getMeasureUnitId()).getTitle());
                                                    schedList.add(packMap);
                                                }
                                                sHmpam.put("schedList", schedList);
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
    public Result updateOfferStatus(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                String status = json.findPath("status").asText();
                                Long offerId = json.findPath("offerId").asLong();
                                OffersEntity offersEntity = entityManager.find(OffersEntity.class,offerId);
                                offersEntity.setStatus(status);
                                entityManager.merge(offersEntity);
                                add_result.put("status", "success");
                                add_result.put("message", "Η ενημέρωση πραγματοποίηθηκε με επιτυχία");
                                return add_result;
                            });
                        },
                        executionContext);
                result = (ObjectNode) addFuture.get();
                return ok( result);
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
                                DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                JsonNode custommer = json.findPath("custommer2");
                                ((ObjectNode) json).remove("custommer2");
                                Long offerId = json.findPath("offerId").asLong();
                                String user_id = json.findPath("user_id").asText();
                                JsonNode internovaSeller = json.findPath("internovaSeller");
                                JsonNode manager = json.findPath("manager");
                                JsonNode billing = json.findPath("billing");
                                JsonNode from = json.findPath("from");
                                JsonNode to = json.findPath("to");
                                JsonNode tableDataTimokatalogosProsfores = json.findPath("tableDataTimokatalogosProsfores");
                                ((ObjectNode) json).remove("tableDataTimokatalogosProsfores");
                                ((ObjectNode) json).remove("internovaSeller");
                                ((ObjectNode) json).remove("schedulesPackages");
                                ((ObjectNode) json).remove("billing");
                                ((ObjectNode) json).remove("from");
                                ((ObjectNode) json).remove("to");
                                ((ObjectNode) custommer).remove("billing");
                                ((ObjectNode) custommer).remove("internovaSeller");
                                ObjectNode add_result = Json.newObject();
                                OffersEntity offersEntity = entityManager.find(OffersEntity.class, offerId);
                                offersEntity.setAa((long) generateRandomDigits(3));

                                if (json.findPath("offerDate").asText() != null && !json.findPath("offerDate").asText().equalsIgnoreCase("")) {
                                    try {
                                        Date offerDateString = myDateFormat.parse(json.findPath("offerDate").asText());
                                        offersEntity.setOfferDate(offerDateString);
                                        System.out.println(offerDateString);

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }


                                if (json.findPath("sendOfferDate").asText() != null && !json.findPath("sendOfferDate").asText().equalsIgnoreCase("")) {
                                    try {
                                        Date sendofferDateString = myDateFormat.parse(json.findPath("sendOfferDate").asText());
                                        offersEntity.setSendOfferDate(sendofferDateString);
                                        System.out.println(sendofferDateString);

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }


                                offersEntity.setSellerId(internovaSeller.findPath("sellerId").asLong());
                                offersEntity.setBillingId(billing.findPath("billingId").asLong());
                                offersEntity.setComments(json.findPath("offers_comments").asText());
                                System.out.println(json.findPath("status").asText().equalsIgnoreCase("ΑΠΟΔΟΧΗ") && !offersEntity.getStatus().equalsIgnoreCase("ΑΠΟΔΟΧΗ"));
                                if(json.findPath("status").asText().equalsIgnoreCase("ΑΠΟΔΟΧΗ") && !offersEntity.getStatus().equalsIgnoreCase("ΑΠΟΔΟΧΗ")){
                                    offersEntity.setAcceptOfferDate(new Date());
                                }
                                offersEntity.setStatus(json.findPath("status").asText());
                                offersEntity.setDeclineReasons(json.findPath("declineReasons").asText());
                                offersEntity.setUpdateDate(new Date());
                                offersEntity.setCustomerId(custommer.findPath("customerSupplierId").asLong());
                                offersEntity.setManagerCustomerId(manager.findPath("managerCustomerId").asLong());
                                offersEntity.setFromAddress(from.findPath("address").asText());
                                offersEntity.setFromCity(from.findPath("city").asText());
                                offersEntity.setFromCountry(from.findPath("country").asText());
                                offersEntity.setFromPostalCode(from.findPath("postalCode").asText().replaceAll(" ", ""));
                                offersEntity.setFromRegion(from.findPath("region").asText());
                                offersEntity.setFromLattitude(from.findPath("lattitude").asDouble());
                                offersEntity.setFromLongtitude(from.findPath("longtitude").asDouble());
                                offersEntity.setToAddress(to.findPath("address").asText());
                                offersEntity.setToCity(from.findPath("city").asText());
                                offersEntity.setToCountry(to.findPath("country").asText());
                                offersEntity.setToPostalCode(to.findPath("postalCode").asText().replaceAll(" ", ""));
                                offersEntity.setToRegion(to.findPath("region").asText());
                                offersEntity.setToLattitude(to.findPath("lattitude").asDouble());
                                offersEntity.setToLongtitude(to.findPath("longtitude").asDouble());
                                entityManager.merge(offersEntity);
                                String sqlOffSchedules = "select * from offers_schedules os where os.offer_id=" + offerId;
                                List<OffersSchedulesEntity> ofList = (List<OffersSchedulesEntity>) entityManager.createNativeQuery(sqlOffSchedules, OffersSchedulesEntity.class).getResultList();
                                for (OffersSchedulesEntity ofsElement : ofList) {
                                    String sqlSchedulesPackeges = "select * from schedule_package_offer spo where spo.offer_id=" + offerId + " and spo.offer_schedule_id=" + ofsElement.getId();
                                    List<SchedulePackageOfferEntity> spoEntList =
                                            (List<SchedulePackageOfferEntity>) entityManager.createNativeQuery(sqlSchedulesPackeges, SchedulePackageOfferEntity.class).getResultList();
                                    for (SchedulePackageOfferEntity p : spoEntList) {
                                        entityManager.remove(p);
                                    }
                                    String wayPoints = "select * from offer_schedule_between_waypoints osw where osw.offer_id=" +
                                            offersEntity.getId();
                                    List<OfferScheduleBetweenWaypointsEntity> waypointsEntityList =
                                            entityManager.createNativeQuery(wayPoints, OfferScheduleBetweenWaypointsEntity.class).getResultList();
                                    for (OfferScheduleBetweenWaypointsEntity wp : waypointsEntityList) {
                                        entityManager.remove(wp);
                                    }
                                    entityManager.remove(ofsElement);
                                }
                                Iterator itOffersSCHEDULDE = tableDataTimokatalogosProsfores.iterator();
                                while (itOffersSCHEDULDE.hasNext()) {
                                    JsonNode offerScheduleNode = (JsonNode) itOffersSCHEDULDE.next();
                                    OffersSchedulesEntity offersSchedulesEntity = new OffersSchedulesEntity();
                                    offersSchedulesEntity.setUpdateDate(new Date());
                                    offersSchedulesEntity.setType(offerScheduleNode.findPath("type").asText());
                                    offersSchedulesEntity.setToken(offerScheduleNode.findPath("token").asText());
                                    offersSchedulesEntity.setFromAddress(offerScheduleNode.findPath("departure").findPath("fromAddress").asText());
                                    offersSchedulesEntity.setFromCity(offerScheduleNode.findPath("departure").findPath("fromCity").asText());
                                    offersSchedulesEntity.setFromCountry(offerScheduleNode.findPath("departure").findPath("fromCountry").asText());
                                    offersSchedulesEntity.setFromPostalCode(offerScheduleNode.findPath("departure").findPath("fromPostalCode").asText().replaceAll(" ", ""));
                                    offersSchedulesEntity.setToAddress(offerScheduleNode.findPath("arrival").findPath("toAddress").asText());
                                    offersSchedulesEntity.setToCity(offerScheduleNode.findPath("arrival").findPath("toCity").asText());
                                    offersSchedulesEntity.setToCountry(offerScheduleNode.findPath("arrival").findPath("toCountry").asText());
                                    offersSchedulesEntity.setToPostalCode(offerScheduleNode.findPath("arrival").findPath("toPostalCode").asText().replaceAll(" ", ""));

                                    offersSchedulesEntity.setOfferId(offersEntity.getId());
                                    entityManager.persist(offersSchedulesEntity);

                                    Iterator itChilds = offerScheduleNode.findPath("schedulePackageList").iterator();
                                    while (itChilds.hasNext()) {
                                        JsonNode schedulePackageOfferNode = (JsonNode) itChilds.next();
                                        SchedulePackageOfferEntity schedulePackageOfferEntity = new SchedulePackageOfferEntity();
                                        schedulePackageOfferEntity.setOfferId(offersEntity.getId());
                                        schedulePackageOfferEntity.setComments(schedulePackageOfferNode.findPath("comments").asText());
                                        schedulePackageOfferEntity.setTypePackageMeasure(schedulePackageOfferNode.findPath("typePackageMeasure").asText());
                                        schedulePackageOfferEntity.setCreationDate(new Date());
                                        schedulePackageOfferEntity.setOfferScheduleId(offersSchedulesEntity.getId()); //TODO
                                        schedulePackageOfferEntity.setFromUnit(schedulePackageOfferNode.findPath("from").asInt());
                                        if(schedulePackageOfferNode.findPath("from").asInt()>schedulePackageOfferNode.findPath("to").asInt()){
                                            schedulePackageOfferEntity.setToUnit(schedulePackageOfferNode.findPath("from").asInt());
                                        }else{
                                            schedulePackageOfferEntity.setToUnit(schedulePackageOfferNode.findPath("to").asInt());
                                        }
                                        schedulePackageOfferEntity.setUnitPrice(schedulePackageOfferNode.findPath("unitPrice").asDouble());
                                        String sqlUnit = "select * from measurement_unit mu where mu.title='" + schedulePackageOfferNode.findPath("measureUnitLabel").asText() + "'";
                                        List<MeasurementUnitEntity> muList = (List<MeasurementUnitEntity>) entityManager.createNativeQuery(sqlUnit, MeasurementUnitEntity.class).getResultList();
                                        schedulePackageOfferEntity.setMeasureUnitId(muList.get(0).getId());
                                        entityManager.persist(schedulePackageOfferEntity);
                                    }
                                    //270.977,34
                                    Iterator itWayPoints = offerScheduleNode.findPath("waypointsEntityList").iterator();
                                    while (itWayPoints.hasNext()) {
                                        JsonNode wayPoint = (JsonNode) itWayPoints.next();
                                        OfferScheduleBetweenWaypointsEntity waypoint = new OfferScheduleBetweenWaypointsEntity();
                                        waypoint.setNestedScheduleIndicator(wayPoint.findPath("nestedScheduleIndicator").asInt());
                                        waypoint.setCity(wayPoint.findPath("city").asText());
                                        waypoint.setCountry(wayPoint.findPath("country").asText());
                                        waypoint.setPostalCode(wayPoint.findPath("postalCode").asText().replaceAll(" ", ""));
                                        waypoint.setOfferId(offersEntity.getId());
                                        waypoint.setOfferScheduleId(offersSchedulesEntity.getId());
                                        waypoint.setCreationDate(new Date());
                                        entityManager.persist(waypoint);
                                    }
                                }
                                add_result.put("status", "success");
                                add_result.put("offerId", offersEntity.getId());
                                add_result.put("message", "Η ενημέρωση πραγματοποίηθηκε με επιτυχία");
                                add_result.put("DO_ID", offersEntity.getId());
                                add_result.put("system", "Προσφορές");
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


    public void genarateOfferAsPdf() throws IOException {

        try {


//                                                genarateOfferAsPdf();
            PDDocument pdDocument = new PDDocument();
            PDPage pdPage = new PDPage();
            pdDocument.addPage(pdPage);

            PDPageContentStream contentStream = new PDPageContentStream(pdDocument, pdPage);
            contentStream.beginText();
            contentStream.newLineAtOffset(25, 700);
            contentStream.setLeading(14.5f);
            PDFont unicodeFont = PDType0Font.load(pdDocument, new File("c:/windows/fonts/Arial.ttf"));
            contentStream.setFont(unicodeFont, 14);


            contentStream.showText("Προσφορά1");
            contentStream.newLine();
            contentStream.showText("Προσφορά3");
            contentStream.newLine();
            contentStream.showText("Προσφορά5");

            contentStream.endText();
            contentStream.close();
            pdDocument.save("D:/developm/internova(Pr)/blank2.pdf");
            pdDocument.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//
//    @SuppressWarnings({"Duplicates", "unchecked"})
//    @BodyParser.Of(BodyParser.Json.class)
//    public Result cloneOffer(final Http.Request request) throws IOException {
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
//                                Long offerId = json.findPath("offerId").asLong();
//
//
//
//
//                                entityManager.persist(ordersEntity);
//                                add_result.put("status", "success");
//                                add_result.put("id", ordersEntity.getId());
//                                add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
//                                add_result.put("DO_ID", ordersEntity.getId());
//                                add_result.put("system", "ΠΑΡΑΓΓΕΛΙΕΣ");
//                                add_result.put("user_id", user_id);
//                                return add_result;
//                            });
//                        },
//                        executionContext);
//                result = (ObjectNode) addFuture.get();
//                return ok(result,request);
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
                                    System.out.println(json);
                                    DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    JsonNode custommer = json.findPath("custommer2");
                                    String user_id = json.findPath("user_id").asText();
                                    ((ObjectNode) json).remove("custommer2");
                                    JsonNode internovaSeller = json.findPath("internovaSeller");
                                    JsonNode billing = json.findPath("billing");
                                    JsonNode manager = json.findPath("manager");
                                    JsonNode from = json.findPath("from");
                                    JsonNode to = json.findPath("to");
                                    boolean cloneInd = json.findPath("cloneInd").asBoolean();
                                    JsonNode tableDataTimokatalogosProsfores =
                                            json.findPath("tableDataTimokatalogosProsfores");
                                    ((ObjectNode) json).remove("tableDataTimokatalogosProsfores");
                                    ((ObjectNode) json).remove("internovaSeller");
                                    ((ObjectNode) json).remove("schedulesPackages");
                                    ((ObjectNode) json).remove("billing");
                                    ((ObjectNode) json).remove("from");
                                    ((ObjectNode) json).remove("to");
                                    ((ObjectNode) custommer).remove("billing");
                                    ((ObjectNode) custommer).remove("internovaSeller");
                                    ObjectNode add_result = Json.newObject();
                                    OffersEntity offersEntity = new OffersEntity();
                                    offersEntity.setAa((long) generateRandomDigits(3));
//                                    Date offerDateString = new SimpleDateFormat("yyyy-MM-dd").parse(json.findPath("offerDate").asText());
//                                    offersEntity.setOfferDate(offerDateString);


                                    if (json.findPath("offerDate").asText() != null && !json.findPath("offerDate").asText().equalsIgnoreCase("")) {
                                        try {
                                            Date offerDateString = myDateFormat.parse(json.findPath("offerDate").asText());
                                            offersEntity.setOfferDate(offerDateString);

                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    if (json.findPath("sendOfferDate").asText() != null && !json.findPath("sendOfferDate").asText().equalsIgnoreCase("")) {
                                        try {
                                            Date sendofferDateString = myDateFormat.parse(json.findPath("sendOfferDate").asText());
                                            offersEntity.setSendOfferDate(sendofferDateString);

                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }


                                    offersEntity.setSellerId(internovaSeller.findPath("sellerId").asLong());
                                    offersEntity.setBillingId(billing.findPath("billingId").asLong());
                                    offersEntity.setComments(json.findPath("offers_comments").asText());
                                    if (cloneInd == true) {
                                        offersEntity.setStatus("ΝΕΑ");
                                    } else {
                                        offersEntity.setStatus(json.findPath("status").asText());
                                        if(json.findPath("status").asText().equalsIgnoreCase("ΑΠΟΔΟΧΗ")){
                                            offersEntity.setAcceptOfferDate(new Date());
                                        }
                                    }
                                    offersEntity.setDeclineReasons(json.findPath("declineReasons").asText());
                                    offersEntity.setCreationDate(new Date());
                                    if (custommer.findPath("customerSupplierId").asText() != null && !custommer.findPath("customerSupplierId").asText().equalsIgnoreCase("")) {
                                        offersEntity.setCustomerId(custommer.findPath("customerSupplierId").asLong());
                                    } else {
                                        offersEntity.setCustomerId(custommer.findPath("customerId").asLong());
                                    }
                                    offersEntity.setManagerCustomerId(manager.findPath("managerCustomerId").asLong());
                                    offersEntity.setFromAddress(from.findPath("address").asText());
                                    offersEntity.setFromCity(from.findPath("city").asText());
                                    offersEntity.setFromCountry(from.findPath("country").asText());
                                    offersEntity.setFromPostalCode(from.findPath("postalCode").asText().replaceAll(" ", ""));
                                    offersEntity.setFromRegion(from.findPath("region").asText());
                                    offersEntity.setFromLattitude(from.findPath("lattitude").asDouble());
                                    offersEntity.setFromLongtitude(from.findPath("longtitude").asDouble());
                                    offersEntity.setToAddress(to.findPath("address").asText());
                                    offersEntity.setToCity(to.findPath("city").asText());
                                    offersEntity.setToCountry(to.findPath("country").asText());
                                    offersEntity.setToPostalCode(to.findPath("postalCode").asText().replaceAll(" ", ""));
                                    offersEntity.setToRegion(to.findPath("region").asText());
                                    offersEntity.setToLattitude(to.findPath("lattitude").asDouble());
                                    offersEntity.setToLongtitude(to.findPath("longtitude").asDouble());
                                    entityManager.persist(offersEntity);
                                    Iterator itOffersSCHEDULDE = tableDataTimokatalogosProsfores.iterator();
                                    while (itOffersSCHEDULDE.hasNext()) {
                                        JsonNode offerScheduleNode = (JsonNode) itOffersSCHEDULDE.next();
                                        OffersSchedulesEntity offersSchedulesEntity = new OffersSchedulesEntity();
                                        offersSchedulesEntity.setCreationDate(new Date());
                                        offersSchedulesEntity.setType(offerScheduleNode.findPath("type").asText());
                                        offersSchedulesEntity.setFromAddress(offerScheduleNode.findPath("departure").findPath("fromAddress").asText());
                                        offersSchedulesEntity.setFromCity(offerScheduleNode.findPath("departure").findPath("fromCity").asText());
                                        offersSchedulesEntity.setFromCountry(offerScheduleNode.findPath("departure").findPath("fromCountry").asText());
                                        offersSchedulesEntity.setFromPostalCode(offerScheduleNode.findPath("departure").findPath("fromPostalCode").asText().replaceAll(" ", ""));
                                        offersSchedulesEntity.setToAddress(offerScheduleNode.findPath("arrival").findPath("toAddress").asText());
                                        offersSchedulesEntity.setToCity(offerScheduleNode.findPath("arrival").findPath("toCity").asText());
                                        offersSchedulesEntity.setToCountry(offerScheduleNode.findPath("arrival").findPath("toCountry").asText());
                                        offersSchedulesEntity.setToPostalCode(offerScheduleNode.findPath("arrival").findPath("toPostalCode").asText().replaceAll(" ", ""));
                                        offersSchedulesEntity.setOfferId(offersEntity.getId());
                                        offersSchedulesEntity.setToken(encrypt(offersSchedulesEntity.getCreationDate().toString().concat(offersSchedulesEntity.getFromCity())));
                                        entityManager.persist(offersSchedulesEntity);
                                        String sqlSchedules = "select * from schedule s where s.from_address='" +
                                                offerScheduleNode.findPath("departure").findPath("fromAddress").asText() + "' and s.to_address='" +
                                                offerScheduleNode.findPath("arrival").findPath("toAddress").asText() + "' and s.from_city='" + offerScheduleNode.findPath("departure").findPath("fromCity").asText() + "'" +
                                                " and s.to_city='" + offerScheduleNode.findPath("arrival").findPath("toCity").asText() + "' and s.from_country='" + offerScheduleNode.findPath("departure").findPath("fromCountry").asText() + "'" +
                                                " and s.to_country='" + offerScheduleNode.findPath("arrival").findPath("toCountry").asText() + "'";
                                        List<ScheduleEntity> scheduleList = (List<ScheduleEntity>) entityManager.createNativeQuery(sqlSchedules, ScheduleEntity.class).getResultList();
                                        ScheduleEntity scheduleEntity = new ScheduleEntity();
                                        if (scheduleList.size() == 0) {
                                            scheduleEntity.setCreationDate(new Date());//
                                            scheduleEntity.setFromAddress(offerScheduleNode.findPath("departure").findPath("fromAddress").asText());
                                            scheduleEntity.setFromCity(offerScheduleNode.findPath("departure").findPath("fromCity").asText());
                                            scheduleEntity.setFromCountry(offerScheduleNode.findPath("departure").findPath("fromCountry").asText());
                                            scheduleEntity.setFromPostalCode(offerScheduleNode.findPath("departure").findPath("fromPostalCode").asText().replaceAll(" ", ""));
                                            scheduleEntity.setToAddress(offerScheduleNode.findPath("arrival").findPath("toAddress").asText());
                                            scheduleEntity.setToCity(offerScheduleNode.findPath("arrival").findPath("toCity").asText());
                                            scheduleEntity.setToCountry(offerScheduleNode.findPath("arrival").findPath("toCountry").asText());
                                            scheduleEntity.setToPostalCode(offerScheduleNode.findPath("arrival").findPath("toPostalCode").asText().replaceAll(" ", ""));
                                            entityManager.persist(scheduleEntity);
                                            // todo:longititude kai lattitude!
                                        }
                                        Iterator itChilds = offerScheduleNode.findPath("schedulePackageList").iterator();
                                        while (itChilds.hasNext()) {
                                            JsonNode schedulePackageOfferNode = (JsonNode) itChilds.next();
                                            SchedulePackageOfferEntity schedulePackageOfferEntity = new SchedulePackageOfferEntity();
                                            schedulePackageOfferEntity.setOfferId(offersEntity.getId());
                                            schedulePackageOfferEntity.setComments(schedulePackageOfferNode.findPath("comments").asText());
                                            schedulePackageOfferEntity.setCreationDate(new Date());
                                            schedulePackageOfferEntity.setOfferScheduleId(offersSchedulesEntity.getId()); //TODO
                                            schedulePackageOfferEntity.setTypePackageMeasure(schedulePackageOfferNode.findPath("typePackageMeasure").asText());
                                            schedulePackageOfferEntity.setFromUnit(schedulePackageOfferNode.findPath("from").asInt());
                                            schedulePackageOfferEntity.setToUnit(schedulePackageOfferNode.findPath("to").asInt());
                                            schedulePackageOfferEntity.setUnitPrice(schedulePackageOfferNode.findPath("unitPrice").asDouble());
                                            String sqlUnit = "select * from measurement_unit mu where mu.title='" + schedulePackageOfferNode.findPath("measureUnitLabel").asText() + "'";
                                            List<MeasurementUnitEntity> muList = (List<MeasurementUnitEntity>) entityManager.createNativeQuery(sqlUnit, MeasurementUnitEntity.class).getResultList();
                                            schedulePackageOfferEntity.setMeasureUnitId(muList.get(0).getId());
                                            entityManager.persist(schedulePackageOfferEntity);
                                            if (scheduleList.size() == 0) {
                                                SchedulePackagesEntity schedulePackagesEntity = new SchedulePackagesEntity();
                                                schedulePackagesEntity.setScheduleId(scheduleEntity.getId());
                                                schedulePackagesEntity.setFromUnit(schedulePackageOfferNode.findPath("from").asInt());
                                                schedulePackagesEntity.setToUnit(schedulePackageOfferNode.findPath("to").asInt());
                                                schedulePackagesEntity.setMeasurementUnitId(muList.get(0).getId());
                                                schedulePackagesEntity.setCreationDate(new Date());
                                                schedulePackagesEntity.setUnitPrice(schedulePackageOfferNode.findPath("unitPrice").asDouble());
                                                entityManager.persist(schedulePackagesEntity);
                                            }
                                        }
                                        Iterator itWayPoints = offerScheduleNode.findPath("waypointsEntityList").iterator();//waypointsEntityList wayPoints
                                        while (itWayPoints.hasNext()) {
                                            JsonNode wayPoint = (JsonNode) itWayPoints.next();
                                            OfferScheduleBetweenWaypointsEntity waypoint = new OfferScheduleBetweenWaypointsEntity();
                                            waypoint.setNestedScheduleIndicator(wayPoint.findPath("nestedScheduleIndicator").asInt());
                                            waypoint.setCity(wayPoint.findPath("city").asText());
                                            waypoint.setCountry(wayPoint.findPath("country").asText());
                                            waypoint.setPostalCode(wayPoint.findPath("postalCode").asText().replaceAll(" ", ""));
                                            waypoint.setOfferId(offersEntity.getId());
                                            waypoint.setOfferScheduleId(offersSchedulesEntity.getId());
                                            waypoint.setCreationDate(new Date());
                                            entityManager.persist(waypoint);
                                        }
                                    }
                                    add_result.put("status", "success");
                                    add_result.put("offerId", offersEntity.getId());
                                    add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
                                    add_result.put("DO_ID", offersEntity.getId());
                                    add_result.put("system", "Προσφορές");
                                    add_result.put("user_id", user_id);
                                    return add_result;
                                } catch (Exception e) {
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
    public Result insertCountriesDb(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode add_result = Json.newObject();
                                JsonNode data = json.findPath("data");
                                Iterator countriesArrayJson = data.iterator();
                                while (countriesArrayJson.hasNext()) {
                                    JsonNode countryObject = (ObjectNode) countriesArrayJson.next();
                                    CountriesEntity countriesEntity = new CountriesEntity();
                                    countriesEntity.setName(countryObject.findPath("name").asText());
                                    countriesEntity.setCode(countryObject.findPath("alpha2").asText());
                                    countriesEntity.setCode2(countryObject.findPath("alpha3").asText());
                                    countriesEntity.setCreationDate(new Date());
                                    entityManager.persist(countriesEntity);
                                }
                                add_result.put("status", "success");
                                add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
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
    public Result getCountries(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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
                                            String country = json.findPath("country").asText();
                                            String sqlCountries = "select * from countries c where 1=1 ";
                                            if( country!=null && !country.equalsIgnoreCase("")){
                                                sqlCountries+="and c.name="+"'"+country+"'";
                                            }
                                            System.out.println(sqlCountries);
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> countriesList = new ArrayList<HashMap<String, Object>>();
                                            List<CountriesEntity> orgsList
                                                    = (List<CountriesEntity>) entityManager.createNativeQuery(
                                                    sqlCountries, CountriesEntity.class).getResultList();
                                            for (CountriesEntity j : orgsList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("name", j.getName());
                                                sHmpam.put("code", j.getNameEn());
                                                sHmpam.put("order_code", j.getCode());
                                                sHmpam.put("code2", j.getCode2());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                countriesList.add(sHmpam);
                                            }
                                            returnList_future.put("data", countriesList);
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
    public Result getPackgesByOfferId(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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

                                            String offerId = json.findPath("offerId").asText();

                                            String sqlSchedules = "select * from schedule_package_offer spo where offer_id=" + offerId;

                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();

                                            List<SchedulePackageOfferEntity> schedulePackageOfferEntityList = (List<SchedulePackageOfferEntity>) entityManager.createNativeQuery(sqlSchedules, SchedulePackageOfferEntity.class).getResultList();
                                            List<HashMap<String, Object>> schedulePackageOfferfinalList = new ArrayList<HashMap<String, Object>>();
                                            for (SchedulePackageOfferEntity spoff : schedulePackageOfferEntityList) {
                                                //suggestedOffersSchedules
                                                HashMap<String, Object> spoffMap = new HashMap<String, Object>();
                                                spoffMap.put("comments", spoff.getComments());
                                                spoffMap.put("creationDate", spoff.getCreationDate());
                                                spoffMap.put("from", spoff.getFromUnit());
                                                spoffMap.put("to", spoff.getToUnit());
                                                spoffMap.put("unitPrice", spoff.getUnitPrice());
                                                spoffMap.put("offerId", spoff.getOfferId());
                                                spoffMap.put("measureUnitId", spoff.getMeasureUnitId());
                                                MeasurementUnitEntity measurementUnitEntity = entityManager.find(MeasurementUnitEntity.class, spoff.getMeasureUnitId());
                                                spoffMap.put("measureUnit", measurementUnitEntity);
                                                spoffMap.put("measurementUnit", measurementUnitEntity);
                                                spoffMap.put("measurementUnit_title", measurementUnitEntity.getTitle());
                                                schedulePackageOfferfinalList.add(spoffMap);
                                            }
                                            returnList_future.put("data", schedulePackageOfferfinalList);
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
    public Result addOfferBetweenWayPoint(final Http.Request request) throws IOException {
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
                                Long offerScheduleId = json.findPath("offerScheduleId").asLong();
                                String address = json.findPath("address").asText();
                                String city = json.findPath("city").asText();
                                String country = json.findPath("country").asText();
                                String postalCode = json.findPath("postalCode").asText();
                                OfferScheduleBetweenWaypointsEntity betweenWayPoint = new OfferScheduleBetweenWaypointsEntity();
                                betweenWayPoint.setOfferId(offerId);
                                betweenWayPoint.setOfferScheduleId(offerScheduleId);
                                betweenWayPoint.setAddress(address);
                                betweenWayPoint.setCity(city);
                                betweenWayPoint.setCountry(country);
                                betweenWayPoint.setPostalCode(postalCode.replaceAll(" ", ""));
                                entityManager.persist(betweenWayPoint);
                                add_result.put("status", "success");
                                add_result.put("id", betweenWayPoint.getId());
                                add_result.put("message", "Η καταχωρηση πραγματοποίηθηκε με επιτυχία");
                                add_result.put("DO_ID", betweenWayPoint.getId());
                                add_result.put("system", "ΕΝΔΙΑΜΕΣΑ ΣΗΜΕΙΑ ΠΡΟΣΦΟΡΑΣ");
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
    public Result updateOfferBetweenWayPoint(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode update_result = Json.newObject();
                                String user_id = json.findPath("user_id").asText();
                                Long id = json.findPath("id").asLong();
                                String address = json.findPath("address").asText();
                                String city = json.findPath("city").asText();
                                String country = json.findPath("country").asText();
                                String postalCode = json.findPath("postalCode").asText();
                                OfferScheduleBetweenWaypointsEntity betweenWayPoint = entityManager.find(OfferScheduleBetweenWaypointsEntity.class, id);
                                betweenWayPoint.setAddress(address);
                                betweenWayPoint.setCity(city);
                                betweenWayPoint.setCountry(country);
                                betweenWayPoint.setPostalCode(postalCode.replaceAll(" ", ""));
                                entityManager.persist(betweenWayPoint);
                                update_result.put("status", "success");
                                update_result.put("id", betweenWayPoint.getId());
                                update_result.put("message", "Η ενημέρωση πραγματοποίηθηκε με επιτυχία");
                                update_result.put("DO_ID", betweenWayPoint.getId());
                                update_result.put("system", "ΕΝΔΙΑΜΕΣΑ ΣΗΜΕΙΑ ΠΡΟΣΦΟΡΑΣ");
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
    }

    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteOfferBetweenWayPoint(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();
                CompletableFuture<JsonNode> delete_Future = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode delete_result = Json.newObject();
                                String user_id = json.findPath("user_id").asText();
                                Long id = json.findPath("id").asLong();
                                OfferScheduleBetweenWaypointsEntity betweenWayPoint = entityManager.find(OfferScheduleBetweenWaypointsEntity.class, id);
                                entityManager.remove(betweenWayPoint);
                                delete_result.put("status", "success");
                                delete_result.put("id", betweenWayPoint.getId());
                                delete_result.put("message", "Η διαγραφή πραγματοποίηθηκε με επιτυχία");
                                delete_result.put("DO_ID", betweenWayPoint.getId());
                                delete_result.put("system", "ΕΝΔΙΑΜΕΣΑ ΣΗΜΕΙΑ ΠΡΟΣΦΟΡΑΣ");
                                delete_result.put("user_id", user_id);
                                return delete_result;
                            });
                        },
                        executionContext);
                result = (ObjectNode) delete_Future.get();
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
    public Result getOffersSchedulesBetweenWayPoints(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
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
                                            String offerId = json.findPath("offerId").asText();
                                            String offerScheduleId = json.findPath("offerScheduleId").asText();//dromologio prosforas
                                            String sqlWaypoints = "select * from offer_schedule_between_waypoints osbw where 1=1 ";
                                            if (offerId != null && !offerId.equalsIgnoreCase("")) {
                                                sqlWaypoints += " and osbw.offer_id=" + offerId;
                                            }
                                            if (offerScheduleId != null && !offerScheduleId.equalsIgnoreCase("")) {
                                                sqlWaypoints += " and osbw.offer_schedule_id=" + offerScheduleId;
                                            }
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                            List<OfferScheduleBetweenWaypointsEntity> offerBetweenWayPointsList
                                                    = (List<OfferScheduleBetweenWaypointsEntity>) entityManager.createNativeQuery(
                                                    sqlWaypoints, OfferScheduleBetweenWaypointsEntity.class).getResultList();
                                            for (OfferScheduleBetweenWaypointsEntity j : offerBetweenWayPointsList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("address", j.getAddress());
                                                sHmpam.put("city", j.getCity());
                                                sHmpam.put("country", j.getCountry());
                                                sHmpam.put("postalCode", j.getPostalCode().replaceAll(" ", ""));
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("offerId", j.getOfferId());
                                                sHmpam.put("offerScheduleId", j.getOfferScheduleId());
                                                sHmpam.put("creationDate", j.getCreationDate());
                                                sHmpam.put("updateDate", j.getUpdateDate());
                                                serversList.add(sHmpam);
                                            }
                                            returnList_future.put("data", serversList);
                                            returnList_future.put("total", offerBetweenWayPointsList.size());
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


    //------------------------------------------------------------------------------------------------------------------------------------------------------------------//
    @SuppressWarnings("Duplicates")
    protected String getSaltString() {
        String SALTCHARS = "abcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 6) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    @SuppressWarnings("Duplicates")
// Generates a random int with n digits
    public static int generateRandomDigits(int n) {//
        int m = (int) Math.pow(10, n - 1);
        return m + new Random().nextInt(9 * m);
    }

    @SuppressWarnings("Duplicates")
    //encryption password user
    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
    }

    @SuppressWarnings("Duplicates")
    public static String encrypt(String Data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());

        String encryptedValue = Base64.getEncoder().encodeToString(encVal);
        return encryptedValue;
    }

    @SuppressWarnings("Duplicates")
    public static String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = Base64.getDecoder().decode(encryptedData);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }


    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }


}
