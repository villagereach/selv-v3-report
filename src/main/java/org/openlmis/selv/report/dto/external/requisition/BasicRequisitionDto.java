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

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.selv.report.dto.external.referencedata.FacilityDto;
import org.openlmis.selv.report.dto.external.referencedata.ProcessingPeriodDto;
import org.openlmis.selv.report.dto.external.referencedata.ProgramDto;

@AllArgsConstructor
@NoArgsConstructor
public class BasicRequisitionDto {
  @Getter
  @Setter
  private UUID id;

  @Getter
  @Setter
  private ZonedDateTime createdDate;

  @Getter
  @Setter
  private ZonedDateTime modifiedDate;

  @Getter
  @Setter
  private FacilityDto facility;

  @Getter
  @Setter
  private ProgramDto program;

  @Getter
  @Setter
  private ProcessingPeriodDto processingPeriod;

  @Getter
  @Setter
  private RequisitionStatusDto status;

  @Getter
  @Setter
  private Boolean emergency;

  @Getter
  @Setter
  private UUID supplyingFacility;

  @Getter
  @Setter
  private UUID supervisoryNode;

  @Getter
  @Setter
  private RequisitionTemplateDto template;

  @Getter
  @Setter
  private Map<String, StatusLogEntryDto> statusChanges = new HashMap<>();
}
