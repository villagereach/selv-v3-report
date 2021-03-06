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

import java.time.ZonedDateTime;
import java.util.UUID;
import org.openlmis.selv.report.dto.external.referencedata.ProcessingScheduleDto;

public class ProcessingScheduleDtoDataBuilder {

  private static int instanceNumber = 0;

  private UUID id;
  private String code;
  private String description;
  private ZonedDateTime modifiedDate;
  private String name;

  /**
   * Constructor for setting default values to build {@link ProcessingScheduleDto}.
   */
  public ProcessingScheduleDtoDataBuilder() {
    instanceNumber++;

    id = UUID.randomUUID();
    code = "PS" + instanceNumber;
    description = "processing schedule " + instanceNumber;
    modifiedDate = ZonedDateTime.now().minusYears(1);
    name = "Processing Schedule " + instanceNumber;
  }

  public ProcessingScheduleDto build() {
    return new ProcessingScheduleDto(id, code, description, modifiedDate, name);
  }
}
