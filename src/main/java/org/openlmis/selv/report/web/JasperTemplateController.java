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

package org.openlmis.selv.report.web;

import static org.apache.commons.lang3.BooleanUtils.isNotFalse;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.openlmis.selv.report.domain.JasperTemplate;
import org.openlmis.selv.report.dto.JasperTemplateDto;
import org.openlmis.selv.report.exception.JasperReportViewException;
import org.openlmis.selv.report.exception.NotFoundMessageException;
import org.openlmis.selv.report.exception.ReportingException;
import org.openlmis.selv.report.i18n.JasperMessageKeys;
import org.openlmis.selv.report.repository.JasperTemplateRepository;
import org.openlmis.selv.report.service.JasperReportsViewService;
import org.openlmis.selv.report.service.JasperTemplateService;
import org.openlmis.selv.report.service.PermissionService;
import org.openlmis.selv.report.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;

@Controller
@Transactional
@RequestMapping("/api/reports/templates/common")
public class JasperTemplateController extends BaseController {
  private static final Logger LOGGER = Logger.getLogger(JasperTemplateController.class);

  @Autowired
  private JasperTemplateService jasperTemplateService;

  @Autowired
  private JasperTemplateRepository jasperTemplateRepository;

  @Autowired
  private JasperReportsViewService jasperReportsViewService;

  @Autowired
  private PermissionService permissionService;

  @Value("${dateTimeFormat}")
  private String dateTimeFormat;

  @Value("${dateFormat}")
  private String dateFormat;

  @Value("${groupingSeparator}")
  private String groupingSeparator;

  @Value("${groupingSize}")
  private String groupingSize;

  @Value("${time.zoneId}")
  private String timeZoneId;

  /**
   * Adding report templates with ".jrxml" format to database.
   *
   * @param file        File in ".jrxml" format to upload
   * @param name        Name of file in database
   * @param description Description of the file
   */
  @RequestMapping(method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  public void createJasperReportTemplate(
      @RequestPart("file") MultipartFile file, String name, String description,
      String[] requiredRights) throws ReportingException {
    permissionService.canEditReportTemplates();

    LOGGER.debug("Saving template with name: " + name);

    List<String> rightList = requiredRights == null
        ? Collections.emptyList() : Arrays.asList(requiredRights);

    JasperTemplate template = jasperTemplateService
        .saveTemplate(file, name, description, rightList);

    LOGGER.debug("Saved template with id: " + template.getId());
  }

  /**
   * Get visible templates.
   *
   * @return Templates.
   */
  @RequestMapping(method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<JasperTemplateDto> getVisibleTemplates() {
    permissionService.canViewReports();
    // we want to show only visible reports
    return JasperTemplateDto.newInstance(jasperTemplateRepository.findByVisible(true));
  }

  /**
   * Get chosen template.
   *
   * @param templateId UUID of template which we want to get
   * @return Template.
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public JasperTemplateDto getTemplate(@PathVariable("id") UUID templateId) {
    permissionService.canViewReports();

    JasperTemplate jasperTemplate = jasperTemplateRepository.findOne(templateId);
    if (jasperTemplate == null) {
      throw new NotFoundMessageException(new Message(
          JasperMessageKeys.ERROR_JASPER_TEMPLATE_NOT_FOUND, templateId));
    }

    return JasperTemplateDto.newInstance(jasperTemplate);
  }

  /**
   * Allows deleting template.
   *
   * @param templateId UUID of template which we want to delete
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTemplate(@PathVariable("id") UUID templateId) {
    permissionService.canEditReportTemplates();
    JasperTemplate jasperTemplate = jasperTemplateRepository.findOne(templateId);
    if (jasperTemplate == null) {
      throw new NotFoundMessageException(new Message(
          JasperMessageKeys.ERROR_JASPER_TEMPLATE_NOT_FOUND, templateId));
    } else {
      jasperTemplateRepository.delete(jasperTemplate);
    }
  }

  /**
   * Generate a report based on the template, the format and the request parameters.
   *
   * @param request    request (to get the request parameters)
   * @param templateId report template ID
   * @param format     report format to generate, default is PDF
   * @return the generated report
   */
  @RequestMapping(value = "/{id}/{format}", method = RequestMethod.GET)
  @ResponseBody
  public ModelAndView generateReport(
      HttpServletRequest request, @PathVariable("id") UUID templateId,
      @PathVariable("format") String format) throws JasperReportViewException {
    JasperTemplate template = jasperTemplateRepository.findOne(templateId);

    if (template == null) {
      throw new NotFoundMessageException(new Message(
          JasperMessageKeys.ERROR_JASPER_TEMPLATE_NOT_FOUND, templateId));
    }

    if (isNotFalse(template.getVisible())) {
      // if template is hidden it means that it is generated from other view than 'report view'
      // we should not check if user has right to view reports but we should check if user has
      // required rights assigned to the template
      permissionService.canViewReports();
    }

    List<String> requiredRights = template.getRequiredRights();
    permissionService.validatePermissions(
        requiredRights.toArray(new String[requiredRights.size()]));

    Map<String, Object> map = jasperTemplateService.mapRequestParametersToTemplate(
        request, template
    );
    map.putAll(jasperTemplateService.mapReportImagesToTemplate(template));

    map.put("format", format);
    map.put("dateTimeFormat", dateTimeFormat);
    map.put("dateFormat", dateFormat);
    map.put("timeZoneId", timeZoneId);
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    decimalFormatSymbols.setGroupingSeparator(groupingSeparator.charAt(0));
    DecimalFormat decimalFormat = new DecimalFormat("", decimalFormatSymbols);
    decimalFormat.setGroupingSize(Integer.parseInt(groupingSize));
    map.put("decimalFormat", decimalFormat);

    JasperReportsMultiFormatView jasperView = jasperReportsViewService
        .getJasperReportsView(template, request);

    String fileName = template.getName().replaceAll("\\s+", "_");
    String contentDisposition = "inline; filename=" + fileName + "." + format;

    jasperView
        .getContentDispositionMappings()
        .setProperty(format, contentDisposition.toLowerCase(Locale.ENGLISH));

    return new ModelAndView(jasperView, map);
  }
}
