package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import controllers.execution_context.DatabaseExecutionContext;
import models.DocumentsEntity;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.libs.Files.TemporaryFile;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

public class DocumentsController {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public DocumentsController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }

    /**
     * created by mpapaspyropoulos
     */
    final static String uploadPath = "D:/developm/internova(Pr)/internova_JAVA_security/uploads/";

    @SuppressWarnings("Duplicates")
    public Result uploadFile(final Http.Request request) {
        ObjectNode result = Json.newObject();
        try {
            Http.MultipartFormData<TemporaryFile> body = request.body().asMultipartFormData();
            Http.MultipartFormData.FilePart<TemporaryFile> tempFile = body.getFile("file");
            String userId;
            try {
                userId = body.asFormUrlEncoded().get("userId")[0].trim();
            } catch (NullPointerException e) {
                result.put("status", "error");
                return ok(result);
            }
            Random rand = new Random();
            if (tempFile != null) {
                String fileName = tempFile.getFilename();
                TemporaryFile file = tempFile.getRef();
                String[] fileNameArr = fileName.split("\\.");
                String extension = fileNameArr[1];
                String originalFileName = fileNameArr[0];
                String fileName_random = fileNameArr[0] + "_" + rand.nextInt(1000);
                String fullPath = userId + "/" + fileName;
                File uploadsDir = new File(ConfigFactory.load().getString("uploads_dir") + "/" + userId);
                if (!uploadsDir.exists()) {
                    uploadsDir.mkdirs();
                }
                file.copyTo(Paths.get(uploadPath + userId + "/" + fileName_random + "." + extension), true);
                CompletableFuture<JsonNode> newDbEntryFile = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode result_future = Json.newObject();
                                DocumentsEntity newDoc = new DocumentsEntity();
                                newDoc.setName(fileName);
                                newDoc.setOriginalFilename(originalFileName);
                                newDoc.setExtension(extension);
                                newDoc.setUploadDate(new Date());
                                newDoc.setName(fileName);
                                newDoc.setUserId(Long.valueOf(userId));
                                newDoc.setFullPath(fullPath);
                                entityManager.persist(newDoc);
                                result_future.put("docId", newDoc.getId());
                                result_future.put("status", "ok");
                                result_future.put("message", "Το έγγραφο ανέβηκε με επιτυχία!");
                                return result_future;
                            });
                        },
                        executionContext);
                result = (ObjectNode) newDbEntryFile.get();
                return ok("File uploaded");
            } else {
                return badRequest().flashing("error", "Missing file");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Πρόβλημα κατά το ανέβασμα αρχείου διαγραφή .");
            return ok(result);
        }
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getUploadsByUserId(final Http.Request request) throws IOException {  // san parametro pernei to org key
        ObjectNode result = Json.newObject();
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                System.out.println(json.findPath("userId"));
                if (json.findPath("userId").asText() == null || json.findPath("userId").asText().equalsIgnoreCase("")) {
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
                                            List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                            String userId = json.findPath("userId").asText();
                                            List<DocumentsEntity> orgsList
                                                    = (List<DocumentsEntity>) entityManager.createNativeQuery(
                                                    "select * from documents d where d.user_id=" + userId, DocumentsEntity.class).getResultList();
                                            for (DocumentsEntity j : orgsList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("name", j.getName());
                                                sHmpam.put("userId", j.getUserId());
                                                sHmpam.put("uploadDate", j.getUploadDate());
                                                serversList.add(sHmpam);
                                            }
                                            returnList_future.put("data", serversList);
                                            returnList_future.put("status", "ok");
                                            returnList_future.put("message", "success");
                                            return returnList_future;
                                        });
                            },
                            executionContext);
                    returnList = getFuture.get();
                    DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
    public Result downloadDocument(final Http.Request request) throws IOException {  // san parametro pernei to org key
        ObjectNode result = Json.newObject();
        try {
            JsonNode json = request.body().asJson();
            ObjectMapper ow = new ObjectMapper();
            File returnFile;
            CompletableFuture<File> getFuture = CompletableFuture.supplyAsync(() -> {
                        return jpaApi.withTransaction(
                                entityManager -> {
                                    String id = json.findPath("id").asText();
                                    List<DocumentsEntity> docsList
                                            = (List<DocumentsEntity>) entityManager.createNativeQuery(
                                            "select * from documents d where d.id=" + id, DocumentsEntity.class).getResultList();
                                    File previewFile = new File(uploadPath + docsList.get(0).getFullPath());
                                    return previewFile;
                                });
                    },
                    executionContext);
            returnFile = getFuture.get();
            return ok(returnFile);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Πρόβλημα κατά την ανάγνωση των στοιχείων");
            return ok(result);
        }
    }
}
