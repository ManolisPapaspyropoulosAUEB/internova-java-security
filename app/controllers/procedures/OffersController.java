package controllers.procedures;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execution_context.DatabaseExecutionContext;
import models.*;
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
                                            String sqlCustSupl = "select * from offers offer where 1=1 order by creation_date desc";
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
                                                //
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
                                            String offer_schedule_id = json.findPath("offer_schedule_id").asText();
                                            String start = json.findPath("start").asText();
                                            String limit = json.findPath("limit").asText();
                                            String sql = "select * from schedule_package_offer sp where 1=1 ";
                                            if (!id.equalsIgnoreCase("") && id != null) {
                                                sql += " and sp.offer_schedule_id =" + id + "";
                                            }
                                            if (!measureUnitLabel.equalsIgnoreCase("") && measureUnitLabel != null) {
                                                sql += " and sp.measurement_unit_id in " +
                                                        " (select id from measurement_unit mu where mu.title like '%"+measureUnitLabel+"%' ) ";
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
                                                if(orderCol.equalsIgnoreCase("measureUnitLabel")){
                                                    sql +=  " order by (select title from measurement_unit r where r.id=sp.measurement_unit_id)"+ descAsc;
                                                }else{
                                                    sql += " order by " + orderCol + " " + descAsc;
                                                }
                                            } else {
                                                sql += " order by sp.creation_date desc";
                                            }
                                            if (!start.equalsIgnoreCase("") && start != null) {
                                                sql += " limit " + start + "," + limit;
                                            }

                                            System.out.println(sql);
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();

                                            List<HashMap<String, Object>> schedList = new ArrayList<HashMap<String, Object>>();
                                            List<SchedulePackageOfferEntity> scheduleEntityList
                                                    = (List<SchedulePackageOfferEntity>) entityManager.createNativeQuery(
                                                    sql, SchedulePackageOfferEntity.class).getResultList();
                                            for(SchedulePackageOfferEntity sp : scheduleEntityList){
                                                HashMap<String, Object> spmap = new HashMap<String, Object>();
                                                spmap.put("from", sp.getFromUnit().toString());
                                                spmap.put("to", sp.getToUnit().toString());
                                                spmap.put("offersScheduleId", sp.getOfferScheduleId());
                                                spmap.put("shdulesPackageId", sp.getId());
                                                spmap.put("id", sp.getId());
                                                spmap.put("measurementUnitId", sp.getMeasureUnitId());
                                                MeasurementUnitEntity measurementUnit = entityManager.find(MeasurementUnitEntity.class,sp.getMeasureUnitId());
                                                spmap.put("measurementUnit_id", measurementUnit.getId());
                                                spmap.put("measurementUnit_title", measurementUnit.getTitle());
                                                spmap.put("measurementUnit", measurementUnit);
                                                spmap.put("unitPrice", sp.getUnitPrice().toString());
                                                spmap.put("updateDate", sp.getUpdateDate());
                                                spmap.put("creationDate", sp.getCreationDate());
                                                spmap.put("measureUnitLabel", entityManager.find(MeasurementUnitEntity.class,sp.getMeasureUnitId()).getTitle());
                                                schedList.add(spmap);
                                            }
                                            //searchMeasureSearch
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
                                                sqlCustSupl += " and offer.id =" + id + "";
                                            }
                                            if (!offerId.equalsIgnoreCase("") && offerId != null) {
                                                sqlCustSupl += " and offer.id like '%" + offerId + "%'";
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
                                                if (orderCol.equalsIgnoreCase("billingName")) {
                                                    sqlCustSupl += " order by (select name from billings b where b.id=offer.billing_id)" + descAsc;
                                                } else if (orderCol.equalsIgnoreCase("sellerName")) {
                                                    sqlCustSupl += " order by (select name from internova_sellers iseller where iseller.id=offer.seller_id)" + descAsc;
                                                } else if (orderCol.equalsIgnoreCase("brandName")) {
                                                    sqlCustSupl += " order by (select brand_name from customers_suppliers cs where cs.id=offer.customer_id)" + descAsc;
                                                } else {
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
                                                sHmpam.put("declineReasons", j.getDeclineReasons());
                                                HashMap<String, Object> fromAddress = new HashMap<String, Object>();
                                                fromAddress.put("city", j.getFromCity());
                                                fromAddress.put("address", j.getFromAddress());
                                                fromAddress.put("country", j.getFromCountry());
                                                fromAddress.put("lattitude", j.getFromLattitude());
                                                fromAddress.put("longtitude", j.getFromLongtitude());
                                                fromAddress.put("postalCode", j.getFromPostalCode());
                                                fromAddress.put("region", j.getFromRegion());
                                                sHmpam.put("from", fromAddress);
                                                String sqlOffersSchedules = "select * from offers_schedules os where os.offer_id= "+j.getId();
                                                List<OffersSchedulesEntity> offschelist = (List<OffersSchedulesEntity>) entityManager.createNativeQuery(sqlOffersSchedules,OffersSchedulesEntity.class).getResultList();
                                                List<HashMap<String, Object>> offschelistFinal = new ArrayList<HashMap<String, Object>>();
                                                for(OffersSchedulesEntity osent : offschelist){
                                                    HashMap<String, Object> osentMap = new HashMap<String, Object>();
                                                    osentMap.put("fromAddress", osent.getFromAddress());
                                                    osentMap.put("fromCity", osent.getFromCity());
                                                    osentMap.put("fromCountry", osent.getFromCountry());
                                                    osentMap.put("toAddress", osent.getToAddress());
                                                    osentMap.put("toCity", osent.getToCity());
                                                    osentMap.put("toCountry", osent.getToCountry());
                                                    osentMap.put("offerScheduleId", osent.getId());
                                                    osentMap.put("offerId", osent.getOfferId());
                                                    osentMap.put("id", osent.getId());
                                                    List<HashMap<String, Object>> spoList = new ArrayList<HashMap<String, Object>>();
                                                    String offersUnit = "select * from schedule_package_offer spo where spo.offer_id="+osent.getOfferId() +
                                                            " and spo.offer_schedule_id="+osent.getId();
                                                    List<SchedulePackageOfferEntity> schedulePackage =(List<SchedulePackageOfferEntity>) entityManager.createNativeQuery(offersUnit,SchedulePackageOfferEntity.class).getResultList();
                                                   for(SchedulePackageOfferEntity spo : schedulePackage){
                                                       HashMap<String, Object> spoMap = new HashMap<String, Object>();
                                                       spoMap.put("from", spo.getFromUnit());
                                                       spoMap.put("to", spo.getToUnit());
                                                       spoMap.put("unitPrice", spo.getUnitPrice());
                                                       spoMap.put("scheduleId", osent.getId());//todo:na ginei object id
                                                       spoMap.put("id", spo.getId());
                                                       spoMap.put("comments", spo.getComments());
                                                       spoMap.put("measureUnitId", spo.getMeasureUnitId());
                                                       spoMap.put("measurementUnitId", spo.getMeasureUnitId());
                                                       spoMap.put("measureUnitLabel", entityManager.find(MeasurementUnitEntity.class,spo.getMeasureUnitId()).getTitle());
                                                       spoMap.put("measurementUnit",  entityManager.find(MeasurementUnitEntity.class,spo.getMeasureUnitId()));
                                                       spoList.add(spoMap);
                                                   }
                                                    osentMap.put("schedulePackageList", spoList);
                                                    offschelistFinal.add(osentMap);
                                                }
                                                //tableDataTimokatalogosProsfores
                                                sHmpam.put("tableDataTimokatalogosProsfores", offschelistFinal);

                                                String sqlSchedules = "select * from schedule_package_offer spo where offer_id="+j.getId();
                                                List<SchedulePackageOfferEntity> schedulePackageOfferEntityList = (List<SchedulePackageOfferEntity>) entityManager.createNativeQuery(sqlSchedules,SchedulePackageOfferEntity.class).getResultList();
                                                List<HashMap<String, Object>> schedulePackageOfferfinalList = new ArrayList<HashMap<String, Object>>();
                                                for(SchedulePackageOfferEntity spoff : schedulePackageOfferEntityList){
                                                    //suggestedOffersSchedules
                                                    HashMap<String, Object> spoffMap = new HashMap<String, Object>();
                                                    spoffMap.put("comments", spoff.getComments());
                                                    spoffMap.put("creationDate", spoff.getCreationDate());
                                                    spoffMap.put("from", spoff.getFromUnit());
                                                    spoffMap.put("to", spoff.getToUnit());
                                                    spoffMap.put("unitPrice", spoff.getUnitPrice());
                                                    spoffMap.put("offerId", spoff.getOfferId());
                                                    spoffMap.put("measureUnitId", spoff.getMeasureUnitId());
                                                    MeasurementUnitEntity measurementUnitEntity = entityManager.find(MeasurementUnitEntity.class,spoff.getMeasureUnitId());
                                                    spoffMap.put("measureUnit",measurementUnitEntity);
                                                    spoffMap.put("measurementUnit",measurementUnitEntity);
                                                    spoffMap.put("measurementUnit_title",measurementUnitEntity.getTitle());
                                                    spoffMap.put("measureUnitLabel",measurementUnitEntity.getTitle());
                                                    //
                                                    schedulePackageOfferfinalList.add(spoffMap);
                                                }
                                                sHmpam.put("schedulesPackages", schedulePackageOfferfinalList);
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
                                                    "select * from \n" +
                                                    "( " +
                                                    "select \n " +
                                                    "sched.id, \n " +
                                                    "sched.from_city, \n " +
                                                    "sched.from_country, \n " +
                                                    "sched.to_city, \n " +
                                                    "sched.to_country, \n " +
                                                    "sched.id as object_id, \n " +
                                                    "'Δρομολόγιο' as type , sched.creation_date , \n " +
                                                    " null as from_address, \n " +
                                                    " null as to_address \n " +
                                                    " from internova_db.schedule sched where  sched.id in (select schedule_id from schedule_packages sp ) \n " +
                                                    "union\n " +
                                                    "select \n " +
                                                    "offesched.id, \n " +
                                                    "offesched.from_city, \n " +
                                                    "offesched.from_country, \n " +
                                                    "offesched.to_city, \n " +
                                                    "offesched.to_country, \n " +
                                                    "offesched.id as object_id, \n " +
                                                    "'Προσφορά' as type , offesched.creation_date , \n " +
                                                    " offesched.from_address as from_address, \n " +
                                                    " offesched.to_address as to_address \n " +
                                                    "from offers_schedules offesched\n " +
                                                    "left join offers offe on (offe.id=offesched.offer_id)\n" +
                                                    "where offe.customer_id="+customerId+"\n " +
                                                    "union\n " +
                                                    " select \n" +
                                                    "offesched.id, \n " +
                                                    "offesched.from_city, \n " +
                                                    "offesched.from_country, \n " +
                                                    "offesched.to_city, \n " +
                                                    "offesched.to_country, \n " +
                                                    "offesched.id as object_id, \n " +
                                                    "'Προσφορά απο άλλους' as type , offesched.creation_date , \n " +
                                                    " offesched.from_address as from_address, \n " +
                                                    " offesched.to_address as to_address \n " +
                                                    "from offers_schedules offesched\n " +
                                                    "left join offers offe on (offe.id=offesched.offer_id)\n " +
                                                    "where offe.customer_id!= "+customerId+"   \n " +
                                                    " ) as sugg where 1=1 \n " +
                                                    " \n" +
                                                    " \n" +
                                                    " ";
                                            if(typeCategory.equalsIgnoreCase("1")){
                                                sqlCustSupl += " and sugg.type ='Δρομολόγιο'" ;
                                            }else if (typeCategory.equalsIgnoreCase("2")){
                                                sqlCustSupl += " and sugg.type ='Προσφορά'" ;
                                            }else if (typeCategory.equalsIgnoreCase("3")){
                                                sqlCustSupl += " and sugg.type ='Προσφορά απο άλλους'" ;
                                            }
                                            if (!fromCity.equalsIgnoreCase("") && fromCity != null) {
                                                sqlCustSupl += " and sugg.from_city like '%"+fromCity+"%'" ;
                                            }
                                            if (!fromCountry.equalsIgnoreCase("") && fromCountry != null) {
                                                sqlCustSupl += " and sugg.from_country like '%"+fromCountry+"%'" ;
                                            }
                                            if (!toCity.equalsIgnoreCase("") && toCity != null) {
                                                sqlCustSupl += " and sugg.to_city like '%"+toCity+"%'" ;
                                            }
                                            if (!toCountry.equalsIgnoreCase("") && toCountry != null) {
                                                sqlCustSupl += " and sugg.to_country like '%"+toCountry+"%'" ;
                                            }
                                            if (!type.equalsIgnoreCase("") && type != null) {
                                                sqlCustSupl += " and sugg.type like '%"+type+"%'" ;
                                            }
                                            sqlCustSupl+=" order by sugg.id desc ";
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
                                    JsonNode custommer = json.findPath("custommer2");
                                    ((ObjectNode) json).remove("custommer2");
                                    Long offerId = json.findPath("offerId").asLong();

                                    JsonNode internovaSeller = json.findPath("internovaSeller");
                                    JsonNode billing = json.findPath("billing");
                                    JsonNode from = json.findPath("from");
                                    JsonNode to = json.findPath("to");

                                    JsonNode tableDataTimokatalogosProsfores = json.findPath("tableDataTimokatalogosProsfores");
                                    ((ObjectNode) json).remove("tableDataTimokatalogosProsfores");

//                                    JsonNode schedulePackageList = tableDataTimokatalogosProsfores.findPath("schedulePackageList");


                                    ((ObjectNode) json).remove("internovaSeller");
                                    ((ObjectNode) json).remove("schedulesPackages");
                                    ((ObjectNode) json).remove("billing");
                                    ((ObjectNode) json).remove("from");
                                    ((ObjectNode) json).remove("to");
                                    ((ObjectNode) custommer).remove("billing");
                                    ((ObjectNode) custommer).remove("internovaSeller");
                                    ObjectNode add_result = Json.newObject();
                                    OffersEntity offersEntity =entityManager.find(OffersEntity.class,offerId);
                                    offersEntity.setAa((long) generateRandomDigits(3));
                                    Date offerDateString = new SimpleDateFormat("yyyy-MM-dd").parse(json.findPath("offerDate").asText());
                                    offersEntity.setOfferDate(offerDateString);
                                    offersEntity.setSellerId(internovaSeller.findPath("sellerId").asLong());
                                    offersEntity.setBillingId(billing.findPath("billingId").asLong());
                                    offersEntity.setComments(json.findPath("offers_comments").asText());
                                    offersEntity.setStatus(json.findPath("status").asText());
                                    offersEntity.setDeclineReasons(json.findPath("declineReasons").asText());
                                    offersEntity.setUpdateDate(new Date());
                                    offersEntity.setCustomerId(custommer.findPath("customerId").asLong());
                                    offersEntity.setFromAddress(from.findPath("address").asText());
                                    offersEntity.setFromCity(from.findPath("city").asText());
                                    offersEntity.setFromCountry(from.findPath("country").asText());
                                    offersEntity.setFromPostalCode(from.findPath("postalCode").asText());
                                    offersEntity.setFromRegion(from.findPath("region").asText());
                                    offersEntity.setFromLattitude(from.findPath("lattitude").asDouble());
                                    offersEntity.setFromLongtitude(from.findPath("longtitude").asDouble());
                                    offersEntity.setToAddress(to.findPath("address").asText());
                                    offersEntity.setToCity(from.findPath("city").asText());
                                    offersEntity.setToCountry(to.findPath("country").asText());
                                    offersEntity.setToPostalCode(to.findPath("postalCode").asText());
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
                                        entityManager.remove(ofsElement);
                                    }




                                    Iterator itOffersSCHEDULDE = tableDataTimokatalogosProsfores.iterator();
                                    while (itOffersSCHEDULDE.hasNext()) {
                                        JsonNode offerScheduleNode = (JsonNode) itOffersSCHEDULDE.next();
                                        OffersSchedulesEntity offersSchedulesEntity = new OffersSchedulesEntity();
                                        offersSchedulesEntity.setUpdateDate(new Date());
                                        offersSchedulesEntity.setFromAddress(offerScheduleNode.findPath("departure").findPath("fromAddress").asText());
                                        offersSchedulesEntity.setFromCity(offerScheduleNode.findPath("departure").findPath("fromCity").asText());
                                        offersSchedulesEntity.setFromCountry(offerScheduleNode.findPath("departure").findPath("fromCountry").asText());
                                        offersSchedulesEntity.setToAddress(offerScheduleNode.findPath("arrival").findPath("toAddress").asText());
                                        offersSchedulesEntity.setToCity(offerScheduleNode.findPath("arrival").findPath("toCity").asText());
                                        offersSchedulesEntity.setToCountry(offerScheduleNode.findPath("arrival").findPath("toCountry").asText());
                                        offersSchedulesEntity.setOfferId(offersEntity.getId());
                                        entityManager.persist(offersSchedulesEntity);
                                        Iterator itChilds = offerScheduleNode.findPath("schedulePackageList").iterator();
                                        while (itChilds.hasNext()) {
                                            JsonNode schedulePackageOfferNode = (JsonNode) itChilds.next();
                                            SchedulePackageOfferEntity schedulePackageOfferEntity = new SchedulePackageOfferEntity();
                                            schedulePackageOfferEntity.setOfferId(offersEntity.getId());
                                            schedulePackageOfferEntity.setComments(schedulePackageOfferNode.findPath("comments").asText());
                                            schedulePackageOfferEntity.setCreationDate(new Date());
                                            schedulePackageOfferEntity.setOfferScheduleId(offersSchedulesEntity.getId()); //TODO
                                            schedulePackageOfferEntity.setFromUnit(schedulePackageOfferNode.findPath("from").asInt());
                                            schedulePackageOfferEntity.setToUnit(schedulePackageOfferNode.findPath("to").asInt());
                                            schedulePackageOfferEntity.setUnitPrice(schedulePackageOfferNode.findPath("unitPrice").asDouble());
                                            String sqlUnit = "select * from measurement_unit mu where mu.title='"+schedulePackageOfferNode.findPath("measureUnitLabel").asText()+"'";
                                            List<MeasurementUnitEntity> muList = ( List<MeasurementUnitEntity>) entityManager.createNativeQuery(sqlUnit,MeasurementUnitEntity.class).getResultList();
                                            schedulePackageOfferEntity.setMeasureUnitId(muList.get(0).getId());
                                            entityManager.persist(schedulePackageOfferEntity);
                                        }
                                    }

                                    add_result.put("status", "success");
                                    add_result.put("offerId", offersEntity.getId());
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
                                    OffersEntity offersEntity = new OffersEntity();
                                    offersEntity.setAa((long) generateRandomDigits(3));
                                    Date offerDateString = new SimpleDateFormat("yyyy-MM-dd").parse(json.findPath("offerDate").asText());
                                    offersEntity.setOfferDate(offerDateString);
                                    offersEntity.setSellerId(internovaSeller.findPath("sellerId").asLong());
                                    offersEntity.setBillingId(billing.findPath("billingId").asLong());
                                    offersEntity.setComments(json.findPath("offers_comments").asText());
                                    offersEntity.setStatus(json.findPath("status").asText());
                                    offersEntity.setDeclineReasons(json.findPath("declineReasons").asText());
                                    offersEntity.setCreationDate(new Date());
                                    offersEntity.setCustomerId(custommer.findPath("id").asLong());
                                    offersEntity.setFromAddress(from.findPath("address").asText());
                                    offersEntity.setFromCity(from.findPath("city").asText());
                                    offersEntity.setFromCountry(from.findPath("country").asText());
                                    offersEntity.setFromPostalCode(from.findPath("postalCode").asText());
                                    offersEntity.setFromRegion(from.findPath("region").asText());
                                    offersEntity.setFromLattitude(from.findPath("lattitude").asDouble());
                                    offersEntity.setFromLongtitude(from.findPath("longtitude").asDouble());
                                    offersEntity.setToAddress(to.findPath("address").asText());
                                    offersEntity.setToCity(to.findPath("city").asText());
                                    offersEntity.setToCountry(to.findPath("country").asText());
                                    offersEntity.setToPostalCode(to.findPath("postalCode").asText());
                                    offersEntity.setToRegion(to.findPath("region").asText());
                                    offersEntity.setToLattitude(to.findPath("lattitude").asDouble());
                                    offersEntity.setToLongtitude(to.findPath("longtitude").asDouble());
                                    entityManager.persist(offersEntity);
                                    Iterator itOffersSCHEDULDE = tableDataTimokatalogosProsfores.iterator();
                                    while (itOffersSCHEDULDE.hasNext()) {
                                        JsonNode offerScheduleNode = (JsonNode) itOffersSCHEDULDE.next();
                                        OffersSchedulesEntity offersSchedulesEntity = new OffersSchedulesEntity();
                                        offersSchedulesEntity.setCreationDate(new Date());
                                        offersSchedulesEntity.setFromAddress(offerScheduleNode.findPath("departure").findPath("fromAddress").asText());
                                        offersSchedulesEntity.setFromCity(offerScheduleNode.findPath("departure").findPath("fromCity").asText());
                                        offersSchedulesEntity.setFromCountry(offerScheduleNode.findPath("departure").findPath("fromCountry").asText());
                                        offersSchedulesEntity.setToAddress(offerScheduleNode.findPath("arrival").findPath("toAddress").asText());
                                        offersSchedulesEntity.setToCity(offerScheduleNode.findPath("arrival").findPath("toCity").asText());
                                        offersSchedulesEntity.setToCountry(offerScheduleNode.findPath("arrival").findPath("toCountry").asText());
                                        offersSchedulesEntity.setOfferId(offersEntity.getId());
                                        entityManager.persist(offersSchedulesEntity);
                                        String sqlSchedules = "select * from schedule s where s.from_address='"+
                                                offerScheduleNode.findPath("departure").findPath("fromAddress").asText()+"' and s.to_address='"+
                                                offerScheduleNode.findPath("arrival").findPath("toAddress").asText()+"' and s.from_city='"+offerScheduleNode.findPath("departure").findPath("fromCity").asText()+"'"+
                                                " and s.to_city='"+offerScheduleNode.findPath("arrival").findPath("toCity").asText() +"' and s.from_country='"+offerScheduleNode.findPath("departure").findPath("fromCountry").asText()+"'"+
                                                " and s.to_country='"+offerScheduleNode.findPath("arrival").findPath("toCountry").asText()+"'";
                                        List<ScheduleEntity> scheduleList = ( List<ScheduleEntity>) entityManager.createNativeQuery(sqlSchedules,ScheduleEntity.class).getResultList();
                                        ScheduleEntity scheduleEntity = new ScheduleEntity();
                                        if(scheduleList.size()==0){
                                            scheduleEntity.setCreationDate(new Date());
                                            scheduleEntity.setFromAddress(offerScheduleNode.findPath("departure").findPath("fromAddress").asText());
                                            scheduleEntity.setFromCity(offerScheduleNode.findPath("departure").findPath("fromCity").asText());
                                            scheduleEntity.setFromCountry(offerScheduleNode.findPath("departure").findPath("fromCountry").asText());
                                            scheduleEntity.setToAddress(offerScheduleNode.findPath("arrival").findPath("toAddress").asText());
                                            scheduleEntity.setToCity(offerScheduleNode.findPath("arrival").findPath("toCity").asText());
                                            scheduleEntity.setToCountry(offerScheduleNode.findPath("arrival").findPath("toCountry").asText());
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
                                            schedulePackageOfferEntity.setFromUnit(schedulePackageOfferNode.findPath("from").asInt());
                                            schedulePackageOfferEntity.setToUnit(schedulePackageOfferNode.findPath("to").asInt());
                                            schedulePackageOfferEntity.setUnitPrice(schedulePackageOfferNode.findPath("unitPrice").asDouble());
                                            String sqlUnit = "select * from measurement_unit mu where mu.title='"+schedulePackageOfferNode.findPath("measureUnitLabel").asText()+"'";
                                            List<MeasurementUnitEntity> muList = ( List<MeasurementUnitEntity>) entityManager.createNativeQuery(sqlUnit,MeasurementUnitEntity.class).getResultList();
                                            schedulePackageOfferEntity.setMeasureUnitId(muList.get(0).getId());
                                            entityManager.persist(schedulePackageOfferEntity);
                                            if(scheduleList.size()==0){
                                                SchedulePackagesEntity schedulePackagesEntity=new SchedulePackagesEntity();
                                                schedulePackagesEntity.setScheduleId(scheduleEntity.getId());
                                                schedulePackagesEntity.setFromUnit(schedulePackageOfferNode.findPath("from").asInt());
                                                schedulePackagesEntity.setToUnit(schedulePackageOfferNode.findPath("to").asInt());
                                                schedulePackagesEntity.setMeasurementUnitId(muList.get(0).getId());
                                                schedulePackagesEntity.setCreationDate(new Date());
                                                schedulePackagesEntity.setUnitPrice(schedulePackageOfferNode.findPath("unitPrice").asDouble());
                                                entityManager.persist(schedulePackagesEntity);
                                            }
                                        }
                                    }
                                    add_result.put("status", "success");
                                    add_result.put("offerId", offersEntity.getId());
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
    public Result getPackgesByOfferId(final Http.Request request) throws IOException {
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

                                            String offerId = json.findPath("offerId").asText();

                                            String sqlSchedules = "select * from schedule_package_offer spo where offer_id="+offerId;

                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();

                                            List<SchedulePackageOfferEntity> schedulePackageOfferEntityList = (List<SchedulePackageOfferEntity>) entityManager.createNativeQuery(sqlSchedules,SchedulePackageOfferEntity.class).getResultList();
                                            List<HashMap<String, Object>> schedulePackageOfferfinalList = new ArrayList<HashMap<String, Object>>();
                                            for(SchedulePackageOfferEntity spoff : schedulePackageOfferEntityList){
                                                //suggestedOffersSchedules
                                                HashMap<String, Object> spoffMap = new HashMap<String, Object>();
                                                spoffMap.put("comments", spoff.getComments());
                                                spoffMap.put("creationDate", spoff.getCreationDate());
                                                spoffMap.put("from", spoff.getFromUnit());
                                                spoffMap.put("to", spoff.getToUnit());
                                                spoffMap.put("unitPrice", spoff.getUnitPrice());
                                                spoffMap.put("offerId", spoff.getOfferId());
                                                spoffMap.put("measureUnitId", spoff.getMeasureUnitId());
                                                MeasurementUnitEntity measurementUnitEntity = entityManager.find(MeasurementUnitEntity.class,spoff.getMeasureUnitId());
                                                spoffMap.put("measureUnit",measurementUnitEntity);
                                                spoffMap.put("measurementUnit",measurementUnitEntity);
                                                spoffMap.put("measurementUnit_title",measurementUnitEntity.getTitle());
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
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Πρόβλημα κατά την ανάγνωση των στοιχείων");
            return ok(result);
        }
    }





}
