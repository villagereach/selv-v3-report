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
import java.util.UUID;
import org.openlmis.selv.report.dto.external.referencedata.ProcessingPeriodDto;
import org.openlmis.selv.report.dto.external.referencedata.ProcessingScheduleDto;

public class ProcessingPeriodDtoDataBuilder {

  private static int instanceNumber = 0;

  private UUID id;
  private ProcessingScheduleDto processingSchedule;
  private String name;
  private String description;
  private LocalDate startDate;
  private LocalDate endDate;
  private Integer durationInMonths;

  /**
   * Constructor for setting default values to build {@link ProcessingPeriodDto}.
   */
  public ProcessingPeriodDtoDataBuilder() {
    instanceNumber++;

    id = UUID.randomUUID();
    processingSchedule = new ProcessingScheduleDtoDataBuilder().build();
    name = "Processing Period " + instanceNumber;
    description = "period " + instanceNumber;
    startDate = LocalDate.of(2019, 1, 1);
    endDate = LocalDate.of(2019, 1, 31);
    durationInMonths = 1;
  }

  public ProcessingPeriodDto build() {
    return new ProcessingPeriodDto(id, processingSchedule, name, description, startDate, endDate,
        durationInMonths);
  }
}
