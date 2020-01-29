/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.selv.report.service;

import static java.io.File.createTempFile;
import static net.sf.jasperreports.engine.export.JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.openlmis.selv.report.i18n.JasperMessageKeys.ERROR_JASPER_FILE_CREATION;
import static org.openlmis.selv.report.i18n.MessageKeys.REQUISITION_ERROR_IO;
import static org.openlmis.selv.report.i18n.MessageKeys.REQUISITION_ERROR_JASPER_FILE_FORMAT;
import static org.openlmis.selv.report.i18n.ReportingMessageKeys.ERROR_REPORTING_CLASS_NOT_FOUND;
import static org.openlmis.selv.report.i18n.ReportingMessageKeys.ERROR_REPORTING_IO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.openlmis.selv.report.domain.JasperTemplate;
import org.openlmis.selv.report.dto.external.requisition.RequisitionDto;
import org.openlmis.selv.report.dto.external.requisition.RequisitionStatusDto;
import org.openlmis.selv.report.dto.external.requisition.RequisitionTemplateColumnDto;
import org.openlmis.selv.report.dto.external.requisition.RequisitionTemplateDto;
import org.openlmis.selv.report.dto.requisition.RequisitionReportDto;
import org.openlmis.selv.report.exception.JasperReportViewException;
import org.openlmis.selv.report.utils.ReportUtils;
import org.openlmis.selv.report.web.requisition.RequisitionReportDtoBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsCsvView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsPdfView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsXlsView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsXlsxView;

@Service
public class JasperReportsViewService {

  private static final String REQUISITION_LINE_REPORT_DIR = "/reports/requisitionLines.jrxml";
  private static final String REQUISITION_REPORT_DIR = "/reports/requisition.jrxml";

  private static final String DATASOURCE = "datasource";
  private static final String DATE_FORMAT = "dateFormat";
  private static final String DECIMAL_FORMAT = "decimalFormat";

  @Value("${dateFormat}")
  private String dateFormat;

  @Value("${groupingSeparator}")
  private String groupingSeparator;

  @Value("${groupingSize}")
  private String groupingSize;

  @Value("${defaultLocale}")
  private String defaultLocale;

  @Value("${currencyLocale}")
  private String currencyLocale;

  @Autowired
  private DataSource replicationDataSource;

  @Autowired
  private RequisitionReportDtoBuilder requisitionReportDtoBuilder;

  /**
   * Create Jasper Report View.
   * Create Jasper Report (".jasper" file) from bytes from Template entity.
   * Set 'Jasper' exporter parameters, JDBC data source, web application context, url to file.
   *
   * @param jasperTemplate template that will be used to create a view
   * @param request  it is used to take web application context
   * @return created jasper view.
   * @throws JasperReportViewException if there will be any problem with creating the view.
   */
  public JasperReportsMultiFormatView getJasperReportsView(
      JasperTemplate jasperTemplate, HttpServletRequest request) throws JasperReportViewException {
    JasperReportsMultiFormatView jasperView = new JasperReportsMultiFormatView();

    setFormatMappings(jasperView);

    jasperView.setUrl(getReportUrlForReportData(jasperTemplate));
    jasperView.setJdbcDataSource(replicationDataSource);

    if (getApplicationContext(request) != null) {
      jasperView.setApplicationContext(getApplicationContext(request));
    }

    return jasperView;
  }

  /**
   * Get application context from servlet.
   */
  public WebApplicationContext getApplicationContext(HttpServletRequest servletRequest) {
    ServletContext servletContext = servletRequest.getSession().getServletContext();
    return WebApplicationContextUtils.getWebApplicationContext(servletContext);
  }

  /**
   * Create custom Jasper Report View for printing a requisition.
   *
   * @param requisition requisition to render report for.
   * @param request  it is used to take web application context.
   * @return created jasper view.
   * @throws JasperReportViewException if there will be any problem with creating the view.
   */
  public ModelAndView getRequisitionJasperReportView(
      RequisitionDto requisition, HttpServletRequest request) throws JasperReportViewException {
    RequisitionReportDto reportDto = requisitionReportDtoBuilder.build(requisition);
    RequisitionTemplateDto template = requisition.getTemplate();

    Map<String, Object> params = ReportUtils.createParametersMap();
    params.put("subreport", createCustomizedRequisitionLineSubreport(
        template, requisition.getStatus()));
    params.put(DATASOURCE, Collections.singletonList(reportDto));
    params.put("template", template);
    params.put(DATE_FORMAT, dateFormat);
    params.put(DECIMAL_FORMAT, createDecimalFormat());
    params.put("currencyDecimalFormat",
        NumberFormat.getCurrencyInstance(getLocaleFromService()));

    JasperReportsMultiFormatView jasperView = new JasperReportsMultiFormatView();
    setExportParams(jasperView);
    setCustomizedJasperTemplateForRequisitionReport(jasperView);

    if (getApplicationContext(request) != null) {
      jasperView.setApplicationContext(getApplicationContext(request));
    }
    return new ModelAndView(jasperView, params);
  }

  private JasperDesign createCustomizedRequisitionLineSubreport(RequisitionTemplateDto template,
      RequisitionStatusDto requisitionStatus)
      throws JasperReportViewException {
    try (InputStream inputStream = getClass().getResourceAsStream(REQUISITION_LINE_REPORT_DIR)) {
      JasperDesign design = JRXmlLoader.load(inputStream);
      JRBand detail = design.getDetailSection().getBands()[0];
      JRBand header = design.getColumnHeader();

      Map<String, RequisitionTemplateColumnDto> columns =
          ReportUtils.getSortedTemplateColumnsForPrint(template.getColumnsMap(), requisitionStatus);

      ReportUtils.customizeBandWithTemplateFields(detail, columns, design.getPageWidth(), 9);
      ReportUtils.customizeBandWithTemplateFields(header, columns, design.getPageWidth(), 9);

      return design;
    } catch (IOException err) {
      throw new JasperReportViewException(err, REQUISITION_ERROR_IO, err.getMessage());
    } catch (JRException err) {
      throw new JasperReportViewException(
          err, REQUISITION_ERROR_JASPER_FILE_FORMAT, err.getMessage());
    }
  }

  private void setFormatMappings(JasperReportsMultiFormatView jasperView) {
    Map<String, Class<? extends AbstractJasperReportsView>> formatMappings = new HashMap<>();
    formatMappings.put("csv", JasperReportsCsvView.class);
    formatMappings.put("html", JasperReportsHtmlView.class);
    formatMappings.put("pdf", JasperReportsPdfView.class);
    formatMappings.put("xls", JasperReportsXlsView.class);
    formatMappings.put("xlsx", JasperReportsXlsxView.class);
    jasperView.setFormatMappings(formatMappings);
  }

  /**
   * Create ".jasper" file with byte array from Template.
   *
   * @return Url to ".jasper" file.
   */
  private String getReportUrlForReportData(JasperTemplate jasperTemplate)
      throws JasperReportViewException {
    File tmpFile;

    try {
      tmpFile = createTempFile(jasperTemplate.getName() + "_temp", ".jasper");
    } catch (IOException exp) {
      throw new JasperReportViewException(
          exp, ERROR_JASPER_FILE_CREATION
      );
    }

    try (ObjectInputStream inputStream =
             new ObjectInputStream(new ByteArrayInputStream(jasperTemplate.getData()))) {
      JasperReport jasperReport = (JasperReport) inputStream.readObject();

      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
           ObjectOutputStream out = new ObjectOutputStream(bos)) {

        out.writeObject(jasperReport);
        writeByteArrayToFile(tmpFile, bos.toByteArray());

        return tmpFile.toURI().toURL().toString();
      }
    } catch (IOException exp) {
      throw new JasperReportViewException(exp, ERROR_REPORTING_IO, exp.getMessage());
    } catch (ClassNotFoundException exp) {
      throw new JasperReportViewException(
          exp, ERROR_REPORTING_CLASS_NOT_FOUND, JasperReport.class.getName());
    }
  }

  private DecimalFormat createDecimalFormat() {
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    decimalFormatSymbols.setGroupingSeparator(groupingSeparator.charAt(0));
    DecimalFormat decimalFormat = new DecimalFormat("", decimalFormatSymbols);
    decimalFormat.setGroupingSize(Integer.valueOf(groupingSize));
    return decimalFormat;
  }

  protected Locale getLocaleFromService() {
    return new Locale(defaultLocale, currencyLocale);
  }

  /**
   * Set export parameters in jasper view.
   */
  private void setExportParams(JasperReportsMultiFormatView jasperView) {
    Map<JRExporterParameter, Object> reportFormatMap = new HashMap<>();
    reportFormatMap.put(IS_USING_IMAGES_TO_ALIGN, false);
    jasperView.setExporterParameters(reportFormatMap);
  }

  private void setCustomizedJasperTemplateForRequisitionReport(
      JasperReportsMultiFormatView jasperView) throws JasperReportViewException {
    try (InputStream inputStream = getClass().getResourceAsStream(REQUISITION_REPORT_DIR)) {
      File reportTempFile = createTempFile("requisitionReport_temp", ".jasper");
      JasperReport report = JasperCompileManager.compileReport(inputStream);

      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
          ObjectOutputStream out = new ObjectOutputStream(bos)) {

        out.writeObject(report);
        writeByteArrayToFile(reportTempFile, bos.toByteArray());

        jasperView.setUrl(reportTempFile.toURI().toURL().toString());
      }
    } catch (IOException err) {
      throw new JasperReportViewException(err, REQUISITION_ERROR_IO, err.getMessage());
    } catch (JRException err) {
      throw new JasperReportViewException(
          err, REQUISITION_ERROR_JASPER_FILE_FORMAT, err.getMessage());
    }
  }
}
