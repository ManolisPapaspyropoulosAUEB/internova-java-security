package controllers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mysql.jdbc.Connection;
import com.typesafe.config.ConfigFactory;
import controllers.execution_context.DatabaseExecutionContext;
import controllers.system.Application;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import java.io.*;
import java.sql.DriverManager;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
public class BaseJasperReport  {

    public static ByteArrayInputStream generatePDF(String reportDefFile, ObjectNode reportParams) {
        String reportPath =  ConfigFactory.load().getString("report_path");
        OutputStream stream = new ByteArrayOutputStream();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con= (Connection) DriverManager.getConnection(
                    ConfigFactory.load().getString("db.default.url"),
                    ConfigFactory.load().getString("db.default.user"),
                    ConfigFactory.load().getString("db.default.password")
            );
            File compiledFile = new File(  ConfigFactory.load().getString("jasper_reports")+reportDefFile+".jasper");
            String subDir = "jasper/test".split("/")[0];
            reportParams.put("SUBREPORT_DIR", reportPath + subDir + "/");
            if (!compiledFile.exists()){
                throw new JRRuntimeException("File main_report.jasper not found.");
            }
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(compiledFile);
            ObjectMapper ow = new ObjectMapper();
            Map<String, Object> reportParamsMap = ow.convertValue(reportParams, new TypeReference<Map<String, Object>>(){});
            JasperPrint jrprint = JasperFillManager.fillReport(jasperReport, reportParamsMap,con);
            JRPdfExporter pdfExporter = new JRPdfExporter();
            pdfExporter.setExporterInput(new SimpleExporterInput(jrprint));
            pdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(stream));
            SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
            pdfExporter.setConfiguration(configuration);
            pdfExporter.exportReport();
            con.close();
            ByteArrayInputStream export = new ByteArrayInputStream(((ByteArrayOutputStream) stream).toByteArray());
            return export;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException io) {
                System.out.println("Caught stream close exception");
                io.printStackTrace();
            }
        }
        return new ByteArrayInputStream(((ByteArrayOutputStream) stream).toByteArray());
    }





}
