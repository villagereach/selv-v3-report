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

import static org.openlmis.selv.report.i18n.JasperMessageKeys.ERROR_JASPER_REPORT_FORMAT_UNKNOWN;
import static org.openlmis.selv.report.i18n.JasperMessageKeys.ERROR_JASPER_REPORT_GENERATION;
import static org.openlmis.selv.report.i18n.MessageKeys.REQUISITION_ERROR_IO;
import static org.openlmis.selv.report.i18n.MessageKeys.REQUISITION_ERROR_JASPER_FILE_FORMAT;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.openlmis.selv.report.domain.JasperTemplate;
import org.openlmis.selv.report.dto.external.requisition.RequisitionDto;
import org.openlmis.selv.report.dto.external.requisition.RequisitionStatusDto;
import org.openlmis.selv.report.dto.external.requisition.RequisitionTemplateColumnDto;
import org.openlmis.selv.report.dto.external.requisition.RequisitionTemplateDto;
import org.openlmis.selv.report.dto.requisition.RequisitionReportDto;
import org.openlmis.selv.report.exception.JasperReportViewException;
import org.openlmis.selv.report.utils.JasperReportDeserializer;
import org.openlmis.selv.report.utils.ReportUtils;
import org.openlmis.selv.report.web.requisition.RequisitionReportDtoBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JasperReportsViewService {
  private static final String PARAM_DATASOURCE = "datasource";

  private static final String REQUISITION_LINE_REPORT_DIR = "/reports/requisitionLines.jrxml";
  private static final String REQUISITION_REPORT_DIR = "/reports/requisition.jrxml";

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
  private JasperReportDeserializer reportDeserializer;

  @Autowired
  private RequisitionReportDtoBuilder requisitionReportDtoBuilder;

  /**
   * Create Jasper Report View. Create Jasper Report (".jasper" file) from bytes from Template
   * entity. Set 'Jasper' exporter parameters, JDBC data source, web application context, url to
   * file.
   *
   * @param template template that will be used to create a view (byte[])
   * @param params map of parameters
   * @return created jasper view.
   * @throws JasperReportViewException if there will be any problem with creating the view.
   */
  public byte[] getJasperReportsView(byte[] template, Map<String, Object> params)
      throws JasperReportViewException {

    try {
      JasperReport jasperReport = reportDeserializer.deserialize(template);

      JasperPrint jasperPrint;
      if (params.containsKey(PARAM_DATASOURCE) && params.get(PARAM_DATASOURCE) != null) {
        Object dataSourceParam = params.get(PARAM_DATASOURCE);
        JRDataSource jrDataSource;
        if (dataSourceParam instanceof JRDataSource) {
          jrDataSource = (JRDataSource) dataSourceParam;
        } else if (dataSourceParam instanceof Collection) {
          jrDataSource = new JRBeanCollectionDataSource((Collection<?>) dataSourceParam);
        } else {
          throw new JasperReportViewException(ERROR_JASPER_REPORT_GENERATION);
        }
        jasperPrint = JasperFillManager.fillReport(jasperReport, params, jrDataSource);
      } else {
        try (Connection connection = replicationDataSource.getConnection()) {
          jasperPrint = JasperFillManager.fillReport(jasperReport, params, connection);
        }
      }
      return prepareReport(jasperPrint, params);
    } catch (IllegalArgumentException iae) {
      throw new JasperReportViewException(iae, ERROR_JASPER_REPORT_FORMAT_UNKNOWN,
          iae.getMessage());
    } catch (Exception e) {
      throw new JasperReportViewException(e, ERROR_JASPER_REPORT_GENERATION);
    }
  }

  /**
   * Create Jasper Report View. Create Jasper Report (".jasper" file) from bytes from Template
   * entity. Set 'Jasper' exporter parameters, JDBC data source, web application context, url to
   * file.
   *
   * @param jasperTemplate template that will be used to create a view
   * @param params         map of parameters
   * @return created jasper view.
   * @throws JasperReportViewException if there will be any problem with creating the view.
   */
  public byte[] getJasperReportsView(JasperTemplate jasperTemplate,
                                     Map<String, Object> params) throws JasperReportViewException {
    return getJasperReportsView(jasperTemplate.getData(), params);
  }

  /**
   * Render a requisition printout as a PDF.
   *
   * @param requisition requisition to render report for.
   * @return the PDF file.
   * @throws JasperReportViewException if there will be any problem with creating the report.
   */
  public byte[] getRequisitionJasperReportView(RequisitionDto requisition)
      throws JasperReportViewException {
    RequisitionReportDto reportDto = requisitionReportDtoBuilder.build(requisition);
    RequisitionTemplateDto template = requisition.getTemplate();

    Map<String, Object> params = ReportUtils.createParametersMap();
    params.put("subreport", createCustomizedRequisitionLineSubreport(
        template, requisition.getStatus()));
    params.put("template", template);
    params.put(DATE_FORMAT, dateFormat);
    params.put(DECIMAL_FORMAT, createDecimalFormat());
    params.put("currencyDecimalFormat",
        NumberFormat.getCurrencyInstance(getLocaleFromService()));

    try (InputStream inputStream = getClass().getResourceAsStream(REQUISITION_REPORT_DIR)) {
      JasperReport report = JasperCompileManager.compileReport(inputStream);
      JasperPrint jasperPrint = JasperFillManager.fillReport(report, params,
          new JRBeanCollectionDataSource(Collections.singletonList(reportDto)));
      return prepareReport(jasperPrint, params);
    } catch (IOException err) {
      throw new JasperReportViewException(err, REQUISITION_ERROR_IO, err.getMessage());
    } catch (JRException err) {
      throw new JasperReportViewException(
          err, REQUISITION_ERROR_JASPER_FILE_FORMAT, err.getMessage());
    }
  }

  protected Locale getLocaleFromService() {
    return new Locale(defaultLocale, currencyLocale);
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

  private DecimalFormat createDecimalFormat() {
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    decimalFormatSymbols.setGroupingSeparator(groupingSeparator.charAt(0));
    DecimalFormat decimalFormat = new DecimalFormat("", decimalFormatSymbols);
    decimalFormat.setGroupingSize(Integer.valueOf(groupingSize));
    return decimalFormat;
  }

  private byte[] prepareReport(JasperPrint jasperPrint, Map<String, Object> params)
      throws JRException {
    return getJasperExporter((String) params.get("format"), jasperPrint).exportReport();
  }

  private JasperExporter getJasperExporter(String format, JasperPrint jasperPrint) {
    switch (format) {
      case "pdf":
        return new JasperPdfExporter(jasperPrint);
      case "csv":
        return new JasperCsvExporter(jasperPrint);
      case "xls":
        return new JasperXlsExporter(jasperPrint);
      case "xlsx":
        return new JasperXlsxExporter(jasperPrint);
      case "html":
        return new JasperHtmlExporter(jasperPrint);
      default:
        throw new IllegalArgumentException(format);
    }
  }
}
