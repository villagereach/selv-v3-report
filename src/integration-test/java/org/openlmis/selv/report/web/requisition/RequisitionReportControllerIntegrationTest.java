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

package org.openlmis.selv.report.web.requisition;

import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.selv.report.databuilder.requisition.RequisitionDtoDataBuilder;
import org.openlmis.selv.report.service.requisition.RequisitionService;
import org.openlmis.selv.report.web.BaseWebIntegrationTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class RequisitionReportControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String REPORT_URL = "/api/reports/requisitions/{id}/print";

  @MockBean
  private RequisitionService requisitionService;

  @Before
  public void setUp() {
    mockUserAuthenticated();
  }

  @Test
  public void shouldPrintRequisition() {
    UUID id = UUID.randomUUID();
    given(requisitionService.findOne(id))
        .willReturn(new RequisitionDtoDataBuilder().build());

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_PDF_VALUE)
        .when()
        .pathParam("id", id)
        .get(REPORT_URL)
        .then()
        .statusCode(500);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
