package controllers.security;

import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.IOResult;
import akka.stream.alpakka.ftp.FtpCredentials;
import akka.stream.alpakka.ftp.FtpFile;
import akka.stream.alpakka.ftp.FtpSettings;
import akka.stream.alpakka.ftp.javadsl.Ftp;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import controllers.execution_context.DatabaseExecutionContext;
import models.AuditLogsEntity;
import models.UsersEntity;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.*;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

public class AuditLogsController {
    private JPAApi jpaApi;
    private DatabaseExecutionContext executionContext;

    @Inject
    public AuditLogsController(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result addAuditLog(final Http.Request request) throws IOException {
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
                                    String system = json.findPath("system").asText();
                                    Long userId = json.findPath("userId").asLong();
                                    Long objectId = json.findPath("objectId").asLong();
                                    String message = json.findPath("message").asText();
                                    AuditLogsEntity log = new AuditLogsEntity();
                                    log.setSystem(system);
                                    log.setUserId(userId);
                                    log.setObjectId(objectId);
                                    log.setMessage(message);
                                    entityManager.persist(log);
                                    add_result.put("status", "success");
                                    add_result.put("message", "Επιτυχης καταχωρηση");
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


//


    public Result ftpRetrieveFromPath(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            //JsonNode json = request.body().asJson();
            System.out.println("uss " + json.findPath("user_id").asText());
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    JSch jsch = new JSch();
                    try {
                        String user = "138181";
                        String host = "212.185.120.138";
                        int port = 22;
                        String pass = "Um7vZ9q1";
                        Session session = jsch.getSession(user, host, port);
                        session.setConfig("StrictHostKeyChecking", "no");
                        session.setPassword(pass);
                        session.connect();
                        System.out.println("Connection established.");
                        System.out.println("Creating SFTP Channel.");
                        Channel channel = session.openChannel("sftp");
                        channel.connect();
                        ChannelSftp sftpChannel = (ChannelSftp) channel;
                        String strCurrentLine = null;
                        BufferedReader objReader = null;
                        InputStream stream = sftpChannel.get("/Oeffentlicher_Bereich/Bestandsabfrage/Frankana/frankana_lager.txt");
                        try {
                       //     FileUtils.copyInputStreamToFile(stream, new File("ee2.txt"));
                            String cont = FileUtils.readFileToString(new File("ee2.txt"), Charset.forName("UTF-8"));
                            String cont2 = FileUtils.readFileToString(new File("ee2.txt"), Charset.forName("UTF-16LE"));
                            System.out.println(cont);
                            System.out.println(cont2);

                            //       String cont = FileUtils.readFileToString(new File("testak.txt"), Charset.forName("UTF-16LE"));
                            //     System.out.println(cont);

                        } finally {
                            stream.close();
                        }
                    } catch (Exception ex) {
                        System.out.println("Oops! Something wrong happened");
                        ex.printStackTrace();
                    }
                    return ok(result);
                } catch (Exception e) {
                    ObjectNode result = Json.newObject();
                    e.printStackTrace();
                    result.put("status", "error");
                    result.put("message", "Προβλημα κατα την σύνδεση ftp frankana");
                    return ok(result);
                }
            }
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την σύνδεση ftp frankana");
            return ok(result);
        }
    }



    public Result ftpRetrieveFromFrankana(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            //JsonNode json = request.body().asJson();
            System.out.println("uss "+json.findPath("user_id").asText());
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();


                        // FileUtils.copyInputStreamToFile(stream, targetFile);

                        try {



                          //  BufferedReader br =new BufferedReader(new FileReader("ee2.txt"));
                            BufferedReader reader = Files.newBufferedReader(Paths.get("ee2.txt"), StandardCharsets.UTF_16LE);

                            // FileUtils.readFileToString(new File("ee2.txt"), Charset.forName("UTF-16LE"));
                            String lineF = reader.readLine();
                            while (lineF != null) {
                                System.out.println(lineF);

                                System.out.println(lineF);
                                ByteBuffer byteBuffer = StandardCharsets.UTF_16LE.encode(lineF);
                                String line = byteBuffer.toString();
                                System.out.println(line);
                                String[] splitLine = line.split("@@@");
                                System.out.println(splitLine[0]);
                                String[] first = splitLine[0].split("@");
                                //System.out.println(splitLine[0]);
                                // System.out.println("\n Code "+first[0]);

                                String supplierSku = "";
                                String qtyFrankana = "";
                                String qtyFrankanaLabel = "";
                                String frankanaApothiki2 = "";
                                String frankanaApothiki3 = "";
                                String frankanaApothiki5 = "";
                                String typeProduct = "frankana";
                                if (first[0].contains("/")) {// Frankana products

                                    String splitLineStr = splitLine[1];
                                    if (splitLine[1].substring(0, 1).equals("@")) {
                                        splitLineStr = splitLineStr.replaceFirst("@", "");
                                    }

                                    String[] second = splitLineStr.split("@");
                                    supplierSku = first[0];
                                    System.out.println("supplierSku " + supplierSku);
                                }

                                lineF = reader.readLine();
                            }


                    } catch (Exception ex) {
                        System.out.println("Oops! Something wrong happened");
                        ex.printStackTrace();
                    }
                    return ok(result);
                } catch (Exception e) {
                    ObjectNode result = Json.newObject();
                    e.printStackTrace();
                    result.put("status", "error");
                    result.put("message", "Προβλημα κατα την σύνδεση ftp frankana");
                    return ok(result);
                }
            }
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Προβλημα κατα την σύνδεση ftp frankana");
            return ok(result);
        }
    }


    String decodeText(String input, String encoding) throws IOException {
        return
                new BufferedReader(
                        new InputStreamReader(
                                new ByteArrayInputStream(input.getBytes()),
                                Charset.forName(encoding)))
                        .readLine();
    }

    public static void transform(File source, String srcEncoding, File target, String tgtEncoding) throws IOException {
        try (
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(source), srcEncoding));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target), tgtEncoding));) {
            char[] buffer = new char[1024];
            int read;
            while ((read = br.read(buffer)) != -1) {
                System.out.println(read);
                bw.write(buffer, 0, read);
            }


        }
    }


//    @SuppressWarnings({"Duplicates", "unchecked"})
//    @BodyParser.Of(BodyParser.Json.class)
//    public Result ftpRetrieveFromPath(final Http.Request request) throws IOException {
//        try {
//            JsonNode json = request.body().asJson();
//            if (json == null) {
//                return badRequest("Expecting Json data");
//            } else {
//                try {
//                    ObjectNode result = Json.newObject();
//                    FTPClient fClient = new FTPClient();
//
//
//
//
//                    try {
//                        fClient.connect("212.185.120.138", 22);
//                     //   System.out.println(replyCode);
//                        //int replyCode = fClient.getReplyCode();
//                     //   System.out.println(replyCode);
//                        boolean success = fClient.login("138181", "Um7vZ9q1");
//                        if (!success) {
//                            System.out.println("Could not login to the server");
//                        } else {
//                            System.out.println("You are now logged on!");
//                        }
//                        fClient.enterLocalPassiveMode();
//                        fClient.setFileType(FTP.BINARY_FILE_TYPE);
//                        File localFile = new File("/Oeffentlicher_Bereich/Bestandsabfrage/Frankana/frankana_lager.txt");
//                        String remoteFile = "shared.txt";
//                        InputStream inputStream = new FileInputStream(localFile);
//                        System.out.println("Start uploading the file");
//                        boolean done = fClient.storeFile(remoteFile, inputStream);
//                        inputStream.close();
//                        if (done) {
//                            System.out.println(remoteFile + " has been uploaded successfully");
//                        }
//                    } catch (IOException ex) {
//                        System.out.println("Oops! Something wrong happened");
//                        ex.printStackTrace();
//                    } finally {
//                        try {
//                            if (fClient.isConnected()) {
//                                fClient.logout();
//                                fClient.disconnect();
//                                System.out.println("FTP Disconnected");
//                            }
//                        } catch (IOException ex) {
//                            ex.printStackTrace();
//                        }
//                    }
//                    return ok(result);
//                } catch (Exception e) {
//                    ObjectNode result = Json.newObject();
//                    e.printStackTrace();
//                    result.put("status", "error");
//                    result.put("message", "Προβλημα κατα την καταχωρηση");
//                    return ok(result);
//                }
//            }
//        } catch (Exception e) {
//            ObjectNode result = Json.newObject();
//            e.printStackTrace();
//            result.put("status", "error");
//            result.put("message", "Προβλημα κατα την καταχωρηση");
//            return ok(result);
//        }
//    }


    public Source<ByteString, CompletionStage<IOResult>> retrieveFromPath(String path) throws Exception {
        FtpSettings ftpSettings =
                FtpSettings.create(InetAddress.getByName("212.185.120.138"))
                        .withPort(22)
                        .withCredentials(FtpCredentials.create("138181", "Um7vZ9q1"))
                        .withBinary(true)
                        .withPassiveMode(true)
                        .withConfigureConnectionConsumer((FTPClient ftpClient) -> {
                            ftpClient.addProtocolCommandListener(
                                    new PrintCommandListener(new PrintWriter(System.out), true));
                        });
        return Ftp.fromPath(path, ftpSettings);
    }


//
//    @SuppressWarnings({"Duplicates","unchecked"})
//    @BodyParser.Of(BodyParser.Json.class)
//    public Result ftpRetrieveFromPathAkka(final Http.Request request) throws IOException {
//        try {
//            JsonNode json = request.body().asJson();
//            if (json == null) {
//                return badRequest("Expecting Json data");
//            } else {
//                try {
//                    ObjectNode result = Json.newObject();
//                    CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
//                                return jpaApi.withTransaction(entityManager -> {
//                                    ObjectNode add_result = Json.newObject();
//
//
//                                    try {
//                                        Source remoteFile = retrieveFromPath("/Oeffentlicher_Bereich/Bestandsabfrage/Frankana/frankana_lager.txt");
//                                        Source pr =       processAndMove("/Oeffentlicher_Bereich/Bestandsabfrage/Frankana/frankana_lager.txt");
//                                        System.out.println(pr);
//
//
//
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//
//
////                                    String server = "212.185.120.138";
////                                    int port = 22;
////                                    String user = "138181";
////                                    String pass = "Um7vZ9q1";
////                                    FTPClient ftpClient = new FTPClient();
////                                    try {
////                                       // Source remoteFile = retrieveFromPath("/Oeffentlicher_Bereich/Bestandsabfrage/Frankana/frankana_lager.txt");
////                                       ftpClient.connect(server, port);
////                                        boolean login=  ftpClient.login(user, pass);
////                                        ftpClient.enterLocalPassiveMode();
////                                        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
////
////                                        System.out.println(login);
////
////
////                                    } catch (Exception e) {
////                                        e.printStackTrace();
////                                    }
//
//
//                                    add_result.put("status", "success");
//                                    add_result.put("message", "Επιτυχης καταχωρηση");
//                                    return add_result;
//                                });
//                            },
//                            executionContext);
//
//
//                    result = (ObjectNode) addFuture.get();
//                    return ok(result);
//
//                } catch (Exception e) {
//                    ObjectNode result = Json.newObject();
//                    e.printStackTrace();
//                    result.put("status", "error");
//                    result.put("message", "Προβλημα κατα την καταχωρηση");
//                    return ok(result);
//                }
//            }
//        } catch (Exception e) {
//            ObjectNode result = Json.newObject();
//            e.printStackTrace();
//            result.put("status", "error");
//            result.put("message", "Προβλημα κατα την καταχωρηση");
//            return ok(result);
//        }
//    }
//


    @SuppressWarnings({"Duplicates", "unchecked"})
    @BodyParser.Of(BodyParser.Json.class)
    public Result updateAuditLog(final Http.Request request) throws IOException {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest("Expecting Json data");
            } else {
                try {
                    ObjectNode result = Json.newObject();
                    CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {
                                return jpaApi.withTransaction(entityManager -> {
                                    ObjectNode result_update = Json.newObject();
                                    String system = json.findPath("system").asText();
                                    Long id = json.findPath("system").asLong();
                                    Long userId = json.findPath("userId").asLong();
                                    Long objectId = json.findPath("objectId").asLong();
                                    String message = json.findPath("message").asText();
                                    AuditLogsEntity log = entityManager.find(AuditLogsEntity.class, id);
                                    log.setSystem(system);
                                    log.setUserId(userId);
                                    log.setObjectId(objectId);
                                    log.setMessage(message);
                                    entityManager.merge(log);
                                    result_update.put("status", "success");
                                    result_update.put("message", "Επιτυχης καταχωρηση");
                                    return result_update;
                                });
                            },
                            executionContext);


                    result = (ObjectNode) updateFuture.get();
                    return ok(result);

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
    public Result deleteLog(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Expecting Json data");
        } else {
            try {
                ObjectNode result = Json.newObject();

                CompletableFuture<JsonNode> deleteFuture = CompletableFuture.supplyAsync(() -> {
                            return jpaApi.withTransaction(entityManager -> {
                                ObjectNode result_delete = Json.newObject();
                                Long id = json.findPath("id").asLong();
                                AuditLogsEntity log = entityManager.find(AuditLogsEntity.class, id);
                                if (log != null) {
                                    entityManager.remove(log);
                                    result_delete.put("status", "success");
                                    result_delete.put("message", "Επιτυχης διαγραφή");
                                } else {
                                    result_delete.put("status", "error");
                                    result_delete.put("message", "Δεν βρέθηκε σχετικός οργανισμός");
                                }
                                return result_delete;
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

    }

    @SuppressWarnings({"Duplicates", "unchecked"})
    public Result getAuditLogs(final Http.Request request) throws IOException, ExecutionException, InterruptedException {
        ObjectNode result = Json.newObject();
        JsonNode json = request.body().asJson();
        try {
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
                                            String system = json.findPath("system").asText();
                                            Long objectId = json.findPath("objectId").asLong();

                                            String query = "select * from audit_logs logs where 1=1 ";
                                            if (objectId != null && objectId != 0) {
                                                query += " and logs.object_id=" + objectId;
                                            }
                                            if (system != null && !system.equalsIgnoreCase("") && !system.equalsIgnoreCase("null")) {
                                                query += " and logs.system='" + system + "'";
                                            }
                                            System.out.println(query);
                                            HashMap<String, Object> returnList_future = new HashMap<String, Object>();
                                            List<HashMap<String, Object>> serversList = new ArrayList<HashMap<String, Object>>();
                                            List<AuditLogsEntity> orgsList
                                                    = (List<AuditLogsEntity>) entityManager.createNativeQuery(
                                                    query, AuditLogsEntity.class).getResultList();
                                            for (AuditLogsEntity j : orgsList) {
                                                HashMap<String, Object> sHmpam = new HashMap<String, Object>();
                                                sHmpam.put("id", j.getId());
                                                sHmpam.put("system", j.getSystem());
                                                sHmpam.put("userId", j.getUserId());
                                                if (j.getUserId() != 0) {
                                                    UsersEntity user = entityManager.find(UsersEntity.class, j.getUserId());
                                                    sHmpam.put("userFullName", user.getFirstname() + " " + user.getLastname());
                                                }
                                                sHmpam.put("objectId", j.getObjectId());
                                                sHmpam.put("creationDate", j.getCreationDate());


                                                if (j.getMethod().substring(0, 12).equalsIgnoreCase("POST /update")) {
                                                    sHmpam.put("method", "επεξεργασία");
                                                } else if (j.getMethod().substring(0, 9).equalsIgnoreCase("POST /add")) {
                                                    sHmpam.put("method", "προσθήκη");
                                                } else if (j.getMethod().substring(0, 12).equalsIgnoreCase("POST /create")) {
                                                    sHmpam.put("method", "προσθήκη");
                                                }


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
}
