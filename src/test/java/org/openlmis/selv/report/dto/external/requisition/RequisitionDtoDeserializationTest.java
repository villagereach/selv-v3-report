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

package org.openlmis.selv.report.dto.external.requisition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

public class RequisitionDtoDeserializationTest {

  private final ObjectMapper objectMapper =
      new MappingJackson2HttpMessageConverter().getObjectMapper();

  @Test
  public void shouldDeserializeRequisitionReturnedByRequisitionService() throws IOException {
    RequisitionDto requisition;
    try (InputStream json = getClass().getResourceAsStream("/requisition/requisition.json")) {
      requisition = objectMapper.readValue(json, RequisitionDto.class);
    }

    assertNotNull(requisition.getTemplate());
    assertFalse(requisition.getTemplate().getColumnsMap().isEmpty());
    RequisitionTemplateColumnDto column = requisition.getTemplate().getColumnsMap()
        .get("requestedQuantity");
    assertEquals("requestedQuantity", column.getName());
    assertFalse(requisition.getRequisitionLineItems().isEmpty());
  }
}
