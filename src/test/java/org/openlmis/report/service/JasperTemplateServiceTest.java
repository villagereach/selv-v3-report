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

package org.openlmis.report.service;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.report.i18n.AuthorizationMessageKeys.ERROR_RIGHT_NOT_FOUND;
import static org.openlmis.report.i18n.ReportingMessageKeys.ERROR_REPORTING_FILE_EMPTY;
import static org.openlmis.report.i18n.ReportingMessageKeys.ERROR_REPORTING_FILE_INCORRECT_TYPE;
import static org.openlmis.report.i18n.ReportingMessageKeys.ERROR_REPORTING_FILE_INVALID;
import static org.openlmis.report.i18n.ReportingMessageKeys.ERROR_REPORTING_FILE_MISSING;
import static org.openlmis.report.i18n.ReportingMessageKeys.ERROR_REPORTING_PARAMETER_MISSING;
import static org.openlmis.report.i18n.ReportingMessageKeys.ERROR_REPORTING_TEMPLATE_EXIST;
import static org.openlmis.report.service.JasperTemplateService.REPORT_TYPE_PROPERTY;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.openlmis.report.domain.JasperTemplate;
import org.openlmis.report.domain.JasperTemplateParameter;
import org.openlmis.report.domain.ReportImage;
import org.openlmis.report.dto.external.referencedata.RightDto;
import org.openlmis.report.exception.JasperReportViewException;
import org.openlmis.report.exception.ReportingException;
import org.openlmis.report.exception.ValidationMessageException;
import org.openlmis.report.i18n.ReportImageMessageKeys;
import org.openlmis.report.repository.JasperTemplateRepository;
import org.openlmis.report.repository.ReportImageRepository;
import org.openlmis.report.service.referencedata.RightReferenceDataService;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(BlockJUnit4ClassRunner.class)
@PrepareForTest({JasperTemplateService.class, JasperCompileManager.class})
@SuppressWarnings("PMD.TooManyMethods")
public class JasperTemplateServiceTest {

  @Mock
  private JasperTemplateRepository jasperTemplateRepository;

  @Mock
  private RightReferenceDataService rightReferenceDataService;

  @Mock
  private ReportImageRepository reportImageRepository;

  @InjectMocks
  private JasperTemplateService jasperTemplateService;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private static final String NAME_OF_FILE = "report.jrxml";
  private static final String DISPLAY_NAME = "displayName";
  private static final String PARAM_DISPLAY_NAME = "Param Display Name";
  private static final String REQUIRED = "required";
  private static final String PARAM1 = "param1";
  private static final String PARAM2 = "param2";
  private static final String PARAM3 = "param3";
  private static final String PARAM4 = "param4";
  private static final String IMAGE_NAME = "image";
  
  private HttpServletRequest request;
  private JasperTemplate template;
  
  @Before
  public void setUp() {
    request = mock(HttpServletRequest.class);
    template = mock(JasperTemplate.class);
  }

  @Test
  public void shouldSaveValidNonExistentTemplate() throws Exception {
    // given
    given(jasperTemplateRepository.findByName(anyString()))
        .willReturn(null);

    // when
    testSaveTemplate(DISPLAY_NAME);
  }

  @Test
  public void shouldUpdateValidExistentTemplate() throws Exception {
    // given
    UUID oldId = UUID.randomUUID();

    JasperTemplate oldTemplate = new JasperTemplate();
    oldTemplate.setName(DISPLAY_NAME);
    oldTemplate.setId(oldId);
    oldTemplate.setRequiredRights(new ArrayList<>());

    given(jasperTemplateRepository.findByName(anyString()))
        .willReturn(oldTemplate);

    // when
    JasperTemplate template = testSaveTemplate(DISPLAY_NAME);

    // then
    assertEquals(template.getId(), oldId);
  }

  private JasperTemplate testSaveTemplate(String name) throws ReportingException {
    JasperTemplateService service = spy(jasperTemplateService);
    MultipartFile file = mock(MultipartFile.class);
    String description = "description";
    List<String> requiredRights = Collections.singletonList("USERS_MANAGE");

    given(rightReferenceDataService.findRight(requiredRights.get(0)))
        .willReturn(new RightDto());

    // validating and saving file is checked by other tests
    doNothing().when(service)
        .validateFileAndSaveTemplate(any(JasperTemplate.class), eq(file));

    // when
    JasperTemplate resultTemplate = service.saveTemplate(file, name, description, requiredRights);

    // then
    assertEquals(name, resultTemplate.getName());
    assertEquals(description, resultTemplate.getDescription());
    assertEquals(requiredRights, resultTemplate.getRequiredRights());

    return resultTemplate;
  }

  @Test
  public void shouldThrowErrorIfReportRequiredRightDoesNotExist() throws Exception {
    expectedException.expect(ValidationMessageException.class);
    expectedException.expectMessage(ERROR_RIGHT_NOT_FOUND);

    // given
    String rejectedRight = "REPORTS_DELETE";
    given(rightReferenceDataService.findRight(rejectedRight)).willReturn(null);

    // when
    jasperTemplateService.saveTemplate(null, null, null, Collections.singletonList(rejectedRight));
  }
  
  @Test
  public void shouldThrowErrorIfFileNotOfTypeJasperXml() throws Exception {
    expectedException.expect(ReportingException.class);
    expectedException.expectMessage(ERROR_REPORTING_FILE_INCORRECT_TYPE);

    MockMultipartFile file = new MockMultipartFile("report.pdf", new byte[1]);
    jasperTemplateService.validateFileAndInsertTemplate(new JasperTemplate(), file);
  }

  @Test
  public void shouldThrowErrorIfFileEmpty() throws Exception {
    expectedException.expect(ReportingException.class);
    expectedException.expectMessage(ERROR_REPORTING_FILE_EMPTY);
    MockMultipartFile file = new MockMultipartFile(
        NAME_OF_FILE, NAME_OF_FILE, "", new byte[0]);

    jasperTemplateService.validateFileAndInsertTemplate(new JasperTemplate(), file);
  }

  @Test
  public void shouldThrowErrorIfFileNotPresent() throws Exception {
    expectedException.expect(ReportingException.class);
    expectedException.expectMessage(ERROR_REPORTING_FILE_MISSING);

    jasperTemplateService.validateFileAndInsertTemplate(new JasperTemplate(), null);
  }

  @Test
  public void shouldThrowErrorIfFileIsInvalid() throws Exception {
    expectedException.expect(ReportingException.class);
    expectedException.expectMessage(ERROR_REPORTING_FILE_INVALID);

    jasperTemplateService.validateFileAndInsertTemplate(new JasperTemplate(),
        new MockMultipartFile(NAME_OF_FILE, NAME_OF_FILE, "", new byte[1]));
  }

  @Test
  public void shouldThrowErrorIfTemplateNameAlreadyExists() throws Exception {
    JasperTemplate jasperTemplate = new JasperTemplate();
    jasperTemplate.setName("Name");
    expectedException.expect(ReportingException.class);
    expectedException.expectMessage(ERROR_REPORTING_TEMPLATE_EXIST);
    when(jasperTemplateRepository.findByName(Matchers.anyObject())).thenReturn(jasperTemplate);

    jasperTemplateService.validateFileAndInsertTemplate(jasperTemplate, null);
  }

  @Test
  public void shouldThrowErrorIfImageDoesNotExist() throws Exception {
    expectedException.expect(ReportingException.class);
    expectedException.expectMessage(ReportImageMessageKeys.ERROR_NOT_FOUND_WITH_NAME);

    MultipartFile file = mock(MultipartFile.class);
    when(file.getOriginalFilename()).thenReturn(NAME_OF_FILE);

    mockStatic(JasperCompileManager.class);
    JasperReport report = mock(JasperReport.class);
    InputStream inputStream = mock(InputStream.class);
    when(file.getInputStream()).thenReturn(inputStream);

    JRParameter param1 = mock(JRParameter.class);

    when(report.getParameters()).thenReturn(new JRParameter[]{param1});
    when(JasperCompileManager.compileReport(inputStream)).thenReturn(report);

    when(param1.isForPrompting()).thenReturn(false);
    when(param1.isSystemDefined()).thenReturn(false);
    when(param1.getName()).thenReturn(IMAGE_NAME);
    when(param1.getValueClassName()).thenReturn("java.awt.Image");

    when(reportImageRepository.findByName(IMAGE_NAME)).thenReturn(null);

    JasperTemplate jasperTemplate = new JasperTemplate();

    jasperTemplateService.validateFileAndInsertTemplate(jasperTemplate, file);

    verify(jasperTemplateService, never()).saveWithParameters(jasperTemplate);
  }

  @Test
  public void shouldThrowErrorIfDisplayNameOfParameterIsMissing() throws Exception {
    expectedException.expect(ReportingException.class);
    expectedException.expect(hasProperty("params", arrayContaining("displayName")));
    expectedException.expectMessage(ERROR_REPORTING_PARAMETER_MISSING);

    MultipartFile file = mock(MultipartFile.class);
    when(file.getOriginalFilename()).thenReturn(NAME_OF_FILE);

    mockStatic(JasperCompileManager.class);
    JasperReport report = mock(JasperReport.class);
    InputStream inputStream = mock(InputStream.class);
    when(file.getInputStream()).thenReturn(inputStream);

    JRParameter param1 = mock(JRParameter.class);
    JRParameter param2 = mock(JRParameter.class);
    JRPropertiesMap propertiesMap = mock(JRPropertiesMap.class);

    when(report.getParameters()).thenReturn(new JRParameter[]{param1, param2});
    when(JasperCompileManager.compileReport(inputStream)).thenReturn(report);
    when(param1.getPropertiesMap()).thenReturn(propertiesMap);
    when(param1.isForPrompting()).thenReturn(true);
    when(param2.isForPrompting()).thenReturn(true);

    String[] propertyNames = {"name1"};
    when(propertiesMap.getPropertyNames()).thenReturn(propertyNames);
    when(propertiesMap.getProperty(DISPLAY_NAME)).thenReturn(null);
    JasperTemplate jasperTemplate = new JasperTemplate();

    jasperTemplateService.validateFileAndInsertTemplate(jasperTemplate, file);

    verify(jasperTemplateService, never()).saveWithParameters(jasperTemplate);
  }

  @Test
  public void shouldValidateFileAndSetData() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    when(file.getOriginalFilename()).thenReturn(NAME_OF_FILE);

    mockStatic(JasperCompileManager.class);
    JasperReport report = mock(JasperReport.class);
    InputStream inputStream = mock(InputStream.class);
    when(file.getInputStream()).thenReturn(inputStream);

    JRParameter param1 = mock(JRParameter.class);
    JRParameter param2 = mock(JRParameter.class);
    JRParameter param3 = mock(JRParameter.class);
    JRPropertiesMap propertiesMap = mock(JRPropertiesMap.class);
    JRExpression jrExpression = mock(JRExpression.class);

    String[] propertyNames = {DISPLAY_NAME};
    when(report.getParameters()).thenReturn(new JRParameter[]{param1, param2, param3});
    when(report.getProperty(REPORT_TYPE_PROPERTY)).thenReturn("test type");
    when(JasperCompileManager.compileReport(inputStream)).thenReturn(report);
    when(propertiesMap.getPropertyNames()).thenReturn(propertyNames);
    when(propertiesMap.getProperty(DISPLAY_NAME)).thenReturn(PARAM_DISPLAY_NAME);
    when(propertiesMap.getProperty(REQUIRED)).thenReturn("true");
    when(propertiesMap.getProperty("options")).thenReturn("option 1,opt\\,ion 2");

    when(param1.getPropertiesMap()).thenReturn(propertiesMap);
    when(param1.getValueClassName()).thenReturn("java.lang.String");
    when(param1.getName()).thenReturn("name");
    when(param1.isForPrompting()).thenReturn(true);
    when(param1.getDescription()).thenReturn("desc");
    when(param1.getDefaultValueExpression()).thenReturn(jrExpression);
    when(jrExpression.getText()).thenReturn("text");

    when(param2.getPropertiesMap()).thenReturn(propertiesMap);
    when(param2.getValueClassName()).thenReturn("java.lang.Integer");
    when(param2.getName()).thenReturn("name");
    when(param2.isForPrompting()).thenReturn(true);
    when(param2.getDescription()).thenReturn("desc");
    when(param2.getDefaultValueExpression()).thenReturn(jrExpression);

    when(param3.getValueClassName()).thenReturn("java.awt.Image");
    when(param3.isForPrompting()).thenReturn(false);
    when(param3.isSystemDefined()).thenReturn(false);
    when(param3.getName()).thenReturn(IMAGE_NAME);
    ReportImage reportImage = mock(ReportImage.class);
    when(reportImageRepository.findByName(IMAGE_NAME)).thenReturn(reportImage);

    ByteArrayOutputStream byteOutputStream = mock(ByteArrayOutputStream.class);
    whenNew(ByteArrayOutputStream.class).withAnyArguments().thenReturn(byteOutputStream);
    ObjectOutputStream objectOutputStream = spy(new ObjectOutputStream(byteOutputStream));
    whenNew(ObjectOutputStream.class).withArguments(byteOutputStream)
        .thenReturn(objectOutputStream);
    doNothing().when(objectOutputStream).writeObject(report);
    byte[] byteData = new byte[1];
    when(byteOutputStream.toByteArray()).thenReturn(byteData);
    JasperTemplate jasperTemplate = new JasperTemplate();

    jasperTemplateService.validateFileAndInsertTemplate(jasperTemplate, file);

    verify(jasperTemplateRepository).save(jasperTemplate);

    assertEquals("test type", jasperTemplate.getType());
    assertThat(jasperTemplate.getTemplateParameters().get(0).getDisplayName(),
        is(PARAM_DISPLAY_NAME));
    assertThat(jasperTemplate.getTemplateParameters().get(0).getDescription(), is("desc"));
    assertThat(jasperTemplate.getTemplateParameters().get(0).getName(), is("name"));
    assertThat(jasperTemplate.getTemplateParameters().get(0).getRequired(), is(true));
    assertThat(jasperTemplate.getTemplateParameters().get(0).getOptions(), contains("option 1",
            "opt,ion 2"));
    assertEquals(1, jasperTemplate.getReportImages().size());
    assertTrue(jasperTemplate.getReportImages().contains(reportImage));
  }

  @Test
  public void shouldValidateFileAndSetDataIfDefaultValueExpressionIsNull() throws Exception {
    MultipartFile file = mock(MultipartFile.class);
    when(file.getOriginalFilename()).thenReturn(NAME_OF_FILE);

    mockStatic(JasperCompileManager.class);
    JasperReport report = mock(JasperReport.class);
    InputStream inputStream = mock(InputStream.class);
    when(file.getInputStream()).thenReturn(inputStream);

    JRParameter param1 = mock(JRParameter.class);
    JRParameter param2 = mock(JRParameter.class);
    JRPropertiesMap propertiesMap = mock(JRPropertiesMap.class);
    JRExpression jrExpression = mock(JRExpression.class);
    String[] propertyNames = {DISPLAY_NAME};

    when(report.getParameters()).thenReturn(new JRParameter[]{param1, param2});
    when(JasperCompileManager.compileReport(inputStream)).thenReturn(report);
    when(propertiesMap.getPropertyNames()).thenReturn(propertyNames);
    when(propertiesMap.getProperty(DISPLAY_NAME)).thenReturn(PARAM_DISPLAY_NAME);

    when(param1.getPropertiesMap()).thenReturn(propertiesMap);
    when(param1.getValueClassName()).thenReturn("java.lang.String");
    when(param1.isForPrompting()).thenReturn(true);
    when(param1.getDefaultValueExpression()).thenReturn(jrExpression);
    when(jrExpression.getText()).thenReturn("text");

    when(param2.getPropertiesMap()).thenReturn(propertiesMap);
    when(param2.getValueClassName()).thenReturn("java.lang.Integer");
    when(param2.isForPrompting()).thenReturn(true);
    when(param2.getDefaultValueExpression()).thenReturn(null);

    ByteArrayOutputStream byteOutputStream = mock(ByteArrayOutputStream.class);
    whenNew(ByteArrayOutputStream.class).withAnyArguments().thenReturn(byteOutputStream);
    ObjectOutputStream objectOutputStream = spy(new ObjectOutputStream(byteOutputStream));
    whenNew(ObjectOutputStream.class).withArguments(byteOutputStream)
        .thenReturn(objectOutputStream);
    doNothing().when(objectOutputStream).writeObject(report);
    byte[] byteData = new byte[1];
    when(byteOutputStream.toByteArray()).thenReturn(byteData);
    JasperTemplate jasperTemplate = new JasperTemplate();

    jasperTemplateService.validateFileAndInsertTemplate(jasperTemplate, file);

    verify(jasperTemplateRepository).save(jasperTemplate);
    assertThat(jasperTemplate.getTemplateParameters().get(0).getDisplayName(),
        is(PARAM_DISPLAY_NAME));
  }

  @Test
  public void mapRequestParametersToTemplateShouldReturnEmptyMapIfNoParameters() {
    when(request.getParameterMap()).thenReturn(Collections.emptyMap());
    when(template.getTemplateParameters()).thenReturn(null);

    Map<String, Object> resultMap = jasperTemplateService.mapRequestParametersToTemplate(request,
        template);

    assertThat(resultMap.size(), is(0));
  }

  @Test
  public void mapRequestParametersToTemplateShouldReturnEmptyMapIfNoTemplateParameters() {
    when(request.getParameterMap()).thenReturn(Collections.singletonMap("key1",
        new String[]{"value1"}));
    when(template.getTemplateParameters()).thenReturn(null);

    Map<String, Object> resultMap = jasperTemplateService.mapRequestParametersToTemplate(request,
        template);

    assertThat(resultMap.size(), is(0));
  }

  @Test
  public void mapRequestParametersToTemplateShouldReturnEmptyMapIfNoRequestParameters() {
    JasperTemplateParameter templateParameter = new JasperTemplateParameter();
    templateParameter.setTemplate(template);
    templateParameter.setName(PARAM1);

    when(request.getParameterMap()).thenReturn(Collections.emptyMap());
    when(template.getTemplateParameters()).thenReturn(Collections.singletonList(templateParameter));

    Map<String, Object> resultMap = jasperTemplateService.mapRequestParametersToTemplate(request,
        template);

    assertThat(resultMap.size(), is(0));
  }

  @Test
  public void mapRequestParametersToTemplateShouldReturnMatchingParameters() {
    JasperTemplateParameter param1 = new JasperTemplateParameter();
    param1.setTemplate(template);
    param1.setName(PARAM1);

    JasperTemplateParameter param2 = new JasperTemplateParameter();
    param2.setTemplate(template);
    param2.setName(PARAM2);

    Map<String, String[]> requestParameterMap = new HashMap<>();
    requestParameterMap.put(PARAM1, new String[]{"value1"});
    requestParameterMap.put(PARAM3, new String[]{"value3"});

    List<JasperTemplateParameter> templateParameterList = Arrays.asList(param1, param2);

    when(request.getParameterMap()).thenReturn(requestParameterMap);
    when(template.getTemplateParameters()).thenReturn(templateParameterList);

    Map<String, Object> resultMap = jasperTemplateService.mapRequestParametersToTemplate(request,
        template);

    assertThat(resultMap.size(), is(1));
    assertTrue(resultMap.containsKey(PARAM1));
    assertEquals("value1", resultMap.get(PARAM1));
  }

  @Test
  public void mapRequestParametersToTemplateShouldNotReturnBlankNullOrUndefinedStringValues() {
    JasperTemplateParameter param1 = new JasperTemplateParameter();
    param1.setTemplate(template);
    param1.setName(PARAM1);

    JasperTemplateParameter param2 = new JasperTemplateParameter();
    param2.setTemplate(template);
    param2.setName(PARAM2);

    JasperTemplateParameter param3 = new JasperTemplateParameter();
    param3.setTemplate(template);
    param3.setName(PARAM3);

    JasperTemplateParameter param4 = new JasperTemplateParameter();
    param4.setTemplate(template);
    param4.setName(PARAM4);

    Map<String, String[]> requestParameterMap = new HashMap<>();
    requestParameterMap.put(PARAM1, new String[]{""});
    requestParameterMap.put(PARAM2, new String[]{" "});
    requestParameterMap.put(PARAM3, new String[]{"null"});
    requestParameterMap.put(PARAM4, new String[]{"undefined"});

    List<JasperTemplateParameter> templateParameterList =
        Arrays.asList(param1, param2, param3, param4);

    when(request.getParameterMap()).thenReturn(requestParameterMap);
    when(template.getTemplateParameters()).thenReturn(templateParameterList);

    Map<String, Object> resultMap = jasperTemplateService.mapRequestParametersToTemplate(request,
        template);

    assertThat(resultMap.size(), is(0));
  }


  @Test
  public void mapReportImagesToTemplateShouldReturnEmptyMapIfNoImages()
      throws JasperReportViewException {
    when(template.getReportImages()).thenReturn(null);

    Map<String, BufferedImage> resultMap = jasperTemplateService
        .mapReportImagesToTemplate(template);

    assertThat(resultMap.size(), is(0));
  }

  @Test
  public void mapReportImagesToTemplateShouldReturnMatchingImages()
      throws JasperReportViewException, IOException {
    BufferedImage image = new BufferedImage(1, 1, TYPE_INT_RGB);
    byte[] expectedData = convertImageToByteArray(image);

    ReportImage reportImage = new ReportImage();
    reportImage.setName(IMAGE_NAME);
    reportImage.setData(expectedData);

    Set<ReportImage> images = new HashSet<>();
    images.add(reportImage);

    when(template.getReportImages()).thenReturn(images);

    Map<String, BufferedImage> resultMap = jasperTemplateService
        .mapReportImagesToTemplate(template);

    assertThat(resultMap.size(), is(1));
    assertTrue(resultMap.containsKey(IMAGE_NAME));
    assertTrue(Arrays.equals(expectedData,
        convertImageToByteArray(resultMap.get(IMAGE_NAME))));
  }

  private byte[] convertImageToByteArray(BufferedImage image) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ImageIO.write(image, "png", os);
    final byte[] expectedData = os.toByteArray();
    os.close();
    return expectedData;
  }
}
