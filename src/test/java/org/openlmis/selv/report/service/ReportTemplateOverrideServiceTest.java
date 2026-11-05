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

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.Test;
import org.openlmis.selv.report.utils.JasperReportDeserializer;

public class ReportTemplateOverrideServiceTest {
  private static final String STOCK_EVENT = "stockEvent";
  private static final byte[] CORE_TEMPLATE = "core-template-data".getBytes();

  private final ReportTemplateOverrideService service = new ReportTemplateOverrideService();

  @Test
  public void shouldReturnRequestedTemplateWhenReportHasNoSelvVersion() throws Exception {
    assertSame(CORE_TEMPLATE, service.resolve("stockCard", CORE_TEMPLATE));
  }

  @Test
  public void shouldReturnSelvStockEventTemplateWithoutStockOnHand() throws Exception {
    JasperReport report = deserialize(service.resolve(STOCK_EVENT, CORE_TEMPLATE));

    assertEquals(STOCK_EVENT, report.getName());
    List<String> fields = Arrays.stream(report.getFields())
        .map(JRField::getName)
        .collect(toList());
    assertFalse(fields.contains("stockonhand"));
    assertTrue(fields.contains("quantity"));
    assertTrue(fields.contains("netcontent"));
    assertFalse(report.getQuery().getText().contains("running_soh"));
  }

  @Test
  public void shouldDeclareParametersSentByStockmanagement() throws Exception {
    JasperReport report = deserialize(service.resolve(STOCK_EVENT, CORE_TEMPLATE));

    List<String> parameters = Arrays.stream(report.getParameters())
        .filter(parameter -> !parameter.isSystemDefined())
        .map(JRParameter::getName)
        .collect(toList());
    assertTrue(parameters.containsAll(Arrays.asList("stockEventId", "showInDoses", "dateFormat",
        "decimalFormat", "headerTemplate")));
  }

  @Test
  public void shouldCompileSelvTemplateOnce() throws Exception {
    assertSame(service.resolve(STOCK_EVENT, CORE_TEMPLATE),
        service.resolve(STOCK_EVENT, CORE_TEMPLATE));
  }

  private JasperReport deserialize(byte[] template) throws Exception {
    return new JasperReportDeserializer().deserialize(template);
  }
}
