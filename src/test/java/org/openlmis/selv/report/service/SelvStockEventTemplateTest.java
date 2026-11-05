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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintFrame;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JRTextField;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.SimpleJasperReportsContext;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.junit.Test;
import org.openlmis.selv.report.utils.JasperReportDeserializer;

/**
 * Checks what the SELV stock event template prints (SELV3-902), filled the way the generate
 * endpoint fills it but from an in-memory data source.
 */
public class SelvStockEventTemplateTest {
  private static final byte[] CORE_TEMPLATE = "core-template-data".getBytes();

  private final ReportTemplateOverrideService service = new ReportTemplateOverrideService();

  @Test
  public void shouldNotPrintStockOnHandOrPackSize() throws Exception {
    JasperReport report = selvTemplate();

    List<String> expressions = new ArrayList<>();
    collectExpressions(report.getTitle(), expressions);
    collectExpressions(report.getColumnHeader(), expressions);
    collectExpressions(report.getPageFooter(), expressions);
    for (JRBand band : report.getDetailSection().getBands()) {
      collectExpressions(band, expressions);
    }
    for (JRGroup group : report.getGroups()) {
      for (JRBand band : group.getGroupHeaderSection().getBands()) {
        collectExpressions(band, expressions);
      }
    }

    assertFalse(expressions.isEmpty());
    for (String expression : expressions) {
      assertFalse(expression, expression.contains("stockonhand"));
      assertFalse(expression, expression.contains("report.column.stockOnHand"));
      assertFalse(expression, expression.contains("report.header.packSize"));
    }
  }

  @Test
  public void shouldPrintQuantitiesInPacksUnderPacksHeader() throws Exception {
    List<String> texts = fill(false, Arrays.asList(line("BCG", 20, 45), line("SYRINGE", 0, 45)));

    assertTrue(texts.contains("Quantity (Packs)"));
    assertTrue(texts.contains("2 ( +5 )"));
    // a net content below 1 counts as 1, like the core stock card print, instead of failing
    assertTrue(texts.contains("45 ( +0 )"));
  }

  @Test
  public void shouldPrintQuantitiesInDosesUnderDosesHeader() throws Exception {
    List<String> texts = fill(true, Collections.singletonList(line("BCG", 20, 1000)));

    assertTrue(texts.contains("Quantity (Doses)"));
    assertTrue(texts.contains("1,000"));
  }

  private JasperReport selvTemplate() throws Exception {
    return new JasperReportDeserializer().deserialize(
        service.resolve("stockEvent", CORE_TEMPLATE));
  }

  private void collectExpressions(JRBand band, List<String> expressions) {
    if (band == null) {
      return;
    }
    for (JRElement element : band.getElements()) {
      if (element instanceof JRTextField) {
        expressions.add(((JRTextField) element).getExpression().getText());
      }
    }
  }

  private List<String> fill(boolean showInDoses, List<Map<String, ?>> lines) throws Exception {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    symbols.setGroupingSeparator(',');
    DecimalFormat decimalFormat = new DecimalFormat("", symbols);
    decimalFormat.setGroupingSize(3);

    Map<String, Object> params = new HashMap<>();
    params.put("showInDoses", showInDoses);
    params.put("decimalFormat", decimalFormat);
    params.put("dateFormat", "dd/MM/yyyy");
    params.put("dateTimeFormat", "dd/MM/yyyy HH:mm:ss");
    params.put("timeZoneId", "UTC");
    params.put(JRParameter.REPORT_RESOURCE_BUNDLE,
        ResourceBundle.getBundle("resourceBundles/report_translations", Locale.ENGLISH));

    // the test JVM may not have the DejaVu fonts the template names
    SimpleJasperReportsContext context = new SimpleJasperReportsContext();
    context.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
    JasperReport report = selvTemplate();
    JasperPrint print = JasperFillManager.getInstance(context)
        .fill(report, params, new JRMapCollectionDataSource(lines));

    List<String> texts = new ArrayList<>();
    for (JRPrintPage page : print.getPages()) {
      collectTexts(page.getElements(), texts);
    }
    return texts;
  }

  private void collectTexts(List<JRPrintElement> elements, List<String> texts) {
    for (JRPrintElement element : elements) {
      if (element instanceof JRPrintText) {
        texts.add(((JRPrintText) element).getFullText());
      } else if (element instanceof JRPrintFrame) {
        collectTexts(((JRPrintFrame) element).getElements(), texts);
      }
    }
  }

  private Map<String, ?> line(String code, int netContent, int quantity) {
    Map<String, Object> line = new HashMap<>();
    line.put("productcode", code);
    line.put("productname", code);
    line.put("netcontent", netContent);
    line.put("lotcode", "LOT-" + code);
    line.put("occurreddate", "2026-07-17");
    line.put("destination", "DDV - MOATIZE");
    line.put("reasonname", "Saida de Stock");
    line.put("quantity", quantity);
    return line;
  }
}
