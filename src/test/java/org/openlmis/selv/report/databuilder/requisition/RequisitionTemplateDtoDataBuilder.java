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

package org.openlmis.selv.report.databuilder.requisition;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.openlmis.selv.report.dto.external.requisition.RequisitionTemplateColumnDto;
import org.openlmis.selv.report.dto.external.requisition.RequisitionTemplateDto;

public class RequisitionTemplateDtoDataBuilder {

  private UUID id;
  private ZonedDateTime createdDate;
  private ZonedDateTime modifiedDate;
  private UUID programId;
  private Integer numberOfPeriodsToAverage;
  private Map<String, RequisitionTemplateColumnDto> columnsMap;

  /**
   * Constructor for setting default values to build {@link RequisitionTemplateDto}.
   */
  public RequisitionTemplateDtoDataBuilder() {
    id = UUID.randomUUID();
    createdDate = ZonedDateTime.now().minusMonths(1);
    modifiedDate = ZonedDateTime.now().minusMonths(1);
    programId = UUID.randomUUID();
    numberOfPeriodsToAverage = 3;
    columnsMap = new HashMap<>();
  }

  public RequisitionTemplateDto build() {
    return new RequisitionTemplateDto(id, createdDate, modifiedDate, programId,
        numberOfPeriodsToAverage, columnsMap);
  }
}
