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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;
import lombok.AllArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * This class is required to print date from status change using zone id,
 * which was not possible using {@link StatusChangeDto}.
 */
@AllArgsConstructor
public class PrintableZonedDateTime {

  private ZonedDateTime createdDate;

  /**
   * Print createdDate for display purposes.
   * @return created date
   */
  public String printDate(String zoneId) {
    Locale locale = LocaleContextHolder.getLocale();
    String datePattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
        FormatStyle.MEDIUM, FormatStyle.MEDIUM, Chronology.ofLocale(locale), locale);
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datePattern)
        .withZone(ZoneId.of(zoneId));

    return dateTimeFormatter.format(createdDate);
  }
}
