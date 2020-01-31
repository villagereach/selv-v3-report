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

package org.openlmis.selv.report.databuilder.referencedata;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.openlmis.selv.report.dto.external.referencedata.FacilityDto;
import org.openlmis.selv.report.dto.external.referencedata.FacilityOperatorDto;
import org.openlmis.selv.report.dto.external.referencedata.FacilityTypeDto;
import org.openlmis.selv.report.dto.external.referencedata.GeographicZoneDto;
import org.openlmis.selv.report.dto.external.referencedata.SupportedProgramDto;

public class FacilityDtoDataBuilder {

  private static int instanceNumber = 0;

  private UUID id;
  private String code;
  private String name;
  private String description;
  private Boolean active;
  private LocalDate goLiveDate;
  private LocalDate goDownDate;
  private String comment;
  private Boolean enabled;
  private Boolean openLmisAccessible;
  private List<SupportedProgramDto> supportedPrograms;
  private GeographicZoneDto geographicZone;
  private FacilityOperatorDto operator;
  private FacilityTypeDto type;

  /**
   * Constructor for setting default values to build {@link FacilityDto}.
   */
  public FacilityDtoDataBuilder() {
    instanceNumber++;
    id = UUID.randomUUID();
    code = "F" + instanceNumber;
    name = "Facility " + instanceNumber;
    description = "facility description " + instanceNumber;
    active = true;
    goLiveDate = LocalDate.now().minusYears(1);
    goDownDate = null;
    comment = "comment " + instanceNumber;
    enabled = true;
    openLmisAccessible = true;
    supportedPrograms = new ArrayList<>();
    geographicZone = new GeographicZoneDtoDataBuilder().build();
    operator = new FacilityOperatorDtoDataBuilder().build();
    type = new FacilityTypeDtoDataBuilder().build();
  }

  public FacilityDto build() {
    return new FacilityDto(id, code, name, description, active, goLiveDate, goDownDate, comment,
        enabled, openLmisAccessible, supportedPrograms, geographicZone, operator, type);
  }
}
