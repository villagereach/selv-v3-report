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

package org.openlmis.selv.report.service.requisition;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.selv.report.dto.external.requisition.BasicRequisitionDto;
import org.openlmis.selv.report.dto.external.requisition.RequisitionDto;
import org.openlmis.selv.report.dto.external.requisition.RequisitionStatusDto;
import org.openlmis.selv.report.utils.RequestParameters;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
public class RequisitionService extends BaseRequisitionService<RequisitionDto> {

  @Override
  protected String getUrl() {
    return "/api/requisitions/";
  }

  @Override
  protected Class<RequisitionDto> getResultClass() {
    return RequisitionDto.class;
  }

  @Override
  protected Class<RequisitionDto[]> getArrayResultClass() {
    return RequisitionDto[].class;
  }

  /**
   * Finds requisitions matching all of the provided parameters.
   */
  public Page<BasicRequisitionDto> search(UUID facility, UUID program,
      ZonedDateTime initiatedDateFrom,
      ZonedDateTime initiatedDateTo, UUID processingPeriod,
                                          UUID supervisoryNode, Set<RequisitionStatusDto>
                                              requisitionStatuses, Boolean emergency) {
    String commaDelimitedStatuses = (requisitionStatuses == null) ? null :
        requisitionStatuses.stream().map(Enum::name).collect(Collectors.joining(","));
    RequestParameters parameters = RequestParameters.init()
        .set("facility", facility)
        .set("program", program)
        .set("initiatedDateFrom", initiatedDateFrom)
        .set("initiatedDateTo", initiatedDateTo)
        .set("processingPeriod", processingPeriod)
        .set("supervisoryNode", supervisoryNode)
        .set("requisitionStatus", commaDelimitedStatuses)
        .set("emergency", emergency);

    return getPage("search", parameters, null, HttpMethod.GET, BasicRequisitionDto.class);
  }
}
