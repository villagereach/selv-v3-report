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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.openlmis.selv.report.databuilder.referencedata.FacilityDtoDataBuilder;
import org.openlmis.selv.report.databuilder.referencedata.ProcessingPeriodDtoDataBuilder;
import org.openlmis.selv.report.databuilder.referencedata.ProgramDtoDataBuilder;
import org.openlmis.selv.report.dto.external.referencedata.FacilityDto;
import org.openlmis.selv.report.dto.external.referencedata.ProcessingPeriodDto;
import org.openlmis.selv.report.dto.external.referencedata.ProgramDto;
import org.openlmis.selv.report.dto.external.requisition.RequisitionDto;
import org.openlmis.selv.report.dto.external.requisition.RequisitionLineItemDto;
import org.openlmis.selv.report.dto.external.requisition.RequisitionStatusDto;
import org.openlmis.selv.report.dto.external.requisition.RequisitionTemplateDto;
import org.openlmis.selv.report.dto.external.requisition.StatusChangeDto;

public class RequisitionDtoDataBuilder {

  private UUID id;
  private ZonedDateTime created;
  private ZonedDateTime modifiedDate;
  private String draftStatusMessage;
  private FacilityDto facility;
  private ProgramDto program;
  private ProcessingPeriodDto processingPeriod;
  private RequisitionStatusDto status;
  private Boolean emergency;
  private UUID supplyingFacility;
  private UUID supervisoryNode;
  private RequisitionTemplateDto template;
  private List<RequisitionLineItemDto> requisitionLineItems;
  private List<StatusChangeDto> statusHistory;
  private Map<String, Object> extraData;

  /**
   * Constructor for setting default values to build {@link RequisitionDto}.
   */
  public RequisitionDtoDataBuilder() {
    id = UUID.randomUUID();
    created = ZonedDateTime.now().minusDays(1);
    modifiedDate = ZonedDateTime.now().minusDays(1);
    draftStatusMessage = "draft message";
    facility = new FacilityDtoDataBuilder().build();
    program = new ProgramDtoDataBuilder().build();
    processingPeriod = new ProcessingPeriodDtoDataBuilder().build();
    status = RequisitionStatusDto.INITIATED;
    emergency = false;
    supplyingFacility = UUID.randomUUID();
    supervisoryNode = UUID.randomUUID();
    template = new RequisitionTemplateDtoDataBuilder().build();
    requisitionLineItems = new ArrayList<>();
    statusHistory = new ArrayList<>();
    extraData = new HashMap<>();
  }

  /**
   * Builds new instance of {@link RequisitionDto}.
   */
  public RequisitionDto build() {
    return new RequisitionDto(id, created, modifiedDate, draftStatusMessage, facility, program,
        processingPeriod, status, emergency, supplyingFacility, supervisoryNode, template,
        requisitionLineItems, statusHistory, extraData);
  }
}
