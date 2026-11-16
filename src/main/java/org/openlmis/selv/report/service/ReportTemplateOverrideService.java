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

import static org.openlmis.selv.report.i18n.ReportingMessageKeys.ERROR_REPORTING_FILE_INVALID;
import static org.openlmis.selv.report.i18n.ReportingMessageKeys.ERROR_REPORTING_IO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.openlmis.selv.report.exception.ReportingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * SELV versions of printouts that the core services render through
 * {@code POST /api/reports/generate}. A core service sends its own compiled template together with
 * the report name; for the names listed here the SELV template from the classpath is filled
 * instead, so the SELV layout applies wherever the core service prints the report from. Templates
 * are compiled on first use and kept for the service lifetime, since they ship in the image.
 */
@Service
public class ReportTemplateOverrideService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ReportTemplateOverrideService.class);

  // SELV3-902: the stock event print (transaction history, and the print offered after an issue
  // or a receive) without the stock on hand column and the pack size, with the quantity unit
  static final Map<String, String> SELV_TEMPLATES =
      Collections.singletonMap("stockEvent", "/reports/stockEvent.jrxml");

  private final Map<String, byte[]> compiledTemplates = new ConcurrentHashMap<>();

  /**
   * Returns the serialized SELV template for the given report name, or the requested template when
   * SELV has no version of that report.
   *
   * @param reportName        the report name sent by the calling service
   * @param requestedTemplate the serialized template sent by the calling service
   * @return the serialized template to fill
   * @throws ReportingException when the SELV template cannot be read or compiled
   */
  public byte[] resolve(String reportName, byte[] requestedTemplate) throws ReportingException {
    String templatePath = SELV_TEMPLATES.get(reportName);
    if (templatePath == null) {
      return requestedTemplate;
    }

    byte[] template = compiledTemplates.get(reportName);
    if (template == null) {
      template = compile(templatePath);
      compiledTemplates.put(reportName, template);
      LOGGER.info("Report {} is printed with the SELV template {}", reportName, templatePath);
    }
    return template;
  }

  private byte[] compile(String templatePath) throws ReportingException {
    try (InputStream inputStream = getClass().getResourceAsStream(templatePath)) {
      if (inputStream == null) {
        throw new IOException("Template " + templatePath + " not found in the classpath");
      }
      JasperReport report = JasperCompileManager.compileReport(inputStream);

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
        out.writeObject(report);
      }
      return bos.toByteArray();
    } catch (JRException ex) {
      throw new ReportingException(ex, ERROR_REPORTING_FILE_INVALID);
    } catch (IOException ex) {
      throw new ReportingException(ex, ERROR_REPORTING_IO, ex.getMessage());
    }
  }
}
